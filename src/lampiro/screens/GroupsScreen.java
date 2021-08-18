/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: GroupsScreen.java 1858 2009-10-16 22:42:29Z luca $
*/

package lampiro.screens;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.TextField;

import lampiro.screens.rosterItems.UIContactGroup;

import it.yup.ui.UICanvas;
import it.yup.ui.UICheckbox;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UIMenu;
import it.yup.ui.UIPanel;
import it.yup.ui.UIScreen;
import it.yup.ui.UISeparator;
import it.yup.ui.UITextField;
import it.yup.ui.wrappers.UIFont;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.xml.Element;
import it.yup.xmpp.Contact;
import it.yup.xmpp.Roster;
import it.yup.client.XMPPClient;
import it.yup.xmpp.XmppConstants;
import it.yup.xmpp.packets.Iq;

/**
 * @author luca
 *
 */
public class GroupsScreen extends UIScreen {

	private static ResourceManager rm = ResourceManager.getManager();

	private UILabel cmd_submit = new UILabel(rm.getString(
			ResourceIDs.STR_SUBMIT).toUpperCase());
	private UILabel cmd_cancel = new UILabel(rm.getString(
			ResourceIDs.STR_CANCEL).toUpperCase());

	private UITextField group_name;

	private Vector groupCBS = new Vector();

	/*
	 * The contact whose groups are to be changed 
	 */
	private Contact contact;

	/**
	 * @param cont 
	 * 
	 */
	public GroupsScreen(Contact cont) {
		super();

		this.contact = cont;

		this.setTitle(rm.getString(ResourceIDs.STR_HANDLE_GROUPS));

		setMenu(new UIMenu(""));
		this.getMenu().append(cmd_submit);
		this.getMenu().append(cmd_cancel);

		UIPanel contactPanel = new UIPanel();
		contactPanel.setMaxHeight(-1);
		contactPanel.setModal(true);
		this.append(contactPanel);

		group_name = new UITextField(rm.getString(ResourceIDs.STR_NEW_GROUP),
				"", 255, TextField.ANY);
		contactPanel.addItem(group_name);

		UISeparator sep = new UISeparator(2);
		sep.setFg_color(0xAAAAAA);
		contactPanel.addItem(sep);

		UILabel existingGroups = new UILabel(rm
				.getString(ResourceIDs.STR_EXISTING_GROUPS));
		contactPanel.addItem(existingGroups);
		UIFont xFont = UICanvas.getInstance().getCurrentScreen().getGraphics()
				.getFont();
		UIFont lFont = UIFont.getFont(xFont.getFace(), UIFont.STYLE_BOLD, xFont
				.getSize());
		existingGroups.setFont(lFont);

		// we should collect all the groups
		// even the groups of the offline contacts
		Roster roster = XMPPClient.getInstance().getRoster();
		Vector groups = new Vector();
		Enumeration en = roster.contacts.elements();
		while (en.hasMoreElements()) {
			Contact ithContact = (Contact) en.nextElement();
			String[] contactGroups = ithContact.getGroups();
			for (int i = 0; i < contactGroups.length; i++) {
				String ithGroup = contactGroups[i];
				boolean virtualGroup = false;
				UIContactGroup uiGroup = UIContactGroup.getContactGroup(
						ithGroup, null, false);
				if (uiGroup != null) virtualGroup = uiGroup.virtualGroup;
				if (virtualGroup == false && groups.contains(ithGroup) == false) groups
						.addElement(ithGroup);
			}
		}
		en = groups.elements();
		while (en.hasMoreElements()) {
			String ithGroup = (String) en.nextElement();
			UICheckbox ithCheckbox = new UICheckbox(ithGroup);
			contactPanel.addItem(ithCheckbox);
			this.groupCBS.addElement(ithCheckbox);
			for (int i = 0; i < contact.getGroups().length; i++) {
				if (contact.getGroups()[i].equals(ithGroup)) {
					ithCheckbox.setChecked(true);
				}
			}
		}

		contactPanel.setSelectedItem(this.group_name);
		this.setSelectedItem(contactPanel);
	}

	public void menuAction(UIMenu menu, UIItem c) {
		if (c == cmd_submit) {
			Vector newGroups = new Vector();
			String groupName = this.group_name.getText();
			if (groupName.length() > 0) newGroups.addElement(groupName);

			Enumeration en = this.groupCBS.elements();
			while (en.hasMoreElements()) {
				UICheckbox ithGroup = (UICheckbox) en.nextElement();
				if (ithGroup.isChecked()) newGroups.addElement(ithGroup
						.getText());
			}

			String server = Contact.domain(contact.jid);
			if (XMPPClient.getInstance().getRoster().distrRosters
					.containsKey(server) == false) {
				Iq iq_roster = new Iq(null, Iq.T_SET);
				Element query = iq_roster.addElement(
						XmppConstants.NS_IQ_ROSTER, Iq.QUERY);
				Element item = query.addElement(XmppConstants.NS_IQ_ROSTER,
						"item");
				item.setAttribute("jid", this.contact.jid);
				if (contact.name.length() > 0) {
					item.setAttribute("name", contact.name);
				}
				XMPPClient xmppClient = XMPPClient.getInstance();
				iq_roster.send(xmppClient.getXmlStream(), null);

				iq_roster = new Iq(null, Iq.T_SET);
				iq_roster.addElement(query);

				en = newGroups.elements();
				while (en.hasMoreElements()) {
					item.addElement(XmppConstants.NS_IQ_ROSTER, "group")
							.addText((String) en.nextElement());
				}

				iq_roster.send(xmppClient.getXmlStream(), null);
			} else {
				en = newGroups.elements();
				String[] groups = new String[newGroups.size()];
				newGroups.copyInto(groups);
				contact.setGroups(groups);
				XMPPClient.getInstance().getRoster().subscribeContact(contact,
						true);
			}
			UICanvas.getInstance().close(this);
		} else if (c == cmd_cancel) {
			UICanvas.getInstance().close(this);
		}
	}
}
