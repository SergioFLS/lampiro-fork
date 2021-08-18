// #condition MIDP
package it.yup.ui;

import java.util.Vector;

public abstract class UIDialog {

	public abstract void selectionMade(String selString);

	private class UIDialogMenu extends UIMenu {

		public UIDialogMenu(String name) {
			super(name);
		}

		protected UIItem keyPressed(int key, int ga) {
			UIItem um = super.keyPressed(key, ga);
			if (um != null) {
				if (key == UICanvas.MENU_RIGHT) {
					selectionMade(choices[0]);
				} else if (key == UICanvas.MENU_LEFT) {
					selectionMade(choices[1]);
				} else {
					um = null;
				}
			}
			return um;
		}
	}

	private class UIDialogScreen extends UIScreen {

		private Vector buttons = new Vector(1);

		private UIPanel diagPanel = new UIPanel(true, false);

		public UIDialogScreen() {
			super();
			this.append(diagPanel);
			setRollEnabled(false);
		}

		public void itemAction(UIItem item) {
			if (buttons.contains(item)) {
				UICanvas.getInstance().close(this);
				selectionMade(((UIButton) item).getText());
			}
		}
	}

	private boolean popUp =
// #ifndef RIM
	true;
	// #endif

	private UIMenu uiItem = null;
	private UIScreen parentScreen = null;
	private String title;
	private String text;
	private String[] choices;
	private UIItem[] additionalItems;

	public UIDialog(UIScreen parentScreen, String title, String text,
			String[] choices) {

		this.parentScreen = parentScreen;
		this.title = title;
		this.text = text;
		this.choices = choices;
	}

	public UIDialog(UIScreen parentScreen, String title, String text,
			String[] choices, UIItem[] additionalItems) {
		this(parentScreen, title, text, choices);
		this.additionalItems = additionalItems;
	}

	public void show() {
		if (popUp) {
			uiItem = new UIDialogMenu(title);
			uiItem.setAbsoluteX(20);
			uiItem.setAbsoluteY(UICanvas.getInstance().getHeight() / 2);
			uiItem.setWidth(UICanvas.getInstance().getWidth() - 20);
			if (choices != null) {
				if (choices.length > 0)
					uiItem.selectMenuString = choices[0];
				if (choices.length > 1)
					uiItem.cancelMenuString = choices[1];
			}
			UILabel firstItem = new UILabel(text);
			firstItem.setWrappable(true, uiItem.getWidth() - 5);
			uiItem.append(firstItem);
			for (int i = 0; additionalItems != null
					&& i < additionalItems.length; i++) {
				UIItem item = additionalItems[i];
				uiItem.append(item);
			}
			uiItem.setSelectedItem(firstItem);
			if (parentScreen != null) {
				parentScreen.addPopup(uiItem);
			}
		} else {
			UIDialogScreen tempItem = new UIDialogScreen();
			tempItem.setTitle(title);
			if (choices != null) {
				int tempWidth = UICanvas.getInstance().getWidth() / 2 - 10;
				UIButton selButton = new UIButton(choices[0]);
				selButton.setWrappable(true, tempWidth);
				tempItem.buttons.addElement(selButton);

				UIButton cancelButton = new UIButton(choices[1]);
				cancelButton.setWrappable(true, tempWidth);
				tempItem.buttons.addElement(cancelButton);

				UIHLayout buttonLayout = UIUtils.easyButtonsLayout(selButton,
						cancelButton);

				UILabel firstItem = new UILabel(text);
				firstItem.setWrappable(true,
						UICanvas.getInstance().getWidth() - 20);
				tempItem.diagPanel.addItem(firstItem);
				for (int i = 0; additionalItems != null
						&& i < additionalItems.length; i++) {
					UIItem item = additionalItems[i];
					tempItem.diagPanel.addItem(item);
				}
				tempItem.diagPanel.addItem(buttonLayout);
				// to be sure all the additioalItems has the correctScreen
				tempItem.diagPanel.setScreen(tempItem);
				buttonLayout.setSelectedItem(selButton);
			}
			uiItem = tempItem;
			UICanvas.getInstance().open(tempItem, true, this.parentScreen);
		}
	}

	public void setPopUp(boolean popUp) {
		this.popUp = popUp;
	}

	public boolean isPopUp() {
		return popUp;
	}

	public void setParentScreen(UIScreen parentScreen) {
		this.parentScreen = parentScreen;
	}

	public UIScreen getParentScreen() {
		return parentScreen;
	}

	/**
	 * @return the innerItem
	 */
	public UIIContainer getUIItem() {
		if (popUp)
			return uiItem;
		else
			return ((UIDialogScreen) uiItem).diagPanel;
	}

}
