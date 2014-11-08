package org.subzero.core.plugin;

import org.apache.log4j.Logger;
import org.subzero.core.helper.FileHelper;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.helper.TvShowInfoHelper;
import org.subzero.core.postprocess.PostProcessBase;

/**
 * Post-Process plug-in that move subtitle & video files to output folder
 * @author Julien
 *
 */
public class PostProcessMoveOnly extends PostProcessBase {
	
	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(PostProcessMoveOnly.class);
	
	/**
	 * Launch the post-process after subtitle retrieval with video file of the TV show
	 * @return Success (true) or failure (false)
	 */
	@Override
	public boolean launchPostProcess() {
		try {
			log.debug("Post-Process Move Only - Start");
			
			// Build output path (with serie, season)
			String outputFolderPath = PropertiesHelper.getOutputFolderPath(tvShowInfo.getSerie(), tvShowInfo.getSeason().toString());			
			
			log.debug(String.format("Try to move files to '%s'", outputFolderPath));
			
			// Rename input video file
			String newVideoFileName = TvShowInfoHelper.prepareVideoFileNameKeepFileType(tvShowInfo, subTitleInfo.getLanguage());
			FileHelper.renameWorkingFile(tvShowInfo.getInputVideoFileName(), newVideoFileName);
			
			// Move subtile & video files to output path
			FileHelper.moveWorkingFileToOutputFolder(newVideoFileName, outputFolderPath);
			FileHelper.moveWorkingFileToOutputFolder(subTitleInfo.getSubFileName(), outputFolderPath);
			
			// Process extra files
			if (subTitleInfo.getExtraFileNames() != null && subTitleInfo.getExtraFileNames().size() > 0) {
				// Move extra input files to Ori folder or delete
				if (PropertiesHelper.getMkvMergeMoveOriFilesKeep()) {
	            	// Keep extra files            	
	            	String oriFolderPath = PropertiesHelper.getMkvMergeMoveOriFilesFolderPath();
	            	log.debug(String.format("Moving extra files to Ori Folder '%s'", oriFolderPath));
	            	for (String extraFileName : subTitleInfo.getExtraFileNames()) {
						FileHelper.moveWorkingFileToOutputFolder(extraFileName, oriFolderPath);
					}
	            }
	            else {
	            	// Delete extra files
	            	log.debug("Deleting extra files");
	    			if (subTitleInfo.getExtraFileNames() != null && subTitleInfo.getExtraFileNames().size() > 0) {
	    				for (String extraFileName : subTitleInfo.getExtraFileNames()) {
	    					FileHelper.deleteWorkingFile(extraFileName);
	    				}
	    			}
	            }
			}			
			
			log.info(String.format("Post-Process Move Only succeeded for file '%s' to folder '%s'", newVideoFileName, newVideoFileName));
			
            return true;
        }
		catch (Exception e) {
			log.error("Error while trying to post-process files with Move Only", e);
            return false;
        }
		finally
		{
			log.debug("Post-Process Move Only - End");
		}
	}
	
}
