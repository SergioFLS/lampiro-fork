// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UIPanel.java 2435 2011-01-30 19:13:08Z luca $
 */

package it.yup.ui;

import it.yup.ui.wrappers.UIGraphics;
import it.yup.ui.wrappers.UIImage;
import java.util.Enumeration;
import java.util.Vector;

/**
 * A panel that stacks vertically items vertically. If the container height is
 * exceeeded, a scrollbar is drawn.
 */
public class UIPanel extends UIItem implements UIIContainer {

	/** the contained items */
	private Vector items;

	/** real height */
	int maxHeight;

	/** the first visible item */
	private int firstVisible;

	/** the last visible item */
	protected int lastVisible;

	/** the selected item */
	protected int selectedIdx;

	private boolean listMode = false;

	private UIImage pointer = UICanvas.getUIImage("/icons/menuarrow.png");

	private int reverseColor = UIUtils.colorize(UIConfig.bg_color, -10);

	boolean needScrollbar = false;

	/*
	 * If it is equal to true the panel support "roll up" and "roll down"
	 */
	boolean modal = false;

	public UIPanel(boolean modal, boolean listMode) {
		this();
		this.modal = modal;
		this.listMode = listMode;
	}

	public UIPanel() {
		selectedIdx = -1;
		maxHeight = -1;
		items = new Vector();
		this.setFocusable(true);
	}

	public void setScreen(UIScreen _us) {
		screen = _us;
		Enumeration en = this.items.elements();
		while (en.hasMoreElements()) {
			UIItem uit = (UIItem) en.nextElement();
			uit.setScreen(_us);
		}
	}

	protected void paint(UIGraphics g, int w, int h) {
		int rh = computeRealHeight(g);
		boolean oldNeedScrollbar = needScrollbar;
		needScrollbar = false;
		if (isScrollbarNeeded(h, rh)) {
			w -= UIConfig.scrollbarWidth; // + 1;
			needScrollbar = true;
		}
		if (oldNeedScrollbar != needScrollbar) this.setDirty(true);
		int otx = g.getTranslateX();
		int oty = g.getTranslateY();
		// if selectedIndex is after lastVisible or before firstVisible I could do
		// need many redraw
		boolean needRedraw = true;
		while (needRedraw) {
			needRedraw = false;
			int th = 0;

			// an optimization extremely useful especially for the UIaccordion
			int l = 0;
			Enumeration en = this.items.elements();
			int size = items.size();
			// if a control is dirty all the subsequent must be set as dirty since the height
			// can be changed
			boolean lastDirty = false;
			while (en.hasMoreElements() && th < h) {
				UIItem ui = (UIItem) en.nextElement();
				if (l >= firstVisible && l < size) {
					int ih = ui.getHeight(g);
					if (lastDirty == true) {
						ui.setDirty(true);
					}
					if (ui.isDirty()) {
						if (!UICanvas.getInstance().hasPointerMotionEvents()
								|| isOnscreen(g, ui, oty, ih)) {
							if (this.getBgImage() != null
									&& ui.getBg_color() == UIItem.TRANSPARENT_COLOR) {
								int src_x = g.getTranslateX() - otx;
								int src_y = g.getTranslateY() - oty;
								paintBGRegion(g, getBgImage(), src_x, src_y, w,
										ih, 0, 0);
								// any time I draw a bg I need to invalidate the whole child;
								ui.setDirty(true);
							}
							paintIthItem(g, w, ui, ih, l);
						}
						lastDirty = true;
					}
					g.translate(0, ih);
					th += ih;
					lastVisible = l;
				}
				l++;
			}
			g.translate(otx - g.getTranslateX(), oty - g.getTranslateY());
			if (isScrollbarNeeded(h, th)) {
				/* the last item is not fully visible */
				fixLastVisible();
			} else {
				/* fill the gap */
				int oc = g.getColor();
				if (this.getBg_color() != UIItem.TRANSPARENT_COLOR) {
					g.setColor(getBg_color() >= 0 ? getBg_color()
							: UIConfig.bg_color);
					g.fillRect(0, th, w, h - th);// - 1);
				}
				if (this.getBgImage() != null) {
					paintBGRegion(g, getBgImage(), 0, th, w, h - th, 0, th);
				}
				g.setColor(oc);
			}
			/* scroll down -> need to calculate space */
			if (selectedIdx != -1 && selectedIdx > lastVisible
					&& this.selectedIdx > this.firstVisible
					&& selectedIdx < items.size()) {
				centerPanel(g, h);
				for (int i = firstVisible; i < this.items.size(); i++) {
					((UIItem) items.elementAt(i)).setDirty(true);
				}
				needRedraw = true;
			}

			/* up check is easier ;) */
			if (selectedIdx != -1 && selectedIdx < firstVisible) {
				firstVisible = selectedIdx;
				for (int i = firstVisible; i < this.items.size(); i++) {
					((UIItem) items.elementAt(i)).setDirty(true);
				}
				needRedraw = true;
			}
		}
		if (isScrollbarNeeded(h, rh)) {
			w += UIConfig.scrollbarWidth;// + 1;
			drawScrollBar(g, w, h, rh);
		}
		/* resets origin to old value */
		g.translate(otx - g.getTranslateX(), oty - g.getTranslateY());
	}

