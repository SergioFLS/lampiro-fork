/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: XMPPClient.java 2325 2010-11-15 20:07:28Z luca $
 */

package it.yup.client;

import it.yup.dispatch.EventDispatcher;
import it.yup.dispatch.EventListener;
import it.yup.dispatch.EventQuery;
import it.yup.dispatch.EventQueryRegistration;
import it.yup.transport.BaseChannel;
import it.yup.transport.BaseSocketChannel;
import it.yup.transport.CountInputStream;
import it.yup.transport.CountOutputStream;



//#mdebug

import it.yup.util.log.Logger;

// #enddebug

import it.yup.util.Digests;
import it.yup.util.Utils;
import it.yup.xml.Element;
import it.yup.xmlstream.BasicXmlStream;

import it.yup.xmlstream.CompressionInitializer;

import it.yup.xmlstream.TLSInitializer;

import it.yup.xmlstream.PacketListener;
import it.yup.xmlstream.SocketStream;


import it.yup.xmpp.AccountRegistration;
import it.yup.xmpp.Contact;
import it.yup.xmpp.IQResultListener;
import it.yup.xmpp.IqManager;
import it.yup.xmpp.MUC;
import it.yup.xmpp.Roster;
import it.yup.xmpp.SimpleDataFormExecutor;
import it.yup.xmpp.SystemConfig;
import it.yup.xmpp.XmppConstants;
import it.yup.xmpp.XmppListener;
import it.yup.xmpp.Roster.PresenceSender;
import it.yup.xmpp.packets.DataForm;
import it.yup.xmpp.packets.Iq;
import it.yup.xmpp.packets.Message;
import it.yup.xmpp.packets.Presence;
import it.yup.xmpp.packets.Stanza;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimerTask;
import java.util.Vector;

import it.yup.transport.MIDPSocketChannel;
import org.bouncycastle.util.encoders.Base64;

//import it.yup.util.encoders.Base64;
//import it.yup.transport.SESocketChannel;

public class XMPPClient implements EventListener, PresenceSender {

	private final static int HANDLER_DATAFORM = 0;
	private final static int HANDLER_DISCO = 1;
	private final static int HANDLER_MESSAGE = 2;
	private final static int HANDLER_PRESENCE = 3;
	private final static int HANDLER_UUID = 4;
	private final static int HANDLER_VERSION = 5;
	
	private class XMPPPacketHandler implements PacketListener {

		public int handlerType;

		private boolean handleError = false;

		public XMPPPacketHandler(int handlerType, boolean handleError) {
			this.handlerType = handlerType;
			switch (handlerType) {
				case HANDLER_MESSAGE:
					this.handleError = handleError;
					break;

				default:
					break;
			}
		}

		public void packetReceived(Element e) {
			switch (handlerType) {
				case HANDLER_DATAFORM:
					dataformPacketReceived(e);
					break;

				case HANDLER_DISCO:
					discoPacketReceived(e);
					break;

				case HANDLER_MESSAGE:
					messagePacketReceived(e);
					break;

				case HANDLER_PRESENCE:
					presencePacketReceived(e);
					break;

				case HANDLER_UUID:
					uuidPacketReceived(e);
					break;
					
				case HANDLER_VERSION:
					versionPacketReceived(e);
					break;

				default:
					break;
			}
		}
		
		private void versionPacketReceived(Element e) {
			Iq reply = Iq.easyReply(e);
			Element query = reply.addElement(
					XmppConstants.NS_JABBER_VERSION, Iq.QUERY);
			query.addElementAndContent(null, "name", Config.CLIENT_NAME
					);
			query.addElementAndContent(null, XmppConstants.VERSION, cfg
					.getProperty(Config.VERSION));
			query.addElementAndContent(null, "os", "J2ME/"
					+ System.getProperty("microedition.platform"));
			XMPPClient.this.sendPacket(reply);
		}

		/**
		 * @param p
		 */
		private void dataformPacketReceived(Element p) {
			SimpleDataFormExecutor task = new SimpleDataFormExecutor(
					xmppListener, xmlStream, roster, p);
			roster.updateTask(task);
			if (xmppListener != null) xmppListener.handleTask(task);
			playSmartTone();
		}

