/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: AddContactScreen.java 2329 2010-11-16 14:12:50Z luca $
*/

package lampiro.screens;

import it.yup.ui.UIButton;
import it.yup.ui.UICanvas;
import it.yup.ui.UICombobox;
import it.yup.ui.UIConfig;
import it.yup.ui.UIHLayout;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UILayout;
import it.yup.ui.UIScreen;
import it.yup.ui.UITextField;
import it.yup.ui.wrappers.UIFont;
import it.yup.ui.wrappers.UIImage;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.util.Utils;
import it.yup.xml.Element;
import it.yup.xmpp.Contact;
import it.yup.xmpp.IQResultListener;
import it.yup.client.XMPPClient;
import it.yup.xmpp.XmppConstants;
import it.yup.xmpp.packets.Iq;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.TextField;

/**
 * XXX: there is a warning on itemStateChanged; we implement here
 * ItemStateListener but the method clashes with a method with same signature
 * defined in Displayable. The Displayable method is package-protected so it is
 * not a problem, but it's a confusing warning... Displayable doesn't implement
 * ItemStateListener... nice job, Sun!
 */
public class AddContactScreen extends UIScreen {

	private static ResourceManager rm = ResourceManager.getManager();

	private class ContactListener extends IQResultListener {

		public static final int DESC_TYPE = 0;
		public static final int REGISTER_TYPE = 1;

		public final int LISTENER_TYPE;

		public ContactListener(int type) {
			this.LISTENER_TYPE = type;
		}

		public void handleError(Element e) {
		}

		public void handleResult(Element e) {
			switch (LISTENER_TYPE) {
				case DESC_TYPE:
					updateDesc(e);
					break;

				case REGISTER_TYPE:
					addContact(e);
					break;
			}
		}

		private void addContact(Element e) {
			Element query = e.getChildByName(XmppConstants.JABBER_IQ_GATEWAY,
					Iq.QUERY);
			// some perverted gateway answers the wrong way !!!
			Element q = query.getChildByName(XmppConstants.JABBER_IQ_GATEWAY,
					"prompt");
			if (q == null) {
				q = query.getChildByName(XmppConstants.JABBER_IQ_GATEWAY, "jid");
			}
			String jid = q.getText();
			String name = t_name.getText();
			String group = "";//t_group.getText();
			AddContactScreen.this.registerContact(jid, name, group);
		}

		private void updateDesc(Element e) {
			Element query = e.getChildByName(XmppConstants.JABBER_IQ_GATEWAY,
					Iq.QUERY);
			Element desc = query.getChildByName(XmppConstants.JABBER_IQ_GATEWAY,
					"desc");
			try {
				synchronized (UICanvas.getLock()) {
					t_help.setText(desc.getText());
					AddContactScreen.this.askRepaint();
				}
			} catch (Exception ex) {
				// TODO: handle exception
			}
		}
	}

	/*
	 * the found gateways
	 */
	private Vector gateways = new Vector();

	private UITextField t_name;
	private UITextField t_jid;
	private UILabel t_help;
	//private UITextField t_group;
	private UITextField t_error;
	private UICombobox t_type;

	private UIButton cmd_save = new UIButton(rm.getString(ResourceIDs.STR_SAVE));
	private UIButton cmd_exit = new UIButton(rm
			.getString(ResourceIDs.STR_CANCEL));

	private XMPPClient xmppClient = XMPPClient.getInstance();

	/** false when changin the nick **/
	private boolean adding = true;

	public AddContactScreen() {
		this.setFreezed(true);
		setTitle(rm.getString(ResourceIDs.STR_ADD_CONTACT));
		t_help = new UILabel(rm.getString(ResourceIDs.STR_ADDRESS));
		t_help.setWrappable(true, UICanvas.getInstance().getWidth() - 10);
		t_help.setFocusable(false);
		UIFont xFont = UIConfig.font_body;
		UIFont lFont = UIFont.getFont(xFont.getFace(), UIFont.STYLE_BOLD, xFont
				.getSize());
		t_help.setFont(lFont);

		t_jid = new UITextField("", null, 64, TextField.EMAILADDR);
		t_name = new UITextField(rm.getString(ResourceIDs.STR_NICKNAME), null,
				64, TextField.NON_PREDICTIVE);
		//t_group = new UITextField(rm.getString(ResourceIDs.STR_GROUP), null,
		//		64, TextField.ANY);
		// create but don't append error
		t_error = new UITextField(rm.getString(ResourceIDs.STR_ERROR), null,
				64, TextField.UNEDITABLE);
		t_type = new UICombobox(rm.getString(ResourceIDs.STR_CONTACT_TYPE),
				false);
		UIImage img = null;
		try {
			img = UIImage.createImage("/transport/xmpp.png");
		} catch (IOException e) {
		}
		UILabel transportLabel = new UILabel(img, "Jabber");
		t_type.append(transportLabel);
		t_type.setSelectedIndex(0);

		append(t_type);
		append(t_help);
		append(t_jid);
		append(t_name);
		//append(t_group);

		/*
		 * XXX: useless? // I add a list of groups only if there are groups
		 * Vector v = XMPPClient.getInstance().getRoster().groups; for(int i =
		 * 1; i < v.size(); i++) { Group g = (Group) v.elementAt(i);
		 * ch_grps.append(g.name, null); } if(ch_grps.size() > 0) {
		 * append(ch_grps); }
		 */

		UIHLayout buttonLayout = new UIHLayout(2);
		buttonLayout.setGroup(false);
		buttonLayout.insert(cmd_exit, 0, 50, UILayout.CONSTRAINT_PERCENTUAL);
		buttonLayout.insert(cmd_save, 1, 50, UILayout.CONSTRAINT_PERCENTUAL);
		append(buttonLayout);
		this.setFreezed(false);
		getGateways();
	}

