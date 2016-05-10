package org.subzero.core.bean;

import java.util.List;

/**
 * TV Show Info
 * @author Julien
 *
 */
public class TvShowInfo {
	private String inputVideoFileName;
	private Integer season;
	private String serie;
	private List<Integer> episodes;
	private String title;
	private String fileType;
	private String releaseGroup;
	private String cleanedReleaseGroup;
	
	public String getInputVideoFileName() {
		return inputVideoFileName;
	}
	public void setInputVideoFileName(String inputVideoFileName) {
		this.inputVideoFileName = inputVideoFileName;
	}
	public Integer getSeason() {
		return season;
	}
	public void setSeason(Integer season) {
		this.season = season;
	}
	public String getSerie() {
		return serie;
	}
	public void setSerie(String serie) {
		this.serie = serie;
	}
	public List<Integer> getEpisodes() {
		return episodes;
	}
	public void setEpisodes(List<Integer> episodes) {
		this.episodes = episodes;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public String getReleaseGroup() {
		return releaseGroup;
	}
	public void setReleaseGroup(String releaseGroup) {
		this.releaseGroup = releaseGroup;
	}
	public String getCleanedReleaseGroup() {
		return cleanedReleaseGroup;
	}
	public void setCleanedReleaseGroup(String cleanedReleaseGroup) {
		this.cleanedReleaseGroup = cleanedReleaseGroup;
	}
	public TvShowInfo()
	{
	}
}
