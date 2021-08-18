/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UIGateway.java 1858 2009-10-16 22:42:29Z luca $
 */
package lampiro.screens.rosterItems;

import java.io.IOException;
import java.util.Hashtable;

import lampiro.screens.RosterScreen;

import it.yup.ui.UICanvas;
import it.yup.ui.UILabel;
import it.yup.ui.UIUtils;
import it.yup.ui.wrappers.UIImage;
import it.yup.util.ResourceIDs;
import it.yup.xmpp.Contact;

public class UIGateway extends UIContact {

	public static UILabel cmd_log = new UILabel(rm
			.getString(ResourceIDs.STR_LOG));

	public static UILabel cmd_remove_network = new UILabel(rm
			.getString(ResourceIDs.STR_REMOVE_NETWORK));

	private static Hashtable gatewayIcons = new Hashtable();
	private String gatewayType;

	public UIGateway(Contact c, String gatewayType) {
		super(c);
		this.gatewayType = gatewayType;
		this.updateContactData();
		// TODO Auto-generated constructor stub
	}

	public static UIImage getGatewayIcons(String type) {
		if (type == null)
			return null;
		UIImage retIcon = (UIImage) gatewayIcons.get(type);
		if (retIcon != null)
			return retIcon;

		try {
			retIcon = UIImage.createImage("/transport/" + type + ".png");
		} catch (IOException ex) {
			try {
				retIcon = UIImage.createImage("/transport/transport.png");
			} catch (IOException e1) {
				// TODO Auto-generated
				// catch block
				e1.printStackTrace();
			}
		}
		gatewayIcons.put(type, retIcon);
		return retIcon;
	}

	protected UIImage getPresenceIcon() {
		if (this.c.getAllPresences() != null)
			return getGatewayIcons(this.gatewayType);
		else
			return UIContact.getPresenceIcon(c, null, Contact.AV_UNAVAILABLE);
	}

	public void openContactMenu() {
		Contact c = this.c;
		RosterScreen rs = RosterScreen.getInstance();

		if (c != null) {
			optionsMenu = UIUtils.easyMenu(c.getPrintableName(), 10, (this)
					.getSubmenu().getAbsoluteY(), UICanvas.getInstance()
					.getWidth() - 20, null);
			optionsMenu.setAutoClose(false);
			optionsAccordion = null;

			optionsMenu.setStatus(c.jid);
			optionsMenu.append(cmd_log);
			optionsMenu.append(cmd_remove_network);
			optionsMenu.append(cmd_querycmd);
			rs.addPopup(optionsMenu);
		}
	}
}
