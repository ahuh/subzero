package org.subzero.core.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

/**
 * Properties file Helper
 * @author Julien
 *
 */
public class PropertiesHelper {	
	
	public final static String VERSION_FILE_PATH = "/Version.properties";
	public final static String PROPERTIES_FILE_PATH = "/SubZero.properties";
	public final static String JAVA_ARG_CONFIGDIR= "configDir";
	
	private final static int COMMAND_LINE_ARGS_SIZE = 100;
	private final static String BASEFOLDER_TOKEN = "{basefolder}";
	private final static String WORKINGFOLDER_TOKEN = "{workingfolder}";
	private final static String MKVMERGEPATH_TOKEN = "{mkvmergePath}";
	private final static String SEPARATOR_TOKEN = ";";
		
    private static Properties properties;
    
    /**
     * Configure the logger file
     */
    public static void configureLogger() {    	
    	try {
    		resolveLoggerAppenderReportPath();
    		resolveLoggerAppenderFilePath();
			PropertyConfigurator.configure(getPropertiesSingleton());
		} catch (Exception e) {
			System.out.println(String.format("ERROR : impossible to initiliaze log4j with configuration file '%s'", PROPERTIES_FILE_PATH));			
			e.printStackTrace();
		}
    }

    /**
     * Get Properties Singleton
     * @return
     * @throws Exception
     */
    private static Properties getPropertiesSingleton() throws Exception
    {
    	if (properties == null) {
    		properties = new Properties();	
    		// Load Version file first (only from inside JAR)
    		properties = loadPropertiesFromFile(properties, VERSION_FILE_PATH, true);
    		// Load Properties file
    		properties = loadPropertiesFromFile(properties, PROPERTIES_FILE_PATH, false);
    	}
    	return properties;
    }
    
    /**
     * Load properties from file
     * @param properties
     * @param propertiesFilePath
     * @param onlyInsideJAR
     * @return
     * @throws Exception
     */
    private static Properties loadPropertiesFromFile(Properties properties, String propertiesFilePath, Boolean onlyInsideJAR) throws Exception {
    	InputStream stream = null;
    	if (onlyInsideJAR) {
    		// Properties file must be loaded only from inside the jar
    		stream = PropertiesHelper.class.getResourceAsStream(propertiesFilePath);
    	}
    	else {
    		// Properties file must be load outside the jar, and generated if not present
    		
    		// Test #1: is program executed inside a JAR ?
    		if (checkIfInJar()) {
    			
    			// Test #2: is a 'configDir' arg specified ? 
	    		String configDir = System.getProperty(JAVA_ARG_CONFIGDIR);
	    		if (configDir == null) {
	    			// No 'configDir' : use the JAR dir instead 
	    			configDir = ClassLoader.getSystemClassLoader().getResource(".").getPath();
	    		}
    			File configFile = new File (configDir + propertiesFilePath);
    			
    			// Test #3: does the 'configDir' contain a properties file ?
    			if (!configFile.exists()) {    				
    				// If not : copy it from inside jar
    				InputStream streamInsideJar = PropertiesHelper.class.getResourceAsStream(propertiesFilePath);
    	    		Files.copy(streamInsideJar, configFile.toPath());
    	    		streamInsideJar.close();
    			}
    			
    			// => Load the config file
    			stream = new FileInputStream(configFile);
    		}
    		else {
    			// Program is not executed in a JAR file (classes in directories)
    			// => load properties file from 
        		URL propertiesURL = PropertiesHelper.class.getResource(propertiesFilePath);
        		File propertiesFileInClasses = new File(URLDecoder.decode(propertiesURL.getFile(), "UTF-8"));
        		if (propertiesFileInClasses.exists()) {
        			stream = new FileInputStream(propertiesFileInClasses);
        		}
        		else {        			
        			System.out.println(String.format("ERROR : impossible to load properties file in path '%s'", 
        				propertiesFileInClasses.getAbsolutePath()));
        		}
    		}
    	}
		properties.load(stream);
		stream.close();
		return properties;
    }
    
