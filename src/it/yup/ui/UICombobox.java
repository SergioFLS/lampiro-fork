// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UICombobox.java 2386 2011-01-17 11:55:37Z luca $
 */

/**
 * 
 */
package it.yup.ui;

import it.yup.ui.wrappers.UIFont;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.util.Utils;

import java.util.Enumeration;
import java.util.Vector;

/**
 * @author luca
 * 
 */
public class UICombobox extends UILabel {

	public static String cancelString = "CANCEL";

	public static String selectString = "SELECT";

	/**
	 * @author luca
	 * 
	 */
	public class UIComboScreen extends UIScreen {

		/*
		 * the time stamp of the last key press
		 */
		private long sel_last_ts = 0;

		/*
		 * the key used when moving selection
		 */
		private int sel_last_key = -1;

		/*
		 * The offset of the slected key in the research pattern
		 */
		private int sel_key_offset = 0;

		/*
		 * The offset of the selected key in the research pattern
		 */
		private String sel_pattern = "";

		UIPanel mainPanel = new UIPanel(true, true);

		private UILabel selectLabel = new UILabel(UICombobox.selectString);

		private UILabel cancelLabel = new UILabel(UICombobox.cancelString);

		/**
		 * 
		 */
		public UIComboScreen(String title) {
			super();
			this.setTitle(title);
			this.append(mainPanel);
			this.mainPanel.setMaxHeight(-1);
			this.setSelectedItem(mainPanel);
// #ifndef RIM_5.0
			addMenu();
			// #endif
		}

		private void addMenu() {
			UIMenu menu = new UIMenu("");
			this.setMenu(menu);
			menu.append(selectLabel);
			menu.append(cancelLabel);
		}

		public void menuAction(UIMenu menu, UIItem c) {
			if (c == cancelLabel) {
				this.sel_pattern = "";
				if (this.originalItems != null) {
					this.updateFilter();
				}
				UICanvas.getInstance().close(this);
			}
		}

		public boolean keyPressed(int key) {
			if (this.popupList.size() > 0
					|| (this.getMenu() != null && this.getMenu()
							.isOpenedState())) { return super.keyPressed(key); }

			int ga = UICanvas.getInstance().getGameAction(key);

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
					int key_num = key - UICanvas.KEY_NUM0;
					long t = System.currentTimeMillis();
					if ((key_num != sel_last_key) || t - sel_last_ts > 1000) {
						// new key
						sel_last_key = key_num;
						sel_key_offset = 0;
						if (sel_pattern.length() == 0) {
							this.originalItems = new UILabel[this.mainPanel
									.getItems().size()];
							this.mainPanel.getItems().copyInto(
									this.originalItems);
						}
						sel_pattern = sel_pattern
								+ Utils.itu_keys[key_num][sel_key_offset];
					} else {
						// shifted key
						sel_key_offset += 1;
						if (sel_key_offset >= Utils.itu_keys[key_num].length) sel_key_offset = 0;
						sel_pattern = sel_pattern.substring(0, sel_pattern
								.length() - 1)
								+ Utils.itu_keys[key_num][sel_key_offset];
					}
					sel_last_ts = t;
					updateFilter();
					return true;
			}

			// any of the "delete" character 
			if (key == UICanvas.MENU_CANCEL || ga == UICanvas.LEFT || key == 8) {
				if (sel_pattern.length() > 0) {
					sel_pattern = sel_pattern.substring(0,
							sel_pattern.length() - 1);
					this.updateFilter();
					return true;
				}
			}

			if (UICanvas.getInstance().hasQwerty()) {
				if ((key >= 'A' && key <= 'Z') || (key >= 'a' && key <= 'z')
						|| (key >= '0' && key <= '9')) {
					if (sel_pattern.length() == 0) {
						this.originalItems = new UILabel[this.getItems().size()];
						this.getItems().copyInto(this.originalItems);
					}
					updateFilter();
					return true;
				}
			}

