package org.subzero.core.plugin;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.subzero.core.bean.SubSearchResult;
import org.subzero.core.bean.SubTitleInfo;
import org.subzero.core.bean.TvShowInfo;
import org.subzero.core.helper.FileHelper;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.helper.TvShowInfoHelper;
import org.subzero.core.subleecher.SubLeecherBase;
import org.subzero.core.subleecher.SubLeecherHelper;

/**
 * SubLeecher plugin for web site http://www.tvsubtitles.net
 * @author Julien
 *
 */
public class SubLeecherTVSubtitles extends SubLeecherBase  {

	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubLeecherTVSubtitles.class);
	
	// Constants
	private static String TVSUBTITLES_URL = "http://www.tvsubtitles.net";
	private static int QUERY_TIME_OUT = 30000;
	
	/**
	 * Remove years part from the serie name
	 * @param serieName Example : House M.D. (2004-2013)
	 * @return Example : House M.D.
	 */
	private String removeYearsFromSerieName(String serieName) {
		if (serieName == null || serieName.equals("")) {
			return null;
		}
		int pos = serieName.lastIndexOf("(");
		if (pos == -1) {
			return serieName.trim();
		}
		else {
			return serieName.substring(0, pos).trim();
		}
	}
	
	/**
	 * Get the serie page URL with season
	 * @param seriePageUrl Example : http://www.tvsubtitles.net/tvshow-9.html
	 * @param season Example : 8
	 * @return Example : http://www.tvsubtitles.net/tvshow-9-8.html
	 */
	private String getSeriePageUrlWithSeason(String seriePageUrl, int season) {
		if (seriePageUrl == null || seriePageUrl.equals("")) {
			return null;
		}
		int pos = seriePageUrl.lastIndexOf(".");
		if (pos == -1) {
			return seriePageUrl;
		}
		else {			
			return seriePageUrl.substring(0, pos) + "-" + season + seriePageUrl.substring(pos);
		}
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
			log.debug(String.format("SubLeecher TVSubtitles - Start - File='%s' ; Language='%s'", this.tvShowInfo.getInputVideoFileName(), this.subLanguage));
			
			String episode = TvShowInfoHelper.getShortNameTypeX(this.tvShowInfo);
			String serie = this.tvShowInfo.getSerie();

			// ********************************************
			// 1 - Search Page
			
			// Connect to search page & search the episode
			log.debug(String.format("Search for serie '%s' ...", serie));
			String searchUrl = TVSUBTITLES_URL + "/search.php?q=" + serie;
			Document docSearch = Jsoup.connect(searchUrl)
					.timeout(QUERY_TIME_OUT)
					.get();
			
			// Iterative through search results
			Element aSerieMatch = null;
			for (Element aSerie : docSearch.select("div[class=left_articles] > ul > li a"))
			{
				String aText = aSerie.text(); 
				String aSerieCleaned = removeYearsFromSerieName(aText);			
				
				// Check if the result text : 
				// - starts with the desired serie name
				// => select the first one matching only
				if (SubLeecherHelper.looseMatchStartsWith(aSerieCleaned, this.tvShowInfo.getSerie()))
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
			String serieUrl = TVSUBTITLES_URL + aSerieMatch.attr("href");
			String seasonsUrl = getSeriePageUrlWithSeason(serieUrl, tvShowInfo.getSeason());
						
			// ********************************************
			// 2 - Season Page
						
			// Connect to season page
			log.debug(String.format("Search for episode '%s' ...", episode));
			Document docSeason = Jsoup.connect(seasonsUrl)
					.timeout(QUERY_TIME_OUT)
					.header("Referer", serieUrl)
					.get();	

			// Browse lines in episodes table
			Elements trEpisodeList = docSeason.select("table[id=table5] tr");
			String episodeListUrl = null;
			boolean isHeader = true;
			for (Element trEpisode : trEpisodeList)
			{
				if (isHeader) {
					// Skip header row
					isHeader = false;
					continue;
				}
				
				Element tdEpisode = trEpisode.select("td").first();
				if (tdEpisode == null) {
					// No TD in row => next line
					continue;
				}
				
				Element aEpisode = trEpisode.select("a").first();
				if (aEpisode == null) {
					// No link in row => next line
					continue;
				}
				
				// Try to analyse the episode title to extract info. Ex : 8x03
				String tdEpisodeText = tdEpisode.text();
				TvShowInfo tdEpisodeInfo = TvShowInfoHelper.populateTvShowInfoFromFreeText(this.tvShowInfo.getSerie() + " " + tdEpisodeText);
				
				// Check if the result text : 
				// - has the season search 
				// - has at least one episode search 
				// => select the first one matching only
				if (tdEpisodeInfo != null
						&& tdEpisodeInfo.getSeason() == this.tvShowInfo.getSeason()
						&& TvShowInfoHelper.testIfOneEpisodeMatches(this.tvShowInfo.getEpisodes(), tdEpisodeInfo.getEpisodes()))
				{					
					log.debug(String.format("> Matching result found : '%s'", tdEpisodeText));
					episodeListUrl = TVSUBTITLES_URL + "/" + aEpisode.attr("href");
					break;
				}
				else {
					log.debug(String.format("> Non matching result : '%s'", tdEpisodeText));
				}
			}
			
			if (episodeListUrl == null) {
				log.debug("> No matching result");
				return null;
			}
			
			// ********************************************
			// 3 - Episode List Page
			
			// Connect to episode List page
			log.debug(String.format("Search for subtitles for episode '%s' ...", episode));
			Document docEpisode = Jsoup.connect(episodeListUrl)
					.timeout(QUERY_TIME_OUT)
					.header("Referer", seasonsUrl)
					.get();	
	
			// Browse subtitle links in subtitles table
			Element divSubtitleList =  docEpisode.select("div[class=left_articles]").first();
			Elements aSubtitleList = divSubtitleList.select("a[href^=/subtitle-]");
			List<SubSearchResult> subSearchResults = new ArrayList<SubSearchResult>();			
			for (Element aSubtitle : aSubtitleList)
			{
				// Get the last div before this link (contains language description)
				Element divLanguage = aSubtitle.previousElementSibling();
				while (divLanguage != null) {
					if (divLanguage.tag().getName().toLowerCase().equals("div")) {
						break;
					}
					divLanguage = divLanguage.previousElementSibling();
				}
				
				String divLanguageText = divLanguage.text();
				if (!SubLeecherHelper.looseMatchContains(divLanguageText, this.subLanguage)) {
					// Language mismatch => next line
					continue;
				}
				
				// Get the subtitle URL
				String subtitleUrl = TVSUBTITLES_URL + aSubtitle.attr("href");
				
				// Get the episode Release (OPTIONAL)
				Element pRelease = aSubtitle.select("p[title=release]").first();
				String episodeRelease = "";
				if (pRelease != null) {
					episodeRelease = pRelease.text();
				}
				
				// Get the number of downloads (OPTIONAL)
				Element pDownloads = aSubtitle.select("p[title=downloaded]").first();
				int episodeNbDownload = 0;
				if (pDownloads != null) {
					episodeNbDownload = Integer.parseInt(pDownloads.text());
				}
				
				subSearchResults.add(new SubSearchResult(subtitleUrl, divLanguageText, episodeNbDownload, episodeRelease));
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
				// 3 - Subtitle Page
				
				// Connect to subtitle page
				String subtitleUrl = scoredSub.getUrl();				
				log.debug(String.format("Go to subtitle page at URL '%s' ...", subtitleUrl));
				Document docSubtitle = Jsoup.connect(subtitleUrl)
						.timeout(QUERY_TIME_OUT)
						.header("Referer", episodeListUrl)
						.get();
				
				Element aDownload = docSubtitle.select("a[href^=download-").first();
				if (aDownload == null) {
					// No download link
					log.debug("> Download not available : no download link found in page");
					continue;
				}
				
				// Get the download redirect URL
				String downloadRedirectUrl = TVSUBTITLES_URL + "/" + aDownload.attr("href");
				
				// ********************************************
				// 4 - Download Redirect Page
				
				log.debug(String.format("Go to download redirect page at URL '%s' ...", downloadRedirectUrl));
				Response response = Jsoup.connect(downloadRedirectUrl)
						.timeout(QUERY_TIME_OUT)
						.followRedirects(false)
						.execute();

				String redirectLocation = response.header("Location");
				if (redirectLocation == null) {
					// No download link
					log.debug("> Download not available : no download redirect location");
					continue;
				}
				
				// Convert redirection location to URL (escape space char to %20)
				String downloadUrl = FileHelper.encodeUnsafeUrl(TVSUBTITLES_URL + "/" + redirectLocation);	
				
				// ********************************************
				// 5 - Download Page
				
				// Connection to download page
				log.debug(String.format("Try to download subtitle at URL '%s' ...", downloadUrl));

				byte[] bytes = Jsoup.connect(downloadUrl)
						.timeout(QUERY_TIME_OUT)
						.header("Referer", subtitleUrl)
						.ignoreContentType(true)
						.execute()
						.bodyAsBytes();
						
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
				
				log.info(String.format("> SubLeecher Addicted - Subtitle found : Video File='%s' ; Language='%s' ; Subtitle File='%s'", 
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
			log.error("Error while trying to sub-leech files with TVSubtitles", e);
            return null;
		}
		finally
		{
			log.debug(String.format("SubLeecher TVSubtitles - End - File='%s' ; Language='%s'", this.tvShowInfo.getInputVideoFileName(), this.subLanguage));
		}
	}
}
