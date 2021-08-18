/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: Iq.java 2328 2010-11-16 14:11:30Z luca $
*/

package it.yup.xmpp.packets;

import it.yup.xml.Element;
import it.yup.xmlstream.BasicXmlStream;
import it.yup.xmpp.IQResultListener;
import it.yup.xmpp.IqManager;

public class Iq extends Stanza {

	public static final String T_GET = "get";
	public static final String T_SET = "set";
	public static final String T_RESULT = "result";
	public static final String T_ERROR = "error";

	public static final String IQ = "iq";
	public static final String QUERY = "query";
	public static final String PROMPT = "prompt";

	public Iq(String to, String type) {
		super(IQ, to, type,
				type.equals(T_SET) || type.equals(T_GET) ? createUniqueId()
						: null);
	}

	/**
	 * Send an Iq packet and register the packet listener for the answer
	 * 
	 * @param iq
	 *            the iq
	 * @param listener
	 *            the listener
	 * @param duration
	 *            the duration before expire in milliseconds
	 */
	public void send(BasicXmlStream xmlStream, IQResultListener listener,
			int duration) {
		if (listener != null) {
			IqManager.getInstance().addRegistration(this, listener, duration);
		}
		xmlStream.send(this);
	}

	/**
	 * Send an Iq packet and register the packet listener for the answer
	 * 
	 * @param iq
	 * @param listener
	 *            (may be null)
	 */
	public void send(BasicXmlStream xmlStream, IQResultListener listener) {
		send(xmlStream, listener, -1);
	}

	public static Iq easyReply(Element e) {
		Iq replIq = new Iq(e.getAttribute(Iq.ATT_FROM), Iq.T_RESULT);
		replIq.setAttribute(Iq.ATT_ID, e.getAttribute(Iq.ATT_ID));
		return replIq;
	}
}