		/**
		 * @param p
		 */
		private void discoPacketReceived(Element p) {
			Element q = p.getChildByName(XmppConstants.NS_IQ_DISCO_INFO,
					Iq.QUERY);
			Iq reply = new Iq(p.getAttribute(Stanza.ATT_FROM), Iq.T_RESULT);
			reply.setAttribute(Stanza.ATT_ID, p.getAttribute(Stanza.ATT_ID));
			String node = q.getAttribute("node");
			Element qr = reply.addElement(XmppConstants.NS_IQ_DISCO_INFO,
					Iq.QUERY);
			String capDisco = XmppConstants.NS_BLUENDO_CAPS + "#"
					+ XMPPClient.this.getCapVer();
			if (node == null || node.compareTo(capDisco) == 0) {
				Element identity = qr.addElement(
						XmppConstants.NS_IQ_DISCO_INFO, XmppConstants.IDENTITY);
				String lampiroName = Config.CLIENT_NAME
						+
						" " + cfg.getProperty(Config.VERSION) + " "
						+ XMPPClient.J2MEVER;
				identity.setAttributes(new String[] { XmppConstants.CATEGORY,
						"type", "name" }, new String[] { "client", "phone",
						lampiroName });
				identity
						.setAttribute(XmppConstants.XML_NS, "lang", Config.lang);
				for (int i = 0; i < features.length; i++) {
					Element feature = qr.addElement(
							XmppConstants.NS_IQ_DISCO_INFO,
							XmppConstants.FEATURE);
					feature.setAttribute("var", features[i]);
				}

			}
			//			else if (MIDP_PLATFORM.equals(node)) {
			//				qr.setAttribute("node", MIDP_PLATFORM);
			//				Element x = qr.addElement(JABBER_X_DATA, "x");
			//				x.setAttribute("type", Iq.T_RESULT);
			//				Element field = x.addElement(JABBER_X_DATA, "field");
			//				field.setAttribute("var", "microedition.platform");
			//				field.addElement(JABBER_X_DATA, "value").addText(
			//						System.getProperty("microedition.platform"));
			//			}
			if (node != null && node.compareTo(capDisco) == 0) {
				qr.setAttribute("node", capDisco);
			}
			sendPacket(reply);
		}

		/**
		 * @param p
		 */
		private void messagePacketReceived(Element p) {
			// different behaviours depending on type

			String type = p.getAttribute(Message.ATT_TYPE);
			if (type == null) type = Message.NORMAL;
			if (handleError == false && type.equals(Message.ERROR)) return;

			// e.g. normal are used to receive invite for MUC;
			// if the MUC user is created here it result in a normal Contact!!!
			// be careful for that 
			Element x = p.getChildByName(XmppConstants.NS_MUC_USER, "x");
			if (x != null && type.equals(Message.GROUPCHAT)) {
				Element invite = x.getChildByName(null, "invite");
				if (invite != null) return;
			}

			Message msg = new Message(p);
			// XXX: we will need to check the type
			String jid = msg.getAttribute(Stanza.ATT_FROM);
			// error packet sometimes do not have from
			if (jid == null) return;
			Contact u = roster.getContactByJid(jid);

			if (u == null) {
				Element group_elements[] = p.getChildrenByName(null, "group");
				String groups[] = new String[group_elements.length];
				for (int j = 0; j < groups.length; j++) {
					groups[j] = group_elements[j].getText();
				}
				u = new Contact(Contact.userhost(jid), p.getAttribute("name"),
						p.getAttribute(XmppConstants.SUBSCRIPTION), groups);
				roster.contacts.put(Contact.userhost(u.jid), u);
			}

			// at this manner only the first msg triggers an update
			u.addMessageToHistory(jid, msg);
			if (xmppListener != null) xmppListener.updateContact(u,
					Contact.CH_MESSAGE_NEW);
			playSmartTone();
		}

		/**
		 * @param e
		 */
		private void presencePacketReceived(Element e) {
			// #mdebug
			Logger.log("PresenceHandler: received packet: "
					+ new String(e.toXml()), Logger.DEBUG);
			// #enddebug

			lastPresenceTime = System.currentTimeMillis();
			String t = e.getAttribute(Stanza.ATT_TYPE);
			if (t == null || Presence.T_UNAVAILABLE.equals(t)) {
				Presence p = new Presence(e);

				String from = e.getAttribute(Stanza.ATT_FROM);
				Contact u = roster.getContactByJid(from);
				String type = e.getAttribute(Stanza.ATT_TYPE);
				if (u == null) {
					if (type != null
							&& type.compareTo(Presence.T_UNAVAILABLE) == 0) return;
					// XXX Guess the subscription
					u = new Contact(Contact.userhost(from), null,
							Contact.SUB_UNKNOWN, null);
					u.updatePresence(p);
					checkCapabilities(u, p);
					roster.contacts.put(u.jid, u);
				} else {
					u.updatePresence(p);
					checkCapabilities(u, p);
				}
				if (xmppListener != null) xmppListener.updateContact(u,
						Contact.CH_STATUS);
			} else if (Presence.T_SUBSCRIBE.equals(t)) {
				handleSubscribe(new Presence(e));
			} else if (Iq.T_ERROR.equals(t) && xmppListener != null) {
				xmppListener.handlePresenceError(new Presence(e));
			} else {
				// XXX At present ignore other cases, but when receiving
				// UNSUBCRIBED we should update the roster
			}
		}

