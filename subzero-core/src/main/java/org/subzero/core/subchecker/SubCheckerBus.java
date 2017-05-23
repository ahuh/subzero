package org.subzero.core.subchecker;

import org.subzero.core.bean.SubTitleInfo;
import org.subzero.core.bean.TvShowInfo;
import org.subzero.core.helper.FileHelper;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.helper.TvShowInfoHelper;

/**
 * Sub Checker Bus Class
 * Launches all Sub Checker plug-ins configured in properties, until an internal subtitle file corresponding to the given language is found
 * @author Julien
 *
 */
public class SubCheckerBus {
	
	protected String subLanguage;
	
	private TvShowInfo tvShowInfo;
	
	private String workingFolderPath;

	/**
	 * Constructor
	 * @param workingFolderPath
	 * @param tvShowInfo
	 * @param subLanguage
	 */
	public SubCheckerBus(String workingFolderPath, TvShowInfo tvShowInfo, String subLanguage) {
		this.workingFolderPath = workingFolderPath;
		this.tvShowInfo = tvShowInfo;
		this.subLanguage = subLanguage;
	}
	
	/**
	 * Checks if an external subtitle file corresponding to the video file already exists in working folder
	 * The subtitle file must be named with serie, season and episode corresponding to the video file,
	 * and it must be suffixed with XX.srt (XX = language, srt = subtitle format)
	 * @param languageList
	 * @return SubTitleInfo object if it exists, null if it does not exist 
	 * @throws Exception 
	 */
	public SubTitleInfo checkExternalSub() throws Exception
	{
		SubTitleInfo existingSubTitleInfo = null;
		
		// Get all subtitle files in working folder
		for (String subtitleFileName : FileHelper.getSubtitleFiles(workingFolderPath)) {
			// Populate TV Show Info from subtitle file name
			TvShowInfo subtitleTvShowInfo = TvShowInfoHelper.populateTvShowInfo(subtitleFileName, true, false);
			
			// Check if TV Show Info from subtitle is the same
			if (subtitleTvShowInfo != null && TvShowInfoHelper.testIfTvShowInfoMatch(tvShowInfo, subtitleTvShowInfo)) {
				
				// Check language in file name
				String existingSubtitleLanguage = null;				
				for (String subFileType : PropertiesHelper.getSubFileExtensions())
				{
					if (subtitleFileName.endsWith(TvShowInfoHelper.prepareSubtitleSuffixFileName(subLanguage, subFileType))) {
						existingSubtitleLanguage = subLanguage;
					}
				}
				
				if (existingSubtitleLanguage != null) {
					existingSubTitleInfo = new SubTitleInfo(subtitleFileName, existingSubtitleLanguage);
					return existingSubTitleInfo;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Check if the video file name contains a bypass keyword for the given language (e.g. .FRENCH. ; .vostfr. ; ...)
	 * No subtitle search is required for "bypass" files (skip files)
	 * @return true : bypass file ; false : process file
	 * @throws Exception 
	 */
	public boolean checkFilenameBypassKeyword() throws Exception {
		for (String bypassKeyword : PropertiesHelper.getSubCheckerBypassKeywords(subLanguage)) {
			if (tvShowInfo.getInputVideoFileName().toLowerCase().contains(bypassKeyword.toLowerCase())) {
				return true;
			}
		}
		return false;		
	}
	
	/**
	 * Launch the internal sub checker bus for the given language and for each plugin
	 * @return true : internal subtitle exists (found by one of the plugins) ; false : no internal subtitle (not found by any plugins)
	 * @throws Exception
	 */
	public boolean launchInternalSubCheckerBus() throws Exception
	{
		// Check internal subtitle for the given language with each subchecker plugin in property			
		for (String stPlugin : PropertiesHelper.getSubCheckerPlugins())
		{
			Class<? extends SubCheckerBase> plugin = Class.forName(stPlugin).asSubclass(SubCheckerBase.class);
			SubCheckerBase subChecker = (SubCheckerBase)plugin.newInstance();

			subChecker.initialize(workingFolderPath, tvShowInfo, subLanguage);

			if (subChecker.checkInternalSub()) {
				// Subtitle found !
				return true;
			}
		}

		// No internal subtitle found...
		return false;
	}
}
