package org.subzero.core.bean;

/**
 * Video file to process in SubZero launcher
 * @author Julien
 *
 */
public class VideoFileToProcess {

	private String videoFileName;
	private String workingFolderPath;
	
	/**
	 * Constructor
	 * 
	 * @param videoFileName
	 * @param workingFolderPath
	 */
	public VideoFileToProcess(String videoFileName, String workingFolderPath) {
		super();
		this.videoFileName = videoFileName;
		this.workingFolderPath = workingFolderPath;
	}

	/**
	 * @return the videoFileName
	 */
	public String getVideoFileName() {
		return videoFileName;
	}

	/**
	 * @param videoFileName the videoFileName to set
	 */
	public void setVideoFileName(String videoFileName) {
		this.videoFileName = videoFileName;
	}

	/**
	 * @return the workingFolderPath
	 */
	public String getWorkingFolderPath() {
		return workingFolderPath;
	}

	/**
	 * @param workingFolderPath the workingFolderPath to set
	 */
	public void setWorkingFolderPath(String workingFolderPath) {
		this.workingFolderPath = workingFolderPath;
	}
}