		private void handleSubscribe(Presence p) {
			// try getting the contact (we may already have it)
			String jid = Contact.userhost(p.getAttribute(Stanza.ATT_FROM));
			Contact u = roster.getContactByJid(jid);
			if (u == null) {
				// we don't have the contact, create it
				u = new Contact(jid, null, null, null);
			}

			// subscription handling
			if (Contact.SUB_BOTH.equals(u.subscription)
					|| Contact.SUB_TO.equals(u.subscription)
					|| Config.CLIENT_AGENT.equals(Contact.userhost(jid))) {
				// subscribe received: if already granted, I don't ask anything
				Presence pmsg = new Presence();
				pmsg.setAttribute(Stanza.ATT_TO, u.jid);
				pmsg.setAttribute(Stanza.ATT_TYPE, Presence.T_SUBSCRIBED);
				sendPacket(pmsg);
			} else {
				// add a nick only if a previous name has been added
				Element nick = p.getChildByName(XmppConstants.NS_NICK, "nick");
				if (nick != null) {
					String nickNameText = nick.getText();
					if (nickNameText != null && nickNameText.length() > 0
							&& (u.name == null || u.name.length() == 0)) {
						u.name = nickNameText;
					}
				}

				Enumeration en = autoAcceptGateways.elements();
				while (en.hasMoreElements()) {
					String ithGateway = (String) en.nextElement();
					if (u.jid.indexOf(ithGateway) >= 0) {
						roster.subscribeContact(u, true);
						return;
					}
				}
				if (xmppListener != null) {
					xmppListener.askSubscription(u);
				}
			}
		}

		/**
		 * @param p
		 */
		private void uuidPacketReceived(Element p) {
			Iq reply = Iq.easyReply(p);
			Element e = reply.addElement(XmppConstants.NS_UUID,
					XmppConstants.UUID);
			String myUid = cfg.getProperty(Config.UUID, "-1");
			e.addText(myUid);
			sendPacket(reply);
		}
	}

	private Config cfg = Config.getInstance();

	/*
	 * The features published by Lampiro are ordered as specified here:
	 * http://tools.ietf.org/html/rfc4790#section-9.3
	 */
	private String[] features = new String[] { XmppConstants.MIDP_PLATFORM,
			XmppConstants.NS_CAPS, XmppConstants.NS_COMMANDS,
			XmppConstants.NS_IQ_DISCO_INFO, XmppConstants.NS_IBB,
			XmppConstants.NS_MUC, XmppConstants.NS_PUBSUB_EVENT,
			XmppConstants.NS_ROSTERX, XmppConstants.FILE_TRANSFER,
			XmppConstants.NS_JABBER_VERSION, XmppConstants.JABBER_X_DATA,
			XmppConstants.JINGLE, XmppConstants.JINGLE_FILE_TRANSFER,
			XmppConstants.JINGLE_IBB_TRANSPORT };

	public static final String J2MEVER = "J2ME/"
			+ System.getProperty("microedition.platform");

	/** the client instance */
	private static XMPPClient xmppInstance;

	// /** the authID value obtained during stream initialization */
	// public String _authID;

	private Roster roster;

	/** myself */
	private Contact me;

	/** my jabber id */
	public String my_jid;

	/** the used XmlStream */
	private BasicXmlStream xmlStream = null;

	/** The actual connection with the Server */
	private BaseChannel connection = null;

	/** true when the stream is valid */
	private boolean valid_stream = false;

	private EventQueryRegistration lostConnReg = null;


	// /** send the subscribe at most once per session */
	// private boolean lampiro_subscribe_sent = false;

	/*
	 * This task is used to retrieve asynchronously the roster
	 * after going online.
	 */
	private TimerTask rosterRetrieveTask = null;

	/*
	 * The time after rosterRetrieveTask is scheduled after receiving
	 * a presence
	 */
	private int rosterRetrieveTime = 5000;

	/*
	 * The time at which the last presence has been received
	 */
	private long lastPresenceTime;

	/*
	 * The number of sent bytes over the socket
	 */
	//public static int bytes_sent = 0;
	/*
	 * The number of received bytes over the socket
	 */
	//public static int bytes_received = 0;
	/*
	 * A flag used to enable or disable compression
	 */
	public boolean addCompression = false;

	/*
	 * A flag used to enable or disable TLS
	 */
	public boolean addTLS = false;

	/*
	 * the gateways whose contacts must be autoaccepted
	 * i.e. the gateways whose presence has been subscripted
	 * within the current session
	 */
	public Vector autoAcceptGateways = new Vector();

	private XmppListener xmppListener;

	/*
	 * Used to notify the XmppClient that the user jid and/or password
	 * are changed from last login
	 */
	private boolean newCredentials = false;

	/*
	 * The registration initializer
	 */
	private AccountRegistration accountRegistration;

	/*
	 * Contains some additional info about the server gateway config
	 */
	public Hashtable gatewayConfig = new Hashtable();

	/*
	 * A flag used to communicate if the client must go online when authenticated
	 */
	private boolean goOnline = false;

	public String myName = null;

	/**
	 * Get the total amount of traffic on the GPRS connection
	 * 
	 * @return an array with two elements: in / out traffic
	 */
	public static int[] getTraffic() {
		CountInputStream cis = BaseChannel.countInputStream;
		int readBytes = cis != null ? cis.getCount() : 0;
		CountOutputStream cos = BaseChannel.countOutputStream;
		int sentBytes = cos != null ? cos.getCount() : 0;
		return new int[] { readBytes, sentBytes };
	}

	private XMPPClient() {
		roster = new Roster(this);
	}