	private void getGateways() {
		Hashtable registeredGateways = XMPPClient.getInstance().getRoster().registeredGateways;
		Enumeration en = registeredGateways.keys();
		while (en.hasMoreElements()) {
			String ithFrom = (String) en.nextElement();
			Element el = (Element) registeredGateways.get(ithFrom);
			Element identity = el.getPath(new String[] {
					XmppConstants.NS_IQ_DISCO_INFO, XmppConstants.NS_IQ_DISCO_INFO },
					new String[] { Iq.QUERY, XmppConstants.IDENTITY });
			String ithType = identity.getAttribute("type");
			String ithName = identity.getAttribute("name");

			UIImage img = null;
			Contact rosterContact = XMPPClient.getInstance().getRoster()
					.getContactByJid(ithFrom);
			if (rosterContact == null || rosterContact.isVisible() == false) continue;

			if (ithType != null) {
				try {
					img = UIImage.createImage("/transport/" + ithType + ".png");
				} catch (IOException ex) {
					try {
						img = UIImage.createImage("/transport/transport.png");
					} catch (IOException e1) {
					}
				}
			} else {
				try {
					img = UIImage.createImage("/transport/transport.png");
				} catch (IOException e1) {
				}
			}
			UILabel transportLabel = new UILabel(img, ithName);
			t_type.append(transportLabel);
			this.gateways.addElement(ithFrom);
		}
	}

	public void itemAction(UIItem cmd) {
		if (cmd == t_type) {
			if (t_type.getSelectedIndex() > 0) {
				IQResultListener gjh = new ContactListener(
						ContactListener.DESC_TYPE);
				String to = (String) this.gateways.elementAt(this.t_type
						.getSelectedIndex() - 1);
				Iq iq = new Iq(to, Iq.T_GET);
				iq.addElement(XmppConstants.JABBER_IQ_GATEWAY, Iq.QUERY);
				iq.send(XMPPClient.getInstance().getXmlStream(),gjh);
			} else {
				t_help.setText(rm.getString(ResourceIDs.STR_ADDRESS));
				this.askRepaint();
			}
		} else if (cmd == cmd_save) {
			if (this.t_type.getSelectedIndex() == 0) {
				String jid = t_jid.getText();
				String name = t_name.getText();
				String group = "";//t_group.getText();
				registerContact(jid, name, group);
			} else {
				IQResultListener gjh = new ContactListener(
						ContactListener.REGISTER_TYPE);
				String to = (String) this.gateways.elementAt(this.t_type
						.getSelectedIndex() - 1);
				Iq iq = new Iq(to, Iq.T_SET);
				Element query = iq.addElement(XmppConstants.JABBER_IQ_GATEWAY,
						Iq.QUERY);
				Element prompt = query.addElement(XmppConstants.JABBER_IQ_GATEWAY,
						Iq.PROMPT);
				prompt.addText(this.t_jid.getText());
				iq.send(XMPPClient.getInstance().getXmlStream(),gjh);
			}
			UICanvas.getInstance().show(RosterScreen.getInstance());
			UICanvas.getInstance().close(this);
		} else if (cmd == cmd_exit) {
			UICanvas.getInstance().close(this);
		}
	}

	/**
	 * 
	 */
	private void registerContact(String jid, String name, String group) {
		Contact c;
		// XXX also check if the contact is not already present in the
		// roster
		if (jid == null || !(Utils.is_jid(jid))) {
			t_error.setText("bad jid");
			append(t_error);
			return;
		}
		if (adding) {
			String groups[] = null;
			if (group != null && group.length() > 0) {
				groups = new String[] { group };
			}
			// I may already have the same Contact!!!
			// do not create it again
			c = XMPPClient.getInstance().getRoster().getContactByJid(jid);
			if (c == null) c = new Contact(jid, name, null, groups);
			RosterScreen.getInstance().subscribeContact(c, false);
		} else {
			if (name != null && name.length() > 0) {
				c = xmppClient.getRoster().getContactByJid(jid);
				Iq iq = new Iq(null, Iq.T_SET);
				Element query = iq
						.addElement(XmppConstants.NS_IQ_ROSTER, Iq.QUERY);
				Element item = query.addElement(null, XmppConstants.ITEM);
				item.setAttribute("jid", jid);
				item.setAttribute("name", name);
				for (int i = 0; i < c.getGroups().length; i++) {
					String gString = c.getGroups()[i];
					item.addElement(null, "group").addText(gString);
				}
				iq.send(xmppClient.getXmlStream(),null);
			}
		}
	}

	public void changeNickSetup(String from) {
		adding = false;
		this.setTitle(rm.getString(ResourceIDs.STR_CHANGE_NICK));
		this.t_help.setText(rm.getString(ResourceIDs.STR_JABBER_ID));
		this.remove(this.t_type);
		Contact c = xmppClient.getRoster().getContactByJid(from);
		UITextField newt_jid = new UITextField("", c.jid, 64,
				TextField.EMAILADDR | TextField.UNEDITABLE);
		this.replace(t_jid, newt_jid);
		this.t_jid = newt_jid;
		if (c.name != null) this.t_name.setText(c.name);
		this.askRepaint();
	}
}
