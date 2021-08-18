/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: AccountRegistration.java 2218 2010-10-04 09:37:39Z luca $
*/

package it.yup.xmpp;

import it.yup.dispatch.EventDispatcher;
import it.yup.dispatch.EventQuery;
import it.yup.xml.Element;
import it.yup.xmlstream.BasicXmlStream;
import it.yup.xmlstream.Initializer;
import it.yup.xmlstream.PacketListener;
import it.yup.xmpp.packets.Iq;
import it.yup.xmpp.packets.Stanza;

/**
 * Class registering an account with a XMPP server <br/>
 * The registration process is simplified, since we don't gather the required fields,
 * and we directly send the chosen username and password. This may lead to
 * compatibility issues with some servers, however we don't care at present.
 *
 */
public class AccountRegistration extends Initializer implements PacketListener {
	
	private static final String IQ_REGISTER =  "jabber:iq:register";
	
	public AccountRegistration() {
		// when used the registration is mandatory, if it fails
		// we cannot go on
		super("http://jabber.org/features/iq-register", false);
	}
	
	public void start(BasicXmlStream xmlStream) {
		this.stream = xmlStream;
		Iq iq_register = new Iq(null, Iq.T_SET);
        Element query = iq_register.addElement(IQ_REGISTER, Iq.QUERY);
        query.addElement(IQ_REGISTER, "username").addText(Contact.user(stream.jid));
        query.addElement(IQ_REGISTER, "password").addText(stream.password);
        EventQuery registerResult = new EventQuery("iq", 
        		new String[]{ Stanza.ATT_ID }, 
        		new String[]{ iq_register.getAttribute(Stanza.ATT_ID) });
        BasicXmlStream.addOnetimePacketListener(registerResult, this);
        stream.send(iq_register, -1);
	}

	public void packetReceived(Element e) {
		if(e.getAttribute("type").equals(Iq.T_RESULT)) {
			EventDispatcher.dispatchEvent(EventDispatcher.STREAM_ACCOUNT_REGISTERED, null);
			stream.nextInitializer();
		} else {
			String errmsg = "Error creating account";
			Element err = e.getChildByName(null, Iq.T_ERROR);
			if(err != null) {
				Element[] children = err.getChildren();
				for(int i = 0; i < children.length; i++) {
					Element cld = children[i];
					errmsg = cld.name;
				}
			}
			EventDispatcher.dispatchEvent(EventDispatcher.STREAM_ERROR, errmsg);
		}
	}

}
