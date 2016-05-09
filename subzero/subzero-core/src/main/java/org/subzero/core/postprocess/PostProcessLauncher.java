package org.subzero.core.postprocess;

import org.subzero.core.bean.SubTitleInfo;
import org.subzero.core.bean.TvShowInfo;
import org.subzero.core.helper.PropertiesHelper;

/**
 * Post-process Launcher Class
 * Launches the post-process plug-in configured in properties, to handle subtitle & video files
 * @author Julien
 *
 */
public class PostProcessLauncher {
	
	private TvShowInfo tvShowInfo;
	
	private SubTitleInfo subTitleInfo;
	
	private String workingFolderPath;
	
	private String outputFolderPath;

	/**
	 * Constructor
	 * @param workingFolderPath
	 * @param tvShowInfo
	 * @param subTitleInfo
	 */
	public PostProcessLauncher(String workingFolderPath, TvShowInfo tvShowInfo, SubTitleInfo subTitleInfo, String outputFolderPath)	{
		this.workingFolderPath = workingFolderPath;
		this.tvShowInfo = tvShowInfo;
		this.subTitleInfo = subTitleInfo;
		this.outputFolderPath = outputFolderPath;
	}

	/**
	 * Launch the post-process with the plug-in
	 * @return Success (true) or failure (false)
	 * @throws Exception
	 */
	public boolean launchPostProcess() throws Exception	{
		String stPlugin = PropertiesHelper.getPostProcessPlugin();
		Class<? extends PostProcessBase> plugin = Class.forName(stPlugin).asSubclass(PostProcessBase.class);
		PostProcessBase postProcess = (PostProcessBase)plugin.newInstance();

		postProcess.initialize(workingFolderPath, tvShowInfo, subTitleInfo, outputFolderPath);

		return postProcess.launchPostProcess();
	}
}
