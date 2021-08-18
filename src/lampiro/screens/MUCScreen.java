/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: MUCScreen.java 2329 2010-11-16 14:12:50Z luca $
 */

package lampiro.screens;

import it.yup.ui.UICanvas;
import it.yup.ui.UICombobox;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UIMenu;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.util.ResourceIDs;
import it.yup.xml.Element;
import it.yup.xmlstream.PacketListener;
import it.yup.xmpp.Contact;
import it.yup.xmpp.MUC;
import it.yup.client.XMPPClient;
import it.yup.xmpp.XmppConstants;
import it.yup.xmpp.packets.DataForm;
import it.yup.xmpp.packets.Iq;
import it.yup.xmpp.packets.Message;
import it.yup.xmpp.packets.Presence;
import java.util.Enumeration;
import java.util.Vector;
import lampiro.screens.HandleMucScreen.HMC_CONSTANTS;

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

public class MUCScreen extends ChatScreen implements PacketListener {

	public static int unnamedMUCCounter = 0;

	UICombobox rosterCombo;
	UICombobox mucParticipants;

	private Vector mucCandidates = new Vector();

	private UILabel cmd_topic = new UILabel(rm
			.getString(ResourceIDs.STR_SET_TOPIC));

	private UILabel cmd_add_bookmarks = new UILabel(rm
			.getString(ResourceIDs.STR_PERSISTENT));

	protected UILabel cmd_handle_groups = new UILabel(rm
			.getString(ResourceIDs.STR_MANAGE_GC));

	protected UILabel cmd_change_nick = new UILabel(rm
			.getString(ResourceIDs.STR_CHANGE_NICK));

	private Vector orderedContacts;

	public MUCScreen(Contact u) {
		super(u, u.jid);
		chatLineStart = 1;
		// this.setFreezed(true);
		toggleMenu();

		if (XMPPClient.getInstance().getRoster().getBookmarkByJid(u.jid, false) == null) {
			this.getMenu().insert(this.getMenu().indexOf(cmd_exit),
					cmd_add_bookmarks);
		}

		setTitle(rm.getString(ResourceIDs.STR_GROUP_CHAT) + " "
				+ user.getPrintableName());
		this.rosterCombo = new UICombobox(rm
				.getString(ResourceIDs.STR_ADD_USER), true);
		this.mucParticipants = new UICombobox(rm
				.getString(ResourceIDs.STR_PARTICIPANTS), false);
		// #ifndef RIM
		this.rosterCombo.setSubmenu(this.closeMenu);
		this.mucParticipants.setSubmenu(this.closeMenu);
		// #endif

		addUser.setText(rm.getString(ResourceIDs.STR_ADD_USER));

		this.chatPanel.insertItemAt(mucParticipants, 0);
		this.mucParticipants.setSelected(false);
		if (chatPanel.getItems().size() > 5) {
			// remember the separator
			chatPanel.setSelectedIndex(chatPanel.getItems().size() - 2);
		} else {
			chatPanel.setSelectedItem(mucParticipants);
		}
		chatPanel.setDirty(true);
		UILabel mucName = (UILabel) header.getItem(0);
		mucName.setText(rm.getString(ResourceIDs.STR_TOPIC) + ": "
				+ ((MUC) this.user).topic);
	}

	/**
	 * 
	 */
	void toggleMenu() {
		super.toggleMenu();
		if (RosterScreen.isOnline()) {
			this.getMenu().insert(getMenu().indexOf(cmd_write) + 1, cmd_topic);
			this.getMenu().insert(getMenu().indexOf(cmd_topic) + 1,
					cmd_change_nick);
			this.getMenu().insert(getMenu().indexOf(cmd_change_nick) + 1,
					cmd_handle_groups);
			this.getMenu().remove(addUser);
			this.getMenu().insert(getMenu().indexOf(cmd_handle_groups) + 1,
					addUser);
		}
		// this.getMenu().remove(this.cmd_capture_img);
		// this.getMenu().remove(this.cmd_capture_aud);
	}

