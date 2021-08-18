/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: HandleMucScreen.java 1858 2009-10-16 22:42:29Z luca $
 */

package lampiro.screens;

import java.util.Hashtable;

import javax.microedition.lcdui.TextField;

import it.yup.dispatch.EventQuery;
import it.yup.ui.UIButton;
import it.yup.ui.UICanvas;
import it.yup.ui.UICheckbox;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UIMenu;
import it.yup.ui.UIPanel;
import it.yup.ui.UIRadioButtons;
import it.yup.ui.UIScreen;
import it.yup.ui.UITextField;
import it.yup.ui.UIUtils;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.util.Utils;
import it.yup.xml.Element;
import it.yup.xmlstream.BasicXmlStream;
import it.yup.xmlstream.PacketListener;
import it.yup.xmpp.Contact;
import it.yup.xmpp.IQResultListener;
import it.yup.xmpp.MUC;
import it.yup.xmpp.Roster;
import it.yup.client.XMPPClient;
import it.yup.xmpp.XmppConstants;
import it.yup.xmpp.packets.DataForm;
import it.yup.xmpp.packets.Iq;
import it.yup.xmpp.packets.Presence;

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

public class HandleMucScreen extends UIScreen {

	static class MUCStateHandler extends IQResultListener {

		private IQResultListener listener;

		public MUCStateHandler(IQResultListener listener) {
			this.listener = listener;
		}

		public void handleError(Element e) {
		}

		public void handleResult(Element e) {
			String _from = e.getAttribute(Iq.ATT_FROM);
			Iq nextIq = new Iq(_from, Iq.T_SET);
			Element nextQuery = nextIq.addElement(XmppConstants.NS_MUC_OWNER,
					Iq.QUERY);
			String name = Contact.user(_from);
			Element x = e.getPath(new String[] { null, null }, new String[] {
					Iq.QUERY, "x" });
			if (x == null) return;
			Element[] fields = x.getChildrenByName(null, "field");

			x = nextQuery.addElement(XmppConstants.JABBER_X_DATA, "x");
			x.setAttribute(Iq.ATT_TYPE, DataForm.TYPE_SUBMIT);

			Hashtable conf = new Hashtable(20);
			conf.put("FORM_TYPE", "http://jabber.org/protocol/muc#roomconfig");
			conf.put("muc#roomconfig_roomname", name);
			conf.put("muc#roomconfig_roomdesc", name);
			//conf.put("muc#roomconfig_maxusers", "None");
			conf.put("muc#roomconfig_roomsecret", " ");
			conf.put("muc#roomconfig_enablelogging", "0");
			conf.put("muc#roomconfig_publicroom", "0");
			conf.put("muc#roomconfig_persistentroom", "0");
			conf.put("muc#roomconfig_changesubject", "1");
			conf.put("muc#roomconfig_allowinvites", "1");
			conf.put("muc#roomconfig_moderatedroom", "0");
			conf.put("muc#roomconfig_whois", "anyone");
			conf.put("muc#roomconfig_membersonly", "1");
			//conf.put("muc#roomconfig_roomadmins", Contact.userhost(XMPPClient
			//		.getInstance().my_jid));

			for (int i = 0; i < fields.length; i++) {
				Element ithField = fields[i];
				Element ithNewField = new Element(ithField.uri, ithField.name);
				String ithVar = ithField.getAttribute("var");
				ithNewField.setAttribute("var", ithVar);
				String ithVal = (String) conf.get(ithVar);
				if (ithVal != null) {
					ithNewField.addElement(ithField.uri, "value").addText(
							ithVal);
				} else {
					// the default value
					Element val = ithField.getChildByName(null, "value");
					if (val != null) {
						ithNewField.addElement(val);
					} else
						ithNewField.addElement(ithField.uri, "value").addText(
								"0");
				}

				x.addElement(ithNewField);
			}
			//System.out.println(Utils.getStringUTF8(nextIq.toXml()));
			nextIq.send(XMPPClient.getInstance().getXmlStream(),listener);
		}
	}

	public static class HMC_CONSTANTS {
		public static int NEVER = 0;
		public static int ALWAYS = 1;
		public static int LAMPIRO_ONLY = 2;
		public static int LAMPIRO_NEVER = 3;

		public static int CREATE = 1;
		public static int CHANGE_NICK = 2;
		public static int JOIN_NOW = 4;
	}

	UIPanel mainPanel = new UIPanel(true, false);
	private static ResourceManager rm = ResourceManager.getManager();

	public UITextField muc_name_field = null;

	private UICheckbox chk_persistent = new UICheckbox(rm
			.getString(ResourceIDs.STR_PERSISTENT), false);