    /**
     * Check if the code is executed inside a jar (release) or not (dev with IDE) 
     * Reference : http://www.rgagnon.com/javadetails/java-0391.html
     * @return
     */
	private static boolean checkIfInJar() {
		String className = PropertiesHelper.class.getName().replace('.', '/');
		String classJar = PropertiesHelper.class.getResource("/" + className + ".class").toString();
		return classJar.startsWith("jar:");
	}

    /**
     * Reset configuration
     * @throws Exception
     */
    public static void resetConfiguration() throws Exception {
    	// Reset properties
    	resetProperties();
    	// Reset logger configuration
    	configureLogger();
    }    
    
    /**
     * Reset Properties
     */
    public static void resetProperties() {
    	if (properties != null) {
    		properties = null;
    	}
    }

    /**
     * Get a property after getting the properties singleton
     * @param propertyName
     * @return
     * @throws Exception
     */
    private static String getPropertyValue(String propertyName) throws Exception {
    	return getPropertiesSingleton().getProperty(propertyName);
    }
    
    /**
     * Get a property value as boolean
     * @param propertyName
     * @return
     * @throws Exception
     */
    private static boolean getBooleanPropertyValue(String propertyName) throws Exception {
    	String value = getPropertyValue(propertyName);
    	if (value != null) {
    		return Boolean.parseBoolean(value);
    	}
    	else {
    		return false;
    	}
    }
    
    /**
     * Get a property value as integer
     * @param propertyName
     * @return
     * @throws Exception
     */
    private static int getIntegerPropertyValue(String propertyName) throws Exception {
    	String value = getPropertyValue(propertyName);
    	if (value != null) {
    		return Integer.parseInt(value);
    	}
    	else {
    		return 0;
    	}
    }
    
    /**
     * Get a property value as double
     * @param propertyName
     * @return
     * @throws Exception
     */
    private static double getDoublePropertyValue(String propertyName) throws Exception {
    	String value = getPropertyValue(propertyName);
    	if (value != null) {
    		return Double.parseDouble(value);
    	}
    	else {
    		return 0;
    	}
    }
        
    /**
     * Get a property value as array
     * @param propertyName
     * @return
     * @throws Exception 
     */
    public static String[] getArrayPropertyValue(String propertyName) throws Exception {
    	String value = getPropertyValue(propertyName);
    	if (value != null) {
    		return value.split(SEPARATOR_TOKEN);
    	}
    	else {
    		return new String[0];
    	}
    }
    
    /**
     * Set a property after getting the properties singleton
     * @param propertyName
     * @param propertyValue
     * @throws Exception
     */
    private static void setPropertyValue(String propertyName, String propertyValue) throws Exception
    {
    	getPropertiesSingleton().setProperty(propertyName, propertyValue);
    }
    
    /**
     * Resolve the Logger Appender Report Path with the BASEFOLDER_TOKEN and BASEFOLDER_TOKEN and WORKINGFOLDER_TOKEN
     * @throws Exception 
     */
    private static void resolveLoggerAppenderReportPath() throws Exception
    {
    	String propertyName = "log4j.appender.report.File";
    	String resolvedPath = getPropertyValue(propertyName)
    			.replace(BASEFOLDER_TOKEN, PropertiesHelper.getBaseFolderPath())
    			.replace(WORKINGFOLDER_TOKEN, PropertiesHelper.getWorkingFolderPath());
    	setPropertyValue(propertyName, resolvedPath);	
    }
    
    /**
     * Resolve the Logger Appender File Path with the BASEFOLDER_TOKEN and BASEFOLDER_TOKEN and WORKINGFOLDER_TOKEN
     * @throws Exception 
     */
    private static void resolveLoggerAppenderFilePath() throws Exception
    {
    	String propertyName = "log4j.appender.file.File";
    	String resolvedPath = getPropertyValue(propertyName)
    			.replace(BASEFOLDER_TOKEN, PropertiesHelper.getBaseFolderPath())
    			.replace(WORKINGFOLDER_TOKEN, PropertiesHelper.getWorkingFolderPath());
    	setPropertyValue(propertyName, resolvedPath);
    }
    
