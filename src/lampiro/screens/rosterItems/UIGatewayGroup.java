/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UIGatewayGroup.java 1858 2009-10-16 22:42:29Z luca $
 */
package lampiro.screens.rosterItems;

import lampiro.screens.GatewayScreen;
import lampiro.screens.RosterScreen;

import it.yup.ui.UIAccordion;
import it.yup.ui.UICanvas;
import it.yup.ui.UIMenu;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.ui.wrappers.UIImage;
import it.yup.util.ResourceIDs;
import it.yup.xmpp.Contact;

public class UIGatewayGroup extends UIContactGroup {

	class UIRegisterContact extends UIRosterItem {

		UIRegisterContact() {
			super();
			UIImage transpImg = UICanvas.getUIImage("/transport/transport.png");
			this.statusLabel.setImg(transpImg);
			this.contactLabel.setText(rm
					.getString(ResourceIDs.STR_GATEWAY_DISCOVERY));
			statusLabel.setLayoutWidth(transpImg.getWidth());
		}

		public void executeAction() {
			UICanvas.getInstance().open(new GatewayScreen(null), true,
					RosterScreen.getInstance());
		}

		public int getHeight(UIGraphics g) {
			this.height = super.getHeight(g);
			if (this.height < UIRosterItem.minHeight
					&& UICanvas.getInstance().hasPointerEvents()) {
				this.height = UIRosterItem.minHeight;
			}
			return this.height;
		}
	}

	private UIRegisterContact registerContact = new UIRegisterContact();

	protected UIGatewayGroup(UIAccordion accordion) {
		super(UIGroup.NETWORKS, accordion, UIGroup.END);
		this.virtualGroup = true;
		accordion.insertPanelItem(this, registerContact, 0);
		updateColors();
	}

	public static UIGatewayGroup getGroup(UIAccordion accordion,
			boolean allocate) {
		return (UIGatewayGroup) UIContactGroup.getGroup(NETWORKS, accordion,
				allocate);
	}

	public boolean updateContact(Contact c, int reason) {
		accordion.removePanelItem(this, registerContact);
		super.updateContact(c, reason);

		// the group has been deleted!!!
		if (accordion.getSubpanel(this) == null) {
			getGroup(accordion, true);
		} else {
			accordion.insertPanelItem(this, registerContact, 0);
		}
		return true;
	}

	protected boolean showUIContact(Contact c) {
		// gateways are always to be shown
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
}
