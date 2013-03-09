package org.subzero.core.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.subzero.core.bean.TvShowInfo;

/**
 * TV Show Info Helper
 * @author Julien
 *
 */
public class TvShowInfoHelper {
	
	// Constants
	private static String PATTERN_S_TYPE = "(.*?)[s](\\d{2})(([e]\\d{2})*)(.*?)(-[^- ]*)?";	
	private static String EPISODE_SEPARATOR_S_TYPE = "e";
	private static String PATTERN_X_TYPE = "(.*?)(\\d{1,2})(([x]\\d{2})*)(.*?)(-[^- ]*)?";
	private static String EPISODE_SEPARATOR_X_TYPE = "x";
	private static String PATTERN_N_TYPE = "(.*?)(\\d{1})((\\d{2})*)(.*?)(-[^- ]*)?";
	private static String PATTERN_EXT = "\\.([0-9a-z]*)";
	private static String OUTPUT_SEASON_PREFIX = "S";
	private static String OUTPUT_EPISODE_PREFIX = "E";
	private static char OUTPUT_RELEASE_GROUP_SEPARATOR = '-';
	private static char OUTPUT_SEPARATOR = '.';
	private static String OUTPUT_ZIPPED_SUBTITLE_EXTENSION = "zip";
	private static String OUTPUT_VIDEO_EXTENSION = "mkv";
	private static String FAKE_RELEASE_GROUP = "SiCKBEARD";
	
	/**
	 * Clean the naming part for input
	 * @param part
	 * @return
	 */
	private static String cleanInputNamingPart(String namingPart)
	{
		if (namingPart == null) {
			return "";
		}
		else {
			return namingPart.replace('.',' ').replace('_',' ').replace('-',' ').trim();
		}
	}
	
	/**
	 * Extract the release group from the naming part
	 * @param namingPart
	 * @return
	 */
	private static String extractReleaseGroupNamingPart(String namingPart)
	{
		if (namingPart == null) {
			return "";
		}
		
		// Remove '-' first character
		namingPart = namingPart.substring(1, namingPart.length()).trim();

		// Remove fake release group (populated by Sickbeard)
    	if (namingPart.toLowerCase().equals(TvShowInfoHelper.FAKE_RELEASE_GROUP.toLowerCase())) {
    		return "";
    	}

    	return namingPart;
	}
	
	/**
	 * Normalize the naming part for ouput
	 * @param namingPart
	 * @return
	 */
	private static String normalizeOutputNamingPart(String namingPart)
	{
		return namingPart.trim().replace(' ',TvShowInfoHelper.OUTPUT_SEPARATOR);
	}
	
	/**
	 * Normalize the season or episode number for output
	 * @param input
	 * @return
	 */
	private static String normalizeOutputSeasonOrEpisode(Integer input)
	{
		if (input > 9)
		{
			return input.toString();
		}
		else
		{
			return "0" + input.toString();
		}
	}
	
	/**
	 * Test if at least one episode matches between the two episodes lists
	 * @param episodesToSearch
	 * @param episodesInResult
	 * @return
	 */
	public static boolean testIfOneEpisodeMatches(List<Integer> episodesToSearch, List<Integer> episodesInResult) {
		boolean episodeMatch = false;
		for (Integer episodeToSearch : episodesToSearch)
		{
			for (Integer episodeInResult : episodesInResult)
			{
				if (episodeToSearch.intValue() == episodeInResult.intValue()) {
					episodeMatch = true;
					break;
				}
			}
			if (episodeMatch) {
				break;
			}
		}
		return episodeMatch;
	}
	
	/**
	 * Populate structured TV Show Info for a type
	 * @param value
	 * @param cleanedValue
	 * @param patternType
	 * @param episodeSeparatorType
	 * @param isFile if true, retrieve and test file extension (must be video) 
	 * @return
	 * @throws Exception 
	 */
	private static TvShowInfo populateTvShowInfoForType(String value, String cleanedValue, String patternType, String episodeSeparatorType, boolean isFile) throws Exception
	{
		Pattern ps = Pattern.compile(patternType, 2);

	    Matcher ms = ps.matcher(cleanedValue);
	    if (ms.matches())
	    {
	    	TvShowInfo tvShowInfo = new TvShowInfo();
	    	
	    	tvShowInfo.setSerie(TvShowInfoHelper.cleanInputNamingPart(ms.group(1)));
	    	tvShowInfo.setSeason(Integer.parseInt(TvShowInfoHelper.cleanInputNamingPart(ms.group(2))));
	    	
	    	List<Integer> episodes = new ArrayList<Integer>();
	    	String stEpisodes = TvShowInfoHelper.cleanInputNamingPart(ms.group(3)).toLowerCase();
	    	if (episodeSeparatorType == null) {
	    		for (int i = 0 ; i < stEpisodes.length() ; i = i+2) {
	    			episodes.add(Integer.parseInt(stEpisodes.substring(i, i+2)));
	    		}
	    	}
	    	else {
		    	for (String episode : stEpisodes.split(episodeSeparatorType))
		    	{
		    		if (!episode.equals(""))
		    		{
		    			episodes.add(Integer.parseInt(episode));
		    		}
		    	}
	    	}
	    	tvShowInfo.setEpisodes(episodes);
	    	
	    	tvShowInfo.setTitle(TvShowInfoHelper.cleanInputNamingPart(ms.group(5)));
	    	
	    	tvShowInfo.setReleaseGroup(TvShowInfoHelper.extractReleaseGroupNamingPart(ms.group(6)));
	    	
	    	if (isFile) {
	    		// Input string is a file name
		    	tvShowInfo.setFileType(TvShowInfoHelper.cleanInputNamingPart(ms.group(7)).toLowerCase());		    	
		    	tvShowInfo.setInputVideoFileName(value);
	    	}
	    	
	    	if (isValidTvShowInfo(tvShowInfo, isFile)) {
	    		return tvShowInfo;
	    	}
	    }
	    return null;
	}
	
