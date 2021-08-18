/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: ChatScreen.java 2447 2011-02-07 13:13:58Z luca $
 */

package lampiro.screens;

import it.yup.dispatch.EventQuery;
import it.yup.dispatch.EventQueryRegistration;
import it.yup.ui.TokenIterator;
import it.yup.ui.UIButton;
import it.yup.ui.UICanvas;
import it.yup.ui.UICombobox;
import it.yup.ui.UIConfig;
import it.yup.ui.UIEmoLabel;
import it.yup.ui.UIHLayout;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UILayout;
import it.yup.ui.UIMenu;
import it.yup.ui.UIPanel;
import it.yup.ui.UIScreen;
import it.yup.ui.UISeparator;
import it.yup.ui.UITextField;
import it.yup.ui.UIUtils;
import it.yup.ui.wrappers.UIFont;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.ui.wrappers.UIImage;

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.util.Utils;
import it.yup.xml.Element;
import it.yup.xmlstream.BasicXmlStream;
import it.yup.xmlstream.PacketListener;
import it.yup.util.Alerts;
import it.yup.client.Config;
import it.yup.xmpp.Contact;
import it.yup.xmpp.IQResultListener;
import it.yup.xmpp.MUC;
import it.yup.client.XMPPClient;
import it.yup.xmpp.XmppConstants;
import it.yup.xmpp.packets.Iq;
import it.yup.xmpp.packets.Message;
import it.yup.xmpp.packets.Presence;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextField;



import lampiro.LampiroMidlet;
import lampiro.screens.rosterItems.UIContact;

