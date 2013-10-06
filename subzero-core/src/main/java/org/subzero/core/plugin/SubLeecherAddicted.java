package org.subzero.core.plugin;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.subzero.core.bean.SubSearchResult;
import org.subzero.core.bean.SubTitleInfo;
import org.subzero.core.bean.TvShowInfo;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.helper.TvShowInfoHelper;
import org.subzero.core.subleecher.SubLeecherBase;
import org.subzero.core.subleecher.SubLeecherHelper;

/**
 * SubLeecher plugin for web site http://www.addic7ed.com
 * @author Julien
 *
 */
public class SubLeecherAddicted extends SubLeecherBase  {

	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubLeecherAddicted.class);
	
	// Constants
	private static final String ADDIC7ED_URL = "http://www.addic7ed.com";
	private static final int QUERY_TIME_OUT = 30000;
	private static final String ALT_DOWNLOAD_CHARSET = "UTF-8";
	
	/**
	 * Extract the number of download from the search results description
	 * @param description Addicted description example : 48 times edited · 52 Downloads · 979 sequences
	 * @return
	 */
	private int extractNbDownloads(String description)
	{
		if (description == null || description.equals("")) {
			return 0;
		}
		
		// Iterate through parts of descriptions delimited by "·"
		for (String descriptionPart : description.split("·"))
		{
			if (SubLeecherHelper.looseMatchContains(descriptionPart, "downloads")) {
				// Return the number if part contains "Downloads"
				return Integer.parseInt(SubLeecherHelper.keepOnlyDigits(descriptionPart));
			}
		}
		
		return 0;
	}
	
	
	/**
	 * Leech the subtitles of the TV show from the web site
	 * @return Output file name and language of the leeched subtitle or null if no subtitle found
	 * 
	 */
	@Override
	public SubTitleInfo leechSub()
	{
		try
		{
			log.debug(String.format("SubLeecher Addicted - Start - File='%s' ; Language='%s'", this.tvShowInfo.getInputVideoFileName(), this.subLanguage));
			
			String episode = TvShowInfoHelper.getShortNameTypeX(this.tvShowInfo);

			// ********************************************
			// 1 - Search Page
			
			// Connect to search page & search the episode
			log.debug(String.format("Search for episode '%s' ...", episode));
			String searchUrl = ADDIC7ED_URL + "/search.php?search=" + episode;
			Document docSearch = Jsoup.connect(searchUrl)
					.timeout(QUERY_TIME_OUT)
					.get();
			
			// Iterative through search results
			Element aEpisodeMatch = null;
			for (Element aEpisode : docSearch.select("a[href^=serie/]"))
			{
				String aText = aEpisode.text();
				TvShowInfo aEpisodeInfo = TvShowInfoHelper.populateTvShowInfoFromFreeText(aText);
				
				// Check if the result text : 
				// - starts with the desired serie name
				// - has the season search 
				// - has at least one episode search 
				// => select the first one matching only
				if (aEpisodeInfo != null
						&& SubLeecherHelper.looseMatchStartsWith(aEpisodeInfo.getSerie(), this.tvShowInfo.getSerie())
						&& aEpisodeInfo.getSeason() == this.tvShowInfo.getSeason()
						&& TvShowInfoHelper.testIfOneEpisodeMatches(this.tvShowInfo.getEpisodes(), aEpisodeInfo.getEpisodes()))
				{					
					log.debug(String.format("> Matching result found : '%s'", aText));
					aEpisodeMatch = aEpisode;
					break;
				}
				else {
					log.debug(String.format("> Non matching result : '%s'", aText));
				}
			}
			
			if (aEpisodeMatch == null) {
				// No episode found => end
				log.debug("> No match in result !");
				return null;
			}

			// Get the episode URL from link
			String episodeUrl = ADDIC7ED_URL + "/" + aEpisodeMatch.attr("href");
						
			// ********************************************
			// 2 - Episode Page
			
			// Connect to episode page
			log.debug(String.format("Search for subtitles for episode '%s' ...", episode));
			Document docEpisode = Jsoup.connect(episodeUrl)
					.timeout(QUERY_TIME_OUT)
					.header("Referer", searchUrl)
					.get();	

			// Browse lines in subtitles table
			Elements tdLanguageList = docEpisode.select("td[class=language]");
			List<SubSearchResult> subSearchResults = new ArrayList<SubSearchResult>();
			for (Element tdLanguage : tdLanguageList)
			{
				String tdLanguageText = tdLanguage.text();
				if (!SubLeecherHelper.looseMatchContains(tdLanguageText, this.subLanguage)) {
					// Language mismatch => next line
					continue;
				}

				Element tdStatus = tdLanguage.nextElementSibling();
				String tdStatusText = tdStatus.text();
				if (!SubLeecherHelper.looseMatchStartsWith(tdStatusText, "completed")) {
					// Language present but not Completed => next line
					continue;
				}

				Element tdDownload = tdStatus.nextElementSibling();
				if (tdDownload == null) {
					// No download button for language => next line
					continue;
				}

				Element aDownloadMatch = null;
				Elements aDownloads = tdDownload.select("a[class=buttonDownload]");
				if (aDownloads == null || aDownloads.size() == 0) {
					// No download button for language => next line
					continue;
				}
				else {
					// Download buttons found for language
					// - Try to get the button entitled "Download" ... 
					aDownloadMatch = aDownloads.select("a:contains(download)").first();
					if (aDownloadMatch == null) {
						// - Try to get the button entitled "most updated" ...
						aDownloadMatch = aDownloads.select("a:contains(most updated)").first();
					}
					if (aDownloadMatch == null) {
						// - Take the first button available
						aDownloadMatch = aDownloads.first();
					}
				}
				
				// Get the download URL
				String downloadUrl = ADDIC7ED_URL + aDownloadMatch.attr("href");
				
				// Get the episode Release (OPTIONAL)
				// => in first line of the table
				Element tdRelease = aDownloadMatch.parent().parent().parent().select("td[class=NewsTitle]").first();
				String episodeRelease = "";
				if (tdRelease != null) {
					// Remove prefix "Version " before group name
					episodeRelease = tdRelease.text().replace("Version ", "");
				}
				
				// Get the description (contains number of download) (OPTIONAL)
				// => in next line of the table
				int episodeNbDownload = 0;
				Element tdDescription = aDownloadMatch.parent().parent().nextElementSibling().select("td[class=newsDate]").first();	
				if (tdDescription != null) {
					episodeNbDownload = extractNbDownloads(tdDescription.text());
				}
				
				subSearchResults.add(new SubSearchResult(downloadUrl, tdLanguageText, episodeNbDownload, episodeRelease));
			}
			
			// Evaluate the matching score and sort the subtitle results !
			List<SubSearchResult> scoredSubs = SubLeecherHelper.evaluateScoreAndSort(subSearchResults, this.tvShowInfo.getReleaseGroup(), this.releaseGroupMatchRequired);
			if (scoredSubs == null || scoredSubs.size() == 0) {
				log.debug("> No matching result");
				return null;
			}
			
			for (SubSearchResult scoredSub : scoredSubs)
			{
				// ********************************************
				// 3 - Download Page
				
				// Connection to download page
				String downloadUrl = scoredSub.getUrl();
				log.debug(String.format("Try to download subtitle at URL '%s' ...", downloadUrl));
				
				byte[] bytes = null;
				String content = "";				
				try {
					bytes = Jsoup.connect(downloadUrl)
							.timeout(QUERY_TIME_OUT)
							.header("Referer", episodeUrl)
							.ignoreContentType(true)
							.execute()
							.bodyAsBytes();			
					content = new String(bytes);
				}
				catch (IllegalCharsetNameException ex) {
					// Charset not detect : try to force download with charset UTF-8
					log.debug(String.format("> Charset not detect : try to force download with charset '%s' ...", ALT_DOWNLOAD_CHARSET));
					URL url = new URL(downloadUrl);
					URLConnection connection = url.openConnection();
					connection.setRequestProperty("Referer", episodeUrl);				
					InputStream stream = connection.getInputStream();
					bytes = IOUtils.toByteArray(stream);
					content = new String(bytes, ALT_DOWNLOAD_CHARSET);
				}

				if (content.toLowerCase().contains("<html xmlns=\"http://www.w3.org/1999/xhtml\">".toLowerCase())
						|| content.toLowerCase().contains("Addic7ed.com - Sorry, download limit exceeded".toLowerCase()))
				{
					// Download page is HTML or daily download limit exceeded => next line
					log.debug("> Download not available : daily download limit exceeded... try tomorrow");
					continue;
				}

				// Save subtitle file to working folder
				String subFileName = TvShowInfoHelper.prepareSubtitleFileName(this.tvShowInfo, this.subLanguage, "srt");			
				String subPath = PropertiesHelper.getWorkingFolderPath() + "/" + subFileName;
				FileOutputStream fos = new FileOutputStream(subPath);
				fos.write(bytes);
				fos.close();

				log.info(String.format("> SubLeecher Addicted - Subtitle found : Video File='%s' ; Language='%s' ; Subtitle File='%s'", 
						this.tvShowInfo.getInputVideoFileName(), 
						this.subLanguage, 
						subFileName));
				return new SubTitleInfo(subFileName, this.subLanguage);
			}

			// No subtitle found => end
			log.debug("No subtitle downloaded");
			return null;
		}
		catch (Exception e)
		{
			log.error("Error while trying to sub-leech files with Addicted", e);
            return null;
		}
		finally
		{
			log.debug(String.format("SubLeecher Addicted - End - File='%s' ; Language='%s'", this.tvShowInfo.getInputVideoFileName(), this.subLanguage));
		}
	}
}
