package org.subzero.core.postprocess;

import org.subzero.core.bean.SubTitleInfo;
import org.subzero.core.bean.TvShowInfo;

/**
 * Post-Process Base Class for plugins
 * @author Julien
 *
 */
public abstract class PostProcessBase {
	
	protected SubTitleInfo subTitleInfo;
	
	protected TvShowInfo tvShowInfo;
	
	/**
	 * Initialize parameters
	 * @param tvShowName
	 */
	public void initialize(TvShowInfo tvShowInfo, SubTitleInfo subTitleInfo)
	{
		this.tvShowInfo = tvShowInfo;
		this.subTitleInfo = subTitleInfo;
	}
	
	/**
	 * Launch the post-process after subtitle retrieval with video file of the TV show
	 * @return Success (true) or failure (false)
	 */
	public abstract boolean launchPostProcess();
}
