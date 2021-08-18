/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UIRosterItem.java 1858 2009-10-16 22:42:29Z luca $
 */
package lampiro.screens.rosterItems;

import java.util.Vector;

import lampiro.screens.RosterScreen;

import it.yup.ui.UIAccordion;
import it.yup.ui.UIConfig;
import it.yup.ui.UIHLayout;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UILayout;
import it.yup.ui.UIMenu;
import it.yup.ui.UIScreen;
import it.yup.ui.UISeparator;
import it.yup.ui.UIUtils;
import it.yup.ui.wrappers.UIFont;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.client.XMPPClient;

public class UIRosterItem extends UIHLayout {

	/*
	 * the contextual menu data associated to an item
	 */
	public static UIMenu optionsMenu = null;
	public static UIAccordion optionsAccordion = null;
	public static UILabel optionsLabel = null;
	public static Vector optionsVector = null;
	public static XMPPClient xmppClient = XMPPClient.getInstance();
	
	/*
	 * When using a touchscreen it is necessery to have a minHeight
	 */
	public static int minHeight=42;

	/*
	 * the image to be shown on the left
	 */
	protected UILabel statusLabel = new UILabel("");

	/*
	 * The text to be shown on the right
	 */
	protected UILabel contactLabel = new UILabel("");

	public static UIMenu actionsMenu;
	public static UILabel actionsLabel;

	static ResourceManager rm = ResourceManager.getManager();

	protected UISeparator sep = new UISeparator(1);

	public static int contactSelectedColor = UIUtils.colorize(
			UIConfig.header_bg, +20);

	protected static int sepColor = 0x00CCCCCC;

	static {
		actionsMenu = new UIMenu("");
		actionsLabel = new UILabel(rm.getString(ResourceIDs.STR_OPTIONS));
		actionsMenu.append(actionsLabel);
	}

	public UIRosterItem() {
		super(2);
		// the correct width for this img is set below !!!
		super.insert(statusLabel, 0, 0, UILayout.CONSTRAINT_PIXELS);
		super.insert(contactLabel, 1, 100, UILayout.CONSTRAINT_PERCENTUAL);
		sep.setFg_color(sepColor);
		contactLabel.setFocusable(true);
		this.setFocusable(true);
		this.setGroup(false);
		this.screen = RosterScreen.getInstance();
		this.setSubmenu(UIContact.actionsMenu);
	}

	public int getHeight(UIGraphics g) {
		int superHeight = super.getHeight(g);
		this.height = superHeight + sep.getHeight(g);
		return this.height;
	}

	protected void paint(UIGraphics g, int w, int h) {
		g.setColor(getBg_color() >= 0 ? getBg_color() : UIConfig.bg_color);
		//int statusLabelWidth = statusLabel.getImg().getWidth();
		g.fillRect(0, 0, w, h);
		int yoffset = super.getHeight(g);
		int oldStatusLabelColor = statusLabel.getBg_color();
		int oldContactFontColor = contactLabel.getFg_color();
		int oldContactSelectedColor = contactLabel.getSelectedColor();
		UIFont oldFont = contactLabel.getFont();
		if (this.isSelected()) {
			statusLabel.setBg_color(contactSelectedColor);
			contactLabel.setSelectedColor(contactSelectedColor);
			contactLabel.setFg_color(UIConfig.menu_title);
			UIFont lFont = UIFont.getFont(UIConfig.font_body.getFace(),
					UIFont.STYLE_BOLD, UIConfig.font_body.getSize());
			contactLabel.setFont(lFont);
		}
		super.paint(g, w, yoffset);
		statusLabel.setBg_color(oldStatusLabelColor);
		contactLabel.setFg_color(oldContactFontColor);

		// to be sure to leave the space to paint subclasses items
		int sepHeight = sep.getHeight(g);
		g.translate(0, h - sepHeight);
		if (this.isSelected()) {
			// if I am selected use another color
			sep.setFg_color(UIConfig.bb_color);
		} else {
			sep.setFg_color(sepColor);
		}

		sep.paint0(g, w, sepHeight);
		g.translate(0, -h);
		if (this.isSelected()) {
			contactLabel.setFont(oldFont);
			contactLabel.setSelectedColor(oldContactSelectedColor);
			g.translate(0, 1);
			sep.paint0(g, w, sepHeight);
			g.translate(0, -1);
		}
		UIScreen cs = this.getScreen();
		if (cs != null) {
			cs.removePaintedItem(statusLabel);
			cs.removePaintedItem(contactLabel);
		}
	}

	public void executeAction() {
		// TODO Auto-generated method stub

	}

	public UIItem getSelectedItem() {
		// i want to return myself and not the selected label!
		return this;
	}

	public String getText() {
		return this.contactLabel.getText();
	}

}
