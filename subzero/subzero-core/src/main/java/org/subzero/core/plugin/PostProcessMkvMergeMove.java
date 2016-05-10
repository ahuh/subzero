package org.subzero.core.plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.subzero.core.helper.FileHelper;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.postprocess.PostProcessBase;

/**
 * Post-Process plug-in that merges subtitle & video files in a new MKV (using MKVMerge) and moves to output folder
 * @author Julien
 *
 */
public class PostProcessMkvMergeMove extends PostProcessBase {
	
	/*
	 * Temp file prefix to generate
	 */
	private static String TEMP_FILE_PREFIX = "TEMP_";
	
	private static String MKV_EXTENSION = "mkv";
	
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
		String outputFilePath = "";
		try {
			log.debug("Post-Process MkvMerge & Move - Start");
			
			// Rename input video file (in case output folder = input folder)
			String inputVideoFileName = tvShowInfo.getInputVideoFileName();
			String tempInputVideoFileName = TEMP_FILE_PREFIX + inputVideoFileName;
			FileHelper.renameWorkingFile(this.workingFolderPath, inputVideoFileName, tempInputVideoFileName);
			
			// Prepare input paths
			String tempVideoFilePath = this.workingFolderPath + FileHelper.FILE_SEPARATOR + tempInputVideoFileName;
			String subFilePath = this.workingFolderPath + FileHelper.FILE_SEPARATOR + subTitleInfo.getSubFileName();
						
			// Prepare output paths
			outputFilePath = outputFolderPath + FileHelper.FILE_SEPARATOR + FileHelper.replaceExtensionInFileName(inputVideoFileName, MKV_EXTENSION);
			
			log.debug(String.format("Try to generate output file '%s'", outputFilePath));
			
			// Ensure that the output folder exists
			FileHelper.ensureFolder(outputFolderPath);
			
			// Prepare subtitle language for MKVMerge : 2 first letters
			String languageCode = subTitleInfo.getLanguage().toLowerCase().substring(0,2);
			
			// Execute MKVMerge command line
			log.debug("Executing MKVMerge and logging output :");
			ProcessBuilder processBuilder = new ProcessBuilder(PropertiesHelper.getMkvMergeMoveMkvMergeCommandLine(
					outputFilePath, 
					languageCode, 
					subFilePath, 
					tempVideoFilePath));
            Process process = processBuilder.start();
            
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
            	throw new Exception("Error detected in MKVMerge !");
            }
            
            // Move original input files to Ori folder or delete
            if (PropertiesHelper.getMkvMergeMoveOriFilesKeep()) {
            	// Keep original files            	
            	String oriFolderPath = PropertiesHelper.getMkvMergeMoveOriFilesFolderPath();
            	log.debug(String.format("Moving original files to Ori Folder '%s'", oriFolderPath));
            	FileHelper.moveWorkingFileToFolder(this.workingFolderPath, tempInputVideoFileName, oriFolderPath, inputVideoFileName);
    			FileHelper.moveWorkingFileToFolder(this.workingFolderPath, subTitleInfo.getSubFileName(), oriFolderPath);
    			// Process extra files
    			if (subTitleInfo.getExtraFileNames() != null && subTitleInfo.getExtraFileNames().size() > 0) {
    				for (String extraFileName : subTitleInfo.getExtraFileNames()) {
    					FileHelper.moveWorkingFileToFolder(this.workingFolderPath, extraFileName, oriFolderPath);
    				}
    			}
            }
            else {
            	// Delete original files
            	log.debug("Deleting original files");
            	FileHelper.deleteWorkingFile(this.workingFolderPath, tvShowInfo.getInputVideoFileName());
            	FileHelper.deleteWorkingFile(this.workingFolderPath, subTitleInfo.getSubFileName());
            	// Process extra files
    			if (subTitleInfo.getExtraFileNames() != null && subTitleInfo.getExtraFileNames().size() > 0) {
    				for (String extraFileName : subTitleInfo.getExtraFileNames()) {
    					FileHelper.deleteWorkingFile(this.workingFolderPath, extraFileName);
    				}
    			}
            }
            
            //TODO:corriger bug : il faut changer extension en mkv dans tous les cas (output folder vide ou pas)
            
            log.info(String.format("Post-Process MkvMerge & Move succeeded for file '%s'", outputFilePath));
            
            return true;
        }
		catch (Exception e) {
			log.error(String.format("Error while trying to post-process files with MKV Merge & Move for file '%s'", outputFilePath), e);
            return false;
        }
		finally
		{
			log.debug("Post-Process MkvMerge & Move - End");
		}
	}
	
}
