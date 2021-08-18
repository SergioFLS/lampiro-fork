// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UITextField.java 2407 2011-01-20 23:38:44Z luca $
 */

package it.yup.ui;

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.game.Sprite;

import it.yup.ui.wrappers.UIFont;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.ui.wrappers.UIImage;
import it.yup.ui.wrappers.UITextbox;

/**
 * Resembles a {@link TextField} from standard microedition.lcdui package,
 * representing a text box with a label. When the select button is pressed on
 * this item, a TextBox screen is opened and used to let the user enter text.
 */
public class UITextField extends UIItem implements CommandListener {

	public static void setButtonsString(String cancelLabel, String okLabel) {
		cmd_cancel = new Command(cancelLabel, Command.CANCEL, 2);
		cmd_ok = new Command(okLabel, Command.OK, 1);
	}

	static {
		setButtonsString("CANCEL", "OK");
	}

	public static final int FORMAT_LOWER_CASE = 0x001;

	/*
	 * used to set extra data as FORMAT_LOWER_CASE
	 */
	private int format;

	// the buttons below are set like this to fix a perverted behavior
	// of nokia N95 that leaves the "cancel" button alone and mixes
	// all the others button on the same menus!!!
	/** ok command for the TextBox */
	private static Command cmd_ok = null;
	/** ok command for the TextBox */
	private static Command cmd_cancel = null;
	/** the TextBox used */
	private static UITextbox tb;

	private UITextPanel innerPanel;

	private int maxHeight = 0;

	/*
	 * Set if the contact is selected and must propagate keypressed
	 */
	private boolean groupSelected = false;

	/*
	 * Must be set to true when the UITextfield must automatically unexpand when
	 * loosing focus
	 */
	private boolean autoUnexpand = true;

	/*
	 * Must be set to true when the UITextfield can be expanded and unexpanded
	 * by the control itself in response to a keyPress (programmatically it
	 * still can be epanded or unexpanded )
	 */
	private boolean expandable = true;

	/** the label */
	private UILabel label = new UILabel("");

	/** the max size for the text. */
	private int maxSize;

	private boolean wrappable = false;

	/**
	 * the constraints on the text. These constraints should be the same
	 * constants defined in {@link TextField} (such as {@link TextField#ANY},
	 * {@link TextField#PASSWORD}, ...). These are set as given in the
	 * constraints of the TextBox that is opened on the pressure of the select
	 * button on this item
	 */
	private int constraints;
	private int maxLines = 4;
	private int minLines = 1;

	private UIFont font = null;

	private boolean isEditable() {
		return !((constraints & TextField.UNEDITABLE) > 0);
	}

	public UITextField(String label, String text, int maxSize, int constraints,
			int format) {
		this(label, text, maxSize, constraints);
		this.format = format;
	}

	public UITextField(String label, String text, int maxSize, int constraints) {
		this.label.setText(label == null ? "" : label);
		this.label.setWrappable(true, UICanvas.getInstance().getWidth() - 10);
		UIFont xFont = UIConfig.font_body;
		UIFont lfont = UIFont.getFont(xFont.getFace(), UIFont.STYLE_BOLD, xFont
				.getSize());
		this.label.setFont(lfont);
		this.innerPanel = new UITextPanel(this);
		innerPanel.setContainer(this);
		innerPanel.setText(text == null ? "" : text);
		innerPanel.setBg_color(UIConfig.input_color);
		// innerPanel.setFg_color(UIConfig.fg_color);
		// innerPanel.setSelectedColor(UIConfig.input_color);
		this.maxSize = maxSize;
		this.constraints = constraints;
		setFocusable(true);
		innerPanel.setFocusable(true);
		dirty = true;
		// the minimum height is the one of a UILabel
		int tempHeight = UIConfig.font_body.getHeight() + 2 + 7;
		UIScreen currentScreen = UICanvas.getInstance().getCurrentScreen();
		if (label != null && label.length() > 0 && currentScreen != null) {
			UIGraphics g = currentScreen.getGraphics();
			tempHeight += this.label.getHeight(g);
		}

		this.setMaxHeight(tempHeight);
		format = 0;
	}

	/**
	 * Sets the label value
	 * 
	 * @param label
	 *            the new label value
	 */
	public void setLabel(UILabel label) {
		this.label = label;
		label.setContainer(this.getContainer());
		this.setDirty(true);
	}

	/**
	 * @return the current field label
	 */
	public UILabel getLabel() {
		return label;
	}