	/**
	 * 
	 */
	protected void fixLastVisible() {
		lastVisible--;
	}

	/**
	 * @param g
	 */
	protected void centerPanel(UIGraphics g, int h) {
		int delta = 0;
		for (int i = lastVisible + 1; i <= selectedIdx; i++) {
			delta += ((UIItem) items.elementAt(i)).getHeight(g);
		}
		do {
			delta -= ((UIItem) items.elementAt(firstVisible)).getHeight(g);
			firstVisible++;
		} while (delta > 0 && firstVisible < this.items.size() - 1);
	}

	/**
	 * @param h
	 * @param rh
	 * @return
	 */
	protected boolean isScrollbarNeeded(int h, int rh) {
		return rh > h;
	}

	protected boolean isOnscreen(UIGraphics g, UIItem ui, int oty, int ih) {
		int clipY = g.getClipY();
		int clipHeight = g.getClipHeight();
		if (clipY + clipHeight > 0 && ih>clipY) return true;
		else
			return false;
	}

	protected void paintIthItem(UIGraphics g, int w, UIItem ui, int ih, int l) {
		if (listMode == false) {
			ui.paint0(g, w, ih);
			return;
		}

		int oc = g.getColor();
		boolean changeColor = (l % 2 == 1);
		int xOffset = pointer.getWidth() + 1;
		if (changeColor) g.setColor(reverseColor);
		else
			g.setColor(getBg_color() >= 0 ? getBg_color() : UIConfig.bg_color);
		g.fillRect(0, 0, xOffset, ih);
		if (ui.isSelected()) g.drawImage(pointer, 0,
				(ih - pointer.getHeight()) / 2, UIGraphics.TOP
						| UIGraphics.LEFT);
		g.setColor(oc);
		g.translate(xOffset, 0);
		int oldBgColor = ui.getBg_color();
		if (changeColor) ui.setBg_color(reverseColor);
		ui.paint0(g, w - xOffset, ih);
		ui.setBg_color(oldBgColor);
		g.translate(-xOffset, 0);
	}

	/**
	 * Draws the scrollbar for the given rectangle
	 * 
	 * @param g
	 *            the graphics context (origin should be translated)
	 * @param w
	 *            the width of this panel
	 * @param h
	 *            the height of this panel
	 * @param rh
	 *            the real height of this panel
	 */
	protected void drawScrollBar(UIGraphics g, int w, int h, int rh) {
		drawScrollBarItems(g, w, h, rh, firstVisible, lastVisible, items.size());
	}

	protected void drawScrollBarItems(UIGraphics g, int w, int h, int rh,
			int firstItem, int lastItem, int size) {
		int oc = g.getColor();
		g.setColor(UIConfig.scrollbar_bg);
		g.translate(w - UIConfig.scrollbarWidth, 0);
		g.fillRect(0, 0, UIConfig.scrollbarWidth, h);

		/* calculate y and height of scrollbar */
		int sy = h * firstItem / size;
		int sh = (h * h) / rh;
		if (sy + sh > h || lastItem == size - 1) {
			sy = h - sh;
		}

		g.setColor(UIConfig.scrollbar_fg);
		g.fillRect(1, sy, UIConfig.scrollbarWidth - 2, sh);
		/* resets origin to old value */
		g.setColor(oc);
	}

