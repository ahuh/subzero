package org.subzero.core.test;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.subzero.core.bean.SubTitleInfo;
import org.subzero.core.bean.TvShowInfo;
import org.subzero.core.helper.FileHelper;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.helper.TvShowInfoHelper;
import org.subzero.core.plugin.SubLeecherAddicted;
import org.subzero.core.plugin.SubLeecherOpenSubtitles;
import org.subzero.core.plugin.SubLeecherPodnapisi;
import org.subzero.core.plugin.SubLeecherTVSubtitles;
import org.subzero.core.postprocess.PostProcessLauncher;
import org.subzero.core.subleecher.SubLeecherBus;

public class SubZeroTest {
	
	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubZeroTest.class);

	@Before
	public void init()
	{
		// Initialize logger configuration
		PropertiesHelper.configureLogger();
	}
	
	//@Test
	public void testFullProcess() throws Exception
	{
		for (String inputVideoFileName : FileHelper.getWorkingVideoFiles())
		{
			log.debug("inputVideoFileName=" + inputVideoFileName);
						
			TvShowInfo tvShowInfo = TvShowInfoHelper.populateTvShowInfo(inputVideoFileName, true);
					
			SubLeecherBus subLeecherBus = new SubLeecherBus(tvShowInfo);
			SubTitleInfo subTitleInfo = subLeecherBus.checkExistingSubFile();				
			if (subTitleInfo == null) {
				subTitleInfo = subLeecherBus.launchBus();				
				if (subTitleInfo != null) {
					log.debug("subLeecherBusFound=" + subTitleInfo.getSubFileName() + ";" + subTitleInfo.getLanguage());
				}
				assertNotNull(subTitleInfo);
			}
			else {
				log.debug("subFileAlreadyExists=" + subTitleInfo.getSubFileName() + ";" + subTitleInfo.getLanguage());
			}
			
			if (subTitleInfo != null) {
				PostProcessLauncher postProcessor = new PostProcessLauncher(tvShowInfo, subTitleInfo);
				boolean ppResult = postProcessor.launchPostProcess();
				if (ppResult) {
					log.debug("ok!");
				}
				assertTrue(ppResult);
			}
		}		
	}

	@Test
	public void testSubLeecherAddicted() throws Exception
	{
		String[] tests = {	"doctor.who.2005.s08e01.hdtv.x264-tla.mp4"/*,
							"the.vampire.diaries.S05E14.hdtv-lol.mp4",
							"Doctor.Who.(2005).S07E02.Dinosaurs.on.a.Spaceship.HDTV-FoV.mkv",
							"the.big.bang.theory.S07E03.mkv",
							"The.Mentalist.S01E15.Red.John.Comes.Back.TV.x264-LOL.mkv",
							"House.7x09.Hoo.Yeah-LOL.mkv",
							"Smash.S02E01E02.HDTV.x264-LOL.mp4",
							"Spaced.S01E02.Gatherings.HD.TV-SiCKBEARD.mkv"*/		
		};
		
		for (String test : tests)
		{
			TvShowInfo tvShowInfo = TvShowInfoHelper.populateTvShowInfo(test, true);			
		
			// 1 - Search subtitles for each language in property
			for (String language : PropertiesHelper.getSubLeecherLanguages())
			{
				SubLeecherAddicted subLeecher = new SubLeecherAddicted();

				subLeecher.initialize(tvShowInfo, language, PropertiesHelper.getSubLeecherReleaseGroupRequired());

				SubTitleInfo subTitleInfo = subLeecher.leechSub();
				if (subTitleInfo != null)
				{
					// Subtitles found !
					log.debug("SUBTITLE FOUUUUUUUUUUUUUUUUUUUUUND !");
				}
				assertNotNull(subTitleInfo);
			}
		}
	}
	
	//@Test
	public void testSubLeecherTVSubtitles() throws Exception
	{
		String[] tests = {	"the.vampire.diaries.S05E14.hdtv-lol.mp4",
							"Doctor.Who.(2005).S07E02.Dinosaurs.on.a.Spaceship.HDTV-FoV.mkv",
							"Smash.1x06.mkv",
							"The.Mentalist.S01E15.Red.John.Comes.Back.TV.x264-LOL.mkv",
							"House.7x09.Hoo.Yeah-LOL.mkv",
							"Smash.S02E01E02.HDTV.x264-LOL.mp4",
							"Spaced.S01E02.Gatherings.HD.TV-SiCKBEARD.mkv"
		};
		
		for (String test : tests)
		{
			TvShowInfo tvShowInfo = TvShowInfoHelper.populateTvShowInfo(test, true);			
		
			// 1 - Search subtitles for each language in property
			for (String language : PropertiesHelper.getSubLeecherLanguages())
			{
				SubLeecherTVSubtitles subLeecher = new SubLeecherTVSubtitles();

				subLeecher.initialize(tvShowInfo, language, PropertiesHelper.getSubLeecherReleaseGroupRequired());

				SubTitleInfo subTitleInfo = subLeecher.leechSub();
				if (subTitleInfo != null)
				{
					// Subtitles found !
					log.debug("SUBTITLE FOUUUUUUUUUUUUUUUUUUUUUND !");
				}
				assertNotNull(subTitleInfo);
			}
		}
	}
	
	//@Test
	public void testSubLeecherPodnapisi() throws Exception
	{
		String[] tests = {	"the.vampire.diaries.S05E14.hdtv-lol.mp4",
							"Doctor.Who.(2005).S07E02.Dinosaurs.on.a.Spaceship.HDTV-FoV.mkv",
							"Smash.1x06.mkv",
							"The.Mentalist.S01E15.Red.John.Comes.Back.TV.x264-LOL.mkv",
							"House.5x14.Hoo.Yeah-LOL.mkv",
							"Smash.S02E01E02.HDTV.x264-LOL.mp4",
							"White.Coller.S02E26.mkv",
							"Spaced.S01E02.Gatherings.HD.TV-SiCKBEARD.mkv"
		};
		
		for (String test : tests)
		{
			TvShowInfo tvShowInfo = TvShowInfoHelper.populateTvShowInfo(test, true);			
		
			// 1 - Search subtitles for each language in property
			for (String language : PropertiesHelper.getSubLeecherLanguages())
			{
				SubLeecherPodnapisi subLeecher = new SubLeecherPodnapisi();

				subLeecher.initialize(tvShowInfo, language, PropertiesHelper.getSubLeecherReleaseGroupRequired());

				SubTitleInfo subTitleInfo = subLeecher.leechSub();
				if (subTitleInfo != null)
				{
					// Subtitles found !
					log.debug("SUBTITLE FOUUUUUUUUUUUUUUUUUUUUUND !");
				}
			}
		}
	}
	
	//@Test
	public void testSubLeecherOpenSubtitles() throws Exception
	{
		String[] tests = {	"the.vampire.diaries.S05E14.hdtv-lol.mp4",
							"Doctor.Who.(2005).S07E02.Dinosaurs.on.a.Spaceship.HDTV-FoV.mkv"
		};
		
		for (String test : tests)
		{
			TvShowInfo tvShowInfo = TvShowInfoHelper.populateTvShowInfo(test, true);			
		
			// 1 - Search subtitles for each language in property
			for (String language : PropertiesHelper.getSubLeecherLanguages())
			{
				SubLeecherOpenSubtitles subLeecher = new SubLeecherOpenSubtitles();

				subLeecher.initialize(tvShowInfo, language, PropertiesHelper.getSubLeecherReleaseGroupRequired());

				SubTitleInfo subTitleInfo = subLeecher.leechSub();
				if (subTitleInfo != null)
				{
					// Subtitles found !
					log.debug("SUBTITLE FOUUUUUUUUUUUUUUUUUUUUUND !");
				}
			}
		}
	}
	
	//@Test
	public void testRegEx() throws Exception
	{
		String[] tests = {	"Doctor.Who.(2005).S07E02.Dinosaurs.on.a.Spaceship.HDTV-FoV.mkv",
							"The.Mentalist.S01E15.Red.John.Comes.Back.TV-x264-LOL.nzb.mkv",
							"The.Mentalist.S01E15.Red.John.Comes.Back.TV.x264-LOL.mkv",
							"The.Mentalist.S01E15.Red.John.Comes.Back.TV.x264-SiCKBEARD.mkv",				
							"The.Mentalist.S01E15.Red.John.Comes.Back.JPISUBFR.srt.mp4",
							"The.Mentalist.115.mkv",
							"The.Mentalist.11516.mkv",
							"The.Mentalist.1151617.Red.John.Comes.Back.TV.x264-LOL.mkv",
							"The.Mentalist.S01E15.mkv", 	
							"The.Mentalist.S01E15.Red.John.Comes.Back.mkv",
							"The.Mentalist.S01E15E16.Red.John.Comes.Back.mkv",
							"The_Mentalist_S01E15.mkv",
							"The_Mentalist_S01E15_Red_John_Comes_Back.mkv",
							"The_Mentalist_S01E15E16_Red_John_Comes_Back.mkv",
							"The Mentalist - S01E15.mkv",
							"The Mentalist - S01E15 - Red John Comes Back.mkv",
							"The Mentalist S01E15E16 - Red John Comes Back.mkv",
		                   	"The.Mentalist.1x15.Red.John.Comes.Back.mkv",
		                   	"The.Mentalist.1x15.mkv", 	
							"The.Mentalist.1x15.Red.John.Comes.Back.mkv",
							"The.Mentalist.1x15x16.Red.John.Comes.Back.mkv",
							"The_Mentalist_1x15.mkv",
							"The_Mentalist_1x15_Red_John_Comes_Back.mkv",
							"The_Mentalist_1x15x16_Red_John_Comes_Back.mkv",
							"The Mentalist - 1x15.mkv",
							"The Mentalist - 1x15 - Red John Comes Back.mkv",
							"The Mentalist - 1x15x16 - Red John Comes Back.mkv"};

		for (String test : tests)
		{
			TvShowInfo tvShowInfo = TvShowInfoHelper.populateTvShowInfo(test, true);
			assertNotNull(tvShowInfo);
			if (tvShowInfo == null) {
				log.debug(String.format("No TvShowInfo > input==%s", test));
			}
			else {
				String ep = "";
				for (Integer i : tvShowInfo.getEpisodes())
				{
					ep += "|" + i.toString() + "|";
				}			
				log.debug(String.format("TvShowInfo > serie=%s ; season=%s ; episode=%s ; title=%s ;  group=%s ; filetype=%s",
						tvShowInfo.getSerie(),
						tvShowInfo.getSeason(),
						ep,
						tvShowInfo.getTitle(),
						tvShowInfo.getReleaseGroup(),
						tvShowInfo.getFileType()));
				
				String shortName = TvShowInfoHelper.getShortName(tvShowInfo);			
				log.debug(String.format("ShortName > %s",
						shortName));
				
				String fileNameWithoutExt = TvShowInfoHelper.prepareVideoFileName(tvShowInfo, "French");			
				log.debug(String.format("PrepareVideoFileName > %s",
						fileNameWithoutExt));
				
				String fileName = TvShowInfoHelper.prepareSubtitleFileName(tvShowInfo, "French", "srt");			
				log.debug(String.format("PrepareSubtitleFileName > %s",
						fileName));
				
				assertNotNull(tvShowInfo.getSerie());
				assertNotNull(tvShowInfo.getSeason());
				assertNotNull(ep);
				assertNotEquals(tvShowInfo.getSerie(), "");
				assertNotEquals(tvShowInfo.getSeason(), "");
				assertNotEquals(ep, "");
			}
		}
	}
}
