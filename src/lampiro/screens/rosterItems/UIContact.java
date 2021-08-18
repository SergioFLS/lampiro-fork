/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: VCardManager.java 1858 2009-10-16 22:42:29Z luca $
 */
package lampiro.screens.rosterItems;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import it.yup.ui.UIAccordion;
import it.yup.ui.UICanvas;
import it.yup.ui.UIConfig;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UIMenu;
import it.yup.ui.UIScreen;
import it.yup.ui.UIUtils;
import it.yup.ui.wrappers.UIFont;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.ui.wrappers.UIImage;
import it.yup.util.ResourceIDs;
import it.yup.xmpp.Contact;
import it.yup.xmpp.MUC;
import it.yup.xmpp.XmppConstants;
import it.yup.xmpp.packets.Iq;
import it.yup.xmpp.packets.Message;
import it.yup.xmpp.packets.Presence;

import lampiro.screens.AlbumScreen;
import lampiro.screens.RosterScreen;

public class UIContact extends UIRosterItem {

	public static UIImage img_msg = UICanvas.getUIImage("/icons/message.png");
	public static UIImage img_cmd = UICanvas.getUIImage("/icons/gear.png");
	public static UIImage img_task = UICanvas.getUIImage("/icons/task.png");

	private static UIImage subscriptionTo = UIMenu.menuImage;
	private static UIImage subscriptionFrom;

	public static UILabel cmd_details = new UILabel(rm
			.getString(ResourceIDs.STR_SEE_DETAILS));
	public static UILabel cmd_resend_auth = new UILabel(rm
			.getString(ResourceIDs.STR_RESEND_AUTH));
	public static UILabel cmd_rerequest_auth = new UILabel(rm
			.getString(ResourceIDs.STR_REREQUEST_AUTH));
	public static UILabel cmd_groups = new UILabel(rm
			.getString(ResourceIDs.STR_HANDLE_GROUPS));
	public static UILabel cmd_delc = new UILabel(rm
			.getString(ResourceIDs.STR_DELETE_CONTACT));
	public static UILabel cmd_send = new UILabel(rm
			.getString(ResourceIDs.STR_SEND_MESSAGE));
	public static UILabel cmd_chat = new UILabel(rm
			.getString(ResourceIDs.STR_CHAT));
	public static UILabel cmd_send_file = new UILabel(rm
			.getString(ResourceIDs.STR_SEND_FILE));
	public static UILabel cmd_contact_capture_img = new UILabel(rm
			.getString(ResourceIDs.STR_SEND_IMAGE));
	public static UILabel cmd_contact_capture_aud = new UILabel(rm
			.getString(ResourceIDs.STR_SEND_AUDIO));
	public static UILabel cmd_querycmd = new UILabel(rm
			.getString(ResourceIDs.STR_QUERYCMD));
	public static UILabel cmd_tasks = new UILabel(rm
			.getString(ResourceIDs.STR_PENDINGTASK));
	public static UILabel cmd_close_muc = new UILabel(rm
			.getString(ResourceIDs.STR_CLOSE_MUC));
	public static UILabel cmd_manage_muc = new UILabel(rm
			.getString(ResourceIDs.STR_MANAGE_GC));
	public static UILabel cmd_exit_muc = new UILabel(rm
			.getString(ResourceIDs.STR_EXIT_MUC));
	public static UILabel cmd_enter_muc = new UILabel(rm
			.getString(ResourceIDs.STR_ENTER_MUC));
	public static UILabel cmd_active_sessions = new UILabel(rm
			.getString(ResourceIDs.STR_ACTIVE_SESSIONS)
			+ ":");
	public static UILabel cmd_change_nick = new UILabel(rm
			.getString(ResourceIDs.STR_CHANGE_NICK));

	public static UIImage jabberImg = UICanvas
			.getUIImage("/transport/xmpp.png");

	private static UIImage presence_icons[];
	private static UIImage presence_phone_icons[];