	private void populateParticipants() {
		mucParticipants.removeAll();
		Presence[] res = this.user.getAllPresences();
		this.mucParticipants.append(rm.getString(ResourceIDs.STR_ADD_USER));
		for (int i = 0; res != null && i < res.length; i++) {
			Presence ithPresence = res[i];
			String ithNick = Contact.resource(ithPresence
					.getAttribute(Presence.ATT_FROM));
			this.mucParticipants.append(ithNick).setFocusable(false);
		}
	}

	protected void paint(UIGraphics g, int w, int h) {
		this.mucParticipants.setSelectedIndex(-1);
		super.paint(g, w, h);
	}

	private void populateRosterCombo() {
		rosterCombo.removeAll();
		mucCandidates.removeAllElements();

		this.orderedContacts = RosterScreen.getOrderedContacts(false);

		for (Enumeration en = orderedContacts.elements(); en.hasMoreElements();) {
			Contact c = (Contact) en.nextElement();
			String printableName = c.getPrintableName();
			Presence[] ps = c.getAllPresences();
			if (ps == null)
				continue;
			if (ps.length == 1 && RosterScreen.supportsMUC(ps[0])) {
				this.rosterCombo.append(printableName);
				this.mucCandidates.addElement(ps[0]);
			} else if (ps.length >= 2) {
				for (int i = 0; i < ps.length; i++) {
					if (RosterScreen.supportsMUC(ps[1])) {
						this.rosterCombo.append(printableName
								+ " "
								+ Contact.resource(ps[i]
										.getAttribute(Message.ATT_FROM)));
						this.mucCandidates.addElement(ps[i]);
					}
				}
			}
		}
	}

	public boolean keyPressed(int kc) {
		if (this.popupList.size() == 0
				&& this.getMenu().isOpenedState() == false) {
			int ga = UICanvas.getInstance().getGameAction(kc);

			if (ga == UICanvas.FIRE) {
				if (RosterScreen.isOnline()
						&& this.chatPanel.getItems().elementAt(
								chatPanel.getSelectedIndex()) == mucParticipants) {
					populateParticipants();
					mucParticipants.setScreen(this);
				}
				// else if (this.chatPanel.getItems().elementAt(
				// chatPanel.getSelectedIndex()) == rosterCombo) {
				// populateRosterCombo();
				// }
			}
		}
		return super.keyPressed(kc);
	}

	public void menuAction(UIMenu menu, UIItem c) {
		if (c == cmd_handle_groups) {
			RosterScreen.getInstance().handleMuc(this.user.jid, this);
		} else if (c == cmd_change_nick) {
			String jid = Contact.userhost(this.user.jid);
			HandleMucScreen cms = new HandleMucScreen(jid, Contact
					.domain(this.user.jid), HMC_CONSTANTS.CHANGE_NICK
					| HMC_CONSTANTS.JOIN_NOW);
			cms.infoLabel.setText(rm.getString(ResourceIDs.STR_CHANGE_NICK));
			cms.muc_name_field.setText(Contact.user(jid));
			UICanvas.getInstance().open(cms, true, this);
		} else if (c == cmd_add_bookmarks) {
			XMPPClient.getInstance().getRoster().saveMUC((MUC) this.user, true,
					false, false);
			this.getMenu().remove(cmd_add_bookmarks);
		} else if (c == topic_button) {
			String topicName = this.topic_name_field.getText();
			XMPPClient client = XMPPClient.getInstance();
			Message msg = new Message(Contact.userhost(this.user.jid),
					Message.GROUPCHAT);
			msg.addElement(null, "subject").addText(topicName);
			client.sendPacket(msg);
			this.topic_name_field.setText("");
			return;
		} else if (c == cmd_topic) {
			askTopic();
			return;
		} else if (c == addUser) {
			this.setSelectedIndex(this.indexOf(this.rosterCombo));
			chatPanel.setSelected(false);
			chatPanel.setDirty(true);
			openInvitation();
			return;
		} else if (c == cmd_clear) {
			super.menuAction(menu, c);
			this.chatPanel.insertItemAt(rosterCombo, 0);
			this.askRepaint();
			return;
		}

		super.menuAction(menu, c);
	}

	boolean needDisplay() {
		Vector allConvs = user.getAllConvs();
		Enumeration en = allConvs.elements();
		while (en.hasMoreElements()) {
			Object[] coupleConv = (Object[]) en.nextElement();
			Vector messages = (Vector) coupleConv[1];
			if (messages.size() > 0) {
				return true;
			}
		}
		return false;
	}

