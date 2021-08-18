// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: XMPPTestClient.java 2329 2010-11-16 14:12:50Z luca $
*/

package it.yup.tests;

import java.util.Vector;
import it.yup.dispatch.EventDispatcher;
import it.yup.dispatch.EventListener;
import it.yup.dispatch.EventQuery;
import it.yup.transport.BaseChannel;
import it.yup.transport.MIDPSocketChannel;
import it.yup.xml.Element;
import it.yup.xmlstream.BasicXmlStream;
import it.yup.xmlstream.PacketListener;
import it.yup.xmlstream.SocketStream;
import it.yup.client.XMPPClient;
import it.yup.xmpp.packets.Message;
import it.yup.xmpp.packets.Presence;
import it.yup.xmpp.packets.Stanza;

public class XMPPTestClient {

	BasicXmlStream stream;
	BaseChannel channel;

	class Listener implements EventListener {

		public void gotStreamEvent(String event, Object source) {
			if (EventDispatcher.STREAM_INITIALIZED.equals(event)) {
				int[] bytes = XMPPClient.getTraffic();
				TestMidlet.yup.log.setText("online, bytes: " + bytes[0] + "/"
						+ bytes[1]);
				Presence p = new Presence();
				p.setShow(Presence.SHOW_DND);
				p.setStatus("Mobile test, don't send me messages!");
				stream.send(p, -1);
			}
		}
	}

	Listener listener = new Listener();

	class Echoer implements PacketListener {

		public void packetReceived(Element e) {
			Message m = new Message(e);
			//int[] bytes = XMPPClient.getTraffic();
			//TestMidlet.yup.log.setText("echoed " + m.getBody()+ ", bytes: "+ bytes[0] + "/" + bytes[1]);
			Message reply = new Message(m.getAttribute(Stanza.ATT_FROM), m
					.getAttribute("type"));
			reply.setBody(m.getBody());
			stream.send(reply, -1);
		}

	}

	public void startClient() {
		stream = new SocketStream(new Vector());
		channel = new MIDPSocketChannel("socket://jabber.bluendo.com:5222", stream);

		EventQuery qAuth = new EventQuery(EventQuery.ANY_EVENT, null, null);
		EventDispatcher.addEventListener(qAuth, listener);

		EventQuery qMessage = new EventQuery(Message.MESSAGE, null, null);
		BasicXmlStream.addPacketListener(qMessage, new Echoer());

		stream.initialize("test_ff@jabber.bluendo.com/pippa", "test_ff","en");
		channel.open();

	}
}
