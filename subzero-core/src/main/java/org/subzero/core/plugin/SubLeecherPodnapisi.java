package org.subzero.core.plugin;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.subzero.core.bean.SubSearchResult;
import org.subzero.core.bean.SubTitleInfo;
import org.subzero.core.bean.TvShowInfo;
import org.subzero.core.helper.FileHelper;
import org.subzero.core.helper.TvShowInfoHelper;
import org.subzero.core.subleecher.SubLeecherBase;
import org.subzero.core.subleecher.SubLeecherHelper;

/**
 * SubLeecher plugin for web site http://www.podnapisi.net
 * @author Julien
 *
 */
public class SubLeecherPodnapisi extends SubLeecherBase  {

	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubLeecherPodnapisi.class);
	
	// Constants
	private static final String SITE_NAME = "Podnapisi";
	private static final String PODNAPISI_URL = "https://www.podnapisi.net";
	private static final int QUERY_TIME_OUT = 30000;
	private static final String ALT_DOWNLOAD_CHARSET = "UTF-8";
	
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
			log.debug(String.format("SubLeecher %s - Start - File='%s' ; Language='%s'", SITE_NAME, this.tvShowInfo.getInputVideoFileName(), this.subLanguage));
			
			String episode = TvShowInfoHelper.getShortName(this.tvShowInfo);			
			String languageCode = subLanguage.toLowerCase().substring(0, 2);
			
			// ********************************************
			// 1 - Serie Search Endpoint (JSON)
			
			// Connect to search endpoint & search the serie
			// https://www.podnapisi.net/subtitles/search/?keywords=the%20walking%20dead&movie_type=tv-series&output_format=json
			log.debug(String.format("Search for serie '%s' ...", this.tvShowInfo.getSerie()));
			String serieSearchUrl = String.format(PODNAPISI_URL + "/subtitles/search/?keywords=%s&movie_type=tv-series&output_format=json",
					URLEncoder.encode(this.tvShowInfo.getSerie(), ALT_DOWNLOAD_CHARSET));
			
			log.debug(String.format("> Search at URL '%s' ...", serieSearchUrl));			
			String jsonString = Jsoup.connect(serieSearchUrl)
					.timeout(QUERY_TIME_OUT)
					.ignoreContentType(true)
					.execute()
					.body();
			
			if (jsonString == null || jsonString.isEmpty()) {
				log.warn("> No JSON response to search query");
				return null;
			}
			
			JSONObject jsonDoc = new JSONObject(jsonString);
            JSONArray jsonDataList = jsonDoc.getJSONArray("data");
            if (jsonDataList == null || jsonDataList.length() == 0) {
				log.debug("> No serie result");
				return null;
			}
            
            // Iterate through serie results in JSON response
            String foundSerieId = "";
            String foundSerieSlug = "";
            for (int i = 0; i < jsonDataList.length(); i++) {
            	JSONObject jsonData = jsonDataList.getJSONObject(i);
            	String jsonDataId = jsonData.getString("id");
            	String jsonDataSlug = jsonData.getString("slug");
            	String jsonDataTitle = jsonData.getString("title");
            	
            	// Check if the serie title : 
				// - starts with the desired serie name
				// => select the first one matching only
				if (!jsonDataId.isEmpty() && !jsonDataSlug.isEmpty() && !jsonDataTitle.isEmpty()
						&& SubLeecherHelper.looseMatchStartsWith(jsonDataTitle, this.tvShowInfo.getSerie(), true)) {
					
					log.debug(String.format("> Matching result found : '%s'", jsonDataTitle));
					foundSerieId = jsonDataId;
					foundSerieSlug = jsonDataSlug;
					break;
				}
				else {
					log.debug(String.format("> Non matching result : '%s'", jsonDataTitle));
				}
            }
            
            if (foundSerieId.isEmpty()) {
            	log.debug("> Serie not found in results");
				return null;
            }
            
            // ********************************************
            // 2 - Episode search page
            
            // Connect to search page & search the episode
 			log.debug(String.format("Search for episode '%s' (page 1) ...", episode));
 			String episodeSearchUrl = String.format(PODNAPISI_URL + "/subtitles/search/%s/%s?seasons=!%s&episodes=!%s&language=%s",
 					foundSerieSlug,
 					foundSerieId,
 					this.tvShowInfo.getSeason(),
 					this.tvShowInfo.getEpisodes().get(0), // Search only first episode if multiple episodes
 					languageCode // Search with parameter language code (2 first letters in lower case)
 					);
 			log.debug(String.format("> Search at URL '%s' ...", episodeSearchUrl));
 			Document docSearch = Jsoup.connect(episodeSearchUrl)
 					.timeout(QUERY_TIME_OUT)
 					.header("Accept-Language", languageCode)
 					.get();
 			
 			/*String html = docSearch.outerHtml();
 			FileUtils.writeStringToFile(new File("c:/temp/test1.html"), html, ALT_DOWNLOAD_CHARSET);
 			html = html.replaceAll("=([^\"^'^> ]+)", "=\"$1\"");
 			docSearch = Jsoup.parse(html);
 			FileUtils.writeStringToFile(new File("c:/temp/test2.html"), html, ALT_DOWNLOAD_CHARSET);*/ 			
 			
 			List<SubSearchResult> subSearchResults = new ArrayList<SubSearchResult>();
 			
 			// Iterate through lines in results table
			Elements trResults = docSearch.select("tr[class=subtitle-entry]");
			for (Element trResult : trResults) {
				
				String downloadUrl = "";
				String attSubtitlePage = trResult.attr("data-href");
				if (attSubtitlePage.isEmpty()) {
					// No link present => next line
					continue;
				}
				else {
					// data-href="/subtitles/fr-the-walking-dead-2010-S06E15/Xew_"
					downloadUrl = PODNAPISI_URL + attSubtitlePage;
				}
								
				// Get the number of downloads (OPTIONAL)												
				Element tdDownloads = trResult.select("td:eq(7)").first();
				int episodeNbDownload = 0;
				if (tdDownloads != null) {
					episodeNbDownload = Integer.parseInt(tdDownloads.text());
				}				
				
				// Get the episode Release (OPTIONAL)
				Set<String> cleanedEpisodeReleases = new HashSet<String>();
				Elements elReleases = trResult.select("span[class=release],div[class=release]");
				for (Element elRelease : elReleases) {
					String detectedEpisodeTitle = elRelease.text();
					TvShowInfo releaseEpisodeInfo = TvShowInfoHelper.populateTvShowInfoFromFreeText(detectedEpisodeTitle, true);
					if (releaseEpisodeInfo != null) {
						String cleanedReleaseGroup = releaseEpisodeInfo.getCleanedReleaseGroup();
						if (cleanedReleaseGroup != null && !cleanedReleaseGroup.isEmpty()) {
							cleanedEpisodeReleases.add(releaseEpisodeInfo.getCleanedReleaseGroup());
						}
					}
				}
				if (elReleases.isEmpty()) {
					// No release group found : add an empty one
					cleanedEpisodeReleases.add("");
				}
				
				for (String cleanedEpisodeRelease : cleanedEpisodeReleases) {				
					subSearchResults.add(new SubSearchResult(downloadUrl, this.subLanguage, episodeNbDownload, cleanedEpisodeRelease));
				}
			}
			
			// Evaluate the matching score and sort the subtitle results !
			List<SubSearchResult> scoredSubs = SubLeecherHelper.evaluateScoreAndSort(
					subSearchResults, 
					this.tvShowInfo.getCleanedReleaseGroup(), 
					this.releaseGroupMatchRequired);
			
			if (scoredSubs == null || scoredSubs.size() == 0) {
				log.debug("> No matching result");
				return null;
			}
			
			// Iterate through sorted results
			for (SubSearchResult scoredSub : scoredSubs) {
				// ********************************************
				// 3 - Download Page
				
				String preDownloadUrl = scoredSub.getUrl();
				String downloadUrl = preDownloadUrl + "/download";
				
				// Connection to download page
				log.debug(String.format("Try to download subtitle at URL '%s' ...", downloadUrl));
				
				byte[] bytes = null;
				try {
					bytes = Jsoup.connect(downloadUrl)
							.timeout(QUERY_TIME_OUT)
							.header("Referer", preDownloadUrl)
							.ignoreContentType(true)
							.execute()
							.bodyAsBytes();	
				}
				catch (IllegalCharsetNameException ex) {
					// Charset not detect : try to force download with charset UTF-8
					log.debug(String.format("> Charset not detect : try to force download with charset '%s' ...", ALT_DOWNLOAD_CHARSET));
					URL url = new URL(downloadUrl);
					URLConnection connection = url.openConnection();
					connection.setRequestProperty("Referer", preDownloadUrl);				
					InputStream stream = connection.getInputStream();
					bytes = IOUtils.toByteArray(stream);
				}
				
				// Save zipped subtitle file to working folder
				String zippedSubFileName = TvShowInfoHelper.prepareZippedSubtitleFileName(this.tvShowInfo, this.subLanguage);				
				String zippedSubPath = this.workingFolderPath + "/" + zippedSubFileName;
				FileOutputStream fos = new FileOutputStream(zippedSubPath);
				fos.write(bytes);
				fos.close();
				log.debug(String.format("> Zipped subtitle downloaded to path '%s'", zippedSubPath));
				
				// Unzip the first subtitle file in ZIP 
				String subFileName = FileHelper.unZipWorkingFirstSubFile(
						this.workingFolderPath, 
						zippedSubFileName, 
						TvShowInfoHelper.prepareBaseOutputFileName(this.tvShowInfo, this.subLanguage));
				
				if (subFileName == null) {
					// No download link
					log.debug("> No subtitle found in ZIP file");
					continue;
				}				
				
				// Add ZIP file name to return value for post-processor
				List<String> extraFileNames = new ArrayList<String>();
				extraFileNames.add(zippedSubFileName);
				
				log.info(String.format("> SubLeecher %s - Subtitle found : Video File='%s' ; Language='%s' ; Subtitle File='%s'", 
						SITE_NAME,
						this.tvShowInfo.getInputVideoFileName(), 
						this.subLanguage, 
						subFileName));
				return new SubTitleInfo(subFileName, this.subLanguage, extraFileNames);
			}

            			
			// No subtitle found => end
			log.debug("No subtitle downloaded");
			return null;
		}
		catch (Exception e)
		{
			log.error("Error while trying to sub-leech files with " + SITE_NAME, e);
            return null;
		}
		finally
		{
			log.debug(String.format("SubLeecher %s - End - File='%s' ; Language='%s'", SITE_NAME, this.tvShowInfo.getInputVideoFileName(), this.subLanguage));
		}
	}
}
