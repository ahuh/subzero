package org.subzero.core.test;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.launcher.SubZeroProcessLauncher;

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
		SubZeroProcessLauncher launcher = new SubZeroProcessLauncher();
		launcher.launchProcess();
	}
}