	public int getHeight(UIGraphics g) {
		/* always all the available space */
		if (maxHeight != -1) return maxHeight;
		if (this.height > 0 && this.isDirty() == false) return this.height;
		// if i have a clip that is my height
		int clipY = g.getClipY();// .getClipY();
		int clippedHeight = g.getClipHeight() + clipY;
		// if (clippedHeight > 0)
		this.height = clippedHeight;
		if (UICanvas.getInstance().hasPointerMotionEvents()) {
			int rh = computeRealHeight(g);
			this.height = Math.max(clippedHeight, rh);
		}
		return this.height;
		// otherwise my last known height
	}

	public void setDirty(boolean _dirty) {
		//              for (int i = 0; i < items.size(); i++) {
		//                      UIItem ui = (UIItem) items.elementAt(i);
		//                      ui.setDirty(_dirty);
		//              }
		Enumeration en = this.items.elements();
		while (en.hasMoreElements()) {
			UIItem ui = (UIItem) en.nextElement();
			ui.setDirty(true);
		}
		dirty = _dirty;
		// so that it will be computed again !
		height = -1;
	}

	public boolean isDirty() {
		Enumeration en = this.items.elements();
		while (en.hasMoreElements()) {
			UIItem ithItem = (UIItem) en.nextElement();
			if (ithItem.isDirty()) return true;
		}
		if (this.dirty) return true;
		return false;
	}

	/**
	 * Calculates the real height of the item
	 */
	protected int computeRealHeight(UIGraphics g) {
		int realHeight = 0;
		/*
		 * saves the old coordinate origin, and calculates the height of each
		 * contained item
		 */
		int otx = g.getTranslateX();
		int oty = g.getTranslateY();
		Enumeration en = this.getItems().elements();
		while (en.hasMoreElements()) {
			Object ithEl = en.nextElement();
			int ih = ((UIItem) ithEl).getHeight(g);
			g.translate(0, ih);
			realHeight += ih;
		}
		/* resets origin to old value */
		g.translate(otx - g.getTranslateX(), oty - g.getTranslateY());
		return realHeight;
	}

	protected void updateChildren() {
		int index = 0;
		for (Enumeration en = this.items.elements(); en.hasMoreElements();) {
			UIItem item = (UIItem) en.nextElement();
			if (this.selectedIdx != index) {
				item.setSelected(false);
			}
			index++;
		}
		((UIItem) this.items.elementAt(selectedIdx)).setSelected(true);
		this.setDirty(true);
		this.askRepaint();
	}