	/**
	 * Changes the text shown in the field.
	 * 
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {

		// change first of all the text
		if ((this.format & FORMAT_LOWER_CASE) > 0) {
			text = text.toLowerCase();
		}

		if (text.length() > maxSize) {
			text = text.substring(0, maxSize);
		}
		innerPanel.setText(text);
		this.setDirty(true);
	}

	/**
	 * @return the current text
	 */
	public String getText() {
		return innerPanel.getText();
	}

	protected void paint(UIGraphics g, int w, int h) {
		this.width = w;
		UIFont tfont = null;
		UIFont xFont = g.getFont();
		int oldColor = g.getColor();
		if (this.font == null) {
			tfont = UIFont.getFont(xFont.getFace(), UIFont.STYLE_PLAIN, xFont
					.getSize());
		} else {
			tfont = this.font;
		}

		int tempBc_color = getBg_color() >= 0 ? getBg_color()
				: UIConfig.bg_color;
		g.setColor(tempBc_color);
		int offset = (h - this.getHeight(g)) / 2 - 1;
		if (offset < 0) offset = 0;
		int labelHeight = 0;
		if (this.getBgImage() != null) {
			paintBGRegion(g, 0, 0, w, offset);
		} else {
			if (this.getBg_color() != UIItem.TRANSPARENT_COLOR) {
				g.fillRect(0, 0, w, offset);
			}
		}
		labelHeight = label.getHeight(g);

		if (label.isDirty()) {
			if (this.getBgImage() != null) {
				paintBGRegion(g, 0, offset, w, labelHeight + 2);
			} else {
				if (this.getBg_color() != UIItem.TRANSPARENT_COLOR) {
					g.fillRect(0, offset, w, labelHeight + 2);
				}
			}
			if (label.getText().length() > 0) {
				g.translate(2, 1 + offset);
				label.paint0(g, w, label.getHeight(g));
				g.translate(-2, -1 - offset);
			}
			label.setDirty(false);
		}

		if (innerPanel.isDirty()) {
			// to be sure to reset all the items
			innerPanel.setDirty(true);
			g.setFont(tfont);
			// the innerPanel can have a maxHeight too big
			// a magic number due to the space between items
			int innerPanelHeight = this.height - 7 - labelHeight;
			// first draw the outer borders and then the inner one
			int x0 = 1, y0 = 2 + labelHeight + offset, x1 = w - 3 + x0, y1 = 3
					+ innerPanelHeight + y0;
			if (this.getBgImage() != null) {
				paintBGRegion(g, 0, labelHeight + 2, w, h - (labelHeight + 2));
			} else {
				g.fillRect(0, labelHeight + 2, w, h - (labelHeight + 2));
				drawInput(g, x0, y0, x1, y1);
			}
			String innerText = innerPanel.getText();
			String t = innerText;
			if (this.wrappable == false) {
				if ((constraints & TextField.PASSWORD) != 0 && t != null
						&& t.length() > 0) {
					/* text should be obscured */
					t = "*******";
				}
				if (tfont.stringWidth(t) > w - 9) {
					int l = 0;
					while (l < innerText.length()
							&& tfont.substringWidth(innerText, 0, l)
									+ tfont.stringWidth("...") < w - 9) {
						l++;
					}
					l--;
					t = innerPanel.getText().substring(0, l) + "...";
				}
				innerPanel.setText(t);
			}
			g.translate(3, 4 + labelHeight + offset);
			innerPanel.paint0(g, w - 6, innerPanelHeight);
			// I don't want my Panel to be "clicked"
			this.screen.removePaintedItem(innerPanel);
			this.screen.removePaintedItem(label);
			if (this.wrappable == false) {
				innerPanel.setText(innerText);
			}
			g.setFont(xFont);
			g.setColor(oldColor);
		}
	}

	private void paintBGRegion(UIGraphics g, int x, int y, int w, int h) {
		UIImage tmpImg = isSelected() ? getBgSelImage() : getBgImage();
		g.drawRegion(tmpImg, x, y, w, h, Sprite.TRANS_NONE, x, y,
				UIGraphics.TOP | UIGraphics.LEFT);
	}

