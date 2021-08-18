// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UILabel.java 2421 2011-01-24 17:22:13Z luca $
 */

/**
 * 
 */
package it.yup.ui;

import it.yup.ui.TokenIterator.UIISplittableItem;
import it.yup.ui.wrappers.UIFont;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.ui.wrappers.UIImage;

import java.util.Enumeration;
import java.util.Vector;

/**
 * @author luca
 * 
 */
public class UILabel extends UIItem implements UIISplittableItem {

	String text;
	UIImage img;
	boolean flip = false;

	protected Vector textLines = null;

	/**
	 * The font used to paint the Label
	 */
	UIFont font = null;

	/**
	 * The text of the label is "wrapped" it text is too long to fit a single
	 * line only if wrappable is set to true. Subclasses are likely to override
	 * this behaviour.
	 */
	boolean wrappable = false;

	int hPadding = 0;

	int vPadding = 0;

	private int maxWrappedLines = Short.MAX_VALUE;

	private boolean _3d = false;

	public UILabel(String text) {
		this(null, text);
	}

	public UILabel(UIImage img) {
		this(img, "");
	}

	public UILabel(UILabel label) {
		this(label.getImg(), label.getText());
	}

	/**
	 * @param screen
	 */
	public UILabel(UIImage img, String text) {
		this.img = img;
		this.text = text;
		// if an img is present it is a nonsense to have the
		// label wrapped! or not ?
		if (this.img != null) this.wrappable = false;
		// TODO Auto-generated constructor stub
	}

	/*
	 * Used to indicated the anchor point for the label with the same syntax as
	 * Graphics (TOP, VCENTER,BOTTOM, LEFT, HCENTER,RIGHT ). It is possible to
	 * use them in or like: anchorPoint = Graphics.TOP | Graphics.LEFT; the
	 * default value is Graphics.VCENTER | Graphics.LEFT.
	 */
	int anchorPoint = UIGraphics.TOP | UIGraphics.LEFT;

	private int imgAnchorPoint = -1;
	private int _3dColor = 0x888888;

	/**
	 * used to get the different components of the acnhorPoint,
	 */
	private int[] divideAP() {
		int[] result = new int[2];
		result[0] = UIGraphics.getVAnchor(anchorPoint);
		result[1] = UIGraphics.getHAnchor(anchorPoint);
		return result;

	}

	public int getTextWidth(String textLine, UIFont font, int startIndex,
			int length) {
		return font.substringWidth(textLine, startIndex, length);
	}

