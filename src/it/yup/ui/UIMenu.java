// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UIMenu.java 2439 2011-01-31 17:32:15Z luca $
 */

/**
 * 
 */
package it.yup.ui;

import it.yup.ui.wrappers.UIFont;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.ui.wrappers.UIImage;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Generico menu da inserire in uno {@link UIScreen}.
 * 
 */
public class UIMenu extends UIItem implements UIIContainer {

	/** The list of items included here. */
	Vector items;
	/** il sottomenu selezionato e aperto */
	private UIMenu openSubMenu;

	private boolean autoClose = true;

	/**
	 * A flag used to know if the scrollbar is need for the menu
	 */
	private boolean needScrollbar = false;

	/** Used to show if the menu is opened */
	boolean openedState = false;

	/**
	 * The selected menu index.
	 */
	int selectedIndex = -1;

	/** A flag to know if the menu has been changed and need repaint */
	boolean changed = false;

	/**
	 * The parent of this menu.
	 */
	private UIMenu parentMenu = null;

	/**
	 * Used to know the absolute origin of the Menu on the screen. We must
	 * invalidate the background when hiding.
	 */
	private int absoluteY = 0;

	/*
	 * The title label if present
	 */
	private UILabel titleLabel;

	/*
	 * The name of the menu
	 */
	String name = "";

	/**
	 * Used to know the absolute origin of the Menu on the screen. We must
	 * invalidate the background when hiding.
	 */
	int absoluteX = 0;
	/**
	 * The index of he first visible item in the screen.
	 */
	protected int firstVisibleIndex = 0;
	/**
	 * The index of he first visible item in the screen.
	 */
	protected int lastVisibleIndex = 0;

	public String cancelMenuString = UIConfig.cancelMenuString;

	public String selectMenuString = UIConfig.selectMenuString;

	int borderSize = 0;

	private int neededHeight = 0;

	// #ifndef RIM
	/*
	 * The offset used for scrolling
	 */
	int paintOffset = 0;

	// #endif

	/**
	 * The images used for submenus
	 */
	static public UIImage menuImage = null;
	static {
		try {
			menuImage = UIImage.createImage("/icons/menuarrow.png");
		} catch (IOException e) {
			// #mdebug
						System.out.println("In allocating menuImage" + e.getMessage());
			// #enddebug
		}
	}

	// #ifndef RIM
	/**
	 * @param paintOffset the paintOffset to set
	 */
	protected void addPaintOffset(int paintOffset) {
		this.paintOffset += paintOffset;
		if (this.paintOffset > 0) this.paintOffset = 0;
		if (neededHeight + this.paintOffset < height) {
			this.paintOffset = height - neededHeight;
		}
	}

	// #endif

	protected int traverseFocusable(int startingIndex, boolean directionDown) {
		if (directionDown) {
			while (startingIndex < this.items.size() - 1
					&& ((UIItem) this.items.elementAt(startingIndex))
							.isFocusable() == false) {
				startingIndex++;
			}
			return startingIndex;
		} else {
			while (startingIndex >= 1
					&& ((UIItem) this.items.elementAt(startingIndex))
							.isFocusable() == false) {
				startingIndex--;
			}
			return startingIndex;
		}

	}

	/**
	 * Costruisce un menu, associandogli un generico "nome"
	 * 
	 * @param name
	 *            il nome del menu
	 */
	public UIMenu(String _name) {

		items = new Vector();
		this.focusable = true;
		this.name = _name;
		if (_name != null && _name.length() > 0
				&& this instanceof UIScreen == false) {
			titleLabel = new UILabel(_name);
			UIFont font_body = UIConfig.font_menu;
			UIFont title_font = UIFont.getFont(font_body.getFace(),
					UIFont.STYLE_BOLD, font_body.getSize());
			titleLabel.setFont(title_font);
			this.append(titleLabel);
			titleLabel.setBg_color(UIConfig.header_bg);
			titleLabel.setFg_color(UIConfig.menu_title);
			titleLabel.setFocusable(false);
			titleLabel.setAnchorPoint(UIGraphics.HCENTER);
		}
		borderSize = 2;
	}

