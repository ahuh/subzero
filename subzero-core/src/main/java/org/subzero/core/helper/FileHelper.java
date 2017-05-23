package org.subzero.core.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.subzero.core.bean.TvShowInfo;

/**
 * File Helper
 * @author Julien
 *
 */
public class FileHelper {
	
	public static final String FILE_SEPARATOR = "/";
	public static final String EXT_SEPARATOR = ".";
	
	/**
	 * Encode an unsafe URL (replace space char by %20)
	 * @param url
	 * @return
	 */
	public static String encodeUnsafeUrl(String url) {
		if (url == null) {
			return null;
		}
		else {
			return url.replace(" ", "%20");
		}
    }
	
	/**
	 * Replace extension of the filename
	 * @param fileName File name with current extension
	 * @param newExtension New extension to replace
	 * @return File name with new extension
	 */
	public static String replaceExtensionInFileName(String fileName, String newExtension) {
		int extPos = fileName.lastIndexOf(FileHelper.EXT_SEPARATOR);
		if (extPos > -1) {
			return fileName.substring(0, extPos) + FileHelper.EXT_SEPARATOR + newExtension;
		}
		else {
			return fileName + FileHelper.EXT_SEPARATOR + newExtension;
		}
	}
	
	/**
	 * Get the list of video files to process in Working folder
	 * 
	 * @param workingFolderPath
	 * @return
	 * @throws Exception
	 */
	public static List<String> getVideoFiles(String workingFolderPath) throws Exception {
		// Get files in folder
		File folder = new File(workingFolderPath);
		File[] listOfFiles = folder.listFiles(); 

		List<String> videoFileNameList = new ArrayList<String>();
		if (listOfFiles != null) {
			for (File file : listOfFiles) {
				if (file.isFile()) {
					// Item is a file (not a folder)
					Path filePath = file.toPath();
					String fileName = filePath.getFileName().toString();
					
					TvShowInfo tvShowInfo = TvShowInfoHelper.populateTvShowInfo(fileName, true);
					if (tvShowInfo != null) {
						// File is a video TV show file					
						if (checkVideoFileMinimumSize(file)) {
							// File has the required minimum size
							videoFileNameList.add(fileName);
						}
					}
				}
			}
		}
		return videoFileNameList;
	}
	
	/**
	 * Get the list of subfolders to process in Working folder (filtered from exclusion list)
	 * 
	 * @param workingFolderPath
	 * @return
	 * @throws Exception
	 */
	public static List<String> getSubFoldersToProcess(String workingFolderPath) throws Exception {
		// Get files in folder
		File folder = new File(workingFolderPath);
		File[] listOfFiles = folder.listFiles(); 

		List<String> folderNameList = new ArrayList<String>();
		if (listOfFiles != null) {
			for (File file : listOfFiles) {
				if (file.isDirectory()) {
					// Item is a file (not a folder)
					Path filePath = file.toPath();
					String folderName = filePath.getFileName().toString();
					
					boolean exclude = false;
					for (String excludeName : PropertiesHelper.getWorkingFolderExcludes()) {
						if (folderName.toLowerCase().equals(excludeName.toLowerCase())) {
							exclude = true;
						}
					}
					
					if (!exclude) {
						folderNameList.add(folderName);
					}
				}
			}
		}
		return folderNameList;
	}
	
	
	/**
	 * Get subtitle files in Working Folder
	 * 
	 * @param workingFolderPath
	 * @return
	 * @throws Exception
	 */
	public static List<String> getSubtitleFiles(String workingFolderPath) throws Exception {
		List<String> output = new ArrayList<String>();
		File workingFolder = new File(workingFolderPath);
		for (File file : workingFolder.listFiles()) {
			for (String subtitleExt : PropertiesHelper.getSubFileExtensions()) {
				if (file.getAbsolutePath().endsWith(FileHelper.EXT_SEPARATOR + subtitleExt)) {
					output.add(file.toPath().getFileName().toString());
				}
			}
		}
		return output;
	}
	