	private void paintLine(UIGraphics g, int w, int h, UIImage imgLine,
			String textLine) {
		int lineHeight = 0;
		int lineWidth = 0;
		int textHeight = 0, textWidth = 0, imgHeight = 0;
		UIFont usedFont = (this.font != null ? this.font : g.getFont());
		if (imgLine != null) {
			lineHeight = imgLine.getHeight() + 2;
			imgHeight = imgLine.getHeight();
		}
		if (h > lineHeight) {
			lineHeight = h;
		}
		textHeight = usedFont.getHeight();
		textWidth = getTextWidth(textLine, usedFont, 0, textLine.length());
		// text and Image must have an offset from the TOP
		// in order to be aligned
		int imgVerticalOffset = (lineHeight - imgHeight - 2) / 2;
		int textVerticalOffset = (h - textHeight - 2) / 2;

		// the horizontalSpace left "free"; must be set depending on the
		// orientation
		int horizontalSpace = 0;
		lineWidth = 0;
		if (imgLine != null) lineWidth = imgLine.getWidth() + 3 + textWidth;
		else
			lineWidth = textWidth + 2;
		lineWidth += (2 * hPadding);
		if (w > lineWidth) {
			horizontalSpace = w - lineWidth;
			this.width = w;
		}
		int horizontalAnchor = this.divideAP()[1];
		switch (horizontalAnchor) {
			case UIGraphics.LEFT:
				horizontalSpace = hPadding;
				break;
			case UIGraphics.HCENTER:
				horizontalSpace /= 2;
				break;
			case UIGraphics.RIGHT:
				horizontalSpace -= hPadding;
				break;
			default:
				break;
		}
		// first erase background
		g.setColor(getBg_color() >= 0 ? getBg_color() : UIConfig.bg_color);
		if (getBg_color() != UIItem.TRANSPARENT_COLOR) {
			if (this.getGradientColor() < 0) g.fillRect(0, 0, w, h);
			else {
				g.fillRect(0, 0, w, h / 2);
				g.setColor(this.getGradientColor());
				g.fillRect(0, h / 2, w, h - (h / 2));
			}

			// than paint in case it is needed
			if (selected) {
				g.setColor(this.getSelectedColor());
				if (this.getGradientSelectedColor() < 0) g.fillRect(0, 0, w, h);
				else {
					g.fillRect(0, 0, w, h / 2);
					g.setColor(this.getGradientSelectedColor());
					g.fillRect(0, h / 2, w, h);
				}
			}
		}

		g.setColor(selected ? UIConfig.header_fg : UIConfig.fg_color);
		if (this.fg_color >= 0) g.setColor(this.fg_color);
		if (flip == false) {
			if (imgLine != null) {
				// if the imgAnchorPoint is set it means image must be overridden
				int imgHOffset = 0;
				if (UIGraphics.getHAnchor(imgAnchorPoint) != UIGraphics.LEFT) {
					imgHOffset += horizontalSpace;
				}
				if (UIGraphics.getVAnchor(imgAnchorPoint) == UIGraphics.TOP) {
					imgVerticalOffset = vPadding;
				}
				g.drawImage(img, imgHOffset, 1 + imgVerticalOffset,
						UIGraphics.LEFT | UIGraphics.TOP);
				paintTextLine(g, textLine, imgLine.getWidth() + 2
						+ horizontalSpace, 1 + textVerticalOffset);
			} else {
				paintTextLine(g, textLine, 1 + horizontalSpace,
						1 + textVerticalOffset);
			}
		} else {
			paintTextLine(g, textLine, 1 + horizontalSpace,
					1 + textVerticalOffset);
			if (imgLine != null) {
				g
						.drawImage(imgLine, textWidth + 2 + horizontalSpace,
								1 + imgVerticalOffset, UIGraphics.LEFT
										| UIGraphics.TOP);
			}
		}
	}