    /**
     * getLoggerAppenderFilePath : get the active logger report path 
     * @return
     * @throws Exception
     */
    public static String getLoggerAppenderReportPath() throws Exception {
    	return getPropertyValue("log4j.appender.report.File");
    }
    
    /**
     * getLoggerAppenderFilePath : get the active logger file path 
     * @return
     * @throws Exception
     */
    public static String getLoggerAppenderFilePath() throws Exception {
    	return getPropertyValue("log4j.appender.file.File");
    }
    
    /**
     * getLoggerAppenderFolderPath : get the parent folder path of logger files
     * @return
     * @throws Exception
     */
    public static String getLoggerAppenderFolderPath() throws Exception {
    	File file = new File(getPropertyValue("log4j.appender.file.File"));
    	return file.getParent();
    }
    
    /**
     * getVersion
     * @return
     * @throws Exception
     */
    public static String getVersion() throws Exception {
    	return getPropertyValue("subzero.version");
    }
    
    /**
     * getReleaseDate
     * @return
     * @throws Exception
     */
    public static String getReleaseDate() throws Exception {
    	return getPropertyValue("subzero.release.date");
    }
    
    /**
     * getBaseFolderPath
     * @return
     * @throws Exception
     */
    public static String getBaseFolderPath() throws Exception {
    	return getPropertyValue("subzero.basefolder.path");
    }
    
    /**
     * getWorkingFolderRecursiveMode
     * @return
     * @throws Exception
     */
    public static boolean getWorkingFolderRecursiveMode() throws Exception {
    	return getBooleanPropertyValue("subzero.workingfolder.recursive.mode");
    }
    
    /**
     * getWorkingFolderExcludes : multiple properties value in file, separator SEPARATOR_TOKEN
     * @return
     * @throws Exception
     */
    public static String[] getWorkingFolderExcludes() throws Exception {
    	return getArrayPropertyValue("subzero.workingfolder.excludes");
    }
    
    /**
     * getWorkingFolderShuffleMode
     * @return
     * @throws Exception
     */
    public static boolean getWorkingFolderShuffleMode() throws Exception {
    	return getBooleanPropertyValue("subzero.workingfolder.shuffle.mode");
    }
    
    /**
     * getWorkingFolderPath
     * @return
     * @throws Exception
     */
    public static String getWorkingFolderPath() throws Exception {
    	String propVal = getPropertyValue("subzero.workingfolder.path");
    	if (propVal != null) {
    		propVal = propVal
    				.replace(BASEFOLDER_TOKEN, PropertiesHelper.getBaseFolderPath());
    	}
    	return propVal;
    }
    
    /**
     * getOutputFolderPath
     * @param serie
     * @param season
     * @return
     * @throws Exception
     */
    public static String getOutputFolderPath(String serie, String season) throws Exception {
    	String propVal = getPropertyValue("subzero.outputfolder.path");
    	if (propVal != null) {
    		propVal = propVal
    				.replace(BASEFOLDER_TOKEN, PropertiesHelper.getBaseFolderPath())
        			.replace(WORKINGFOLDER_TOKEN, PropertiesHelper.getWorkingFolderPath())
        			.replace("{serie}", serie)
        			.replace("{season}", season);
    	}
    	return propVal;
    }
    
    /**
     * getAutomaticProcessInitDelay : property value in file in seconds
     * @return Value in milliseconds
     * @throws Exception
     */
    public static int getAutomaticProcessInitDelay() throws Exception {
    	return getIntegerPropertyValue("subzero.automaticprocess.initdelay")*1000;
    }
    