	/**
	 * Get the XMPP client (a singleton)
	 * 
	 * @return the unique instance of the client
	 * 
	 */
	public static XMPPClient getInstance() {
		if (xmppInstance == null) {
			xmppInstance = new XMPPClient();
		}
		return xmppInstance;
	}

	public void startClient() {

	}

	/** close the connection XXX i don't like this name */
	public void stopClient() {
		// saveToStorage();
	}

	//	/**
	//	 * Add a listener for XMPP packets
	//	 * 
	//	 * @param _q
	//	 *            the xpath like query
	//	 * @param _l
	//	 *            the listener
	//	 * @return The registration object to be used with
	//	 *         {@link #unregisterListener(EventQueryRegistration)} for removing
	//	 *         the listener
	//	 */
	//	public EventQueryRegistration registerListener(EventQuery _q, Object _l) {
	//		return BasicXmlStream.addEventListener(_q, _l);
	//	}
	//
	//	/**
	//	 * Add a one time listener for XMPP packets
	//	 * 
	//	 * @param _q
	//	 *            the xpath like query
	//	 * @param _l
	//	 *            the listener
	//	 * @return The registration object to be used with
	//	 *         {@link #unregisterListener(EventQueryRegistration)} for removing
	//	 *         the listener
	//	 * 
	//	 */
	//	public EventQueryRegistration registerOneTimeListener(EventQuery _q,
	//			Object _l) {
	//		return BasicXmlStream.addOnetimeEventListener(_q, _l);
	//	}
	//
	//	/**
	//	 * Remove a registered listener
	//	 * 
	//	 * @param reg
	//	 *            the registration obtained with
	//	 *            {@link #registerListener(EventQuery, PacketListener)}
	//	 * 
	//	 */
	//	public void unregisterListener(EventQueryRegistration reg) {
	//		BasicXmlStream.removeEventListener(reg);
	//	}

	/**
	 * Queue a packet into the send queue
	 * 
	 * @param pack
	 *            the packet to be sent
	 */
	public void sendPacket(Element pack) {
		xmlStream.send(pack, Config.TIMEOUT);
	}

	public Contact getMyContact() {
		return me;
	}

	/**
	 * Start the XML Stream using the current configuration
	 * 
	 * @param register
	 *            set true if the stream must create the account
	 * @param newCredentials
	 * @return
	 */
	public BasicXmlStream createStream(boolean register, boolean newCredentials) {
		this.newCredentials = newCredentials;
		// this must be done to clean the Roster after a reconnect
		this.roster.purge();
		buildSocketConnection();


		// connection = new SimpleBTConnector(
		// Config.HTTP_GW_HOST,
		// Config.HTTP_GW_PATH,
		// xmlStream
		// );

		// XXX useful with messages? I don't think so
		// eq = new EventQuery(Message.MESSAGE, null, null);
		// eq.child = new EventQuery(Iq.QUERY, new String[] { "xmlns" },
		// new String[] { "http://jabber.org/protocol/disco#items" } );
		// xmlStream.addEventListener(eq, adch);

		if (register) {

			EventQuery qReg = new EventQuery(
					EventDispatcher.STREAM_ACCOUNT_REGISTERED, null, null);
			/*
			 * The registration used to be notified of the registration
			 */
			EventDispatcher.addEventListener(qReg, this);

			accountRegistration = new AccountRegistration();
			xmlStream.addInitializer(accountRegistration, 0);
		}

		return xmlStream;
	}

	/**
	 * Build the low level connection based on plain sockets
	 */
	private void buildSocketConnection() {
		// XXX add initializers here
		Vector initializers = new Vector();
		// prepare the default initializers
		if (addTLS) {
			initializers.addElement(new TLSInitializer());
		}
		if (addCompression) {
			initializers.addElement(new CompressionInitializer());
		}
		/// XXX change the way xmlStream API is exposed
		setXmlStream(new SocketStream(initializers));
		
		// MIDP
		// #ifndef BT_PLAIN_SOCKET
				String connectionUrl = "socket://"
						+ cfg.getProperty(Config.CONNECTING_SERVER);
				connection = new MIDPSocketChannel(connectionUrl, xmlStream);
		// #endif
		// end MIDP
		
		// else
		// connection = new SESocketChannel("jabber.bluendo.com", 5222, xmlStream);
		// endif
		
		((BaseSocketChannel) connection).KEEP_ALIVE = Long.parseLong(cfg
				.getProperty(Config.KEEP_ALIVE));
	}


	public void openStream(boolean goOnline) {
		this.goOnline = goOnline;
		String resource = cfg.getProperty(Config.YUP_RESOURCE,
				Config.CLIENT_NAME);
		resource = Config.RESOURCE_PREFIX + resource;
		xmlStream.initialize(cfg.getProperty(Config.USER) + "@"
				+ cfg.getProperty(Config.SERVER) + "/" + resource, cfg
				.getProperty(Config.PASSWORD), Config.lang);

		EventQuery qAuth = new EventQuery(EventDispatcher.STREAM_INITIALIZED,
				null, null);
		/*
		 * The registration used to be notified of the authentication
		 */
		EventDispatcher.addEventListener(qAuth, this);


		if (!connection.isOpen()) {
			connection.open();
		}
	}

