package org.subzero.core.test;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.subzero.core.bean.SubTitleInfo;
import org.subzero.core.bean.TvShowInfo;
import org.subzero.core.helper.FileHelper;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.helper.TvShowInfoHelper;
import org.subzero.core.postprocess.PostProcessLauncher;
import org.subzero.core.subleecher.SubLeecherBus;

public class SubZeroFullProcessTest {
	
	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubZeroFullProcessTest.class);

	@Before
	public void init()
	{
		// Initialize logger configuration
		PropertiesHelper.configureLogger();
	}
	
	//@Test
	public void testFullProcess() throws Exception
	{
		String workingFolderPath = PropertiesHelper.getWorkingFolderPath();
		for (String inputVideoFileName : FileHelper.getVideoFiles(workingFolderPath))
		{
			log.debug("inputVideoFileName=" + inputVideoFileName);
						
			TvShowInfo tvShowInfo = TvShowInfoHelper.populateTvShowInfo(inputVideoFileName, true);
					
			SubLeecherBus subLeecherBus = new SubLeecherBus(workingFolderPath, tvShowInfo);
			SubTitleInfo subTitleInfo = subLeecherBus.checkExistingSubFile();
			if (subTitleInfo == null) {
				subTitleInfo = subLeecherBus.launchBus();				
				if (subTitleInfo != null) {
					log.debug("subLeecherBusFound=" + subTitleInfo.getSubFileName() + ";" + subTitleInfo.getLanguage());
				}
				assertNotNull(subTitleInfo);
			}
			else {
				log.debug("subFileAlreadyExists=" + subTitleInfo.getSubFileName() + ";" + subTitleInfo.getLanguage());
			}
			
			if (subTitleInfo != null) {
				PostProcessLauncher postProcessor = new PostProcessLauncher(workingFolderPath, tvShowInfo, subTitleInfo);
				boolean ppResult = postProcessor.launchPostProcess();
				if (ppResult) {
					log.debug("ok!");
				}
				assertTrue(ppResult);
			}
		}		
	}
}
