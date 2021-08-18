// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UIHLayout.java 2325 2010-11-15 20:07:28Z luca $
 */

/**
 * 
 */
package it.yup.ui;

import javax.microedition.lcdui.game.Sprite;

import it.yup.ui.wrappers.UIGraphics;
import it.yup.ui.wrappers.UIImage;

/**
 * @author luca
 * 
 */
public class UIHLayout extends UILayout {

	/*
	 * Force to traverse all the elements horizontally with up/down keys
	 */
	private boolean traverseHorizontally = false;

	/**
	 * @param colNumber
	 *            The number of columns in the layOut.
	 */
	public UIHLayout(int colNumber) {
		super(colNumber);
		this.dirKey1 = UICanvas.LEFT;
		this.dirKey2 = UICanvas.RIGHT;
	}

	/**
	 * Inserts and {@link UIItem} at the index-th position in the layout.
	 * 
	 * @param item
	 *            The {@link UIItem} to add.
	 * @param index
	 *            The index in the column array.
	 * @param type
	 *            The type of column (can be UIHLayout.pix or UIHLayout.perc)
	 */
	public void insert(UIItem item, int index, int width, int type) {
		this.layoutItems[index] = item;
		item.setLayoutWidth(width);
		item.setType(type);
		item.setScreen(screen);
		item.setContainer(this);
		item.setDirty(true);
	}

	protected int getLayoutDimension(int i) {
		return layoutItems[i].getLayoutWidth();
	}

	protected int getMyDimension(UIGraphics g, int w) {
		return w;
	}

	protected void paintLayoutItem(int i, UIGraphics g, int w, int h,
			int forcedDim) {
		layoutItems[i].paint0(g, forcedDim, h);
	}
	
	protected void paintBGRegion(UIGraphics g, int x, int y, int w, int h,
			int forcedDim) {
		if (this.getBgImage() != null) {
			if (h>getBgImage().getHeight()) h=getBgImage().getHeight();
			UIImage img = this.isSelected() ? getBgSelImage() : getBgImage();
			g.drawRegion(img, x, y, forcedDim, h, Sprite.TRANS_NONE, 0, 0,
					UIGraphics.TOP | UIGraphics.LEFT);
		}
	}

	protected void paintLastItem(int i, UIGraphics g, int w, int h,
			int pixelIndex) {
		paintLayoutItem(i, g, w, h, w - pixelIndex - 2 * hPadding);
	}
	
	protected void paintLastBgRegion(UIGraphics g, int x, int y,int w, int h,
			int pixelIndex){
		paintBGRegion(g, x, y, w, h, w - pixelIndex - 2 * hPadding);
	}

	protected void translateG(UIGraphics g, int i, int forcedDim) {
		g.translate(forcedDim, 0);
	}

	public int getHeight(UIGraphics g) {
		this.height = 0;
		// the height in this case is the greatest among
		// all the items
		for (int i = 0; i < layoutItems.length; i++) {
			int tempHeight = layoutItems[i].getHeight(g);
			if (tempHeight > this.height) this.height = tempHeight;
		}
		height += vPadding * 2;
		if (this.getBgImage() != null && this.getBgImage().getHeight() > height) {
			this.height = getBgImage().getHeight();
		}
		return height;
	}

	void chooseSelectedIndex() {
		if (traverseHorizontally == false || this.getContainer() == null) {
			super.chooseSelectedIndex();
			return;
		}
		int lastGameAction = UICanvas.getInstance().getLastGameAction();
		if (lastGameAction == -1) { return; }
		int newSelIndex = lastGameAction == UICanvas.DOWN ? -1 : this
				.getItems().size();
		do {
			newSelIndex = newSelIndex
					+ (lastGameAction == UICanvas.DOWN ? +1 : -1);
		} while (newSelIndex < layoutItems.length && newSelIndex >= 0
				&& !layoutItems[newSelIndex].isFocusable());
		if (selectedIndex >= 0 && selectedIndex < layoutItems.length) {
			layoutItems[selectedIndex].setSelected(false);
		}
		if (newSelIndex >= 0 && newSelIndex < layoutItems.length) {
			layoutItems[newSelIndex].setSelected(true);
			selectedIndex = newSelIndex;
		}
	}

	/**
	 * @param traverseHorizontally
	 *            the traverseHorizontally to set
	 */
	public void setTraverseHorizontally(boolean traverseHorizontally) {
		this.traverseHorizontally = traverseHorizontally;
		this.dirKey1 = UICanvas.UP;
		this.dirKey2 = UICanvas.DOWN;
	}

	/**
	 * @return the traverseHorizontally
	 */
	public boolean isTraverseHorizontally() {
		return traverseHorizontally;
	}

	protected int getMyPadding() {
		return hPadding;
	}
}
