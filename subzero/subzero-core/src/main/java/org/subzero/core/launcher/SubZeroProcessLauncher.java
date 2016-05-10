package org.subzero.core.launcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.subzero.core.bean.ProcessReport;
import org.subzero.core.bean.SubTitleInfo;
import org.subzero.core.bean.TvShowInfo;
import org.subzero.core.bean.VideoFileToProcess;
import org.subzero.core.helper.FileHelper;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.helper.TvShowInfoHelper;
import org.subzero.core.postprocess.PostProcessLauncher;
import org.subzero.core.subchecker.SubCheckerBus;
import org.subzero.core.subleecher.SubLeecherBus;

/**
 * SubZero Process Launcher
 * @author Julien
 *
 */
public class SubZeroProcessLauncher {

	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubZeroProcessLauncher.class);
	
	// Counters
	int nbFileToProcess = 0;
	int nbFileSuccess = 0;
	int nbFileNoSub = 0;
	int nbFileNoPostProcess = 0;
	
	/**
	 * List of input video files to process
	 */
	List<VideoFileToProcess> inputVideoFileList = null;
	
	/**
	 * Constructor
	 */
	public SubZeroProcessLauncher() {
		super();
	}
	
	/**
	 * Launch SubZero process
	 * @return Process Report
	 * @throws Exception
	 */
	public ProcessReport launchProcess() throws Exception {
		ProcessReport report = null;
		
		try {					
			log.debug("****************************************");
			log.debug(String.format("Begin SubZero Process on working folder '%s' ...", PropertiesHelper.getWorkingFolderPath()));
			
			// Get video files to process in Working Folder
			inputVideoFileList = null;
			populateVideoFileList(PropertiesHelper.getWorkingFolderPath());
			log.debug(String.format("Number of video files to process (evaluation) : %s", inputVideoFileList.size()));
			
			// Shuffle mode : randomize processing order of all files to process
			if (PropertiesHelper.getWorkingFolderShuffleMode()) {
				Collections.shuffle(inputVideoFileList);
			}
			
			// Process each video file
			for (VideoFileToProcess inputVideoFile : inputVideoFileList) {
				boolean fileToProcess = false;
				boolean fileSuccess = false;
				boolean fileNoSub = false;
				boolean fileNoPostProcess = false;
				
				// Process each subtitle language
				for (String subLanguage : PropertiesHelper.getSubLeecherLanguages()) {
					// Populate TV Show Info from video file
					TvShowInfo tvShowInfo = TvShowInfoHelper.populateTvShowInfo(inputVideoFile.getVideoFileName(), true);
					
					// Initialize SubChecker Bus and 
					SubCheckerBus subCheckerBus = new SubCheckerBus(inputVideoFile.getWorkingFolderPath(), tvShowInfo, subLanguage);
					
					// 1) Check if the video file name contains a bypass keyword for the given language (e.g. .FRENCH. ; .vostfr. ; ...)
					if (subCheckerBus.checkFilenameBypassKeyword()) {
						// Internal subtitle found : nothing to do
						log.debug(String.format("Video file '%s' (language %s) must be bypassed : nothing to do", inputVideoFile.getVideoFileName(), subLanguage));
						fileToProcess = false;
						fileSuccess = false;
						fileNoSub = false;
						fileNoPostProcess = false;
						break;
					}
					
					// 2) Launch SubChecker Bus for internal subtitles ...
					log.debug(String.format("Processing video file '%s' (language %s) : checking internal subtitle...", inputVideoFile.getVideoFileName(), subLanguage));
					if (subCheckerBus.launchInternalSubCheckerBus()) {
						// Internal subtitle found : nothing to do
						log.debug(String.format("Internal subtitle file (language %s) already exists : nothing to do", subLanguage));
						fileToProcess = false;
						fileSuccess = false;
						fileNoSub = false;
						fileNoPostProcess = false;
						break;
					}					
					
					// 3) Check if an external subtitle file exists in working folder
					SubTitleInfo subTitleInfo = subCheckerBus.checkExternalSub();															
					if (subTitleInfo != null) {
						log.debug(String.format("External subtitle file '%s' (language %s) already exists : no leech)", subTitleInfo.getSubFileName(), 
								subTitleInfo.getLanguage()));
					}
					else {		 
						// 4) Initialize SubLeecher Bus
						SubLeecherBus subLeecherBus = new SubLeecherBus(inputVideoFile.getWorkingFolderPath(), tvShowInfo, subLanguage);
						
						// Launch SubLeecher Bus ...
						log.info(String.format("Processing video file '%s' (language %s) : searching for subtitle...", inputVideoFile.getVideoFileName(), subLanguage));
						subTitleInfo = subLeecherBus.launchSubLeecherBus(PropertiesHelper.getSubLeecherLanguages());
					}
					
					if (subTitleInfo == null) {
						// No subtitle leeched : try next language in priority order for this video file
						log.warn(String.format("No subtitle file found for file '%s' (language %s) ! Try later...", inputVideoFile.getVideoFileName(), subLanguage));
						nbFileNoSub++;
						fileToProcess = true;
						fileSuccess = false;
						fileNoSub = true;
						fileNoPostProcess = false;
					}
					else 
					{
						// 5) Launch PostProcessor ...
						log.info(String.format("Processing video file '%s' (language %s) : post-processing ...", inputVideoFile.getVideoFileName(), subLanguage));
						
						// Build output folder path
						String outputFolderPath = PropertiesHelper.getOutputFolderPath(tvShowInfo.getSerie(), tvShowInfo.getSeason().toString());
						if (outputFolderPath == null) {
							// No output folder specified in property : use current working folder instead
							outputFolderPath = inputVideoFile.getWorkingFolderPath();
						}
						
						PostProcessLauncher postProcessor = new PostProcessLauncher(inputVideoFile.getWorkingFolderPath(), tvShowInfo, subTitleInfo, outputFolderPath);
						boolean ppResult = postProcessor.launchPostProcess();
						if (ppResult) {
							// Success : stop process for this video file
							log.debug("Video & subtitle files post-processed with success !");
							fileToProcess = true;
							fileSuccess = true;
							fileNoSub = false;
							fileNoPostProcess = false;
							break;
						}
						else {
							// Post-process error : stop process for this video file
							log.warn(String.format("Video & subtitle files post-process has failed for file '%s'...", inputVideoFile.getVideoFileName()));
							fileToProcess = true;
							fileSuccess = false;
							fileNoSub = false;
							fileNoPostProcess = true;
							break;
						}
					}
				}
				
				// 6) Update counters for report
				if (fileToProcess) {
					nbFileToProcess++;
				}
				if (fileSuccess) {					
					nbFileSuccess++;
				}
				if (fileNoSub) {
					nbFileNoSub++;
				}
				if (fileNoPostProcess) {
					nbFileNoPostProcess++;
				}				
			}
			
			// Populate report
			report = new ProcessReport(nbFileToProcess, nbFileSuccess, nbFileNoSub, nbFileNoPostProcess);
		}
		finally {
			if (report != null) {
				if (report.getNbFileToProcess() > 0) {
					log.info(String.format("Process report : %s files to process, %s files processed with success, %s files with no subtitle found, %s files with post-process failed", 
							report.getNbFileToProcess(), report.getNbFileSuccess(), report.getNbFileNoSub(), report.getNbFileNoPostProcess()));
				}
				else {
					log.debug("Process report : no file to process");
				}
			}
			log.debug("... Ending process on working folder");
		}
		return report;
	}

	/**
	 * Populate video file list to process
	 * 
	 * @param workingFolderPath
	 * @throws Exception
	 */
	private void populateVideoFileList(String workingFolderPath) throws Exception {
		
		// Initialize
		if (inputVideoFileList == null) {
			inputVideoFileList = new ArrayList<VideoFileToProcess>();
		}
		
		// Populate with video files in current folder 
		for (String fileName : FileHelper.getVideoFiles(workingFolderPath)) {
			inputVideoFileList.add(new VideoFileToProcess(fileName, workingFolderPath));
		}
		
		// Recursive mode : populate with video files in subfolders
		if (PropertiesHelper.getWorkingFolderRecursiveMode()) {			
			for (String folderName : FileHelper.getSubFoldersToProcess(workingFolderPath)) {
				populateVideoFileList(workingFolderPath + FileHelper.FILE_SEPARATOR + folderName);
			}			
		}		
	}
	
}
