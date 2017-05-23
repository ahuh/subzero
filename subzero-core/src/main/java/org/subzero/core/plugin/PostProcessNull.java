package org.subzero.core.plugin;

import org.apache.log4j.Logger;
import org.subzero.core.postprocess.PostProcessBase;


/**
 * Post-Process plug-in with no operation (disable post-processing)
 * @author Julien
 *
 */
public class PostProcessNull extends PostProcessBase {
	
	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(PostProcessNull.class);
	
	/**
	 * Launch the post-process after subtitle retrieval with video file of the TV show
	 * @return Success (true) or failure (false)
	 */
	@Override
	public boolean launchPostProcess() {
		log.debug("Post-Process Move Null");
		return true;
	}
	
}