	public int getHeight(UIGraphics g) {
		if (dirty) {
			// if label is different from "" add its text
			// a magic number due to the space between items
			height = innerPanel.getHeight(g) + 7;
			if (label.getText().length() > 0) {
				int labelHeight = label.getHeight(g);
				height += labelHeight;
			}
			if (height > this.maxHeight && this.maxHeight > 0) this.height = this.maxHeight;
		}
		if (this.getBgImage() != null && this.getBgImage().getHeight() > height) {
			height = getBgImage().getHeight();
		}
		return height;
	}

	public void setSelected(boolean _selected) {
		super.setSelected(_selected);
		innerPanel.setSelected(_selected);
		if (_selected == false) {
			this.groupSelected = false;
		}
	}

	/**
	 * @return the wrappable
	 */
	public boolean isWrappable() {
		return this.wrappable;
	}

	/**
	 * @param wrappable
	 *            the wrappable to set
	 */
	public void setWrappable(boolean wrappable) {
		this.wrappable = wrappable;
		// depending on the real height of the textPanel
		// I recompute the needed height
		if (this.wrappable == true) {
			computeRealHeight();
			this.setDirty(true);
			this.askRepaint();
		}
	}

	private void computeRealHeight() {
		// needed for the borders
		int w = this.width - 14;
		if (w < 0) w = UICanvas.getInstance().getWidth() - 14
				- UIConfig.scrollbarWidth;

		TokenIterator ti = innerPanel.tokenIterator;
		if (ti.isComputed() == false) {
			ti.computeLazyLines(UIConfig.font_body, w);
			// at least minLines will be shown; and at most maxLines
			// hence I need to compute them to paint
			if (maxLines > 0) {
				int tempMaxLines = maxLines;
				// compute all the lines since it must be shown 
				if (UICanvas.getInstance().hasPointerMotionEvents()) tempMaxLines = Integer.MAX_VALUE;
				for (int i = 0; i < ti.getLinesNumber() && i < tempMaxLines; i++) {
					ti.elementAt(i);
				}
			}
		}
		int nLines = ti.getLinesNumber();
		int tempMaxLines = this.maxLines;
		// compute all the lines since it must be shown 
		if (UICanvas.getInstance().hasPointerMotionEvents()) tempMaxLines = Integer.MAX_VALUE;
		if (nLines > tempMaxLines) nLines = tempMaxLines;
		if (nLines < this.minLines) nLines = this.minLines;
		// if nothing is inside at least one row should be present
		if (nLines == 0) nLines = 1;
		int textHeight = nLines * (UIConfig.font_body.getHeight() + 2) + 8;
		if (label.getText() != null && label.getText().length() > 0) {
			UIGraphics g = UICanvas.getInstance().getCurrentScreen()
					.getGraphics();
			textHeight += this.label.getHeight(g);
		}
		this.setMaxHeight(textHeight);
		ti.reset();
	}

	public boolean keyPressed(int key) {
		int ga = UICanvas.getInstance().getGameAction(key);
		if (wrappable && groupSelected) {
			if (ga != UICanvas.FIRE) {
				boolean innerKeyKeep = innerPanel.keyPressed(key);
				if (innerKeyKeep == false && this.autoUnexpand
						&& this.expandable) {
					unExpand();
				}
				return innerKeyKeep;
			} else {
				if (this.expandable) unExpand();
				return true;
			}
		}

		// if a keyNum has been pressed open the textField
		// and print it!!!
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
		if (keyNum == -1 && ga != UICanvas.FIRE) { return false; }
		// the only need for expansion is when:
		// 1) fire is pressed
		// 2) the object is wrappable and hence could need the innerPanel to
		// open
		// 3) this has not yet been selected.
		// 4) this object is editable
		// 5) the scrollbar is visible
		// 6) the UITextfield is expandable ||or is modal
		if (ga == UICanvas.FIRE && this.wrappable
				&& this.groupSelected == false && isEditable() == false
				&& innerPanel.needScrollbar) {
			if (this.expandable) expand();
			return true;
		}

		if (isEditable()) {
			// some mobile phones crash when a label is ""
			handleScreen();
			tb.setCommandListener(this);
		}
		return true;
	}

	public void unExpand() {
		this.groupSelected = false;
		innerPanel.setSelected(true);
		this.setWrappable(true);
		this.setDirty(true);

		if (this.getContainer() != null) {
			Vector items = this.getContainer().getItems();
			this.getContainer().setSelectedItem((UIItem) items.elementAt(0));
			this.askRepaint();
			this.getContainer().setSelectedItem(this);
		}

		this.askRepaint();
	}

