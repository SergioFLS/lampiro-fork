/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: PacketListener.java 2325 2010-11-15 20:07:28Z luca $
*/

package it.yup.xmlstream;

import it.yup.xml.Element;


/**
 * Listening to XMPP packets
 *
 */
public interface PacketListener 
{
	/**
	 * Called when an XMPP element is received
	 * @param e
	 */
    public void packetReceived (Element e);
}