	/**
	 * Returns the index of an item
	 * 
	 * @param item
	 *            The item to find
	 * @return The item index or -1 if this item is not in list.
	 */
	public int indexOf(UIItem item) {
		return items.indexOf(item);
	}

	/**
	 * Aggiunge l'item fornito al fondo della lista.
	 * 
	 * @param ui
	 *            the item to add
	 * @return the position where the Item has been added
	 */
	public int append(UIItem ui) {
		// UIMenu subMenu is not here anymore I add it outside
		setupNewItem(ui);
		items.addElement(ui);
		return items.size() - 1;
	}

	private void setupNewItem(UIItem ui) {
		ui.setFocusable(true);
		ui.setDirty(true);
		ui.setBg_color(UIConfig.menu_color);
		if (ui instanceof UILabel) {
			int hPadding = 2;
			int vPadding = 0;
			UILabel uiLabel = (UILabel) ui;
			uiLabel.setFont(UIConfig.font_menu);
			if (UICanvas.getInstance().hasPointerEvents()) vPadding = UIConfig.menu_padding;
			uiLabel.setPaddings(hPadding, vPadding);
		}
		if (ui.getSubmenu() != null) ui.getSubmenu().setParentMenu(this);
		ui.setScreen(this.screen);
		ui.setContainer(this);
	}

	/**
	 * inserisce l'item alla posizione indicata
	 * 
	 * @param pos
	 *            la posizione in cui inserire l'item
	 * @param ui
	 *            l'item da inserire
	 */
	public void insert(int pos, UIItem ui) {
		if (pos < 0 || pos >= items.size()) { throw new ArrayIndexOutOfBoundsException(
				"Invalid menu pos: " + pos + ", " + items.size()); }
		setupNewItem(ui);
		items.insertElementAt(ui, pos);
	}

	/**
	 * rimuove l'item alla posizione indicata. Tutte le voci vengono "shiftate"
	 * (non vengono lasciati buchi)
	 * 
	 * @param pos
	 *            la posizione a cui rimuovere l'item
	 */
	public UIItem remove(int pos) {
		UIItem ui = null;
		if (pos < items.size() && pos >= 0) {
			try {
				ui = (UIItem) items.elementAt(pos);
				items.removeElementAt(pos);
				this.setDirty(true);
			} catch (Exception e) {
				System.out.println("In removing menu item" + e.getMessage());
				e.printStackTrace();
				// #mdebug
				// #enddebug
			}
		}
		if (this.screen != null) {
			this.screen.removePaintedItem(ui);
		}
		return ui;
	}

	/**
	 * rimuove l'item fornito (scorre tutti gli item e cerca l'item X per cui
	 * vale {@code X.equals(ui)}
	 * 
	 * @param ui
	 *            l'item da rimuovere.
	 */
	public boolean remove(UIItem ui) {
		int idx = items.indexOf(ui);
		if (idx != -1) {
			remove(idx);
			if (this.screen != null) {
				this.screen.removePaintedItem(ui);
			}
			return true;
		}
		return false;
	}

	/**
	 * rimuove tutti gli items
	 */
	public void removeAll() {
		if (selectedIndex >= 0 && selectedIndex < this.items.size()) ((UIItem) this.items
				.elementAt(this.selectedIndex)).setSelected(false);
		if (this.screen != null) {
			Enumeration enItem = items.elements();
			while (enItem.hasMoreElements()) {
				this.screen.removePaintedItem((UIItem) enItem.nextElement());
			}
		}
		items.removeAllElements();
		selectedIndex = -1;
	}

	/**
	 * sostituisce l'item alla posizione indicata con quello nuovo fornito.
	 * L'item deve essere presente (ossia deve valere che la lista abbia la
	 * posizione {@code pos} valida.
	 * 
	 * @param pos
	 *            la posizione dell'item
	 * @param ui
	 *            l'item da sostituire
	 */
	public UIItem replace(int pos, UIItem ui) {
		if (pos >= this.items.size()) throw new ArrayIndexOutOfBoundsException(
				"Invalid itemList pos: " + pos + ", " + items.size());

		int oldSelectedIndex = this.getSelectedIndex();
		UIItem posth = remove(pos);
		insert(pos, ui);
		this.setSelectedIndex(oldSelectedIndex);
		return posth;
	}

