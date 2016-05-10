package org.subzero.core.plugin;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
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
	private static final String SITE_NAME = "TVSubtitles";
	private static final String TVSUBTITLES_URL = "http://www.tvsubtitles.net";
	private static final int QUERY_TIME_OUT = 30000;
	private static final String ALT_DOWNLOAD_CHARSET = "UTF-8";
	
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
			log.debug(String.format("SubLeecher %s - Start - File='%s' ; Language='%s'", SITE_NAME, this.tvShowInfo.getInputVideoFileName(), this.subLanguage));
			
			String episode = TvShowInfoHelper.getShortNameTypeX(this.tvShowInfo);
			String serie = this.tvShowInfo.getSerie();

			// ********************************************
			// 1 - Search Page
			
			// Connect to search page & search the episode
			String searchUrl = TVSUBTITLES_URL + "/search.php?q=" + URLEncoder.encode(serie, "UTF-8");
			log.debug(String.format("Search for serie '%s' at URL '%s' ...", serie, searchUrl));
			
			Document docSearch = Jsoup.connect(searchUrl)
					.timeout(QUERY_TIME_OUT)
					.get();
			
			// Iterative through search results
			Element aSerieMatch = null;
			for (Element aSerie : docSearch.select("div[class=left_articles] > ul > li a"))
			{
				String aText = aSerie.text(); 
				String aSerieCleaned = TvShowInfoHelper.removeYearsFromSerieName(aText);
				
				// Check if the result text : 
				// - starts with the desired serie name
				// OR
				// - ends with the desired serie name
				// => select the first one matching only
				if (SubLeecherHelper.looseMatchStartsWith(aSerieCleaned, this.tvShowInfo.getSerie(), true)
						|| SubLeecherHelper.looseMatchEndsWith(aSerieCleaned, this.tvShowInfo.getSerie(), true))
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
			log.debug(String.format("Search for episode '%s' at URL '%s' ...", episode, seasonsUrl));
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
				TvShowInfo tdEpisodeInfo = TvShowInfoHelper.populateTvShowInfoFromFreeText(this.tvShowInfo.getSerie() + " " + tdEpisodeText, true);
				
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
			log.debug(String.format("Search for subtitles for episode '%s' at URL '%s' ...", episode, episodeListUrl));
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
				// Get the flag image (corresponding to the language) 
				Element imgFlag = aSubtitle.select("img[src^=images/flags/]").first();
				
				// Check if the image has the right language image
				// example : src=images/flags/fr.gif
				if (imgFlag == null ||
						!imgFlag.attr("src").toLowerCase().startsWith("images/flags/" + this.subLanguage.toLowerCase().substring(0,2))) {
					// Language mismatch => next line
					continue;
				}		
				
				// Get the subtitle URL
				String subtitleUrl = TVSUBTITLES_URL + aSubtitle.attr("href");
				
				// Get the episode Release (OPTIONAL)
				Element pRelease = aSubtitle.select("p[title=release]").first();
				String episodeRelease = "";
				if (pRelease != null) {
					episodeRelease = TvShowInfoHelper.cleanReleaseGroupNamingPart(pRelease.text());
				}
				
				// Get the number of downloads (OPTIONAL)
				Element pDownloads = aSubtitle.select("p[title=downloaded]").first();
				int episodeNbDownload = 0;
				if (pDownloads != null) {
					episodeNbDownload = Integer.parseInt(pDownloads.text());
				}
				
				subSearchResults.add(new SubSearchResult(subtitleUrl, this.subLanguage, episodeNbDownload, episodeRelease));
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
			
			for (SubSearchResult scoredSub : scoredSubs)
			{
				// ********************************************
				// 3 - Subtitle Page
				
				// Connect to subtitle page
				String subtitleUrl = scoredSub.getUrl();				
				log.debug(String.format("Go to subtitle page at URL '%s'", subtitleUrl));
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

				byte[] bytes = null;				
				try {
					bytes = Jsoup.connect(downloadUrl)
							.timeout(QUERY_TIME_OUT)
							.header("Referer", subtitleUrl)
							.ignoreContentType(true)
							.execute()
							.bodyAsBytes();			
				}
				catch (IllegalCharsetNameException ex) {
					// Charset not detect : try to force download with charset UTF-8
					log.debug(String.format("> Charset not detect : try to force download with charset '%s' ...", ALT_DOWNLOAD_CHARSET));
					URL url = new URL(downloadUrl);
					URLConnection connection = url.openConnection();
					connection.setRequestProperty("Referer", subtitleUrl);				
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
