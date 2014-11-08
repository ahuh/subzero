package org.subzero.core.subleecher;

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
	 * @return SubTitleInfo object if it exists, null if it does not exist 
	 * @throws Exception 
	 */
	public SubTitleInfo checkExistingSubFile() throws Exception
	{
		SubTitleInfo existingSubTitleInfo = null;
		
		for (String language : PropertiesHelper.getSubLeecherLanguages())
		{
			for (String subFileType : PropertiesHelper.getSubFileExtensions())
			{
				String subFileName = TvShowInfoHelper.prepareSubtitleFileName(tvShowInfo, language, subFileType);
				if (FileHelper.doesWorkingFileExist(subFileName)) {						
					existingSubTitleInfo = new SubTitleInfo(subFileName, language);
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