	private UIRadioButtons radio_autojoin = null;

	private UIButton cmd_close = new UIButton(rm
			.getString(ResourceIDs.STR_CLOSE));

	public UIButton cmd_save = new UIButton(rm.getString(ResourceIDs.STR_SAVE));

	private UILabel lbl_autojoin = new UILabel(rm
			.getString(ResourceIDs.STR_AUTOJOIN));

	private UITextField txt_nick = new UITextField(rm
			.getString(ResourceIDs.STR_NICK_NAME), Contact.user(XMPPClient
			.getInstance().getMyContact().jid), 100, TextField.ANY);

	private UITextField txt_pwd = new UITextField(rm
			.getString(ResourceIDs.STR_PASSWORD), "", 100, TextField.PASSWORD);

	public UILabel infoLabel = new UILabel("");

	private boolean creator = false;

	private String jid;

	public boolean joinNow = true;

	/*
	 * The host for the conference
	 */
	private String host;
	private boolean changeNick = false;

	/*
	 * @param creator
	 * if myContact is the creator
	 */
	public HandleMucScreen(String jid, String host, int constants) {
		super();
		this.changeNick = (constants & HMC_CONSTANTS.CHANGE_NICK) > 0;
		this.jid = jid;
		this.creator = (constants & HMC_CONSTANTS.CREATE) > 0;
		this.host = host;
		this.joinNow = (constants & HMC_CONSTANTS.JOIN_NOW) > 0;

		String[] items = new String[] {
				rm.getString(ResourceIDs.STR_AUTOJOIN_NO),
				rm.getString(ResourceIDs.STR_AUTOJOIN_YES),
				rm.getString(ResourceIDs.STR_AUTOJOIN_LAMPIRO),
				rm.getString(ResourceIDs.STR_AUTOJOIN_OC) };
		radio_autojoin = new UIRadioButtons(items);

		infoLabel.setWrappable(true, UICanvas.getInstance().getWidth() - 30);

		setTitle(rm.getString(ResourceIDs.STR_NAME));
		this.append(mainPanel);

		if (creator == false) {
			muc_name_field = new UITextField(rm
					.getString(ResourceIDs.STR_GROUP_CHAT), "", 50,
					TextField.UNEDITABLE);
			mainPanel.addItem(infoLabel);
		} else {
			muc_name_field = new UITextField(rm
					.getString(ResourceIDs.STR_CHOOSE_NAME), "", 50,
					TextField.ANY, UITextField.FORMAT_LOWER_CASE);
		}
		mainPanel.addItem(muc_name_field);
		mainPanel.addItem(txt_nick);
		mainPanel.addItem(txt_pwd);
		if (changeNick == false
				&& XMPPClient.getInstance().getRoster().serverStorage == true) {
			mainPanel.addItem(chk_persistent);
			mainPanel.addItem(lbl_autojoin);
			mainPanel.addItem(radio_autojoin);
		} else
			chk_persistent.setChecked(false);
		if (changeNick == true) mainPanel.removeItem(txt_pwd);
		placeItems();
		radio_autojoin.setSelectedIndex(HMC_CONSTANTS.NEVER);
		radio_autojoin.setSelected(false);
		mainPanel.setSelectedItem(muc_name_field);
		mainPanel.addItem(UIUtils.easyButtonsLayout(cmd_close, cmd_save));
		if (jid != null) {
			Roster roster = XMPPClient.getInstance().getRoster();
			Element bookEl = roster.getBookmarkByJid(jid, false);
			Contact c = roster.getContactByJid(jid);
			if (c != null) {
				MUC muc = ((MUC) c);
				if (muc.nick != null) this.txt_nick.setText(muc.nick);
				this.txt_pwd.setText(muc.pwd != null ? muc.pwd : "");
			}

			boolean autojoin = false;
			if (bookEl != null) {
				this.chk_persistent.setChecked(true);
				String attribute = bookEl.getAttribute(MUC.AUTO_JOIN);
				if (attribute != null) autojoin = attribute.equals("true");
			}

			bookEl = roster.getBookmarkByJid(jid, true);
			boolean lampiro_autojoin = true;
			if (bookEl != null) lampiro_autojoin = bookEl.getAttribute(
					MUC.LAMPIRO_AUTO_JOIN).equals("true");

			int selectedIdx = (autojoin ^ lampiro_autojoin) ? 1 : 0;
			selectedIdx *= 2;
			selectedIdx += (autojoin ? 1 : 0);
			this.radio_autojoin.setSelectedIndex(selectedIdx);
			placeItems();
		}
	}

