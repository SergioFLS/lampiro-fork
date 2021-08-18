/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: LogConsumer.java 2218 2010-10-04 09:37:39Z luca $
*/

package it.yup.util.log;

public interface LogConsumer {
	
	public void gotMessage(String message, int level);
	
	public void setExiting();
}
