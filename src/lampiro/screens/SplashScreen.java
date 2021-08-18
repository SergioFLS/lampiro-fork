/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: SplashScreen.java 2442 2011-02-02 23:27:52Z luca $
 */

package lampiro.screens;

import it.yup.ui.UICanvas;
import it.yup.ui.UIConfig;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UILayout;
import it.yup.ui.UIMenu;
import it.yup.ui.UIScreen;
import it.yup.ui.UITextField;
import it.yup.ui.UIUtils;
import it.yup.ui.UIVLayout;


// #ifndef RIM

import javax.microedition.lcdui.TextField;

// #endif

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.util.Utils;
import it.yup.client.Config;
import it.yup.client.XMPPClient;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.ui.wrappers.UIImage;

import java.util.TimerTask;

public class SplashScreen extends UIScreen {

	// In this screen it is hard to use FIRE and Menu keys
	// since the software may be uniintialized so
	// lots of shortcuts, pointing events and controls are handled manually like
	// this
	//
	class UISplashTextField extends UITextField {
		public UISplashTextField(String label, String text, int maxSize,
				int constraints) {
			super(label, text, maxSize, constraints);
		}

		public boolean keyPressed(int key) {
			boolean retVal = super.keyPressed(key);
			int ga = UICanvas.getInstance().getGameAction(key);
			if (ga == UICanvas.FIRE) { return false; }
			return retVal;
		}
	};

	private static ResourceManager rm = ResourceManager.getManager();

	// #ifndef RIM
	private UIMenu helpMenu;

	private UISplashTextField helpField = null;

	// #endif


	public SplashScreen() {
		try {
			/*
			 * Load the configuration in UIConfig
			 */
			UIConfig.cancelMenuString = rm.getString(ResourceIDs.STR_CANCEL)
					.toUpperCase();
			UIConfig.selectMenuString = rm.getString(ResourceIDs.STR_SELECT)
					.toUpperCase();
			UIConfig.menuString = rm.getString(ResourceIDs.STR_MENU)
					.toUpperCase();
			UIConfig.optionsString = rm.getString(ResourceIDs.STR_OPTIONS)
					.toUpperCase();

			final UIVLayout uvl = new UIVLayout(5, -1);
			final UILabel dummyLabel = new UILabel("");
			int dummyHeight = 50;
			uvl.insert(dummyLabel, 0, dummyHeight,
					UILayout.CONSTRAINT_PERCENTUAL);

			setTitle(Config.CLIENT_NAME);
// #ifndef GLIDER
						
									UIImage logo = UIImage.createImage("/icons/lampiro_icon.png");
									UILabel ul = new UILabel("Loading Lampiro...");
			// #endif
			UILabel up = new UILabel(logo);
			up.setAnchorPoint(UIGraphics.HCENTER | UIGraphics.VCENTER);
			uvl
					.insert(up, 1, logo.getHeight() + 10,
							UILayout.CONSTRAINT_PIXELS);

			ul.setAnchorPoint(UIGraphics.HCENTER | UIGraphics.VCENTER);
			uvl.insert(ul, 2, UIConfig.font_body.getHeight(),
					UILayout.CONSTRAINT_PIXELS);
			uvl.setGroup(false);
			uvl.insert(dummyLabel, 3, dummyHeight,
					UILayout.CONSTRAINT_PERCENTUAL);
			uvl.insert(dummyLabel, 4, dummyHeight,
					UILayout.CONSTRAINT_PERCENTUAL);
			append(uvl);

		} catch (Exception ex) {
			// #mdebug
			ex.printStackTrace();
			Logger.log("In splash screen:" + ex.getClass());
			// #enddebug
		}

		// #ifndef ADVER
						Utils.tasks.schedule(new TimerTask() {
							public void run() {
								checkKeys();
							}
						}, 2000);
		// #endif
	}

	// #ifndef RIM
	public void menuAction(UIMenu menu, UIItem item) {
		if (item == this.helpField) keyPressed(UICanvas.getInstance()
				.getKeyCode(UICanvas.FIRE));
		else if (menu == helpMenu) keyPressed(UICanvas.getInstance()
				.getKeyCode(UICanvas.FIRE));
	}

