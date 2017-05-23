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
	private static String PATTERN_S_TYPE = "(?<serie>.*?([\\(\\)\\-\\s](19|20)[0-9][0-9][\\(\\)\\-]?)*)[\\.\\_\\-\\s][s](?<season>\\d{2})(?<episodes>([e]\\d{2})*)(?<title>[\\.\\_\\-\\s].*?)?(?<releasegroup>-[^- ]*)?";	
	private static String EPISODE_SEPARATOR_S_TYPE = "e";
	private static String PATTERN_X_TYPE = "(?<serie>.*?([\\(\\)\\-\\s](19|20)[0-9][0-9][\\(\\)\\-]?)*)[\\.\\_\\-\\s](?<season>\\d{1,2})(?<episodes>([x]\\d{2})*)(?<title>[\\.\\_\\-\\s].*?)?(?<releasegroup>-[^- ]*)?";
	private static String EPISODE_SEPARATOR_X_TYPE = "x";
	private static String PATTERN_N_TYPE = "(?<serie>.*?([\\(\\)\\-\\s](19|20)[0-9][0-9][\\(\\)\\-]?)*)[\\.\\_\\-\\s](?<season>\\d{1})(?<episodes>(\\d{2})*)(?<title>[\\.\\_\\-\\s].*?)?(?<releasegroup>-[^- ]*)?";
	private static String PATTERN_EXT = "\\.(?<filetype>[0-9a-z]*)";
	private static String PATTERN_SERIE_NO_YEAR = "(?<serie>.*?)([\\(\\)\\-\\s](19|20)[0-9][0-9][\\(\\)\\-]?)*[\\.\\_\\-\\s]$";
	private static String OUTPUT_SEASON_PREFIX = "S";
	private static String OUTPUT_EPISODE_PREFIX = "E";
	private static char OUTPUT_RELEASE_GROUP_SEPARATOR = '-';
	private static char OUTPUT_RELEASE_GROUP_STOPPER = '[';
	private static char OUTPUT_SEPARATOR = '.';
	private static String OUTPUT_ZIPPED_SUBTITLE_EXTENSION = "zip";
	
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
	 * @throws Exception 
	 */
	public static String extractReleaseGroupNamingPart(String namingPart) throws Exception
	{
		if (namingPart == null) {
			return "";
		}
		
		// Remove '-' first character
		String releaseGroupSeparator = String.valueOf(OUTPUT_RELEASE_GROUP_SEPARATOR);
		if (namingPart.startsWith(releaseGroupSeparator)) {
			namingPart = namingPart.substring(releaseGroupSeparator.length(), namingPart.length()).trim();
		}
		
		return namingPart;
	}
	
	/**
	 * Clean the release group from the naming part
	 * @param namingPart
	 * @return
	 * @throws Exception 
	 */
	public static String cleanReleaseGroupNamingPart(String namingPart) throws Exception
	{
		namingPart = extractReleaseGroupNamingPart(namingPart);
		
		// Remove '[...]' at the end of release group
		int posStopper = namingPart.indexOf(OUTPUT_RELEASE_GROUP_STOPPER);
		if (posStopper > -1) {
			namingPart = namingPart.substring(0, posStopper);
		}

		// Remove fake release groups (populated by SickBeard, SickRage, etc)
		for (String fakeReleaseGroup : PropertiesHelper.getSubLeecherReleaseGroupFakeList()) {
			if (fakeReleaseGroup != null && fakeReleaseGroup.toLowerCase().equals(namingPart.toLowerCase())) {
				return "";
			}
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
	 * Test if all episodes match between the two episodes lists
	 * @param episodesToSearch
	 * @param episodesInResult
	 * @return
	 */
	public static boolean testIfAllEpisodeMatch(List<Integer> episodesToSearch, List<Integer> episodesInResult) {
		
		int nbEp = episodesToSearch.size();
		if (nbEp != episodesInResult.size()) {
			return false;
		}
		
		for (int i = 0 ; i < nbEp ; i++) {
			if (!episodesToSearch.get(i).equals(episodesInResult.get(i))) {
				return false;
			}
		}
		
		return true;
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
	 * Test if at least one episode matches between the two episodes lists
	 * @param episodesToSearch
	 * @param episodeInResult
	 * @return
	 */
	public static boolean testIfOneEpisodeMatches(List<Integer> episodesToSearch, Integer episodeInResult) {
		List<Integer> episodesInResult = new ArrayList<Integer>();
		episodesInResult.add(episodeInResult);
		return testIfOneEpisodeMatches(episodesToSearch, episodesInResult);
	}
	
	/**
	 * Test if two tv show info match (serie, season and episodes)
	 * @param tvShowInfo1
	 * @param tvShowInfo2
	 * @return
	 */
	public static boolean testIfTvShowInfoMatch(TvShowInfo tvShowInfo1, TvShowInfo tvShowInfo2) {
		if (tvShowInfo1 == null && tvShowInfo2 == null) {
			return true;
		}
		
		if (tvShowInfo1 == null || tvShowInfo2 == null) {
			return false;
		}
		
		if ((tvShowInfo1.getSerie() == null && tvShowInfo2.getSerie() == null) || (tvShowInfo1.getSerie().equals(tvShowInfo2.getSerie()))
			&& (tvShowInfo1.getSeason() == null && tvShowInfo2.getSeason() == null) || (tvShowInfo1.getSeason().equals(tvShowInfo2.getSeason()))
			&& (tvShowInfo1.getEpisodes() == null && tvShowInfo2.getEpisodes() == null) || testIfAllEpisodeMatch(tvShowInfo1.getEpisodes(), tvShowInfo2.getEpisodes())
			) {
			// Serie, Season and Episodes match
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Populate structured TV Show Info for a type
	 * @param value
	 * @param cleanedValue
	 * @param patternType
	 * @param episodeSeparatorType
	 * @param isFile if true, retrieve and test file extension (must be video)
	 * @param removeYearFromSerieName
	 * @param checkVideoFileExt
	 * @return
	 * @throws Exception 
	 */
	private static TvShowInfo populateTvShowInfoForType(String value, String cleanedValue, String patternType, String episodeSeparatorType, 
														boolean isFile, boolean removeYearFromSerieName, boolean checkVideoFileExt) throws Exception
	{
		Pattern ps = Pattern.compile(patternType, 2);

	    Matcher ms = ps.matcher(cleanedValue);
	    if (ms.matches())
	    {
	    	TvShowInfo tvShowInfo = new TvShowInfo();
	    	
	    	String stSerie = TvShowInfoHelper.cleanInputNamingPart(ms.group("serie"));
	    	if (removeYearFromSerieName) {
	    		stSerie = removeYearsFromSerieName(stSerie);
	    	}
	    	tvShowInfo.setSerie(stSerie);
	    	
	    	tvShowInfo.setSeason(Integer.parseInt(TvShowInfoHelper.cleanInputNamingPart(ms.group("season"))));
	    	
	    	List<Integer> episodes = new ArrayList<Integer>();
	    	String stEpisodes = TvShowInfoHelper.cleanInputNamingPart(ms.group("episodes")).toLowerCase();
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
	    	
	    	tvShowInfo.setTitle(TvShowInfoHelper.cleanInputNamingPart(ms.group("title")));
	    	
	    	tvShowInfo.setReleaseGroup(TvShowInfoHelper.extractReleaseGroupNamingPart(ms.group("releasegroup")));
	    	
	    	tvShowInfo.setCleanedReleaseGroup(TvShowInfoHelper.cleanReleaseGroupNamingPart(ms.group("releasegroup")));
	    	
	    	if (isFile) {
	    		// Input string is a file name
		    	tvShowInfo.setFileType(TvShowInfoHelper.cleanInputNamingPart(ms.group("filetype")).toLowerCase());
		    	tvShowInfo.setInputVideoFileName(value);
	    	}
	    	
	    	if (isValidTvShowInfo(tvShowInfo, checkVideoFileExt)) {
	    		return tvShowInfo;
	    	}
	    }
	    return null;
	}
	
	/**
	 * Check if the TV Show Info is valid or not (all properties populated + video file type only if specified)
	 * @param tvShowInfo
	 * @param checkVideoFileExt
	 * @return
	 * @throws Exception 
	 */
	private static boolean isValidTvShowInfo(TvShowInfo tvShowInfo, boolean checkVideoFileExt) throws Exception
	{
		if (tvShowInfo == null
				|| (tvShowInfo.getSerie() == null || tvShowInfo.getSerie().equals(""))
				|| (tvShowInfo.getSeason() == null || tvShowInfo.getSeason().intValue() < 1)
				|| (tvShowInfo.getEpisodes() == null || tvShowInfo.getEpisodes().size() == 0))
		{
			return false;
		}
		
		if (checkVideoFileExt) {
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
	 * @param removeYearFromSerieName
	 * @param checkVideoFileExt
	 * @return
	 * @throws Exception 
	 */
	private static TvShowInfo populateTvShowInfo(String value, boolean isFile, boolean removeYearFromSerieName, boolean checkVideoFileExt) throws Exception
	{
		String cleanedValue = TvShowInfoHelper.cleanFromNoiseStrings(value);
		
		// Try with S type
		String patternS = TvShowInfoHelper.PATTERN_S_TYPE;
		if (isFile) {
			patternS += TvShowInfoHelper.PATTERN_EXT;
		}
		TvShowInfo tvShowInfo = populateTvShowInfoForType(value, cleanedValue, patternS, TvShowInfoHelper.EPISODE_SEPARATOR_S_TYPE, isFile, removeYearFromSerieName, checkVideoFileExt);
		if (tvShowInfo != null)
		{
			return tvShowInfo;
		}
		
		// Try with X type
		String patternX = TvShowInfoHelper.PATTERN_X_TYPE;
		if (isFile) {
			patternX += TvShowInfoHelper.PATTERN_EXT;
		}
		tvShowInfo = populateTvShowInfoForType(value, cleanedValue, patternX, TvShowInfoHelper.EPISODE_SEPARATOR_X_TYPE, isFile, removeYearFromSerieName, checkVideoFileExt);
		if (tvShowInfo != null)
		{
			return tvShowInfo;
		}
		
		// Try with N type
		String patternN = TvShowInfoHelper.PATTERN_N_TYPE;
		if (isFile) {
			patternN += TvShowInfoHelper.PATTERN_EXT;
		}
		tvShowInfo = populateTvShowInfoForType(value, cleanedValue, patternN, null, isFile, removeYearFromSerieName, checkVideoFileExt);
		return tvShowInfo;
	}
	
	/**
	 * Populate structured TV Show Info from input video file name
	 * @param inputVideoFileName
	 * @param removeYearFromSerieName
	 * @return
	 * @throws Exception 
	 */
	public static TvShowInfo populateTvShowInfo(String inputVideoFileName, boolean removeYearFromSerieName) throws Exception
	{
		return populateTvShowInfo(inputVideoFileName, removeYearFromSerieName, true);
	}
	
	/**
	 * Populate structured TV Show Info from input video file name
	 * @param inputVideoFileName
	 * @param removeYearFromSerieName
	 * @param checkVideoFileExt
	 * @return
	 * @throws Exception 
	 */
	public static TvShowInfo populateTvShowInfo(String inputVideoFileName, boolean removeYearFromSerieName, boolean checkVideoFileExt) throws Exception
	{
		return populateTvShowInfo(inputVideoFileName, true, removeYearFromSerieName, checkVideoFileExt);
	}
	
	/**
	 * Populate structured TV Show Info from free-text
	 * @param freeText
	 * @param removeYearFromSerieName
	 * @return
	 * @throws Exception 
	 */
	public static TvShowInfo populateTvShowInfoFromFreeText(String freeText, boolean removeYearFromSerieName) throws Exception
	{
		return populateTvShowInfoFromFreeText(freeText, removeYearFromSerieName, false);
	}
	
	/**
	 * Populate structured TV Show Info from free-text
	 * @param freeText
	 * @param removeYearFromSerieName
	 * @return
	 * @throws Exception 
	 */
	public static TvShowInfo populateTvShowInfoFromFreeText(String freeText, boolean removeYearFromSerieName, boolean checkVideoFileExt) throws Exception
	{
		return populateTvShowInfo(freeText, false, removeYearFromSerieName, checkVideoFileExt);
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
	 * Prepare suffix file name of subtitle (e.g. .fr.srt)
	 * @param language
	 * @param subFileType
	 * @return
	 * @throws Exception
	 */
	public static String prepareSubtitleSuffixFileName(String language, String subFileType) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(PropertiesHelper.getSubfileLanguageSuffix(language));
		sb.append(TvShowInfoHelper.OUTPUT_SEPARATOR);
		sb.append(subFileType);
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
	 * Remove years part from the serie name
	 * @param serieName Example : House M.D. (2004-2013) ; Doctor Who 2005
	 * @return Example : House M.D. ; Doctor Who
	 */
	public static String removeYearsFromSerieName(String serieName) {
		if (serieName == null || serieName.equals("")) {
			return null;
		}
		serieName = serieName + " ";
		Pattern ps = Pattern.compile(TvShowInfoHelper.PATTERN_SERIE_NO_YEAR, 2);
	    Matcher ms = ps.matcher(serieName);
	    if (ms.matches()) {
	    	return ms.group("serie").trim();
	    }
	    else {
	    	return serieName.trim();
	    }
	    
	}
}