    /**
     * getAutomaticProcessFrequency : property value in file in minutes
     * @return Value in milliseconds
     * @throws Exception
     */
    public static int getAutomaticProcessFrequency() throws Exception {
    	return getIntegerPropertyValue("subzero.automaticprocess.frequency")*60*1000;
    }
    
    /**
     * getMkvMergeMoveMkvMergeCommandLine
     * @param outputFilePath
     * @param language
     * @param subFilePath
     * @param inputVideoFilePath
     * @return
     * @throws Exception
     */
    public static ArrayList<String> getMkvMergeMoveMkvMergeCommandLine(String outputFilePath, String language, String subFilePath, String inputVideoFilePath) throws Exception {    	
    	ArrayList<String> argTab = new ArrayList<String>();
    	for (int i = 0; i<COMMAND_LINE_ARGS_SIZE; i++) {
    		String propVal = getPropertyValue("plugin.mkvmergemove.mkvmerge.commandline." + i);
    		if (propVal == null) {
    			break;
    		}
    		else {
    			argTab.add(propVal.replace(MKVMERGEPATH_TOKEN, getMkvMergePath())
    					.replace("{outputFilePath}", outputFilePath)
    			    	.replace("{language}", language)
    			    	.replace("{subFilePath}", subFilePath)
    			    	.replace("{inputVideoFilePath}", inputVideoFilePath));
    		}
    	}
    	
    	return argTab;
    }    
    
    /**
     * getMkvMergeMoveOriFilesKeep
     * @return
     * @throws Exception
     */
    public static boolean getMkvMergeMoveOriFilesKeep() throws Exception {
    	return getBooleanPropertyValue("plugin.mkvmergemove.orifiles.keep");
    }
    
    /**
     * getMkvMergeMoveOriFilesFolderPath
     * @return
     * @throws Exception
     */
    public static String getMkvMergeMoveOriFilesFolderPath() throws Exception {
    	String propVal = getPropertyValue("plugin.mkvmergemove.orifiles.folderpath");
    	if (propVal != null) {
    		propVal = propVal
    				.replace(BASEFOLDER_TOKEN, PropertiesHelper.getBaseFolderPath())
        			.replace(WORKINGFOLDER_TOKEN, PropertiesHelper.getWorkingFolderPath());
    	}
    	return propVal;
    }
    
    /**
     * getSubLeecherPlugins : multiple properties value in file, separator SEPARATOR_TOKEN
     * @return
     * @throws Exception
     */
    public static String[] getSubLeecherPlugins() throws Exception {
    	return getArrayPropertyValue("subzero.subleecher.plugins");
    }    
    
    /**
     * getSubLeecherLanguages : multiple properties value in file, separator SEPARATOR_TOKEN
     * @return
     * @throws Exception
     */
    public static String[] getSubLeecherLanguages() throws Exception {
    	return getArrayPropertyValue("subzero.subleecher.languages");
    }
        
    /**
     * getSubLeecherReleaseGroupRequired
     * @return
     * @throws Exception
     */
    public static boolean getSubLeecherReleaseGroupRequired() throws Exception {
    	return getBooleanPropertyValue("subzero.subleecher.releasegroup.required");
    }
    
    /**
     * getSubCheckerBypassKeywords : multiple properties value in file, separator SEPARATOR_TOKEN
     * @return
     * @throws Exception
     */
    public static String[] getSubLeecherReleaseGroupFakeList() throws Exception {
    	return getArrayPropertyValue("subzero.subleecher.releasegroup.fake.list");
    }
    
    /**
     * getMkMergePath
     * @return
     * @throws Exception
     */
    public static String getMkvMergePath() throws Exception {
    	return getPropertyValue("subzero.mkvmerge.path");
    }

    /**
     * getSubCheckerBypassKeywords : multiple properties value in file, separator SEPARATOR_TOKEN
     * @param language
     * @return
     * @throws Exception
     */
    public static String[] getSubCheckerBypassKeywords(String language) throws Exception {
    	return getArrayPropertyValue(String.format("subzero.subchecker.bypass.keywords.%s", language.toLowerCase()));
    }
    
