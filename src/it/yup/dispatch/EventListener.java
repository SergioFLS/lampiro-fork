/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: EventListener.java 2218 2010-10-04 09:37:39Z luca $
*/

package it.yup.dispatch;

/**
 * Implemit.yup.xmlstreamce in order to receive stream events
 */
public interface EventListener {
	
	/**
	 *EventListenertream event happens
	 * @param event 
	 * 	The name of the event
	 * @param source 
	 * 	The source of the event. It may be either the object that has distpatched it or some
	 *  data that must be passed to the listener 
	 */
	public void gotStreamEvent(String event, Object source);
	
}