	public void closeStream() {
		if (connection.isOpen()) {
			connection.close();
		}
	}

	public void gotStreamEvent(String event, Object source) {
		if (EventDispatcher.STREAM_INITIALIZED.equals(event)) {

			// all these registration are made here 
			// Register the handler for incoming messages
			EventQuery eq = new EventQuery(Message.MESSAGE, null, null);
			eq.child = new EventQuery(Message.BODY, null, null);
			BasicXmlStream.addPacketListener(eq, new XMPPPacketHandler(
					HANDLER_MESSAGE, false));

			// a handler only for error type messages
			eq = new EventQuery(Message.MESSAGE,
					new String[] { Message.ATT_TYPE },
					new String[] { Message.ERROR });
			BasicXmlStream.addPacketListener(eq, new XMPPPacketHandler(
					HANDLER_MESSAGE, true));

			// Register the presence handler
			eq = new EventQuery(Presence.PRESENCE, null, null);
			BasicXmlStream.addPacketListener(eq, new XMPPPacketHandler(
					HANDLER_PRESENCE, false));

			// Register the disco handler
			eq = new EventQuery(Iq.IQ, new String[] { "type" },
					new String[] { "get" });
			eq.child = new EventQuery(Iq.QUERY, new String[] { "xmlns" },
					new String[] { XmppConstants.NS_IQ_DISCO_INFO });
			BasicXmlStream.addPacketListener(eq, new XMPPPacketHandler(
					HANDLER_DISCO, false));

			// Register the UUID handler
			eq = new EventQuery(Iq.IQ, new String[] { "type" },
					new String[] { "get" });
			eq.child = new EventQuery(XmppConstants.UUID,
					new String[] { "xmlns" },
					new String[] { XmppConstants.NS_UUID });
			BasicXmlStream.addPacketListener(eq, new XMPPPacketHandler(
					HANDLER_UUID, false));

			// Register the version handler
			eq = new EventQuery(Iq.IQ, new String[] { "type" },
					new String[] { "get" });
			eq.child = new EventQuery(Iq.QUERY, new String[] { "xmlns" },
					new String[] { XmppConstants.NS_JABBER_VERSION });
			BasicXmlStream.addPacketListener(eq, new XMPPPacketHandler(
					HANDLER_VERSION,false));
			
			// Register the handler for dataforms (both as <iq/> and <message/>)
			// payloads
			PacketListener dh = new XMPPPacketHandler(HANDLER_DATAFORM, false);
			eq = new EventQuery(Message.MESSAGE, null, null);
			eq.child = new EventQuery(DataForm.X, new String[] { "xmlns" },
					new String[] { DataForm.NAMESPACE });
			BasicXmlStream.addPacketListener(eq, dh);
			eq = new EventQuery(Iq.IQ, null, null);
			eq.child = new EventQuery(DataForm.X, new String[] { "xmlns" },
					new String[] { DataForm.NAMESPACE });
			BasicXmlStream.addPacketListener(eq, dh);

			IqManager iqm = IqManager.getInstance();
			iqm.setMaxPermTime(Config.MAX_PERM_TIME);
			iqm.streamInitialized();
			roster.streamInitialized(xmlStream);

			stream_authenticated();
		} else if (EventDispatcher.STREAM_ACCOUNT_REGISTERED.equals(event)) {
			xmlStream.removeInitializer(accountRegistration);
		}
	}

