/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: StderrConsumer.java 2218 2010-10-04 09:37:39Z luca $
*/

package it.yup.util.log;

public class StderrConsumer implements LogConsumer {

	public void gotMessage(String message, int level) {
		System.err.println(message);
	}

	public void setExiting() {
		// just ignore
	}

}