	boolean isMyPacket(Element e) {
		return Contact.userhost(e.getAttribute(Iq.ATT_FROM)).equals(
				preferredResource);
	}

	/**
	 * 
	 * @param screen_width
	 * @return true if new messages have been added
	 */
	boolean updateConversation() {
		return this.updateResConversation(this.user.jid);
	}

	void updateResource() {
		// the MUCScreen does not have to update the resource:
		// it is made by the presence handling logic
	}

	protected void openComposer() {
		SimpleComposerScreen cs = new MUCComposerScreen(this, (MUC) user);
		UICanvas.display(cs.getTextBox());
	}

	static void handlePresence(MUC presenceMUC, Element e, String type) {
		String jidName = Contact.resource(e.getAttribute(Message.ATT_FROM));
		// avoid printing myself data here
		boolean goingOffline = "unavailable".equals(type);
		// if (jidName.equals(presenceMUC.nick)) return;
		Message msg = null;
		msg = new Message(presenceMUC.jid, Message.HEADLINE);
		msg.setAttribute(Message.ATT_FROM, e.getAttribute(Message.ATT_FROM));
		String msgText = "";
		boolean send = false;
		String endString = " "
				+ rm.getString(ResourceIDs.STR_GROUP_CHAT).toLowerCase() + ".";
		if (type == null) {
			send = true;
			msgText = jidName + " " + rm.getString(ResourceIDs.STR_JOINED_MUC)
					+ endString;
		} else if (goingOffline) {
			send = true;
			msgText = jidName + " " + rm.getString(ResourceIDs.STR_LEFT_MUC)
					+ endString;
		}
		if (send == true) {
			msg.setBody(msgText);
			// presenceMUC.lastResource = null;
			presenceMUC
					.addMessageToHistory(e.getAttribute(Message.ATT_TO), msg);
			RosterScreen rs = RosterScreen.getInstance();
			try {
				synchronized (UICanvas.getLock()) {
					rs._updateContact(presenceMUC, Contact.CH_MESSAGE_NEW);
					rs.askRepaint();
					MUCScreen ms = (MUCScreen) RosterScreen.getChatScreenList()
							.get(presenceMUC.jid);
					if (ms != null) {
						if (UICanvas.getInstance().getCurrentScreen() == ms)
							ms.updateConversation();
						ms.askRepaint();
					}
				}
			} catch (Exception ex) {
				// #mdebug
				Logger.log("In handling presence error:");
				ex.printStackTrace();
				// #enddebug
			}
		}
	}

	void sendInvite(String ithContact) {
		Message msg = new Message(user.jid, null);
		Element x = new Element(XmppConstants.NS_MUC_USER, DataForm.X);
		msg.addElement(x);
		Element invite = new Element("", "invite");
		invite.setAttribute(Message.ATT_TO, ithContact);
		x.addElement(invite);
		XMPPClient.getInstance().sendPacket(msg);
	}

	protected void getPrintableHeight(UIGraphics g, int h) {
		super.getPrintableHeight(g, h);
		if (mucParticipants == null) {
			// this method could be called without mucParticipants
			// being initialized.
			this.mucParticipants = new UICombobox(rm
					.getString(ResourceIDs.STR_ADD_USER), true);
		}
		this.printableHeight -= this.mucParticipants.getHeight(g);
	}

	ConversationEntry wrapMessage(String text[]) {

		// #ifdef TIMING
		// @ long t1 = System.currentTimeMillis();
		// #endif

		// byte type = (text[2] != null && Contact.resource(text[2]) != null &&
		// Contact
		// .resource(text[2]).equals(Contact.user(text[0]))) ?
		// ConversationEntry.ENTRY_TO
		// : ConversationEntry.ENTRY_FROM;

		String otherNick = Contact.resource(text[4]);
		byte type = (otherNick == null || otherNick
				.equals(((MUC) this.user).nick)) ? ConversationEntry.ENTRY_FROM
				: ConversationEntry.ENTRY_TO;

		// #ifdef TIMING
		// @ System.out.println("wrap conv: " + (System.currentTimeMillis() -
		// @ // t1));
		// #endif

		String labelText = "";
		labelText += text[1];
		ConversationEntry convEntry = new ConversationEntry(labelText, type);
		if (type == ConversationEntry.ENTRY_TO)
			convEntry.from = otherNick != null ? otherNick : "";
		convEntry.arriveTime = text[2];
		convEntry.messageType = text[3];
		return convEntry;
	}

