package org.subzero.core.subleecher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.subzero.core.bean.SubSearchResult;

/**
 * SubLeecher Helper
 * @author Julien
 *
 */
public class SubLeecherHelper {
	
	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubLeecherHelper.class);
	
	/**
	 * Remove all non alphabetics or digits characters in string (to lower case)
	 * @param value
	 * @return
	 */
	public static String keepOnlyAlphaAndDigits(String value) {
		if (value == null) {
			value = "";
		}
		return value.replaceAll("[^a-zA-Z0-9]+","").toLowerCase();
	}
	
	/**
	 * Remove all non alphabetics characters in string (to lower case)
	 * @param value
	 * @return
	 */
	public static String keepOnlyAlpha(String value) {
		if (value == null) {
			value = "";
		}
		return value.replaceAll("[^a-zA-Z]+","").toLowerCase();
	}
	
	/**
	 * Remove all non digits characters in string
	 * @param value
	 * @return
	 */
	public static String keepOnlyDigits(String value) {
		if (value == null) {
			value = "";
		}
		return value.replaceAll("[^0-9]+","");
	}
	
	/**
	 * Remove a year at the end of a string
	 * @param value
	 * @return
	 */
	public static String removeYearAtTheEnd(String value) {
		if (value == null) {
			value = "";
		}
		return value.replaceAll("[0-2][0-9]{3}$","").toLowerCase();
	}
	
	/**
	 * Does "value" equals "search" (loose-matching) 
	 * @param value
	 * @param search
	 * @return
	 */
	public static boolean looseMatchEquals(String value, String search) {
		String adSearch = SubLeecherHelper.keepOnlyAlphaAndDigits(search);
		String adValue = SubLeecherHelper.keepOnlyAlphaAndDigits(value);
		
		if (!search.equals("") && !value.equals("") && adValue.equals(adSearch)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Does "value" string starts with "search" (loose-matching) 
	 * @param value
	 * @param search
	 * @param removeYearInSerie
	 * @return
	 */
	public static boolean looseMatchStartsWith(String value, String search, boolean removeYearInSerie) {
		String adSearch = SubLeecherHelper.keepOnlyAlphaAndDigits(search);
		String adValue = SubLeecherHelper.keepOnlyAlphaAndDigits(value);
		if (removeYearInSerie) {
			adSearch = SubLeecherHelper.removeYearAtTheEnd(adSearch);
			adValue = SubLeecherHelper.removeYearAtTheEnd(adValue);
		}
		
		if (!search.equals("") && !value.equals("") && adValue.startsWith(adSearch)) {
			return true;			
		}
		
		return false;
	}
	
	/**
	 * Does "value" string ends with "search" (loose-matching) 
	 * @param value
	 * @param search
	 * @param removeYearInSerie
	 * @return
	 */
	public static boolean looseMatchEndsWith(String value, String search, boolean removeYearInSerie) {
		String adSearch = SubLeecherHelper.keepOnlyAlphaAndDigits(search);
		String adValue = SubLeecherHelper.keepOnlyAlphaAndDigits(value);
		if (removeYearInSerie) {
			adSearch = SubLeecherHelper.removeYearAtTheEnd(adSearch);
			adValue = SubLeecherHelper.removeYearAtTheEnd(adValue);
		}
		
		if (!search.equals("") && !value.equals("") && adValue.endsWith(adSearch)) {
			return true;			
		}
		
		return false;
	}
	
	/**
	 * Does "value" string contains "search" (loose-matching) 
	 * @param value
	 * @param search
	 * @return
	 */
	public static boolean looseMatchContains(String value, String search) {
		String adSearch = SubLeecherHelper.keepOnlyAlphaAndDigits(search);
		String adValue = SubLeecherHelper.keepOnlyAlphaAndDigits(value);
		
		if (!search.equals("") && !value.equals("") && adValue.contains(adSearch)) {
			return true;			
		}
		
		return false;
	}
	
	/**
	 * Evaluate the score for each subtitle search result in list, and return the sorted list (best score to worst score)
	 * @param subSearchResults
	 * @param releaseGroup
	 * @param releaseGroupMatchRequired if "true" and "releaseGroup" specified, the non matching release group results will be excluded
	 * @return
	 */
	public static List<SubSearchResult> evaluateScoreAndSort(List<SubSearchResult> subSearchResults, String releaseGroup, boolean releaseGroupMatchRequired) {
		// Evaluate score and build new list
		List<SubSearchResult> outList = new ArrayList<SubSearchResult>();
		for (SubSearchResult subSearchResult : subSearchResults)
		{
			SubSearchResult outResult = SubLeecherHelper.evaluateScore(subSearchResult, releaseGroup, releaseGroupMatchRequired);
			if (outResult.getScore() == -1) {
				if (log.isInfoEnabled()) {
					log.info(String.format("> Non matching result (no match for release group '%s') : Language='%s' ; NbDowloads='%s' ; Release='%s' ; Score='%s' ; URL='%s'", 
							releaseGroup,
							outResult.getLanguage(),
							outResult.getNbDownloads(),
							outResult.getRelease(),
							outResult.getScore(),
							outResult.getUrl()));
				}
			}
			else {
				outList.add(outResult);
			}
		}
		
		if (outList.size() > 0) {
			// Sort the new list from best score to worst score
			Collections.sort(outList);
		}
		
		if (log.isInfoEnabled()) {
			for (int i = 0 ; i < outList.size() ; i++) {
				log.info(String.format("> Matching result #%s : Language='%s' ; NbDowloads='%s' ; Release='%s' ; Score='%s' ; URL='%s'", 
						i+1,
						outList.get(i).getLanguage(),
						outList.get(i).getNbDownloads(),
						outList.get(i).getRelease(),
						outList.get(i).getScore(),
						outList.get(i).getUrl()));
			}
		}
		
		return outList;
	}
	
	/**
	 * Evaluation the score for the subtitle search result, based on release group match and number of downloads
	 * @param subSearchResult
	 * @param releaseGroup
	 * @param releaseGroupMatchRequired if "true" and "releaseGroup" specified, a non matching release group will return null
	 * @return SubSearchResult object with score populated, or with score = -1 if excluded because of "releaseGroup" & "releaseGroupMatchRequired" parameters
	 */
	public static SubSearchResult evaluateScore(SubSearchResult subSearchResult, String releaseGroup, boolean releaseGroupMatchRequired) {
		if (subSearchResult == null) {
			return null;
		}

		SubSearchResult outResult = new SubSearchResult(
				subSearchResult.getUrl(),
				subSearchResult.getLanguage(),
				subSearchResult.getNbDownloads(), 
				subSearchResult.getRelease());
		
		int releaseMatchBonus = 0;
		if (releaseGroup == null || releaseGroup.equals("")) {
			// No release group specified : no bonus on score
		}
		else {
			// Release group specified
			String srRelease = outResult.getRelease();
			if (srRelease == null || srRelease.equals("")) {
				// No release in result : no bonus on score
			}
			else {
				if (SubLeecherHelper.looseMatchEquals(srRelease, releaseGroup)) {
					// Search result release equals specified release group : 1000 points !
					releaseMatchBonus = 1000;
				}
				else if (SubLeecherHelper.looseMatchStartsWith(srRelease, releaseGroup, false)) {
					// Search result release starts with specified release group : 500 points !
					releaseMatchBonus = 500;
				}
				else if (SubLeecherHelper.looseMatchEndsWith(srRelease, releaseGroup, false)) {
					// Search result release ends with specified release group : 250 points !
					releaseMatchBonus = 250;
				}
				else if (SubLeecherHelper.looseMatchContains(srRelease, releaseGroup)) {
					// Search result release contains specified release group : 100 points !
					releaseMatchBonus = 100;
				}
			}
			if (releaseGroupMatchRequired && releaseMatchBonus == 0) {
				// The Release group is required and specified in search, but has not been found in result
				// => exclude this result
				outResult.setScore(-1);
				return outResult;
			}
		}
		
		// We calculate the score with ponderation : Bonus Release Group x 1000 + Nb Downloads
		outResult.setScore((releaseMatchBonus*1000) + outResult.getNbDownloads());
		
		return outResult;
	}
}