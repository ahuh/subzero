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
	 * Get the list of video files to process in Working folder
	 * @return
	 * @throws Exception
	 */
	public static List<String> getWorkingVideoFiles() throws Exception {
		// Get files in folder
		File folder = new File(PropertiesHelper.getWorkingFolderPath());
		File[] listOfFiles = folder.listFiles(); 

		List<String> videoFileNameList = new ArrayList<String>();
		for (File file : listOfFiles) 
		{
			if (file.isFile()) 
			{
				// Item is a file (not a folder)
				Path filePath = file.toPath();
				String fileName = filePath.getFileName().toString();
				
				TvShowInfo tvShowInfo = TvShowInfoHelper.populateTvShowInfo(fileName, true);
				if (tvShowInfo != null) {
					videoFileNameList.add(fileName);
				}
			}
		}
		return videoFileNameList;
	}
	
	/**
	 * Does the file exist in Working Folder
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static boolean doesWorkingFileExist(String fileName) throws Exception {
		String filePath = PropertiesHelper.getWorkingFolderPath() + File.separator + fileName;
		File file = new File(filePath);
		return file.exists();
	}
	
	/**
	 * Delete a Working folder file
	 * @param fileName
	 * @throws Exception
	 */
	public static void deleteWorkingFile(String fileName) throws Exception {
		String filePath = PropertiesHelper.getWorkingFolderPath() + File.separator + fileName;
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}
	}
	
	/**
	 * Rename a Working folder file
	 * @param oldFileName
	 * @param newFileName
	 * @throws Exception
	 */
	public static void renameWorkingFile(String oldFileName, String newFileName) throws Exception {
		String filePath = PropertiesHelper.getWorkingFolderPath() + File.separator + oldFileName;
		String targetFilePath = PropertiesHelper.getWorkingFolderPath() + File.separator + newFileName;
				
		File file = new File(filePath);
		if (file.exists()) {
			file.renameTo(new File(targetFilePath));
		}
	}
	
	/**
	 * Move a Working folder file to an output folder
	 * @param fileName
	 * @param outputFolderPath
	 * @throws Exception
	 */
	public static void moveWorkingFileToOutputFolder(String fileName, String outputFolderPath) throws Exception {
		String filePath = PropertiesHelper.getWorkingFolderPath() + File.separator + fileName;
		String targetFilePath = outputFolderPath + File.separator + fileName;
		
		FileHelper.ensureFolder(outputFolderPath);
		
		File file = new File(filePath);
		if (file.exists()) {
			file.renameTo(new File(targetFilePath));
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
	 * @param zipFilePath
	 * @param outputSubFileNameWithoutFileType
	 * @return outputSubFileName or null is not found 
	 * @throws IOException 
	 */
	public static String unZipWorkingFirstSubFile(String zipFileName, String outputSubFileNameWithoutFileType) throws Exception {		
		String outputSubFileName = null;
		
		// loop through all subfile extensions in properties
		for (String subFileExtension : PropertiesHelper.getSubFileExtensions()) {
			String subFileExtensionWithDot = "." + subFileExtension;
			
			//get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(PropertiesHelper.getWorkingFolderPath() + File.separator + zipFileName));
			
			//get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();
	
			while(ze!=null){
				String fileName = ze.getName();
							
				if (fileName.toLowerCase().endsWith(subFileExtensionWithDot.toLowerCase())) {
					// File in ZIP is subtitle
					File newFile = new File(PropertiesHelper.getWorkingFolderPath()
							+ File.separator + outputSubFileNameWithoutFileType
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
