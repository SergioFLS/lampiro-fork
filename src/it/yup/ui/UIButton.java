// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UIButton.java 2325 2010-11-15 20:07:28Z luca $
*/

/**
 * 
 */
package it.yup.ui;

import it.yup.ui.wrappers.UIFont;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.ui.wrappers.UIImage;

/**
 * @author luca
 * 
 */
public class UIButton extends UILabel {

	private boolean pressed = false;

	/*
	 * the horizontal padding from button borders to text 
	 */
	private int buttonHPadding = 2;

	private int buttonColor = -1;

	private int borderColor = -1;

	private int selectedBorderColor = -1;

	/**
	 * @param text
	 * @param screen
	 */
	public UIButton(String text) {
		super(text);
		this.focusable = true;
		this.wrappable = false;
		this.anchorPoint = UIGraphics.HCENTER | UIGraphics.TOP;
		//this.setSelectedColor(UIConfig.button_selected_color);
	}

	public UIButton(UIImage img, String text) {
		super(img, text);
		this.focusable = true;
		this.wrappable = false;
		this.anchorPoint = UIGraphics.HCENTER | UIGraphics.TOP;
		//this.setSelectedColor(UIConfig.button_selected_color);
	}

	public boolean keyPressed(int key) {
		if (UICanvas.getInstance().getGameAction(key) == UICanvas.FIRE) {
			int oldBgColor = this.getSelectedColor();
			this.setSelectedColor(0xAAAAAA);
			this.setDirty(true);
			this.askRepaint();
			this.setSelectedColor(oldBgColor);
			this.setDirty(true);
			this.askRepaint();
		}
		return false;
	}

	public int getSelectedColor() {
		return selectedColor;
	}

	protected void paint(UIGraphics g, int w, int h) {
		// what should happen in case the text of the button
		// is too long to fit ?

		UIFont usedFont = (this.font != null ? this.font : g.getFont());
		this.height = this.getHeight(g);
		this.width = usedFont.stringWidth(text) + 8 + 2 * buttonHPadding;

		if (this.wrappable == true && textLines == null) {
			int availableWidth = w - 8 - 2 * buttonHPadding;
			computeTextLines(usedFont, availableWidth);
			// if h is lower then 0 it means
			// use the minimum height
			if (h < 0) paint(g, availableWidth, this.getHeight(g));
			else
				paint(g, availableWidth, h);
			return;
		}

		if (this.getBg_color() != UIItem.TRANSPARENT_COLOR) {
			g.setColor(getBg_color() >= 0 ? getBg_color() : UIConfig.bg_color);
			g.fillRect(0, 0, w, h);
		}

		int originalX = g.getTranslateX();
		int originalY = g.getTranslateY();

		// change colors for the UILabel
		int oldBgColor = this.getBg_color();
		int oldSelectedColor = this.getSelectedColor();
		if (oldBgColor != UIItem.TRANSPARENT_COLOR) {
			this.setBg_color(buttonColor != UIItem.DEFAULT_COLOR ? buttonColor
					: UIConfig.button_color);
		}
		if (oldSelectedColor != UIItem.TRANSPARENT_COLOR) {
			this
					.setSelectedColor(oldSelectedColor != UIItem.DEFAULT_COLOR ? oldSelectedColor
							: UIConfig.button_selected_color);
		}
		g.translate(3, 3);
		g.setColor(selected ? this.getSelectedColor() : this.getBg_color());
		// if i have a bgimg i don't fill the bg
		if (this.getBgImage() == null) {
			g.fillRect(0, 0, w - 5, h - 5);
		} else {
			int tempTransX = g.getTranslateX();
			int tempTransY = g.getTranslateY();
			g.translate(originalX - tempTransX, originalY - tempTransY);
			int hAnchor = UIGraphics.getHAnchor(anchorPoint);
			int hPoint = 0;
			if (hAnchor == UIGraphics.HCENTER) hPoint = w / 2;
			else if (hAnchor == UIGraphics.RIGHT) hPoint = w;
			g.drawImage(selected ? getBgSelImage() : getBgImage(), hPoint,
					h / 2, hAnchor | UIGraphics.VCENTER);
			g.translate(tempTransX - originalX, tempTransY - originalY);
		}

		g.translate(1 + buttonHPadding, 1);
		super.paint(g, w - 8 - 2 * buttonHPadding, h - 8);

		this.setBg_color(oldBgColor);
		this.setSelectedColor(oldSelectedColor);
		this.height = this.getHeight(g);
		g.translate(originalX - g.getTranslateX(), originalY
				- g.getTranslateY());

		if (this.getBgImage() == null) {
			int x0 = 1, x1 = w - 2, y0 = 1, y1 = h - 2;

			int oldColor = g.getColor();

			int currentbbColor = 0;
			int innerUp = 0;
			int innerDown = 0;

			if (borderColor == -1) {
				currentbbColor = selected ? UIConfig.bbs_color
						: UIConfig.bb_color;
				innerUp = selected ? UIConfig.blbs_color : UIConfig.blb_color;
				innerDown = selected ? UIConfig.bdbs_color : UIConfig.bdb_color;
			} else {
				currentbbColor = selected ? selectedBorderColor : borderColor;
				innerUp = selected ? selectedBorderColor : borderColor;
				innerDown = selected ? selectedBorderColor : borderColor;
			}

			int colors[] = new int[] { currentbbColor, innerUp, innerDown,
					currentbbColor };
			int border[][] = null;
			if (UIConfig.menu_3d == true) {
				border = new int[][] {
						new int[] { currentbbColor, currentbbColor, -1 },
						new int[] { currentbbColor, innerUp, -1 },
						new int[] { -1, -1, -1 }, };
			} else {
				int diagColor = UIUtils.medColor(
						getBg_color() >= 0 ? getBg_color() : UIConfig.bg_color,
						currentbbColor);
				int innerColor = UIUtils.medColor(
						selected ? (selectedColor != -1 ? selectedColor
								: UIConfig.button_selected_color)
								: (buttonColor != -1 ? buttonColor
										: UIConfig.button_color),
						currentbbColor);
				border = new int[][] { new int[] { -1, diagColor, -1 },
						new int[] { diagColor, innerDown, -1 },
						new int[] { -1, -1, innerColor }, };
			}
			drawBorder(g, new int[] { x0, y0, x1, y1 }, colors, border);
			if (UIConfig.menu_3d == true) {
				g.setColor(innerDown);
				g.drawPixel(x1 - 1, y0 + 1);
				g.drawPixel(x0 + 1, y1 - 1);
				g.drawPixel(x1 - 1, y1 - 1);
			}
			g.setColor(oldColor);
		}

		g.translate(originalX - g.getTranslateX(), originalY
				- g.getTranslateY());

	}