	/**
	 * Check if the TV Show Info is valid or not (all properties populated + video file type only if specified)
	 * @param tvShowInfo
	 * @param isFile if true, test on video file type
	 * @return
	 * @throws Exception 
	 */
	private static boolean isValidTvShowInfo(TvShowInfo tvShowInfo, boolean isFile) throws Exception
	{
		if (tvShowInfo == null
				|| (tvShowInfo.getSerie() == null || tvShowInfo.getSerie().equals(""))
				|| (tvShowInfo.getSeason() == null || tvShowInfo.getSeason().intValue() < 1)
				|| (tvShowInfo.getEpisodes() == null || tvShowInfo.getEpisodes().size() == 0))
		{
			return false;
		}
		
		if (isFile) {
			// Check if file type exists and is video 
			if (tvShowInfo.getFileType() != null && !tvShowInfo.getFileType().equals("")
				&& tvShowInfo.getInputVideoFileName() != null && !tvShowInfo.getInputVideoFileName().equals(""))
			{
				for (String videoExtension : PropertiesHelper.getVideoFileExtensions()) {
					if (tvShowInfo.getFileType().toLowerCase().equals(videoExtension.toLowerCase())) {
						return true;
					}
				}
			}
			return false;
		}
		else {
			// No file extension test
			return true;
		}
	}
	
	/**
	 * Clean the value from all noise strings in properties
	 * @param inputVideofileName
	 * @return
	 * @throws Exception
	 */
	private static String cleanFromNoiseStrings(String value) throws Exception
	{
		for (String noiseString : PropertiesHelper.getInputFileNoiseStrings())
		{
			value = value.replace(noiseString, "");
		}
		return value;
	}
	
	/**
	 * Populate structured TV Show Info from input value
	 * @param value
	 * @param isFile if true, retrieve and test file extension (must be video) 
	 * @return
	 * @throws Exception 
	 */
	private static TvShowInfo populateTvShowInfo(String value, boolean isFile) throws Exception
	{
		String cleanedValue = TvShowInfoHelper.cleanFromNoiseStrings(value);
		
		// Try with S type
		String patternS = TvShowInfoHelper.PATTERN_S_TYPE;
		if (isFile) {
			patternS += TvShowInfoHelper.PATTERN_EXT;
		}
		TvShowInfo tvShowInfo = populateTvShowInfoForType(value, cleanedValue, patternS, TvShowInfoHelper.EPISODE_SEPARATOR_S_TYPE, isFile);
		if (tvShowInfo != null)
		{
			return tvShowInfo;
		}
		
		// Try with X type
		String patternX = TvShowInfoHelper.PATTERN_X_TYPE;
		if (isFile) {
			patternX += TvShowInfoHelper.PATTERN_EXT;
		}
		tvShowInfo = populateTvShowInfoForType(value, cleanedValue, patternX, TvShowInfoHelper.EPISODE_SEPARATOR_X_TYPE, isFile);
		if (tvShowInfo != null)
		{
			return tvShowInfo;
		}
		
		// Try with N type
		String patternN = TvShowInfoHelper.PATTERN_N_TYPE;
		if (isFile) {
			patternN += TvShowInfoHelper.PATTERN_EXT;
		}
		tvShowInfo = populateTvShowInfoForType(value, cleanedValue, patternN, null, isFile);
		return tvShowInfo;
	}
	
	/**
	 * Populate structured TV Show Info from input video file name
	 * @param inputVideoFileName
	 * @return
	 * @throws Exception 
	 */
	public static TvShowInfo populateTvShowInfo(String inputVideoFileName) throws Exception
	{
		return populateTvShowInfo(inputVideoFileName, true);
	}
	
	/**
	 * Populate structured TV Show Info from free-text
	 * @param freeText
	 * @return
	 * @throws Exception 
	 */
	public static TvShowInfo populateTvShowInfoFromFreeText(String freeText) throws Exception
	{
		return populateTvShowInfo(freeText, false);
	}
	
