/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: GrpMessageComposerScreen.java 1858 2009-10-16 22:42:29Z luca $
*/

package lampiro.screens;

import java.util.Enumeration;

import lampiro.screens.rosterItems.UIContact;
import lampiro.screens.rosterItems.UIContactGroup;
import it.yup.ui.UICanvas;
import it.yup.ui.UIItem;
import it.yup.ui.UIMenu;
import it.yup.util.ResourceIDs;
import it.yup.xmpp.Contact;
import it.yup.client.XMPPClient;
import it.yup.xmpp.packets.Message;

/**
 * @author luca
 *
 */
public class GrpMessageComposerScreen extends MessageComposerScreen {

	/*
	 * The group to send the message to 
	 */
	private UIContactGroup uiGroup;

	/**
	 * @param user
	 * @param preferredResource
	 * @param default_type
	 */
	public GrpMessageComposerScreen(UIContactGroup group, int default_type) {
		super(XMPPClient.getInstance().getMyContact(), "", default_type);
		this.uiGroup = group;
		setTitle(rm.getString(ResourceIDs.STR_MESSAGE_TO) + " " + group.name);
	}

	public void menuAction(UIMenu menu, UIItem cmd) {
		if (cmd == cmd_send) {
			Message msg;
			Enumeration en = this.uiGroup.contacts.elements();
			String body = tf_body.getText();
			if (body == null) body = "";
			XMPPClient xmppClient = XMPPClient.getInstance();
			msg = compileMessage("fake@j.id");

			while (en.hasMoreElements()) {
				UIContact ithUIContact = (UIContact) en.nextElement();
				Contact ithContact = ithUIContact.c;

				String to = ithContact.jid;
				msg.setAttribute(Message.ATT_TO, to);

				msg.setBody(body);
				xmppClient.sendPacket(msg);
				ithContact.addMessageToHistory(null, msg);
			}
			UICanvas.getInstance().close(this);
		} else {
			super.menuAction(menu, cmd);
		}
	}

}