public class ChatScreen extends UIScreen implements PacketListener,
		CommandListener {

	class MUCUpdateListener extends IQResultListener {

		public void handleError(Element e) {

		}

		public void handleResult(Element e) {
			String mucJid = e.getAttribute(Message.ATT_FROM);
			XMPPClient xmppClient = XMPPClient.getInstance();
			Contact mucContact = xmppClient.getRoster().getContactByJid(mucJid);
			if (mucContact == null) return;
			MUC muc = (MUC) mucContact;
			try {
				synchronized (UICanvas.getLock()) {
					UICanvas.getInstance().close(ChatScreen.this);
					RosterScreen.getInstance().chatWithContact(muc, null);
				}
			} catch (Exception ex) {
				// #mdebug
								Logger.log("In updating mucresultlistener");
								System.out.println(ex.getMessage());
								ex.printStackTrace();
				// #enddebug
			}
			MUCScreen mucScreen = (MUCScreen) RosterScreen.getChatScreenList()
					.get(mucJid);
			// the jid of the already present contact
			String cJid = ChatScreen.this.preferredResource;
			if (cJid == null) cJid = ChatScreen.this.user.jid;
			mucScreen.sendInvite(cJid);
			mucScreen.setSelectedItem(mucScreen.chatPanel);
			mucScreen.chatPanel.setSelectedItem(mucScreen.mucParticipants);
			mucScreen.keyPressed(UICanvas.getInstance().getKeyCode(
					UICanvas.FIRE));
			// meanwhile send the whole history!! :D

			Enumeration en = ChatScreen.this.getCurrent_conversation()
					.elements();
			String myJid = xmppClient.my_jid;
			while (en.hasMoreElements()) {
				ConversationEntry entry = (ConversationEntry) en.nextElement();
				if (entry.messageType.equals(Message.CHAT)) {
					Message m = new Message(mucJid, Message.GROUPCHAT);
					m.setBody(entry.text);
					Element delay = m.addElement(XmppConstants.NS_DELAY,
							XmppConstants.DELAY);
					String completeFrom = (entry.type == ConversationEntry.ENTRY_TO ? myJid
							: cJid);
					delay.setAttribute(Message.ATT_FROM, completeFrom);
					xmppClient.sendPacket(m);
				}
			}
		}

	}

	class UICutLabel extends UIEmoLabel {

		private boolean needCut = false;

		public UICutLabel(String text) {
			super(text);
		}

		public int getHeight(UIGraphics g) {
			return super.getHeight(g);
		}

		protected void computeTextLines(UIFont usedFont, int w) {
			textLines = new Vector();
			TokenIterator ti = new TokenIterator(this);
			ti.computeLazyLines(usedFont, w);
			int count = 0;
			// compute only the needed lines to fit the screen
			int fontHeight = usedFont.getHeight();
			int cumulativeHeight = (2 * getPaddings()[1]);
			while (count < ti.getLinesNumber()) {
				String ithString = ti.elementAt(count).trim();
				if (ithString.length() > 0) {
					cumulativeHeight += ((fontHeight + 2));
					if (cumulativeHeight <= printableHeight) {
						textLines.addElement(ithString);
					} else {
						needCut = true;
						this.setSubmenu(zoomSubmenu);
						break;
					}
				}
				count++;
			}
		}

		protected void paint(UIGraphics g, int w, int h) {
			super.paint(g, w, h);
			if (needCut) {
				g.setColor(0x555555);
				UIFont currentFont = this.getFont();
				if (currentFont == null) currentFont = UIConfig.font_body;
				String moreString = " " + rm.getString(ResourceIDs.STR_MORE)
						+ " ";
				int moreWidth = currentFont.stringWidth(moreString);
				g.fillRect(1, h - currentFont.getHeight() - 2, moreWidth + 1,
						currentFont.getHeight() + 1);
				g.setColor(0xFFFFFF);
				g.drawString(moreString, 1, h - currentFont.getHeight() - 1,
						UIGraphics.TOP | UIGraphics.LEFT);
			}
		}

	}

	static ResourceManager rm = ResourceManager.getManager();

	UITextField topic_name_field = new UITextField(rm
			.getString(ResourceIDs.STR_ROOM_NAME), "", 50, TextField.ANY);

	UIButton topic_button = new UIButton(rm.getString(ResourceIDs.STR_SUBMIT));

	/*
	 * Used to avoid to repaint for the same status twice
	 */
	private String oldStatus = "";

	/*
	 * some blocking or long operation must be done when the screen has already
	 * been shown
	 */
	private boolean filledScreen = false;

	/*
	 * The area in which the screen can paint (hence excluding headers footers
	 * and title screen)
	 */
	protected int printableHeight = -1;

	/*
	 * The header used to show stats and advice;
	 */
	protected UIHLayout header;

	/*
	 * The user associated to this UIscreen
	 */
	protected Contact user;
	UILabel cmd_exit = new UILabel(rm.getString(ResourceIDs.STR_CLOSE));
	protected UILabel cmd_write = new UILabel(rm
			.getString(ResourceIDs.STR_WRITE));
	protected UILabel cmd_clear = new UILabel(rm
			.getString(ResourceIDs.STR_CLEAR_HIST));

	protected UILabel addUser = new UILabel(rm
			.getString(ResourceIDs.STR_GROUP_CHAT));

	protected UILabel cmd_capture_img = new UILabel(rm
			.getString(ResourceIDs.STR_SEND_IMAGE));

	protected UILabel cmd_capture_aud = new UILabel(rm
			.getString(ResourceIDs.STR_SEND_AUDIO));

	protected UILabel cmd_send_file = new UILabel(rm
			.getString(ResourceIDs.STR_SEND_FILE));

	protected UILabel cmd_forward_message = new UILabel(rm
			.getString(ResourceIDs.STR_FORWARD_MESSAGE));

	/*
	 * The menu used by UIlabels in chatscreen to close the screen
	 */
	UIMenu closeMenu;
	/*
	 * The UIlabel used by UIlabels in chatscreen to close the screen
	 */
	UILabel closeLabel;

	// #mdebug
		private UILabel cmd_debug = new UILabel("Debug");
	// #enddebug

	/* Used to save the "http" addresses seen on screen */
	private Hashtable cmd_urls = new Hashtable();

	// XXX add a global handler for icons
	protected static UIImage img_msg;

	// the panel containing headers and labels
	protected UIPanel chatPanel;

	// /** number of currently displayed entries */
	// private int displayed_entries;

	// private boolean _entries_scrolled = false;

	static int scroll_color = 0x444444;

	private EventQueryRegistration reg = null;

	/** wrapped conversation cache */
	static Hashtable conversations = new Hashtable();
	private Vector current_conversation = null;
	private int hs = 2 * Short.parseShort(Config.getInstance().getProperty(
			Config.HISTORY_SIZE, "30"));
	UIMenu zoomSubmenu = new UIMenu("");
	UILabel zoomLabel = new UILabel(rm.getString(ResourceIDs.STR_MORE)
			.toUpperCase());

	protected String preferredResource;

	UILabel headerImg = null;

	UILabel headerStatus = null;

	// the start index of the chat lines in the chat panel:
	// 0 for chats and 2 for MUCs (the rosterCombo is present)
	int chatLineStart = 0;

	String defaultText = "";

// #ifndef RIM_5.0
	private boolean bookScroolDown = false;
	// #endif

	static {
		try {
			img_msg = UIImage.createImage("/icons/message.png");
		} catch (IOException e) {
			img_msg = UIImage.createImage(16, 16);
		}
	}

	void askTopic() {
		UIMenu topicNameMenu = UIUtils.easyMenu(rm
				.getString(ResourceIDs.STR_CHOOSE_NAME), 10, 20, this
				.getWidth() - 20, topic_name_field);
		topic_name_field.setText(XMPPClient.getInstance().getMyContact().jid
				.replace('@', '_')
				+ MUCScreen.unnamedMUCCounter);
		MUCScreen.unnamedMUCCounter++;
		topicNameMenu.append(UIUtils.easyCenterLayout(topic_button, 100));
		topicNameMenu.setDirty(true);
		topicNameMenu.setSelectedIndex(topicNameMenu.indexOf(topic_name_field));
		this.addPopup(topicNameMenu);
	}

	public ChatScreen(Contact u, String preferredResource) {
		super();

		// lots of insertion and deletion ...
		// this.setFreezed(true);

		// prepare closeMenu
		closeMenu = new UIMenu("");
		closeLabel = new UILabel(rm.getString(ResourceIDs.STR_CLOSE)
				.toUpperCase());
		closeMenu.append(closeLabel);

		user = u;
		this.preferredResource = preferredResource;
		setMenu(new UIMenu(""));
		setTitle(rm.getString(ResourceIDs.STR_CHAT_WITH) + " "
				+ user.getPrintableName());

		current_conversation = (Vector) conversations.get(preferredResource);
		if (current_conversation == null) {
			current_conversation = new Vector();
			conversations.put(preferredResource, current_conversation);
		}
		/*
		 * XXX: hack, create an item and select it, the item won't relinquish
		 * focus
		 */

		chatPanel = new UIPanel();
		chatPanel.setMaxHeight(-1);
		chatPanel.setFocusable(true);
		// the panel has a contextual menu that let close the the screen
		// as well as the uilabels used to print the chat lines
		// #ifndef RIM
				chatPanel.setSubmenu(this.closeMenu);
		// #endif
		chatPanel.setModal(true);
		UIImage img = UIContact.getPresenceIcon(user, preferredResource, user
				.getAvailability(preferredResource));
		header = new UIHLayout(2);
		header.setGroup(false);
		String status = getPrintableStatus();
		headerStatus = new UILabel(status);
		header.insert(headerStatus, 0, 50, UILayout.CONSTRAINT_PERCENTUAL);
		headerImg = new UILabel(img);
		headerImg.setBg_color(UIConfig.header_bg);
		headerImg.setFg_color(UIConfig.menu_title);
		headerStatus.setBg_color(UIConfig.header_bg);
		headerStatus.setFg_color(UIConfig.menu_title);
		header.insert(headerImg, 1, img.getWidth() + 2,
				UILayout.CONSTRAINT_PIXELS);
		header.setFocusable(false);
		this.append(header);
		UISeparator sep = new UISeparator(2);
		sep.setFg_color(0xCCCCCC);
		this.append(sep);
		this.append(chatPanel);
		this.setSelectedIndex(2);

		// to compute the printableHeight the currently visualized screen must
		// be
		// used and not this one since it may be invisible
		getPrintableHeight(UICanvas.getInstance().getCurrentScreen()
				.getGraphics(), this.height);

	}

	/**
	 * 
	 */
	public void fillScreen() {
		for (int j = 0; j < current_conversation.size(); j++) {
			ConversationEntry entry = (ConversationEntry) current_conversation
					.elementAt(j);
			this.updateLabel(entry);
		}
		if (chatPanel.getItems().size() > 0) {
			// remember the separator
			chatPanel.setSelectedIndex(chatPanel.getItems().size() - 2);
			chatPanel.setDirty(true);
		}

		// prepare zoomSubMenu
		zoomSubmenu.append(this.zoomLabel);
		zoomLabel.setAnchorPoint(UIGraphics.HCENTER);
		zoomSubmenu.setAbsoluteX(10);
		zoomSubmenu.setAbsoluteY(10);
		zoomSubmenu.setWidth(this.getWidth() - 30);

		// listen for all incoming messages with bodies
		EventQuery q = new EventQuery(Message.MESSAGE, null, null);
		q.child = new EventQuery("body", null, null);
		if (reg == null) {
			reg = BasicXmlStream.addPacketListener(q, this);
		}

		// so to reset the status in the roster
		updateConversation();
		RosterScreen roster = RosterScreen.getInstance();
		roster._updateContact(user, Contact.CH_MESSAGE_READ);
		filledScreen = true;
		this.askRepaint();
	}

	/**
	 * @param preferredResource
	 */
	void toggleMenu() {
		UIMenu menu = getMenu();
		menu.removeAll();
		// #debug
				menu.append(cmd_debug);
		if (RosterScreen.isOnline()) {
			menu.append(cmd_write);
			menu.append(cmd_forward_message);
			RosterScreen rs = RosterScreen.getInstance();
			if (rs.isCameraOn()) menu.append(this.cmd_capture_img);
			if (rs.isMicOn()) menu.append(this.cmd_capture_aud);
			menu.append(this.cmd_send_file);
			if (rs.mucJid != null
					&& RosterScreen.supportsMUC(user
							.getPresence(preferredResource))) menu
					.append(addUser);
		}
		menu.append(cmd_clear);
		menu.append(cmd_exit);
		// the menu has been cleaned hence the url have been lost
		Enumeration en = this.cmd_urls.elements();
		while (en.hasMoreElements()) {
			UILabel object = (UILabel) en.nextElement();
			UILabel ithItem = object;
			menu.append(ithItem);
		}
	}

	private String getPrintableStatus() {
		String status = "";
		Presence[] allPresences = user.getAllPresences();
		// could even be null if the user is offline now
		if (allPresences != null) {
			Presence p = user.getPresence(preferredResource);
			if (p != null) status = p.getStatus();
		}
		if (status == null || status.length() == 0) {
			if (user.name.length() > 0) status = user.getPrintableName();
			else {
				Presence p = user.getPresence(preferredResource);
				if (p != null) status = p.getAttribute(Iq.ATT_FROM);
				else
					status = user.getPrintableName();
			}
		}
		return status;
	}

	void updateResource() {
		if (preferredResource == null) return;
		UIImage img = UIContact.getPresenceIcon(user, preferredResource, user
				.getAvailability(preferredResource));
		this.headerImg.setImg(img);
		this.headerStatus.setText(getPrintableStatus());

		String status = "";
		if (user.getAvailability(preferredResource) == Contact.AV_UNAVAILABLE) {
			status = rm.getString(ResourceIDs.STR_OFFLINE);
		} else {
			String showStatus = user.getPresence(preferredResource).getStatus();
			if (showStatus == null) showStatus = "";
			status = (rm.getString(ResourceIDs.STR_ONLINE) + ": " + showStatus);
		}

		String msgText = user.getPrintableName() + " "
				+ rm.getString(ResourceIDs.STR_IS) + " " + status;

		if (msgText.equals(oldStatus)) return;
		oldStatus = msgText;
		Message msg = null;
		msg = new Message(preferredResource, Message.HEADLINE);
		msg.setAttribute(Message.ATT_FROM, preferredResource);
		msg.setBody(msgText);
		user.addMessageToHistory(preferredResource, msg);
		updateConversation();
	}

	protected void paint(UIGraphics g, int w, int h) {
		super.paint(g, w, h);
		// #ifndef RIM
				if (bookScroolDown && UICanvas.getInstance().hasPointerMotionEvents()) {
					bookScroolDown = false;
					int hHeight = headerLayout.getHeight(g);
					int fHeight = footer.getHeight(g);
					int avHeight = UICanvas.getInstance().getClipHeight() - hHeight
							- fHeight - 1;
					this.setDirty(true);
					addPaintOffset(avHeight - height - getPaintOffset());
					askRepaint();
				}
		// #endif
	}

	protected void getPrintableHeight(UIGraphics g, int h) {
		int maxHeight = h - 10;
		maxHeight -= this.header.getHeight(g);
		maxHeight -= this.headerLayout.getHeight(g);
		// #ifndef RIM
				maxHeight -= this.footer.getHeight(g);
		// #endif
		this.printableHeight = maxHeight;
	}

	/**
	 * 
	 * @param screen_width
	 * @return true if new messages have been added
	 */
	boolean updateConversation() {
		return this.updateResConversation(null);
	}

	boolean updateResConversation(String res) {
		if (res == null) res = this.preferredResource;
		// if the user is offline i get all the conversations
		// Presence[] ps = user.getAllPresences();
		Vector messages = null;
		// if (ps == null || ps.length ==0)
		messages = user.getMessageHistory(res);
		// else
		// messages = user.getAllMessageHistory();
		if (messages == null || messages.size() == 0) { return false; }
		Enumeration en = messages.elements();
		boolean updated = false;
		while (en.hasMoreElements()) {
			String[] msg = (String[]) en.nextElement();
			ConversationEntry entry = wrapMessage(msg);
			updateLabel(entry);
			current_conversation.addElement(entry);
			updated = true;
		}
		// if (ps == null || ps.length ==0)
		user.resetMessageHistory(res);
		// else
		// user.resetAllMessageHistory();

		// must be done here after repaint to be sure all the panel
		// has been updated
		this.chatPanel.setSelectedIndex(this.chatPanel.getItems().size() - 2);
		this.chatPanel.setDirty(true);
		return updated;
	}

	/**
	 * @param entry
	 */
	private UILabel updateLabel(ConversationEntry entry) {
		String s = entry.text;
		s = getLabelHeader(entry) + s;
		UICutLabel uel = new UICutLabel(s);
		uel.setWrappable(true, this.width - 10);
		uel.setFocusable(true);
// #ifndef RIM
				checkUrls(cmd_urls, s, this.getMenu());
		// #endif

		int newBgColor = -1;
		if (entry.type == ConversationEntry.ENTRY_TO) {
			uel.setAnchorPoint(UIGraphics.RIGHT);
		} else {
			uel.setAnchorPoint(UIGraphics.LEFT);
			newBgColor = UIUtils.colorize(UIConfig.bg_color, -10);
			uel.setBg_color(newBgColor);
		}
		uel.setStatus(entry);

		// little modifications depending on message type
		if (entry.messageType.equals(Message.ERROR)) uel.setFg_color(0xCC0000);
		else if (entry.messageType.equals(Message.HEADLINE)) uel
				.setFg_color(0x00CC00);

		this.chatPanel.addItem(uel);
		// #ifndef RIM
				uel.setSubmenu(this.closeMenu);
		// #endif
		UISeparator sep = new UISeparator(1);
		sep.setFg_color(0xCCCCCC);
		this.chatPanel.addItem(sep);
		// empty oldMessages
		while (this.chatPanel.getItems().size() > hs) {
			this.chatPanel.removeItemAt(this.chatLineStart);
			this.chatPanel.removeItemAt(this.chatLineStart);
		}
		if (UICanvas.getInstance().hasPointerMotionEvents()) {
			scrollDown();
		}
		return uel;
	}

// #ifndef RIM_5.0

	private void scrollDown() {
		this.bookScroolDown = true;
	}

	// #endif

	String getLabelHeader(ConversationEntry entry) {
		String retString = "";
		if (entry.arriveTime.length() > 0) {
			retString = "[" + entry.arriveTime + "] ";
		}
		return retString;
	}

	public void showNotify() {
		toggleMenu();
		// reset the status img
		UIImage img = UIContact.getPresenceIcon(user, preferredResource, user
				.getAvailability());
		if (img != ((UILabel) this.header.getItem(1)).getImg()) {
			((UILabel) this.header.getItem(1)).setImg(img);
			this.askRepaint();
		}
		if (filledScreen && updateConversation()) {
			this.askRepaint();
		}
	}

	public static UIMenu checkUrls(Hashtable cmd_urls, String text, UIMenu menu) {
		// parse the urls and add to the command menu
		Enumeration en = Utils.find_urls(text).elements();
		while (en.hasMoreElements()) {
			String url = (String) en.nextElement();
			if (!cmd_urls.containsKey(url)) {
				UILabel cmd = new UILabel(url);
				cmd_urls.put(url, cmd);
				if (menu == null) {
					menu = new UIMenu("");
				}
				menu.append(cmd);
			}
		}
		return menu;
	}

	/**
	 * Wrap a message so that it fits the windows
	 * 
	 * @param
	 * @param screen_width
	 * 
	 * @return
	 */
	ConversationEntry wrapMessage(String text[]) {

		// #ifdef TIMING
		// @ long t1 = System.currentTimeMillis();
		// #endif

		String myJid = Contact.userhost(XMPPClient.getInstance().my_jid);
		byte type = Contact.userhost(text[0]).equals(myJid) ? ConversationEntry.ENTRY_FROM
				: ConversationEntry.ENTRY_TO;

		// #ifdef TIMING
		// @ System.out.println("wrap conv: " + (System.currentTimeMillis() -
		// @ // t1));
		// #endif

		ConversationEntry convEntry = new ConversationEntry(text[1], type);
		convEntry.arriveTime = text[2];
		convEntry.messageType = text[3];
		return convEntry;
	}

	public void itemAction(UIItem item) {
		UIMenu tempMenu = item.getSubmenu();
		if (tempMenu != null && tempMenu.getItems().size() > 0) {
			UILabel firstItem = (UILabel) tempMenu.getItems().elementAt(0);
			if (cmd_urls.contains(firstItem) || firstItem == zoomLabel) {
				menuAction(tempMenu, firstItem);
			}
		}
	}

	public void menuAction(UIMenu menu, UIItem cmd) {
		if (cmd == cmd_exit || cmd == this.closeLabel) {
			closeMe();
		} else if (cmd == cmd_capture_aud) {
			RosterScreen.getInstance().captureMedia(preferredResource,
					XmppConstants.AUDIO_TYPE);
		} else if (cmd == cmd_capture_img) {
			RosterScreen.getInstance().captureMedia(preferredResource,
					XmppConstants.IMG_TYPE);
		} else if (cmd == cmd_send_file) {
			AlbumScreen alb = AlbumScreen.getInstance(preferredResource);
			UICanvas.getInstance().open(alb, true);
		} else if (cmd == cmd_write) {
			openComposer();
		} else if (cmd == cmd_clear) {
			current_conversation.removeAllElements();
			Enumeration en = cmd_urls.elements();
			UIMenu mn = getMenu();
			while (en.hasMoreElements()) {
				mn.remove((UILabel) en.nextElement());
			}
			this.setFreezed(true);
			this.chatPanel.removeAllItems();
			this.setFreezed(false);
			cmd_urls.clear();
			this.setDirty(true);
			this.askRepaint();
			// #mdebug
					} else if (cmd == cmd_debug) {
						Logger.log(
			
						"h:" + UICanvas.getInstance().getHeight() + "w:"
								+ UICanvas.getInstance().getWidth() + "ch:");
						Logger.log(this.getGraphics().getClipHeight() + "cw:"
								+ this.getGraphics().getClipWidth() + "ph:"
								+ this.chatPanel.getHeight(getGraphics()));
						//
						DebugScreen debugScreen = new DebugScreen();
						UICanvas.getInstance().open(debugScreen, true);
			// #enddebug
		} else if (cmd == this.addUser) {
			askTopic();
		} else if (cmd == this.cmd_forward_message) {
			String text = null;
			UIItem selItem = (UIItem) chatPanel.getItems().elementAt(
					chatPanel.getSelectedIndex());
			if (selItem instanceof UILabel == false) return;
			text = ((UILabel) selItem).getText();
			Object status = selItem.getStatus();
			if (status instanceof ConversationEntry == false) return;
			// ConversationEntry entry = (ConversationEntry) status;
			String fromContact = "";
			fromContact = XMPPClient.getInstance().my_jid;
			// if (entry.type == ConversationEntry.ENTRY_TO) fromContact =
			// XMPPClient
			// .getInstance().my_jid;
			// else
			// fromContact = (preferredResource != null ? preferredResource
			// : user.jid);
			ForwardScreen fs = new ForwardScreen(this, text, fromContact, user,
					preferredResource);
			UICanvas.getInstance().open(fs, true);
		} else if (cmd == topic_button) {
			RosterScreen rs = RosterScreen.getInstance();
			HandleMucScreen.createMUC(this.topic_name_field.getText(),
					rs.mucJid, null, null, new HandleMucScreen.MUCStateHandler(
							new MUCUpdateListener()), true, true);
		} else if (cmd == this.zoomLabel) {
			UICutLabel selLabel = (UICutLabel) this.chatPanel.getSelectedItem();
			final String selText = selLabel.getText();

			final UITextField expField = new UITextField("", selText, selText
					.length(), TextField.UNEDITABLE);
			expField.setWrappable(true);
			expField.setExpandable(false);

			int maxHeight = UICanvas.getInstance().getClipHeight() - 10;
			UIGraphics g = this.getGraphics();
			maxHeight -= this.headerLayout.getHeight(g);
			// #ifndef RIM
						maxHeight -= this.footer.getHeight(g);
			// #endif

			expField.setMaxHeight(maxHeight);
			UIMenu expandMenu = new UIMenu("");
			expandMenu.append(closeLabel);
			UIScreen zoomScreen = new UIScreen() {
				public void menuAction(UIMenu menu, UIItem cmd) {
					if (cmd == closeLabel) {
						// so that the user preferred resource is reset
						UICanvas.getInstance().close(this);
						UICanvas.getInstance().show(ChatScreen.this);
					}
				}

				public void itemAction(UIItem item) {
					if (item == expField) {
						menuAction(null, closeLabel);
					}
				}
			};
			UIPanel uip = new UIPanel(false, false);
			zoomScreen.append(uip);
			uip.addItem(expField);
			expField.setSubmenu(expandMenu);
			// uip.setSubmenu(expandMenu);
			zoomScreen.setSelectedItem(uip);
			uip.setSelectedItem(expField);
			zoomScreen.setTitle(rm.getString(ResourceIDs.STR_EXPANDED));
			UICanvas.getInstance().open(zoomScreen, true);
			expField.setSelected(true);
			expField.expand();
		} else {
			if (this.cmd_urls.contains(cmd)) {
				String url = ((UILabel) cmd).getText();

				try {
					LampiroMidlet.makePlatformRequest(url);
				} catch (ConnectionNotFoundException e) {
					UICanvas.showAlert(Alerts.ERROR, "URL Error",
							"Can't open URL:" + e.getMessage());
				}
			}
		}
	}

	public void closeMe() {
		// so that the user preferred resource is reset
		// user.lastResource = null;
		Hashtable chatScreenList = RosterScreen.getChatScreenList();
		// better to search me and nothing else
		Enumeration en = chatScreenList.keys();
		while (en.hasMoreElements()) {
			Object ithKey = en.nextElement();
			Object object = chatScreenList.get(ithKey);
			if (object == this) chatScreenList.remove(ithKey);
		}
		// chatScreenList.remove(this.user.jid);

		// reset the status in the roster
		RosterScreen rs = RosterScreen.getInstance();
		rs._updateContact(user, Contact.CH_MESSAGE_READ);
		if (reg != null) {
			reg.remove();
			reg = null;
		}
		// to reset the header from outside this screen too ;
		rs.setDirty(true);
		UICanvas.getInstance().close(this);
	}

	boolean isPrintable(int key) {
		int keyNum = -1;
		switch (key) {
			case UICanvas.KEY_NUM0:
			case UICanvas.KEY_NUM1:
			case UICanvas.KEY_NUM2:
			case UICanvas.KEY_NUM3:
			case UICanvas.KEY_NUM4:
			case UICanvas.KEY_NUM5:
			case UICanvas.KEY_NUM6:
			case UICanvas.KEY_NUM7:
			case UICanvas.KEY_NUM8:
			case UICanvas.KEY_NUM9:
				keyNum = key;
		}
		if (keyNum == -1
				&& UICanvas.getInstance().getGameAction(key) != UICanvas.FIRE) { return false; }
		return true;
	}

	/**
	 * Handle key events
	 * 
	 * @param kc
	 *            the pressed key
	 */
	public boolean keyPressed(int kc) {
		if (this.popupList.size() == 0
				&& this.getMenu().isOpenedState() == false) {
			int ga = UICanvas.getInstance().getGameAction(kc);

			boolean ip = isPrintable(kc);

			if (ip || ga == UICanvas.FIRE) {
				if (needComposer()) {
					if (RosterScreen.isOnline()) openComposer();
					return true;
				}
			}

			switch (ga) {
				case UICanvas.RIGHT: {
					RosterScreen roster = RosterScreen.getInstance();
					roster._updateContact(user, Contact.CH_MESSAGE_READ);
					RosterScreen.showNextScreen(this);
					return true;
				}
				case UICanvas.LEFT: {
					RosterScreen roster = RosterScreen.getInstance();
					roster._updateContact(user, Contact.CH_MESSAGE_READ);
					RosterScreen.showPreviousScreen(this);
					return true;
				}
			}
		}
		return super.keyPressed(kc);
	}

	private boolean needComposer() {
		if (this.chatPanel.getSelectedIndex() < 0) return true;
		else {
			UIItem selItem = (UIItem) this.chatPanel.getItems().elementAt(
					this.chatPanel.getSelectedIndex());
			if (selItem instanceof UICombobox == true) return false;
			UIMenu selMenu = selItem.getSubmenu();
			if (selMenu != null && selMenu.getItems().size() >= 0) {
				UILabel selLabel = (UILabel) selMenu.getItems().elementAt(0);
				if (cmd_urls.contains(selLabel) || selLabel == zoomLabel) { return false; }
			}
			return true;
		}
	}

	protected void openComposer() {
		SimpleComposerScreen cs = new SimpleComposerScreen(this, user,
				this.preferredResource);
		UICanvas.display(cs.getTextBox());
	}

	public void packetReceived(Element e) {
		// avoid useless repaint when computing conversation
		this.setFreezed(true);
		// check if it is a msg for myself so the img_msg is not shown
		// and avoid fast bot problem
		// String fullJid = user.getFullJid();
		// fullJid could be null for offline contact
		// so let's use in that case the userhost and nothing more
		// if (fullJid == null) fullJid = user.jid;
		// String userHost = Contact.userhost(fullJid);
		boolean myPacket = isMyPacket(e);
		boolean updated = false;
		if (myPacket && needDisplay()
				&& this == UICanvas.getInstance().getCurrentScreen()) {
			try {
				synchronized (UICanvas.getLock()) {
					updated = updateConversation();
					RosterScreen.getInstance()._updateContact(user,
							Contact.CH_MESSAGE_READ);
					if (updated == false) {
						this.setFreezed(false);
						return;
					}
				}
			} finally {

			}
		} else if (myPacket == false) {
			((UILabel) this.header.getItem(1)).setImg(img_msg);
			updated = true;
			/*
			 * ((UILabel) this.header.getItem(1)).setDirty(true);
			 * this.askRepaint();
			 */
		}
		try {
			synchronized (UICanvas.getLock()) {
				this.setFreezed(false);
				if (updated) askRepaint();
			}
		} catch (Exception ex) {
			// #mdebug
						Logger.log("In updating chatscreen");
						System.out.println(ex.getMessage());
						ex.printStackTrace();
			// #enddebug
		}
	}

	boolean needDisplay() {
		return user.getHistoryLength(this.preferredResource) > 0;
	}

	boolean isMyPacket(Element e) {
		return e.getAttribute(Iq.ATT_FROM).equals(preferredResource);
	}

	/**
	 * Entry for a conversation
	 */
	static class ConversationEntry {
		public String messageType;
		/** message from */
		public static final byte ENTRY_FROM = 0;
		/** message to */
		public static final byte ENTRY_TO = 1;

		/** previous message wrap XXX ? */
		public static final byte ENTRY_ERROR = 2;

		/** message type in / on */
		public byte type;

		/** the message itself */
		public String text;

		/** first line of the entry that is displayed */
		public int entry_offset = 0;
		public String from = "";
		public String arriveTime = "";

		public ConversationEntry(String text, byte type) {
			this.type = type;
			this.text = text;
		}
	}

	public void commandAction(Command cmd, Displayable disp) {
		try {
			// #ifndef RIM
						synchronized (UICanvas.getLock()) {
			// #endif
			UICanvas.display(null);
			this.dirty = true;
			// #ifndef RIM
						}
			// #endif
		} finally {

		}

	}

	public Vector getCurrent_conversation() {
		return current_conversation;
	}

	public boolean askClose() {
		this.closeMe();
		return false;
	}

}