	static {
		// preload the presence icons
		String mapping[] = Contact.availability_mapping;
		presence_icons = new UIImage[mapping.length];
		presence_phone_icons = new UIImage[mapping.length];
		try {
			presence_icons[0] = UIImage.createImage("/icons/presence_"
					+ mapping[1] + ".png");
			presence_phone_icons[0] = UIImage.createImage("/icons/presence_"
					+ mapping[1] + "_phone.png");
			presence_icons[1] = UIImage.createImage("/icons/presence_"
					+ mapping[1] + ".png");
			presence_phone_icons[1] = UIImage.createImage("/icons/presence_"
					+ mapping[1] + "_phone.png");
			presence_icons[2] = UIImage.createImage("/icons/presence_"
					+ mapping[2] + ".png");
			presence_phone_icons[2] = UIImage.createImage("/icons/presence_"
					+ mapping[2] + "_phone.png");
			presence_icons[3] = UIImage.createImage("/icons/presence_"
					+ mapping[3] + ".png");
			presence_phone_icons[3] = UIImage.createImage("/icons/presence_"
					+ mapping[3] + "_phone.png");
			presence_icons[4] = UIImage.createImage("/icons/presence_"
					+ mapping[3] + ".png");
			presence_phone_icons[4] = UIImage.createImage("/icons/presence_"
					+ mapping[3] + "_phone.png");
			presence_icons[5] = UIImage.createImage("/icons/presence_"
					+ mapping[5] + ".png");
			presence_phone_icons[5] = UIImage.createImage("/icons/presence_"
					+ mapping[5] + "_phone.png");
			presence_icons[6] = UIImage.createImage("/icons/muc.png");
			presence_icons[7] = UIImage.createImage("/icons/muc-offline.png");

		} catch (Exception e) {
			// should not happen
		}
	}

	public static UIImage getPresenceIcon(Contact c, String preferredResource,
			int availability) {
		Presence p = null;
		if (c != null) {
			// first check if it is a MUC
			if (c instanceof MUC == true) {
				if (c.getPresence(null) != null) return presence_icons[Contact.MUC_IMG];
				else
					return presence_icons[Contact.MUC_IMG_OFFLINE];
			}
			// next the usual ones
			else if (preferredResource != null) p = c
					.getPresence(preferredResource);
			else
				p = c.getPresence(null);
		}
		if (availability >= 0 && availability < presence_icons.length) {
			if (p == null || p.pType == Presence.PC) {
				return presence_icons[availability];
			} else if (p.pType == Presence.PHONE) { return presence_phone_icons[availability]; }
		}
		return null; // maybe we could return an empty image
	}

	public Contact c;

	/*
	 * The text pertaining the status of the user
	 */
	private UILabel statusText = new UILabel("");

	public static int textLabelSelectedColor = UIUtils.colorize(
			UIConfig.bg_color, -20);
	public static int textLabelFontColor = 0x000000;

	public static Vector extraCommands = new Vector();

