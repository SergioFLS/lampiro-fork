// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UITextPanel.java 1858 2009-10-16 22:42:29Z luca $
 */

package it.yup.ui;

import it.yup.ui.wrappers.UIFont;
import it.yup.ui.wrappers.UIGraphics;
import java.util.Enumeration;

/**
 * @author luca
 * 
 */
public class UITextPanel extends UIPanel implements
		TokenIterator.UIISplittableItem {

	String text = "";
	//private Vector textLines = null;

	TokenIterator tokenIterator = new TokenIterator(this);

	private int firstLabel = 0;
	private int lastLabel = 0;

	/* The possible container for this object */
	private UIItem container = null;

	private boolean enableEmoticons = false;

	private UITextField utf;
	private int anchor = UIGraphics.TOP | UIGraphics.LEFT;

	/**
	 * 
	 */
	public UITextPanel(UITextField utf) {
		this.setFocusable(true);
		this.utf = utf;
	}

	public void setText(String text) {
		this.text = text;
		tokenIterator.reset();
		this.getItems().removeAllElements();
	}

	public void setMaxHeight(int mh) {
		if (this.maxHeight != mh) {
			// so to recompute the lines
			tokenIterator.reset();
		}
		super.setMaxHeight(mh);
	}

	public int getHeight(UIGraphics g) {
		/* always all the available space */
		if (maxHeight != -1) return maxHeight;
		if (this.height > 0) return this.height;
		// if i have a clip that is my height
		int clipY = g.getClipY();
		int clippedHeight = g.getClipHeight() + clipY;
		clippedHeight -= 7;
		if (this.utf.getLabel().getText().length() > 0) {
			int labelHeight = utf.getLabel().getHeight(g);
			clippedHeight -= labelHeight;
		}
		this.height = clippedHeight;
		// a magic number due to the space between items
		if (UICanvas.getInstance().hasPointerMotionEvents()) {
			if (tokenIterator.isComputed() == false) {
				int w = this.width - 14;
				if (w < 0) w = UICanvas.getInstance().getWidth() - 14
						- UIConfig.scrollbarWidth;
				tokenIterator.computeLazyLines(UIConfig.font_body, w);
				for (int i = 0; i < tokenIterator.getLinesNumber(); i++) {
					tokenIterator.elementAt(i);
				}
				fillItems(g, Integer.MAX_VALUE);
			}
			int rh = computeRealHeight(g);
			this.height = Math.max(clippedHeight, rh);
		}
		return this.height;
		// otherwise my last known height
	}

	protected void paint(UIGraphics g, int w, int h) {
		int availableWidth = w - 1;
		if (utf.isWrappable()) availableWidth -= UIConfig.scrollbarWidth;
		if (tokenIterator.isComputed() == false) {
			this.removeAllItems();
			// -2 is for the borders"
			tokenIterator.computeLazyLines(g.getFont(), availableWidth - 2);
			fillItems(g, h);
		}
		int paintedHeight = 0;
		Enumeration en = this.getItems().elements();
		while (en.hasMoreElements()) {
			UIItem ithLabel = (UIItem) en.nextElement();
			paintedHeight += ithLabel.getHeight(g);
		}
		this.needScrollbar = false;
		int paintOffset = 0;
		if (UIGraphics.getVAnchor(anchor) == UIGraphics.VCENTER) {
			paintOffset = (h - paintedHeight) / 2;
			h -= 2 * paintOffset;
		} else if (UIGraphics.getVAnchor(anchor) == UIGraphics.BOTTOM) {
			paintOffset = (h - paintedHeight);
			h -= paintOffset;
		}
		g.translate(0, paintOffset);
		super.paint(g, w, h);
		g.translate(0, -paintOffset);
		// i don't want my labels to be "clicked"
		en = this.getItems().elements();
		while (en.hasMoreElements()) {
			this.screen.removePaintedItem((UIItem) en.nextElement());
		}

		// fill the gap
		g.setColor(getBg_color());
		if (needScrollbar) w -= UIConfig.scrollbarWidth;
		if (this.getBg_color() != UIItem.TRANSPARENT_COLOR) g.fillRect(0,
				paintedHeight, w, h - paintedHeight);

	}

	private void fillItems(UIGraphics g, int h) {
		int labelsHeight = 0;
		UILabel ithLabel = null;
		int count = 0;
		do {
			if (this.enableEmoticons) ithLabel = new UIEmoLabel("");
			else
				ithLabel = new UILabel("");
			if (this.getBg_color() != UIItem.TRANSPARENT_COLOR) ithLabel
					.setBg_color(UIConfig.input_color);
			else
				ithLabel.setBg_color(UIItem.TRANSPARENT_COLOR);
			if (this.fg_color >= 0) ithLabel.setFg_color(fg_color);
			else
				ithLabel.setFg_color(UIConfig.fg_color);
			ithLabel.setSelectedColor(UIConfig.input_color);
			ithLabel.setAnchorPoint(anchor);
			ithLabel.setFocusable(true);
			ithLabel.setSelected(this.selected);
			ithLabel.setText(tokenIterator.elementAt(count));
			this.addItem(ithLabel);
			labelsHeight += ithLabel.getHeight(g);
			count++;
		} while (labelsHeight < (h - ithLabel.getHeight(g))
				&& count < tokenIterator.getLinesNumber());
		firstLabel = 0;
		lastLabel = this.getItems().size() - 1;
	}

	protected int computeRealHeight(UIGraphics g) {
		if (this.getItems().size() == 0) return 0;

		// my real height is the the number of textLines
		// per the height of a label
		if (this.utf.isWrappable()) {
			return tokenIterator.getLinesNumber()
					* ((UILabel) this.getItems().elementAt(0)).getHeight(g);
		} else {
			return ((UILabel) this.getItems().elementAt(0)).getHeight(g);
		}
	}

	public boolean keyPressed(int key) {

		int ga = UICanvas.getInstance().getGameAction(key);
		if (ga != UICanvas.DOWN && ga != UICanvas.UP) { return super
				.keyPressed(key); }
		if (tokenIterator.isComputed()) {
			switch (ga) {
				case UICanvas.DOWN:
					if (lastLabel < this.tokenIterator.getLinesNumber() - 1) {
						this.firstLabel++;
						this.lastLabel++;
						UILabel ithLabel = (UILabel) this.getItems().elementAt(
								0);
						this.getItems().removeElementAt(0);
						ithLabel.setText(tokenIterator.elementAt(lastLabel));
						this.getItems().addElement(ithLabel);
						this.setDirty(true);
						this.askRepaint();
						return true;
					} else if (lastLabel == this.tokenIterator.getLinesNumber() - 1
							&& (needScrollbar == false)) { return false; }
					return true;

				case UICanvas.UP: {
					if (firstLabel > 0) {
						this.firstLabel--;
						this.lastLabel--;
						UILabel ithLabel = (UILabel) this.getItems().elementAt(
								this.getItems().size() - 1);
						this.getItems().removeElementAt(
								this.getItems().size() - 1);
						ithLabel.setText(tokenIterator.elementAt(firstLabel));
						this.getItems().insertElementAt(ithLabel, 0);
						this.setDirty(true);
						this.askRepaint();
						return true;
					} else if (firstLabel == 0 && (needScrollbar == false)) { return false; }
					return true;
				}
			}
		}
		return false;
	}

	protected void drawScrollBar(UIGraphics g, int w, int h, int rh) {
		this.needScrollbar = true;
		drawScrollBarItems(g, w, h, rh, firstLabel, lastLabel,
				tokenIterator.getLinesNumber());
	}

	public UIItem getSelectedItem() {
		if (this.container != null) return this.container;
		return this;
	}

	public void setContainer(UIItem container) {
		this.container = container;
	}

	public String getText() {
		return this.text;
	}

	public void setSelected(boolean _selected) {
		this.selected = _selected;
		this.dirty = true;
		Enumeration en = this.getItems().elements();
		while (en.hasMoreElements()) {
			((UIItem) en.nextElement()).setSelected(_selected);
		}
	}

	public void setScreen(UIScreen _us) {
		screen = _us;
		Enumeration en = this.getItems().elements();
		while (en.hasMoreElements()) {
			((UIItem) en.nextElement()).setScreen(_us);
		}
	}

	public boolean isNeedScrollbar() {
		return needScrollbar;
	}

	public boolean isEnableEmoticons() {
		return enableEmoticons;
	}

	public void setEnableEmoticons(boolean enableEmoticons) {
		this.enableEmoticons = enableEmoticons;
	}

	public int getTextWidth(String textLine, UIFont font, int startIndex,
			int endIndex) {
		return font.substringWidth(text, startIndex, endIndex);
	}

	public void setAnchor(int anchor) {
		this.anchor = anchor;
	}
}
