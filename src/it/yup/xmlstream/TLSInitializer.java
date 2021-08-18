/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: TLSInitializer.java 2325 2010-11-15 20:07:28Z luca $
*/

package it.yup.xmlstream;

import it.yup.dispatch.EventDispatcher;
import it.yup.dispatch.EventQuery;
import it.yup.xml.Element;
import java.io.IOException;

public class TLSInitializer extends Initializer implements PacketListener {

	BasicXmlStream stream;

	public TLSInitializer() {
		super("urn:ietf:params:xml:ns:xmpp-tls", true);
	}

	public void start(BasicXmlStream stream) {

		this.stream = stream;

		Element starttls = new Element(this.namespace, "starttls");
		EventQuery pq = new EventQuery(EventQuery.ANY_PACKET, null, null);
		BasicXmlStream.addOnetimePacketListener(pq, this);
		stream.send(starttls, -1);
	}

	public void packetReceived(Element e) {
		if ("proceed".equals(e.name)) {
			try {
				((SocketStream) stream).startTLS();
				EventDispatcher.dispatchEvent(EventDispatcher.TLS_INITIALIZED,
						null);
			} catch (IOException e1) {
				// notify error
				e1.printStackTrace();
			}
			stream.restart();
		} else {
			stream.nextInitializer();
		}
	}

}