	/*
	 * The label showing subscriptions
	 */
	//	private UILabel subLabel = null;
	static {
		try {
			subscriptionFrom = UIImage.createImage("/icons/menuarrow.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public UIContact(Contact c) {
		super();
		this.c = c;
		statusText.setFont(UIFont.getFont(UIFont.FACE_PROPORTIONAL,
				UIFont.STYLE_PLAIN, UIFont.SIZE_SMALL));
		statusText.setFg_color(0xAAAAAA);
		this.updateContactData();
		//checkSubscription();
	}

	//	public void checkSubscription() {
	//		if (c.subscription.equals(Iq.ATT_FROM)) subLabel = new UILabel(
	//				subscriptionFrom);
	//		else if (c.subscription.equals(Iq.ATT_TO)) subLabel = new UILabel(
	//				subscriptionTo);
	//		else
	//			subLabel = null;
	//	}

	public boolean updateContactData() {
		boolean needRepaint = false;
		String uname = c.getPrintableName();
		UIImage pimg = null;

		pimg = getPresenceIcon();
		if (pimg == null) pimg = UIContact.getPresenceIcon(null, null,
				Contact.AV_UNAVAILABLE);
		// setup the status text label
		if (contactLabel.getText().equals(uname) == false) {
			needRepaint = true;
			this.contactLabel.setText(uname);
		}
		String fixedStatus = "";
		if (this.c instanceof MUC == false) {
			String status = null;
			Presence[] resources = c.getAllPresences();
			if (resources != null && resources.length > 0) {
				status = resources[0].getStatus();
			}
			fixedStatus = status != null ? status : "";
		} else {
			fixedStatus = ((MUC) c).topic;
		}
		if (statusText.getText().equals(fixedStatus) == false) {
			needRepaint = true;
			this.statusText.setText(fixedStatus);
		}

		// if a message has arrived the icons replaces the normal ones
		if (c.unread_msg()) pimg = img_msg;

		if (this.statusLabel.getImg() != pimg) {
			needRepaint = true;
			this.statusLabel.setImg(pimg);
			statusLabel.setLayoutWidth(pimg.getWidth());
		}
		chooseInfoImgs();
		this.setDirty(needRepaint);
		return needRepaint;
	}

	protected UIImage getPresenceIcon() {
		return UIContact.getPresenceIcon(c, null, c.getAvailability());
	}

	public int getHeight(UIGraphics g) {
		this.height = super.getHeight(g);
		// a minimum width in case it is 0 (and hence not painted yet)

		int infoHeight = 0;
		int infoWidth = 2;
		if ((this.isSelected() || UICanvas.getInstance().hasPointerEvents())
				&& infoImgs.size() > 0) {
			Enumeration en = this.infoImgs.elements();
			while (en.hasMoreElements()) {
				UIImage object = (UIImage) en.nextElement();
				infoWidth += object.getWidth();
				if (object.getHeight() > infoHeight) infoHeight = object
						.getHeight();
			}
			// 4 is for the padding
			infoHeight += 4;
		}

		int minWidth = RosterScreen.getInstance().getWidth() - 25 - infoWidth;
		int statusHeight = 0;
		if (this.isSelected() && minWidth > 25) this.statusText.setWrappable(
				true, minWidth);
		else
			this.statusText.setWrappable(false, -1);
		if (this.statusText.getText().length() > 0) {
			// 4 is for the padding
			statusHeight = statusText.getHeight(g) + 4;
		}

		this.height += Math.max(statusHeight, infoHeight);
		return this.height;
	}

	private void chooseInfoImgs() {
		infoImgs.removeAllElements();

		//subscriptions
		if (c.subscription.equals(Iq.ATT_FROM)) {
			infoImgs.addElement(subscriptionFrom);
		} else if (c.subscription.equals(Iq.ATT_TO)) {
			infoImgs.addElement(subscriptionTo);
		}

		// contact type
		Hashtable gws = RosterScreen.getInstance().getGateways();
		Enumeration en = gws.keys();
		boolean found = false;
		while (en.hasMoreElements()) {
			String from = (String) en.nextElement();
			if (c.jid.indexOf(from) > 0) {
				Object[] data = (Object[]) gws.get(from);
				infoImgs.addElement((UIImage) UIGateway
						.getGatewayIcons((String) data[1]));
				found = true;
				break;
			}
		}
		if (found == false) {
			infoImgs.addElement(jabberImg);
		}

		Presence[] aps = c.getAllPresences();
		if (aps != null && aps.length > 0 && RosterScreen.supportsMUC(aps[0])) {
			infoImgs.addElement(UIContact.getPresenceIcon(null, null,
					Contact.MUC_IMG));
		}

		// command
		if (c.pending_tasks) infoImgs.addElement(img_task);
		if (c.cmdlist != null) infoImgs.addElement(img_cmd);
	}

	private Vector infoImgs = new Vector();

	protected void paint(UIGraphics g, int w, int h) {
		int yoffset = super.getHeight(g);
		int statusLabelWidth = statusLabel.getImg().getWidth();
		super.paint(g, w, h);
		// an additional offset if the subscription has been painted
		g.translate(0, yoffset);
		//		int orX = g.getTranslateX();
		//		int orY = g.getTranslateY();

		int infoHeight = 0;
		int infoWidth = 2;
		if ((this.isSelected() || UICanvas.getInstance().hasPointerEvents())
				&& infoImgs.size() > 0) {
			Enumeration en = this.infoImgs.elements();
			while (en.hasMoreElements()) {
				UIImage object = (UIImage) en.nextElement();
				infoWidth += object.getWidth();
				if (object.getHeight() > infoHeight) infoHeight = object
						.getHeight();
			}
			if (this.isSelected()) {
				g.setColor(textLabelSelectedColor);
			}
			g.fillRect(0, 0, w, h - yoffset);
			g.translate(statusLabelWidth, 2);
			if (this.isSelected()) {
				g.setColor(getBg_color() >= 0 ? getBg_color()
						: UIConfig.bg_color);
			}
			en = infoImgs.elements();
			while (en.hasMoreElements()) {
				UIImage ithImg = (UIImage) en.nextElement();
				paintIthImage(g, ithImg);
			}
		} else
			g.translate(statusLabelWidth, 2);

		//		g.translate(orX - g.getTranslateX() + statusLabelWidth, orY
		//				- g.getTranslateY() + infoHeight);
		g.translate(2, 0);

		if (this.statusText.getText().length() > 0) {
			// +4 is for the padding
			int statusTextHeight = statusText.getHeight(g) + 2;
			int oldStatusColor = statusText.getBg_color();
			int oldStatusFontColor = statusText.getFg_color();
			if (isSelected()) {
				//				int oldColor = g.getColor();
				//				g.setColor(textLabelSelectedColor);
				//				g.fillRect(-statusLabelWidth, 0, statusLabelWidth,
				//						statusTextHeight);
				//				g.setColor(oldColor);
				statusText.setBg_color(textLabelSelectedColor);
				statusText.setFg_color(textLabelFontColor);
			}
			statusText.paint0(g, w - statusLabelWidth - infoWidth,
					statusTextHeight);
			statusText.setBg_color(oldStatusColor);
			statusText.setFg_color(oldStatusFontColor);
		}
		//                      // Remove these elements because the pointerPressed must 
		//                      // find the UIContact 
		UIScreen cs = this.getScreen();
		if (cs != null) {
			cs.removePaintedItem(sep);
			cs.removePaintedItem(statusText);
		}
	}

	/**
	 * @param g
	 * @param ithImg
	 */
	private void paintIthImage(UIGraphics g, UIImage ithImg) {
		if (ithImg != null) {
			g.drawImage(ithImg, 0, 0, UIGraphics.TOP | UIGraphics.LEFT);
			g.translate(ithImg.getWidth(), 0);
		}
	}

	public void executeAction() {
		RosterScreen rs = RosterScreen.getInstance();
		Contact c = this.c;
		if (rs.getSelectedContact() != c) {
			rs.rosterAccordion.setSelectedItem(this);
		}
		if (c != null) {
			if (c.unread_msg()) {
				// at this manner the loop is made to all the resources
				// even the offline ones
				Vector allConvs = c.getAllConvs();
				Enumeration en = allConvs.elements();
				while (en.hasMoreElements()) {
					Object[] coupleConv = (Object[]) en.nextElement();
					String ithRes = (String) coupleConv[0];
					Vector messages = (Vector) coupleConv[1];
					if (messages.size() > 0) {
						rs.chatWithSelected(ithRes);
						return;
					}
				}
			}
			// join a muc 
			if (c instanceof MUC
					&& (c.getAllPresences() == null || c.getAllPresences().length == 0)) {
				if (RosterScreen.isOnline()) {
					rs.enterMuc(c.jid);
					return;
				}
			}

			Presence presence = c.getPresence(null);
			String toJid = (presence != null ? presence
					.getAttribute(Message.ATT_FROM) : c.jid);
			rs.chatWithSelected(toJid);
		}
	}

	public void openContactMenu() {
		if (UICanvas.getInstance().hasPointerMotionEvents()) openContactMenuTouch();
		else
			openContactMenuKey();
	}

	private void openContactMenuTouch() {
		Contact c = this.c;
		RosterScreen rs = RosterScreen.getInstance();
		extraCommands.removeAllElements();
		if (c != null) {
			boolean isOnline = RosterScreen.isOnline();
			boolean isMuc = c instanceof MUC;
			optionsMenu = UIUtils.easyMenu(c.getPrintableName(), 10, (this)
					.getSubmenu().getAbsoluteY(), UICanvas.getInstance()
					.getWidth() - 20, null);
			optionsMenu.setAutoClose(false);
			optionsAccordion = null;
			Presence[] res = c.getAllPresences();
			if (res != null && res.length > 1 && isMuc == false) {
				if (isOnline) {
					optionsMenu.append(cmd_details);
					optionsMenu.append(cmd_change_nick);
					optionsMenu.append(cmd_groups);
					optionsMenu.append(cmd_resend_auth);
					optionsMenu.append(cmd_rerequest_auth);
					optionsMenu.append(cmd_delc);
				}
				for (int i = 0; i < res.length; i++) {

					String resString = null;
					resString = Contact.resource(res[i]
							.getAttribute(Iq.ATT_FROM));
					if (resString == null) resString = res[i]
							.getAttribute(Iq.ATT_FROM);
					UIImage img = UIContact.getPresenceIcon(c, res[i]
							.getAttribute(Iq.ATT_FROM), c.getAvailability());
					optionsLabel = new UILabel(img, resString);
					optionsLabel.setWrappable(true, UICanvas.getInstance()
							.getWidth() - 30);
					String fullJid = res[i].getAttribute(Message.ATT_FROM);
					optionsLabel.setStatus(fullJid);
					optionsLabel.setAnchorPoint(UIGraphics.HCENTER);
					optionsMenu.append(optionsLabel);
					optionsLabel.setBg_color(UIConfig.header_bg);
					optionsLabel.setFg_color(UIConfig.menu_title);
					optionsLabel.setFocusable(false);
					UILabel ithLabel = new UILabel(cmd_chat);
					extraCommands.addElement(new Object[] { ithLabel, cmd_chat,
							fullJid });
					optionsMenu.append(ithLabel);
					if (isOnline) {
						ithLabel = new UILabel(cmd_send);
						extraCommands.addElement(new Object[] { ithLabel,
								cmd_send, fullJid });
						optionsMenu.append(ithLabel);
						if (AlbumScreen.getCount(XmppConstants.IMG_TYPE) > 0
								|| AlbumScreen
										.getCount(XmppConstants.AUDIO_TYPE) > 0) {
							ithLabel = new UILabel(cmd_send_file);
							extraCommands.addElement(new Object[] { ithLabel,
									cmd_send_file, fullJid });
							optionsMenu.append(ithLabel);
						}
						if (rs.cameraOn) {
							ithLabel = new UILabel(cmd_contact_capture_img);
							extraCommands.addElement(new Object[] { ithLabel,
									cmd_contact_capture_img, fullJid });
							optionsMenu.append(ithLabel);
						}
						if (rs.micOn) {
							ithLabel = new UILabel(cmd_contact_capture_aud);
							extraCommands.addElement(new Object[] { ithLabel,
									cmd_contact_capture_aud, fullJid });
							optionsMenu.append(ithLabel);
						}
						ithLabel = new UILabel(cmd_querycmd);
						optionsMenu.append(ithLabel);
						extraCommands.addElement(new Object[] { ithLabel,
								cmd_querycmd, fullJid });
						if (c.pending_tasks) {
							ithLabel = new UILabel(cmd_tasks);
							extraCommands.addElement(new Object[] { ithLabel,
									cmd_tasks, fullJid });
							optionsMenu.append(ithLabel);
						}
					}
				}
				optionsMenu.setSelectedItem(cmd_details);
			} else {
				openContactMenuSingleResource(c, rs, isOnline, isMuc, res);
			}
			rs.addPopup(optionsMenu);
		}
	}

	private void openContactMenuSingleResource(Contact c, RosterScreen rs,
			boolean isOnline, boolean isMuc, Presence[] res) {
		String toRes = (res != null && res.length >= 1 ? res[0]
				.getAttribute(Message.ATT_FROM) : c.jid);
		optionsMenu.setStatus(toRes);
		optionsMenu.append(UIContact.cmd_chat);
		optionsMenu.append(UIContact.cmd_change_nick);
		if (isOnline) {
			optionsMenu.append(UIContact.cmd_send);
			if (AlbumScreen.getCount(XmppConstants.IMG_TYPE) > 0
					|| AlbumScreen.getCount(XmppConstants.AUDIO_TYPE) > 0) {
				optionsMenu.append(UIContact.cmd_send_file);
			}
			if (rs.cameraOn) optionsMenu
					.append(UIContact.cmd_contact_capture_img);
			if (rs.micOn) optionsMenu.append(UIContact.cmd_contact_capture_aud);
		}
		optionsMenu.append(UIContact.cmd_details);
		if (isOnline) {
			optionsMenu.append(UIContact.cmd_resend_auth);
			optionsMenu.append(UIContact.cmd_rerequest_auth);
			optionsMenu.append(UIContact.cmd_groups);
			optionsMenu.append(UIContact.cmd_delc);
			optionsMenu.append(UIContact.cmd_querycmd);
			if (c.pending_tasks) {
				optionsMenu.append(UIContact.cmd_tasks);
			}
		}
		if (isMuc) {
			optionsMenu.remove(UIContact.cmd_delc);
			optionsMenu.remove(UIContact.cmd_resend_auth);
			optionsMenu.remove(UIContact.cmd_rerequest_auth);
			optionsMenu.remove(UIContact.cmd_groups);
			optionsMenu.remove(UIContact.cmd_querycmd);
			optionsMenu.remove(UIContact.cmd_change_nick);
			if (isOnline) {
				optionsMenu.insert(optionsMenu.indexOf(UIContact.cmd_chat) + 1,
						UIContact.cmd_manage_muc);
				optionsMenu.insert(optionsMenu
						.indexOf(UIContact.cmd_manage_muc) + 1,
						UIContact.cmd_close_muc);

				UIItem presenceLabel = null;
				UIItem orderLabel = null;
				if (c.getAllPresences() != null) {
					presenceLabel = cmd_exit_muc;
					orderLabel = cmd_manage_muc;
				} else {
					cmd_enter_muc.setStatus(c.jid);
					presenceLabel = cmd_enter_muc;
					orderLabel = (UIItem) optionsMenu.getItems().elementAt(0);
					optionsMenu.remove(cmd_chat);
				}
				optionsMenu.insert(optionsMenu.indexOf(orderLabel) + 1,
						presenceLabel);
			}
		}
		optionsMenu.setSelectedItem(UIContact.cmd_chat);
	}

	private void openContactMenuKey() {
		Contact c = this.c;
		RosterScreen rs = RosterScreen.getInstance();
		if (c != null) {
			boolean isOnline = RosterScreen.isOnline();
			boolean isMuc = c instanceof MUC;
			optionsMenu = UIUtils.easyMenu(c.getPrintableName(), 10, (this)
					.getSubmenu().getAbsoluteY(), UICanvas.getInstance()
					.getWidth() - 20, null);
			optionsMenu.setAutoClose(false);
			optionsAccordion = null;
			Presence[] res = c.getAllPresences();
			if (res != null && res.length > 1 && isMuc == false) {
				optionsAccordion = new UIAccordion();
				optionsAccordion.setFocusable(true);
				optionsAccordion.setMaxHeight(0);
				optionsAccordion.setBg_color(UIConfig.menu_color);
				optionsAccordion.setOneOpen(false);
				optionsAccordion.setModal(true);
				optionsAccordion.addSpareItem(cmd_details);
				if (isOnline) {
					optionsAccordion.addSpareItem(cmd_change_nick);
					optionsAccordion.addSpareItem(cmd_groups);
					optionsAccordion.addSpareItem(cmd_resend_auth);
					optionsAccordion.addSpareItem(cmd_rerequest_auth);
					optionsAccordion.addSpareItem(cmd_delc);
				}
				optionsAccordion.addSpareItem(cmd_active_sessions);
				cmd_active_sessions.setFocusable(false);

				for (int i = 0; i < res.length; i++) {
					optionsVector = new Vector();

					String resString = null;
					resString = Contact.resource(res[i]
							.getAttribute(Iq.ATT_FROM));
					if (resString == null) resString = res[i]
							.getAttribute(Iq.ATT_FROM);
					UIImage img = UIContact.getPresenceIcon(c, res[i]
							.getAttribute(Iq.ATT_FROM), c.getAvailability());
					optionsLabel = new UILabel(img, resString);
					optionsLabel.setWrappable(true, UICanvas.getInstance()
							.getWidth() - 30);
					optionsLabel.setStatus(res[i]
							.getAttribute(Message.ATT_FROM));
					optionsVector.addElement(cmd_chat);
					if (isOnline) {
						optionsVector.addElement(cmd_send);
						if (AlbumScreen.getCount(XmppConstants.IMG_TYPE) > 0
								|| AlbumScreen
										.getCount(XmppConstants.AUDIO_TYPE) > 0) {
							optionsVector.addElement(cmd_send_file);
						}
						if (rs.cameraOn) optionsVector
								.addElement(cmd_contact_capture_img);
						if (rs.micOn) optionsVector
								.addElement(cmd_contact_capture_aud);
						optionsVector.addElement(cmd_querycmd);
						if (c.pending_tasks) {
							optionsVector.addElement(cmd_tasks);
						}
					}
					optionsAccordion.addItem(optionsLabel, optionsVector);
				}
				optionsMenu.append(optionsAccordion);
				optionsAccordion.setSelectedIndex(0);
				optionsMenu.setSelectedItem(cmd_details);
			} else {
				openContactMenuSingleResource(c, rs, isOnline, isMuc, res);
			}
			rs.addPopup(optionsMenu);
		}
	}

	public static UIItem getExtraCommand(UIItem inputItem, UIMenu menu) {
		Enumeration en = extraCommands.elements();
		while (en.hasMoreElements()) {
			Object[] triple = (Object[]) en.nextElement();
			UIItem newLabel = (UIItem) triple[0];
			UIItem orLabel = (UIItem) triple[1];
			String fullJid = (String) triple[2];
			if (newLabel == inputItem) {
				menu.setStatus(fullJid);
				return orLabel;
			}
		}
		return null;
	}
}
