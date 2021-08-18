/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: ForwardScreen.java 1858 2009-10-16 22:42:29Z luca $
 */

package lampiro.screens;

import it.yup.ui.UICanvas;
import it.yup.ui.UIConfig;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UIMenu;
import it.yup.ui.UIPanel;
import it.yup.ui.UIScreen;
import it.yup.ui.UISeparator;
import it.yup.ui.UIUtils;
import it.yup.ui.wrappers.UIFont;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.util.ResourceIDs;
import it.yup.util.Utils;
import it.yup.xmpp.Contact;
import it.yup.xmpp.MUC;
import it.yup.client.XMPPClient;
import it.yup.xmpp.packets.Message;
import it.yup.xmpp.packets.Presence;

import java.util.Enumeration;
import java.util.Vector;

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

class ForwardScreen extends UIScreen {

	public interface ForwardListener {
		void messageSent();
	}

	/**
	 * 
	 */
	private UIScreen prevScreen;

	private UIPanel contactPanel = new UIPanel(true, true);

	private UILabel cmd_exit = new UILabel(ChatScreen.rm.getString(
			ResourceIDs.STR_CLOSE).toUpperCase());

	Vector orderedContacts = null;

	private String messageToForward = "";

	private String fromContact = "";

	private Contact user;

	private String preferredResource;

	/*
	 * the key used when filtering contacts
	 */
	private int sel_last_key = -1;

	/*
	 * the pattern used when filtering contacts
	 */

	private String sel_pattern = "";

	/*
	 * the time stamp of the last key press
	 */
	private long sel_last_ts = 0;

	/*
	 * The offset of the selected key in the research pattern
	 */
	private int sel_key_offset = 0;

	private UILabel instrLabel;

	private UISeparator sep;

	public ForwardScreen(UIScreen prevScreen, String text, String fromContact,
			Contact user, String preferredResource) {
		super();
		this.user = user;
		this.preferredResource = preferredResource;
		this.prevScreen = prevScreen;
		this.append(contactPanel);
		this.messageToForward = text;
		this.fromContact = fromContact;
		this.setTitle(this.prevScreen.getTitle());
		this.orderedContacts = RosterScreen.getOrderedContacts(true);
		String cutMsg = messageToForward;
		if (cutMsg.length() > 100) {
			cutMsg = cutMsg.substring(1, 100) + " ...";
		}
		instrLabel = new UILabel(ChatScreen.rm
				.getString(ResourceIDs.STR_FORWARD)
				+ " \""
				+ cutMsg
				+ "\" "
				+ ChatScreen.rm.getString(ResourceIDs.STR_TO).toLowerCase()
				+ ":");
		instrLabel.setWrappable(true, UICanvas.getInstance().getWidth() - 40);
		instrLabel.setAnchorPoint(UIGraphics.HCENTER);
		UIFont xFont = UIConfig.font_body;
		UIFont lfont = UIFont.getFont(xFont.getFace(), UIFont.STYLE_BOLD, xFont
				.getSize());
		instrLabel.setFont(lfont);
		sep = new UISeparator(2, UIUtils.colorize(UIConfig.bg_color, -10));
		this.contactPanel.addItem(instrLabel);
		this.contactPanel.addItem(sep);
		instrLabel.setFocusable(false);

		this.setMenu(new UIMenu(""));
		UIMenu menu = this.getMenu();
		menu.append(cmd_exit);

		for (Enumeration en = orderedContacts.elements(); en.hasMoreElements();) {
			Contact c = (Contact) en.nextElement();
			String printableName = c.getPrintableName();
			Presence[] ps = c.getAllPresences();
			boolean isMUC = c instanceof MUC;
			try {
				if (ps == null || ps.length == 1 || isMUC) {
					UILabel ithContactLabel = new UILabel(printableName);
					ithContactLabel.setFocusable(true);
					ithContactLabel.setWrappable(true, UICanvas.getInstance()
							.getWidth() - 40);
					this.contactPanel.addItem(ithContactLabel);
					ithContactLabel.setStatus(c);
				} else if (ps.length >= 2) {
					for (int i = 0; i < ps.length; i++) {
						String printablePresName = printableName
								+ " "
								+ Contact.resource(ps[i]
										.getAttribute(Message.ATT_FROM));
						UILabel ithContactLabel = new UILabel(printablePresName);
						ithContactLabel.setWrappable(true, UICanvas
								.getInstance().getWidth() - 30);
						ithContactLabel.setFocusable(true);
						this.contactPanel.addItem(ithContactLabel);
						ithContactLabel.setStatus(ps[i]);
					}
				}
			} catch (Exception e) {
				// #mdebug
				e.printStackTrace();
				Logger.log(e.getClass().getName());
				// #enddebug
			}
		}
		// select the first contact
		if (this.contactPanel.getItems().size() >= 3)
			contactPanel.setSelectedIndex(2);
		this.setSelectedItem(contactPanel);
	}