    /**
     * getSubCheckerMkvCheckAudioTrack
     * @param language
     * @return
     * @throws Exception
     */
    public static boolean getSubCheckerMkvCheckAudioTrack(String language) throws Exception {
    	return getBooleanPropertyValue(String.format("plugin.subcheckermkv.check.audio.track.%s", language.toLowerCase()));
    }
    
    /**
     * getSubCheckerMkvVideoFileExtensions : multiple properties value in file, separator SEPARATOR_TOKEN
     * @return
     * @throws Exception
     */
    public static String[] getSubCheckerMkvVideoFileExtensions() throws Exception {
    	return getArrayPropertyValue("plugin.subcheckermkv.videofile.extensions");
    }
    
    /**
     * getSubCheckerMkvMkvMergeCommandLine
     * @param inputVideoFilePath
     * @param uiLanguage
     * @return
     * @throws Exception
     */
    public static ArrayList<String> getSubCheckerMkvMkvMergeCommandLine(String inputVideoFilePath, String language) throws Exception {    	
    	ArrayList<String> argTab = new ArrayList<String>();
    	for (int i = 0; i<COMMAND_LINE_ARGS_SIZE; i++) {
    		String propVal = getPropertyValue("plugin.subcheckermkv.mkvmerge.commandline." + i);
    		if (propVal == null) {
    			break;
    		}
    		else {
    			argTab.add(propVal.replace(MKVMERGEPATH_TOKEN, getMkvMergePath())
    					.replace("{inputVideoFilePath}", inputVideoFilePath)
    			    	.replace("{language}", language));
    		}
    	}
    	
    	return argTab;
    }   
        
    /**
     * getSubCheckerlugins : multiple properties value in file, separator SEPARATOR_TOKEN
     * @return
     * @throws Exception
     */
    public static String[] getSubCheckerPlugins() throws Exception {
    	return getArrayPropertyValue("subzero.subchecker.plugins");
    }
    
    /**
     * getPostProcessPlugin
     * @return
     * @throws Exception
     */
    public static String getPostProcessPlugin() throws Exception {
    	return getPropertyValue("subzero.postprocess.plugin");
    }
    
    /**
     * getVideoFileExtensions : multiple properties value in file, separator SEPARATOR_TOKEN
     * @return
     * @throws Exception
     */
    public static String[] getVideoFileExtensions() throws Exception {
    	return getArrayPropertyValue("subzero.videofile.extensions");
    }
    
    /**
     * getInputFileNoiseStrings : multiple properties value in file, separator SEPARATOR_TOKEN
     * @return
     * @throws Exception
     */
    public static String[] getInputFileNoiseStrings() throws Exception {
    	return getArrayPropertyValue("subzero.inputfile.noisestrings");
    }
    
    /**
     * getInputFileMinimumSize
     * @return
     * @throws Exception
     */
    public static double getInputFileMinimumSize() throws Exception {
    	return getDoublePropertyValue("subzero.inputfile.minimum.size");
    }
    
    /**
     * getSubFileExtensions : multiple properties value in file, separator SEPARATOR_TOKEN
     * @return
     * @throws Exception
     */
    public static String[] getSubFileExtensions() throws Exception {
    	return getArrayPropertyValue("subzero.subfile.extensions");
    }
    
    /**
     * getSubfileLanguageSuffix
     * @param language
     * @return
     * @throws Exception
     */
    public static String getSubfileLanguageSuffix(String language) throws Exception {
    	String propVal = getPropertyValue("subzero.subfile.languagesuffix");
    	if (propVal != null) {
    		propVal = propVal
		    	.replace("{language}", language.substring(0, 2).toLowerCase())
		    	.replace("{LANGUAGE}", language.substring(0, 2).toUpperCase());
    	}
    	return propVal;
    }  
}

