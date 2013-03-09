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
	
	/**
	 * Initialize parameters
	 * @param tvShowInfo
	 * @param subLanguage
	 * @param releaseGroupMatchRequired
	 */
	public void initialize(TvShowInfo tvShowInfo, String subLanguage, boolean releaseGroupMatchRequired)
	{
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
