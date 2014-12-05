package org.subzero.core.subleecher;

import java.io.File;

import org.subzero.core.bean.SubTitleInfo;
import org.subzero.core.bean.TvShowInfo;
import org.subzero.core.helper.FileHelper;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.helper.TvShowInfoHelper;

/**
 * Sub Leecher Bus Class
 * Launches all Sub Leecher plug-ins configured in properties, until a subtitle file has been retrieved
 * @author Julien
 *
 */
public class SubLeecherBus {
	
	private TvShowInfo tvShowInfo;

	/**
	 * Constructor
	 * @param tvShowInfo
	 */
	public SubLeecherBus(TvShowInfo tvShowInfo)
	{
		this.tvShowInfo = tvShowInfo;
	}

	/**
	 * Check if a subtitle file corresponding to the video file already exists in working folder
	 * The subtitle file must be named with serie, season and episode corresponding to the video file,
	 * and it must be suffixed with (SUB.ZERO.XX).srt (XX = language, srt = subtitle format)
	 * @return SubTitleInfo object if it exists, null if it does not exist 
	 * @throws Exception 
	 */
	public SubTitleInfo checkExistingSubFile() throws Exception
	{
		SubTitleInfo existingSubTitleInfo = null;
		
		// Get all subtitle files in working folder
		for (String subtitleFileName : FileHelper.getSubtitleFiles()) {
			// Populate TV Show Info from subtitle file name
			TvShowInfo subtitleTvShowInfo = TvShowInfoHelper.populateTvShowInfo(subtitleFileName, true, false);
			
			// Check if TV Show Info from subtitle is the same
			if (subtitleTvShowInfo != null && TvShowInfoHelper.testIfTvShowInfoMatch(tvShowInfo, subtitleTvShowInfo)) {
				
				// Check language in file name
				String subtitleLanguage = null;				
				for (String language : PropertiesHelper.getSubLeecherLanguages())
				{
					for (String subFileType : PropertiesHelper.getSubFileExtensions())
					{
						if (subtitleFileName.endsWith(TvShowInfoHelper.prepareSubtitleSuffixFileName(language, subFileType))) {
							subtitleLanguage = language;
						}
					}
				}
				
				if (subtitleLanguage != null) {
					existingSubTitleInfo = new SubTitleInfo(subtitleFileName, subtitleLanguage);
					return existingSubTitleInfo;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Launch the subleecher bus for each language and each plugin
	 * @return Output subtitle file name and language if found, null if not found
	 * @throws Exception
	 */
	public SubTitleInfo launchBus() throws Exception
	{
		// 1 - Search subtitles for each language in property
		for (String language : PropertiesHelper.getSubLeecherLanguages())
		{
			// 2 - Search subtitles with each subleecher plugin in property			
			for (String stPlugin : PropertiesHelper.getSubLeecherPlugins())
			{
				Class<? extends SubLeecherBase> plugin = Class.forName(stPlugin).asSubclass(SubLeecherBase.class);
				SubLeecherBase subLeecher = (SubLeecherBase)plugin.newInstance();

				subLeecher.initialize(tvShowInfo, language, PropertiesHelper.getSubLeecherReleaseGroupRequired());

				SubTitleInfo subTitleInfo = subLeecher.leechSub();
				if (subTitleInfo != null)
				{
					// Subtitles found !
					return subTitleInfo;
				}
			}
		}
		// No subtitles found...
		return null;
	}
}
