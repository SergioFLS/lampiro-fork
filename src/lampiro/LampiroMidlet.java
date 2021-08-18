/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: LampiroMidlet.java 2447 2011-02-07 13:13:58Z luca $
 */

package lampiro;

// #mdebug

import it.yup.util.log.Logger;
import it.yup.util.log.MemoryLogConsumer;
import it.yup.util.log.StderrConsumer;
import it.yup.util.log.XMPPConsumer;
import it.yup.util.log.XMPPConsumer.XMPPLogger;

// #enddebug

// #ifdef UI

import it.yup.transport.NetConnector;
import it.yup.ui.UICanvas;
import it.yup.ui.UICombobox;
import it.yup.ui.UIConfig;
import it.yup.ui.UITextField;
import it.yup.ui.UIUtils;

import it.yup.ui.wrappers.UIFont;
import lampiro.screens.SplashScreen;

// #ifndef RIM

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Display;

// #endif

// #endif
// #ifndef UI
//@
//@import it.yup.screens.SplashScreen;
//@
// #endif

import it.yup.client.Config;
import it.yup.xml.Element;
import it.yup.xmpp.Contact;
import it.yup.client.XMPPClient;
import it.yup.xmpp.packets.Presence;

import javax.microedition.io.ConnectionNotFoundException;

import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.util.storage.KeyStoreFactory;

/**
 * Lampiro Midlet.
 * 
 * XXX: Use ResourceMgr for the phone hold on message or move the hold-on logic
 * in XMPPClient (maybe better)
 */
// #ifndef RIM
public class LampiroMidlet extends MIDlet
// #endif
// #mdebug
		implements XMPPLogger
