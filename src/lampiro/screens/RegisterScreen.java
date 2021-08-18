/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: RegisterScreen.java 2447 2011-02-07 13:13:58Z luca $
 */

package lampiro.screens;

import it.yup.dispatch.EventDispatcher;
import it.yup.dispatch.EventListener;
import it.yup.dispatch.EventQuery;
import it.yup.dispatch.EventQueryRegistration;
import it.yup.transport.NetConnector;
import it.yup.ui.UIButton;
import it.yup.ui.UICanvas;
import it.yup.ui.UICheckbox;
import it.yup.ui.UICombobox;
import it.yup.ui.UIConfig;
import it.yup.ui.UIGauge;
import it.yup.ui.UIHLayout;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UILayout;
import it.yup.ui.UIMenu;
import it.yup.ui.UIScreen;
import it.yup.ui.UITextField;
import it.yup.ui.UIUtils;
import it.yup.ui.UIVLayout;
import it.yup.ui.wrappers.UIFont;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.ui.wrappers.UIImage;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.util.Utils;

// #ifdef BLUENDO_REG
//@
//@import it.yup.util.BluendoXMLRPC;
//@import it.yup.xml.Element;
//@import it.yup.xmpp.packets.Iq;
//@
// #endif

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

// #ifndef RIM

import javax.microedition.io.Connector;
import it.yup.ui.UISeparator;

// #endif

import it.yup.util.Alerts;
import it.yup.client.Config;
import it.yup.xmpp.Contact;
import it.yup.client.XMPPClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStore;

// #ifdef BLUENDO_REG
//@
//@import org.bouncycastle.util.encoders.Base64;
//@import it.yup.xmpp.XmppConstants;
//@
// #endif

// #ifndef GLIDER

import lampiro.LampiroMidlet;

// TODO: Auto-generated Javadoc
// #endif

/**
 * The Class RegisterScreen.
 */
public class RegisterScreen extends UIScreen implements EventListener {

	/*
	 * A flag used to know if the connection has been initiated; that means that
	 * the server has been found at least once;
	 */
	private boolean streamConnected = false;

	/** The logo label. */
	private UILabel logoLabel;

	/** The logo layout. */
	private UIHLayout logoLayout;

	/** The rm. */
	private static ResourceManager rm = ResourceManager.getManager();

	/*
	 * The login info label
	 */
	/** The ul. */
	private UILabel ul = new UILabel(rm.getString(ResourceIDs.STR_LOGGING_IN));

	/** The tf_jid_label. */
	private UILabel tf_jid_label = new UILabel(rm
			.getString(ResourceIDs.STR_JABBER_ID)
			+ " (?)");


	/** The tf_jid_name. */
	UITextField tf_jid_name = new UITextField("", "", 128, TextField.ANY
			| TextField.NON_PREDICTIVE, UITextField.FORMAT_LOWER_CASE);

	private UICheckbox autoreconnect = new UICheckbox(rm
			.getString(ResourceIDs.STR_AUTORECONNECT));

	// private int regbgColor = 0xf5e2bc;
	private int regbgColor = UIUtils.colorize(UIConfig.bg_color, -2);

// #ifndef RIM
			/** The conf layout. */
			private UIHLayout confLayout = new UIHLayout(2);
		
			/** The key_configuration. */
			private UILabel key_configuration = new UILabel(rm
					.getString(ResourceIDs.STR_KEY_CONFIGURATION));
		
	// #endif


	/** The tf_pwd. */
	private UITextField tf_pwd = new UITextField(rm
			.getString(ResourceIDs.STR_PASSWORD), null, 32, TextField.ANY
			| TextField.PASSWORD);

	/** The but_cancel. */
	private UIButton but_cancel = new UIButton(rm
			.getString(ResourceIDs.STR_STOP_LOGIN));

	/** The tf_email. */
	private UITextField tf_email = new UITextField(rm
			.getString(ResourceIDs.STR_EMAIL_ADDRESS)
	// #ifndef GLIDER
									+ " (" + rm.getString(ResourceIDs.STR_OPTIONAL) + " )"
			// #endif
			, null, 128, TextField.EMAILADDR);

	/** The grp_new_account. */
	private UICheckbox grp_new_account = new UICheckbox(rm
			.getString(ResourceIDs.STR_NEW_USER));

	/** The wrong pwd. */
	private UIMenu wrongPwd;

	/** The tf_pwd_confirm. */
	private UITextField tf_pwd_confirm = new UITextField(rm
			.getString(ResourceIDs.STR_CONFIRM)
			+ " " + rm.getString(ResourceIDs.STR_PASSWORD).toLowerCase(), null,
			32, TextField.ANY | TextField.PASSWORD);

// #ifndef GLIDER
			/** The wizard shown. */
			private static boolean wizardShown = false;
			/** The button yes. */
			private UIButton buttonYes;
		
			/** The button no. */
			private UIButton buttonNo;
		
			/** The button layout. */
			private UIHLayout buttonLayout;
		
			/** The wizard text. */
			private UITextField wizardText;
		
			/** The wizard text gateways. */
			private UITextField wizardTextGateways;
	// #endif

	/** The tf_server. */
	private UITextField tf_server = new UITextField(rm
			.getString(ResourceIDs.STR_SERVER_NAME), null, 50, TextField.ANY
			| TextField.NON_PREDICTIVE);

	/** The grp_advanced. */
	private UICheckbox grp_advanced = new UICheckbox(rm
			.getString(ResourceIDs.STR_ADVANCED_OPTIONS));

	/** The reset_config. */
	private UICheckbox reset_config = new UICheckbox(rm
			.getString(ResourceIDs.STR_RESET_CONFIG));

	/** The reset_all_data. */
	private UICheckbox reset_all_data = new UICheckbox(rm
			.getString(ResourceIDs.STR_RESET_DATA));

	/** The resource. */
	private UITextField resource = null;

	/** The login label. */
	private UILabel loginLabel;

	/** The login menu. */
	private UIMenu loginMenu;

	// private UIMenu scaryGmail;

	/** The enable roll. */
	private boolean enableRoll = true;

	// #ifdef COMPRESSION
//@	private UICheckbox cb_compression = new UICheckbox(rm
//@			.getString(ResourceIDs.STR_ENABLE_COMPRESSION));
	// #endif

