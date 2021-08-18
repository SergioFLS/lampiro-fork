// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UIUtils.java 1858 2009-10-16 22:42:29Z luca $
 */
package it.yup.ui;

import it.yup.ui.wrappers.UIGraphics;

import java.util.Enumeration;
import java.util.Vector;

public class UIUtils {

	protected static boolean contains(Vector items, UIItem item) {
		if (items.contains(item)) return true;
		Enumeration en = items.elements();
		while (en.hasMoreElements()) {
			UIItem ithItem = (UIItem) en.nextElement();
			if (ithItem instanceof UIIContainer) {
				UIIContainer iic = (UIIContainer) ithItem;
				if (iic.contains(item)) return true;
			}
		}
		return false;
	}

	public static int colorize(int inputColor, int percentage) {
		int[] rgb = new int[] { inputColor, inputColor, inputColor };
		int outputColor = 0;
		for (int i = 0; i < 3; i++) {
			rgb[i] &= (0xFF0000 >> (i * 8));
			int temp = rgb[i] >> (16 - i * 8);
			temp += ((0xFF * (percentage > 0 ? 1 : 0) - temp) * Math
					.abs(percentage)) / 100;
			rgb[i] = temp << (16 - i * 8);
			outputColor += rgb[i];
		}
		return outputColor;
	}

	/*
	 * An helper function that builds and initialize a UIMenu.
	 * 
	 * @param item
	 * the UIMenu title
	 * 
	 * @param absoluteX
	 * the X position of the UIMenu
	 * 
	 * @param absoluteY
	 * the Y position of the UIMenu
	 * 
	 * @param width
	 * the width of the UIMenu
	 * 
	 * @param firstItem
	 * the first item to add to the UIMenu
	 */
	public static UIMenu easyMenu(String title, int absoluteX, int absoluteY,
			int width, UIItem firstItem) {
		UIMenu retMenu = new UIMenu(title);
		if (absoluteX > 0) retMenu.setAbsoluteX(absoluteX);
		if (absoluteY > 0) retMenu.setAbsoluteY(absoluteY);
		if (width > 0) retMenu.setWidth(width);
		if (firstItem != null) {
			retMenu.append(firstItem);
			retMenu.setSelectedItem(firstItem);
		}
		return retMenu;
	}

	public static UIMenu easyMenu(String title, int absoluteX, int absoluteY,
			int width, UIItem firstItem, String cancelString,
			String selectString) {
		UIMenu retMenu = UIUtils.easyMenu(title, absoluteX, absoluteY, width,
				firstItem);
		retMenu.cancelMenuString = cancelString;
		retMenu.selectMenuString = selectString;
		return retMenu;
	}

	/*
	 * An helper function that builds an horizontal layout of three items.
	 * The first and third item are dummy and the second is the passed item.
	 * Size is the size in pixel od the seconde layout element
	 * 
	 * @param item
	 * the item to insert in the middle of the layout
	 * 
	 * @param size
	 * the size of the middle element of the layout
	 */
	public static UIHLayout easyCenterLayout(UIItem item, int size) {
		UIHLayout buttonLayout = new UIHLayout(3);
		UISeparator dummySeparator = new UISeparator(0);
		if (item instanceof UILabel) ((UILabel) item)
				.setAnchorPoint(UIGraphics.TOP | UIGraphics.HCENTER);
		buttonLayout.setGroup(false);
		buttonLayout.insert(dummySeparator, 0, 50,
				UILayout.CONSTRAINT_PERCENTUAL);
		buttonLayout.insert(item, 1, size, UILayout.CONSTRAINT_PIXELS);
		buttonLayout.insert(dummySeparator, 2, 50,
				UILayout.CONSTRAINT_PERCENTUAL);
		return buttonLayout;
	}

	public static UIHLayout easyButtonsLayout(UIButton leftButton,
			UIButton rightButton) {
		UIHLayout buttonLayout = new UIHLayout(5);
		UISeparator dummySeparator = new UISeparator(0);
		buttonLayout.setGroup(false);
		buttonLayout.insert(dummySeparator, 0, 10, UILayout.CONSTRAINT_PIXELS);
		buttonLayout.insert(leftButton, 1, 50, UILayout.CONSTRAINT_PERCENTUAL);
		buttonLayout.insert(dummySeparator, 2, 7, UILayout.CONSTRAINT_PIXELS);
		buttonLayout.insert(rightButton, 3, 50, UILayout.CONSTRAINT_PERCENTUAL);
		buttonLayout.insert(dummySeparator, 4, 10, UILayout.CONSTRAINT_PIXELS);
		return buttonLayout;
	}

	public static int medColor(int firstColor, int secondColor) {
		int[] rgb1 = new int[] { firstColor & 0xFF0000, firstColor & 0xFF00,
				firstColor & 0xFF };
		int[] rgb2 = new int[] { secondColor & 0xFF0000, secondColor & 0xFF00,
				secondColor & 0xFF };
		int outputColor = 0;
		for (int i = 0; i < 3; i++) {
			int temp = ((rgb1[i] + rgb2[i]) / 2) & (0xFF0000 >> (i * 8));
			outputColor += temp;
		}
		return outputColor;
	}

}
