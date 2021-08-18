/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: Message.java 2325 2010-11-15 20:07:28Z luca $
*/

package it.yup.xmpp.packets;

import it.yup.xml.Element;
import it.yup.xmpp.XmppConstants;

public class Message extends Stanza {

	public static final String MESSAGE = "message";
	public static final String BODY = "body";
	public static final String SUBJECT = "subject";
	public static final String THREAD = "thread";
	public static final String ERROR = "error";

	public static final String CHAT = "chat";
	public static final String GROUPCHAT = "groupchat";
	public static final String NORMAL = "normal";
	public static final String HEADLINE = "headline";

	public Message(String to, String type) {
		super(MESSAGE, to, type, null);
	}

	public void setBody(String body) {
		removeChild(NS_JABBER_CLIENT, BODY);
		addElementAndContent(NS_JABBER_CLIENT, BODY, body);
	}

	public String getBody() {
		Element el = (Element) this.getChildByName(NS_JABBER_CLIENT, BODY);
		if (el != null) { return el.getText(); }
		return null;
	}

	public String getErrorText() {
		Element el = getChildByName(NS_JABBER_CLIENT, ERROR);
		if (el == null) return null;

		Element txt = el.getChildByName("urn:ietf:params:xml:ns:xmpp-stanzas",
				XmppConstants.TEXT);
		if (txt == null) { return null; }

		return txt.getText();
	}

	public Message(Element e) {
		super(e);
	}

}