	public void replace(UIItem oldItem, UIItem newItem) {
		int pos = this.indexOf(oldItem);
		if (pos >= 0) replace(pos, newItem);
	}

	/**
	 * Removes all items
	 */
	public void clear() {
		items.setSize(0);
	}

	/**
	 * Esegue il paint del menu
	 * 
	 * @param g
	 *            il contesto grafico da usare
	 * @param w
	 *            la larghezza massima da riempire
	 * @param h
	 *            l'altezza massima da riempire
	 */
	protected void paint(UIGraphics g, int w, int h) {
		this.width = w;
		if (this instanceof UIScreen) this.height = 0;
		else {
			this.height = 1;
		}
		neededHeight = 0;
		int ox = g.getTranslateX();
		int oy = g.getTranslateY();
		g.translate(0, 1);
		for (Enumeration en = items.elements(); en.hasMoreElements();) {
			UIItem ithItem = (UIItem) en.nextElement();
			int uithHeight = ithItem.getHeight(g);
			// if uithHeight <0 use a default height to compute scrollbar
			// stuff
			if (uithHeight < 0) uithHeight = g.getFont().getSize();
			// many UIItem use clipping and position to dinamically compute
			// height !!!!
			g.translate(0, uithHeight);
			this.height += uithHeight;
		}
		this.height += 2 * borderSize;
		g.translate(0, oy - g.getTranslateY());
		neededHeight = this.height;
		if (neededHeight == 0) neededHeight = 1;

		// now that we have "height" and "h" that is our available height
		// we must check how the menu can be resized or moved in the screen
		// so that it assumes a nice visible aspect
		boolean oldNeedScrollbar = needScrollbar;
		needScrollbar = false;

		h = resizeHeight(g, h);

		checkScrollbar(oldNeedScrollbar);

		int initialX = g.getTranslateX();
		int initialY = g.getTranslateY();
		// notify the screen my coordinates 
		coors[1] = initialY;
		coors[3] = this.height;
		int originalClipX = g.getClipX();
		int originalClipY = g.getClipY();
		int originalClipHeight = g.getClipHeight();
		int originalClipWidth = g.getClipWidth();
		computeClip(g, width, height);
		// moving down or up the screen can cause the lastVisible or
		// firstVisible
		// items not to be fully visible. Recheck !!!
		boolean paintNeeded = true;
		while (paintNeeded == true) {
			paintNeeded = false;
			setTranslation(g, initialX, initialY);

			int scrollbarHeight = 0;
			int scrollbarPosition = 0;

			if (needScrollbar) {
				scrollbarHeight = getScrollbarHeight(h, neededHeight);
				scrollbarPosition = getScrollbarPosition(g, h, scrollbarHeight);
			}

			int lastHeight = borderSize;
			// if a control is dirty all the subsequent must be set as dirty since the height
			// can be changed
			boolean lastDirty = false;
			for (int i = this.firstVisibleIndex; i < this.items.size(); i++) {
				UIItem uith = (UIItem) this.items.elementAt(i);

				if (lastDirty == true) {
					uith.setDirty(true);
				}
				if (uith.isDirty()) {
					lastDirty = true;
					// remove the space needed for border and scrollabar
					int reservedWidth = w - 2 * borderSize;
					// the screen need an additional pixel for border
					//					if (this instanceof UIScreen)
					reservedWidth -= 1;
					if (needScrollbar == true) reservedWidth -= UIConfig.scrollbarWidth;
					int uithHeight = uith.getHeight(g);

					// I must invalidate all the UIMenus different from myself
					// that are over uith
					if (this.screen != null && this.screen != this) {
						int[] coors = new int[] { g.getTranslateX(),
								g.getTranslateY(),
								g.getTranslateX() + reservedWidth,
								g.getTranslateY() + uithHeight };
						this.screen.invalidatePopups(this, coors);
					}

					if (!UICanvas.getInstance().hasPointerMotionEvents()
							|| isOnscreen(g, uith, initialY, uithHeight)) {
						if (this.getBgImage() != null
								&& uith.getBg_color() == UIItem.TRANSPARENT_COLOR) {
							// if I paint a bg i need to invalidate all the item!!!
							paintBGRegion(g, getBgImage(), 0, lastHeight,
									reservedWidth, uithHeight, 0, 0);
							uith.setDirty(true);
						}
						uith.paint0(g, reservedWidth, uithHeight);
					}

					// if I am not a Screen I must show arrows to show there
					// are submenus
					UIMenu selectedSubmenu = null;
					if (uith.getSelectedItem() != null) selectedSubmenu = uith
							.getSelectedItem().getSubmenu();
					if (this instanceof UIScreen == false
							&& selectedSubmenu != null) g.drawImage(menuImage,
							reservedWidth + 1 - UIMenu.menuImage.getWidth(),
							(uithHeight - UIMenu.menuImage.getHeight()) / 2,
							UIGraphics.TOP | UIGraphics.LEFT);
					changed = true;
				}
				int itemHeight = uith.getHeight(g);
				g.translate(0, itemHeight);
				lastHeight += itemHeight;
				this.lastVisibleIndex = i;

				// only the visible UIItem
				if (heightReached(h, lastHeight)) {
					/* last item is only "partially visible" */
					this.lastVisibleIndex--;
					lastHeight -= itemHeight;
					g.translate(0, -itemHeight);
					break;
				}

				if (lastVisibleIndex == this.items.size() - 1) {
					g.setColor(getBg_color() >= 0 ? getBg_color()
							: UIConfig.menu_color);
					int yGapHeight = this.height - lastHeight - 1;
					if (getBgImage() != null) {
						int bgWidth = getBgImage().getWidth();
						if (needScrollbar) bgWidth -= UIConfig.scrollbarWidth;
						if (yGapHeight > 0) {
							paintBGRegion(g, getBgImage(), g.getTranslateX()
									- ox, g.getTranslateY() - oy, bgWidth,
									yGapHeight, 0, 0);
						}
					} else {
						if (this.needScrollbar == false) {
							g.fillRect(0, 0, w - 1, yGapHeight - borderSize);
						} else {
							g.fillRect(0, 0, w - UIConfig.scrollbarWidth - 1,
									yGapHeight - borderSize);
						}
					}
				}

				if (lastItemReached(h, lastHeight)) {
					/* last item is perfectly visible */
					break;
				}
			}
			resetTranslation(g);
			// draw the scrollbar
			if (needScrollbar == true) {
				drawScrollbar(g, w, h, initialX, initialY, scrollbarHeight,
						scrollbarPosition);
			}

			g.translate(initialX - g.getTranslateX(), initialY
					- g.getTranslateY());
			// the border is painted later if I am a screen
			if (this instanceof UIScreen == false) {
				int currentbbColor = UIConfig.menu_border;
				int innerUp = 0;
				int innerDown = 0;
				int border[][] = null;
				if (UIConfig.menu_3d == true) {
					innerUp = UIUtils.colorize(currentbbColor, 50);
					innerDown = UIUtils.colorize(currentbbColor, -50);
					border = new int[][] {
							new int[] { currentbbColor, currentbbColor, -1 },
							new int[] { currentbbColor, innerUp, -1 },
							new int[] { -1, -1, -1 }, };
				} else {
					innerUp = UIConfig.menu_border;
					innerDown = UIConfig.menu_border;
					int innerColor = UIUtils.medColor(UIConfig.menu_color,
							currentbbColor);
					int diagColor = UIUtils.medColor(UIConfig.bg_color,
							currentbbColor);
					border = new int[][] { new int[] { -1, diagColor, -1 },
							new int[] { diagColor, innerDown, -1 },
							new int[] { -1, -1, innerColor }, };
				}
				int colors[] = new int[] { currentbbColor, innerUp, innerDown,
						currentbbColor };
				drawBorder(g, new int[] { 1, 1, width - 1, height - 1 },
						colors, border);
				if (UIConfig.menu_3d == true) {
					g.setColor(innerDown);
					g.drawPixel(width - 2, 2);
					g.drawPixel(2, height - 2);
					g.drawPixel(width - 2, height - 2);
				}
			}

			// moving down or up the screen can cause the lastVisible or
			// firstVisible
			// items not to be fully visible. Recheck !!! :D
			// be careful that if the UIItem is bigger than the screen
			// the lastvisibleindex can be smaller than firstvisibleindex
			if (this.selectedIndex > this.lastVisibleIndex
					&& this.selectedIndex > this.firstVisibleIndex) {
				paintNeeded = true;
				int gapHeight = 0;
				for (int i = this.lastVisibleIndex + 1; i <= selectedIndex; i++)
					gapHeight += ((UIItem) this.items.elementAt(i))
							.getHeight(g);
				gapHeight -= h;
				for (int i = this.firstVisibleIndex; i <= lastVisibleIndex; i++)
					gapHeight += ((UIItem) this.items.elementAt(i))
							.getHeight(g);

				do {
					UIItem ithElem = ((UIItem) this.items
							.elementAt(firstVisibleIndex));
					gapHeight -= ithElem.getHeight(g);
					this.firstVisibleIndex++;
				} while (gapHeight > 0);

				/* set items dirty */
				this.setDirty(true);
			}
			if (this.selectedIndex >= 0
					&& this.selectedIndex < this.firstVisibleIndex) {
				paintNeeded = true;
				firstVisibleIndex = selectedIndex;
				/* set items dirty */
				for (int i = firstVisibleIndex; i < this.items.size(); i++) {
					((UIItem) this.items.elementAt(i)).setDirty(true);
				}
				this.setDirty(true);
			}
		}
		g.setClip(originalClipX, originalClipY, originalClipWidth,
				originalClipHeight);
	}

