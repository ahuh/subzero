package org.subzero.core.bean;

/**
 * Subtitle Search Result
 * @author Julien
 *
 */
public class SubSearchResult implements Comparable<SubSearchResult> {
	private String url;
	private String language;
	private int nbDownloads;
	private String release;
	private int score;
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public int getNbDownloads() {
		return nbDownloads;
	}
	public void setNbDownloads(int nbDownloads) {
		this.nbDownloads = nbDownloads;
	}
	public String getRelease() {
		return release;
	}
	public void setRelease(String release) {
		this.release = release;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public SubSearchResult(String url, String language, int nbDownloads, String release) {
		super();
		this.url = url;
		this.language = language;
		this.nbDownloads = nbDownloads;
		this.release = release;
	}
	
	@Override
	public String toString() {
		return String.format("%s ; %s ; %s ; %s", url, language, nbDownloads, release);
	}
	
	/**
	 * Compare method for List Sort : ordered by score (best to worse)
	 */
	@Override
	public int compareTo(SubSearchResult compareObject) {
		if (getScore() < compareObject.getScore()) {
			return 1;
		}
		else if (getScore() == compareObject.getScore()) {
			return 0;
		}
		else {
			return -1;
		}
	}	
}
