/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: OptionsScreen.java 2329 2010-11-16 14:12:50Z luca $
 */

package lampiro.screens;

import it.yup.ui.UIButton;
import it.yup.ui.UICanvas;
import it.yup.ui.UICheckbox;
import it.yup.ui.UICombobox;
import it.yup.ui.UIConfig;
import it.yup.ui.UIGauge;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UIScreen;
import it.yup.ui.UITextField;
import it.yup.ui.UIUtils;
import it.yup.ui.wrappers.UIFont;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.util.Utils;
import it.yup.util.Alerts;
import it.yup.client.Config;

import javax.microedition.lcdui.TextField;

// #ifdef UI
import lampiro.LampiroMidlet;

// #endif

public class OptionsScreen extends UIScreen {

	private static ResourceManager rm = ResourceManager.getManager();

	private UIButton cmd_save = new UIButton(rm.getString(ResourceIDs.STR_SAVE));
	private UIButton cmd_cancel = new UIButton(rm
			.getString(ResourceIDs.STR_CANCEL));
	private UIButton cmd_reset = new UIButton(rm
			.getString(ResourceIDs.STR_RESET_OPTIONS));

	private UICombobox ch_vibrate;
	private UICombobox ch_tone;
	public UICombobox color;
	public UICombobox resolution;
	public UICombobox font_size;
	public UICombobox menu_font_size;

	private UITextField tf_keepalive;
	private UITextField history_size;
	private UICheckbox qwerty;

	/*
	 * The old color index used in case of cancel saving
	 */
	private int oldColorIndex = 0;

	/*
	 * The old font index used in case of cancel saving
	 */
	private int oldFontIndex = 0;

	/*
	 * The old font index used in case of cancel saving
	 */
	private int oldMenuFontIndex = 0;


	private UIGauge g_volume;