	/**
	 * @param oldNeedScrollbar
	 */
	protected void checkScrollbar(boolean oldNeedScrollbar) {
		if (needScrollbar == false && oldNeedScrollbar) {
			// #ifndef RIM
			paintOffset = 0;
			// #endif
			Enumeration en = this.items.elements();
			while (en.hasMoreElements()) {
				UIItem ithItem = (UIItem) en.nextElement();
				ithItem.setDirty(true);
			}
		}
	}

	private boolean isOnscreen(UIGraphics g, UIItem ui, int oty, int ih) {
		int clipY = g.getClipY();
		int clipHeight = g.getClipHeight();
		if (clipY + clipHeight > 0 && ih > clipY) return true;
		else
			return false;
	}

	/**
	 * @param g
	 * @param h
	 * @return
	 */
	protected int getScrollbarPosition(UIGraphics g, int h, int scrollbarHeight) {
		int scrollbarPosition = 0;
		for (int i = 0; i < this.firstVisibleIndex; i++) {
			UIItem uith = (UIItem) this.items.elementAt(i);
			scrollbarPosition += uith.getHeight(g);
		}
		scrollbarPosition *= h;
		scrollbarPosition /= neededHeight;
		// #ifndef RIM
		if (UICanvas.getInstance().hasPointerMotionEvents()) {
			if (this.paintOffset == height - neededHeight) {
				scrollbarPosition = (height - scrollbarHeight);
			} else {
				scrollbarPosition = (-paintOffset * height) / neededHeight;
			}
			return scrollbarPosition;
		}
		// #endif
		if (this.lastVisibleIndex == this.items.size() - 1) {
			scrollbarPosition = h - scrollbarHeight;
		}
		return scrollbarPosition;
	}

