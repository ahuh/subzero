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
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.subzero.core.bean.SubSearchResult;
import org.subzero.core.bean.SubTitleInfo;
import org.subzero.core.bean.TvShowInfo;
import org.subzero.core.helper.FileHelper;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.helper.TvShowInfoHelper;
import org.subzero.core.subleecher.SubLeecherBase;
import org.subzero.core.subleecher.SubLeecherHelper;

/**
 * SubLeecher plugin for web site http://www.opensubtitles.org
 * @author Julien
 *
 */
public class SubLeecherOpenSubtitles extends SubLeecherBase  {

	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubLeecherOpenSubtitles.class);
	
	// Constants
	private static final String SITE_NAME = "OpenSubtitles";
	private static final String OPENSUBTITLES_URL = "http://www.opensubtitles.org";
	private static final String OPENSUBTITLES_DOWNLOAD_URL = "http://dl.opensubtitles.org/en/download/sub/";
	
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
			
			String episode = TvShowInfoHelper.getShortNameTypeX(this.tvShowInfo);
			String serie = this.tvShowInfo.getSerie();

			// ********************************************
			// 1 - Search Page
			
			// Connect to search page & search the serie
			log.debug(String.format("Search for serie '%s' ...", serie));
			String searchUrl = OPENSUBTITLES_URL + "/en/search2/sublanguageid-all/moviename-" + serie.replace(' ', '+');
			Document docSearch = Jsoup.connect(searchUrl)
					.timeout(QUERY_TIME_OUT)
					.get();
			
			// Iterative through search results
			Element aSerieMatch = null;
			for (Element trResult : docSearch.select("table#search_results tr"))
			{
				// Get the 2nd column in result table
				Element tdSerie = trResult.select("td:eq(1)").first();				
				if (tdSerie == null) {
					// No columns (header) => next line
					continue;
				}
				
				// Check if the result is a TV serie
				Element imgTvSerie = tdSerie.select("img[src$=tv-series.gif]").first();
				if (imgTvSerie == null) {
					// No TV serie image => next line
					continue;
				}
				
				// Get the TV serie page link 
				Element aSerie = tdSerie.select("a[href^=/en/search/sublanguageid-all]").first();
				if (aSerie == null) {
					// No serie page link => next line
					continue;
				}
				
				String aText = aSerie.text(); 
				String aSerieCleaned = TvShowInfoHelper.removeYearsFromSerieName(aText);
				
				// Check if the result text : 
				// - starts with the desired serie name
				// => select the first one matching only
				if (SubLeecherHelper.looseMatchStartsWith(aSerieCleaned, this.tvShowInfo.getSerie(), true))
				{					
					log.debug(String.format("> Matching result found : '%s'", aText));
					aSerieMatch = aSerie;
					break;
				}
				else {
					log.debug(String.format("> Non matching result : '%s'", aText));
				}
			}
			
			if (aSerieMatch == null) {
				// No episode found => end
				log.debug("> No match in result !");
				return null;
			}

			// Get the season URL from serie link (add season number at the end of URL)
			String serieUrl = OPENSUBTITLES_URL + aSerieMatch.attr("href");
			
			// ********************************************
			// 2 - Season Page
						
			// Connect to season page
			log.debug(String.format("Search for episode '%s' ...", episode));
			Document docSeason = Jsoup.connect(serieUrl)
					.timeout(QUERY_TIME_OUT)
					.header("Referer", searchUrl)
					.get();	

			// Iterative through search results
			Integer currentSeason = null;
			Integer currentEpisode = null;
			String episodeListUrl = null;
			for (Element trEpisodeList : docSeason.select("table#search_results tr"))
			{
				// Get the 1st column in result table
				Element tdFirstCol = trEpisodeList.select("td:eq(0)").first();				
				if (tdFirstCol == null) {
					// No columns (header) => next line
					continue;
				}
				
				// Update the season if this result line is a season header 
				// example : <span id="season-7">...</span>
				Element spanSeason = tdFirstCol.select("span[id^=season-]").first();
				if (spanSeason != null) {
					String spanSeasonId = spanSeason.attr("id");
					currentSeason = Integer.parseInt(spanSeasonId.substring(7));
					// => next line
					continue;
				}
				
				// Get the episode number from span 
				// example : <span itemprop="episodeNumber">2</span>
				Element spanEpisode = tdFirstCol.select("span[itemprop=episodeNumber]").first();
				if (spanEpisode == null) {
					// No episode => next line
					continue;
				}
				currentEpisode = Integer.parseInt(spanEpisode.text());
				
				// Get the episode page link 
				Element aEpisode = tdFirstCol.select("a[href^=/en/search/sublanguageid-all]").first();
				if (aEpisode == null) {
					// No episode page link => next line
					continue;
				}
				
				// Check if the result :
				// - has the season search 
				// - has at least one episode search 
				// => select the first one matching only
				String episodeDescription = String.format("%sx%s", currentSeason, currentEpisode);
				if (currentSeason != null
						&& currentEpisode != null
						&& currentSeason.equals(this.tvShowInfo.getSeason())
						&& TvShowInfoHelper.testIfOneEpisodeMatches(this.tvShowInfo.getEpisodes(), currentEpisode))
				{					
					log.debug(String.format("> Matching result found : '%s'", episodeDescription));
					episodeListUrl = OPENSUBTITLES_URL + aEpisode.attr("href");
					break;
				}
				else {
					log.debug(String.format("> Non matching result : '%s'", episodeDescription));
				}
			}
			
			if (episodeListUrl == null) {
				log.debug("> No matching result");
				return null;
			}
			
			// ********************************************
			// 3 - Episode List Pages
			
			log.debug(String.format("Search for episode '%s' ...", episode));
			
			List<SubSearchResult> subSearchResults = new ArrayList<SubSearchResult>();
			
			// The episode list pages are paginated : iterate through all pages to retrieve all episodes
			String nextPageUrl = episodeListUrl;
			
			while (nextPageUrl != "")
			{				
				// Connect to episode List page
				log.debug(String.format("> Search URL : %s", nextPageUrl));
				Document docEpisode = Jsoup.connect(nextPageUrl)
						.timeout(QUERY_TIME_OUT)
						.header("Referer", serieUrl)
						.get();	
				
				for (Element trEpisodeList : docEpisode.select("table#search_results tr"))
				{
					// Get the flag column in result table (2nd)
					Element tdFlagCol = trEpisodeList.select("td:eq(1)").first();				
					if (tdFlagCol == null) {
						// No flag columns => next line
						continue;
					}
					
					// Get the flag div (corresponding to the language) 
					Element divFlag = tdFlagCol.select("div[class^=flag]").first();
					
					// Check if the div has the right language class
					// example : <div class="flag fr">...</div>
					if (divFlag == null ||
							!divFlag.attr("class").toLowerCase().endsWith(this.subLanguage.toLowerCase().substring(0,2))) {
						// Language mismatch => next line
						continue;
					}
					
					// Get the TD containing ID of the subtitle
					// example : <td id="main4658701" ...>
					Element tdId = trEpisodeList.select("td[id^=main]").first();
					if (tdId == null) {
						// No TD with ID => next line
						continue;
					}
					
					// Build subtitle URL from the ID
					// example : http://dl.opensubtitles.org/en/download/sub/4658701
					String subtitleId = tdId.attr("id").replaceAll("main", "");
					String subtitleUrl = OPENSUBTITLES_DOWNLOAD_URL + subtitleId;
										
					// Get the label of the episode	
					String episodeTitle = "";
					Element tdIdFirstSpan = tdId.select("span").first();
					if (tdIdFirstSpan != null) {
						// - Case 1 : text too long => located in the first span's title
						//   Example : <br />[S07E02]<span title="Doctor.Who.s07e04.The.Power.of.Three.HD1080p.WEB-DL">Doctor.Who.s07e04.The.Power.of.Three.HD1080p.WE... </span><br />
						episodeTitle = tdIdFirstSpan.attr("title");
					}
					else {
						// - Case 2 : text not too long => first text node, not in span				
						//   Example : <br />[S07E02] Doctor.Who.2005.S07E02.HDTV.x264-FoV<br />
						for (Node child : tdId.childNodes()) {
						    if (child instanceof TextNode) {
						    	episodeTitle = ((TextNode)child).text();
						    	break;
						    }
						}
						// Remove the prefix : [S07E02]
						// => only keep title
						int prefixTitleEnd = episodeTitle.indexOf("]");
						if (prefixTitleEnd > -1) {
							episodeTitle = episodeTitle.substring(prefixTitleEnd+1).trim();
						}
					}
					
					// Get the episode Release (OPTIONAL)
					String episodeRelease = "";
					if (episodeTitle != "") {
						TvShowInfo episodeResult = TvShowInfoHelper.populateTvShowInfoFromFreeText(episodeTitle, true);
						episodeRelease = episodeResult.getReleaseGroup();
					}
										
					// Get the nb of downloads column in result table (3rd)
					int episodeNbDownload = 0;
					Element tdNbDownloadsCol = trEpisodeList.select("td:eq(4)").first();				
					if (tdNbDownloadsCol != null)
					{
						// Check the "srt" subtitle format ("sub" not supported)
						Element spanFormat = tdNbDownloadsCol.select("span[class=p]").first();	
						if (spanFormat != null) {
							if (!spanFormat.text().toLowerCase().equals("srt")) {
								// No "srt" format => next line
								continue;
							}
						}
						
						// Get the number of downloads (OPTIONAL)
						// example : <a ...>46x </a>
						Element aNbDownloads = tdNbDownloadsCol.select("a").first();
						if (aNbDownloads != null) {
							String episodeNbDownloadRaw = aNbDownloads.text();
							int xPos = episodeNbDownloadRaw.indexOf("x");
							if (xPos > -1) {
								episodeNbDownload = Integer.parseInt(episodeNbDownloadRaw.substring(0, xPos).trim());
							}
						}
					}
					
					subSearchResults.add(new SubSearchResult(subtitleUrl, this.subLanguage, episodeNbDownload, episodeRelease));
				}
				
				// Look for the pager div, and get the last link ">>" URL to go to next page
				nextPageUrl = "";
				Element divPager = docEpisode.select("div#pager").first();
				if (divPager != null) {
					Element lastPage = divPager.select("a").last();
					if (lastPage.text().equals(">>")) {
						nextPageUrl = OPENSUBTITLES_URL + lastPage.attr("href");
					}
				}
			}
			
			// Evaluate the matching score and sort the subtitle results !
			List<SubSearchResult> scoredSubs = SubLeecherHelper.evaluateScoreAndSort(subSearchResults, this.tvShowInfo.getReleaseGroup(), this.releaseGroupMatchRequired);
			if (scoredSubs == null || scoredSubs.size() == 0) {
				log.debug("> No matching result");
				return null;
			}
			
			for (SubSearchResult scoredSub : scoredSubs)
			{	
				String downloadUrl = scoredSub.getUrl();
				
				// ********************************************
				// 4 - Download Page
				
				// Connection to download page
				log.debug(String.format("Try to download subtitle at URL '%s' ...", downloadUrl));
				
				byte[] bytes = null;				
				try {
					bytes = Jsoup.connect(downloadUrl)
							.timeout(QUERY_TIME_OUT)
							.header("Referer", episodeListUrl)
							.ignoreContentType(true)
							.execute()
							.bodyAsBytes();
				}
				catch (IllegalCharsetNameException ex) {
					// Charset not detect : try to force download with charset UTF-8
					log.debug(String.format("> Charset not detect : try to force download with charset '%s' ...", ALT_DOWNLOAD_CHARSET));
					URL url = new URL(downloadUrl);
					URLConnection connection = url.openConnection();
					connection.setRequestProperty("Referer", episodeListUrl);			
					InputStream stream = connection.getInputStream();
					bytes = IOUtils.toByteArray(stream);
				}
				
				// Save zipped subtitle file to working folder
				String zippedSubFileName = TvShowInfoHelper.prepareZippedSubtitleFileName(this.tvShowInfo, this.subLanguage);				
				String zippedSubPath = PropertiesHelper.getWorkingFolderPath() + "/" + zippedSubFileName;
				FileOutputStream fos = new FileOutputStream(zippedSubPath);
				fos.write(bytes);
				fos.close();
				log.debug(String.format("> Zipped subtitle downloaded to path '%s'", zippedSubPath));
				
				// Unzip the first subtitle file in ZIP 
				String subFileName = FileHelper.unZipWorkingFirstSubFile(zippedSubFileName, TvShowInfoHelper.prepareBaseOutputFileName(this.tvShowInfo, this.subLanguage));
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