	public boolean keyPressed(int kc) {

		if (helpMenu == null) return super.keyPressed(kc);

		int ga = UICanvas.getInstance().getGameAction(kc);
		switch (ga) {
			// case Canvas.UP:
			// case Canvas.DOWN:
			// case Canvas.LEFT:
			// case Canvas.RIGHT:
			// return super.keyPressed(kc);
			case UICanvas.FIRE:
				this.removePopup(this.helpMenu);
				this.helpMenu = null;
				UICanvas.getInstance().open(RegisterScreen.getInstance(), true);
				UICanvas.getInstance().close(SplashScreen.this);
				return true;
		}
		return super.keyPressed(kc);
	}

	// #endif


	private void checkKeys() {
		synchronized (UICanvas.getLock()) {
			try {
				_checkKeys();
			} catch (Exception e) {
				// #mdebug
				Logger.log("In splash screen:" + e.getClass());
				UILabel label = new UILabel(e.getClass().toString());
				label.setWrappable(true, 170);
				addPopup(UIUtils.easyMenu("Error in checkKeys ", 20, 20, 180,
						label));
				e.printStackTrace();
				// #enddebug
			}
		}
	}

	private void _checkKeys() {
		int q;

		Config cfg = Config.getInstance();
		String keys = cfg.getProperty(Config.CANVAS_KEYS);

		if (keys != null && (q = keys.indexOf(',')) != -1) {
			int l = Integer.parseInt(keys.substring(0, q));
			int r = Integer.parseInt(keys.substring(q + 1));
			UICanvas.setMenuKeys(l, r);

			UIScreen sc = null;
// #ifndef GLIDER
									sc = RegisterScreen.getInstance();
			// #endif
			UICanvas.getInstance().open(sc, true);
			UICanvas.getInstance().close(SplashScreen.this);
			if (cfg.updatedConfig) {
				cfg.updatedConfig = false;
				String upText = Config.CLIENT_NAME + " "
						+ rm.getString(ResourceIDs.STR_UPDATE_TEXT);
				upText = Utils.replace(upText, "SOFTWARE_VERSION", cfg
						.getProperty(Config.VERSION));
				UILabel label = new UILabel(upText);
				int avWidth = UICanvas.getInstance().getWidth() - 60;
				label.setWrappable(true, avWidth - 10);
				UIMenu popup = UIUtils.easyMenu(rm
						.getString(ResourceIDs.STR_UPDATE), 30, 30, avWidth,
						label, "", rm.getString(ResourceIDs.STR_CONTINUE));
				sc.addPopup(popup);
			}
		} else {
			// save actual configuration
			// SplashScreen.this.close = new
			// UIButton(rm.getString(ResourceIDs.STR_CLOSE));
			keys = UICanvas.MENU_LEFT + "," + UICanvas.MENU_RIGHT;
			cfg.setProperty(Config.CANVAS_KEYS, keys);
			cfg.saveToStorage();

// #ifndef RIM
			String help = rm.getString(ResourceIDs.STR_KEY_HELP);
			help = help.replace('<', '\n');

			helpField = new UISplashTextField("", help, help.length(),
					TextField.UNEDITABLE);
			helpField.setWrappable(true);
			helpField.setAutoUnexpand(false);
			helpField.setExpandable(false);

			helpMenu = UIUtils.easyMenu(rm.getString(ResourceIDs.STR_HELP), 1,
					20, UICanvas.getInstance().getWidth() - 2, helpField);
			helpMenu.selectMenuString = "";
			((UIItem) helpMenu.getItems().elementAt(0)).setFocusable(true);
			helpMenu.setSelectedIndex(1);
			helpMenu.cancelMenuString = "";
			// UIHLayout uhl = UIHLayout.easyCenterLayout(close, 80);
			// helpMenu.append(uhl);
			this.addPopup(helpMenu);
			this.askRepaint();
			helpField.expand();
			// #endif
		}
	}

	public boolean askClose() {
		return false;
	}
}
