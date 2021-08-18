/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: IqManager.java 1858 2009-10-16 22:42:29Z luca $
*/

package it.yup.xmpp;

import it.yup.dispatch.EventQuery;
import it.yup.xml.Element;
import it.yup.xmlstream.BasicXmlStream;
import it.yup.xmpp.packets.Iq;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author luca
 *
 */
public class IqManager extends IQResultListener {

	private static IqManager _instance = null;
	
	private int maxPermTime = 120000;

	/**
	 * 
	 */
	private IqManager() {
	}
	
	public void setMaxPermTime (int maxPermTime){
		this.maxPermTime= maxPermTime;
	}

	private Hashtable iqs = new Hashtable();

	public void streamInitialized() {
		// prepare the Iq listener
		EventQuery eq = new EventQuery("iq", new String[] { "type" },
				new String[] { Iq.T_ERROR });
		BasicXmlStream.addPacketListener(eq, _instance);
		eq = new EventQuery("iq", new String[] { "type" },
				new String[] { "result" });
		BasicXmlStream.addPacketListener(eq, _instance);
	}

	public static IqManager getInstance() {
		if (_instance == null) {
			_instance = new IqManager();
		}
		return _instance;
	}

	public void handleError(Element e) {
		handleAnswer(e, false);
	}

	public void handleResult(Element e) {
		handleAnswer(e, true);
	}

	private void handleAnswer(Element e, boolean result) {
		String id = e.getAttribute(Iq.ATT_ID);
		IQResultListener listener = null;
		synchronized (iqs) {
			listener = (IQResultListener) iqs.remove(id);
			// #mdebug
//			if (listener != null) {
//				System.out.println("IQM Removed: " + id);
//			}
			// #enddebug
		}
		if (listener != null) if (result) listener.handleResult(e);
		else
			listener.handleError(e);
		purge();
	}

	private void purge() {
		// XXX instead of purging it would be better to raise an error
		synchronized (iqs) {
			Enumeration en = iqs.keys();
			Vector keysToRemove = new Vector();
			while (en.hasMoreElements()) {
				Object key = (Object) en.nextElement();
				IQResultListener listener = (IQResultListener) iqs.get(key);
				if (listener.expireTime < System.currentTimeMillis()) keysToRemove
						.addElement(key);
			}
			en = keysToRemove.elements();
			while (en.hasMoreElements()) {
				Object key = en.nextElement();
				iqs.remove(key);
				// #mdebug
				System.out.println("IQM Purged: " + key);
				// #enddebug
			}
		}
	}

	/**
	 * Adds the registration.
	 * 
	 * @param iq the iq
	 * @param listener the listener
	 * @param duration the duration before expire in milliseconds
	 */
	public void addRegistration(Iq iq, IQResultListener listener, int duration) {
		synchronized (iqs) {
			if (duration < 0) duration = this.maxPermTime;
			listener.expireTime = System.currentTimeMillis() + duration;
			iqs.put(iq.getAttribute(Iq.ATT_ID), listener);
			// #mdebug
			//System.out.println("IQM Added: " + iq.getAttribute(Iq.ATT_ID));
			// #enddebug
		}
	}

}