	/**
	 * 
	 */
	public boolean keyPressed(int key) {
		if (this.selectedIdx >= 0
				&& selectedIdx < this.items.size()
				&& ((UIItem) this.items.elementAt(selectedIdx)).keyPressed(key) == true) {
			// this is needed since we cannot know if anything below has been
			// repainted
			updateChildren();
			return true;
		}

		int ga = UICanvas.getInstance().getGameAction(key);
		boolean keepFocus = true;
		int nsi;
		UIItem ui = null;

		if (items.size() == 0) { return false; }

		int tempIndex = 0;
		Enumeration en = null;

		switch (ga) {
			case UICanvas.DOWN:
				if (selectedIdx == -1) {
					/* XXX: nothing selected still: select first visible */
					// selectedIdx = firstVisible - 1;
				}
				keepFocus = false;
				// if none of the following items can be focused
				// the UIPanel looses the focus
				for (int i = selectedIdx + 1; i < items.size()
						&& items.size() > 0; i++) {
					if (((UIItem) this.items.elementAt(i)).isFocusable() == true) {
						keepFocus = true;
						break;
					}
				}

				int lastSelectableIndex = -1;
				tempIndex = 0;
				en = this.items.elements();
				while (en.hasMoreElements()) {
					UIItem ithObject = (UIItem) en.nextElement();
					if (ithObject.isFocusable()) lastSelectableIndex = tempIndex;
					tempIndex++;
				}

				if (selectedIdx >= lastSelectableIndex && selectedIdx >= 0) {
					/*
					 * end of list, won't go further: will lose selection if there's
					 * another item after
					 */
					if (modal == false) {
						keepFocus = false;
						break;
					} else {
						// restart selection from start
						ui = (UIItem) items.elementAt(selectedIdx);
						ui.setSelected(false);
						for (int i = 0; i < firstVisible; i++) {
							((UIItem) items.elementAt(i)).setDirty(true);
						}
						firstVisible = 0;
						selectedIdx = -1;
					}
				}
				dirty = true;
				/* set selection on next item (if exists) */
				nsi = selectedIdx + 1;
				for (; nsi < items.size(); nsi++) {
					ui = (UIItem) items.elementAt(nsi);
					if (ui.isFocusable()) {
						ui.setSelected(true);
						keepFocus = true;
						/* breaks out of loop not from switch */
						break;
					}
				}
				/*
				 * found another selectable item and there was another old selected
				 * item, remove selection from old item
				 */
				if (selectedIdx != -1 && nsi < items.size()) {
					ui = (UIItem) items.elementAt(selectedIdx);
					ui.setSelected(false);
				}
				/* found another selectable item, select it */
				if (nsi < items.size()) {
					selectedIdx = nsi;
				} else if (lastVisible < items.size() - 1) {
					/*
					 * there's still something after but it's not visible, move down
					 * so it can be shown
					 */
					firstVisible++;
					for (int i = firstVisible; i < items.size(); i++) {
						((UIItem) items.elementAt(i)).setDirty(true);
					}
				}

				for (int i = Math.max(selectedIdx, 0); i < items.size(); i++) {
					((UIItem) items.elementAt(i)).setDirty(true);
				}
				break;
			case UICanvas.UP:
				if (selectedIdx == -1) {
					/* XXX: nothing selected, select last visible */
					// selectedIdx = lastVisible + 1;
				}
				keepFocus = false;
				if (selectedIdx == -1 && modal == true) {
					selectedIdx = this.getItems().size();
				}
				// if none of the previous items can be focused
				// the UIPanel looses the focus
				for (int i = 0; i < selectedIdx; i++) {
					if (((UIItem) this.items.elementAt(i)).isFocusable() == true) {
						keepFocus = true;
						break;
					}
				}
				int firstSelectableIndex = -1;
				tempIndex = 0;
				en = this.items.elements();
				while (en.hasMoreElements()) {
					UIItem ithObject = (UIItem) en.nextElement();
					if (ithObject.isFocusable()) {
						firstSelectableIndex = tempIndex;
						break;
					}
					tempIndex++;
				}
				if (selectedIdx >= 0 && selectedIdx == firstSelectableIndex) {
					/* start of list, won't go further */
					// selectedIdx = -1;
					// ui = (UIItem) items.elementAt(0);
					// ui.setSelected(false);
					if (modal == false) {
						keepFocus = false;
						break;
					} else {
						// restart selection from the end
						ui = (UIItem) items.elementAt(selectedIdx);
						ui.setSelected(false);
						selectedIdx = this.items.size();
					}
				}

				dirty = true;
				/* set selection on previous item (if exists) */
				nsi = selectedIdx - 1;
				ui = null;
				for (; nsi >= 0; nsi--) {
					ui = (UIItem) items.elementAt(nsi);
					if (ui.isFocusable()) {
						ui.setSelected(true);
						keepFocus = true;
						/* breaks out of loop not from switch */
						break;
					}
				}
				if (nsi >= 0) {
					/* found another selectable item, remove selection from old item */
					if (selectedIdx < this.items.size()) {
						ui = (UIItem) items.elementAt(selectedIdx);
						ui.setSelected(false);
					}
					selectedIdx = nsi;
				} else if (firstVisible > 0) {
					/* move up anyway */
					firstVisible--;
					for (int i = firstVisible; i < items.size(); i++) {
						((UIItem) items.elementAt(i)).setDirty(true);
					}
				}
				for (int i = Math.max(selectedIdx, 0); i < items.size(); i++) {
					((UIItem) items.elementAt(i)).setDirty(true);
				}

				if (selectedIdx >= this.items.size()) selectedIdx = -1;
				break;
			default:
				break;
		}

		// If I am loosing the focus reset the selection to the first index
		if (keepFocus == false && this.getSelectedIndex() >= 0
				&& this.getSelectedIndex() < this.getItems().size()) {
			//			((UIItem) this.getItems().elementAt(this.getSelectedIndex()))
			//					.setSelected(false);
			//			this.setSelectedIndex(-1);
			dirty = true;
		}

		if (key == UICanvas.MENU_LEFT || key == UICanvas.MENU_RIGHT
				|| ga == UICanvas.LEFT || ga == UICanvas.RIGHT) {
			keepFocus = false;
		}

		if (dirty) {
			askRepaint();
		}

		return keepFocus;
	}