			if (key == UICanvas.MENU_LEFT) {
				menuAction(null, cancelLabel);
				return true;
			} else if ((key == UICanvas.MENU_RIGHT || ga == UICanvas.FIRE)
					&& (this.selectedIndex >= 0 || UICombobox.this.multiChoice)) {
				UIItem selItem = this.mainPanel.getSelectedItem();
				if (selItem != null) {
					UICombobox.this.selectedIndex = this.mainPanel.getItems()
							.indexOf(selItem);
				}
				if (UICombobox.this.multiChoice == false) {
					UICombobox.this.screen.itemAction(UICombobox.this);
					// when "changing" the filter pattern search the index of items
					// MAY change
					this.sel_pattern = "";
					if (this.originalItems != null) {
						this.updateFilter();
						this.mainPanel.setSelectedItem(selItem);
					}
					UICombobox.this.selectedIndex = this.mainPanel.getItems()
							.indexOf(selItem);
					UICanvas.getInstance().close(this);
					//UICanvas.getInstance().open(UICombobox.this.screen, true);
					this.screen.itemAction(UICombobox.this);
				} else {
					if (ga == UICanvas.FIRE) {
						// UICheckbox selectedItem = (UICheckbox) this.itemList
						// .elementAt(this.selectedIndex);
						// (selectedItem).setChecked(!selectedItem.isChecked());
						super.keyPressed(key);
					} else {
						if (UICombobox.this.multiChoice == false) {
							Enumeration en = UIComboScreen.this.getItems()
									.elements();
							while (en.hasMoreElements()) {
								UILabel uil = (UILabel) en.nextElement();
								uil.setWrappable(false, width);
							}
						}
						if (this.originalItems != null) {
							sel_pattern = "";
							this.updateFilter();
							this.setSelectedItem(selItem);
						}
						UICanvas.getInstance().close(this);
						//UICanvas.getInstance().open(UICombobox.this.screen,
						//		true);
						UICombobox.this.screen.itemAction(UICombobox.this);
					}
					UICombobox.this.setDirty(true);
					UIComboScreen.this.askRepaint();
				}
				return true;
			} else {
				boolean retVal = super.keyPressed(key);
				//				if (UICombobox.this.multiChoice == false) {
				for (int i = 0; i < this.mainPanel.getItems().size(); i++) {
					UILabel uil = (UILabel) this.mainPanel.getItems()
							.elementAt(i);
					uil.setWrappable(false, width);
				}
				Object selItem = this.mainPanel.getSelectedItem();
				if (selItem instanceof UILabel) ((UILabel) selItem)
						.setWrappable(true,
								UICanvas.getInstance().getWidth() - 20);
				//				}
				this.askRepaint();
				return retVal;
			}
		}

		private UILabel[] originalItems;

