package org.subzero.tool;

import org.apache.log4j.Logger;
import org.subzero.core.helper.PropertiesHelper;

/**
 * SubZero Start class, containing main method
 * 
 * @author Julien
 *
 */
public class SubZeroStart {

	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubZeroStart.class);

	/**
	 * Constants
	 */
	private final static String JAVA_ARG_HEADLESS_MODE = "headless";

	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Initialize logger configuration
		PropertiesHelper.configureLogger();

		// Prevent several instances of same program
		try {
			@SuppressWarnings("unused")
			SubZeroLock lock = new SubZeroLock();
		} catch (RuntimeException e) {
			// Exit main app
			log.debug("Program is already running : abort new program execution");
			return;
		} catch (Exception e) {
			log.error("Error with the lock process", e);
		}

		if (System.getProperty(JAVA_ARG_HEADLESS_MODE) != null) {
			// Start the headless application
			SubZeroHeadless.start();
		} else {
			// Start the SysTray application
			SubZeroSysTray.start();
		}
	}
}
