package org.subzero.core.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.subzero.core.helper.FileHelper;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.helper.TvShowInfoHelper;
import org.subzero.core.postprocess.PostProcessBase;

/**
 * Post-Process plug-in that merges subtitle & video files in a new MKV (using MKVMerge) and moves to output folder
 * @author Julien
 *
 */
public class PostProcessMkvMergeMove extends PostProcessBase {
	
	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(PostProcessMkvMergeMove.class);
	
	/**
	 * Launch the post-process after subtitle retrieval with video file of the TV show
	 * @return Success (true) or failure (false)
	 */
	@Override
	public boolean launchPostProcess() {
		try {
			log.debug("Post-Process MkvMerge & Move - Start");
			
			// Prepare input paths
			String workingFolderPath = PropertiesHelper.getWorkingFolderPath();
			String inputVideoFilePath = workingFolderPath + File.separator + tvShowInfo.getInputVideoFileName();
			String subFilePath = workingFolderPath + File.separator + subTitleInfo.getSubFileName();
						
			// Prepare output paths (serie, season)
			String outputFolderPath = PropertiesHelper.getOutputFolderPath(tvShowInfo.getSerie(), tvShowInfo.getSeason().toString());			
			String outputFilePath = outputFolderPath + File.separator + TvShowInfoHelper.prepareVideoFileName(tvShowInfo, subTitleInfo.getLanguage());
			
			log.debug(String.format("Try to generate output file '%s'", outputFilePath));
			
			// Ensure that the output folder exists
			FileHelper.ensureFolder(outputFolderPath);
			
			// Prepare subtitle language for MKVMerge : 2 first letters
			String language = subTitleInfo.getLanguage().toLowerCase().substring(0,2);
			
			// Execute MKVMerge command line
			log.debug("Executing MKVMerge and logging output :");
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(PropertiesHelper.getMkvMergeMoveMkvMergeCommandLine(outputFilePath, language, subFilePath, inputVideoFilePath));
            
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line=null;
            while((line=input.readLine()) != null) {
            	if (line != null && !line.equals("")) {
            		log.debug(line);
            	}
            }

            int exitVal = process.waitFor();
            log.debug(String.format("Exited with return code : %s", exitVal));
            if (exitVal == 2) {
            	throw new Exception(String.format("Error detected in MKVMerge !", exitVal));
            }
            
            // Move original input files to Ori folder or delete
            if (PropertiesHelper.getMkvMergeMoveOriFilesKeep()) {
            	// Keep original files            	
            	String oriFolderPath = PropertiesHelper.getMkvMergeMoveOriFilesFolderPath();
            	log.debug(String.format("Moving original files to Ori Folder '%s'", oriFolderPath));
            	FileHelper.moveWorkingFileToOutputFolder(tvShowInfo.getInputVideoFileName(), oriFolderPath);
    			FileHelper.moveWorkingFileToOutputFolder(subTitleInfo.getSubFileName(), oriFolderPath);
    			// Process extra files
    			if (subTitleInfo.getExtraFileNames() != null && subTitleInfo.getExtraFileNames().size() > 0) {
    				for (String extraFileName : subTitleInfo.getExtraFileNames()) {
    					FileHelper.moveWorkingFileToOutputFolder(extraFileName, oriFolderPath);
    				}
    			}
            }
            else {
            	// Delete original files
            	log.debug("Deleting original files");
            	FileHelper.deleteWorkingFile(tvShowInfo.getInputVideoFileName());
            	FileHelper.deleteWorkingFile(subTitleInfo.getSubFileName());
            	// Process extra files
    			if (subTitleInfo.getExtraFileNames() != null && subTitleInfo.getExtraFileNames().size() > 0) {
    				for (String extraFileName : subTitleInfo.getExtraFileNames()) {
    					FileHelper.deleteWorkingFile(extraFileName);
    				}
    			}
            }
            
            log.info(String.format("Post-Process MkvMerge & Move succeeded for file '%s'", outputFilePath));
            
            return true;
        }
		catch (Exception e) {
			log.error("Error while trying to post-process files with MKV Merge & Move", e);
            return false;
        }
		finally
		{
			log.debug("Post-Process MkvMerge & Move - End");
		}
	}
	
}