	/**
	 * Check if a video file has minimum size to be processed
	 * @param file
	 * @return
	 * @throws Exception
	 */
	private static Boolean checkVideoFileMinimumSize(File file) throws Exception {
		double minSize = PropertiesHelper.getInputFileMinimumSize();
		if (minSize > 0) {
			double fileSize = file.length();
			double fileSizeInMB = (fileSize / (1024*1024));
			if (fileSizeInMB < minSize) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Does the file exist in Working Folder
	 * @param workingFolderPath
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static boolean doesWorkingFileExist(String workingFolderPath, String fileName) throws Exception {
		String filePath = workingFolderPath + FileHelper.FILE_SEPARATOR + fileName;
		File file = new File(filePath);
		return file.exists();
	}
	
	/**
	 * Delete a Working folder file
	 * @param workingFolderPath
	 * @param fileName
	 * @throws Exception
	 */
	public static void deleteWorkingFile(String workingFolderPath, String fileName) throws Exception {
		String filePath = workingFolderPath + FileHelper.FILE_SEPARATOR + fileName;
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}
	}
	
	/**
	 * Rename a Working folder file
	 * @param workingFolderPath
	 * @param oldFileName
	 * @param newFileName
	 * @throws Exception
	 */
	public static void renameWorkingFile(String workingFolderPath, String oldFileName, String newFileName) throws Exception {
		String filePath = workingFolderPath + FileHelper.FILE_SEPARATOR + oldFileName;
		String targetFilePath = workingFolderPath + FileHelper.FILE_SEPARATOR + newFileName;
				
		File file = new File(filePath);
		if (file.exists()) {
			file.renameTo(new File(targetFilePath));
		}
	}
	
	/**
	 * Move a Working folder file to a folder (keep file name)
	 * @param workingFolderPath
	 * @param fileName
	 * @param outputFolderPath
	 * @throws Exception
	 */
	public static void moveWorkingFileToFolder(String workingFolderPath, String fileName, String outputFolderPath) throws Exception {		
		moveWorkingFileToFolder(workingFolderPath, fileName, outputFolderPath, fileName);
	}
	
	/**
	 * Move a Working folder file to a folder (change file name)
	 * @param workingFolderPath
	 * @param workingFileName
	 * @param outputFolderPath
	 * @param outputFilename
	 * @throws Exception
	 */
	public static void moveWorkingFileToFolder(String workingFolderPath, String workingFileName, String outputFolderPath, String outputFilename) 
			throws Exception {
		
		String workingfilePath = workingFolderPath + FileHelper.FILE_SEPARATOR + workingFileName;
		String targetFilePath = outputFolderPath + FileHelper.FILE_SEPARATOR + outputFilename;
		
		FileHelper.ensureFolder(outputFolderPath);
		
		File workingFile = new File(workingfilePath);
		File targetFile = new File(targetFilePath);
		if (targetFile.exists()) {
			// Delete target file before move
			targetFile.delete();
		}
		if (workingFile.exists()) {
			workingFile.renameTo(new File(targetFilePath));
		}
	}
	
	/**
	 * Ensure that the folder exists (create all folders in path if neeeded)
	 * @param folderPath
	 */
	public static void ensureFolder(String folderPath) {
		File folder = new File(folderPath);
		folder.mkdirs();
	}
	
	/**
	 * Unzip the first Subtitle file in ZIP file
	 * @param workingFolderPath
	 * @param zipFilePath
	 * @param outputSubFileNameWithoutFileType
	 * @return outputSubFileName or null is not found 
	 * @throws IOException 
	 */
	public static String unZipWorkingFirstSubFile(String workingFolderPath, String zipFileName, String outputSubFileNameWithoutFileType) throws Exception {		
		String outputSubFileName = null;
		
		// loop through all subfile extensions in properties
		for (String subFileExtension : PropertiesHelper.getSubFileExtensions()) {
			String subFileExtensionWithDot = FileHelper.EXT_SEPARATOR + subFileExtension;
			
			//get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(workingFolderPath + FileHelper.FILE_SEPARATOR + zipFileName));
			
			//get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();
	
			while(ze!=null){
				String fileName = ze.getName();
							
				if (fileName.toLowerCase().endsWith(subFileExtensionWithDot.toLowerCase())) {
					// File in ZIP is subtitle
					File newFile = new File(workingFolderPath
							+ FileHelper.FILE_SEPARATOR + outputSubFileNameWithoutFileType
							+ subFileExtensionWithDot);
		
					FileOutputStream fos = new FileOutputStream(newFile);
	
					int len;
					byte[] buffer = new byte[1024];
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
	
					fos.close();
					
					outputSubFileName = newFile.getName();
					
					break;
				}
				
				ze = zis.getNextEntry();
			}
	
			zis.closeEntry();
			zis.close();
			
			if (outputSubFileName != null) {
				// Subtitle found : end
				break;
			}
		}

		return outputSubFileName;
	}
}
