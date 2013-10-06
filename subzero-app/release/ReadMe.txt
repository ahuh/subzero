SubZero
=======
Watches your TV show video files... and adds subtitle
by ahuh
v1.0.3
2013-10-06


What is it ?
------------
SubZero is a subtitle autodownloader for TV show video files.

Features :

- Watches your incoming video files folder for new files to process

- Filename analysis with information extraction (TV show name, season, episodes, title, release group)
  from multiple patterns (see examples below)
  	
- Support of subleecher plugins, to fetch subtitles from multiple web sites

- Support of multiple languages subtitles : if it does not find subtitle for the first specified language,
  it tries to find subtitle for the next language
  
- Support of post-processing plugin, launched after subtitle retrieval. By default, you may move video and
  subtitle files to an output folder (supporting TV show name / season / episode token replacement), and
  merge both files as MKV file (with MKVMerge)
  
- Report and technical logs (with log4j)

- By default, SubZero is a SysTray application, but it can run as a background process with no GUI (daemon)


Supported input file name patterns (examples)
---------------------------------------------
	TV.Show.Name.S01E15.Episode.Title.TV.x264-ReleaseGroup.mkv
	TV Show Name 1x15 Episode Title TV x264-ReleaseGroup.mkv
	TV_Show_Name_115_Episode_Title_TV_x264-ReleaseGroup.mkv
	TV.Show.Name.S01E15E16-ReleaseGroup.mkv
	TV Show Name 1x15x16-ReleaseGroup.mkv
	TV_Show_Name_11516-ReleaseGroup.mkv


Prerequisites
-------------
- Java 1.7 installed (JRE or JDK)
- MKVMerge installed (if used as post-processing plugin)


How to launch
-------------
	SubZero.jar						=> Executable JAR (double-click on it), in 'SysTray' mode
	
	java -jar Subzero.jar			=> Execute JAR from java command line, in 'SysTray' mode
	
	java -jar SubZero.jar d
	OR								=> Execute JAR from java command line, in 'Daemon' mode (no GUI)
	java -jar SubZero.jar daemon


How to configure
----------------
Configure 'SubZero.properties' in the same directory as 'SubZero.jar'.
See the file content for help.


License
-------
Released under MIT license:
http://opensource.org/licenses/MIT