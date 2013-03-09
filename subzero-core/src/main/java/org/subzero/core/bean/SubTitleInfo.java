package org.subzero.core.bean;

import java.util.List;


/**
 * Subtitle Info
 * @author Julien
 *
 */
public class SubTitleInfo {
	private String subFileName;
	private String language;
	private List<String> extraFileNames;
	
	public String getSubFileName() {
		return subFileName;
	}


	public void setSubFileName(String subFileName) {
		this.subFileName = subFileName;
	}


	public String getLanguage() {
		return language;
	}


	public void setLanguage(String language) {
		this.language = language;
	}
		
	public List<String> getExtraFileNames() {
		return extraFileNames;
	}


	public void setExtraFileNames(List<String> extraFileNames) {
		this.extraFileNames = extraFileNames;
	}


	public SubTitleInfo(String subFileName, String language)
	{
		this.setSubFileName(subFileName);
		this.setLanguage(language);
	}
	
	public SubTitleInfo(String subFileName, String language, List<String> extraFileNames)
	{
		this.setSubFileName(subFileName);
		this.setLanguage(language);
		this.setExtraFileNames(extraFileNames);
	}
}
