/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UIContactGroup.java 1858 2009-10-16 22:42:29Z luca $
*/
package lampiro.screens.rosterItems;

import it.yup.ui.UIAccordion;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UIMenu;
import it.yup.util.ResourceIDs;


import it.yup.xml.Element;
import it.yup.xmpp.Contact;
import it.yup.xmpp.MUC;
import it.yup.client.XMPPClient;
import it.yup.xmpp.XmppConstants;
import it.yup.xmpp.packets.Iq;

import java.util.Hashtable;
import java.util.Vector;

import lampiro.screens.RosterScreen;
import lampiro.screens.rosterItems.UIContact;

public class UIContactGroup extends UIGroup {
	public Hashtable contacts = new Hashtable();

	public static UILabel groupMessage = new UILabel(rm
			.getString(ResourceIDs.STR_SEND_GRP_MSG));
	public static UILabel chgGroupName = new UILabel(rm
			.getString(ResourceIDs.STR_CHG_GROUP_NAME));
	public static UILabel createMUC = new UILabel(rm
			.getString(ResourceIDs.STR_CREATE_GROUP_CHAT));

	// private boolean moving = false;

	protected UIContactGroup(String groupName, UIAccordion accordion,
			int constraint) {
		super(groupName, accordion, constraint);
	}

	public void removeContact(Contact c) {
		UIItem uic = (UIItem) this.contacts.remove(c);
		if (uic != null) accordion.removePanelItem(this, uic);
		if (accordion.getPanelSize(this) == 0) {
			accordion.removeItem(this);
			uiGroups.remove(this.name);
		}
	}

	public boolean updateContact(Contact c, int reason) {
		boolean needRepaint = false;
		UIContact uic = (UIContact) this.contacts.get(c);
		RosterScreen rs = RosterScreen.getInstance();
		// used to know if need rePaint
		int oldAccordionSize = accordion.getPanelSize(this);
		boolean needReinsert = (uic == null ? true : checkRemoval(uic));

		if (uic != null) {
			if (needReinsert) {
				needRepaint = true;
				accordion.removePanelItem(this, uic);
			}
			// if the status is changed we could need
			//to update the subscription
			//if (reason == Contact.CH_STATUS) uic.checkSubscription();
		}

		if (uic != null || c.isVisible() || rs.isShow_offlines()) {
			// reinsert if it is visible
			int i = 0;
			if (showUIContact(c)) {
				if (needReinsert) {
					if (uic == null) {
						uic = UIContactGroup.createUIContact(c);
						this.contacts.put(c, uic);
					}
					needRepaint = true;
					int min = 0;
					int max = accordion.getPanelSize(this);
					int med = 0;
					while (min != max) {
						med = (min + max) / 2;
						UIContact ithContact = (UIContact) accordion
								.getPanelItem(this, med);
						if (compare(uic, ithContact) < 0) min = med + 1;
						else
							max = med;
					}
					i = min;
					accordion.insertPanelItem(this, uic, i);
				}
				needRepaint |= uic.updateContactData();
			}

			//			if ((reason == Contact.CH_MESSAGE_NEW
			//					|| reason == Contact.CH_TASK_NEW || c.unread_msg() || c.pending_tasks)
			//					&& UIContactGroup.movingGroup == null) {
			//				// set the correct selection to the just updated task
			//				needRepaint = true;
			//				accordion.openLabel(this);
			//				accordion.setSelectedItem(uic);
			//			}
		}

		if (showUIContact(c) == false) {
			// if the contact is not visible remove it
			if (uic != null) {
				accordion.removePanelItem(this, uic);
				needRepaint = true;
			}
			this.contacts.remove(c);
		}

		if (rs.isFiltering() == false
				&& rs.getAccordion() == rs.searchAccordion) needRepaint |= rs
				.filterContacts(true);
		int newAccordionSize = accordion.getPanelSize(this);
		if (newAccordionSize == 0) {
			accordion.removeItem(this);
			uiGroups.remove(this.getText());
			if (oldAccordionSize != 0) needRepaint = true;
		}
		return needRepaint;
	}

	private int compare(UIContact left, UIContact right) {
		return left.c.compareTo(right.c);
	}

	protected boolean showUIContact(Contact c) {
		RosterScreen rs = RosterScreen.getInstance();
		return c.isVisible() || rs.isShow_offlines()
				|| RosterScreen.chatScreenList.contains(c.jid);
	}

	private boolean checkRemoval(UIContact uic) {
		Vector v = accordion.getSubpanel(this);
		if (v == null) return false;
		int index = v.indexOf(uic);
		if (index > 0) {
			UIContact previousItem = ((UIContact) v.elementAt(index - 1));
			if (uic.c.compareTo(previousItem.c) > 0) return true;
		}
		if (index < v.size() - 1) {
			UIContact nextItem = ((UIContact) v.elementAt(index + 1));
			if (uic.c.compareTo(nextItem.c) < 0) return true;
		}
		return false;
	}

	public UIContact getUIContact(Contact c) {
		// TODO Auto-generated method stub
		return (UIContact) this.contacts.get(c);
	}

	public static UIContactGroup getContactGroup(String group,
			UIAccordion accordion, boolean allocate) {
		return (UIContactGroup) UIGroup.getGroup(group, accordion, allocate);
	}

	public UIMenu openGroupMenu() {
		RosterScreen rs = RosterScreen.getInstance();
		boolean oldFreezed = rs.isFreezed();
		rs.setFreezed(true);
		UIMenu superMenu = super.openGroupMenu();
		if (this.name.equals(MUC.GROUP_CHATS) == false) {
			superMenu.append(UIContactGroup.groupMessage);
			if (this.name.equals(ungrouped) == false
					&& this.name.equals(highLightString) == false) {
				superMenu.append(UIContactGroup.chgGroupName);
			}
		} else
			superMenu.append(UIContactGroup.createMUC);
		rs.setFreezed(oldFreezed);
		askRepaint();
		return superMenu;
	}

	protected static UIContact createUIContact(Contact c) {
		Element gatewayData = (Element) XMPPClient.getInstance().getRoster().registeredGateways
				.get(c.jid);
		UIContact uic = null;
		if (gatewayData != null) {
			Element identity = gatewayData.getPath(new String[] {
					XmppConstants.NS_IQ_DISCO_INFO,
					XmppConstants.NS_IQ_DISCO_INFO }, new String[] { Iq.QUERY,
					XmppConstants.IDENTITY });
			String type = identity.getAttribute("type");
			uic = new UIGateway(c, type);
		}
		else {
			uic = new UIContact(c);
		}
		return uic;
	}
}