	protected void computeClip(UIGraphics g, int width, int height) {
		g.setClip(0, 1, width, height);
	}

	/**
	 * @param h
	 * @param lastHeight
	 * @return
	 */
	protected boolean lastItemReached(int h, int lastHeight) {
		boolean res = lastHeight == h;
		// #ifndef RIM
		if (UICanvas.getInstance().hasPointerMotionEvents()) res = false;
		// #endif
		return res;
	}

	/**
	 * @param h
	 * @param lastHeight
	 * @return
	 */
	protected boolean heightReached(int h, int lastHeight) {
		boolean hr = lastHeight > h - borderSize;
		// #ifndef RIM
		if (UICanvas.getInstance().hasPointerMotionEvents()) hr = false;
		// #endif
		return hr;
	}

	/**
	 * @param g
	 * @param w
	 * @param h
	 * @param initialX
	 * @param initialY
	 * @param scrollbarHeight
	 * @param scrollbarPosition
	 */
	protected void drawScrollbar(UIGraphics g, int w, int h, int initialX,
			int initialY, int scrollbarHeight, int scrollbarPosition) {
		g.translate(w - UIConfig.scrollbarWidth - borderSize + initialX
				- g.getTranslateX(), initialY - g.getTranslateY() + 1);
		g.setColor(UIConfig.scrollbar_bg);
		g.fillRect(0, 0, UIConfig.scrollbarWidth, height - 1);
		g.setColor(UIConfig.scrollbar_fg);
		g.translate(0, scrollbarPosition);
		g.fillRect(1, 0, UIConfig.scrollbarWidth - 2, scrollbarHeight);
	}

