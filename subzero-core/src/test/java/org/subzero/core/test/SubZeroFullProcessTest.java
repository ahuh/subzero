package org.subzero.core.test;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.launcher.SubZeroProcessLauncher;

public class SubZeroFullProcessTest {

	@Before
	public void init()
	{
		// Initialize logger configuration
		PropertiesHelper.configureLogger();
	}
	
	@Test
	@Ignore
	public void testFullProcess() throws Exception
	{
		SubZeroProcessLauncher launcher = new SubZeroProcessLauncher();
		launcher.launchProcess();
	}
}
