package org.subzero.core.subleecher;

import org.subzero.core.bean.SubTitleInfo;
import org.subzero.core.bean.TvShowInfo;
import org.subzero.core.helper.PropertiesHelper;

/**
 * Sub Leecher Bus Class
 * Launches all Sub Leecher plug-ins configured in properties, until a subtitle file for the given language has been retrieved
 * @author Julien
 *
 */
public class SubLeecherBus {
	
	protected String subLanguage;
	
	private TvShowInfo tvShowInfo;
	
	private String workingFolderPath;

	/**
	 * Constructor
	 * @param workingFolderPath
	 * @param tvShowInfo
	 * @param subLanguage
	 */
	public SubLeecherBus(String workingFolderPath, TvShowInfo tvShowInfo, String subLanguage)
	{
		this.workingFolderPath = workingFolderPath;
		this.tvShowInfo = tvShowInfo;
		this.subLanguage = subLanguage;
	}	
	/**
	 * Launch the subleecher bus for each language and each plugin
	 * @param languageList
	 * @return Output subtitle file name and language if found, null if not found
	 * @throws Exception
	 */
	public SubTitleInfo launchSubLeecherBus(String[] languageList) throws Exception
	{
		// Search subtitles for given language with each subleecher plugin in property			
		for (String stPlugin : PropertiesHelper.getSubLeecherPlugins())
		{
			Class<? extends SubLeecherBase> plugin = Class.forName(stPlugin).asSubclass(SubLeecherBase.class);
			SubLeecherBase subLeecher = (SubLeecherBase)plugin.newInstance();

			subLeecher.initialize(workingFolderPath, tvShowInfo, subLanguage, PropertiesHelper.getSubLeecherReleaseGroupRequired());

			SubTitleInfo subTitleInfo = subLeecher.leechSub();
			if (subTitleInfo != null)
			{
				// Subtitles found !
				return subTitleInfo;
			}
		}
		
		// No subtitles found...
		return null;
	}
}