	// #ifdef TLS
//@			private UICheckbox cb_TLS = new UICheckbox(rm
//@					.getString(ResourceIDs.STR_ENABLE_TLS));
	// #endif

	/** The grp_server. */
	private UICombobox grp_server = new UICombobox(rm
			.getString(ResourceIDs.STR_SERVER_TYPE), false);

	/** Progress bar during login. */
	private UIGauge progress_gauge = new UIGauge(rm
			.getString(ResourceIDs.STR_WAIT), false, Gauge.INDEFINITE,
			Gauge.CONTINUOUS_RUNNING);

	/** The btn_login. */
	UIButton btn_login = new UIButton(rm.getString(ResourceIDs.STR_LOGIN));

	private UIVLayout credLayout;

	/** The log layout. */
	protected UIHLayout logLayout;

	/*
	 * The colors for button
	 */
	private int oldBorderColor;

	private int oldButtonColor;

	private int oldFGColor;

// #ifndef GLIDER
			/** The cmd_exit. */
			private UILabel cmd_exit = new UILabel(rm.getString(ResourceIDs.STR_EXIT)
					.toUpperCase());
	// #endif

	/** The cmd_state. */
	private UILabel cmd_state = new UILabel(rm.getString(
			ResourceIDs.STR_CHANGE_STATUS).toUpperCase());

	/** The last_status. */
	private UITextField last_status = null;

	/** The instructions sub menu. */
	private UIMenu instructionsSubMenu;

	// #ifdef BLUENDO_REG
//@
//@	// used to know the steps of the Bluendo registration
//@	// -1 not started, 0 started
//@	private int regStatus = -1;
//@
//@	private BluendoXMLRPC blrpc = null;
//@
//@	private UILabel captchaLabel = null;
//@
//@	private UITextField captchaText = new UITextField("", null, 10,
//@			TextField.NUMERIC | TextField.NON_PREDICTIVE,
//@			UITextField.FORMAT_LOWER_CASE);
//@
//@	private String sessionId = "";
//@
	// #endif

	/*
	 * The subMenu deputed to open the status screen
	 */
	/** The set status. */
	private UIMenu setStatus = new UIMenu("");

	// #mdebug
	private UIButton cmd_debug = new UIButton("debug");
	// #enddebug

	/** true if we must register a new account. */
	private boolean register = false;

	/** The reg. */
	private EventQueryRegistration reg;

	/** The cfg. */
	private Config cfg = Config.getInstance();

	/** local copy of the jid server (for detecting changes). */
	private String jid_server = "";

	/** The hint. */
	private UITextField hint = new UITextField(rm
			.getString(ResourceIDs.STR_NOTE), rm
			.getString(ResourceIDs.STR_LOGIN_HINT), 5000, TextField.UNEDITABLE);

	/** The _register screen. */
	private static RegisterScreen _registerScreen = null;

	/** The instruction label. */
	private UILabel instructionLabel = new UILabel(rm.getString(
			ResourceIDs.STR_INSTRUCTIONS).toUpperCase());

	/** The ex username. */
	private String exUsername = "<username>";

	/** The ex server. */
	private String exServer = "<example.org>";

	private UIFont confFont = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.yup.ui.UIScreen#keyPressed(int)
	 */
	public boolean keyPressed(int key) {
		// trick to remove the "exUsername" in the username textField
		int ka = UICanvas.getInstance().getGameAction(key);
		// int selIndex = this.getSelectedIndex();
		// UIItem selItem = (UIItem) (selIndex >= 0 ? this.getItems().elementAt(
		// selIndex) : null);
		UIItem selItem = this.getSelectedItem();
		if (ka == UICanvas.FIRE
				&& (selItem == this.tf_jid_name || selItem == credLayout
				)) {
			if (getUIJid().indexOf(exUsername) == 0) {
				String tfText = getUIJid();
				String newJid = tfText.substring(exUsername.length(), tfText
						.length());
				setUIJid(newJid);
			}
			if (getUIJid().indexOf(exServer) > 0) {
				String tfText = getUIJid();
				String newJid = tfText.substring(0, tfText.indexOf(exServer));
				setUIJid(newJid);
			}
		}

		if (super.keyPressed(key))
			return true;
		if (enableRoll)
			return RosterScreen.makeRoll(key, this);
		return false;
	}

