package org.subzero.core.postprocess;

import org.subzero.core.bean.SubTitleInfo;
import org.subzero.core.bean.TvShowInfo;

/**
 * Post-Process Base Class for plugins
 * @author Julien
 *
 */
public abstract class PostProcessBase {
	
	protected TvShowInfo tvShowInfo;
	
	protected SubTitleInfo subTitleInfo;
	
	protected String workingFolderPath;
	
	protected String outputFolderPath;
	
	/**
	 * Initialize parameters
	 * @param tvShowName
	 */
	public void initialize(String workingFolderPath, TvShowInfo tvShowInfo, SubTitleInfo subTitleInfo, String outputFolderPath)
	{
		this.workingFolderPath = workingFolderPath;
		this.tvShowInfo = tvShowInfo;
		this.subTitleInfo = subTitleInfo;
		this.outputFolderPath = outputFolderPath;
	}
	
	/**
	 * Launch the post-process after subtitle retrieval with video file of the TV show
	 * @return Success (true) or failure (false)
	 */
	public abstract boolean launchPostProcess();
}
