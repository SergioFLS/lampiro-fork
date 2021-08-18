/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: ChgGroupNameScreen.java 1858 2009-10-16 22:42:29Z luca $
*/

package lampiro.screens;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.TextField;

import lampiro.screens.rosterItems.UIContactGroup;
import it.yup.ui.UIButton;
import it.yup.ui.UICanvas;
import it.yup.ui.UIHLayout;
import it.yup.ui.UIItem;
import it.yup.ui.UILayout;
import it.yup.ui.UIPanel;
import it.yup.ui.UIScreen;
import it.yup.ui.UITextField;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.xml.Element;
import it.yup.xmpp.Contact;
import it.yup.xmpp.Group;
import it.yup.xmpp.XmppConstants;

import it.yup.client.XMPPClient;
import it.yup.xmpp.packets.Iq;

/**
 * @author luca
 *
 */
public class ChgGroupNameScreen extends UIScreen {

	private static ResourceManager rm = ResourceManager.getManager();

	private UIPanel groupPanel = new UIPanel();
	private UITextField utf = new UITextField(rm
			.getString(ResourceIDs.STR_GROUP), "", 100, TextField.ANY);

	private UIButton submit = new UIButton(rm.getString(ResourceIDs.STR_SUBMIT));
	private UIButton cancel = new UIButton(rm.getString(ResourceIDs.STR_CANCEL));

	private UIContactGroup uiGroup;

	/**
	 * @param group 
	 * 
	 */
	public ChgGroupNameScreen(UIContactGroup group) {
		super();
		this.setTitle(rm.getString(ResourceIDs.STR_CHG_GROUP_NAME));
		groupPanel.setMaxHeight(-1);
		groupPanel.setModal(true);
		groupPanel.setFocusable(true);
		this.append(groupPanel);

		groupPanel.addItem(utf);

		UIHLayout buttonLayout = new UIHLayout(2);
		buttonLayout.setGroup(false);
		buttonLayout.insert(cancel, 0, 50, UILayout.CONSTRAINT_PERCENTUAL);
		buttonLayout.insert(submit, 1, 50, UILayout.CONSTRAINT_PERCENTUAL);
		groupPanel.addItem(buttonLayout);

		this.uiGroup = group;
		utf.setText(group.name);
	}

	public void itemAction(UIItem item) {
		if (item == submit) {
			Vector v = new Vector();
			Group g = Group.getGroup(uiGroup.name);
			Enumeration en = g.getContacts();
			while (en.hasMoreElements()) {
				v.addElement(XMPPClient.getInstance().getRoster()
						.getContactByJid((String) en.nextElement()));
			}
			en = v.elements();
			XMPPClient xmppClient = XMPPClient.getInstance();
			while (en.hasMoreElements()) {
				Contact c = (Contact) en.nextElement();
				Iq iq = new Iq(null, Iq.T_SET);
				Element query = iq
						.addElement(XmppConstants.NS_IQ_ROSTER, Iq.QUERY);
				Element itemEl = query.addElement(null, XmppConstants.ITEM);
				itemEl.setAttribute("jid", c.jid);
				if (c.name != null) itemEl.setAttribute("name", c.name);
				for (int i = 0; i < c.getGroups().length; i++) {
					String gString = c.getGroups()[i];
					if (gString.equals(this.uiGroup.name)) {
						gString = this.utf.getText();
					}
					itemEl.addElement(null, "group").addText(gString);
				}
				iq.send(xmppClient.getXmlStream(),null);
			}
			UICanvas.getInstance().close(this);
		}
	}
}
