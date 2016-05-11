package org.subzero.core.test;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.subzero.core.bean.SubTitleInfo;
import org.subzero.core.bean.TvShowInfo;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.helper.TvShowInfoHelper;
import org.subzero.core.plugin.SubLeecherAddicted;
import org.subzero.core.plugin.SubLeecherOpenSubtitles;
import org.subzero.core.plugin.SubLeecherPodnapisi;
import org.subzero.core.plugin.SubLeecherTVSubtitles;

public class SubZeroSubLeecherTest {
	
	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubZeroSubLeecherTest.class);

	@Before
	public void init()
	{
		// Initialize logger configuration
		PropertiesHelper.configureLogger();
	}
	
	//@Test
	public void testSubLeecherAddicted() throws Exception
	{
		String[] tests = {	"doctor.who.2005.s09e01.hdtv.x264-tla.mp4",
							/*"the.vampire.diaries.609.hdtv-lol.mp4",
							"The Clone Wars S03E14 HDTV - Witches of the Mist.avi",
							"doctor.who.2005.s08e01.hdtv.x264-tla.mp4",
							"the.vampire.diaries.S05E14.hdtv-lol.mp4",
							"Doctor.Who.(2005).S07E02.Dinosaurs.on.a.Spaceship.HDTV-FoV.mkv",
							"the.big.bang.theory.S07E03.mkv",
							"The.Mentalist.S01E15.Red.John.Comes.Back.TV.x264-LOL.mkv",
							"House.7x09.Hoo.Yeah-LOL.mkv",
							"Smash.S02E01E02.HDTV.x264-LOL.mp4",
							"Spaced.S01E02.Gatherings.HD.TV-SiCKBEARD.mkv"*/
		};
		
		String workingFolderPath = PropertiesHelper.getWorkingFolderPath();
		for (String test : tests)
		{
			TvShowInfo tvShowInfo = TvShowInfoHelper.populateTvShowInfo(test, true);			
			SubTitleInfo subTitleInfo = null;
			
			// 1 - Search subtitles for each language in property
			for (String language : PropertiesHelper.getSubLeecherLanguages())
			{
				SubLeecherAddicted subLeecher = new SubLeecherAddicted();

				subLeecher.initialize(workingFolderPath, tvShowInfo, language, PropertiesHelper.getSubLeecherReleaseGroupRequired());

				subTitleInfo = subLeecher.leechSub();
				if (subTitleInfo != null)
				{
					// Subtitles found !
					log.debug("SUBTITLE FOUUUUUUUUUUUUUUUUUUUUUND !");
				}				
			}
			assertNotNull(subTitleInfo);
		}
	}
	
	//@Test
	public void testSubLeecherTVSubtitles() throws Exception
	{
		String[] tests = {	"The Clone Wars S03E14 HDTV - Witches of the Mist.avi"
							/*"the.vampire.diaries.S05E14.hdtv-lol.mp4",
							"Doctor.Who.(2005).S07E02.Dinosaurs.on.a.Spaceship.HDTV-FoV.mkv",
							"Smash.1x06.mkv",
							"The.Mentalist.S01E15.Red.John.Comes.Back.TV.x264-LOL.mkv",
							"House.7x09.Hoo.Yeah-LOL.mkv",
							"Smash.S02E01E02.HDTV.x264-LOL.mp4",
							"Spaced.S01E02.Gatherings.HD.TV-SiCKBEARD.mkv"*/
		};
		
		String workingFolderPath = PropertiesHelper.getWorkingFolderPath();
		for (String test : tests)
		{
			TvShowInfo tvShowInfo = TvShowInfoHelper.populateTvShowInfo(test, true);
			SubTitleInfo subTitleInfo = null;
		
			// 1 - Search subtitles for each language in property
			for (String language : PropertiesHelper.getSubLeecherLanguages())
			{
				SubLeecherTVSubtitles subLeecher = new SubLeecherTVSubtitles();

				subLeecher.initialize(workingFolderPath, tvShowInfo, language, PropertiesHelper.getSubLeecherReleaseGroupRequired());

				subTitleInfo = subLeecher.leechSub();
				if (subTitleInfo != null)
				{
					// Subtitles found !
					log.debug("SUBTITLE FOUUUUUUUUUUUUUUUUUUUUUND !");
				}				
			}
			assertNotNull(subTitleInfo);
		}
	}
	
	//@Test
	public void testSubLeecherPodnapisi() throws Exception
	{
		String[] tests = {	"The.Walking.Dead.S06E15.East.HDTV.x264-KILLERS.mkv",
							/*"The Clone Wars S03E14 HDTV - Witches of the Mist.avi",
							"the.vampire.diaries.S05E14.hdtv-lol.mp4",
							"Doctor.Who.(2005).S07E02.Dinosaurs.on.a.Spaceship.HDTV-FoV.mkv",
							"Smash.1x06.mkv",
							"The.Mentalist.S01E15.Red.John.Comes.Back.TV.x264-LOL.mkv",
							"House.5x14.Hoo.Yeah-LOL.mkv",
							"Smash.S02E01E02.HDTV.x264-LOL.mp4",
							"White.Coller.S02E26.mkv",
							"Spaced.S01E02.Gatherings.HD.TV-SiCKBEARD.mkv"*/
		};
		
		String workingFolderPath = PropertiesHelper.getWorkingFolderPath();
		for (String test : tests)
		{
			TvShowInfo tvShowInfo = TvShowInfoHelper.populateTvShowInfo(test, true);
			SubTitleInfo subTitleInfo = null;
		
			// 1 - Search subtitles for each language in property
			for (String language : PropertiesHelper.getSubLeecherLanguages())
			{
				SubLeecherPodnapisi subLeecher = new SubLeecherPodnapisi();

				subLeecher.initialize(workingFolderPath, tvShowInfo, language, PropertiesHelper.getSubLeecherReleaseGroupRequired());

				subTitleInfo = subLeecher.leechSub();
				if (subTitleInfo != null)
				{
					// Subtitles found !
					log.debug("SUBTITLE FOUUUUUUUUUUUUUUUUUUUUUND !");
				}				
			}			
			assertNotNull(subTitleInfo);
		}
	}
	
	//@Test
	public void testSubLeecherOpenSubtitles() throws Exception
	{
		String[] tests = {	"The Clone Wars S03E14 HDTV - Witches of the Mist.avi"
							/*"the.vampire.diaries.S05E14.hdtv-lol.mp4",
							"Doctor.Who.(2005).S07E02.Dinosaurs.on.a.Spaceship.HDTV-FoV.mkv"*/
		};
		
		String workingFolderPath = PropertiesHelper.getWorkingFolderPath();
		for (String test : tests)
		{
			TvShowInfo tvShowInfo = TvShowInfoHelper.populateTvShowInfo(test, true);			
		
			// 1 - Search subtitles for each language in property
			for (String language : PropertiesHelper.getSubLeecherLanguages())
			{
				SubLeecherOpenSubtitles subLeecher = new SubLeecherOpenSubtitles();

				subLeecher.initialize(workingFolderPath, tvShowInfo, language, PropertiesHelper.getSubLeecherReleaseGroupRequired());

				SubTitleInfo subTitleInfo = subLeecher.leechSub();
				if (subTitleInfo != null)
				{
					// Subtitles found !
					log.debug("SUBTITLE FOUUUUUUUUUUUUUUUUUUUUUND !");
				}
			}
		}
	}
}