	public OptionsScreen() {
		setTitle(rm.getString(ResourceIDs.STR_OPTIONS_SETUP));
		boolean flags[] = new boolean[2];
		Config cfg = Config.getInstance();
		String selected_status = cfg.getProperty(
				Config.VIBRATION_AND_TONE_SETTINGS, "1");

		// prepare the form
		// tone and vibration settings
		append(new UILabel(rm.getString(ResourceIDs.STR_WHEN_NEW_MESSAGES)));
		ch_vibrate = new UICombobox(rm.getString(ResourceIDs.STR_VIBRATE_IF),
				true);
		ch_vibrate.append(rm.getString(ResourceIDs.STR_HIDDEN));
		ch_vibrate.append(rm.getString(ResourceIDs.STR_SHOW));
		flags = Utils.str2flags(selected_status, 0, 2);
		ch_vibrate.setSelectedFlags(flags);
		append(ch_vibrate);

		ch_tone = new UICombobox(rm.getString(ResourceIDs.STR_PLAY_TONE_IF),
				true);
		ch_tone.append(rm.getString(ResourceIDs.STR_HIDDEN));
		ch_tone.append(rm.getString(ResourceIDs.STR_SHOW));
		flags = Utils.str2flags(selected_status, 2, 2);
		ch_tone.setSelectedFlags(flags);
		append(ch_tone);
		// XXX: append(new Spacer(100, 1));

		color = new UICombobox(rm.getString(ResourceIDs.STR_COLOR), false);
		color.setAnchorPoint(UIGraphics.RIGHT);
		color.append(rm.getString(ResourceIDs.STR_COLOR_BLUE));
		color.append(rm.getString(ResourceIDs.STR_COLOR_GREEN));
		color.append(rm.getString(ResourceIDs.STR_COLOR_RED));
		color.append(rm.getString(ResourceIDs.STR_COLOR_ALEF));
		append(color);
		String colorString = Config.getInstance()
				.getProperty(Config.COLOR, "0");
		int colorInt = Integer.parseInt(colorString);
		color.setSelectedIndex(colorInt);

		resolution = new UICombobox(rm
				.getString(ResourceIDs.STR_CAMERA_RESOLUTION), false);
		resolution.setAnchorPoint(UIGraphics.RIGHT);
		resolution.append(rm.getString(ResourceIDs.STR_RESOLUTION_DEFAULT));
		resolution.append(rm.getString(ResourceIDs.STR_CAMERA_LOW));
		resolution.append(rm.getString(ResourceIDs.STR_CAMERA_MEDIUM));
		resolution.append(rm.getString(ResourceIDs.STR_CAMERA_HIGH));
		resolution.append(rm.getString(ResourceIDs.STR_CAMERA_HUGE));
		append(resolution);
		String resolutionString = Config.getInstance().getProperty(
				Config.CAMERA_RESOLUTION, "0");
		int resolutionInt = Integer.parseInt(resolutionString);
		resolution.setSelectedIndex(resolutionInt);

		String font_sizeString = rm.getString(ResourceIDs.STR_FONT_SIZE);
		font_size = new UICombobox(font_sizeString, false);
		font_size.setAnchorPoint(UIGraphics.RIGHT);
		font_size.append(rm.getString(ResourceIDs.STR_FONT_SMALL));
		font_size.append(rm.getString(ResourceIDs.STR_FONT_MEDIUM));
		font_size.append(rm.getString(ResourceIDs.STR_FONT_BIG));
		append(font_size);
		font_sizeString = Config.getInstance().getProperty(Config.FONT_SIZE,
				"0");
		font_size.setSelectedIndex(font_sizeString.toCharArray()[0] - '0');

		String font_menu_sizeString = rm
				.getString(ResourceIDs.STR_MENU_FONT_SIZE);
		menu_font_size = new UICombobox(font_menu_sizeString, false);
		menu_font_size.setAnchorPoint(UIGraphics.RIGHT);
		menu_font_size.append(rm.getString(ResourceIDs.STR_FONT_SMALL));
		menu_font_size.append(rm.getString(ResourceIDs.STR_FONT_MEDIUM));
		menu_font_size.append(rm.getString(ResourceIDs.STR_FONT_BIG));
		append(menu_font_size);
		font_menu_sizeString = Config.getInstance().getProperty(
				Config.FONT_MENU_SIZE, "1");
		menu_font_size
				.setSelectedIndex(font_menu_sizeString.toCharArray()[0] - '0');

		// volume
		int volume = Integer
				.parseInt(cfg.getProperty(Config.TONE_VOLUME, "50")) / 10;
		g_volume = new UIGauge(rm.getString(ResourceIDs.STR_VOLUME), true, 10,
				volume);
		append(g_volume);
		// XXX: append(new Spacer(100, 1));

		// keepalive
		long ka = Long.parseLong(cfg.getProperty(Config.KEEP_ALIVE)) / 1000;

		tf_keepalive = new UITextField(rm.getString(ResourceIDs.STR_KEEPALIVE),
				String.valueOf(ka), 5, TextField.NUMERIC);
		append(tf_keepalive);


		short hs = Short.parseShort(cfg.getProperty(Config.HISTORY_SIZE, "30"));
		history_size = new UITextField(rm
				.getString(ResourceIDs.STR_HISTORY_SIZE), String.valueOf(hs),
				5, TextField.NUMERIC);
		append(history_size);

		qwerty = new UICheckbox(rm.getString(ResourceIDs.STR_QWERTY));
		//qwerty.setAnchorPoint(Graphics.LEFT);
		//qwerty.setFlip(true);
		UIFont xFont = UICanvas.getInstance().getCurrentScreen().getGraphics()
				.getFont();
		UIFont lfont = UIFont.getFont(xFont.getFace(), UIFont.STYLE_BOLD, xFont
				.getSize());
		qwerty.setFont(lfont);
		String defaultVal =
		// #ifndef RIM
				Config.FALSE;
		// #endif
		qwerty.setChecked(cfg.getProperty(Config.QWERTY, defaultVal).equals(
				Config.TRUE));
		append(qwerty);
		append(UIUtils.easyButtonsLayout(cmd_save, cmd_reset));
		append(UIUtils.easyCenterLayout(cmd_cancel, 100));

		this.setSelectedItem(ch_vibrate);

		oldColorIndex = this.color.getSelectedIndex();
		oldFontIndex = this.font_size.getSelectedIndex();
		oldMenuFontIndex = this.menu_font_size.getSelectedIndex();
	}

