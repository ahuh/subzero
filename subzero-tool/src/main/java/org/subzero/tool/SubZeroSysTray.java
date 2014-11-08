package org.subzero.tool;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.subzero.core.bean.ProcessReport;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.version.VersionConstants;

/**
 * SubZero SysTray class
 * @author Julien
 *
 */
public class SubZeroSysTray {
	
	/**
	 * Constants
	 */
	private static String SUBZERO_POPUP_NAME = "SubZero";
	private static String SUBZERO_POPUP_DESCRIPTION = "SubZero %s - Watches your TV show video files... and adds subtitle";
	private static String SUBZERO_POPUP_ABOUT = "SubZero\n  Watches your TV show video files... and adds subtitle\n    by ahuh\n      %s - %s";
	private static String SUBZERO_POPUP_PREFIX_PAUSED = "[PAUSED] ";
	private static String SUBZERO_ICON_PATH = "/images/subzero.png";
	private static String SUBZERO_ICON_WORKING_PATH = "/images/subzero_working.png";
	private static String SUBZERO_ICON_PAUSED_PATH = "/images/subzero_paused.png";
	
	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubZeroSysTray.class);
	    
    /**
     * Static properties (shared)
     */	
    private static boolean processIsRunning = false;    
    private static PopupMenu popup;
    private static TrayIcon trayIcon;
    private static SystemTray tray;
    private static boolean paused = false;
    private static Timer timer;
    private static MenuItem launchNowMenuItem;
    private static MenuItem pauseMenuItem;
    private static MenuItem resumeMenuItem;
        
    /**
     * Launch SubZero process safely in a new thread
     * @param startAndEndMessage
     */
	public static void launchSubZeroProcessSafely(boolean startAndEndMessage) {		
		final boolean paramStartAndEndMessage = startAndEndMessage;
		
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
						if (paramStartAndEndMessage) {
							trayIcon.displayMessage(SUBZERO_POPUP_NAME, "Launching process ...", TrayIcon.MessageType.INFO);
						}
						trayIcon.setImage(getImage(SUBZERO_ICON_WORKING_PATH));
						ProcessReport report = SubZeroProcessLauncher.launchProcess();
						if (report == null) {
							// No Report
							trayIcon.displayMessage(SUBZERO_POPUP_NAME, "Error while processing video files :(", TrayIcon.MessageType.ERROR);
						}
						else if (report.getNbFileToProcess() > 0) {
							// Display result message in bubble
							String message = "";
							String smiley = " :)";
							MessageType messageType = TrayIcon.MessageType.INFO;
							if (report.getNbFileSuccess() > 0) {
								String s = "s";
								if (report.getNbFileSuccess() == 1) s = "";
								message += String.format("%s video file%s processed with success", report.getNbFileSuccess(), s);
							}					
							if (report.getNbFileNoSub() > 0) {
								if (!message.equals("")) message += ", ";
								String s = "s";
								if (report.getNbFileNoSub() == 1) s = "";
								message += String.format("%s subtitle file%s not found", report.getNbFileNoSub(), s);
								smiley = " :/";
								messageType = TrayIcon.MessageType.WARNING;
							}
							if (report.getNbFileNoPostProcess() > 0) {
								String s = "s";
								if (report.getNbFileNoPostProcess() == 1) s = "";
								if (!message.equals("")) message += ", ";
								message += String.format("%s file%s with post-process error", report.getNbFileNoPostProcess(), s);
								smiley = " :(";
								messageType = TrayIcon.MessageType.ERROR;
							}
							trayIcon.displayMessage(SUBZERO_POPUP_NAME, message + smiley, messageType);
						}
						else if (paramStartAndEndMessage) {
							trayIcon.displayMessage(SUBZERO_POPUP_NAME, "Nothing to do !", TrayIcon.MessageType.INFO);
						}
					}
					catch (Exception e) {
						trayIcon.displayMessage(SUBZERO_POPUP_NAME, "Arrrrg... Unexpected error while processing video files :(", TrayIcon.MessageType.ERROR);
						log.error("Error while processing video files", e);
					}
					finally
					{
						processIsRunning = false;
						trayIcon.setImage(getImage(SUBZERO_ICON_PATH));
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
    	
        /* Use an appropriate Look and Feel */
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            log.error("Error while setting UI look and feel", e);
        }
        
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        
        // Schedule a job for the event-dispatching thread:
        // adding TrayIcon
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
	    
    /**
     * Obtain the image URL
     * @param path
     * @return
     */
    protected static Image getImage(String path) {
    	//URL imageURL = Thread.currentThread().getContextClassLoader().getResource(path);
    	URL imageURL = SubZeroSysTray.class.getResource(path);
    	return Toolkit.getDefaultToolkit().getImage(imageURL);
    }
    
    /**
     * Obtain the file or folder
     * @param path
     * @return
     * @throws Exception 
     */
    protected static File getFileOrFolder(String path) throws Exception {
    	//URL config = Thread.currentThread().getContextClassLoader().getResource("");
    	URL itemURL = SubZeroSysTray.class.getResource(path);
    	return new File(URLDecoder.decode(itemURL.getFile(), "UTF-8"));
    }
	
    /**
     * Open log folder
     * @param causeFileExtNotAssociated
     */
    private static void openLogFolder(String causeFileExtNotAssociated) {
    	try {
    		// Open folder
    		Desktop.getDesktop().open(new File(PropertiesHelper.getLoggerAppenderFolderPath()));
    		if (causeFileExtNotAssociated != null) {
				// Open the folder because the specified file type is not associated and cannot be opened
				trayIcon.displayMessage(SUBZERO_POPUP_NAME, 
						String.format("No editor has been found for file type '%s'. The log folder has been opened instead", causeFileExtNotAssociated), TrayIcon.MessageType.WARNING);
			}
    	} catch (Exception e3) {
			trayIcon.displayMessage(SUBZERO_POPUP_NAME, "Error while opening log folder :(", TrayIcon.MessageType.ERROR);
			log.error("Error while opening log folder", e3);
		}
    }
    
    /**
     * Open config folder
     * @param causeFileExtNotAssociated
     */
    private static void openConfigFolder(String causeFileExtNotAssociated) {
    	try {
			// Open folder
			Desktop.getDesktop().open(getFileOrFolder("/"));
			if (causeFileExtNotAssociated != null) {
				// Open the folder because the specified file type is not associated and cannot be opened
				trayIcon.displayMessage(SUBZERO_POPUP_NAME, 
						String.format("No editor has been found for file type '%s'. The config folder has been opened instead", causeFileExtNotAssociated), TrayIcon.MessageType.WARNING);
			}
		}
		catch (Exception e2) {
			trayIcon.displayMessage(SUBZERO_POPUP_NAME, "Error while opening config foler :(", TrayIcon.MessageType.ERROR);
			log.error("Error while opening config foler", e2);
		}
    }
    
    /**
     * Switch pause mode
     * @param pauseMode
     */
    private static void switchPauseMode(boolean pauseMode) {
    	if (processIsRunning) {
    		// Process in running : do nothing
    		return;
    	}
    	    	
    	// Switch pause flag and relaunch automatic job
		paused = pauseMode;    		
		launchAutomaticJob(false);
		
    	// Switch icon
    	if (pauseMode) {
    		trayIcon.setImage(getImage(SUBZERO_ICON_PAUSED_PATH));
    	}
    	else {
    		trayIcon.setImage(getImage(SUBZERO_ICON_PATH));
    	}
		
    	// Prepare tooltip message
    	String message = String.format(SUBZERO_POPUP_DESCRIPTION, VersionConstants.SUBZERO_VERSION_NUMBER);
        if (pauseMode) {
        	message = SUBZERO_POPUP_PREFIX_PAUSED + message;
        }
        
        // Set tool tip message
        trayIcon.setToolTip(message);
        
        // Switch pause / resume menu item (and remove "Launch Now !" while in pause)        
        if (pauseMode) {
        	popup.remove(0);
        	popup.remove(0);
        	popup.insert(resumeMenuItem, 0);
        }
        else {
        	popup.remove(0);
        	popup.insert(launchNowMenuItem, 0);
        	popup.insert(pauseMenuItem, 1);
        }
        
        // Popup
        if (pauseMode) {
        	trayIcon.displayMessage(SUBZERO_POPUP_NAME, "Going to sleep...", TrayIcon.MessageType.INFO);
        }
        else {
        	trayIcon.displayMessage(SUBZERO_POPUP_NAME, "I'm awake !", TrayIcon.MessageType.INFO);
        }
    }
    
    /**
     * Create and show GUI
     */
    private static void createAndShowGUI() {
        // Check the SystemTray support
        if (!SystemTray.isSupported()) {
        	log.error("Error while creating SysTray application : not supported");
        	System.exit(1);
        }
        
        // Initialize UI components 
        popup = new PopupMenu();
        trayIcon = new TrayIcon(getImage(SUBZERO_ICON_PATH));
        tray = SystemTray.getSystemTray();
        
        // Create a popup menu components
        launchNowMenuItem = new MenuItem("Launch now !");
        pauseMenuItem = new MenuItem("Pause");
        resumeMenuItem = new MenuItem("Resume");
        MenuItem openWorkingFolderMenuItem = new MenuItem("Open Working folder");
        MenuItem openReportLogMenuItem = new MenuItem("Open Report Log");
        MenuItem openTechLogMenuItem = new MenuItem("Open Tech Log");
        MenuItem openLogFolderMenuItem = new MenuItem("Open Log Folder");
        MenuItem editConfigFileMenuItem = new MenuItem("Edit Config File");
        MenuItem openReloadConfigMenuItem = new MenuItem("Reload Config");
        MenuItem aboutMenuItem = new MenuItem("About");
        MenuItem exitMenuItem = new MenuItem("Exit");
        
        // Add components to popup menu
        popup.add(launchNowMenuItem);
        popup.add(pauseMenuItem);
        popup.addSeparator();
        popup.add(openWorkingFolderMenuItem);
        popup.addSeparator();
        popup.add(openReportLogMenuItem);
        popup.add(openTechLogMenuItem);
        popup.add(openLogFolderMenuItem);
        popup.addSeparator();
        popup.add(editConfigFileMenuItem);
        popup.add(openReloadConfigMenuItem);
        popup.addSeparator();
        popup.add(aboutMenuItem);
        popup.addSeparator();
        popup.add(exitMenuItem);
        
        trayIcon.setPopupMenu(popup);
        
        // Add the TrayIcon
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            log.error("Error while adding the TrayIcon", e);
            System.exit(1);
        }
        
        // Set tool tip message
        trayIcon.setToolTip(String.format(SUBZERO_POPUP_DESCRIPTION, VersionConstants.SUBZERO_VERSION_NUMBER));

        // Events capture :
        
        // ===========================
        // Double click
        trayIcon.addMouseListener(new MouseListener() {			
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() >= 2){
					if (paused) {
						// Resume
						switchPauseMode(false);
					}
					else {
						// Launch Now
						launchSubZeroProcessSafely(true);
					}
	            }
			}

			@Override
			public void mouseEntered(MouseEvent e) {			
			}

			@Override
			public void mouseExited(MouseEvent e) {		
			}

			@Override
			public void mousePressed(MouseEvent e) {		
			}

			@Override
			public void mouseReleased(MouseEvent e) {	
			}
		});
        
        // ===========================
        // Launch now
        launchNowMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				if (paused) {
					// Resume
					switchPauseMode(false);
				}
				else {
					// Launch Now
					launchSubZeroProcessSafely(true);
				}
            }
        });
        
        // ===========================
        // Pause
        pauseMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				switchPauseMode(true);
            }
        });
        
        // ===========================
        // Resume
        resumeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	switchPauseMode(false);
            }
        });
        
        // ===========================
        // Open Log
        openReportLogMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	try {
            		Desktop.getDesktop().open(new File(PropertiesHelper.getLoggerAppenderReportPath()));
				}
            	catch (IOException e1) {   		
            		openLogFolder(".html");        		            		
            	}
            	catch (Exception e1) {
					trayIcon.displayMessage(SUBZERO_POPUP_NAME, "Error while opening report log :(", TrayIcon.MessageType.ERROR);
					log.error("Error while opening report log", e1);
				}
            }
        });
        
        // ===========================
        // Open Tech Log
        openTechLogMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	try {
            		Desktop.getDesktop().open(new File(PropertiesHelper.getLoggerAppenderFilePath()));
            	}
            	catch (IOException e1) {   		
            		openLogFolder(".log");        		            		
            	}
            	catch (Exception e3) {
					trayIcon.displayMessage(SUBZERO_POPUP_NAME, "Error while opening tech log :(", TrayIcon.MessageType.ERROR);
					log.error("Error while opening tech log", e3);
				}
            }
        });
        
        // ===========================
        // Open Log Folder
        openLogFolderMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	openLogFolder(null);
            }
        });
        
        // ===========================
        // Open Working Folder
        openWorkingFolderMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	try {
            		Desktop.getDesktop().open(new File(PropertiesHelper.getWorkingFolderPath()));
            	}
            	catch (Exception e1) {
					trayIcon.displayMessage(SUBZERO_POPUP_NAME, "Error while opening working folder :(", TrayIcon.MessageType.ERROR);
					log.error("Error while opening working folder", e1);
				}
            }
        });
        
        // ===========================
        // Edit Config File
        editConfigFileMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	try {
            		// Edit File
            		Desktop.getDesktop().open(getFileOrFolder(PropertiesHelper.PROPERTIES_FILE_PATH));
            	}
            	catch (IOException e1) {   		
            		openConfigFolder(".properties");        		            		
            	}
            	catch (Exception e3) {
					trayIcon.displayMessage(SUBZERO_POPUP_NAME, "Error while opening config file :(", TrayIcon.MessageType.ERROR);
					log.error("Error while opening config file", e3);
				}
            }
        });
        
        // ===========================
        // Reload Config File
        openReloadConfigMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	try {
            		// Reload Configuration
            		PropertiesHelper.resetConfiguration();
            		trayIcon.displayMessage(SUBZERO_POPUP_NAME, "The configuration has been reloaded !", TrayIcon.MessageType.INFO);
            	} catch (Exception e1) {
					trayIcon.displayMessage(SUBZERO_POPUP_NAME, "Error while reloading config file :(", TrayIcon.MessageType.ERROR);
					log.error("Error while reloading config file", e1);
				}
            }
        });
        
        // ===========================
        // About
        aboutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	JOptionPane.showMessageDialog(null, String.format(SUBZERO_POPUP_ABOUT, VersionConstants.SUBZERO_VERSION_NUMBER, VersionConstants.SUBZERO_VERSION_DATE));                
            }
        });
        
        // ===========================
        // Exit
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	boolean confirmExit = true;
				if (processIsRunning) {
					int response = JOptionPane
							.showConfirmDialog(
									null,
									"A process is currently running. Are you sure you want to quit anyway ?",
									"Warning", JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE);
					if (response == JOptionPane.YES_OPTION) {
						confirmExit = true;
					}
					else {
						confirmExit = false;
					}
				}
            	if (confirmExit) {            	
	            	log.debug("SubZero - End");
	                tray.remove(trayIcon);
	                System.exit(0);
            	}
            }
        });
        
        // ===========================
        // Automatic Job
        launchAutomaticJob(true);
    }
    
    /**
     * Launch Automatic job
     * @param firstLaunch : if false, disable initDelay
     */
    private static void launchAutomaticJob(boolean firstLaunch) {
    	if (paused) {
    		// Pause mode : stop job if running
	    	if (timer != null && timer.isRunning()) {
	        	timer.stop();
	        }
    	}
    	else {
    		// Not in pause mode : start job if not started
    		if (timer == null || !timer.isRunning()) {
	    		int initDelay = 0;
	            int frequency = 0;
	            try {
	            	initDelay = PropertiesHelper.getAutomaticProcessInitDelay();
	            	frequency = PropertiesHelper.getAutomaticProcessFrequency();
	            } catch (Exception e) {
	            	log.error("Error while trying to get properties in configuration file", e);
	                return;
	            }        
	            timer = new Timer(frequency, new ActionListener() {
	                public void actionPerformed(ActionEvent e) {
	                	// No starting display message in job mode (only result message)
	    				launchSubZeroProcessSafely(false);
	                }
	            });
	            if (firstLaunch) {
	            	// Do not enable initDelay if already launched before (for instance, after a "pause-resume" event)
	            	timer.setInitialDelay(initDelay);
	            }
	            timer.setRepeats(true);
	            timer.start();
    		}
    	}
    }
}