		private void updateFilter() {
			mainPanel.setSelectedIndex(-1);
			this.firstVisibleIndex = 0;
			this.lastVisibleIndex = 0;

			if (sel_pattern.length() > 0) {
				this.setTitle(UICombobox.this.title.getText() + ": "
						+ sel_pattern);
				UILabel ithLabel = null;
				this.mainPanel.removeAllItems();
				for (int i = 0; i < originalItems.length; i++) {
					ithLabel = originalItems[i];
					if (ithLabel.getText().toUpperCase().indexOf(
							this.sel_pattern.toUpperCase()) == 0) {
						this.mainPanel.addItem(ithLabel);
					}
				}
			} else {
				this.setTitle(UICombobox.this.title.getText());
				this.mainPanel.removeAllItems();
				for (int i = 0; i < this.originalItems.length; i++) {
					this.mainPanel.addItem(this.originalItems[i]);
				}
				this.originalItems = null;
			}
			this.setDirty(true);
			this.askRepaint();
		}
	}

	/** the selected index in the item Vector */
	private int selectedIndex = 0;

	/** the pop-up menu shown when FIRE is pressed on the combo-box */
	public UIComboScreen comboScreen = null;

	boolean multiChoice = false;
	protected UILabel title = new UILabel("");

	/** Constructor */
	public UICombobox(String title, boolean multichoice) {
		super(UICanvas.getUIImage("/icons/combo.png"), "");
		this.focusable = true;
		this.flip = true;
		this.multiChoice = multichoice;
		this.title.setText(title);
		this.wrappable = false;
		this.comboScreen = new UIComboScreen(title);
		this.anchorPoint = UIGraphics.RIGHT | UIGraphics.VCENTER;
		UIFont f = UIConfig.font_body;
		UIFont titleFont = UIFont.getFont(f.getFace(), UIFont.STYLE_BOLD, f
				.getSize());
		this.title.setFont(titleFont);
		this.title.setFocusable(true);
		this.setBg_color(UIConfig.input_color);
		this.setFg_color(UIConfig.fg_color);
		this.setSelectedColor(UIConfig.input_color);
		//this.title.setBg_color(UIConfig.input_color);
		this.title.setFg_color(UIConfig.fg_color);
		this.title.setSelectedColor(UIConfig.bg_color);
		this.title.setWrappable(true, UICanvas.getInstance().getWidth() - 20);
	}

	public void setSelected(boolean _selected) {
		this.title.setSelected(_selected);
		super.setSelected(_selected);
	}

	public int getHeight(UIGraphics g) {
		// 6 is the number of pixels for the border
		this.height = super.getHeight(g) + this.title.getHeight(g) + 6;
		return this.height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.yup.ui.UIItem#paint(javax.microedition.lcdui.Graphics, int, int)
	 */
	protected void paint(UIGraphics g, int w, int h) {
		Vector comboItems = this.comboScreen.mainPanel.getItems();
		if (this.multiChoice == false) {
			if (comboItems.size() > 0 && this.selectedIndex >= 0) this.text = ((UILabel) comboItems
					.elementAt(this.selectedIndex)).getText();
		} else {
			this.text = "";
			if (comboItems.size() > 0) {
				Enumeration en = comboItems.elements();
				while (en.hasMoreElements()) {
					Object ithItem = en.nextElement();
					if (ithItem instanceof UICheckbox) {
						UICheckbox uic = (UICheckbox) ithItem;
						if (uic.isChecked()) this.text += (uic.getText() + " ");
					}
				}

			}
		}
		this.height = h;

		// the computation above is used to compute the correct offset in
		// drawing

		int titleHeight = title.getHeight(g);
		this.title.paint(g, w, titleHeight);
		g.translate(0, titleHeight);
		int superHeight = super.getHeight(g);
		if (this.bg_color != UIItem.TRANSPARENT_COLOR) {
			g.setColor(UIConfig.bg_color);
			g.fillRect(0, 0, w, superHeight + 6);
		}
		//drawBorder(g, 1, 1, w-2, superHeight+4);
		drawInput(g, 1, 1, w - 2, superHeight + 4);
		g.translate(3, 3);
		super.paint(g, w - 6, superHeight);
	}

	public void setBg_color(int bg_color) {
		this.bg_color = bg_color;
		this.title.setBg_color(bg_color);
	}

	public UILabel append(String comboItem) {

		UILabel uimi = null;
		if (this.multiChoice == false) {
			uimi = new UILabel(comboItem);
			((UILabel) uimi).setWrappable(true, UICanvas.getInstance()
					.getWidth() - 20);
		} else {
			uimi = new UICheckbox(comboItem);
		}
		comboScreen.mainPanel.addItem(uimi);
		uimi.setFocusable(true);
		return uimi;
	}

	public UILabel insertAt(String comboItem, int position) {
		UILabel uimi = null;
		if (this.multiChoice == false) {
			uimi = new UILabel(comboItem);
			((UILabel) uimi).setWrappable(true, UICanvas.getInstance()
					.getWidth() - 20);
		} else {
			uimi = new UICheckbox(comboItem);
		}
		comboScreen.mainPanel.insertItemAt(uimi, position);
		uimi.setFocusable(true);
		return uimi;
	}

	public void append(UILabel comboItem) {
		comboScreen.mainPanel.addItem(comboItem);
		comboItem.setWrappable(false, -1);
		comboItem.setFocusable(true);
	}

	public void removeAt(int index) {
		this.comboScreen.mainPanel.removeItemAt(index);
	}

	public void removeAll() {
		this.comboScreen.mainPanel.removeAllItems();
	}

	public boolean keyPressed(int key) {
		int ga = UICanvas.getInstance().getGameAction(key);
		if (ga == UICanvas.FIRE) {
			openMenu();
			return false;
		}
		return super.keyPressed(key);
	}

	/**
	 * @return
	 */
	public void openMenu() {
		if (comboScreen.mainPanel.getSelectedIndex() < 0
				&& comboScreen.mainPanel.getItems().size() > 0) {
			this.setSelectedIndex(0);
		}
		boolean oldRolled = false;
		if (this.getScreen() != null) {
			oldRolled = this.getScreen().isRollEnabled();
			this.getScreen().setRollEnabled(true);
		}
		UICanvas.getInstance().open(comboScreen, true, this.getScreen());
		if (this.getScreen() != null) this.getScreen()
				.setRollEnabled(oldRolled);
	}

	/**
	 * @return The selected index in the item list.
	 */
	public int getSelectedIndex() {
		return selectedIndex;
	}

	public int[] getSelectedIndeces() {
		Vector tempNumber = new Vector(5);
		int selNumber = 0;
		for (Enumeration en = comboScreen.mainPanel.getItems().elements(); en
				.hasMoreElements();) {
			Object o = en.nextElement();
			if (o instanceof UICheckbox && ((UICheckbox) o).isChecked()) tempNumber
					.addElement(new Integer(selNumber));
			selNumber++;
		}
		int[] resInt = new int[tempNumber.size()];
		for (int i = 0; i < resInt.length; i++) {
			resInt[i] = ((Integer) tempNumber.elementAt(i)).intValue();
		}

		return resInt;
	}

	/**
	 * @return The array of selection flags.
	 */
	public boolean[] getSelectedFlags() {
		boolean[] flags = new boolean[comboScreen.mainPanel.getItems().size()];
		if (this.multiChoice) {
			for (int i = 0; i < flags.length; i++) {
				UICheckbox uic = (UICheckbox) comboScreen.mainPanel.getItems()
						.elementAt(i);
				flags[i] = uic.isChecked();
			}
		} else if (this.selectedIndex >= 0
				&& comboScreen.mainPanel.getItems().size() > 0) flags[this.selectedIndex] = true;
		return flags;
	}

	/**
	 * Return the selected UIItem within the UIItem itself; usually it is the
	 * UIItem itself but in the subclasses (like UIVLayout) it could one of the
	 * contained object.
	 * 
	 * @return
	 */
	public UIItem getSelectedItem() {
		// UICombobox SHOULD not raise any itemAction when it is "clicked"
		// and it is closed
		return null;
	}

	public String[] getSelectedStrings() {
		int[] selIndeces = this.getSelectedIndeces();
		String[] resString = new String[selIndeces.length];
		for (int i = 0; i < selIndeces.length; i++) {
			Object ithSel = this.comboScreen.mainPanel.getItems().elementAt(
					selIndeces[i]);
			resString[i] = ((UILabel) ithSel).getText();
		}
		return resString;
	}

	public String getSelectedString() {
		int selIndex = this.getSelectedIndex();
		String selString = ((UILabel) this.comboScreen.mainPanel.getItems()
				.elementAt(selIndex)).getText();
		return selString;
	}

	/**
	 * Sets the selected index in the list
	 * 
	 * @param si
	 *            the new selected index
	 */
	public void setSelectedIndex(int si) {
		selectedIndex = si;
		UIItem selItem = comboScreen.mainPanel.getSelectedItem();
		if (selItem instanceof UILabel) ((UILabel) selItem).setWrappable(false,
				-1);
		comboScreen.mainPanel.setSelectedIndex(si);
		selItem = comboScreen.mainPanel.getSelectedItem();
		if (selItem instanceof UILabel) ((UILabel) selItem).setWrappable(true,
				UICanvas.getInstance().getWidth() - 20);
		dirty = true;
	}

	/**
	 * Used only for a multichoice combo box: sets selected/deselected flags on
	 * all items
	 * 
	 * @param flags
	 *            the selected flags
	 */
	public void setSelectedFlags(boolean[] flags) {
		if (!isMultiChoice() || flags == null
				|| flags.length != comboScreen.mainPanel.getItems().size()) { return; }
		for (int i = 0; i < flags.length; i++) {
			UICheckbox uic = (UICheckbox) comboScreen.mainPanel.getItems()
					.elementAt(i);
			uic.setChecked(flags[i]);
		}
		dirty = true;
	}

	public void setDirty(boolean dirty) {
		super.setDirty(dirty);
		//if (comboScreen != null) this.comboScreen.setDirty(dirty);
	}

	/**
	 * @return the multiChoice
	 */
	public boolean isMultiChoice() {
		return multiChoice;
	}

	/**
	 * Returns true if the item at the selected index is selected.
	 * 
	 * @param idx
	 *            The item to check for selection
	 * @return {@code true} it the item is selected. {@code false} if the item
	 *         is not selected or idx is not a valid index.
	 */
	public boolean isSelected(int idx) {
		if (idx < 0 || idx > comboScreen.mainPanel.getItems().size()) { return false; }
		UICheckbox uic = (UICheckbox) comboScreen.mainPanel.getItems()
				.elementAt(idx);
		return uic.isChecked();
	}

	public UILabel getTitle() {
		return title;
	}
}