	//	protected void drawBorder(Graphics g, int x0, int y0, int x1, int y1) {
	//		int currentbbColor = selected ? UIConfig.bbs_color : UIConfig.bb_color;
	//
	//		g.setColor(currentbbColor);
	//		g.drawRect(x0, y0, x1 - x0, y1 - y0);
	//		g.setColor(selected ? UIConfig.blbs_color : UIConfig.blb_color);
	//		g.drawLine(x0 + 1, y0 + 1, x1 - 1, y0 + 1);
	//		g.drawLine(x0 + 1, y0 + 1, x0 + 1, y1 - 1);
	//		g.setColor(selected ? UIConfig.bdbs_color : UIConfig.bdb_color);
	//		g.drawLine(x1 - 1, y1 - 1, x1 - 1, y0 + 1);
	//		g.drawLine(x1 - 1, y1 - 1, x0 + 1, y1 - 1);
	//
	//		if (UIConfig.menu_3d == false) {
	//			//			g.setStrokeStyle(Graphics.SOLID);
	//			//			g.setColor(currentbbColor );
	//			//			g.drawRoundRect(x0, y0, x1-x0, y1-y0, 2, 2);
	//			//			//g.drawRect(x0, y0, x1-x0, y1-y0);
	//			//			g.setColor(selected ? UIConfig.blbs_color: UIConfig.blb_color );
	//			//			g.drawRoundRect(x0+1, y0+1, x1-x0-2, y1-y0-2, 2, 2);
	//
	//			g.drawRect(x0 + 1, y0 + 1, x1 - x0 - 2, y1 - y0 - 2);
	//
	//			g.setColor(this.getBg_color());
	//			g.drawLine(x0, y0, x0, y0);
	//			g.drawLine(x1, y0, x1, y0);
	//			g.drawLine(x0, y1, x0, y1);
	//			g.drawLine(x1, y1, x1, y1);
	//			g.setColor(UIUtils.medColor(getBg_color() >= 0 ? getBg_color()
	//					: UIConfig.bg_color, currentbbColor));
	//			g.drawLine(x0 + 1, y0, x0, y0 + 1);
	//			g.drawLine(x0 + 1, y1, x0, y1 - 1);
	//			g.drawLine(x1 - 1, y0, x1, y0 + 1);
	//			g.drawLine(x1, y1 - 1, x1 - 1, y1);
	//			g.setColor(UIUtils.medColor(
	//					selected ? UIConfig.button_selected_color
	//							: UIConfig.button_color, currentbbColor));
	//			g.drawLine(x0 + 2, y0 + 2, x0 + 2, y0 + 2);
	//			g.drawLine(x1 - 2, y1 - 2, x1 - 2, y1 - 2);
	//			g.drawLine(x0 + 2, y1 - 2, x0 + 2, y1 - 2);
	//			g.drawLine(x1 - 2, y0 + 2, x1 - 2, y0 + 2);
	//
	//		}
	//	}

	/**
	 * @return the pressed
	 */
	public boolean isPressed() {
		return pressed;
	}

	/**
	 * @param pressed
	 *            the pressed to set
	 */
	public void setPressed(boolean pressed) {
		this.pressed = pressed;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getHeight(UIGraphics g) {
		int marginSpace = (8 + 2 * buttonHPadding);
		this.width -= marginSpace;
		int superHeight = super.getHeight(g) + 8;
		this.width += marginSpace;
		if (this.getBgImage() != null) {
			int bgHeight = getBgImage().getHeight();
			if (bgHeight > superHeight) superHeight = bgHeight;
		}
		return superHeight;
	}

	public void setButtonHPadding(int hPadding) {
		this.buttonHPadding = hPadding;
	}

	public int buttonHPadding() {
		return buttonHPadding;
	}

	public void setButtonColor(int buttonColor) {
		this.buttonColor = buttonColor;
	}

	public int getButtonColor() {
		return buttonColor;
	}

	/**
	 * @param borderColor the borderColor to set
	 */
	public void setBorderColor(int borderColor) {
		this.borderColor = borderColor;
	}

	/**
	 * @return the borderColor
	 */
	public int getBorderColor() {
		return borderColor;
	}

	/**
	 * @param borderSelected the borderSelected to set
	 */
	public void setSelectedBorderColor(int borderSelected) {
		this.selectedBorderColor = borderSelected;
	}

	/**
	 * @return the borderSelected
	 */
	public int getSelectedBorderColor() {
		return this.selectedBorderColor;
	}

}