	public void stream_authenticated() {
		// create the self contact and setup the initial presence
		my_jid = xmlStream.jid;

		me = new Contact(Contact.userhost(my_jid), null, null, null);
		Presence p = new Presence();
		p.setAttribute(Stanza.ATT_FROM, my_jid);
		p.setAttribute(XmppConstants.XML_NS, "lang", Config.lang);
		p.toXml();
		String show = cfg.getProperty(Config.LAST_PRESENCE_SHOW);
		if (show != null && !"online".equals(show)) {
			p.setShow(show);
		}
		String msg = cfg.getProperty(Config.LAST_STATUS_MESSAGE);
		String tempPriority = cfg.getProperty(Config.LAST_PRIORITY, "0");
		p.setPriority(Integer.parseInt(tempPriority));
		p.setStatus(msg);
		// set capabilities
		String uri = XmppConstants.NS_CAPS;
		Element cap = p.addElement(uri, "c");
		cap.setAttribute("node", XmppConstants.NS_BLUENDO_CAPS);
		cap.setAttribute("hash", "sha-1");
		cap.setAttribute("ver", getCapVer());

		// XXX I don't like this, it could be better to send capabilities with a
		// different hash in the version
		//		Element x = p.addElement(JABBER_X_DATA, "x");
		//		x.setAttribute("type", Iq.T_RESULT);
		//		Element field = x.addElement(JABBER_X_DATA, "field");
		//		field.setAttribute("var", "FORM_TYPE");
		//		field.setAttribute("type", "hidden");
		//		field.addElement(JABBER_X_DATA, "value").addText(MIDP_PLATFORM);
		//
		//		field = x.addElement(JABBER_X_DATA, "field");
		//		field.setAttribute("var", "microedition.platform");
		//		field.addElement(JABBER_X_DATA, "value").addText(
		//				System.getProperty("microedition.platform"));

		me.updatePresence(p);

		// we are connected, set the stream as valid
		valid_stream=true;

		// Listen for lost connections
		lostConnReg = EventDispatcher.addEventListener(new EventQuery(
				EventDispatcher.STREAM_TERMINATED, null, null),
				new EventListener() {

					public void gotStreamEvent(String event, Object source) {
						if (rosterRetrieveTask != null) rosterRetrieveTask
								.cancel();
						valid_stream=false;
						closeStream();
						if (xmppListener != null) xmppListener.connectionLost();
						EventDispatcher.removeEventListener(lostConnReg);
					}
				});

		// if this is the first login it is better to ask the roster and go online suddenly
		// otherwise i only go online and the ask the roster after a while
		// however first try to load the roster from db
		roster.setupStore(Contact.userhost(my_jid));
		roster.loadGateways();
		roster.readFromStorage();
		roster.retrieveBookmarks();
		roster.setMyContact(me);

		checkTrustedServices();

		// if already have a UUID I can go online
		// otherwise i ask the UUID and then i will go 
		// online within the chckUUID function
		String myUid = cfg.getProperty(Config.UUID, "-1");
		if (myUid.equals("-1")) {
			checkUUID();
		} else if (goOnline) {
			askRoster();
		}
		askVcard();

		if (this.xmppListener != null) xmppListener.authenticated();
	}

	private void askVcard() {
		Iq iq = new Iq(Contact.userhost(my_jid), Iq.T_GET);
		iq.addElement(XmppConstants.VCARD_TEMP, XmppConstants.VCARD);
		iq.send(xmlStream, new XmppIqListener(XmppIqListener.ASK_VCARD));
	}

	/**
	 * 
	 */
	public void askRoster() {
		if (newCredentials || !"null".equals(this.roster.getRosterVersion())) {
			roster.retrieveRoster(true, false);
		} else {
			this.setPresence(-1, null);

			lastPresenceTime = System.currentTimeMillis();
			rosterRetrieveTask = new TimerTask() {
				public void run() {
					if (lastPresenceTime + rosterRetrieveTime < System
							.currentTimeMillis()) {
						roster.retrieveRoster(false, false);
						//roster.retrieveBookmarks();
						this.cancel();
					}
				}
			};
			Utils.tasks.schedule(rosterRetrieveTask, rosterRetrieveTime,
					rosterRetrieveTime);
		}
	}

	/**
	 * @param myUid
	 */
	private void checkUUID() {
		// if i don't have a UUID I ask the server for it
		// check the trusted services
		String myUid = cfg.getProperty(Config.UUID, "-1");
		if (myUid.equals("-1")) {
			Iq UUIDReq = new Iq(Config.UUID_SERVER, Iq.T_SET);
			Element command = UUIDReq.addElement(XmppConstants.NS_COMMANDS,
					XmppConstants.COMMAND);
			command.setAttribute("node", "uuid_gen");
			command.setAttribute(XmppConstants.ACTION, "execute");

			IQResultListener resultListener = new XmppIqListener(
					XmppIqListener.UUID);
			UUIDReq.send(xmlStream, resultListener, 240000);
		}
	}

	/**
	 * 
	 */
	private void checkTrustedServices() {
		// check the trusted services
		Iq systemConfigIq = new Iq(Config.CLIENT_SERVICES, Iq.T_GET);
		Element pubsub = systemConfigIq.addElement(XmppConstants.NS_PUBSUB,
				XmppConstants.PUBSUB);
		Element items = pubsub.addElement(null, XmppConstants.ITEMS);
		items.setAttribute(XmppConstants.NODE, Config.CLIENT_CONFIG);
		XmppIqListener resultListener = new XmppIqListener(
				XmppIqListener.TRUSTED_SERVICES);

		systemConfigIq.send(xmlStream, resultListener, 240000);
	}

	private String getCapVer() {
		Vector ss = new Vector();
		ss.addElement("client/");
		ss.addElement("phone/");
		ss.addElement(Config.lang + "/");
		ss.addElement(Config.CLIENT_NAME +
				" " + cfg.getProperty(Config.VERSION) + " " + J2MEVER + "<");
		for (int i = 0; i < features.length; i++) {
			ss.addElement(features[i]);
			ss.addElement("<");
		}
		Enumeration en = ss.elements();
		String S = "";
		while (en.hasMoreElements()) {
			S += en.nextElement();
		}
		S = new String(Base64.encode(Digests.digest(S, "sha1")));
		return S;
	}

	private class XmppIqListener extends IQResultListener {

		public static final int UUID = 0;
		public static final int TRUSTED_SERVICES = 1;
		public static final int ASK_VCARD = 2;
		public static final int CAPABILITIES = 3;
		public int LISTENER_TYPE;

		private String queryCapNode;
		private String queryCapVer;
		private Presence presence;
		private Contact contact;