	public void itemAction(UIItem item) {
		// to update the colors of the rosterscreen
		RosterScreen.getInstance().updateScreen();
		if (cmd_save == item) {
			saveOptions(true);
		} else if (cmd_reset == item) {
			Config cfg = Config.getInstance();
			cfg.resetStorage(false);
			// this *CAN* be set now instead of waiting reload
			UICanvas.getInstance().close(this);
			OptionsScreen os = new OptionsScreen();
			UICanvas.getInstance().open(os, true, this.getReturnScreen());
			os.itemAction(os.color);
			os.itemAction(os.font_size);
			os.itemAction(os.menu_font_size);
			os.saveOptions(true);
		} else if (cmd_cancel == item) {
			int tempColorIndex = this.color.getSelectedIndex();
			int tempFontIndex = this.font_size.getSelectedIndex();
			int tempMenuFontIndex = this.menu_font_size.getSelectedIndex();
			if (tempColorIndex != this.oldColorIndex) {
				this.color.setSelectedIndex(oldColorIndex);
				itemAction(color);
				RosterScreen.getInstance().updateScreen();
			}
			if (tempFontIndex != this.oldFontIndex) {
				this.font_size.setSelectedIndex(oldFontIndex);
				itemAction(font_size);
				RosterScreen.getInstance().updateScreen();
			}
			if (tempMenuFontIndex != this.oldMenuFontIndex) {
				this.menu_font_size.setSelectedIndex(oldMenuFontIndex);
				itemAction(menu_font_size);
				RosterScreen.getInstance().updateScreen();
			}
			UICanvas.getInstance().close(this);
		} else if (item == this.color) {
			int colorIndex = this.color.getSelectedIndex();
			// #ifdef UI
			LampiroMidlet.changeColor(colorIndex);
			// #endif
			Config cfg = Config.getInstance();
			cfg.setProperty(Config.COLOR, this.color.getSelectedIndex() + "");
			cfg.saveToStorage();

			//better the pythonic way
			UIItem[] colorItems = new UIItem[] { titleLabel,
					this.color.comboScreen.titleLabel
			// #ifndef RIM
					, footerLeft, footerRight,
					this.color.comboScreen.footerLeft,
					this.color.comboScreen.footerRight
			// #endif
			};
			for (int i = 0; i < colorItems.length; i++) {
				UIItem ithItem = colorItems[i];
				ithItem.setBg_color(UIConfig.header_bg);
				ithItem.setFg_color(UIConfig.menu_title);
			}

			this.setDirty(true);
			this.askRepaint();
		} else if (item == this.font_size) {
			int fontIndex = this.font_size.getSelectedIndex();
			// #ifdef UI
			LampiroMidlet.changeFont(fontIndex, menu_font_size
					.getSelectedIndex());
			// #endif 
			Config cfg = Config.getInstance();
			cfg.setProperty(Config.FONT_SIZE, fontIndex + "");
			cfg.saveToStorage();
			this.setDirty(true);
			this.askRepaint();
		} else if (item == this.menu_font_size) {
			int fontIndex = this.menu_font_size.getSelectedIndex();
			// #ifdef UI
			LampiroMidlet.changeFont(this.font_size.getSelectedIndex(),
					fontIndex);
			// #endif 
			Config cfg = Config.getInstance();
			cfg.setProperty(Config.FONT_MENU_SIZE, fontIndex + "");
			cfg.saveToStorage();
			this.setDirty(true);
			this.askRepaint();
		}
	}

	/**
	 * 
	 */
	public void saveOptions(boolean showAlert) {
		Config cfg = Config.getInstance();
		boolean flags[] = new boolean[4];
		flags[0] = ch_vibrate.isSelected(0);
		flags[1] = ch_vibrate.isSelected(1);
		flags[2] = ch_tone.isSelected(0);
		flags[3] = ch_tone.isSelected(1);
		cfg.setProperty(Config.VIBRATION_AND_TONE_SETTINGS, Utils.flags2str(
				flags, 0));
		cfg.setProperty(Config.TONE_VOLUME, String
				.valueOf(g_volume.getValue() * 10));
		cfg.setProperty(Config.KEEP_ALIVE, String.valueOf(Integer
				.parseInt(tf_keepalive.getText()) * 1000));
		cfg.setProperty(Config.HISTORY_SIZE, String.valueOf(Integer
				.parseInt(history_size.getText())));


		String qwertyValue = Config.FALSE;
		if (qwerty.isChecked()) {
			qwertyValue = Config.TRUE;
			UICanvas.getInstance().setQwerty(true);
		} else {
			qwertyValue = Config.FALSE;
			UICanvas.getInstance().setQwerty(false);
		}
		cfg.setProperty(Config.QWERTY, qwertyValue);
		cfg.setProperty(Config.CAMERA_RESOLUTION, resolution.getSelectedIndex()
				+ "");
		cfg.saveToStorage();

		UICanvas.getInstance().close(this);
		if (showAlert) {
			UICanvas.showAlert(Alerts.WARNING, rm
					.getString(ResourceIDs.STR_WARNING), rm
					.getString(ResourceIDs.STR_SETTINGS_EFFECT));
		}

	}
}
