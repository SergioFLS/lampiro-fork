// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UICheckbox.java 2386 2011-01-17 11:55:37Z luca $
 */

package it.yup.ui;

import it.yup.ui.wrappers.UIImage;
import java.io.IOException;

/**
 * 
 */

/**
 * @author luca
 * 
 */
public class UICheckbox extends UILabel {
	private UIImage checkedImg = UICanvas.getUIImage("/icons/checked.png");
	private UIImage uncheckedImg = UICanvas.getUIImage("/icons/unchecked.png");

	/**
	 * Keeps the checked state of the Checkbox
	 * 
	 */
	private boolean checked = false;

	/**
	 * @throws IOException
	 * 
	 */
	public UICheckbox(String text) {
		super(UICanvas.getUIImage("/icons/unchecked.png"), text);
		this.focusable = true;
		this.wrappable = false;
		if (UICanvas.getInstance().hasPointerMotionEvents()) this.setPaddings(
				2, 6);
	}

	public UICheckbox(String text, boolean checked) {
		this(text);
		this.setChecked(checked);
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		boolean changed = false;
		if (checked != this.checked) changed = true;
		this.checked = checked;
		if (changed == true) {
			if (checked == true) this.img = this.checkedImg;
			else
				this.img = this.uncheckedImg;
			this.dirty = true;
			this.askRepaint();
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	public boolean keyPressed(int key) {
		if (UICanvas.getInstance().getGameAction(key) == UICanvas.FIRE) this
				.setChecked(!this.checked);
		return false;
	}

	/**
	 * @param checkedImg
	 *            the checkedImg to set
	 */
	public void setCheckedImg(UIImage checkedImg) {
		this.checkedImg = checkedImg;
		if (this.isChecked()) setImg(checkedImg);
		this.dirty = true;
		this.askRepaint();
	}

	/**
	 * @return the checkedImg
	 */
	public UIImage getCheckedImg() {
		return checkedImg;
	}

	/**
	 * @param uncheckedImg
	 *            the uncheckedImg to set
	 */
	public void setUncheckedImg(UIImage uncheckedImg) {
		this.uncheckedImg = uncheckedImg;
		if (this.isChecked() == false) setImg(uncheckedImg);
		this.dirty = true;
		this.askRepaint();
	}

	/**
	 * @return the uncheckedImg
	 */
	public UIImage getUncheckedImg() {
		return uncheckedImg;
	}
}
