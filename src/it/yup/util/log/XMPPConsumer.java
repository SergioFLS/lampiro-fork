/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: XMPPConsumer.java 1858 2009-10-16 22:42:29Z luca $
*/

package it.yup.util.log;

import java.util.Vector;

import it.yup.xml.Element;

/**
 * @author luca
 *
 */
public class XMPPConsumer extends Thread implements LogConsumer {

	public String debugJid = "helpMePlease@jabber.bluendo.com";

	private static XMPPConsumer consumer = null;

	private Vector messages = new Vector();
	private Vector outGoingMessages = new Vector();

	private boolean active = false;

	private XMPPLogger logger;

	public interface XMPPLogger {
		public boolean messageReady(Element e);
		/*
		 * if (xmppClient != null && xmppClient.my_jid != null) {
					try {
						xmppClient.sendPacket(e);
						outGoingMessages.removeElementAt(0);
					} catch (Exception e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				} else
					break;
		 * 
		 */
	}

	public static XMPPConsumer getConsumer(XMPPLogger logger) {
		if (consumer == null) {
			consumer = new XMPPConsumer();
		}
		consumer.logger = logger;
		return consumer;
	};

	/**
	 * 
	 */
	public XMPPConsumer() {
		active = true;
		start();
	}

	public void run() {

		while (active) {

			synchronized (messages) {
				try {
					messages.wait();
					while (messages.size() > 0) {
						String message = (String) messages.elementAt(0);
						// avoid sending XMPP traffic for infinite recursion
						// and useless date lake presencehandler
						if (message.startsWith("[SEND]")
								|| message.startsWith("[RECV]")
								|| message.startsWith("Sender: waiting")
								|| message.startsWith("PresenceHandler")) {
							;
						} else {
							Element msg = null;
							msg = new Element("jabber:client", "message");
							msg.setAttribute("to", debugJid);
							msg.setAttribute("type", "chat");
							msg.addElementAndContent(null, "body", message);
							outGoingMessages.addElement(msg);
						}
						messages.removeElementAt(0);
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			}
			while (outGoingMessages.size() > 0) {
				Element e = (Element) outGoingMessages.elementAt(0);
				if (logger.messageReady(e))
					outGoingMessages.removeElementAt(0);
			}
		}
	}

	/* (non-Javadoc)
	 * @see it.yup.util.LogConsumer#gotMessage(java.lang.String, int)
	 * 
	 */
	public void gotMessage(String message, int level) {
		synchronized (messages) {
			if (active) {
				messages.addElement(message);
				messages.notify();
			}
		}
	}

	/* (non-Javadoc)
	 * @see it.yup.util.LogConsumer#setExiting()
	 */
	public void setExiting() {
		this.active = false;

	}

}