	/**
	 * Instantiates a new register screen.
	 */
	private RegisterScreen() {
		progress_gauge.cancel();
		resource = new UITextField(rm.getString(ResourceIDs.STR_RESOURCE), cfg
				.getProperty(Config.YUP_RESOURCE, Config.CLIENT_NAME), 50,
				TextField.ANY);
		instructionsSubMenu = UIUtils.easyMenu("", 20, 20, UICanvas
				.getInstance().getWidth(), instructionLabel);
		tf_jid_label.setSubmenu(instructionsSubMenu);
		UIFont xFont = UIConfig.font_body;
		UIFont lFont = UIFont.getFont(xFont.getFace(), UIFont.STYLE_BOLD, xFont
				.getSize());
		tf_jid_label.setFont(lFont);
		tf_jid_label.setFocusable(true);

		if (UICanvas.getInstance().hasPointerMotionEvents()) {
			confFont = UIFont.getFont(xFont.getFace(), UIFont.STYLE_BOLD
					| UIFont.STYLE_ITALIC | UIFont.STYLE_UNDERLINED,
					UIFont.SIZE_MEDIUM);
		} else {
			confFont = UIFont.getFont(xFont.getFace(), UIFont.STYLE_BOLD
					| UIFont.STYLE_ITALIC | UIFont.STYLE_UNDERLINED,
					UIFont.SIZE_SMALL);
		}
		// #ifndef RIM
						int confWidth = 0;
						key_configuration.setFont(confFont);
						key_configuration.setAnchorPoint(UIGraphics.RIGHT);
						key_configuration.setFocusable(true);
						confWidth = confFont.stringWidth(key_configuration.getText()) + 10;
						confLayout.insert(new UISeparator(0), 0, 100,
								UILayout.CONSTRAINT_PERCENTUAL);
						confLayout.insert(key_configuration, 1, confWidth,
								UILayout.CONSTRAINT_PIXELS);
						confLayout.setGroup(false);
		// #endif
		loginLabel = new UILabel(rm.getString(ResourceIDs.STR_LOGIN)
				.toUpperCase());
		loginMenu = UIUtils.easyMenu("", -1, -1, -1, loginLabel);

		UIMenu mainMenu = new UIMenu("");
		this.setMenu(mainMenu);
		mainMenu.append(this.cmd_exit);
		_registerScreen = this;

		tf_jid_name.setFocusable(true);
		tf_pwd.setFocusable(true);

		tf_pwd_confirm.setFocusable(true);
		tf_email.setFocusable(true);
		tf_server.setFocusable(true);

		// Add options to the connecting group
		grp_server.append(rm.getString(ResourceIDs.STR_AUTOMATIC));
		grp_server.append(rm.getString(ResourceIDs.STR_MANUAL));

		// set the values from config
		if (cfg.getProperty(Config.USER) != null) {
			String tempUser = cfg.getProperty(Config.USER, "") + "@"
					+ cfg.getProperty(Config.SERVER, "");
			if (tempUser.compareTo("@") == 0) {
				tempUser = exUsername + "@" + Config.DEFAULT_SERVER;
			} else
				grp_server.setSelectedIndex(1);
			setUIJid(tempUser);
			jid_server = getServer(getUIJid());
			tf_pwd.setText(cfg.getProperty(Config.PASSWORD, ""));
			String savedServer = cfg.getProperty(Config.CONNECTING_SERVER, "");
			tf_server.setText(savedServer);
			if (savedServer.length() == 0)
				grp_server.setSelectedIndex(0);
			// #ifdef COMPRESSION
//@			boolean enable_compression = Short.parseShort(cfg.getProperty(
//@					Config.COMPRESSION, "1")) == 1;
//@			cb_compression.setChecked(enable_compression);
			// #endif
			// #ifdef TLS
//@									boolean enable_TLS = Short.parseShort(cfg.getProperty(Config.TLS,
//@											"0")) == 1;
//@									cb_TLS.setChecked(enable_TLS);
			// #endif
			// append(btn_login);
		}
		logLayout = UIUtils.easyCenterLayout(btn_login, 150);
		setStatus.append(cmd_state);
		String defaultVal =
		// #ifndef RIM
						Config.FALSE;
		// #endif
		UICanvas.getInstance().setQwerty(
				Config.getInstance().getProperty(Config.QWERTY, defaultVal)
						.equals(Config.TRUE));
		UIImage logo = null;
		try {
// #ifndef GLIDER
									setTitle(rm.getString(ResourceIDs.STR_TITLE));
									logo = UIImage.createImage("/icons/lampiro_icon.png");
			// #endif
			logoLabel = new UILabel(logo);
			logoLabel.setFocusable(false);
		} catch (Exception e) {
			// #mdebug
			e.printStackTrace();
			// #enddebug
		}
		logoLayout = UIUtils.easyCenterLayout(logoLabel, logo.getWidth());
		boolean ar = cfg.getProperty(Config.AUTORECONNECT, Config.FALSE)
				.equals(Config.TRUE) ? true : false;
		autoreconnect.setChecked(ar);
		oldBorderColor = btn_login.getBorderColor();
		oldButtonColor = btn_login.getButtonColor();
		oldFGColor = btn_login.getFg_color();
		// #mdebug
		this.append(cmd_debug);
		// #enddebug
	}

	// #ifndef GLIDER
			/**
			 * Place wizard.
			 */
			private void placeWizard() {
				removeAll();
		
				this.setFreezed(true);
		
				this.append(logoLayout);
				this.wizardText = new UITextField("", rm
						.getString(ResourceIDs.STR_WIZARD_TEXT), 1000,
						TextField.UNEDITABLE);
				wizardText.setWrappable(true);
				this.append(wizardText);
		
				buttonLayout = new UIHLayout(2);
				buttonLayout.setGroup(false);
				buttonYes = new UIButton(rm.getString(ResourceIDs.STR_YES));
				buttonYes.setAnchorPoint(UIGraphics.HCENTER);
				buttonNo = new UIButton(rm.getString(ResourceIDs.STR_NO));
				buttonNo.setAnchorPoint(UIGraphics.HCENTER);
				buttonLayout.insert(buttonYes, 0, 50, UILayout.CONSTRAINT_PERCENTUAL);
				buttonLayout.insert(buttonNo, 1, 50, UILayout.CONSTRAINT_PERCENTUAL);
				append(buttonLayout);
		
				this.wizardTextGateways = new UITextField("", rm
						.getString(ResourceIDs.STR_WIZARD_GATEWAYS)
						+ " ", 1000, TextField.UNEDITABLE);
				wizardTextGateways.setWrappable(true);
				this.append(wizardTextGateways);
		
				this.setFreezed(false);
				this.askRepaint();
			}
		
	// #endif

	/** Called to notify that the {@link UIScreen} has become invisible */
	public void hideNotify() {
		int tempbgColor = UIConfig.bg_color;
		UIConfig.bg_color = regbgColor;
		regbgColor = tempbgColor;
	}

	/**
	 * Called to notify that the {@link UIScreen} has become visible.
	 */
	public void showNotify() {
		hideNotify();
		setStatusLabel();
		// #ifndef GLIDER
						String user = cfg.getProperty(Config.USER);
						// the wizard is shown when the configuration is empty for the user
						// or if the wizard has already been shown
						if ((user == null || user.length() == 0) && wizardShown == false)
							placeWizard();
						else
		// #endif
		placeItems();

		// select the login button when shown for the first time
		if (this.contains(logLayout)) {
			this.setSelectedItem(logLayout);
			logLayout.setSelectedItem(btn_login);
			this.askRepaint();
		}
	}

	/**
	 * Sets the status label.
	 */
	public void setStatusLabel() {
		String show = cfg.getProperty(Config.LAST_PRESENCE_SHOW, "");
		String msg = cfg.getProperty(Config.LAST_STATUS_MESSAGE, "");
		String statusText = "";
		statusText += (show.length() > 0 ? show + "\n" : "");
		statusText += (msg.length() > 0 ? msg : "");
		if (statusText.length() > 0) {
			this.last_status = new UITextField(rm
					.getString(ResourceIDs.STR_DISPLAYED_STATUS), statusText,
					1000, TextField.UNEDITABLE);
			this.last_status.setWrappable(true);
			this.last_status.setSubmenu(setStatus);
		} else {
			this.last_status = null;
		}
	}

