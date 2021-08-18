// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UISeparator.java 2325 2010-11-15 20:07:28Z luca $
*/

/**
 * 
 */
package it.yup.ui;

import it.yup.ui.wrappers.UIGraphics;

/**
 * @author luca
 * 
 */
public class UISeparator extends UIItem {

	/**
	 * 
	 */
	public UISeparator(int height) {
		this.height = height;
	}

	public UISeparator(int height, int color) {
		this.height = height;
		this.setFg_color(color);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.yup.ui.UIItem#paint(javax.microedition.lcdui.Graphics, int, int)
	 */
	protected void paint(UIGraphics g, int w, int h) {
		if (this.getFg_color() == UIItem.TRANSPARENT_COLOR) return;
		g.setColor(getFg_color() >= 0 ? getFg_color() : UIConfig.fg_color);
		// the separator always uses its imposed height!!! and not the one asked from paint(...) 
		g.fillRect(0, 0, w, this.height);
	}

	public void setHeight(int height) {
		this.height = height;
	}
}