		public XmppIqListener(int LISTENER_TYPE) {
			this.LISTENER_TYPE = LISTENER_TYPE;
		}

		public void handleError(Element e) {
			switch (LISTENER_TYPE) {
				case UUID:
					// i go online and ask roster after having the UUID
					// or receiving an error
					saveAndAskRoster(null);
					break;

				default:
					break;
			}
			// #mdebug
			System.out.println(e.toXml());
			// #enddebug
		}

		/**
		 * @param e
		 */
		private void setupTrustedServices(Element e) {
			Element config = e.getChildByName(XmppConstants.NS_PUBSUB,
					XmppConstants.PUBSUB);
			if (config != null) {
				Element[] items = config.getChildrenByNameAttrs(
						XmppConstants.NS_PUBSUB, XmppConstants.ITEMS,
						new String[] { XmppConstants.NODE },
						new String[] { Config.CLIENT_CONFIG });
				if (items.length > 0) {
					Element[] gateways = items[0].getChildrenByNameAttrs(null,
							XmppConstants.ITEM, new String[] { "id" },
							new String[] { "gateways" });
					if (gateways.length > 0) {
						gatewayConfig = SystemConfig.parse(gateways[0]);
						if (gatewayConfig != null) gatewayConfig = (Hashtable) gatewayConfig
								.get("gateways");
					}
				}
			}
		}

		/**
		 * @param myUid
		 */
		private void saveAndAskRoster(String myUid) {
			// with a wrong myUID i use a default one
			if (myUid == null) myUid = -1 + "";
			cfg.setProperty(Config.UUID, myUid);
			cfg.saveToStorage();
			// i go online and ask roster after having the UUID
			if (goOnline) {
				askRoster();
			}
		}

		public void handleResult(Element e) {
			switch (LISTENER_TYPE) {
				case TRUSTED_SERVICES:
					setupTrustedServices(e);
					break;

				case UUID:
					saveUUID(e);
					break;

				case ASK_VCARD:
					Element FN = e.getPath(
							new String[] { XmppConstants.VCARD_TEMP,
									XmppConstants.VCARD_TEMP }, new String[] {
									XmppConstants.VCARD, "FN" });
					if (FN != null && FN.getText().length() > 0) {
						myName = FN.getText();
					}
					break;

				case CAPABILITIES:
					setCapabilities(e);

				default:
					break;
			}
		}

		private void setCapabilities(Element e) {
			Element query = e.getChildByName(XmppConstants.NS_IQ_DISCO_INFO,
					Iq.QUERY);
			String fullNode = query.getAttribute("node");
			// some client forget to initialize this!!!
			if (fullNode == null) {
				if (queryCapVer != null && queryCapNode != null) fullNode = queryCapNode
						+ "#" + queryCapVer;
			}
			if (fullNode != null) {
				Vector fn = Utils.tokenize(fullNode, '#');
				String node = (String) fn.elementAt(0);
				String ver = (String) fn.elementAt(1);
				cfg.saveCapabilities(node, ver, query);
				contact.updatePresence(presence);
				checkCapabilities(contact, presence);
				if (xmppListener != null) xmppListener.updateContact(contact,
						Contact.CH_STATUS);
			}
		}

		/**
		 * @param e
		 */
		private void saveUUID(Element e) {
			Element x = e.getPath(new String[] { null, null }, new String[] {
					XmppConstants.COMMAND, "x" });
			String myUid = null;
			if (x != null) {
				Element[] fields = x.getChildrenByNameAttrs(null, "field",
						new String[] { "var" },
						new String[] { XmppConstants.UUID });
				if (fields.length > 0) {
					myUid = fields[0].getChildByName(null, "value").getText();

				}
			}
			saveAndAskRoster(myUid);
		}
	}

	public void playSmartTone() {
		if (this.xmppListener != null) {
			xmppListener.playSmartTone();
		}
	}

	/*
	 * Set the current priority of the client (store and send it).
	 * After setting the priority calls setpresence.
	 * 
	 * @param priority The priority to set
	 */
	public void setPresence(int availability, String status, int priority) {
		Presence p = me.getPresence(my_jid);
		p.setPriority(priority);
		this.setPresence(availability, status);
	}

	/**
	 * Set the current presence of the client (store and send it)
	 * 
	 * @param availability
	 * @param status
	 */
	public void setPresence(int availability, String status) {

		Presence p = me.getPresence(my_jid);
		if (p == null) return;
		Presence new_p = new Presence();

		// set my lang
		new_p.setAttribute(XmppConstants.XML_NS, "lang", Config.lang);
		new_p.setAttribute(Stanza.ATT_FROM, p.getAttribute(Stanza.ATT_FROM));

		if (availability >= 0) {
			if (Contact.AV_ONLINE == availability) {

			} else if (Contact.AV_UNAVAILABLE == availability) {
				new_p.setAttribute(Stanza.ATT_TYPE, Presence.T_UNAVAILABLE);
			} else {
				new_p.setShow(Contact.availability_mapping[availability]);
			}
		}

		if (status != null) {
			new_p.setStatus(status);
		} else {
			new_p.setStatus(p.getStatus());
		}

		//new_p.addElement(p.getChildByName(null, "x"));
		new_p.addElement(p.getChildByName(null, "c"));
		new_p.setPriority(p.getPriority());
		me.updatePresence(new_p);
		sendPacket(new_p);
		if (Presence.T_UNAVAILABLE.equals(new_p.getAttribute(Stanza.ATT_TYPE))) {
			closeStream();
		}
	}