// #enddebug
{
	// #ifndef RIM
	/** The main display */
	public static Display disp;
	/** The midlet instance */
	public static LampiroMidlet _lampiro;
	// #endif

	private XMPPClient xmpp = null;

	/**
	 * information saved when the app is paused (i.e. a phone call or an SMS is
	 * received or the user switches to another application).
	 */
	private int last_availability = -1;
	private String last_status;

	/**
	 * Constructor
	 */
	public LampiroMidlet() {
		// first of all initialize config
		ResourceManager.setDefaultProperties(Config.lang,
				new String[] { "/locale/common" },
				// #ifndef GLIDER
				new String[][] {}
		// #endif
				);
		String rmsName = Config.CLIENT_NAME + "rms";
		Config.makeInstance(KeyStoreFactory.getStore(rmsName));
		xmpp = XMPPClient.getInstance();
		// #mdebug
				Logger.addConsumer(new StderrConsumer());
				Logger.addConsumer(MemoryLogConsumer.getConsumer());
		// #enddebug
		_lampiro = this;
		// #ifndef RIM
		LampiroMidlet.disp = Display.getDisplay(this);
		// #endif
		// #ifdef UI
		// #ifndef RIM
		UICanvas.setDisplay(Display.getDisplay(this));
		// #endif
		UICanvas canvas = UICanvas.getInstance();
		UICanvas.display(null);
		String defaultColor = "0";
		Config cfg = Config.getInstance();
		String colorString = cfg.getProperty(Config.COLOR, defaultColor);
		int colorInt = colorString.charAt(0) - '0';
		LampiroMidlet.changeColor(colorInt);
		String defaultFont = UICanvas.getInstance().hasPointerEvents() ? "1"
				: "0";
		String fontString = cfg.getProperty(Config.FONT_SIZE, defaultFont);
		int fontInt = fontString.charAt(0) - '0';
		String defaultMenuFont = UICanvas.getInstance().hasPointerEvents() ? "2"
				: "1";
		String fontMenuString = cfg.getProperty(Config.FONT_MENU_SIZE,
				defaultMenuFont);
		int fontMenuInt = fontMenuString.charAt(0) - '0';
		LampiroMidlet.changeFont(fontInt, fontMenuInt);

		// update the default strings for comboboxes
		ResourceManager rm = ResourceManager.getManager();
		UICombobox.selectString = rm.getString(ResourceIDs.STR_SELECT);
		String cancelString = rm.getString(ResourceIDs.STR_CANCEL);
		String okString = rm.getString(ResourceIDs.STR_OK);
		// #ifndef RIM
		UICombobox.selectString = UICombobox.selectString.toUpperCase();
		cancelString = cancelString.toUpperCase();
		okString = okString.toUpperCase();
		// #endif
		UICombobox.cancelString = cancelString;
		UITextField.setButtonsString(cancelString, okString);
		// #ifndef RIM
		openCanvas();
		// #endif
		// #endif
		// #ifndef UI
		//@		 disp.setCurrent(new SplashScreen());
		// #endif
	}

	// #mdebug
		public boolean messageReady(Element e) {
			XMPPClient xmppClient = XMPPClient.getInstance();
			if (xmppClient != null && xmppClient.my_jid != null
					&& xmppClient.isValid_stream()) {
				try {
					xmppClient.sendPacket(e);
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
					ex.printStackTrace();
					return false;
				}
				return true;
			} else {
				return false;
			}
		}
	
	// #enddebug

	private void openCanvas() {
		synchronized (UICanvas.getLock()) {
			try {
				UICanvas.getInstance().open(new SplashScreen(), true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Starts the application or re-starts it after being placed in background.
	 */
	public void startApp() {
		if (last_availability >= 0) {
			updatePresence(last_availability, last_status);
			last_availability = -1;
		}
	}

	public static void vibrate(int duration) {
		// #ifndef RIM
		LampiroMidlet.disp.vibrate(duration);
		// #endif
	}

	public final static boolean makePlatformRequest(String url)
			throws ConnectionNotFoundException {
		// #ifndef RIM
		return _lampiro.platformRequest(url);
		// #endif
	}

	/**
	 * Closes the application.
	 * 
	 * @param unconditional
	 *            stop is forced
	 */
	protected void destroyApp(boolean unconditional) {
		xmpp.stopClient();
		Config.getInstance().saveToStorage();
		_lampiro = null;
	}

	/**
	 * Pauses the application placing it in background (i.e. due to a phone call
	 * or an SMS or the user switches to another application). The app saves the
	 * current Presence and sets it to a status indicating the user is not
	 * available.
	 */
	protected void pauseApp() {
		ResourceManager rm = ResourceManager.getManager();
		Contact myContact = xmpp.getMyContact();
		last_availability = myContact.getAvailability();
		String pauseStr = rm.getString(ResourceIDs.STR_PAUSE_APP);
		Presence pres = myContact.getPresence(null);
		if (pres != null) {
			last_status = pres.getStatus();
		}
		updatePresence(Contact.AV_AWAY, pauseStr);
		//		if (updatePresence) {
		//			try {
		//				Thread.sleep(1000);
		//			} catch (InterruptedException e) {
		//				e.printStackTrace();
		//			}
		//		}
	}

	private boolean updatePresence(int availability, String status) {
		Contact myContact = xmpp.getMyContact();
		if (myContact != null) {
			Presence pres = myContact.getPresence(null);
			if (pres != null) {
				xmpp.setPresence(availability, status);
				return true;
			}
		}
		return false;
	}

	public static void exit() {
		if (_lampiro == null) { return; }
		LampiroMidlet m = _lampiro;
		m.destroyApp(false);
		// #ifndef RIM
		m.notifyDestroyed();
		// #endif
	}

	// #ifdef UI

	static public void changeFont(int fontIndex, int menuFontIndex) {
		switch (fontIndex) {
			case 0:
				UIConfig.font_body = UIFont.getFont(UIFont.FACE_PROPORTIONAL,
						UIFont.STYLE_PLAIN, UIFont.SIZE_SMALL);
				break;
			case 1:
				UIConfig.font_body = UIFont.getFont(UIFont.FACE_PROPORTIONAL,
						UIFont.STYLE_PLAIN, UIFont.SIZE_MEDIUM);
				break;
			case 2:
				UIConfig.font_body = UIFont.getFont(UIFont.FACE_PROPORTIONAL,
						UIFont.STYLE_PLAIN, UIFont.SIZE_LARGE);
				break;

			default:
				break;
		}

		switch (menuFontIndex) {
			case 0:
				UIConfig.font_menu = UIFont.getFont(UIFont.FACE_PROPORTIONAL,
						UIFont.STYLE_PLAIN, UIFont.SIZE_SMALL);
				break;
			case 1:
				UIConfig.font_menu = UIFont.getFont(UIFont.FACE_PROPORTIONAL,
						UIFont.STYLE_PLAIN, UIFont.SIZE_MEDIUM);
				break;
			case 2:
				UIConfig.font_menu = UIFont.getFont(UIFont.FACE_PROPORTIONAL,
						UIFont.STYLE_PLAIN, UIFont.SIZE_LARGE);
				break;

			default:
				break;
		}
	}

	static public void changeColor(int colorIndex) {
		switch (colorIndex) {
			case 0:
				UIConfig.tbb_color = 0xb0c2c8;
				UIConfig.input_color = 0xFFFFFF;
				UIConfig.header_bg = 0x567cfe;
				UIConfig.tbs_color = UIConfig.header_bg;
				UIConfig.header_fg = 0xDDE7EC;
				UIConfig.menu_title = 0xDDE7EC;
				UIConfig.bg_color = 0xddddff;
				UIConfig.menu_border = 0x223377;
				UIConfig.menu_color = 0xacc2d8;
				UIConfig.menu_3d = true;
				UIConfig.button_color = UIConfig.tbb_color;
				UIConfig.button_selected_color = UIConfig.header_bg;
				UIConfig.bb_color = UIConfig.menu_border;
				UIConfig.bbs_color = UIConfig.menu_border;
				UIConfig.bdb_color = UIUtils.colorize(UIConfig.menu_color, -50);
				UIConfig.blb_color = UIUtils.colorize(UIConfig.menu_color, 50);
				UIConfig.bdbs_color = UIConfig.bdb_color;
				UIConfig.blbs_color = UIConfig.blb_color;
				break;
			case 1:
				UIConfig.tbb_color = 0xb0c2c8;
				UIConfig.input_color = 0xFFFFFF;
				UIConfig.header_bg = 0x24982f;
				UIConfig.tbs_color = UIUtils.colorize(UIConfig.header_bg, 30);
				UIConfig.header_fg = 0xDDE7EC;
				UIConfig.menu_title = 0xDDE7EC;
				UIConfig.bg_color = 0xddffdd;
				UIConfig.menu_border = 0x227733;
				UIConfig.menu_color = 0xacd8c2;
				UIConfig.menu_3d = true;
				UIConfig.button_color = UIConfig.tbb_color;
				UIConfig.button_selected_color = UIConfig.tbs_color;
				UIConfig.bb_color = UIConfig.menu_border;
				UIConfig.bbs_color = UIConfig.menu_border;
				UIConfig.bdb_color = UIUtils.colorize(UIConfig.menu_color, -50);
				UIConfig.blb_color = UIUtils.colorize(UIConfig.menu_color, 50);
				UIConfig.bdbs_color = UIConfig.bdb_color;
				UIConfig.blbs_color = UIConfig.blb_color;
				break;
			case 2:
				UIConfig.tbb_color = 0xb0c2c8;
				UIConfig.input_color = 0xFFFFFF;
				UIConfig.header_bg = 0xdb0724;
				UIConfig.tbs_color = UIUtils.colorize(UIConfig.header_bg, 20);
				UIConfig.header_fg = 0xDDE7EC;
				UIConfig.menu_title = 0xDDE7EC;
				UIConfig.bg_color = 0xffdddd;
				UIConfig.menu_border = 0x773322;
				UIConfig.menu_color = 0xd8c2ac;
				UIConfig.menu_3d = false;
				UIConfig.button_color = UIConfig.tbb_color;
				UIConfig.button_selected_color = UIConfig.tbs_color;
				UIConfig.bb_color = UIConfig.menu_border;
				UIConfig.bbs_color = UIConfig.menu_border;
				UIConfig.bdb_color = UIConfig.bb_color;
				UIConfig.blb_color = UIConfig.bb_color;
				UIConfig.bdbs_color = UIConfig.bdb_color;
				UIConfig.blbs_color = UIConfig.blb_color;

				break;
			case 3:
				UIConfig.tbb_color = 0xb0c2c8;
				UIConfig.input_color = 0xFFFFFF;
				UIConfig.header_bg = 0x111111;
				UIConfig.tbs_color = 0xff8000;
				UIConfig.header_fg = 0xDDE7EC;
				UIConfig.menu_title = 0xfb7c00;
				UIConfig.bg_color = 0xf8ebcf;
				UIConfig.menu_border = 0xfe611b;
				UIConfig.menu_color = 0xffc22a;
				UIConfig.menu_3d = false;
				UIConfig.button_color = 0xffa658;
				UIConfig.button_selected_color = 0xff8000;
				UIConfig.bb_color = UIConfig.button_selected_color;
				UIConfig.bbs_color = UIConfig.button_color;
				UIConfig.bdb_color = UIConfig.button_selected_color;
				UIConfig.blb_color = UIConfig.button_selected_color;
				UIConfig.bdbs_color = UIConfig.button_color;
				UIConfig.blbs_color = UIConfig.button_color;
				break;

			default:
				break;
		}
	}

	// #endif

}
