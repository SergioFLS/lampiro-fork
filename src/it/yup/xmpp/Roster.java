/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: Roster.java 2432 2011-01-30 15:26:48Z luca $
*/

package it.yup.xmpp;

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

import it.yup.xml.BProcessor;
import it.yup.xml.Element;
import it.yup.xmlstream.BasicXmlStream;
import it.yup.xmlstream.PacketListener;
import it.yup.xmpp.XmppListener;
import it.yup.xmpp.packets.DataForm;
import it.yup.xmpp.packets.Iq;
import it.yup.xmpp.packets.Message;
import it.yup.xmpp.packets.Presence;
import it.yup.xmpp.packets.Stanza;
import it.yup.dispatch.EventQuery;
import it.yup.util.Utils;
import it.yup.util.storage.KeyStore;
import it.yup.util.storage.KeyStoreFactory;
import it.yup.util.Alerts;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class Roster implements PacketListener {

	public interface PresenceSender {
		public void sendPresence();
	}

	class DistrRoster {

		private String jid;

		public DistrRoster(String jid) {
			this.jid = jid;
			distrRosters.put(jid, this);
		}

		public void retrieveRoster() {
			Iq iq_roster = new Iq(jid, Iq.T_GET);
			Element query = iq_roster.addElement(XmppConstants.NS_IQ_ROSTER,
					Iq.QUERY);
			//			if (client.xmlStream.hasFeature(XMPPClient.NS_ROSTERVER)) {
			//				query.setAttribute("ver", this.rosterVersion);
			//			}
			RosterIqListener rosterListener = new RosterIqListener(
					RosterIqListener.DISTR_ROSTER);
			// after receiving roster I need to go online for this roster
			iq_roster.send(getXmlStream(), rosterListener, 240000);
		}

	}

	private class RosterIqListener extends IQResultListener {

		public static final int BOOKMARK = 0;
		public static final int ROSTER = 1;
		public static final int SUBSCRIBE = 2;
		public static final int UPDATE_GATEWAY = 3;
		public static final int DISTR_ROSTER = 4;
		private int LISTENER_TYPE = 0;
		public boolean go_online;
		public boolean accept;
		public Contact c;

		public RosterIqListener(int LISTENER_TYPE) {
			this.LISTENER_TYPE = LISTENER_TYPE;
		}

		public void handleError(Element e) {
			switch (LISTENER_TYPE) {
				case BOOKMARK:
					serverStorage = false;
					break;

				case ROSTER:
					setupRoster(e);
					break;

				default:
					break;
			}
			// #mdebug
						System.out.println(e.toXml());
			// #enddebug
		}

		public void handleResult(Element e) {
			switch (LISTENER_TYPE) {
				case BOOKMARK:
					setupBookmark(e);
					break;
				case ROSTER:
					setupRoster(e);
					break;
				case SUBSCRIBE:
					handleSubscribe();
					break;
				case UPDATE_GATEWAY:
					updateGateway(e);
					break;
				case DISTR_ROSTER:
					recreateRoster(e, false);
					saveToStorage();
					presenceToGateway(e.getAttribute(Stanza.ATT_FROM), true);
					break;
				default:
					break;
			}

		}

		/**
		 * @param e
		 */
		private void updateGateway(Element e) {
			String from = e.getAttribute(Stanza.ATT_FROM);

			Element identity = e.getPath(new String[] {
					XmppConstants.NS_IQ_DISCO_INFO,
					XmppConstants.NS_IQ_DISCO_INFO }, new String[] { Iq.QUERY,
					XmppConstants.IDENTITY });
			if (identity != null) {
				String category = identity.getAttribute(XmppConstants.CATEGORY);
				if (category.compareTo("gateway") == 0) {
					addGateway(from, e);
					saveGateways();
					// to notify the RosterScreen of the gateway presence 
					if (getXmppListener() != null) getXmppListener()
							.updateContact(c, Contact.CH_STATUS);
				}
			}
		}

		/**
		 * 
		 */
		private void handleSubscribe() {
			Presence psub;
			if (accept) {
				psub = new Presence(Presence.T_SUBSCRIBED, null, null, -1);
				psub.setAttribute(Stanza.ATT_TO, c.jid);
				getXmlStream().send(psub);
			}
			psub = new Presence(Presence.T_SUBSCRIBE, null, null, -1);
			psub.setAttribute(Stanza.ATT_TO, c.jid);
			getXmlStream().send(psub);
		}

		/**
		 * @param e
		 */
		private void setupRoster(Element e) {
			recreateRoster(e, true);
			saveToStorage();
			if (go_online && presenceSender != null) {
				presenceSender.sendPresence();
			}
			if (getXmppListener() != null) getXmppListener().rosterRetrieved();
		}

		/**
		 * @param e
		 */
		private void setupBookmark(Element e) {
			serverStorage = true;
			Element storage = e.getPath(new String[] { null,
					XmppConstants.NS_BOOKMARKS }, new String[] { Iq.QUERY,
					XmppConstants.STORAGE });
			if (storage != null) {
				privateStorage.removeChild(XmppConstants.NS_BOOKMARKS,
						XmppConstants.STORAGE);
				privateStorage.addElement(storage);
			}

			Element exStorage = e.getPath(new String[] { null,
					XmppConstants.NS_STORAGE_LAMPIRO }, new String[] {
					Iq.QUERY, XmppConstants.STORAGE });
			if (exStorage != null) {
				privateStorage.removeChild(XmppConstants.NS_STORAGE_LAMPIRO,
						XmppConstants.STORAGE);
				privateStorage.addElement(exStorage);
			}

			Element[] conferences = storage.getChildrenByName(null,
					XmppConstants.CONFERENCE);
			for (int i = 0; i < conferences.length; i++) {
				Element el = conferences[i];
				String jid = el.getAttribute("jid");
				Element nickEl = el.getChildByName(null, "nick");
				Element pwdEl = el.getChildByName(null, "password");
				String nick = nickEl != null ? nickEl.getText() : null;
				String pwd = pwdEl != null ? pwdEl.getText() : null;
				String autoJoinString = el.getAttribute(MUC.AUTO_JOIN);
				boolean autoJoin = false;
				if (autoJoinString != null && autoJoinString.equals("true")) autoJoin = true;

				Element[] extEl = exStorage.getChildrenByNameAttrs(null,
						XmppConstants.CONFERENCE, new String[] { "jid" },
						new String[] { jid });

				boolean lampiroAutoJoin = autoJoin;
				if (extEl.length > 0) {
					if (extEl[0].getAttribute(MUC.LAMPIRO_AUTO_JOIN).equals(
							"false")) {
						lampiroAutoJoin = false;
					} else {
						lampiroAutoJoin = true;
					}
				}

				Contact c = getContactByJid(Contact.user(jid));
				if (c != null && c instanceof MUC == false) {
					contacts.remove(c.jid);
					if (getXmppListener() != null) getXmppListener()
							.removeContact(c, false);
				}
				MUC muc = createMuc(jid, Contact.user(jid), nick, pwd,
						lampiroAutoJoin);
				if (getXmppListener() != null) getXmppListener().updateContact(
						muc, Contact.CH_STATUS);
			}
		}
	}

	/*
	 *	Implements the XEP for roster push 
	 */
	class RosterX implements PacketListener {
		public RosterX() {
			EventQuery q = new EventQuery(Message.MESSAGE, null, null);
			EventQuery x = new EventQuery("x", new String[] { "xmlns" },
					new String[] { XmppConstants.NS_ROSTERX });
			q.child = x;
			BasicXmlStream.addPacketListener(q, this);

			q = new EventQuery("iq", new String[] { Iq.ATT_TYPE },
					new String[] { Iq.T_SET });
			q.child = x;
			BasicXmlStream.addPacketListener(q, this);
		}

		public void packetReceived(Element e) {
			//System.out.println(new String(e.toXml()));
			// check the packet sender
			// check what to do with contacts 
			// answer in case it is an Iq
			if (getXmppListener() != null) getXmppListener()
					.rosterXsubscription(e);
		}
	}

	/*
	 * The roster version
	 */
	private String rosterVersion = "0";

	/** All contacts */
	public Hashtable contacts = new Hashtable();

	public boolean serverStorage = false;
	/** All contacts */
	private Element privateStorage = null;

	private XmppListener xmppListener;

	private RosterX rosterX;

	public Hashtable distrRosters = new Hashtable();

	public static String unGroupedCode = new String(
			new char[] { ((char) 0x08) });

	public Hashtable registeredGateways = new Hashtable(5);

	private BasicXmlStream xmlStream;

	private PresenceSender presenceSender;

	public Roster(PresenceSender presenceSender) {
		this.presenceSender = presenceSender;
		privateStorage = new Element(XmppConstants.NS_PRIVATE, Iq.QUERY);
		privateStorage.addElement(new Element(XmppConstants.NS_BOOKMARKS,
				XmppConstants.STORAGE));
		privateStorage.addElement(new Element(XmppConstants.NS_STORAGE_LAMPIRO,
				XmppConstants.STORAGE));
	}

	public void presenceToGateway(String to, boolean online) {
		Element pres = null;
		if (online) {
			pres = myContact.getPresence(null).clone();
		} else {
			pres = new Presence();
			pres.setAttribute(Presence.ATT_TYPE, Presence.T_UNAVAILABLE);
		}
		pres.setAttribute(Presence.ATT_TO, to);
		xmlStream.send(pres);
	}

	public void streamInitialized(BasicXmlStream xmlStream) {
		this.xmlStream = xmlStream;
		EventQuery eq = new EventQuery(Iq.IQ, new String[] { "type" },
				new String[] { "set" });
		eq.child = new EventQuery(Iq.QUERY, new String[] { "xmlns" },
				new String[] { XmppConstants.NS_IQ_ROSTER });
		BasicXmlStream.addPacketListener(eq, this);

		// roster new listener
		eq = new EventQuery(Iq.IQ, new String[] { "type" },
				new String[] { "set" });
		eq.child = new EventQuery(Iq.QUERY, new String[] { "xmlns" },
				new String[] { XmppConstants.NS_XMPP_SUBSCRIPTIONS });
		final BasicXmlStream tmpStream = xmlStream;
		BasicXmlStream.addPacketListener(eq, new PacketListener() {

			public void packetReceived(Element e) {
				Element query = e.getChildByName(null, Iq.QUERY);
				// check all possible child
				Element subscribed = query.getChildByName(null,
						Presence.T_SUBSCRIBED);
				Element subscribe = query.getChildByName(null,
						Presence.T_SUBSCRIBE);
				if (subscribe != null || subscribed != null) {
					Element subPacket = null;
					boolean complete;
					if (subscribe != null) {
						subPacket = subscribe;
						complete = false;
					} else {
						subPacket = subscribed;
						complete = true;
					}
					Element item = new Element(XmppConstants.NS_IQ_ROSTER,
							XmppConstants.ITEM);
					item.setAttribute(XmppConstants.SUBSCRIPTION,
							complete ? Contact.SUB_BOTH : Iq.ATT_FROM);
					Element nickEl = subPacket.getChildByName(null,
							XmppConstants.NICK);
					if (nickEl != null) item.setAttribute("name", nickEl
							.getText());
					String jid = subPacket.getAttribute("jid");
					item.setAttribute("jid", jid);
					Element[] groups = subPacket.getChildrenByName(null,
							"group");
					for (int i = 0; i < groups.length; i++) {
						Element element = groups[i];
						item.addElement(element);
					}
					updateRosterItem(item);
					Contact c = getContactByJid(jid);
					if (!complete) xmppListener.askSubscription(c);
				}
				Element unsubscribe = query.getChildByName(null,
						Presence.T_UNSUBSCRIBE);
				if (unsubscribe != null) {
					Element item = new Element(XmppConstants.NS_IQ_ROSTER,
							XmppConstants.ITEM);
					item.setAttribute(XmppConstants.SUBSCRIPTION,
							Contact.SUB_NONE);
					String jid = unsubscribe.getAttribute("jid");
					item.setAttribute("jid", jid);
					updateRosterItem(item);
				}
				Element unsubscribed = query.getChildByName(null,
						Presence.T_UNSUBSCRIBED);
				if (unsubscribed != null) {

				}
				Element reply = Iq.easyReply(e);
				tmpStream.send(reply);
			}

		});

		rosterX = new RosterX();
	}

	KeyStore rosterStore;

	private Contact myContact;

	/**
	 * Read the contacts from the RMS
	 * 
	 */
	public synchronized void readFromStorage() {
		try {
			rosterStore.open();
			byte[] rosterData = rosterStore.load(Utils
					.getBytesUtf8(XmppConstants.ROSTER));
			if (rosterData != null) {
				Element rosterEl = BProcessor.parse(rosterData);
				setRosterVersion(rosterEl.getAttribute("ver"));
				if (getRosterVersion() == null) setRosterVersion("null");
				Element[] children = rosterEl.getChildrenByName(null, "group");
				for (int i = 0; i < children.length; i++) {
					Element ithChild = children[i];
					String gName = ithChild.getText();
					try {
						byte[] gData = rosterStore.load(Utils
								.getBytesUtf8(gName));
						if (gData != null) {
							Element gEl = BProcessor.parse(gData);
							Element[] gChildren = gEl.getChildrenByName(null,
									"item");
							for (int j = 0; j < gChildren.length; j++) {
								Element item = gChildren[j];
								this.updateRosterItem(item);
							}
						}
					} catch (OutOfMemoryError e) {
						// #mdebug
									Logger.log("Error in reading from storage: " + e.getMessage(),
											Logger.DEBUG);
						// #enddebug
					}
				}
			}
		} catch (Exception e) {
			// #mdebug
						Logger.log("Error in reading from storage: " + e.getMessage(),
								Logger.DEBUG);
			// #enddebug
			xmppListener.showAlert(Alerts.ERROR, XmppConstants.ALERT_DATA,
					XmppConstants.ALERT_DATA, e.getClass().toString());
		} finally {
			rosterStore.close();
		}
		// #mdebug
				Logger.log("Finish read from storage:" + System.currentTimeMillis());
		// #enddebug
	}

	/**
	 * Save the roster to the RMS
	 * 
	 */
	protected synchronized void saveToStorage() {
		try {
			rosterStore.open();
			Element rosterEl = new Element(XmppConstants.NS_IQ_ROSTER,
					XmppConstants.ROSTER);
			if (getRosterVersion() == null) setRosterVersion("null");
			rosterEl.setAttribute("ver", this.getRosterVersion());
			Hashtable groups = Group.getGroups();
			Enumeration en = groups.elements();
			while (en.hasMoreElements()) {
				try {
					Group g = (Group) en.nextElement();
					byte[] groupData = BProcessor.toBinary(g.store(this));
					rosterStore.store(Utils.getBytesUtf8(g.name), groupData);
					Element groupEl = new Element(XmppConstants.NS_IQ_ROSTER,
							"group");
					groupEl.addText(g.name);
					rosterEl.addElement(groupEl);
				} catch (OutOfMemoryError e) {
					// #mdebug
								Logger.log("Error in saving to storage: " + e.getMessage(),
										Logger.DEBUG);
					// #enddebug
				}
			}
			rosterStore.store(Utils.getBytesUtf8(XmppConstants.ROSTER),
					BProcessor.toBinary(rosterEl));
		} catch (Exception e) {
			// #mdebug
						Logger.log("Error in saving to storage: " + e.getMessage(),
								Logger.DEBUG);
			// #enddebug
			xmppListener.showAlert(Alerts.ERROR, XmppConstants.ALERT_DATA,
					XmppConstants.ALERT_DATA, e.getClass().toString());
		} finally {
			rosterStore.close();
		}
	}

	public void packetReceived(Element e) {
		// #mdebug
				Logger.log("RosterHandler: received packet: " + new String(e.toXml()),
						Logger.DEBUG);
		// #enddebug

		Element query = e.getChildByName(null, Iq.QUERY);
		Element items[] = query.getChildrenByName(null, "item");
		for (int i = 0; i < items.length; i++) {
			updateRosterItem(items[i]);
		}
		String tempVer = query.getAttribute("ver");
		if (tempVer != null) this.setRosterVersion(tempVer);
		saveToStorage();

		Element reply = Iq.easyReply(e);
		xmlStream.send(reply);
	}

	/**
	 * Send a roster query
	 * 
	 * @param go_online
	 *            if true we go online when received the roster
	 */
	public void retrieveRoster(final boolean go_online, boolean purge) {
		//ask the roster and after the bookmarks
		Iq iq_roster = new Iq(null, Iq.T_GET);
		Element query = iq_roster.addElement(XmppConstants.NS_IQ_ROSTER,
				Iq.QUERY);
		if (xmlStream.hasFeature(XmppConstants.NS_ROSTERVER)) {
			query.setAttribute("ver", this.getRosterVersion());
		}
		RosterIqListener rosterListener = new RosterIqListener(
				RosterIqListener.ROSTER);
		rosterListener.go_online = go_online;
		iq_roster.send(xmlStream, rosterListener, 240000);
		Enumeration en = this.distrRosters.elements();
		while (en.hasMoreElements()) {
			DistrRoster distr = (DistrRoster) en.nextElement();
			distr.retrieveRoster();
		}
	}

	public void retrieveBookmarks() {
		Element query;
		Iq iq_bookMarks = new Iq(null, Iq.T_GET);
		query = iq_bookMarks.addElement(XmppConstants.NS_PRIVATE, Iq.QUERY);
		query.addElement(XmppConstants.NS_BOOKMARKS, XmppConstants.STORAGE);
		query.addElement(XmppConstants.NS_STORAGE_LAMPIRO,
				XmppConstants.STORAGE);
		IQResultListener bookmarkListener = new RosterIqListener(
				RosterIqListener.BOOKMARK);
		iq_bookMarks.send(xmlStream, bookmarkListener, 240000);
	}

	/**
	 * Subscribe to a contact. Adding a contact fires the transmission of two
	 * messages: an iq of type set for updating the roster, and a presence of
	 * type subscribe
	 * 
	 * @param c
	 *            : the contact to be subscribed
	 * @param accept
	 *            : true if this is a response to a subscribe request
	 */
	public void subscribeContact(Contact c, boolean accept) {
		// i check by means of the domain
		// if it is a normal contact or from a gateway supporting roster new
		String domain = Contact.domain(c.jid);
		if (domain == null || distrRosters.containsKey(domain) == false) {
			contacts.put(c.jid, c);
			Iq iq_roster = new Iq(null, Iq.T_SET);
			Element query = iq_roster.addElement(XmppConstants.NS_IQ_ROSTER,
					Iq.QUERY);
			Element item = query.addElement(XmppConstants.NS_IQ_ROSTER, "item");
			item.setAttribute("jid", c.jid);
			if (c.name.length() > 0) {
				item.setAttribute("name", c.name);
			}
			for (int i = 0; i < c.getGroups().length; i++) {
				item.addElement(XmppConstants.NS_IQ_ROSTER, "group").addText(
						c.getGroups()[i]);
			}
			if (c.getGroups().length == 0) this.addGatewayGroup(c, item);

			RosterIqListener subscribeListener = new RosterIqListener(
					RosterIqListener.SUBSCRIBE);
			subscribeListener.accept = accept;
			subscribeListener.c = c;
			iq_roster.send(xmlStream, subscribeListener);
			// recreateGroups();
		} else {
			String sub = c.subscription;
			Iq iq = new Iq(domain, Iq.T_SET);
			Element query = iq.addElement(XmppConstants.NS_XMPP_SUBSCRIPTIONS,
					Iq.QUERY);
			Element subscribe = query.addElement(null, Presence.T_SUBSCRIBE);
			if (Iq.ATT_FROM.equals(sub)) {
				c.subscription = "both";
				subscribe.name = Presence.T_SUBSCRIBED;
			} else if (c.name.length() > 0) {
				subscribe.addElement("nickname", XmppConstants.NICK).addText(
						c.name);
			}
			for (int i = 0; i < c.getGroups().length; i++) {
				Element g = new Element(XmppConstants.NS_XMPP_SUBSCRIPTIONS,
						"group");
				g.addText(c.getGroups()[i]);
				subscribe.addElement(g);
			}
			subscribe.setAttribute("jid", c.jid);
			iq.send(xmlStream, null);
		}
	}

	private void addGatewayGroup(Contact c, Element item) {
		Enumeration en = registeredGateways.keys();
		while (en.hasMoreElements()) {
			String from = (String) en.nextElement();
			String domain = Contact.domain(c.jid);
			if (from.equals(domain)) {
				Element elGroup = (Element) registeredGateways.get(from);
				Element identity = elGroup.getPath(new String[] {
						XmppConstants.NS_IQ_DISCO_INFO,
						XmppConstants.NS_IQ_DISCO_INFO }, new String[] {
						Iq.QUERY, XmppConstants.IDENTITY });
				String groupName = identity.getAttribute("type");
				item.addElement(XmppConstants.NS_IQ_ROSTER, "group").addText(
						groupName);
				break;
			}
		}
	}

	/** remove a contact */

	public void unsubscribeContact(Contact c) {
		contacts.remove(c.jid);
		String domain = Contact.domain(c.jid);
		Iq iq_roster = null;
		Element query = null;
		String to = null;
		String ns = XmppConstants.NS_IQ_ROSTER;
		String itemName = XmppConstants.ITEM;
		boolean remSub = true;
		// it is a roster new
		if (domain != null && distrRosters.containsKey(domain)) {
			to = domain;
			ns = XmppConstants.NS_XMPP_SUBSCRIPTIONS;
			itemName = Presence.T_UNSUBSCRIBE;
			remSub = false;
		}
		iq_roster = new Iq(to, Iq.T_SET);
		query = iq_roster.addElement(ns, Iq.QUERY);
		Element item = query.addElement(null, itemName);
		item.setAttribute("jid", c.jid);
		if (remSub) item.setAttribute(XmppConstants.SUBSCRIPTION,
				XmppConstants.REMOVE);
		xmlStream.send(iq_roster);
	}

	private void recreateRoster(Element iq, boolean purge) {

		// XXX -> this should be run within a synchronized

		//		// Build a lookup table with roster
		//		Hashtable oldrst = new Hashtable();
		//		Enumeration en = contacts.elements();
		//		while (en.hasMoreElements()) {
		//			Contact c = (Contact) en.nextElement();
		//			oldrst.put(c.jid, c);
		//		}

		Element query = iq.getChildByName(null, Iq.QUERY);
		if (query == null) { return; }
		String tempVer = query.getAttribute("ver");
		if (tempVer != null) this.setRosterVersion(tempVer);
		Element items[] = query.getChildrenByName(null, "item");

		String myDomain = Contact.domain(myContact.jid);
		Contact myDomainContact = getContactByJid(myDomain);

		if (purge) {
			Hashtable newContacts = new Hashtable();
			// the old contacts that have a presence but are not 
			// in the roster
			Vector oldUnRosterContacts = new Vector();
			for (int i = 0; i < items.length; i++) {
				Contact c = getContactByJid(items[i].getAttribute("jid"));
				if (c != null) newContacts.put(c.jid, c);
			}
			Enumeration en = this.contacts.keys();
			while (en.hasMoreElements()) {
				Object ithElem = en.nextElement();
				Contact contactToRemove = (Contact) this.contacts.get(ithElem);
				// Presence[] ps = contactToRemove.getAllPresences();
				if (newContacts.containsKey(ithElem) == false) {
					// my server is always visible
					if (!contactToRemove.isVisible()
							&& !myDomain.equals(contactToRemove.jid)) {
						if (xmppListener != null) xmppListener.removeContact(
								contactToRemove, false);
					} else {
						newContacts.put(contactToRemove.jid, contactToRemove);
						oldUnRosterContacts.addElement(contactToRemove);
					}
				}
			}
			this.contacts = newContacts;
			// these old contacts must be updated
			en = oldUnRosterContacts.elements();
			while (en.hasMoreElements()) {
				if (xmppListener != null) xmppListener.updateContact(
						(Contact) en.nextElement(), Contact.CH_STATUS);
			}
		}

		for (int i = 0; i < items.length; i++) {
			updateRosterItem(items[i]);
		}

		Element serverEl;
		if (myDomainContact == null || myDomainContact.resources == null
				|| myDomainContact.resources.length == 0) {
			serverEl = new Element("", "serverEl");
			serverEl.setAttributes(new String[] { Iq.ATT_TO, "jid", "name",
					XmppConstants.SUBSCRIPTION }, new String[] { myContact.jid,
					myDomain, "Jabber Server", Contact.SUB_BOTH });
			updateRosterItem(serverEl);
			/// create a a fictitious presence
			Presence p = new Presence(myContact.jid, Presence.T_SUBSCRIBED,
					"online", "Jabber Server", 1);
			p.setAttribute(Presence.ATT_FROM, myDomain);
			myDomainContact = getContactByJid(myDomain);
			myDomainContact.updatePresence(p);
			updateRosterItem(serverEl);
		}
	}

	/**
	 * Update roster item
	 * 
	 * @param item
	 */
	private void updateRosterItem(Element item) {
		// XXX handle the case in which the subscription is "remove"
		// XXX: A lot of the group logic should be redone
		//	for example I don't like all the translations between group <--> String and so on.. ugly
		String jid = item.getAttribute("jid");
		boolean changedGroups = false;

		Element group_elements[] = item.getChildrenByName(null, "group");
		String groups[] = new String[group_elements.length];
		for (int j = 0; j < groups.length; j++) {
			groups[j] = group_elements[j].getText();
		}

		// "ungrouped" contact if no group assign the ungrouped
		Contact c = getContactByJid(jid);
		if (c == null) {
			c = new Contact(jid, item.getAttribute("name"), item
					.getAttribute(XmppConstants.SUBSCRIPTION), groups);
		} else {
			// contact found, just update
			c.subscription = item.getAttribute(XmppConstants.SUBSCRIPTION);
			String name = item.getAttribute("name");
			if (name != null) {
				c.name = name;
			}
			changedGroups = c.setGroups(groups);
		}

		if (changedGroups) {
			if (xmppListener != null) xmppListener.updateContact(c,
					Contact.CH_GROUP);
		}

		String subscription = item.getAttribute(XmppConstants.SUBSCRIPTION);
		if (xmppListener != null) {
			if (XmppConstants.REMOVE.equals(subscription)) {
				// if the user has removed me from roster
				// there is nothing to do remove contacts and nothing all
				contacts.remove(c.jid);
				xmppListener.removeContact(c, true);
				return;
			} else if (Iq.ATT_FROM.equals(subscription)) {
				Element subEl = item.getChildByName(null,
						XmppConstants.SUBSCRIPTION);
				if (subEl != null) {
					String state = subEl.getAttribute(XmppConstants.STATE);
					if (XmppConstants.ASK.equals(state)) {
						xmppListener.askSubscription(c);
					}
				}
			}
		}

		contacts.put(c.jid, c);
		// check if this contact is one of my registered gateways
		updateGateways(c);
		if (xmppListener != null) xmppListener.updateContact(c,
				Contact.CH_STATUS);
	}

	private void addGateway(String from, Element e) {
		this.registeredGateways.put(from, e);
		// if it is a roster provider I need to ask it
		Element query = e.getChildByName(null, Iq.QUERY);
		Element[] feature = query.getChildrenByNameAttrs(null,
				XmppConstants.FEATURE, new String[] { "var" },
				new String[] { XmppConstants.NS_IQ_ROSTER });
		if (feature.length > 0) {
			if (distrRosters.containsKey(from) == false) {
				DistrRoster distr = new DistrRoster(from);
				distr.retrieveRoster();
			}
		}
	}

	/*
	 * Load the registered gateways from recordStore
	 */
	public synchronized void loadGateways() {
		rosterStore.open();

		try {
			byte[] gwBytes = rosterStore.load(Utils
					.getBytesUtf8(XmppConstants.REGISTERED_GATEWAYS));

			// to check it is a valid xml
			if (gwBytes == null || gwBytes.length == 0) return;

			Element decodedPacket = null;
			try {
				decodedPacket = BProcessor.parse(gwBytes);
			} catch (Exception e) {
				// #mdebug
								e.printStackTrace();
								Logger.log("In loading gateways" + e.getClass().getName()
										+ "\n" + e.getMessage());
				//#enddebug
				return;
			}

			Element[] children = decodedPacket.getChildren();
			try {
				for (int i = 0; i < children.length; i++) {
					Element ithElem = children[i];
					String ithFrom = ithElem.getAttribute(Stanza.ATT_FROM);
					addGateway(ithFrom, ithElem);
				}
			} catch (Exception e) {
				rosterStore.store(Utils
						.getBytesUtf8(XmppConstants.REGISTERED_GATEWAYS),
						new byte[] {});
			}
		} catch (Exception e) {
			rosterStore.store(Utils
					.getBytesUtf8(XmppConstants.REGISTERED_GATEWAYS),
					new byte[] {});
		} finally {
			rosterStore.close();
		}
	}

	/*
	 * save the registered gateways to recordStore
	 */
	private synchronized void saveGateways() {
		Element el = new Element("", "gws");
		Enumeration en = this.registeredGateways.elements();
		while (en.hasMoreElements()) {
			el.addElement((Element) en.nextElement());
		}

		rosterStore.open();
		try {
			rosterStore.store(Utils
					.getBytesUtf8(XmppConstants.REGISTERED_GATEWAYS),
					BProcessor.toBinary(el));
		} catch (Exception e) {

		} finally {
			rosterStore.close();
		}
	}

	/*
	 * Check if contact is a gateway and in case 
	 * start the procedure to add it to the registered gateways
	 * 
	 * @param c
	 * 		The contact to check for
	 */
	private void updateGateways(Contact c) {
		if (c.jid.indexOf('@') >= 0
				|| registeredGateways.containsKey(Contact.userhost(c.jid))) return;

		RosterIqListener gw = new RosterIqListener(
				RosterIqListener.UPDATE_GATEWAY);
		gw.c = c;

		Iq iq = new Iq(c.jid, Iq.T_GET);
		iq.addElement(XmppConstants.NS_IQ_DISCO_INFO, Iq.QUERY);
		iq.send(xmlStream, gw, 240000);
	}

	public Contact getContactByJid(String jid) {
		return (Contact) contacts.get(Contact.userhost(jid));
	}

	public Element getBookmarkByJid(String jid, boolean extended) {
		String ns = extended == false ? XmppConstants.NS_BOOKMARKS
				: XmppConstants.NS_STORAGE_LAMPIRO;
		Element storage = this.privateStorage.getChildByName(ns, "storage");
		Element[] conference = storage.getChildrenByNameAttrs(null,
				XmppConstants.CONFERENCE, new String[] { "jid" },
				new String[] { jid });
		if (conference.length > 0) return conference[0];
		return null;
	}

	public void purge() {
		this.contacts.clear();
	}

	public MUC createMuc(String mucJid, String mucName, String nick,
			String pwd, boolean joinNow) {
		// first check if for some reason a contact 
		// with that jid already exists
		MUC u = null;
		Contact c = this.getContactByJid(mucJid);
		String roomNick = nick != null ? nick : Contact.user(myContact
				.getPrintableName());
		if (c != null && c instanceof MUC == true) {
			u = (MUC) c;
			// the nick could have changed
			u.nick = nick;
			u.pwd = pwd;
		}
		if (u == null) {
			u = new MUC(mucJid, mucName, nick, pwd);
			contacts.put(u.jid, u);
		}
		if (joinNow == false) return u;

		Presence pres = new Presence(myContact.getPresence(null));
		pres.setAttribute(Stanza.ATT_TO, mucJid + "/" + roomNick);
		Element el = new Element(XmppConstants.NS_MUC, DataForm.X);
		pres.addElement(el);
		if (pwd != null) el.addElement(null, "password").addText(pwd);

		xmlStream.send(pres);

		return u;
	}

	public void saveMUC(MUC u, boolean persistent, boolean autojoin,
			boolean lampiroauto_join) {
		Element storage = privateStorage.getChildByName(
				XmppConstants.NS_BOOKMARKS, XmppConstants.STORAGE);
		Element[] conferences = storage.getChildrenByNameAttrs(null,
				XmppConstants.CONFERENCE, new String[] { "jid" },
				new String[] { u.jid });
		Element conference = null;
		if (conferences.length == 0) {
			conference = new Element(XmppConstants.NS_BOOKMARKS,
					XmppConstants.CONFERENCE);
			storage.addElement(conference);
		} else {
			conference = conferences[0];
		}
		if (u.nick != null) {
			Element nickEl = conference.getChildByName(null, "nick");
			if (nickEl == null) nickEl = conference.addElement(
					XmppConstants.NS_BOOKMARKS, "nick");
			nickEl.resetText();
			nickEl.addText(u.nick);
		}
		if (u.pwd != null) {
			Element pwdEl = conference.getChildByName(null, "password");
			if (pwdEl == null) pwdEl = conference.addElement(
					XmppConstants.NS_BOOKMARKS, "password");
			pwdEl.resetText();
			pwdEl.addText(u.pwd);
		}

		if (persistent == false) storage.removeChild(conference);

		conference.setAttribute(MUC.AUTO_JOIN, autojoin ? "true" : "false");
		conference.setAttribute("jid", u.jid);
		conference.setAttribute("name", Contact.user(u.jid));

		storage = privateStorage.getChildByName(
				XmppConstants.NS_STORAGE_LAMPIRO, XmppConstants.STORAGE);
		conferences = storage.getChildrenByNameAttrs(null,
				XmppConstants.CONFERENCE, new String[] { "jid" },
				new String[] { u.jid });
		Element extConference = null;
		if (conferences.length == 0) {
			extConference = new Element(XmppConstants.NS_STORAGE_LAMPIRO,
					XmppConstants.CONFERENCE);
			storage.addElement(extConference);
		} else {
			extConference = conferences[0];
		}

		// if the conference is not persistent or it does not exist anymore i need to
		// remove even the ext conference
		if (persistent == false) {
			storage.removeChild(extConference);
		}

		extConference.setAttribute(MUC.LAMPIRO_AUTO_JOIN,
				lampiroauto_join ? "true" : "false");
		extConference.setAttribute("jid", u.jid);

		if (serverStorage == true) {
			Iq iq = new Iq(null, Iq.T_SET);
			iq.addElement(privateStorage);
			iq.send(xmlStream, null, 240000);
		}

	}

	// XXX temporary removed
	// private void recreateGroups() {
	//	    
	// // unclassified users are group 0, remove all other groups
	// groups.removeAllElements();
	// Group ng = new Group("No Group");
	// groups.addElement(ng);
	//	    
	// Group gi;
	// Enumeration en = contacts.elements();
	// while(en.hasMoreElements()) {
	// Contact c = (Contact) en.nextElement();
	//
	// // the contact is not in any group
	// if(c.groups.length == 0) {
	// ng.addContact(c);
	// } else {
	//
	// // add a contact in all the pertaining groups
	// for(int p = 0; p < c.groups.length; p++) {
	// gi = findGroup(c.groups[p]);
	// gi.addContact(c);
	// }
	// }
	// }
	// }

	public void cleanAndRetrieve() {
		readFromStorage();
		retrieveBookmarks();
		retrieveRoster(false, true);
	}

	public void setupStore(String my_jid) {
		String rmsName = getRecordStoreName(my_jid);
		this.rosterStore = KeyStoreFactory.getStore(rmsName);
	}

	public KeyStore getRecordStore() {
		return this.rosterStore;
	}

	/**
	 * @param my_jid
	 * @return
	 */
	public String getRecordStoreName(String my_jid) {
		String rmsName = Utils.jabberify("rstr_" + my_jid, 31);
		return rmsName;
	}

	/**
	 * @param rosterVersion the rosterVersion to set
	 */
	public void setRosterVersion(String rosterVersion) {
		this.rosterVersion = rosterVersion;
	}

	/**
	 * @return the rosterVersion
	 */
	public String getRosterVersion() {
		return rosterVersion;
	}

	public void setXmppListener(XmppListener xmppListener) {
		this.xmppListener = xmppListener;
	}

	public void setMyContact(Contact me) {
		this.myContact = me;
	}

	/**
	 * Update the status of a task. Queue it if this is the first time it's
	 * status is updated
	 * 
	 * @param task
	 */
	public void updateTask(Task task) {
		Contact user = getContactByJid(task.getFrom());
		// I may wish to execute commands even on unknown contacts
		if (user == null) {
			user = new Contact(Contact.userhost(task.getFrom()), null, null,
					null);
			contacts.put(Contact.userhost(user.jid), user);
		}
		user.addTask(task);
		// #mdebug
				System.out.println("Tsk: " + Integer.toHexString(task.getStatus()));
		//#enddebug
		byte type = task.getStatus();

		// true if we should display the command
		boolean display = false;
		boolean removed = false;

		if ((type & Task.CMD_MASK) == Task.CMD_MASK) {
			switch (type) {
				case Task.CMD_FORM_LESS:
					display = false;
					removed = true;
					user.removeTask(task);
					break;
				case Task.CMD_INPUT:
					display = true;
					break;
				case Task.CMD_EXECUTING:
					// do nothing, just wait for an answer
					break;
				case Task.CMD_CANCELING:
					// do nothing, just wait for an answer
					break;
				case Task.CMD_CANCELED:
					display = true;
					removed = true;
					user.removeTask(task);
					break;
				case Task.CMD_FINISHED:
					// tasks.removeElement(task);
					display = true;
					break;
				case Task.CMD_ERROR:
					display = true;
					removed = true;
					break;
				case Task.CMD_DESTROY:
					removed = true;
					user.removeTask(task);
					break;
			}
		} else { // simple data form
			switch (type) {
				case Task.DF_FORM:
					display = true;
					break;
				case Task.DF_SUBMITTED:
					removed = true;
					user.removeTask(task);
					break;
				case Task.DF_CANCELED:
					removed = true;
					user.removeTask(task);
					break;
				case Task.DF_RESULT:
					display = true;
					break;
				case Task.DF_ERROR:
					display = true;
					removed = true;
					break;
				case Task.DF_DESTROY:
					removed = true;
					user.removeTask(task);
					break;
			}
		}

		task.setEnableDisplay(display);
		task.setEnableNew(!removed);
	}

	/**
	 * @return the xmppListener
	 */
	public XmppListener getXmppListener() {
		return xmppListener;
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
	};
}
