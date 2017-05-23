package org.subzero.core.plugin;

import org.apache.log4j.Logger;
import org.subzero.core.bean.SubTitleInfo;
import org.subzero.core.subleecher.SubLeecherBase;

/**
 * Sub leecher plug-in with no operation (no subtitle leech)
 * @author Julien
 *
 */
public class SubLeecherNull extends SubLeecherBase  {
	
	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubLeecherNull.class);
		
	
	/**
	 * Leech the subtitles of the TV show from the web site
	 * @return Output file name and language of the leeched subtitle or null if no subtitle found
	 * 
	 */
	@Override
	public SubTitleInfo leechSub() {
		log.debug("SubLeecher Null");
		return null;
	}
	
}