	public void itemAction(UIItem c) {
		if (c == this.mucParticipants) {
			String selString = this.mucParticipants.getSelectedString();
			if (rm.getString(ResourceIDs.STR_ADD_USER).equals(selString)) {
				openInvitation();
			} else {
				// hack to leave the correct screen opened
				mucParticipants.setScreen(this);
			}
			return;
		} else if (c == this.rosterCombo) {
			int[] selIndeces = this.rosterCombo.getSelectedIndeces();
			for (int i = 0; i < selIndeces.length; i++) {
				int ithInt = selIndeces[i];
				Presence p = (Presence) this.mucCandidates.elementAt(ithInt);
				String invitedJid = p.getAttribute(Message.ATT_FROM);
				this.sendInvite(invitedJid);
				this.notifyUser(invitedJid);
			}
			this.setSelectedIndex(this.indexOf(this.chatPanel));
			boolean flags[] = new boolean[this.mucCandidates.size()];
			for (int i = 0; i < flags.length; i++)
				flags[i] = false;
			this.rosterCombo.setSelectedFlags(flags);
			this.chatPanel
					.setSelectedIndex(this.chatPanel.getItems().size() - 2);

			rosterCombo.removeAll();
			mucCandidates.removeAllElements();
			if (orderedContacts != null)
				orderedContacts.removeAllElements();

			RosterScreen rs = RosterScreen.getInstance();
			rs._updateContact(this.user, Contact.CH_MESSAGE_NEW);

			// replace the rostercombo with the participants combo
			this.chatPanel.insertItemAt(mucParticipants, this.chatPanel
					.removeItem(rosterCombo));

			// hack to leave the correct screen opened
			mucParticipants.setScreen(this);

			rs.askRepaint();
			this.askRepaint();
			return;
		}
		super.itemAction(c);
	}

	private void openInvitation() {
		// the invite has been requested
		UICanvas.getInstance().close(mucParticipants.comboScreen);
		this.chatPanel.insertItemAt(rosterCombo, this.chatPanel
				.removeItem(mucParticipants));
		this.chatPanel.setSelectedItem(rosterCombo);
		// this.keyPressed(UICanvas.getInstance().getKeyCode(Canvas.FIRE));
		populateRosterCombo();
		rosterCombo.openMenu();
		this.chatPanel.insertItemAt(mucParticipants, this.chatPanel
				.removeItem(rosterCombo));
		this.chatPanel.setSelectedItem(mucParticipants);
		// hack to leave the correct screen opened
		mucParticipants.setScreen(rosterCombo.comboScreen);
	}

	private void notifyUser(String invitedJid) {
		Message msg = null;
		msg = new Message(this.user.jid, Message.HEADLINE);
		String myJid = this.user.jid + "/" + ((MUC) this.user).nick;
		msg.setAttribute(Message.ATT_FROM, myJid);
		Contact c = XMPPClient.getInstance().getRoster().getContactByJid(
				invitedJid);
		String msgText = rm.getString(ResourceIDs.STR_INVITATION_SENT) + " "
				+ c.getPrintableName();

		msg.setBody(msgText);
		// presenceMUC.lastResource = null;
		this.user.addMessageToHistory(myJid, msg);
	}

	String getLabelHeader(ConversationEntry entry) {
		String retString = "";
		int fromLength = entry.from.length();
		int arriveTimeLength = entry.arriveTime.length();
		if (arriveTimeLength > 0 || fromLength > 0) {
			retString = "[";
			if (fromLength > 0)
				retString += entry.from;
			if (fromLength > 0 && arriveTimeLength > 0)
				retString += " ";
			if (arriveTimeLength > 0)
				retString += entry.arriveTime;
			retString += "] ";
		}
		return retString;
	}

}
