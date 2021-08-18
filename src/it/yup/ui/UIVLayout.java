// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UIVLayout.java 2325 2010-11-15 20:07:28Z luca $
 */

/**
 * 
 */
package it.yup.ui;

import javax.microedition.lcdui.game.Sprite;

import it.yup.ui.wrappers.UIGraphics;

/**
 * @author luca
 * 
 */
public class UIVLayout extends UILayout {

	/**
	 * @param rowNumber
	 *            The row number of the layOut.
	 * @param height
	 *            The height of the item
	 */
	public UIVLayout(int rowNumber, int height) {
		super(rowNumber);
		this.dirKey1 = UICanvas.UP;
		this.dirKey2 = UICanvas.DOWN;
		this.height = height;
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
	public void insert(UIItem item, int index, int height, int type) {
		this.layoutItems[index] = item;
		item.setLayoutHeight(height);
		item.setType(type);
		item.setScreen(screen);
		item.setContainer(this);
	}

	protected int getLayoutDimension(int i) {
		return layoutItems[i].getLayoutHeight();
	}

	protected int getMyDimension(UIGraphics g, int w) {
		return this.getHeight(g);
	}

	protected void paintLayoutItem(int i, UIGraphics g, int w, int h,
			int forcedDim) {
		layoutItems[i].paint0(g, w, forcedDim);
	}

	protected void paintBGRegion(UIGraphics g, int x, int y, int w, int h,
			int forcedDim) {
		if (this.getBgImage() != null) {
			g.drawRegion(getBgImage(), x, y, w, forcedDim, Sprite.TRANS_NONE,
					0, 0, UIGraphics.TOP | UIGraphics.LEFT);
		}
	}

	protected void paintLastItem(int i, UIGraphics g, int w, int h,
			int pixelIndex) {
		paintLayoutItem(i, g, w, h, this.getHeight(g) - pixelIndex - 2
				* vPadding);
	}

	protected void paintLastBgRegion(UIGraphics g, int x, int y, int w, int h,
			int pixelIndex) {
		paintBGRegion(g, x, y, w, h, this.getHeight(g) - pixelIndex - 2
				* vPadding);
	}

	protected void translateG(UIGraphics g, int i, int forcedDim) {
		g.translate(0, forcedDim);
	}

	public int getHeight(UIGraphics g) {
		if (height == -1) {
			height = g.getClipHeight() + g.getClipY();
		}
		if (this.getBgImage() != null && this.getBgImage().getHeight() > height) {
			this.height = getBgImage().getHeight();
		}
		return this.height;
	}

	public void setHeight(int layoutHeight) {
		this.height = layoutHeight;
	}

	protected int getMyPadding() {
		return vPadding;
	}
}
