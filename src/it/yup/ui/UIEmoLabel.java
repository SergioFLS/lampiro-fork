// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UIEmoLabel.java 2325 2010-11-15 20:07:28Z luca $
*/

/**
 * 
 */
package it.yup.ui;

import it.yup.ui.wrappers.UIFont;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.ui.wrappers.UIImage;

import java.util.Hashtable;

/**
 * @author luca
 * 
 */
public class UIEmoLabel extends UILabel {

	private static Hashtable emos;
	private static String[] codes;

	/**
	 * @param text
	 */
	public UIEmoLabel(String text) {
		super(text);
	}

	static {
		initialize();
	}

	private static void initialize() {
		emos = new Hashtable();
		// the number of emoticons
		int emoNumber = 16;
		codes = new String[] { /*
													                * these are the possible emos the next lines
													                * are only other codes for these
													                */
		":-S", "B-)", ":D", ":O", "O:)", ">:)", "|-)", ";-)", ":-/", ":-@",
				":-&", ":'(", ":(", ":)", ":*", ":P",
				/* additional codes for the same emos (starting at emonuber index) */
				";)", ":d", ":o", "o:)", ":p" };
		for (int i = 0; i < emoNumber; i++) {
			emos.put(codes[i], UICanvas.getUIImage("/emo/" + i + ".png"));
		}
		// additional code (in case of multiple coding for the same emo)
		emos.put(codes[emoNumber], UICanvas.getUIImage("/emo/7.png"));
		emoNumber++;
		emos.put(codes[emoNumber], UICanvas.getUIImage("/emo/2.png"));
		emoNumber++;
		emos.put(codes[emoNumber], UICanvas.getUIImage("/emo/3.png"));
		emoNumber++;
		emos.put(codes[emoNumber], UICanvas.getUIImage("/emo/4.png"));
		emoNumber++;
		emos.put(codes[emoNumber], UICanvas.getUIImage("/emo/15.png"));
	}

	void paintTextLine(UIGraphics g, String textLine, int horizontalSpace,
			int verticalSpace) {
		Object[] emoTuple = this.findEmoTuple(textLine, 0, textLine.length());
		if (emoTuple == null) {
			g.drawString(textLine, horizontalSpace, verticalSpace,
					UIGraphics.LEFT | UIGraphics.TOP);
		} else {
			int index = ((Integer) emoTuple[0]).intValue();
			String code = (String) emoTuple[1];
			UIImage img = (UIImage) UIEmoLabel.emos.get(code);
			String secondHalf = textLine.substring(index + code.length());
			g.drawSubstring(textLine, 0, index, horizontalSpace, verticalSpace,
					UIGraphics.LEFT | UIGraphics.TOP);
			int additionalSpace = 0;
			if (g.getFont().getHeight() > img.getHeight()) {
				additionalSpace = (g.getFont().getHeight() - img.getHeight()) / 2;
			}
			int firstWidth = g.getFont().substringWidth(textLine, 0, index);
			g.drawImage(img, horizontalSpace + firstWidth, verticalSpace
					+ additionalSpace, UIGraphics.LEFT | UIGraphics.TOP);
			int originalX = g.getTranslateX();
			g.translate(horizontalSpace + firstWidth + img.getWidth() + 1, 0);
			paintTextLine(g, secondHalf, 0, verticalSpace);
			g.translate(originalX - g.getTranslateX(), 0);
		}
	}

	private Object[] findEmoTuple(String textLine, int startIndex, int length) {
		// it is better to make a substr to avoid searching for the whole string
		// it can be very long
		String substr = textLine.substring(startIndex, startIndex + length);
		int index = 0;
		String code = null;
		for (int i = 0; i < UIEmoLabel.codes.length; i++) {
			index = substr.indexOf(UIEmoLabel.codes[i]);
			if (index >= 0) {
				code = UIEmoLabel.codes[i];
				break;
			}
		}
		if (code != null) {
			Object[] retVal = new Object[2];
			retVal[0] = new Integer(index + startIndex);
			retVal[1] = code;
			return retVal;
		}

		return null;
	}

	public int getTextWidth(String textLine, UIFont font, int startIndex,
			int length) {
		Object[] emoTuple = this.findEmoTuple(textLine, startIndex, length);
		if (emoTuple == null) {
			return font.substringWidth(textLine, startIndex, length);
		} else {
			int index = ((Integer) emoTuple[0]).intValue();
			String code = (String) emoTuple[1];
			UIImage img = (UIImage) UIEmoLabel.emos.get(code);
			int endIndex = index + code.length();
			// new length could be negative if an emoticon is splitted
			int newLength = length - endIndex + startIndex;
			if (newLength < 0) newLength = 0;
			return font
					.substringWidth(textLine, startIndex, index - startIndex)
					+ img.getWidth()
					+ 1
					+ getTextWidth(textLine, font, endIndex, newLength);
		}
	}
}