	/**
	 * Gets the single instance of RegisterScreen.
	 * 
	 * @return single instance of RegisterScreen
	 */
	public static RegisterScreen getInstance() {
		// first delete all the references to the old instance
		if (_registerScreen == null) {
			_registerScreen = new RegisterScreen();
		} else {
			// recreate a new registerscreen unless registerscreen
			// is open or doing setup
			UIScreen cs = UICanvas.getInstance().getCurrentScreen();
			if (cs != _registerScreen
			// #ifndef RIM
											&& cs instanceof KeyScreen == false
			// #endif
			) {
				UICanvas.getInstance().close(_registerScreen);
				_registerScreen = new RegisterScreen();
			}
		}
		return _registerScreen;
	}

	/*
	 * Chooses which controls should be placed on screen depending on user
	 * choices, system settings, stream error event or compile flags. It should
	 * be synch because it is called in many places by different threads.
	 */
	/**
	 * Place items.
	 */
	private void placeItems() {
		UIItem oldSelectedItem = this.getSelectedIndex() > 0 ? (UIItem) this
				.getItems().elementAt(this.getSelectedIndex()) : null;
		this.setFreezed(true);
		removeAll();

		// reset the progress element when logging
		ul.setText(rm.getString(ResourceIDs.STR_LOGGING_IN));
		progress_gauge.setOffset(0);

		append(logoLayout);
		// #ifndef RIM
						append(confLayout);
		// #endif
		appendCredLayout();
		append(grp_advanced);
		checkLogin();

		if (grp_advanced.isChecked()) {
			// #ifdef COMPRESSION
//@			append(this.cb_compression);
			// #endif
			// #ifdef TLS
//@									append(this.cb_TLS);
			// #endif
			append(resource);
			if (this.last_status != null)
				append(this.last_status);
			append(autoreconnect);

			append(grp_server);
			if (grp_server.getSelectedIndex() == 1) {
				append(tf_server);
			}
			append(grp_new_account);
			append(reset_config);
			append(reset_all_data);
		}
		// #debug
		this.append(cmd_debug);
		// #ifdef BLUENDO_REG
//@		if (regStatus == 0) {
//@			removeAll();
//@			append(UIUtils.easyCenterLayout(captchaLabel, 150));
//@			captchaText.setText("");
//@			append(captchaText);
//@			append(logLayout);
//@			logLayout.setBg_color(-1);
//@			checkLogin();
//@		}
		// #endif
		this.setFreezed(false);
		this.askRepaint();
		if (oldSelectedItem != null)
			this.setSelectedItem(oldSelectedItem);
		this.askRepaint();
	}

