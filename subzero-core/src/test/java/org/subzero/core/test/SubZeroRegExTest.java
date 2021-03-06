package org.subzero.core.test;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.subzero.core.bean.TvShowInfo;
import org.subzero.core.helper.PropertiesHelper;
import org.subzero.core.helper.TvShowInfoHelper;

public class SubZeroRegExTest {
	
	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubZeroRegExTest.class);

	@Before
	public void init()
	{
		// Initialize logger configuration
		PropertiesHelper.configureLogger();
	}
	
	@Test
	public void testRegExSerieYear() throws Exception
	{
		String[] tests = {	"Doctor Who",
							"Doctor Who 2005",
							"Doctor Who (2005)",
							"House M.D.",
							"House M.D. 1992-2013",
							"House M.D. (1992-2013)"
						};
		
		for (String test : tests)
		{
			log.debug(String.format("**** Serie Year > input=%s", test));
			String serieNoYear = TvShowInfoHelper.removeYearsFromSerieName(test);			
			log.debug(String.format("Serie Year > output=%s", serieNoYear));
			
			// Check no numeric in value
			assertNotNull(serieNoYear);
			assertNotEquals(serieNoYear, "");
			assertFalse(serieNoYear.matches(".*\\d.*"));
		}
	}
	
	@Test
	public void testRegExSerieNoYear() throws Exception
	{
		String[] tests = {	"Beverly Hills 90210",
							"Anno 1790",
							"Code 37",
							"This Is England 86"
						};
		
		for (String test : tests)
		{
			log.debug(String.format("**** Serie No Year > input=%s", test));
			String serieNoYear = TvShowInfoHelper.removeYearsFromSerieName(test);			
			log.debug(String.format("Serie No Year > output=%s", serieNoYear));
			
			// Check numeric in value
			assertNotNull(serieNoYear);
			assertNotEquals(serieNoYear, "");
			assertTrue(serieNoYear.matches(".*\\d.*"));
		}
	}
	
	@Test
	public void testRegEx() throws Exception
	{
		String[] tests = {	"Doctor Who - 09x01 - The Magician's Apprentice.mp4",
							"doctor.who.2005.s09e01.hdtv.x264-tla.mp4",
							"The.Strain.S02E08.Intruders.720p.WEB-DL.DD5.1.H.264-Juggalotus.mkv",
							"Defiance.S03E06.Where.The.Apples.Fell.1080p.WEB-DL.DD5.1.H.264-ECI.mkv",
							"The.Strain.S02E01.BK.NY.720p.WEB-DL.DD5.1.H.264-Juggalotus.mkv",
							"doctor_who_2005_S08E03E04_hdtv_x264-tla.mp4",
							"doctor_who_2005_8x03x04_hdtv_x264-tla.mp4",
							"doctor_who_2005_80304_hdtv_x264-tla.mp4",
							"Doctor.Who.(2005).S07E02.Dinosaurs.on.a.Spaceship.HDTV-FoV.mkv",
							"Doctor.Who.(2005).7x02.Dinosaurs.on.a.Spaceship.HDTV-FoV.mkv",
							"Doctor.Who.(2005).702.Dinosaurs.on.a.Spaceship.HDTV-FoV.mkv",
							"doctor_who_2004_2013_S08E03E04_hdtv_x264-tla.mp4",
							"doctor_who_2004_2013_8x03x04_hdtv_x264-tla.mp4",
							"doctor_who_2004_2013_80304_hdtv_x264-tla.mp4",
							"Doctor.Who.(2004-2013).S07E02.Dinosaurs.on.a.Spaceship.HDTV-FoV.mkv",
							"Doctor.Who.(2004-2013).7x02.Dinosaurs.on.a.Spaceship.HDTV-FoV.mkv",
							"Doctor.Who.(2004-2013).702.Dinosaurs.on.a.Spaceship.HDTV-FoV.mkv",
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
							"The Mentalist - 1x15x16 - Red John Comes Back.mkv"
						};

		for (String test : tests)
		{
			log.debug(String.format("**** TvShowInfo > input=%s", test));
			TvShowInfo tvShowInfo = TvShowInfoHelper.populateTvShowInfo(test, false);
			if (tvShowInfo == null) {
				log.debug(String.format("No TvShowInfo > input=%s", test));
				assertNotNull(tvShowInfo);
			}
			else {
				String ep = "";
				for (Integer i : tvShowInfo.getEpisodes())
				{
					ep += "|" + i.toString() + "|";
				}			
				log.debug(String.format("TvShowInfo > serie=%s ; season=%s ; episode=%s ; title=%s ; group=%s ; filetype=%s",
						tvShowInfo.getSerie(),
						tvShowInfo.getSeason(),
						ep,
						tvShowInfo.getTitle(),
						tvShowInfo.getReleaseGroup(),
						tvShowInfo.getFileType()));
				
				String shortName = TvShowInfoHelper.getShortName(tvShowInfo);			
				log.debug(String.format("ShortName > %s",
						shortName));
				
				String fileName = TvShowInfoHelper.prepareSubtitleFileName(tvShowInfo, "French", "srt");			
				log.debug(String.format("PrepareSubtitleFileName > %s",
						fileName));
				
				// Check non empty values
				assertNotNull(tvShowInfo.getSerie());
				assertNotNull(tvShowInfo.getSeason());
				assertNotNull(ep);
				assertNotEquals(tvShowInfo.getSerie(), "");
				assertNotEquals(tvShowInfo.getSeason(), "");
				assertNotEquals(tvShowInfo.getSeason(), "0");
				assertNotEquals(ep, "");
				for (Integer i : tvShowInfo.getEpisodes())
				{
					assertNotNull(i);
					assertNotEquals(i.intValue(), 0);
				}
			}
		}
	}
}
