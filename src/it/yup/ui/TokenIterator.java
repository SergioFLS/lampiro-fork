// #condition MIDP
/**
 * 
 */
package it.yup.ui;

import it.yup.ui.wrappers.UIFont;
import java.util.Vector;

public class TokenIterator {

	public interface UIISplittableItem {
		String getText();

		int getTextWidth(String textLine, UIFont font, int startIndex,
				int length);
	}

	/**
	 * 
	 */
	private final UIISplittableItem splittableItem;

	boolean computed = false;

	private int linesNumber = 0;

	private Vector linesIndeces = new Vector();

	private UIFont font;

	private int w;

	/**
	 * @param textPanel
	 *            TODO
	 * 
	 */
	public TokenIterator(UIISplittableItem textItem) {
		this.splittableItem = textItem;

	}

	public void reset() {
		computed = false;
		linesIndeces.removeAllElements();
		linesNumber = 0;
	}

	public boolean isComputed() {
		return computed;
	}

	public void computeLazyLines(UIFont f, int w) {
		linesNumber = (int) Math.ceil((float) f.stringWidth(this.splittableItem
				.getText())
				/ (float) w);
		if (linesNumber < 0)
			linesNumber = 1;
		computed = true;
		this.w = w;
		this.font = f;
	}

	public int getLinesNumber() {
		// TODO Auto-generated method stub
		return linesNumber;
	}

	public String elementAt(int i) {
		int[] limits = null;
		while (i >= linesIndeces.size()) {
			computeTextLines();
			limits = (int[]) linesIndeces.elementAt(i);
			linesNumber = (int) Math.ceil((float) (this.splittableItem
					.getText().length() * linesIndeces.size())
					/ (float) (limits[1] + 1));
		}
		limits = (int[]) linesIndeces.elementAt(i);
		if (limits[0] >= 0 && limits[1] >= 0
				&& limits[0] < this.splittableItem.getText().length()
				&& limits[1] < this.splittableItem.getText().length())
			return this.splittableItem.getText().substring(limits[0],
					limits[1] + 1);
		else
			return "";
	}

	public void computeTextLines() {
		char ithChar = 0;
		boolean needSplit = false;
		int[] limits = null;
		if (linesIndeces.size() > 0) {
			limits = (int[]) linesIndeces.lastElement();
		} else {
			limits = new int[] { -1, -1 };
		}
		int startIndex = limits[1] + 1;
		int goodIndex = limits[1] + 1;
		String text = this.splittableItem.getText();
		for (int i = limits[1] + 1; i < text.length(); i++) {
			ithChar = text.charAt(i);
			switch (ithChar) {
			case '\n':
			case '\r':
				if (i == startIndex) {
					startIndex++;
				} else {
					int ithWidth = splittableItem.getTextWidth(text, font,
							startIndex, i - startIndex);
					if (ithWidth <= w) {
						linesIndeces
								.addElement(new int[] { startIndex, i - 1 });
						return;
					} else
						needSplit = true;
				}
				break;

			case ' ':
				if (i == startIndex) {
					startIndex++;
				} else {
					int ithWidth = splittableItem.getTextWidth(text, font,
							startIndex, i - startIndex);
					if (ithWidth <= w)
						goodIndex = i - 1;
					else
						needSplit = true;
				}
				break;
			}
			if (needSplit) {
				if (goodIndex > startIndex) {
					linesIndeces
							.addElement(new int[] { startIndex, goodIndex });
				} else {
					splitLongString(startIndex, i);
				}
				return;
			}
		}
		// last line special ending
		int lastIndex = text.length() - 1;
		// if (startIndex > lastIndex) startIndex = lastIndex;
		if (lastIndex < 0) {
			startIndex = 0;
			lastIndex = 0;
		}
		int length = lastIndex - startIndex + 1;
		if (length + startIndex > text.length())
			length = 0;
		int ithWidth = splittableItem.getTextWidth(text, font, startIndex,
				length);
		if (ithWidth <= w)
			linesIndeces.addElement(new int[] { startIndex, lastIndex });
		else {
			if (goodIndex > startIndex && goodIndex <= lastIndex) {
				linesIndeces.addElement(new int[] { startIndex, goodIndex });
				return;
			}
			splitLongString(startIndex, lastIndex);
		}
	}

	/**
	 * @param startIndex
	 * @param endIndex
	 */
	private void splitLongString(int startIndex, int endIndex) {
		int min = startIndex;
		int max = endIndex;
		int med = 0;
		int ithWidth = 0;
		while (min != max) {
			med = (min + max) / 2;
			ithWidth = splittableItem.getTextWidth(this.splittableItem
					.getText(), font, startIndex, med + 1 - startIndex);
			if (ithWidth <= w)
				min = med + 1;
			else
				max = med;
		}
		linesIndeces.addElement(new int[] { startIndex, min - 1 });
	}
}
