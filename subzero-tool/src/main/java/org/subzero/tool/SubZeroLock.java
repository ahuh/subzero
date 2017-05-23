package org.subzero.tool;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import org.apache.log4j.Logger;
 
/**
 * Lock Class to prevent several instances 
 * @author Burhan Uddin
 */
public class SubZeroLock {
	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(SubZeroLock.class);
	
    private static File f;
    private static FileChannel channel;
    private static FileLock lock;
     
    public SubZeroLock() throws Exception
    {
        try
        {
        	URL resourceURL = SubZeroLock.class.getResource("/");
            f = new File(URLDecoder.decode(resourceURL.getFile(), "UTF-8") + "SubZero.lock");
            // Check if the lock exist
            if (f.exists()) // if exist try to delete it
                f.delete();
            // Try to get the lock
            @SuppressWarnings("resource")
			RandomAccessFile rf =  new RandomAccessFile(f, "rw");
            channel = rf.getChannel();
            lock = channel.tryLock();
            if(lock == null)
            {
                // File is lock by other application
                channel.close();
                throw new RuntimeException("Two instance cannot run at a time.");
            }
            // Add shutdown hook to release lock when application shutdown
            ShutdownHook shutdownHook = new ShutdownHook();
            Runtime.getRuntime().addShutdownHook(shutdownHook);
             
        }
        catch(IOException e)
        {
            throw new Exception("Could not start process.", e);
        }
    }
 
    public static void unlockFile() {
        // release and delete file lock
        try
        {
            if (lock != null)
            {
                lock.release();
                channel.close();
                f.delete();
            }
        }
        catch(IOException e)
        {
        	log.error("Error while unlocking file", e);
        }
 }
 
    static class ShutdownHook extends Thread {
        public void run() {
            unlockFile();
        }
    }
}