	public boolean keyPressed(int kc) {
		// #ifdef UI_DEBUG
		// @ Logger.log("Roster screen keypressed :" + kc);
		// #endif
		if (this.popupList.size() == 0
				& this.getMenu().isOpenedState() == false) {
			if (UICanvas.getInstance().hasQwerty()) {
				if ((kc >= 'A' && kc <= 'Z') || (kc >= 'a' && kc <= 'z')
						|| (kc >= '0' && kc <= '9')) {
					this.setFreezed(true);
					sel_pattern = sel_pattern + (char) kc;
					filterContacts();
					this.setFreezed(false);
					this.askRepaint();
					return true;
				}
			}

			switch (kc) {
			case UICanvas.KEY_NUM0:
			case UICanvas.KEY_NUM1:
			case UICanvas.KEY_NUM2:
			case UICanvas.KEY_NUM3:
			case UICanvas.KEY_NUM4:
			case UICanvas.KEY_NUM5:
			case UICanvas.KEY_NUM6:
			case UICanvas.KEY_NUM7:
			case UICanvas.KEY_NUM8:
			case UICanvas.KEY_NUM9:
				int key_num = kc - UICanvas.KEY_NUM0;
				this.setFreezed(true);
				long t = System.currentTimeMillis();
				if ((key_num != sel_last_key) || t - sel_last_ts > 1000) {
					// new key
					sel_key_offset = 0;
					sel_last_key = key_num;
					sel_pattern = sel_pattern
							+ Utils.itu_keys[key_num][sel_key_offset];
				} else {
					// shifted key
					sel_key_offset += 1;
					if (sel_key_offset >= Utils.itu_keys[key_num].length)
						sel_key_offset = 0;
					sel_pattern = sel_pattern.substring(0,
							sel_pattern.length() - 1)
							+ Utils.itu_keys[key_num][sel_key_offset];
				}
				filterContacts();
				sel_last_ts = t;
				this.setFreezed(false);
				if (this.contactPanel.getItems().size() > 0) {
					this.contactPanel.setSelectedIndex(0);
				}
				this.askRepaint();
				return true;
			}

			int ga = UICanvas.getInstance().getGameAction(kc);
			if (kc == UICanvas.MENU_CANCEL || kc == 8 || ga == UICanvas.LEFT) {
				if (sel_pattern.length() > 0) {
					this.setFreezed(true);
					sel_pattern = sel_pattern.substring(0,
							sel_pattern.length() - 1);
					filterContacts();
					this.setFreezed(false);
					askRepaint();
					return true;
				}
			}
		}
		return super.keyPressed(kc);
	}

	private Vector savedContacts = null;

	private void filterContacts() {
		try {
			if (sel_pattern.length() == 1 && savedContacts == null) {
				savedContacts = new Vector(this.contactPanel.getItems().size());
				Enumeration en = this.contactPanel.getItems().elements();
				while (en.hasMoreElements()) {
					savedContacts.addElement(en.nextElement());
				}
				savedContacts.removeElement(instrLabel);
				savedContacts.removeElement(sep);
			}
			this.contactPanel.removeAllItems();
			this.contactPanel.addItem(instrLabel);
			this.contactPanel.addItem(sep);
			Enumeration en = this.savedContacts.elements();
			boolean cond = true;
			while (en.hasMoreElements()) {
				UILabel uil = (UILabel) en.nextElement();
				if (sel_pattern.length() >= 1)
					cond = (uil.getText().toLowerCase().indexOf(
							sel_pattern.toLowerCase()) == 0);
				if (cond)
					this.contactPanel.addItem(uil);
			}
			if (sel_pattern.length() == 0) {
				this.savedContacts = null;
				this.setTitle(this.prevScreen.getTitle());
			} else if (sel_pattern.length() >= 1) {
				this.setTitle(sel_pattern);
			}
		} catch (Exception e) {
			// #mdebug
			e.printStackTrace();
			Logger.log(e.getClass().getName());
			// #enddebug
		}
	}

	public void itemAction(UIItem item) {
		if (item instanceof UILabel) {
			Object statusTo = item.getStatus();
			if (statusTo != null) {
				Message msg = null;
				String to = (statusTo instanceof Contact ? ((Contact) statusTo).jid
						: ((Presence) statusTo).getAttribute(Message.ATT_FROM));
				Contact forwardContact = XMPPClient.getInstance().getRoster()
						.getContactByJid(to);
				String msgType = Message.CHAT;
				String forwardResource = to;
				if (statusTo instanceof MUC) {
					msgType = Message.GROUPCHAT;
				} else if (statusTo instanceof Contact) {
					Presence[] resources = forwardContact.getAllPresences();
					forwardResource = (resources != null
							&& resources.length > 0 ? resources[0]
							.getAttribute(Message.ATT_FROM) : to);
				}
				msg = new Message(to, msgType);
				String tempText = null;
				if (fromContact != null) {
					tempText = ChatScreen.rm
							.getString(ResourceIDs.STR_FORWARDED_TEXT)
							+ " "
							+ fromContact
							+ ": \""
							+ this.messageToForward + "\"";
				} else {
					tempText = this.messageToForward;
				}
				msg.setBody(tempText);

				XMPPClient.getInstance().sendPacket(msg);

				if (user != null) {
					this.user.addMessageToHistory(this.preferredResource, msg);
				}
				if (forwardContact != null) {
					// i added to the message history and the remove it
					// so that it is maintained now but the "icon" is not
					// updated
					forwardContact.addMessageToHistory(forwardResource, msg);
					RosterScreen.getInstance()._updateContact(forwardContact,
							Contact.CH_MESSAGE_NEW);
					// forwardContact.resetMessageHistory(forwardResource);
				}
				if (prevScreen instanceof ForwardListener) {
					((ForwardListener) prevScreen).messageSent();
				}
			}
			this.closeMe();
		}
	}

	public void menuAction(UIMenu menu, UIItem cmd) {
		if (cmd == cmd_exit) {
			closeMe();
		}
	}

	private void closeMe() {
		UICanvas.getInstance().close(this);
		UICanvas.getInstance().open(prevScreen, true);
	}

}
