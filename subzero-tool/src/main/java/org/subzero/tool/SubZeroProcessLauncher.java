package org.subzero.tool;

import java.util.List;

import org.apache.log4j.Logger;
import org.subzero.core.bean.ProcessReport;
import org.subzero.core.bean.SubTitleInfo;
import org.subzero.core.bean.TvShowInfo;
import org.subzero.core.helper.FileHelper;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.helper.TvShowInfoHelper;
import org.subzero.core.postprocess.PostProcessLauncher;
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
	
	/**
	 * Launch SubZero process
	 * @return Process Report
	 * @throws Exception
	 */
	public static ProcessReport launchProcess() throws Exception {
		ProcessReport report = null;
		
		try {					
			log.debug("****************************************");
			log.debug(String.format("Begin SubZero Process on working folder '%s' ...", PropertiesHelper.getWorkingFolderPath()));
			
			// Get video files to process in Working Folder
			int nbFileSuccess = 0;
			int nbFileNoSub = 0;
			int nbFileNoPostProcess = 0;
			List<String> fileList = FileHelper.getWorkingVideoFiles();
			log.debug(String.format("Number of video files to process : %s", fileList.size()));
			
			for (String inputVideoFileName : fileList)
			{
				// Populate TV Show Info from video file
				TvShowInfo tvShowInfo = TvShowInfoHelper.populateTvShowInfo(inputVideoFileName);
				
				// Initialize SubLeecher Bus and check if a subtitle file exists in working folder
				SubLeecherBus subLeecherBus = new SubLeecherBus(tvShowInfo);
				SubTitleInfo subTitleInfo = subLeecherBus.checkExistingSubFile();	
				
				if (subTitleInfo == null) {
					// Launch SubLeecher Bus ...
					log.info(String.format("Processing video file ''%s' : searching for subtitle...", inputVideoFileName));
					subTitleInfo = subLeecherBus.launchBus();
				}
				else {
					log.debug(String.format("Subtitle file already exists (no leech) : '%s' (%s)", subTitleInfo.getSubFileName(), subTitleInfo.getLanguage()));
				}
				
				if (subTitleInfo == null) {
					log.warn(String.format("No subtitle file found for file '%s' ! Try later...", inputVideoFileName));
					nbFileNoSub++;
				}
				else 
				{
					// Launch PostProcessor ...
					log.info(String.format("Processing video file ''%s' : post-processing ...", inputVideoFileName));
					PostProcessLauncher postProcessor = new PostProcessLauncher(tvShowInfo, subTitleInfo);
					boolean ppResult = postProcessor.launchPostProcess();
					if (ppResult) {
						log.debug("Video & subtitle files post-processed with success !");
						nbFileSuccess++;
					}
					else {
						log.warn(String.format("Video & subtitle files post-process has failed for file '%s'...", inputVideoFileName));
						nbFileNoPostProcess++;
					}
				}
			}
			
			// Populate report
			report = new ProcessReport(fileList.size(), nbFileSuccess, nbFileNoSub, nbFileNoPostProcess);
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
	
}
