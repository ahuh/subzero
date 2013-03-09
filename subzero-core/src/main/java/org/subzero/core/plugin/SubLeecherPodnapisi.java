package org.subzero.core.plugin;

import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
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
	private static String PODNAPISI_URL = "http://www.podnapisi.net";
	private static int QUERY_TIME_OUT = 30000;	
	
	/**
	 * Get Search Page URL
	 * @param page Number of result page
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String getSearchUrl(int page) throws UnsupportedEncodingException {
		return String.format(PODNAPISI_URL + "/ppodnapisi/search?sT=1&sK=%s&sTS=%s&sTE=%s&page=%s",
				URLEncoder.encode(this.tvShowInfo.getSerie(), "UTF-8"),
				this.tvShowInfo.getSeason(),
				this.tvShowInfo.getEpisodes().get(0),
				page);
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
			log.debug(String.format("SubLeecher Podnapisi - Start - File='%s' ; Language='%s'", this.tvShowInfo.getInputVideoFileName(), this.subLanguage));
			
			// Language ID mapping for specified language (REQUIRED)
			int languageId = -1;
			String languagePodnapisi = PropertiesHelper.getPluginPodnapisiIdLanguage(this.subLanguage);
			if (languagePodnapisi == null) {
				log.warn("No Podnapisi language ID mapped to '%' in properties file");
				return null;
			}
			else {
				languageId = Integer.parseInt(languagePodnapisi);
			}
			
			String episode = TvShowInfoHelper.getShortName(this.tvShowInfo);

			// ********************************************
			// 1 - Search Page
			
			// Connect to search page & search the episode
			log.debug(String.format("Search for episode '%s' (page 1) ...", episode));
			String searchUrl = getSearchUrl(1);
			Document docSearch = Jsoup.connect(searchUrl)
					.timeout(QUERY_TIME_OUT)
					.get();
			
			// Get last result page number
			Element aResultPage = docSearch.select("div[id=content_left] div[class=buttons] div[class=left] span[class=pages] a").last();
			int lastPageNumber = 1;
			if (aResultPage != null) {
				// At least 2 result pages available
				String resultUrl = aResultPage.attr("href");
				for (String urlPart : resultUrl.split("&"))
				{
					// Get the URL part "&page=X" to extract last page number
					String pagePart = "page=";
					if (urlPart.toLowerCase().startsWith(pagePart)) {
						lastPageNumber = Integer.parseInt(urlPart.toLowerCase().replace(pagePart, ""));
						break;
					}
				}
			}
			log.debug(String.format("> Number of result pages in search : %s", lastPageNumber));
			
			List<SubSearchResult> subSearchResults = new ArrayList<SubSearchResult>();
			
			// Iterate through result pages
			for (int i = 1 ; i <= lastPageNumber ; i++)
			{
				if (i > 1) {
					// ********************************************
					// 1.X - Next Search Pages
					
					// Connect to next result page
					log.debug(String.format("Search for episode '%s' (page %s) ...", episode, i));
					String searchUrlNext = getSearchUrl(i);
					docSearch = Jsoup.connect(searchUrlNext)
							.timeout(QUERY_TIME_OUT)
							.header("Referer", searchUrl)
							.get();
				}
								
				// Iterate through lines in results table
				Elements trResults = docSearch.select("div[id=content_left] table[class=list first_column_title] tr");
				boolean isHeader = true;
				for (Element trResult : trResults)
				{
					if (isHeader) {
						// Skip header
						isHeader = false;
						continue;
					}
					
					Element aSubtitlePage = trResult.select("a[class=subtitle_page_link]").first();
					String subtitleUrl = "";
					if (aSubtitlePage != null) {
						subtitleUrl = PODNAPISI_URL + aSubtitlePage.attr("href");
					}
					if (subtitleUrl.equals("")) {
						// No link present => next line
						continue;
					}
					
					Element divFlag = trResult.select("div[class=flag]").first();
					int flagId = -1;
					if (divFlag != null) {
						String aFlagHref = divFlag.parent().attr("href");
						String[] aFlagHrefParts = aFlagHref.split("/ppodnapisi/kategorija/jezik/");
						if (aFlagHrefParts != null && aFlagHrefParts.length > 0) {
							flagId = Integer.parseInt(aFlagHrefParts[aFlagHrefParts.length-1]);
						}
					}
					if (flagId == -1 || flagId != languageId) {
						// No language present or language mismatch => next line
						continue;
					}
					
					Element spanDescription = trResult.select("div[class=list_div2] span[class=release]").first();
					String episodeRelease = "";
					if (spanDescription != null) {
						boolean resultMatch = false;
						String concatDescription = "";
						
						// Iterate through lines of description
						String description = spanDescription.attr("html_title");
						for (String descriptionLine : description.split("<br/>"))
						{							
							// Analyze description line as TV Show Info
							TvShowInfo aEpisodeInfo = TvShowInfoHelper.populateTvShowInfoFromFreeText(descriptionLine);
							
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
								resultMatch = true;
								
								// Concatenate description with ";" separator (for log only)
								if (!concatDescription.equals("")) concatDescription += ";";
								concatDescription += descriptionLine;
								
								// Concatenate release groups with ";" separator
								String releaseGroup = aEpisodeInfo.getReleaseGroup();
								if (releaseGroup != null && !releaseGroup.equals("")) {
									if (!episodeRelease.equals("")) episodeRelease += ";";
									episodeRelease += releaseGroup;
								}
							}
						}						
						
						if (resultMatch) {
							log.debug(String.format("> Matching result found : '%s'", concatDescription));
						}
						else {
							// No TV Show matching found => next line
							log.debug(String.format("> Non matching result : '%s'", description));
							continue;
						}					
					}
					else {
						// No description present => next line
						continue;
					}
															
					Element tdDownloads = trResult.select("td:eq(3)").first();
					int episodeNbDownload = 0;
					if (tdDownloads != null) {
						// Number of downloads (OPTIONAL)
						episodeNbDownload = Integer.parseInt(tdDownloads.text());
					}
					
					subSearchResults.add(new SubSearchResult(subtitleUrl, this.subLanguage, episodeNbDownload, episodeRelease));
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
				// ********************************************
				// 2 - Subtitle Page
				
				// Connect to subtitle page
				String subtitleUrl = scoredSub.getUrl();				
				log.debug(String.format("Go to subtitle page at URL '%s' ...", subtitleUrl));
				Document docSubtitle = Jsoup.connect(subtitleUrl)
						.timeout(QUERY_TIME_OUT)
						.header("Referer", searchUrl)
						.get();
				
				Element aDownload = docSubtitle.select("div[id=subtitle] a[class=button big download]").first();
				if (aDownload == null) {
					// No download link
					log.debug("> Download not available : no download link found in page");
					continue;
				}
				
				// Get the download URL
				String downloadUrl = PODNAPISI_URL + aDownload.attr("href");
				
				
				// ********************************************
				// 3 - Download Page
				
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
			log.error("Error while trying to sub-leech files with Podnapisi", e);
            return null;
		}
		finally
		{
			log.debug(String.format("SubLeecher Podnapisi - End - File='%s' ; Language='%s'", this.tvShowInfo.getInputVideoFileName(), this.subLanguage));
		}
	}
}