	/**
	 * Handle an incoming command list
	 * 
	 * @param e
	 *            the received element with commands
	 * @param show
	 *            when true show the command list screen
	 * @param optionalArguments
	 */
	public void handleClientCommands(Element e) {
		String from = e.getAttribute(Stanza.ATT_FROM);
		if (from == null) return;
		Contact c = roster.getContactByJid(from);
		if (c == null) return;
		Element q = e.getChildByName(XmppConstants.NS_IQ_DISCO_ITEMS, Iq.QUERY);
		if (q != null) {
			Element items[] = q.getChildrenByName(
					XmppConstants.NS_IQ_DISCO_ITEMS, XmppConstants.ITEM);
			c.cmdlist = new String[items.length][2];
			for (int i = 0; i < items.length; i++) {
				c.cmdlist[i][0] = items[i].getAttribute(XmppConstants.NODE);
				c.cmdlist[i][1] = items[i].getAttribute("name");
			}
		} // XXX we could add an alert if it's empty and we have to show
	}

	/**
	 * Show an error screen, if multiple errors occur only append the message
	 * 
	 * @param type
	 * @param title
	 *            Title of the screen
	 * @param text
	 *            Displayed error message
	 * @param next_screen
	 *            the screen where we have to return to
	 */
	//	public void showAlert(AlertType type, String title, String text,
	//			final Object next_screen) {
	//		if (xmppListener != null) {
	//			xmppListener.showAlert(type, title, text, next_screen);
	//		}
	//	}
	public void showAlert(int type, int titleCode, int textCode,
			String additionalText) {
		if (xmppListener != null) {
			xmppListener.showAlert(type, titleCode, textCode, additionalText);
		}
	}

	public Roster getRoster() {
		return roster;
	}

	public void setXmppListener(XmppListener xmppListener) {
		this.xmppListener = xmppListener;
		roster.setXmppListener(xmppListener);
	}

	public XmppListener getXmppListener() {
		return xmppListener;
	}

	public void setMUCGroup(String str) {
		MUC.GROUP_CHATS = str;
	}

	public Element getCapabilities(Presence p) {
		if (p == null) return null;
		Element c = p.getChildByName(XmppConstants.NS_CAPS, "c");
		if (c == null) { return null; }
		String node = c.getAttribute("node");
		String ver = c.getAttribute("ver");
		return cfg.getCapabilities(node, ver);
	}

	/**
	 * Ask new capabilities if missing otherwise set the correct capabilities for the contact
	 * 
	 * @param contact the contact
	 * @param p the p
	 */
	public void checkCapabilities(Contact contact, Presence p) {
		// check the capabilities
		// pass them to the roster that checks the db 
		String queryCapNode = null;
		String queryCapVer = null;

		Element capNode = p.getChildByName(null, "c");
		if (capNode != null) {
			queryCapNode = capNode.getAttribute("node");
			queryCapVer = capNode.getAttribute("ver");
			Element cap = cfg.getCapabilities(queryCapNode, queryCapVer);
			if (cap != null) {
				Element identity = cap.getChildByName(null,
						XmppConstants.IDENTITY);
				if (identity != null) {
					String type = identity.getAttribute("type");
					if (type != null && type.indexOf("phone") >= 0) {
						p.pType = Presence.PHONE;
					}
				}
			} else {
				Element c = null;
				if (p != null) {
					c = p.getChildByName(XmppConstants.NS_CAPS, "c");
				}
				Iq iq = new Iq(p.getAttribute(Message.ATT_FROM), Iq.T_GET);
				Element query = iq.addElement(XmppConstants.NS_IQ_DISCO_INFO,
						Iq.QUERY);
				if (c != null) {
					query.setAttribute("node", c.getAttribute("node") + "#"
							+ c.getAttribute("ver"));
				}
				XmppIqListener xil = new XmppIqListener(
						XmppIqListener.CAPABILITIES);
				xil.queryCapNode = queryCapNode;
				xil.queryCapVer = queryCapVer;
				xil.presence = p;
				xil.contact = contact;
				iq.send(xmlStream, xil);
			}
		}
	}

	/**
	 * @param xmlStream the xmlStream to set
	 */
	public void setXmlStream(BasicXmlStream xmlStream) {
		this.xmlStream = xmlStream;
	}

	/**
	 * @return the xmlStream
	 */
	public BasicXmlStream getXmlStream() {
		return xmlStream;
	}

	public void sendPresence() {
		this.setPresence(-1, null);
	}

	/**
	 * @return the valid_stream
	 */
	public boolean isValid_stream() {
		return valid_stream;
	}
}