	/**
	 * For now, always focusable
	 */
	public boolean isFocusable() {
		return true;
	}

	/**
	 * Selection status change. When select becomes true, select first item if
	 * no item has been selected. De-select last selected item if Panel gets
	 * de-selected.
	 * 
	 * @param _selected
	 *            {@code true} if panel becomes selected, {@code false} otherwise.
	 */
	public void setSelected(boolean _selected) {
		super.setSelected(_selected);
		if (_selected && selectedIdx == -1 && items.size() > 0) {
			// try to select an Item (normally)
			UIItem firstItem = ((UIItem) items.elementAt(0));
			if (firstItem.isFocusable()) {
				selectedIdx = 0;
				firstItem.setSelected(_selected);
			}
		}
		if (selectedIdx >= 0 && selectedIdx < items.size()) {
			((UIItem) items.elementAt(selectedIdx)).setSelected(_selected);
		}
	}

	/**
	 * Sets the maximum height for this Panel. A max height of -1 (or any
	 * negative value) indicates that the Panel should take up all the available
	 * space.
	 * 
	 * @param mh
	 *            The new max height
	 */
	public void setMaxHeight(int mh) {
		if (mh < -1) {
			mh = -1;
		}
		maxHeight = mh;
	}

	/**
	 * @return The current max height or -1 if this Panel takes up all the
	 *         available space.
	 */
	public int getMaxHeight() {
		return maxHeight;
	}

	/**
	 * Adds an item to the panel
	 * 
	 * @param it
	 *            The item to add
	 */
	public void addItem(UIItem it) {
		items.addElement(it);
		it.setScreen(this.screen);
		it.setContainer(this);
		it.setDirty(true);
		this.dirty = true;
		this.height = -1;
	}

	/**
	 * 
	 * The number of items in the Panel.
	 * 
	 * @return The number of items.
	 */
	public Vector getItems() {
		return this.items;
	}

	public void setItems(Vector v) {
		this.items = v;
	}

	/**
	 * Removes an item from the panel
	 * 
	 * @param it
	 *            The item to remove
	 */
	public int removeItem(UIItem it) {
		this.dirty = true;
		it.setDirty(true);
		int iIndex = indexOf(it);
		this.removeItemAt(iIndex);
		return iIndex;
	}

	/**
	 * Removes the item at the given index from the list.
	 * 
	 * @param idx
	 *            The item index to remove
	 */
	public UIItem removeItemAt(int idx) {
		if (idx < 0 || idx > items.size()) { return null; }
		if (idx < firstVisible) firstVisible--;
		if (idx < lastVisible) fixLastVisible();

		if (selectedIdx > idx) {
			/* clear selection */
			selectedIdx -= 1;
		}
		UIItem ithItem = ((UIItem) items.elementAt(idx));
		ithItem.setSelected(false);
		items.removeElementAt(idx);
		if (this.screen != null) {
			this.screen.removePaintedItem(ithItem);
		}
		if (selectedIdx < 0 || selectedIdx >= this.items.size()) selectedIdx = idx - 1;
		int startDirty = (selectedIdx >= 0 ? Math.min(idx, selectedIdx) : idx);
		for (int i = startDirty; i < items.size(); i++) {
			UIItem ithElem = ((UIItem) items.elementAt(i));
			ithElem.setDirty(true);
		}
		if (idx == selectedIdx) {
			for (int i = selectedIdx; i < items.size(); i++) {
				UIItem ithElem = ((UIItem) items.elementAt(i));
				if (ithElem.isFocusable()) {
					this.setSelectedItem(ithElem);
					break;
				}
			}
		}
		this.dirty = true;
		return ithItem;
	}

