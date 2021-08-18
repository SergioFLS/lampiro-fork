/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UIServices.java 1858 2009-10-16 22:42:29Z luca $
 */
package lampiro.screens.rosterItems;

import lampiro.screens.RosterScreen;
import it.yup.ui.UIAccordion;
import it.yup.ui.UIMenu;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.xmpp.Contact;

public class UIServices extends UIContactGroup {

	private static ResourceManager rm = ResourceManager.getManager();

	protected UIServices(UIAccordion accordion) {
		super(rm.getString(ResourceIDs.STR_SERVICES), accordion, UIGroup.END);

		this.virtualGroup = true;
		updateColors();
	}

	public static UIContactGroup getGroup(UIAccordion accordion,
			boolean allocate) {
		return (UIContactGroup) UIGroup.getGroup(rm
				.getString(ResourceIDs.STR_SERVICES), accordion, allocate);
	}

	protected boolean showUIContact(Contact c) {
		// services are always to be shown
		return true;
	}

	public UIMenu openGroupMenu() {
		RosterScreen rs = RosterScreen.getInstance();
		rs.setFreezed(true);
		UIMenu superMenu = super.openGroupMenu();
		superMenu.remove(UIContactGroup.groupMessage);
		superMenu.remove(UIContactGroup.chgGroupName);
		rs.setFreezed(false);
		askRepaint();
		return superMenu;
	}

	public boolean updateContact(Contact c, int reason) {
		boolean needRepaint = false;
		UIContact uic = (UIContact) this.contacts.get(c);
		RosterScreen rs = RosterScreen.getInstance();
		// used to know if need rePaint
		int oldAccordionSize = accordion.getPanelSize(this);
		boolean needReinsert = (uic == null);

		// XXX check if something better
		if (uic != null || c.isVisible() || rs.isShow_offlines()) {
			// reinsert if it is visible
			if (showUIContact(c)) {
				if (needReinsert) {
					uic = UIContactGroup.createUIContact(c);
					this.contacts.put(c, uic);
					needRepaint = true;
					accordion.insertPanelItem(this, uic, 0);
				}
				needRepaint |= uic.updateContactData();
			}
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
				&& rs.getAccordion() == rs.searchAccordion)
			needRepaint |= rs.filterContacts(true);
		int newAccordionSize = accordion.getPanelSize(this);
		if (newAccordionSize == 0) {
			accordion.removeItem(this);
			uiGroups.remove(this.getText());
			if (oldAccordionSize != 0)
				needRepaint = true;
		}
		return needRepaint;
	}
}
