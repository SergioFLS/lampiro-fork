/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: Logger.java 2218 2010-10-04 09:37:39Z luca $
*/

package it.yup.util.log;

import java.util.Vector;

public class Logger {
	
	public static int ERROR = 0; 
	public static int WARNING = 1;
	public static int INFO = 2;
	public static int DEBUG = 3;

	private static int level = WARNING; 
		
	private static Vector consumers = new Vector();
	
	private Logger() {}; // forbid direct instantiation
	
	public static void setLevel(int level){
		Logger.level = level;
	}
	
	public static boolean isLevel(int level){
		return Logger.level >= level;
	}
	
	public static void addConsumer(LogConsumer consumer) {
		Logger.consumers.addElement(consumer);
	}
	
	public static void removeConsumer(LogConsumer consumer) {
		Logger.consumers.removeElement(consumer);
	}
	
	public static void log(String message, int level) {
		for(int i=0; i<Logger.consumers.size(); i++) {
			((LogConsumer)Logger.consumers.elementAt(i)).gotMessage(message, level);
		}
	}
	
	public static void log(String message) {
		log(message, Logger.INFO);
	}
}