	/**
	 * Removes the item at the given index from the list.
	 * 
	 * @param idx
	 *            The item index to remove
	 */
	public void insertItemAt(UIItem it, int idx) {
		if (idx < 0 || idx > items.size()) { return; }
		if (selectedIdx >= idx && selectedIdx < items.size()) {
			/* move selection after */
			((UIItem) this.getItems().elementAt(selectedIdx))
					.setSelected(false);
			if (selectedIdx < items.size()) {
				selectedIdx++;
			}
		}
		items.insertElementAt(it, idx);
		if (selectedIdx >= 0) ((UIItem) this.getItems().elementAt(selectedIdx))
				.setSelected(true);
		for (int i = idx + 1; i < items.size(); i++) {
			((UIItem) items.elementAt(i)).setDirty(true);
		}
		it.setContainer(this);
		it.setScreen(this.screen);
		this.dirty = true;
	}

	/**
	 * Remove all elements
	 */
	public void removeAllItems() {
		if (selectedIdx >= 0 && selectedIdx < this.items.size()) ((UIItem) items
				.elementAt(this.selectedIdx)).setSelected(false);
		items.removeAllElements();
		setDirty(true);
		selectedIdx = -1;
		firstVisible = 0;
	}

	/**
	 * Sets the currently selected item at the position given.
	 * 
	 * @param idx
	 *            The index to select. -1 to clear selection
	 */
	public void setSelectedIndex(int idx) {
		if (this.getContainer() != null) {
			this.getContainer().setSelectedItem(this);
		}
		if (idx < -1 || idx >= items.size()) {
			/* wrong index, ignore */
			return;
		}
		if (selectedIdx != -1) {
			UIItem selItem = ((UIItem) items.elementAt(selectedIdx));
			selItem.setSelected(false);
			selItem.setDirty(true);
		}
		selectedIdx = idx;
		if (selectedIdx != -1) {
			UIItem ithItem = ((UIItem) items.elementAt(idx));
			ithItem.setSelected(true);
			ithItem.setDirty(true);
		}
		if (selectedIdx > lastVisible) {
			// forcing it to be the last visible to avoid useless
			// redraw
			lastVisible = selectedIdx;
		}
	}

	/**
	 * Return the selected UIItem within the UIItem itself; usually it is the
	 * UIItem itself but in the subclasses (like UIVLayout) it could be one of
	 * the contained object.
	 * 
	 * @return
	 */
	public UIItem getSelectedItem() {
		if (this.selectedIdx >= 0
				&& this.items.size() >= (this.selectedIdx + 1)) {
			return ((UIItem) this.items.elementAt(this.selectedIdx))
					.getSelectedItem();
		} else {
			return this;
		}
	}

	public void setSelectedItem(UIItem item) {
		int index = indexOf(item);
		if (index != selectedIdx) this.setSelectedIndex(index);
		if (this.getContainer() != null) {
			this.getContainer().setSelectedItem(this);
		}
	}

	public boolean contains(UIItem item) {
		return UIUtils.contains(items, item);
	}

	/**
	 * @param modal
	 *            the modal to set
	 */
	public void setModal(boolean modal) {
		this.modal = modal;
	}

	/**
	 * @return the modal
	 */
	public boolean isModal() {
		return modal;
	}

	public int getSelectedIndex() {
		return this.selectedIdx;
	}

	public void setFirstVisible(int firstVisible) {
		this.firstVisible = firstVisible;
	}

	public int getFirstVisibleIndex() {
		return firstVisible;
	}

	public int getLastVisibleIndex() {
		return lastVisible;
	}

	public void setListMode(boolean listMode) {
		this.listMode = listMode;
	}

	public boolean isListMode() {
		return listMode;
	}

	public int indexOf(UIItem item) {
		return this.items.indexOf(item);
	}
}