	public void expand() {
		this.groupSelected = true;
		innerPanel.setSelected(false);
		if (innerPanel.tokenIterator.isComputed()) {
			int nLines = innerPanel.tokenIterator.getLinesNumber();
			UIGraphics g = this.screen.getGraphics();
			int neededHeight = nLines * (UIConfig.font_body.getHeight() + 2)
					+ 7 + 5 + label.getHeight(g);

			// The height is computed easily if the container has a fixed size
			// otherwise make an approximation
			int maxHeight = 0;
			if (this.getContainer() instanceof UIMenu == false) {
				maxHeight = ((UIItem) this.getContainer()).getHeight(g) - 7
						- label.getHeight(g); /* a little bit of margin */
			} else {
				maxHeight = UICanvas.getInstance().getClipHeight();
				maxHeight -= this.screen.headerLayout.getHeight(g);
				// #ifndef RIM
				maxHeight -= this.screen.footer.getHeight(g);
				// #endif
				// a little bit of margin for the label...
				maxHeight -= 35;
			}
			int tempMaxHeight = maxHeight;
			if (neededHeight > tempMaxHeight) neededHeight = tempMaxHeight;
			this.setMaxHeight(neededHeight);
		}
		this.setDirty(true);
		this.askRepaint();
	}

	public void handleScreen() {
		String tempLabel = null;
		tempLabel = (label.getText().length() == 0) ? "_" : label.getText();
		tb = new UITextbox(tempLabel, innerPanel.getText(), maxSize,
				constraints);
		tb.addCommand(cmd_cancel);
		tb.addCommand(cmd_ok);
		tb.setCommandListener(this);
		UICanvas.display(tb.getTextBox());
	}

	public void commandAction(Command cmd, Displayable disp) {
		try {
			// #ifndef RIM
			synchronized (UICanvas.getLock()) {
				// #endif
				if (cmd == cmd_ok) {
					setText(tb.getString());
					if (this.wrappable) this.setWrappable(true);
					screen.itemAction(this);
					UICanvas.display(null);
				} else if (cmd == cmd_cancel) {
					UICanvas.display(null);
				}
				this.setDirty(true);
				this.askRepaint();
				// #ifndef RIM
			}
			// #endif
		} catch (Exception e) {
			// #mdebug
						e.printStackTrace();
						Logger.log("In text field command action: " + e.getClass());
			// #enddebug
		}
	}

	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
		innerPanel.setMaxHeight(maxHeight - 7);
	}
	
	public void setWidth(int width) {
		if (width != this.width){
			this.innerPanel.tokenIterator.reset();
		}
		this.width = width;
	}

	public void setMaxLines(int maxLines) {
		this.maxLines = maxLines;
	}

	public void setScreen(UIScreen _us) {
		screen = _us;
		innerPanel.setScreen(_us);
		this.label.setScreen(_us);
	}

	public void setContainer(UIIContainer ui) {
		super.setContainer(ui);
		innerPanel.setContainer(ui);
		this.label.setContainer(ui);
	}

	public void setDirty(boolean _dirty) {
		this.dirty = _dirty;
		innerPanel.setDirty(_dirty);
		this.label.setDirty(_dirty);
	}

	public boolean isDirty() {
		return this.dirty || innerPanel.isDirty() || this.label.isDirty();
	}

	public void setAutoUnexpand(boolean autoUnexpand) {
		this.autoUnexpand = autoUnexpand;
	}

	private boolean isAutoUnexpand() {
		return autoUnexpand;
	}

	public void setExpandable(boolean expandable) {
		this.expandable = expandable;
	}

	public boolean isExpandable() {
		return expandable;
	}

	public void setMinLines(int minLines) {
		this.minLines = minLines;
	}

	public int getMinLines() {
		return minLines;
	}

	/**
	 * @return the innerPanel
	 */
	public UITextPanel getInnerPanel() {
		return innerPanel;
	}

	public void setBg_color(int bg_color) {
		super.setBg_color(bg_color);
		label.setBg_color(bg_color);
	}

	public void setBgImages(UIImage bgImg, UIImage bgSelImg) {
		super.setBgImages(bgImg, bgSelImg);
		label.setBg_color(UIItem.TRANSPARENT_COLOR);
		innerPanel.setBg_color(UIItem.TRANSPARENT_COLOR);
	}

	public void setAnchor(int anchor) {
		this.innerPanel.setAnchor(anchor);
	}

	public void setFont(UIFont font) {
		this.font = font;
	}
}