	/**
	 * Get short name from structured TV Show Info (type-S)
	 * @param tvShowInfo
	 * @return
	 */
	public static String getShortName(TvShowInfo tvShowInfo)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(TvShowInfoHelper.normalizeOutputNamingPart(tvShowInfo.getSerie()));
		sb.append(TvShowInfoHelper.OUTPUT_SEPARATOR);
		sb.append(TvShowInfoHelper.OUTPUT_SEASON_PREFIX);
		sb.append(TvShowInfoHelper.normalizeOutputSeasonOrEpisode(tvShowInfo.getSeason()));
		for (Integer episode : tvShowInfo.getEpisodes())
		{
			sb.append(TvShowInfoHelper.OUTPUT_EPISODE_PREFIX);
			sb.append(TvShowInfoHelper.normalizeOutputSeasonOrEpisode(episode));
		}
		return sb.toString();
	}
	
	/**
	 * Get short name from structured TV Show Info (type-X)
	 * @param tvShowInfo
	 * @return
	 */
	public static String getShortNameTypeX(TvShowInfo tvShowInfo)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(TvShowInfoHelper.normalizeOutputNamingPart(tvShowInfo.getSerie()));
		sb.append(TvShowInfoHelper.OUTPUT_SEPARATOR);
		sb.append(tvShowInfo.getSeason());
		for (Integer episode : tvShowInfo.getEpisodes())
		{
			sb.append(TvShowInfoHelper.EPISODE_SEPARATOR_X_TYPE);
			sb.append(TvShowInfoHelper.normalizeOutputSeasonOrEpisode(episode));
		}
		return sb.toString();
	}
	
	/**
	 * Prepare base output file name from structured TV Show Info
	 * @param tvShowInfo
	 * @return
	 * @throws Exception 
	 */
	public static String prepareBaseOutputFileName(TvShowInfo tvShowInfo, String language) throws Exception
	{	
		StringBuilder sb = new StringBuilder();
		sb.append(TvShowInfoHelper.getShortName(tvShowInfo));
		if (!tvShowInfo.getTitle().equals(""))
		{
			sb.append(TvShowInfoHelper.OUTPUT_SEPARATOR);
			sb.append(TvShowInfoHelper.normalizeOutputNamingPart(tvShowInfo.getTitle()));
		}
		if (!tvShowInfo.getReleaseGroup().equals(""))
		{
			sb.append(TvShowInfoHelper.OUTPUT_RELEASE_GROUP_SEPARATOR);
			sb.append(TvShowInfoHelper.normalizeOutputNamingPart(tvShowInfo.getReleaseGroup()));
		}
		sb.append(TvShowInfoHelper.OUTPUT_SEPARATOR);
		sb.append(PropertiesHelper.getSubfileLanguageSuffix(language));
		return sb.toString();
	}
	
	/**
	 * Prepare output zipped subtitle file name from structured TV Show Info and subtitle language
	 * @param tvShowInfo
	 * @return
	 * @throws Exception 
	 */
	public static String prepareZippedSubtitleFileName(TvShowInfo tvShowInfo, String language) throws Exception
	{	
		StringBuilder sb = new StringBuilder();
		sb.append(TvShowInfoHelper.prepareBaseOutputFileName(tvShowInfo, language));
		sb.append(TvShowInfoHelper.OUTPUT_SEPARATOR);
		sb.append(TvShowInfoHelper.OUTPUT_ZIPPED_SUBTITLE_EXTENSION);
		return sb.toString();
	}
	
	/**
	 * Prepare output subtitle file name from structured TV Show Info and subtitle language
	 * @param tvShowInfo
	 * @param language
	 * @param subtitleFileType
	 * @return
	 * @throws Exception 
	 */
	public static String prepareSubtitleFileName(TvShowInfo tvShowInfo, String language, String subtitleFileType) throws Exception
	{	
		StringBuilder sb = new StringBuilder();
		sb.append(TvShowInfoHelper.prepareBaseOutputFileName(tvShowInfo, language));
		sb.append(TvShowInfoHelper.OUTPUT_SEPARATOR);
		sb.append(subtitleFileType);
		return sb.toString();
	}
	
	/**
	 * Prepare output video file name from structured TV Show Info and subtitle language
	 * @param tvShowInfo
	 * @return
	 * @throws Exception 
	 */
	public static String prepareVideoFileName(TvShowInfo tvShowInfo, String language) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		sb.append(TvShowInfoHelper.prepareBaseOutputFileName(tvShowInfo, language));
		sb.append(TvShowInfoHelper.OUTPUT_SEPARATOR);
		sb.append(TvShowInfoHelper.OUTPUT_VIDEO_EXTENSION);
		return sb.toString();
	}
	
	/**
	 * Prepare output video file name from structured TV Show Info and subtitle language, with original video file extension preserved
	 * @param tvShowInfo
	 * @return
	 * @throws Exception 
	 */
	public static String prepareVideoFileNameKeepFileType(TvShowInfo tvShowInfo, String language) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		sb.append(TvShowInfoHelper.prepareBaseOutputFileName(tvShowInfo, language));
		sb.append(TvShowInfoHelper.OUTPUT_SEPARATOR);
		sb.append(tvShowInfo.getFileType());
		return sb.toString();
	}
}