	private void appendCredLayout() {
		UIGraphics g = UICanvas.getInstance().getCurrentScreen().getGraphics();

		int itemsHeight = 0;
		Vector uiItems = new Vector();
		uiItems.addElement(tf_jid_label);
// #ifndef RIM_LIGHT
				uiItems.addElement(tf_jid_name);
		// #endif
		uiItems.addElement(tf_pwd);
		if (grp_new_account.isChecked()) {
			uiItems.addElement(tf_pwd_confirm);
			uiItems.addElement(tf_email);
		}
		uiItems.addElement(logLayout);
		Enumeration en = uiItems.elements();
		while (en.hasMoreElements()) {
			UIItem ithItem = (UIItem) en.nextElement();
			itemsHeight += ithItem.getHeight(g);
		}
		int paddingSize = 4;
		UIVLayout innerLayout = new UIVLayout(uiItems.size(), itemsHeight + 2
				* paddingSize);
		en = uiItems.elements();
		int i = 0;
		while (en.hasMoreElements()) {
			UIItem ithItem = (UIItem) en.nextElement();
			innerLayout.insert(ithItem, i, ithItem.getHeight(g),
					UILayout.CONSTRAINT_PIXELS);
			i++;
		}
		innerLayout.setPaddings(paddingSize, paddingSize);
		innerLayout.setGroup(false);
		innerLayout.setRoundRect(true);
		credLayout = new UIVLayout(1, itemsHeight + paddingSize * 4);
		credLayout.insert(innerLayout, 0, 100, UILayout.CONSTRAINT_PERCENTUAL);
		credLayout.setPaddings(paddingSize, paddingSize);
		credLayout.setGroup(false);
		append(credLayout);
		innerLayout.setBg_color(0xFFFFFF);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.yup.ui.UIScreen#menuAction(it.yup.ui.UIMenu, it.yup.ui.UIItem)
	 */
	public void menuAction(UIMenu menu, UIItem c) {
		if (c == cmd_exit) {
// #ifndef GLIDER
									LampiroMidlet.exit();
			// #endif
		} else if (c == instructionLabel) {
			int labelWidth = UICanvas.getInstance().getWidth() - 20;
			UILabel hint = new UILabel(rm.getString(ResourceIDs.STR_HINT));
			UIMenu instructionsMenu = UIUtils.easyMenu(rm
					.getString(ResourceIDs.STR_INSTRUCTIONS), 10, 30,
					labelWidth, hint);
			instructionsMenu.cancelMenuString = "";
			instructionsMenu.selectMenuString = rm.getString(
					ResourceIDs.STR_CONTINUE).toUpperCase();
			hint.setWrappable(true, labelWidth);
			this.addPopup(instructionsMenu);
		} else if (c == cmd_state) {
			StatusScreen ssc = new StatusScreen();
			UICanvas.getInstance().open(ssc, true, this);
		} else if (c == loginLabel) {
			this.itemAction(this.btn_login);
		}

	}

	/**
	 * Login.
	 * 
	 * @param goOnline
	 *            the go online
	 */
	void login(final boolean goOnline) {
		enableRoll = false;
		String resourceString = this.resource.getText();
		cfg.setProperty(Config.YUP_RESOURCE, resourceString);
		cfg.saveToStorage();
		// first do all the "clean" operation
		if (this.reset_config.isChecked()) {
			this.cfg.resetStorage(true);
		}

		if (this.reset_all_data.isChecked()) {
			String[] rss = RecordStore.listRecordStores();
			for (int i = 0; i < rss.length; i++) {
				String rs = rss[i];
				try {
					RecordStore.deleteRecordStore(rs);
				} catch (Exception e) {
					// #mdebug
					System.out.println("Error in cleaning recordstores");
					e.printStackTrace();
					// #enddebug
				}
			}
			this.cfg.resetStorage(true);
		}

		// if registering check the pwds coherency
		if (grp_new_account.isChecked()) {
			if (wrongPwd != null) {
				this.removePopup(wrongPwd);
			}
			if (tf_pwd.getText().equals(tf_pwd_confirm.getText()) == false) {
				UILabel gatewayHint = new UILabel(rm
						.getString(ResourceIDs.STR_WRONG_PWD));
				int canvasWidth = UICanvas.getInstance().getWidth() - 20;
				wrongPwd = UIUtils.easyMenu(rm
						.getString(ResourceIDs.STR_INSTRUCTIONS), 10, 30,
						canvasWidth, gatewayHint, "", rm.getString(
								ResourceIDs.STR_CONTINUE).toUpperCase());
				gatewayHint.setWrappable(true, canvasWidth);
				this.addPopup(wrongPwd);
				return;
			}
		}
		removeAll();
		ul.setAnchorPoint(UIGraphics.HCENTER | UIGraphics.TOP);
		append(ul);
		append(progress_gauge);
		progress_gauge.cancel();
		progress_gauge.start();
		// if compression is enabled even TLS is
		hint.setWrappable(true);
		append(hint);
		UIHLayout uhl = UIUtils.easyCenterLayout(this.but_cancel,
				UIConfig.font_body.stringWidth(but_cancel.getText()) + 20);
		append(uhl);

		String jabText = getUIJid();
		if (jabText.indexOf('@') >= 0) {
			String tempUser = Contact.user(jabText);
			String tempServer = Contact.domain(jabText);
			jabText = Utils.jabberify(tempUser, -1) + '@'
					+ Utils.jabberify(tempServer, -1);
			setUIJid(jabText);
		}

		new Thread() {
			public void run() {
				// #ifdef BLUENDO_REG
//@				String server = getServer(tf_jid_name.getText());
//@				if (register && server.compareTo(Config.DEFAULT_SERVER) == 0) {
//@					if (regStatus == -1) {
//@						try {
//@							synchronized (UICanvas.getLock()) {
//@								bluendoReg();
//@							}
//@						} catch (Exception e) {
							// #mdebug
//@							Logger.log("In Bluendorreg :" + e.getClass());
//@							e.printStackTrace();
							// #enddebug
//@						}
//@					}
//@				} else {
//@					xmppLogin(register, goOnline);
//@				}
				// #endif
// #ifndef BLUENDO_REG
												xmppLogin(register, goOnline);
				// #endif
			}
		}.start();
	}

	// #ifdef BLUENDO_REG
//@	private void bluendoReg() {
//@		Element req = new Element(XmppConstants.BLUENDO_XMLRPC, "req");
//@		req.setAttribute("target", XmppConstants.BLUENDO_REGISTER);
//@		Element method = req.addElement(null, "method");
//@		method.setAttribute("name", "start_register");
//@		Element params = req.addElement(null, "params");
//@
//@		sessionId = this.getUIJid() + System.currentTimeMillis();
//@		Element uidParam = params.addElement(null, "param");
//@		uidParam.setAttribute("name", "session_id");
//@		uidParam.setAttribute("type", "string");
//@		uidParam.addText(sessionId);
//@
//@		String bluendoRegServer = null;
		// #ifdef DEBUG
//@		bluendoRegServer = Config.DEFAULT_SERVER + ":5221";
		// #endif
// #ifndef DEBUG
//@								bluendoRegServer = Config.DEFAULT_SERVER + ":5221";
		// #endif
//@		blrpc = new BluendoXMLRPC("socket://" + bluendoRegServer);
//@
//@		Element res = null;
//@		try {
//@			blrpc.open();
//@			blrpc.write(req);
//@			blrpc.flush();
//@			res = blrpc.read();
//@		} catch (Exception e) {
//@			handleError(null);
			// #mdebug
//@			e.printStackTrace();
			// #enddebug
//@			return;
//@		}
//@
//@		String status = res.getAttribute("status");
//@		if (status.compareTo("200") != 0) {
//@			Element elError = res.getChildByName(null, Iq.T_ERROR);
//@			if (status.compareTo("500") == 0)
//@				handleError(null);
//@			else if (elError != null)
//@				handleError(elError.getText());
//@			return;
//@		}
//@		Element result = res.getChildByName(null, "result");
//@		if (result.getAttribute("enable_captcha").equals("True")) {
//@			String resString = result.getText();
//@			byte[] imgBytes = Base64.decode(resString);
//@			UIImage img = UIImage.createImage(imgBytes, 0, imgBytes.length);
//@			captchaLabel = new UILabel(img);
//@			regStatus = 0;
//@			progress_gauge.cancel();
//@			placeItems();
//@		} else {
//@			// if i am not asking captcha it means i have to do
//@			// the login now in case of success
//@			if (bluendoEndReg("0000") == false) {
//@				regStatus = -1;
//@			} else {
//@				regStatus = 1;
//@				login(true);
//@			}
//@		}
//@	}
//@
//@	/*
//@	 * Returns true when the registration is successeful
//@	 */
//@	private boolean bluendoEndReg(String captcha) {
//@		Element req = new Element(XmppConstants.BLUENDO_XMLRPC, "req");
//@		req.setAttribute("target", XmppConstants.BLUENDO_REGISTER);
//@		Element method = req.addElement(null, "method");
//@		method.setAttribute("name", "register");
//@		Element params = req.addElement(null, "params");
//@
//@		Element uidParam = params.addElement(null, "param");
//@		uidParam.setAttribute("name", "session_id");
//@		uidParam.setAttribute("type", "string");
//@		uidParam.addText(sessionId);
//@
//@		uidParam = params.addElement(null, "param");
//@		uidParam.setAttribute("name", "jid");
//@		uidParam.setAttribute("type", "string");
//@		uidParam.addText(Utils.getStringUTF8(Base64.encode(Utils
//@				.getBytesUtf8(this.getUIJid()))));
//@
//@		uidParam = params.addElement(null, "param");
//@		uidParam.setAttribute("name", "password");
//@		uidParam.setAttribute("type", "string");
//@		uidParam.addText(Utils.getStringUTF8(Base64.encode(Utils
//@				.getBytesUtf8(this.tf_pwd.getText()))));
//@
//@		uidParam = params.addElement(null, "param");
//@		uidParam.setAttribute("name", "email");
//@		uidParam.setAttribute("type", "string");
//@		String email = this.tf_email.getText();
//@		email = email != null ? email : "";
//@		uidParam.addText(Utils.getStringUTF8(Base64.encode(Utils
//@				.getBytesUtf8(email))));
//@
//@		uidParam = params.addElement(null, "param");
//@		uidParam.setAttribute("name", "captcha");
//@		uidParam.setAttribute("type", "string");
//@		uidParam.addText(captcha);
//@
//@		Element res = null;
//@		try {
//@			blrpc.write(req);
//@			blrpc.flush();
//@			res = blrpc.read();
//@		} catch (Exception e) {
//@			handleError(null);
			// #mdebug
//@			e.printStackTrace();
			// #enddebug
//@			return false;
//@		}
//@
//@		String status = res.getAttribute("status");
//@		if (status.compareTo("200") != 0) {
//@			Element elError = res.getChildByName(null, Iq.T_ERROR);
//@			if (status.compareTo("500") == 0)
//@				handleError(null);
//@			else if (elError != null)
//@				handleError(elError.getText());
//@			return false;
//@		}
//@		Element result = res.getChildByName(null, "result");
//@		String resString = result.getText();
//@		if (resString.equals("ok")) {
//@			regStatus = 1;
//@			grp_new_account.setChecked(false);
//@			register = false;
//@			// placeItems();
//@		} else {
//@			handleError(resString);
//@			return false;
//@		}
//@		try {
//@			blrpc.close();
//@		} catch (IOException e) {
			// #mdebug
//@			e.printStackTrace();
			// #enddebug
//@		}
//@		return true;
//@	}
//@
//@	private void handleError(String errorString) {
//@		UICanvas.showAlert(Alerts.ERROR, rm
//@				.getString(ResourceIDs.STR_REGFAIL_TITLE),
//@				errorString != null ? errorString : rm
//@						.getString(ResourceIDs.STR_REGFAIL_DESC));
//@		try {
//@			this.blrpc.flush();
//@			this.blrpc.close();
//@		} catch (Exception e) {
			// #mdebug
//@			System.out.println("Error in bluendo registration");
//@			e.printStackTrace();
			// #enddebug
//@		}
//@		regStatus = -1;
//@		progress_gauge.setOffset(0);
//@		ul.setText(rm.getString(ResourceIDs.STR_LOGGING_IN));
//@		progress_gauge.cancel();
//@		placeItems();
//@	}
//@
	// #endif

	/**
	 * Xmpp login.
	 * 
	 * @param newUser
	 *            the new user
	 * @param goOnline
	 *            the go online
	 */
	private void xmppLogin(boolean newUser, boolean goOnline) {
		String user = getUser(getUIJid());
		String server = getServer(getUIJid());

		String cfg_user = cfg.getProperty(Config.USER);
		String cfg_server = cfg.getProperty(Config.SERVER);
		boolean clientInitialized = true;
		if ((cfg_user == null || !cfg_user.equals(user))
				|| (cfg_server == null || !cfg_server.equals(server))
				|| Config.FALSE.equals(cfg.getProperty(
						Config.CLIENT_INITIALIZED, Config.FALSE))) {
			cfg.setProperty(Config.CLIENT_INITIALIZED, Config.FALSE);
			clientInitialized = false;
		}
		cfg.setProperty(Config.USER, user);
		cfg.setProperty(Config.SERVER, server);
		cfg.setProperty(Config.PASSWORD, tf_pwd.getText());
		cfg.setProperty(Config.EMAIL, tf_email.getText());
		// #ifdef COMPRESSION
//@		String enableCompression = "0";
//@		enableCompression = (cb_compression.isChecked() ? 1 : 0) + "";
//@		cfg.setProperty(Config.COMPRESSION, enableCompression);
		// #endif
		// #ifdef TLS
//@						String enableTlS = "0";
//@						enableTlS = (cb_TLS.isChecked() ? 1 : 0) + "";
//@						cfg.setProperty(Config.TLS, enableTlS);
		// #endif
		cfg.setProperty(Config.AUTORECONNECT,
				autoreconnect.isChecked() ? Config.TRUE : Config.FALSE);
		if (grp_server.getSelectedIndex() == 0) {
			if (clientInitialized == false) {
				String serverQuery = srvQuery();
				cfg.setProperty(Config.CONNECTING_SERVER, serverQuery);
				tf_server.setText(serverQuery);
			}
		} else {
			cfg.setProperty(Config.CONNECTING_SERVER, tf_server.getText());
		}
		// #mdebug
		// Logger.log("user:" + cfg.getProperty(Config.USER) + " server:"
		// + cfg.getProperty(Config.SERVER) + " password:"
		// + cfg.getProperty(Config.PASSWORD) + " email:"
		// + cfg.getProperty(Config.EMAIL) + " connecting-server:"
		// + cfg.getProperty(Config.CONNECTING_SERVER));
		// #enddebug
		cfg.saveToStorage();
		// Get the XMPP client
		XMPPClient xmpp = XMPPClient.getInstance();
		xmpp.setMUCGroup(rm.getString(ResourceIDs.STR_GROUP_CHAT));
		// #ifdef COMPRESSION
//@		xmpp.addCompression = cb_compression.isChecked();
		// #endif
		// #ifdef TLS
//@						xmpp.addTLS = cb_TLS.isChecked();
		// #endif
		// this rosterListener must be set before the Roster starts
		// calling its methods
		RosterScreen rs = RosterScreen.getInstance();
		xmpp.setXmppListener(rs);

		Config cfg = Config.getInstance();
		boolean newCredentials = Config.FALSE.equals(cfg
				.getProperty(Config.CLIENT_INITIALIZED));
		xmpp.createStream(newUser, newCredentials);

		EventQuery qAuth = new EventQuery(EventQuery.ANY_EVENT, null, null);
		reg = EventDispatcher.addEventListener(qAuth, RegisterScreen.this);
		xmpp.openStream(goOnline);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.yup.xmlstream.StreamEventListener#gotStreamEvent(java.lang.String,
	 * java.lang.Object)
	 */
	public void gotStreamEvent(String event, Object source) {
		try {
			synchronized (UICanvas.getLock()) {
				XMPPClient client = XMPPClient.getInstance();
				if (EventDispatcher.STREAM_CONNECTED.equals(event)) {
					streamConnected = true;
					ul.setText(rm.getString(ResourceIDs.STR_CONNECTED));
					progress_gauge.setOffset(30);
					this.askRepaint();
					return;
				} else if (EventDispatcher.TLS_INITIALIZED.equals(event)) {
					ul.setText(rm.getString(ResourceIDs.STR_TLS_INITIALIZED));
					progress_gauge.setOffset(50);
					this.askRepaint();
					return;
				} else if (EventDispatcher.COMPRESSION_INITIALIZED
						.equals(event)) {
					ul
							.setText(rm
									.getString(ResourceIDs.STR_COMPRESSION_INITIALIZED));
					progress_gauge.setOffset(60);
					this.askRepaint();
					return;
				} else if (EventDispatcher.STREAM_AUTHENTICATED.equals(event)) {
					ul.setText(rm.getString(ResourceIDs.STR_AUTHENTICATED));
					progress_gauge.setOffset(70);
					this.askRepaint();
					return;
				} else if (EventDispatcher.STREAM_INITIALIZED.equals(event)) {
					ul.setText(rm.getString(ResourceIDs.STR_INITIALIZED));
					progress_gauge.setOffset(90);
					this.askRepaint();
				}
				if (EventDispatcher.STREAM_ERROR.equals(event)
						|| EventDispatcher.CONNECTION_FAILED.equals(event)
						|| EventDispatcher.REGISTRATION_FAILED.equals(event)
						|| EventDispatcher.NOT_AUTHORIZED.equals(event)
						|| EventDispatcher.CONNECTION_LOST.equals(event)) {
					reg.remove();
					try {
						client.closeStream();
					} catch (Exception e) {
						// #mdebug
						System.out.println(e);
						// #enddebug
					}

					String description = null;
					if (EventDispatcher.CONNECTION_FAILED.equals(event)) {
						if (streamConnected == false) {
							cfg.setProperty(Config.CLIENT_INITIALIZED,
									Config.FALSE);
							grp_server.setSelectedIndex(0);
						}
						description = rm
								.getString(ResourceIDs.STR_CONNECTION_FAILED);
					} else if (EventDispatcher.CONNECTION_LOST.equals(event)) {
						description = rm
								.getString(ResourceIDs.STR_CONNECTION_LOST);
					} else if (EventDispatcher.REGISTRATION_FAILED
							.equals(event)) {
						description = rm
								.getString(ResourceIDs.STR_REG_UNALLOWED);
					} else if (EventDispatcher.NOT_AUTHORIZED.equals(event)) {
						description = rm
								.getString(ResourceIDs.STR_WRONG_USERNAME);
					} else {
						description = (String) source;
					}
					if ("conflict".equals(source)) {
						description = rm
								.getString(ResourceIDs.STR_ALREADY_EXIST);
					}

					// UITextField error = new UITextField()


					String errTitle = register ? rm
							.getString(ResourceIDs.STR_REGFAIL_TITLE) : rm
							.getString(ResourceIDs.STR_LOGFAIL_TITLE);
					String errString = (register ? (rm
							.getString(ResourceIDs.STR_REGFAIL_DESC)) : (rm
							.getString(ResourceIDs.STR_LOGFAIL_DESC)))
							+ " " + description;
					// #ifndef RIM
															UICanvas.showAlert(Alerts.ERROR, errTitle,
																	errString);
					// #endif
					this.progress_gauge.cancel();
					placeItems();

				} else if (EventDispatcher.STREAM_INITIALIZED.equals(event)) {
					this.progress_gauge.cancel();
					reg.remove();

					// to clean the screen before the roster paint
					this.removeAll();

					RegisterScreen._registerScreen = null;
					UICanvas.getInstance().close(this);
				}
			}
		} catch (Exception e) {
			// #mdebug
			Logger.log("In Registerscreen event handling:" + e.getClass());
			e.printStackTrace();
			// #enddebug
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.yup.ui.UIScreen#itemAction(it.yup.ui.UIItem)
	 */
	public void itemAction(UIItem item) {
		if (item == instructionLabel || item == tf_jid_label) {
			menuAction(null, instructionLabel);
			// #ifndef RIM
								} else if (item == key_configuration) {
									KeyScreen ks = new KeyScreen();
									UICanvas.getInstance().open(ks, true, this);
									ks.checkKeys();
			// #endif
			// #ifndef GLIDER
								} else if (item == buttonYes) {
									this.tf_jid_name.setText(exUsername + "@" + exServer);
									wizardShown = true;
									grp_new_account.setChecked(false);
									placeItems();
								} else if (item == buttonNo) {
						
									wizardShown = true;
									grp_new_account.setChecked(true);
									placeItems();
									remove(tf_email);
									this.itemAction(this.grp_new_account);
			// #endif
		} else if (item == grp_new_account) {
			register = grp_new_account.isChecked();
			placeItems();
			// #mdebug
		} else if (item == cmd_debug) {
			DebugScreen debugScreen = new DebugScreen();
			UICanvas.getInstance().open(debugScreen, true);
			// #enddebug
		} else if (item == but_cancel) {
			progress_gauge.cancel();
			try {
				XMPPClient.getInstance().closeStream();
			} catch (Exception e) {
				// during closing connection
				// exceptions from the transport can be generated
				// #mdebug
				System.out.println(e);
				// #enddebug
			}
			// #ifdef BLUENDO_REG
//@			regStatus = -1;
			// #endif
			placeItems();
		} else if (item == grp_server) {
			if (grp_server.getSelectedIndex() == 1) {
				if (tf_server.getText().length() == 0) {
					String serString = getUIJid();
					tf_server.setText(getServer(serString) + ":5222");
				}
				if (this.getItems().contains(tf_server) == false) {
					append(tf_server);
				}
			} else {
				remove(tf_server);
			}
			return;
		} else if (item == grp_advanced) {
			placeItems();
			return;
		} else if (item == tf_jid_name || item == tf_pwd
				|| item == this.tf_pwd_confirm) {
			if (grp_server.getSelectedIndex() == 1) {
				String jsvr = getServer(getUIJid());
				if (!jid_server.equals(jsvr)) {
					jid_server = jsvr;
					grp_server.setSelectedIndex(0);
					remove(tf_server);
				}
			}
			placeItems();
		} else if (item == btn_login) {
			// #ifdef BLUENDO_REG
//@			if (regStatus == 0) {
//@				if (bluendoEndReg(this.captchaText.getText()) == false) {
//@					regStatus = -1;
//@				} else {
//@					regStatus = 1;
//@					login(true);
//@				}
//@			} else
				// #endif
				login(true);
			return;
		} else if (item == this.last_status) {
			menuAction(setStatus, cmd_state);
		} else if (item == this.hint) {
			return;
		}

		// check if we must enable / disable
		checkLogin();
	}

	/**
	 * check if we must enable the login.
	 */
	private void checkLogin() {
		String items[] = null;
		boolean checkmail[] = null;

		if (register == false) {
			items = new String[3];
			checkmail = new boolean[] { true, false, false };
		} else {
			// #ifndef GLIDER
									items = new String[4];
									checkmail = new boolean[] { true, false, false, false };
			// #endif
		}

		items[0] = getUIJid();
		items[1] = tf_pwd.getText();
		if (grp_server.getSelectedIndex() == 1) {
			items[2] = tf_server.getText();
		}
		if (register) {
			items[3] = tf_pwd_confirm.getText();
		}

		boolean complete = true;
		for (int i = 0; i < items.length; i++) {
			if (items[i] == null) {
				continue;
			}
			String s = items[i];
			if (s.length() == 0) {
				complete = false;
				break;
			} else if (checkmail[i] && !Utils.is_email(s)) {
				complete = false;
				break;
			}
		}

		if (complete) {
			btn_login.setBorderColor(oldBorderColor);
			btn_login.setButtonColor(oldButtonColor);
			btn_login.setFg_color(oldFGColor);
			btn_login.setFocusable(true);
		} else if (!complete) {
			btn_login.setBorderColor(0xD3D3D3);
			btn_login.setFg_color(0xD3D3D3);
			btn_login.setButtonColor(0xA9A9A9);
			btn_login.setFocusable(false);
		}
		UIMenu tempSubmenu = (complete == true ? loginMenu : null);
		tf_jid_name.setSubmenu(tempSubmenu);
		grp_new_account.setSubmenu(tempSubmenu);
		tf_pwd_confirm.setSubmenu(tempSubmenu);
		tf_pwd.setSubmenu(tempSubmenu);
		btn_login.setSubmenu(tempSubmenu);
		grp_advanced.setSubmenu(tempSubmenu);
		resource.setSubmenu(tempSubmenu);
		tf_server.setSubmenu(tempSubmenu);
		grp_server.setSubmenu(tempSubmenu);
		reset_config.setSubmenu(tempSubmenu);
		reset_all_data.setSubmenu(tempSubmenu);
		// logoLabel.setSubmenu(tempSubmenu);
		String loginString = rm.getString(ResourceIDs.STR_LOGIN);
		if (grp_new_account.isChecked() == false) {
			this.btn_login.setText(loginString);
		} else {
			// #ifdef BLUENDO_REG
//@			String regString = rm.getString(ResourceIDs.STR_REGISTER);
//@			String captchaString = rm.getString(ResourceIDs.STR_CAPTCHA);
//@			if (getServer(tf_jid_name.getText()).compareTo(
//@					Config.DEFAULT_SERVER) != 0) {
//@				this.btn_login.setText(regString);
//@				loginLabel.setText(regString.toUpperCase());
//@			} else {
//@				if (this.regStatus == -1) {
//@					this.btn_login.setText(regString);
//@					loginLabel.setText(regString.toUpperCase());
//@				} else if (this.regStatus == 0) {
//@					this.btn_login.setText(captchaString);
//@					loginLabel.setText(captchaString.toUpperCase());
//@				} else if (this.regStatus == 1) {
//@					this.btn_login.setText(loginString);
//@					loginLabel.setText(loginString.toUpperCase());
//@				}
//@			}
			// #endif
// #ifndef BLUENDO_REG
									this.btn_login.setText(rm.getString(ResourceIDs.STR_REGISTER));
			// #endif
		}
		// #ifdef COMPRESSION
//@		cb_compression.setSubmenu(tempSubmenu);
		// #endif
		// #ifdef TLS
//@						cb_TLS.setSubmenu(tempSubmenu);
		// #endif
	}

	/**
	 * Srv query.
	 * 
	 * @return the string
	 */
	private String srvQuery() {

		String host = getServer(getUIJid());
		try {
			String domain = host;
			String conString = Config.SRV_QUERY_PATH + domain;
			HttpConnection conn = (HttpConnection) NetConnector.open(conString);
			InputStream is = conn.openInputStream();
			int b = -1;
			StringBuffer buffer = new StringBuffer();
			while ((b = is.read()) != -1) {
				buffer.append((char) b);
			}
			String result = buffer.toString();
			if ("_:-1".equals(result)) {
				result = host + ":5222";
			}
			return result;
		} catch (SecurityException e) {
			return host + ":5222";
		} catch (IOException e) {
			return host + ":5222";
		}

	}

	/**
	 * Gets the server.
	 * 
	 * @param jid
	 *            the jid
	 * @return the server
	 */
	private String getServer(String jid) {
		int server_idx = jid.indexOf("@");
		if (server_idx >= 0) {
			return jid.substring(server_idx + 1);
		} else {
			return "";
		}
	}

	public String getUIJid() {
// #ifndef RIM_LIGHT
				return tf_jid_name.getText();
		// #endif
	}

	public void setUIJid(String fullJid) {
// #ifndef RIM_LIGHT
				tf_jid_name.setText(fullJid);
		// #endif
	}

	/**
	 * Gets the user.
	 * 
	 * @param jid
	 *            the jid
	 * @return the user
	 */
	private String getUser(String jid) {
		int server_idx = jid.indexOf("@");
		if (server_idx >= 0) {
			return jid.substring(0, server_idx);
		} else {
			return "";
		}
	}
}