	/**
	 * @param h
	 * @param neededHeight
	 * @return
	 */
	protected int getScrollbarHeight(int h, int neededHeight) {
		int scrollbarHeight = (h * h) / neededHeight;
		// to avoid a too big scrollbar;
		if (scrollbarHeight > (2 * h) / 3) {
			scrollbarHeight = (2 * h) / 3;
		}
		return scrollbarHeight;
	}

	/**
	 * @param g
	 * @param cr
	 * @return
	 */
	protected int resizeHeight(UIGraphics g, int cr) {
		// is it too big and it doesn't fit ?
		if (this.height > cr) {
			// can we move it ?
			// use an offset from up
			int Y = this.absoluteY;
			int clipY = g.getClipY();
			int translateY = g.getTranslateY();
			if ((Y - clipY - translateY) > (this.height - cr)) {
				this.absoluteY -= (this.height - cr);
				// System.out.println("Translate 1: 0, " + (h - this.height));
				g.translate(0, (cr - this.height));
				cr = this.height;
			} else {
				int offset = 0;
				// an offset is used for popup to avoid their
				// touching the upper border
				offset = g.getClipHeight() / 7;
				this.absoluteY = translateY + clipY + offset;
				this.height = g.getClipHeight() - offset;
				g.translate(0, this.absoluteY - g.getTranslateY());
				needScrollbar = true;
				// recompute height only if not a screen:
				// screens have fixed size
				cr = this.height;
			}
		}
		return cr;
	}

	protected void resetTranslation(UIGraphics g) {
		// #ifndef RIM
		if (UICanvas.getInstance().hasPointerMotionEvents()) {
			g.translate(0, -paintOffset);
		}
		// #endif
	}

	/**
	 * @param g
	 * @param initialX
	 * @param initialY
	 */
	protected void setTranslation(UIGraphics g, int initialX, int initialY) {
		g.translate(1 - g.getTranslateX() + initialX, 1 - g.getTranslateY()
				+ initialY);
		g.translate(borderSize, borderSize);
		// #ifndef RIM
		if (UICanvas.getInstance().hasPointerMotionEvents()) {
			g.translate(0, paintOffset);
		}
		// #endif
	}

