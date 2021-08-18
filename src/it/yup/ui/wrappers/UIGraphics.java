// #condition MIDP
/**
 * 
 */
package it.yup.ui.wrappers;

// #mdebug

import it.yup.util.log.Logger;

// #enddebug

// #ifndef RIM

import javax.microedition.lcdui.Graphics;

// #endif

/**
 * @author luca
 * 
 */
public final class UIGraphics {

	private Graphics g;
	public static final int TOP = Graphics.TOP;
	public static final int LEFT = Graphics.LEFT;
	public static final int RIGHT = Graphics.RIGHT;
	public static final int BOTTOM = Graphics.BOTTOM;
	public static final int HCENTER = Graphics.HCENTER;
	public static final int VCENTER = Graphics.VCENTER;

	public static final int SOLID = javax.microedition.lcdui.Graphics.SOLID;
	public static final int DOTTED = javax.microedition.lcdui.Graphics.DOTTED;


	private UIFont uiFont = null;

	/**
	 * 
	 */
	public UIGraphics(Graphics g) {
		this.g = g;
		this.uiFont = new UIFont(g.getFont());
	}

	public int getClipHeight() {
// #ifndef RIM
				return g.getClipHeight();
		// #endif
	}

	public int getClipWidth() {
// #ifndef RIM
				return g.getClipWidth();
		// #endif
	}

	public int getTranslateX() {
		return g.getTranslateX();
	}

	public int getTranslateY() {
		return g.getTranslateY();
	}

	public int getClipX() {
// #ifndef RIM
				return g.getClipX();
		// #endif
	}

	public int getClipY() {
// #ifndef RIM
				return g.getClipY();
		// #endif
	}

	public void translate(int x, int y) {
		g.translate(x, y);
	}

	public void setFont(UIFont font) {
		this.uiFont = font;
		g.setFont(font.getFont());
	}

	public void setClip(int x, int y, int width, int height) {
// #ifndef RIM
				g.setClip(x, y, width, height);
		// #endif
	}

	public void clipRect(int x, int y, int width, int height) {
// #ifndef RIM
				g.clipRect(x, y, width, height);
		// #endif
	}

	public int getColor() {
		return g.getColor();
	}

	public UIFont getFont() {
		return uiFont;
	}

	public void setColor(int color) {
		g.setColor(color);
	}

	public void fillRect(int x, int y, int width, int height) {
		g.fillRect(x, y, width, height);
	}

	public void drawLine(int x1, int y1, int x2, int y2) {
		g.drawLine(x1, y1, x2, y2);
	}

	public void drawRect(int x, int y, int width, int height) {
// #ifndef RIM
				g.drawRect(x, y, width, height);
		// #endif
	}

	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
// #ifndef RIM
				g.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
		// #endif
	}

	public void drawString(String string, int x, int y, int flags) {
		if (uiFont instanceof UICustomFont == false) {
// #ifndef RIM
						g.drawString(string, x, y, flags);
			// #endif
		} else {
			((UICustomFont) this.uiFont).drawString(this, string, x, y, flags);
		}
	}

	public void drawImage(UIImage img, int x, int y, int anchor) {
// #ifndef RIM
				g.drawImage(img.getImage(), x, y, anchor);
		// #endif
	}

	public static int getVAnchor(int anchor) {
		int mask1 = (-1) ^ UIGraphics.LEFT;
		int mask2 = (-1) ^ UIGraphics.HCENTER;
		int mask3 = (-1) ^ UIGraphics.RIGHT;
		anchor = anchor & mask1 & mask2 & mask3;
		return anchor;
	}

	public static int getHAnchor(int anchor) {
		int mask1 = (-1) ^ UIGraphics.TOP;
		int mask2 = (-1) ^ UIGraphics.VCENTER;
		int mask3 = (-1) ^ UIGraphics.BOTTOM;
		anchor = anchor & mask1 & mask2 & mask3;
		return anchor;
	}

	public void drawPixel(int x, int y) {
// #ifndef RIM
				g.drawLine(x, y, x, y);
		// #endif
	}

	public int getStrokeStyle() {
// #ifndef RIM
				return g.getStrokeStyle();
		// #endif
	}

	public void setStrokeStyle(int strokeStyle) {
// #ifndef RIM
				g.setStrokeStyle(strokeStyle);
		// #endif
	}

	public Graphics getGraphics() {
		return g;
	}

	public void drawSubstring(String str, int offset, int len, int x, int y,
			int anchor) {
		if (this.uiFont instanceof UICustomFont == false) {
// #ifndef RIM
						g.drawSubstring(str, offset, len, x, y, anchor);
			// #endif
		} else {
			this.drawString(str.substring(offset, len), x, y, anchor);
		}
	}

	public void drawRGB(int[] rgbData, int offset, int scanlength, int x,
			int y, int width, int height, boolean processAlpha) {
// #ifndef RIM
				g.drawRGB(rgbData, offset, scanlength, x, y, width, height,
						processAlpha);
		// #endif
	}

	public void setGraphics(Graphics graphics) {
		this.g = graphics;
	}

	public void fillRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		g.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	public void drawRegion(UIImage src, int x_src, int y_src, int width,
			int height, int transform, int x_dest, int y_dest, int anchor) {
		// #ifndef RIM
				try {
					g.drawRegion(src.getImage(), x_src, y_src, width, height,
							transform, x_dest, y_dest, anchor);
				} catch (Exception e) {
		// #mdebug
					Logger.log("In drawing region: " + e.getClass() + " - "
							+ e.getMessage());
					e.printStackTrace();
		// #enddebug
				}
		// #endif

	}
}
