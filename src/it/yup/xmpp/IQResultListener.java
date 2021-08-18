/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: IQResultListener.java 2325 2010-11-15 20:07:28Z luca $
*/

package it.yup.xmpp;

import it.yup.xml.Element;
import it.yup.xmlstream.PacketListener;
import it.yup.xmpp.packets.Iq;
import it.yup.xmpp.packets.Stanza;

public abstract class IQResultListener implements PacketListener {

	public long expireTime=-1;

	public void packetReceived(Element e) {
		String type = e.getAttribute(Stanza.ATT_TYPE);
		if (Iq.T_RESULT.equals(type)) handleResult(e);
		else
			handleError(e);
	}

	public abstract void handleResult(Element e);

	public abstract void handleError(Element e);
}