	/**
	 * /** Handles the key events. Only these events are handled: DOWN, UP,
	 * BACK/CANCEL, SELECT
	 * 
	 * @param key
	 *            the key pressed
	 * @param ga
	 * @return The selected UIMenuItem
	 */
	protected UIItem keyPressed(int key, int ga) {
		//UIItem v = null;
		if (openSubMenu != null) {
			/* v = */openSubMenu.keyPressed(key, ga);
			return null;
		}
		if (key != UICanvas.MENU_RIGHT
				&& ga != UICanvas.FIRE
				&& this.selectedIndex >= 0
				&& selectedIndex < this.items.size()
				&& ((UIItem) this.items.elementAt(selectedIndex))
						.keyPressed(key) == true) { return null; }
		UIItem selItem = null;
		switch (ga) {
			case UICanvas.UP:
				if (selectedIndex >= 0 && selectedIndex < this.items.size()) ((UIItem) this.items
						.elementAt(selectedIndex)).setSelected(false);
				if (selectedIndex == 0) {
					selectedIndex = this.items.size() - 1;
				} else if (selectedIndex == -1) selectedIndex = 0;
				else {
					if (selectedIndex > 0
					//							&& ((UIItem) this.itemList
					//									.elementAt(selectedIndex - 1))
					//									.isFocusable()
					) {
						selectedIndex--;
					}
				}
				if (selectedIndex >= 0 && selectedIndex < this.items.size()) {
					selItem = ((UIItem) this.items.elementAt(selectedIndex));
					selItem.setSelected(true);
					if (selItem.isFocusable() == false) return keyPressed(key,
							ga);
				}
				this.dirty = true;
				this.askRepaint();
				break;

			case UICanvas.DOWN:
				if (selectedIndex >= 0 && selectedIndex < this.items.size()) ((UIItem) this.items
						.elementAt(selectedIndex)).setSelected(false);
				if (selectedIndex == this.items.size() - 1) {
					firstVisibleIndex = 0;
					selectedIndex = 0;
					this.setDirty(true);
				} else if (selectedIndex == -1) selectedIndex = 0;
				else
					selectedIndex++;
				if (selectedIndex >= 0 && selectedIndex < this.items.size()) {
					selItem = ((UIItem) this.items.elementAt(selectedIndex));
					selItem.setSelected(true);
					if (selItem.isFocusable() == false) return keyPressed(key,
							ga);
				}
				this.dirty = true;
				this.askRepaint();
				break;

			default:
				break;
		}

		if ((key == UICanvas.MENU_RIGHT || ga == UICanvas.FIRE)
				&& selectedIndex >= 0 && this.items.size() > 0) {
			UIItem selectedItem = ((UIItem) this.items.elementAt(selectedIndex))
					.getSelectedItem();
			if (selectedItem.keyPressed(key) == true) { return null; }
			if (selectedItem.getSubmenu() != null) {
				this.openSubMenu = selectedItem.getSubmenu();

				// set submenu positions and dimension
				if (openSubMenu.getWidth() <= 0) openSubMenu
						.setWidth(this.width - 10);
				openSubMenu.absoluteX = this.absoluteX;
				openSubMenu.absoluteY = this.absoluteY;
				int remainingXSpace = UICanvas.getInstance().getWidth()
						- this.width - this.absoluteX;
				int remainingYSpace = UICanvas.getInstance().getClipHeight()
						- this.height - this.absoluteY;
				if (openSubMenu.absoluteX > remainingXSpace) openSubMenu.absoluteX -= 5;
				else
					openSubMenu.absoluteX += 15;
				if (openSubMenu.absoluteY > remainingYSpace) openSubMenu.absoluteY -= 5;
				else
					openSubMenu.absoluteY += 15;
				this.screen.addPopup(selectedItem.getSubmenu());
			}
			return selectedItem;
		}
		return null;
	}

	public boolean isOpenedState() {
		return openedState;
	}

	public void setOpenedState(boolean openedState) {
		this.openedState = openedState;
		if (openedState == false) {
			if (selectedIndex >= 0 && this.items.size() > 0
					&& selectedIndex < this.items.size()) {
				UIItem uimi = (UIItem) this.items.elementAt(selectedIndex);
				uimi.setSelected(false);
			}
			this.selectedIndex = -1;
			if (this.screen != null) {
				Enumeration en = this.items.elements();
				while (en.hasMoreElements()) {
					this.screen.removePaintedItem((UIItem) en.nextElement());
				}
				this.screen.removePaintedItem(this);
			}
			this.setDirty(true);
		}
	}

	public int getAbsoluteY() {
		return absoluteY;
	}

	public void setAbsoluteY(int absoluteY) {
		this.absoluteY = absoluteY;
	}

	public int getAbsoluteX() {
		return absoluteX;
	}

