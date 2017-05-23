package org.subzero.core.plugin;

import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.subzero.core.helper.FileHelper;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.subchecker.SubCheckerBase;

/**
 * SubChecker plugin for MKV files
 * @author Julien
 *
 */
public class SubCheckerMkv extends SubCheckerBase  {

	private static final String MKVMERGE_OUTPUT_ENCODING = "UTF-8";
	
	private static final String JSON_TRACK_TYPE_SUBTITLE = "subtitles";	
	private static final String JSON_TRACK_TYPE_AUDIO = "audio";

	private static final String JSON_TRACKS = "tracks";
	private static final String JSON_TRACK_TYPE = "type";
	
	private static final String JSON_PROPERTIES = "properties";
	private static final String JSON_PROPERTY_LANGUAGE = "language";

	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubCheckerMkv.class);

	/**
	 * Checks if an internal subtitle file corresponding to the given language already exists in the MKV video file
	 * @return true : internal subtitle exists ; false : no internal subtitle
	 */
	@Override
	public boolean checkInternalSub() {
		String inputVideoFilePath = "";
		try {
			log.debug("SubChecker MKV - Start");
			
			// Prepare input path
			String inputVideoFileName = tvShowInfo.getInputVideoFileName();
			inputVideoFilePath = this.workingFolderPath + FileHelper.FILE_SEPARATOR + inputVideoFileName;
			
			// Check file extension (must by MKV)
			boolean isMkvFile = false;
			for (String mkvFileExt : PropertiesHelper.getSubCheckerMkvVideoFileExtensions()) {
				if (inputVideoFileName.toLowerCase().endsWith(FileHelper.EXT_SEPARATOR + mkvFileExt.toLowerCase())) {
					isMkvFile = true;
					break;
				}
			}
			if (!isMkvFile) {
				log.debug(String.format("Video file '%s' (language '%s') is not a MKV file : skip MKV sub checking", inputVideoFilePath, subLanguage));
				return false;
			}			
			
			// Prepare UI language for MKVMerge : 2 first letters
			String uiLanguage = subLanguage.toLowerCase().substring(0,2);
			
			// Execute MKVMerge command line
			log.debug("Executing MKVMerge ...");
			ProcessBuilder processBuilder = new ProcessBuilder(PropertiesHelper.getSubCheckerMkvMkvMergeCommandLine(
					inputVideoFilePath, 
					uiLanguage));
            Process process = processBuilder.start();
            
            StringWriter writer = new StringWriter();
            IOUtils.copy(process.getInputStream(), writer, MKVMERGE_OUTPUT_ENCODING);
            String jsonString = writer.toString();

            int exitVal = process.waitFor();
            log.debug(String.format("Exited with return code : %s", exitVal));
            if (exitVal == 2) {
            	throw new Exception(String.format("Error detected in MKVMerge : %s", jsonString));
            }
            
            // Parse JSON response
            JSONObject jsonDoc = new JSONObject(jsonString);
            JSONArray jsonTracks = jsonDoc.getJSONArray(JSON_TRACKS);
            
            // Parse tracks
            boolean trackWithGivenLanguageFound = false;
            for (int i = 0; i < jsonTracks.length(); i++) {
            	JSONObject jsonTrack = jsonTracks.getJSONObject(i);
            	String jsonTrackType = jsonTrack.getString(JSON_TRACK_TYPE);
            	if (JSON_TRACK_TYPE_SUBTITLE.toLowerCase().equals(jsonTrackType.toLowerCase())) {
            		// Track type "subtitle"
            		if (isTrackInGivenLanguage(jsonTrack)) {
            			log.debug(String.format("Subtitle track found for video file '%s' (language '%s')", inputVideoFilePath, subLanguage));
            			trackWithGivenLanguageFound = true;
            			break;
            		}
            	}
            	else if (PropertiesHelper.getSubCheckerMkvCheckAudioTrack(subLanguage) 
            			&& JSON_TRACK_TYPE_AUDIO.toLowerCase().equals(jsonTrackType.toLowerCase())) {
            		// Track type "audio" and check audio parameter is set for the given language
            		if (isTrackInGivenLanguage(jsonTrack)) {
            			log.debug(String.format("Audio track found for video file '%s' (language '%s')", inputVideoFilePath, subLanguage));
            			trackWithGivenLanguageFound = true;
            			break;
            		}
            	}
            }
            
            if (!trackWithGivenLanguageFound) {
            	log.debug(String.format("No track found for video file '%s' (language '%s')", inputVideoFilePath, subLanguage));
            }
            
            return trackWithGivenLanguageFound;
        }
		catch (Exception e) {
			log.error(String.format("Error while trying to check subtitles with MKV SubChecker for file '%s'", inputVideoFilePath), e);
            return false;
        }
		finally
		{
			log.debug("SubChecker MKV - End");
		}
	}

	/**
	 * Checks if the JSON track is in the given language
	 * @param jsonTrack
	 * @return
	 */
	private boolean isTrackInGivenLanguage(JSONObject jsonTrack) {
		JSONObject jsonProperties = jsonTrack.getJSONObject(JSON_PROPERTIES);
        
        // Parse properties
		String jsonPropertyLanguage = jsonProperties.getString(JSON_PROPERTY_LANGUAGE);
        	
    	if (jsonPropertyLanguage != null && !jsonPropertyLanguage.isEmpty() &&
    			subLanguage.toLowerCase().startsWith(jsonPropertyLanguage.toLowerCase())) {
    		// Property language corresponds to the given language
    		return true;
    	}
        
        return false;
	}
}
