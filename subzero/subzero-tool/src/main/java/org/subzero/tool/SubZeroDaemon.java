package org.subzero.tool;

import org.apache.log4j.Logger;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.launcher.SubZeroProcessLauncher;

/**
 * SubZero Daemon class
 * @author Julien
 *
 */
public class SubZeroDaemon {
	
	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubZeroDaemon.class);    
	
	/**
     * Static properties (shared)
     */	
    private static boolean processIsRunning = false;    
    
    /**
     * Launch SubZero process safely in a new thread
     */
	public static void launchSubZeroProcessSafely() {		
		// Declare new thread
		Thread thread = new Thread(new Runnable()
		{	
			// Run in new thread
			public void run() {
				if (processIsRunning) {
					// Process is already running
					log.debug("Trying to launch new process : blocked because a process is already running");
				}
				else {
					// Process is not running : launch it and lock the running flag
					processIsRunning = true;
					try {
						SubZeroProcessLauncher launcher = new SubZeroProcessLauncher();
						launcher.launchProcess();
					}
					catch (Exception e) {
						log.error("Error while processing video files", e);
					}
					finally
					{
						processIsRunning = false;
					}
				}
			}
		});
		
		// Execute new thread
		thread.start();
	}
		
    /**
     * Start
     */
    public static void start() {
    	log.debug("SubZero - Start");
    	    	
    	int initDelay = 0;
        int frequency = 0;
        try {
        	initDelay = PropertiesHelper.getAutomaticProcessInitDelay();
        	frequency = PropertiesHelper.getAutomaticProcessFrequency();
        } catch (Exception e) {
        	log.error("Error while trying to get properties in configuration file", e);
            return;
        }
        
        try {
	        boolean firstLaunch = true;
	        
	        // Endless loop
	    	for (;;) {
	    		// Wait the delay specified in properties before launching process
	    		if (firstLaunch) {
	    			Thread.sleep(initDelay);
	    			firstLaunch = false;
	    		}
	    		else {
	    			Thread.sleep(frequency);
	    		}
	    		
	    		// Launch process
				launchSubZeroProcessSafely();
	    	}
        } catch (Exception e) {
        	log.error("Error while waiting before next process launch", e);
            return;
        }
    }
}
