package org.subzero.core.plugin;

import org.apache.log4j.Logger;
import org.subzero.core.subchecker.SubCheckerBase;

/**
 * Sub checker plug-in with no operation (no subtitle check)
 * @author Julien
 *
 */
public class SubCheckerNull extends SubCheckerBase  {

	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubCheckerNull.class);

	/**
	 * Checks if an internal subtitle file corresponding to the given language already exists in the video file : always return false
	 * @return true : internal subtitle exists ; false : no internal subtitle
	 */
	@Override
	public boolean checkInternalSub() {
		log.debug("SubChecker Null");
		return false;
	}
}
