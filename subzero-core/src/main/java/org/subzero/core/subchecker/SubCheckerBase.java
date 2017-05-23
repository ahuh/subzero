package org.subzero.core.subchecker;

import org.subzero.core.bean.TvShowInfo;

/**
 * SubChecker Base Class for plugins
 * @author Julien
 *
 */
public abstract class SubCheckerBase {
	
	protected String subLanguage;
	
	protected TvShowInfo tvShowInfo;
	
	protected String workingFolderPath;
	
	/**
	 * Initialize parameters
	 * @param workingFolderPath
	 * @param tvShowInfo
	 * @param subLanguage
	 */
	public void initialize(String workingFolderPath, TvShowInfo tvShowInfo, String subLanguage)
	{
		this.workingFolderPath = workingFolderPath;
		this.tvShowInfo = tvShowInfo;
		this.subLanguage = subLanguage;
	}
	
	/**
	 * Checks if an internal subtitle file corresponding to the given language already exists in the video file
	 * @return true : internal subtitle exists ; false : no internal subtitle
	 * 
	 */
	public abstract boolean checkInternalSub();
}
