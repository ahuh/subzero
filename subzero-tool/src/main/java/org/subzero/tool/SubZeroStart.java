package org.subzero.tool;

import org.apache.log4j.Logger;
import org.subzero.core.helper.PropertiesHelper;

/**
 * SubZero Start class, containing main method
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
	private static String ARG_DAEMON_MODE_LONG = "daemon";
	private static String ARG_DAEMON_MODE_SHORT = "d";
    
	/**
	 * Main method
	 * @param args
	 */
	public static void main(String[] args) {
    	// Initialize logger configuration
    	PropertiesHelper.configureLogger();
    	
    	// Prevent several instances of same program
    	try
        {
            @SuppressWarnings("unused")
			SubZeroLock lock = new SubZeroLock();
        }
        catch(RuntimeException e)
        {
            // Exit main app
        	log.debug("Program is already running : abort new program execution");
            return;
        }
        catch (Exception e)
        {
        	log.error("Error with the lock process", e);
        }
    	
    	if (args.length > 0 && (args[0].toLowerCase().equals(ARG_DAEMON_MODE_LONG.toLowerCase())
    			|| args[0].toLowerCase().equals(ARG_DAEMON_MODE_SHORT.toLowerCase()))) {
    		// Start the daemon application
    		SubZeroDaemon.start();
    	}
    	else {
	    	// Start the SysTray application
	    	SubZeroSysTray.start();
    	}
	}
}
