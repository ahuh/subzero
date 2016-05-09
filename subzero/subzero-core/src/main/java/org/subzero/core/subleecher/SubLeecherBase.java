package org.subzero.core.subleecher;

import org.subzero.core.bean.SubTitleInfo;
import org.subzero.core.bean.TvShowInfo;

/**
 * SubLeecher Base Class for plugins
 * @author Julien
 *
 */
public abstract class SubLeecherBase {
	
	protected String subLanguage;
	
	protected TvShowInfo tvShowInfo;
	
	protected boolean releaseGroupMatchRequired;
	
	protected String workingFolderPath;
	
	/**
	 * Initialize parameters
	 * @param workingFolderPath
	 * @param tvShowInfo
	 * @param subLanguage
	 * @param releaseGroupMatchRequired
	 */
	public void initialize(String workingFolderPath, TvShowInfo tvShowInfo, String subLanguage, boolean releaseGroupMatchRequired)
	{
		this.workingFolderPath = workingFolderPath;
		this.tvShowInfo = tvShowInfo;
		this.subLanguage = subLanguage;
		this.releaseGroupMatchRequired = releaseGroupMatchRequired;
	}
	
	/**
	 * Leech the subtitles of the TV show from the web site
	 * @return Output file name and language of the leeched subtitle or null if no subtitle found
	 * 
	 */
	public abstract SubTitleInfo leechSub();
}
