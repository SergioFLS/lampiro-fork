// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UIJidCombo.java 1858 2009-10-16 22:42:29Z luca $
*/
package it.yup.ui;

import it.yup.util.Utils;

import javax.microedition.lcdui.TextField;

public class UIJidCombo extends UICombobox {

	public class UIJidComboScreen extends UIComboScreen {

		private UITextField jidField = new UITextField(newContactString, "@",
				255, TextField.EMAILADDR);

		public UIJidComboScreen(String title) {
			super(title);
			this.mainPanel.setModal(false);
			this.insert(0, jidField);
		}

		public boolean keyPressed(int key) {
			if (this.popupList.size() > 0
					|| (this.getMenu() != null && this.getMenu()
							.isOpenedState())) { return super.keyPressed(key); }

			int ga = UICanvas.getInstance().getGameAction(key);
			if (ga == UICanvas.FIRE) {
				UIItem selItem = this.getSelectedItem();
				if (selItem == this.jidField) { return jidField.keyPressed(key); }
			}
			if (key == UICanvas.MENU_RIGHT
					&& Utils.is_jid(this.jidField.getText())) {
				UIJidCombo.this.insertAt(this.jidField.getText(), 0);
				UIJidCombo.this.setSelectedIndex(1);
				((UICheckbox) this.mainPanel.getItems().elementAt(0))
						.setChecked(true);
				this.jidField.setText("@");
			}
			return super.keyPressed(key);
		}

		public void itemAction(UIItem item) {
			if (item == this.jidField) {
				if (UIJidCombo.this.multiChoice == false
						&& Utils.is_jid(this.jidField.getText())) {
					UIJidCombo.this.insertAt(this.jidField.getText(), 0);
					UIJidCombo.this.setSelectedIndex(0);
					UICanvas.getInstance().close(this);
					this.jidField.setText("@");
				}
			}
		}
	}

	private String newContactString;

	public UIJidCombo(String title, boolean multichoice, String newContactString) {
		super(title, multichoice);
		this.newContactString = newContactString;
		this.comboScreen = new UIJidComboScreen(title);
	}
}