	void paintTextLine(UIGraphics g, String textLine, int horizontalSpace,
			int verticalSpace) {
		if (this._3d) {
			int oldColor = g.getColor();
			g.setColor(get_3dColor());
			g.translate(0, -1);
			g.drawString(textLine, horizontalSpace, verticalSpace,
					UIGraphics.LEFT | UIGraphics.TOP);
			g.setColor(oldColor);
			g.translate(0, +1);
		}
		g.drawString(textLine, horizontalSpace, verticalSpace, UIGraphics.LEFT
				| UIGraphics.TOP);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.yup.ui.UIItem#paint(javax.microedition.lcdui.Graphics, int, int)
	 */
	protected void paint(UIGraphics g, int w, int h) {
		UIFont oldFont = g.getFont();
		if (this.font != null) g.setFont(this.font);

		this.height = 0;
		this.width = (2 * hPadding);
		if (this.img != null) {
			if (this.text != null && this.text.length() >= 2) {
				this.width += (img.getWidth() + 3 + g.getFont().stringWidth(
						text));
			} else
				this.width += (img.getWidth() + 2);
		} else
			this.width += (g.getFont().stringWidth(text) + 2);

		// if the label is not wrappable we remove the extra characters
		// we must check that image is not wider than w
		if (this.wrappable == false) {
			if (this.width > w && (this.img == null || this.img.getWidth() < w)) {
				String newText = "";
				this.width = (2 * hPadding);
				/*
				 * while (this.width < w) {
				 * newText = newText + this.text.charAt(index);
				 * this.width = g.getFont().stringWidth(newText + "...") + 2;
				 * if (img != null) this.width += (img.getWidth() + 1);
				 * index++;
				 * }
				 */
				int min = 0, max = this.text.length(), med = 0;
				int nl = 0;
				while (min != max) {
					med = (min + max) / 2;
					newText = this.text.substring(0, med);
					nl = g.getFont().stringWidth(newText + "...") + 2;
					if (img != null) this.width += (img.getWidth() + 1);
					if (nl < w) min = med + 1;
					else
						max = med;
				}
				if (newText.length() > 1) {
					newText = newText.substring(0, newText.length() - 2);
					newText = newText + "...";
				}

				paintLine(g, w, h, this.img, newText);
			} else {
				paintLine(g, w, h, this.img, this.text);
			}
		} else {
			this.width = w;

			// just in case it has not been called before
			UIFont usedFont = (this.font != null ? this.font : g.getFont());
			int reservedWidth = this.width - (2 * hPadding);
			reservedWidth -= (img != null ? (img.getWidth() + 3) : 0);
			if (textLines == null) {
				computeTextLines(usedFont, reservedWidth);
			}
			this.height = (usedFont.getHeight() + 2) * this.textLines.size()
					+ 2 * vPadding;

			int originalY = g.getTranslateY();
			int reservedHeight = (this.textLines.size() > 0 ? h
					/ this.textLines.size() : 0);
			int summedheight = 0;
			Enumeration en = this.textLines.elements();
			int i = 0;
			while (en.hasMoreElements()) {
				String subStr = (String) en.nextElement();
				if (i == textLines.size() - 1) reservedHeight = h
						- summedheight;
				paintLine(g, w, reservedHeight, null, subStr);

				g.translate(0, reservedHeight);
				summedheight += reservedHeight;
				i++;
			}
			g.translate(0, originalY - g.getTranslateY());
			// draw the first line with image
			String tempText = this.textLines.size() > 0 ? (String) textLines
					.elementAt(0) : "";
			paintLine(g, w, reservedHeight, this.img, tempText);
		}

		// #mdebug
				//				System.out.println("Drawn UILabel '" + text + "' at: ("
				//						+ g.getTranslateX() + ", " + g.getTranslateY() + ")"
				//						+ (selected ? "S" : ""));
		// #enddebug

		g.setFont(oldFont);
	}

	protected void computeTextLines(UIFont usedFont, int w) {
		textLines = new Vector();
		TokenIterator ti = new TokenIterator(this);
		ti.computeLazyLines(usedFont, w);
		int count = 0;
		while (count < ti.getLinesNumber() && count < maxWrappedLines) {
			String ithString = ti.elementAt(count).trim();
			if (ithString.length() > 0) textLines.addElement(ithString);
			count++;
		}
		// a dummy "\n" to be sure to end
		//		String textChars = (text + "\n");
		//		for (int i = 0; i < textChars.length(); i++) {
		//			ithChar = textChars.charAt(i);
		//			switch (ithChar) {
		//				case '\n':
		//				case '\r':
		//					if (i == startIndex) {
		//						startIndex++;
		//					} else {
		//						int ithWidth = this.getTextWidth(textChars.substring(startIndex,
		//								i), usedFont);
		//						if (ithWidth <= w) {
		//							String ithString = textChars.substring(startIndex,
		//									i);
		//							textLines.addElement(ithString);
		//							startIndex = i + 1;
		//							goodIndex = startIndex;
		//						} else
		//							needSplit = true;
		//					}
		//					break;
		//
		//				case ' ':
		//					if (i == startIndex) {
		//						startIndex++;
		//					} else {
		//						int ithWidth = this.getTextWidth(textChars.substring(
		//								startIndex, i), usedFont);
		//						if (ithWidth <= w) goodIndex = i - 1;
		//						else
		//							needSplit = true;
		//					}
		//					break;
		//			}
		//			if (needSplit) {
		//				if (goodIndex > startIndex) {
		//					String ithString = textChars.substring(startIndex,
		//							goodIndex + 1);
		//					textLines.addElement(ithString);
		//					startIndex = goodIndex + 1;
		//				} else {
		//					int min = startIndex;
		//					int max = i;
		//					int med = 0;
		//					while (min != max) {
		//						med = (min + max) / 2;
		//						int ithWidth = getTextWidth(textChars.substring(
		//								startIndex, med + 1), usedFont);
		//						if (ithWidth <= w) min = med + 1;
		//						else
		//							max = med;
		//					}
		//					String ithString = textChars.substring(startIndex, min);
		//					textLines.addElement(ithString);
		//					startIndex = min;
		//				}
		//				goodIndex = startIndex;
		//				i = goodIndex - 1;
		//				needSplit = false;
		//			}
		//		}
	}

	/**
	 * @return the flip
	 */
	public boolean isFlip() {
		return flip;
	}

	/**
	 * @param flip
	 *            the flip to set
	 */
	public void setFlip(boolean flip) {
		this.flip = flip;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	public int getHeight(UIGraphics g) {
		UIFont usedFont = (this.font != null ? this.font : g.getFont());
		this.height = 0;
		if (this.wrappable == false) {
			if (img != null) this.height = img.getHeight() + 2;
			if (usedFont.getHeight() + 2 > this.height) this.height = usedFont
					.getHeight() + 2;

		} else {
			// #mdebug
						//			if (this.getContainer() == null || ((UIItem) this.getContainer()).getWidth()<=0)
						//			{
						//				System.out.println("+++++ wrong container width");
						//			}
			// #enddebug

			int fontHeight = usedFont.getHeight();
			if (textLines == null) {
				int reservedWidth = this.width - (2 * hPadding);
				reservedWidth -= (img != null ? (img.getWidth() + 3) : 0);
				computeTextLines(usedFont, reservedWidth);
			}
			if (img != null) this.height = img.getHeight() + 2;
			int tempHeight = (fontHeight + 2) * this.textLines.size();
			if (tempHeight > this.height) this.height = tempHeight;
		}
		height += (2 * vPadding);
		return height;
	}

	/**
	 * @return this label text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets/changes the text for this label
	 * 
	 * @param _text
	 */
	public void setText(String _text) {
		if (text.equals(_text) == false) this.textLines = null;
		text = _text;
		dirty = true;
	}

	/**
	 * @param anchorPoint
	 *            the anchorPoint to set
	 */
	public void setAnchorPoint(int anchorPoint) {
		this.anchorPoint = anchorPoint;
	}

	/**
	 * @return the anchorPoint
	 */
	public int getAnchorPoint() {
		return anchorPoint;
	}

	/**
	 * @param wrappable
	 *            the wrappable to set
	 */
	public void setWrappable(boolean wrappable, int width) {
		//if (this.img != null) return;
		if (wrappable != this.wrappable) this.setDirty(true);
		this.wrappable = wrappable;
		//		if (this.width != width)
		//			this.textLines = null;
		this.width = width;
	}

	/**
	 * @return the wrappable
	 */
	public boolean isWrappable() {
		return wrappable;
	}

	/**
	 * @param font
	 *            the font to set
	 */
	public void setFont(UIFont font) {
		this.font = font;
	}

	/**
	 * @return the font
	 */
	public UIFont getFont() {
		return font;
	}

	/**
	 * @return the img
	 */
	public UIImage getImg() {
		return img;
	}

	/**
	 * @param img
	 *            the img to set
	 */
	public void setImg(UIImage img) {
		if (this.img == img) return;
		this.dirty = true;
		this.img = img;
		this.wrappable = false;
	}

	public void setImgAnchorPoint(int imgAnchorPoint) {
		this.imgAnchorPoint = imgAnchorPoint;
	}

	public int getImgAnchorPoint() {
		return imgAnchorPoint;
	}

	public void setPaddings(int hPadding, int vPadding) {
		this.hPadding = hPadding;
		this.vPadding = vPadding;
	}

	public int[] getPaddings() {
		return new int[] { hPadding, vPadding };
	}

	public void setMaxWrappedLines(int maxWrappedLines) {
		this.maxWrappedLines = maxWrappedLines;
	}

	public int getMaxWrappedLines() {
		return maxWrappedLines;
	}

	/**
	 * @param _3d the _3d to set
	 */
	public void set_3d(boolean _3d) {
		this._3d = _3d;
	}

	/**
	 * @return the _3d
	 */
	public boolean is_3d() {
		return _3d;
	}

	/**
	 * @param _3dColor the _3dColor to set
	 */
	public void set_3dColor(int _3dColor) {
		this._3dColor = _3dColor;
	}

	/**
	 * @return the _3dColor
	 */
	public int get_3dColor() {
		return _3dColor;
	}
}
