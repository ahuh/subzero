package org.subzero.core.plugin;

import org.apache.log4j.Logger;
import org.subzero.core.subchecker.SubCheckerBase;

/**
 * SubChecker plugin for MKV files
 * @author Julien
 *
 */
public class SubCheckerMkv extends SubCheckerBase  {

	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubCheckerMkv.class);

	/**
	 * Checks if an internal subtitle file corresponding to the given language already exists in the MKV video file
	 * @return true : internal subtitle exists ; false : no internal subtitle
	 */
	@Override
	public boolean checkInternalSub() {
		// TODO Implémenter la logique avec appel ligne commande MKVMerge et parsing JSON ! 
		return false;
	}	
	
}