	public void setAbsoluteX(int absoluteX) {
		this.absoluteX = absoluteX;
	}

	/**
	 * @param parentMenu
	 *            the parentMenu to set
	 */
	public void setParentMenu(UIMenu parentMenu) {
		this.parentMenu = parentMenu;
	}

	/**
	 * @return the parentMenu
	 */
	public UIMenu getParentMenu() {
		return parentMenu;
	}

	public UIMenu getSubmenu() {
		return openSubMenu;
	}

	public void setSubmenu(UIMenu submenu) {
		this.openSubMenu = submenu;
	}

	/**
	 * @return The height of the item
	 * @param g
	 *            the {@link Graphics} on which to paint into
	 */
	public int getHeight(UIGraphics g) {
		this.height = 1;
		for (Enumeration en = items.elements(); en.hasMoreElements();) {
			UIItem ithItem = (UIItem) en.nextElement();
			this.height += ithItem.getHeight(g);
		}
		//for borders
		height += (2 * borderSize);
		return height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setScreen(UIScreen _us) {
		screen = _us;
		for (Enumeration en = items.elements(); en.hasMoreElements();) {
			UIItem ithItem = (UIItem) en.nextElement();
			ithItem.setScreen(screen);
		}
		if (openSubMenu != null) {
			openSubMenu.setScreen(screen);
		}
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
		for (Enumeration en = items.elements(); en.hasMoreElements();) {
			UIItem ithItem = (UIItem) en.nextElement();
			ithItem.setDirty(dirty);
		}
	}

	/**
	 * Set the {@link UIScreen} to be repainted.
	 * 
	 * @param dirty
	 *            The new value for dirty.
	 */
	public final boolean isDirty() {
		// this.height = -1;
		// this.width = -1;
		for (Enumeration en = items.elements(); en.hasMoreElements();) {
			UIItem ithItem = (UIItem) en.nextElement();
			if (ithItem.isDirty()) return true;
		}
		if (this.dirty) return true;
		return false;
	}

	/**
	 * @param selectedIndex
	 *            the selectedIndex to set
	 */
	public void setSelectedIndex(int selectedIndex) {
		if (selectedIndex == this.selectedIndex) return;
		if (this.selectedIndex >= 0 && this.selectedIndex < this.items.size()) {
			((UIItem) this.items.elementAt(this.selectedIndex))
					.setSelected(false);
		}
		if (selectedIndex >= 0 && selectedIndex < this.items.size()) {
			((UIItem) this.items.elementAt(selectedIndex)).setSelected(true);
		}
		this.selectedIndex = selectedIndex;
	}

	/**
	 * @return the selectedIndex
	 */
	public int getSelectedIndex() {
		return selectedIndex;
	}

	public UIItem getSelectedItem() {
		if (selectedIndex >= 0 && selectedIndex < this.items.size()) return (UIItem) this.items
				.elementAt(selectedIndex);
		return this;
	}

	/**
	 * @param itemList
	 *            the itemList to set
	 */
	public void setItemList(Vector itemList) {
		this.items = itemList;
	}

	/**
	 * @return the itemList
	 */
	public Vector getItems() {
		return items;
	}

	public void setSelectedItem(UIItem item) {
		int index = this.indexOf(item);
		this.setSelectedIndex(index);
		if (this.getContainer() != null) {
			this.getContainer().setSelectedItem(this);
		}
	}

	public boolean contains(UIItem item) {
		return UIUtils.contains(items, item);
	}

	public void setAutoClose(boolean autoClose) {
		this.autoClose = autoClose;
	}

	public boolean isAutoClose() {
		return autoClose;
	}

	/**
	 * @return the titleLabel
	 */
	public UILabel getTitleLabel() {
		return titleLabel;
	}

	/**
	 * @param needScrollbar the needScrollbar to set
	 */
	public void setNeedScrollbar(boolean needScrollbar) {
		this.needScrollbar = needScrollbar;
	}

	/**
	 * @return the needScrollbar
	 */
	public boolean isNeedScrollbar() {
		return needScrollbar;
	}
}