	public void menuAction(UIMenu menu, UIItem item) {

	}

	public void itemAction(UIItem c) {
		if (c == this.cmd_close) {
			UICanvas.getInstance().close(this);
		} else if (c == cmd_save) {
			// muc field text
			String mf = muc_name_field.getText();
			if (creator && Utils.is_email(mf)) {
				// when a creating a muc if muc_name is a 
				// jid it means it is on another server
				this.host = Contact.domain(mf);
				muc_name_field.setText(Utils.jabberify(Contact.user(mf), -1));
			}
			final String mucName = this.muc_name_field.getText();
			boolean persistent = this.chk_persistent.isChecked();
			boolean autojoin = this.radio_autojoin.getSelectedIndex() == HMC_CONSTANTS.ALWAYS
					|| this.radio_autojoin.getSelectedIndex() == HMC_CONSTANTS.LAMPIRO_NEVER;
			boolean lampiroauto_join = this.radio_autojoin.getSelectedIndex() == HMC_CONSTANTS.ALWAYS
					|| this.radio_autojoin.getSelectedIndex() == HMC_CONSTANTS.LAMPIRO_ONLY;

			String nickTxt = txt_nick.getText();
			final String pwdTxt = txt_pwd.getText().length() > 0 ? txt_pwd
					.getText() : null;
			final String corText = nickTxt.length() == 0 ? null : nickTxt;
			final String hostText = this.host != null ? this.host
					: RosterScreen.getInstance().mucJid;

			EventQuery q = new EventQuery("presence",
					new String[] { Presence.ATT_FROM }, new String[] { mucName
							+ "@" + hostText + "/" + corText });
			q.child = new EventQuery("x", new String[] { "xmlns" },
					new String[] { XmppConstants.NS_MUC_USER });
			BasicXmlStream.addOnetimePacketListener(q, new PacketListener() {

				public void packetReceived(Element e) {
					Element statusEl = e.getPath(new String[] {
							XmppConstants.NS_MUC_USER, null }, new String[] { "x",
							"status" });
					if (statusEl != null
							&& "201".equals(statusEl.getAttribute("code"))) {
						try {
							synchronized (UICanvas.getLock()) {
								createMUC(mucName, hostText, corText, pwdTxt,
										null, false, true);
							}
						} catch (Exception ex) {
							// #mdebug
							Logger.log("In creating a MUC:");
							ex.printStackTrace();
							// #enddebug
						}
					}
				}
			});

			MUC u = createMUC(mucName, hostText, corText, pwdTxt, null,
					joinNow, false);

			Roster roster = XMPPClient.getInstance().getRoster();
			if (changeNick == false) roster.saveMUC(u, persistent, autojoin,
					lampiroauto_join);

			itemAction(cmd_close);
		} else if (c == chk_persistent) {
			placeItems();
		}
	}

	private void placeItems() {
		colorize(radio_autojoin, chk_persistent.isChecked());
		this.askRepaint();
	}

	private void colorize(UIRadioButtons chk, boolean blacked) {
		chk.setDirty(true);
		lbl_autojoin.setDirty(true);
		chk.setFocusable(blacked);
		chk.setFg_color(0x999999 * (blacked == false ? 1 : 0));
		lbl_autojoin.setFg_color(0x999999 * (blacked == false ? 1 : 0));
		this.askRepaint();
	}

	public static MUC createMUC(String mucName, String mucJid, String nick,
			String pwd, IQResultListener listener, boolean joinNow,
			boolean configureNow) {
		String mucSimplejid = mucName + "@" + mucJid;
		XMPPClient xmppClient = XMPPClient.getInstance();
		Contact c = xmppClient.getRoster().getContactByJid(mucSimplejid);
		if (c != null && c instanceof MUC == false) {
			// if I have a contact instead of a MUC something went wrong
			// I want to delete it
			xmppClient.getRoster().contacts.remove(c);
			RosterScreen.getInstance()._removeContact(c);
		}
		MUC u = xmppClient.getRoster().createMuc(mucSimplejid, mucName, nick,
				pwd, joinNow);
		RosterScreen.getInstance()._updateContact(u, Contact.CH_STATUS);

		if (configureNow == true) {
			Iq iq = new Iq(mucSimplejid + "/", Iq.T_GET);
			Element query = new Element(XmppConstants.NS_MUC_OWNER, Iq.QUERY);
			iq.addElement(query);
			//		Element x = new Element(DataForm.NAMESPACE, DataForm.X);
			//		x.setAttribute("type", "submit");
			//		query.addElement(x);
			iq.send(XMPPClient.getInstance().getXmlStream(),listener);
		}
		return u;
	}
}
