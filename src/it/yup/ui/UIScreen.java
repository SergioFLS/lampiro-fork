// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UIScreen.java 2432 2011-01-30 15:26:48Z luca $
 */

package it.yup.ui;

import it.yup.ui.wrappers.UIFont;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.ui.wrappers.UIImage;

import java.util.Enumeration;
import java.util.Vector;


/**
 * Fornisce uno schermo che mostra una lista di items generici e espone
 * funzionalita' di menu. Ogni item puo' avere una azione associata alla
 * pressione del tasto di selezione.<br>
 * 
 * Il Menu viene mostrato alla pressione del tasto del menu (tipicamente quello
 * destro), E' una lista di voci che viene mostrata in un overlay che non copre
 * tutto lo schermo (la dimensione dipende dalla dimensione dello schermo e
 * dalla dimensione del font; si pua' aggiungere che se la scritta sfora la
 * larghezza del menu e la voce e' selezionata ogni X (=1 o 2) secondi il menu
 * viene ridipinto.<br>
 * 
 */
public class UIScreen extends UIMenu implements UIIContainer {


	/** il menu di questo screen */
	private UIMenu menu;

	private boolean freezed = false;

	/*
	 * UIScreen needs a fresh repaint (meaning even the background must be completly
	 * repainted) when changing from one screen to another.
	 */
	boolean firstPaint = false;

	/**
	 * The {@link UICanvas} owning the screen.
	 */
	private UICanvas canvas = null;

	/**
	 * The list of the popup for this screen.
	 */
	protected Vector popupList;

	/**
	 * The screen title
	 */
	public UILabel titleLabel = new UILabel("");

	public UIHLayout headerLayout = new UIHLayout(3);

	public UISeparator headerSep = null;

	/**
	 * The footer elements
	 */
	// #ifndef RIM
	protected UIHLayout footer = new UIHLayout(2);
	public UILabel footerLeft = new UILabel("");
	public UILabel footerRight = new UILabel("");
	// #endif

	private UIScreen returnScreen = null;

	/**
	 * The graphics in which the screen is painted
	 * 
	 */
	private UIGraphics graphics;

	private Vector paintedItems = new Vector(10);

	/*
	 * Used for canvas to know if the screen can loose focus in favour of others
	 */
	private boolean rollEnabled = true;

	/** Called to notify that the {@link UIScreen} has become visible */
	public void showNotify() {

	}

	/** Called to notify that the {@link UIScreen} has become invisible */
	public void hideNotify() {

	}

	/**
	 * Constructor, each screen is associated to the Canvas
	 */
	public UIScreen() {
		super("Screen");
		// #ifndef RIM
		this.footerLeft.setFocusable(true);
		this.footerRight.setFocusable(true);
		// #endif
		this.titleLabel.setFocusable(true);
		borderSize = 0;
		UICanvas canvasInstance = UICanvas.getInstance();
		UIImage lag = canvasInstance.lag;
		UIImage rag = canvasInstance.lag;
		int imgHeight = canvasInstance.lag.getImage().getHeight();
		UIVLayout innerLayout = new UIVLayout(2, imgHeight);
		headerSep = new UISeparator(1);
		headerSep.setFg_color(UIConfig.header_bg);
		innerLayout.insert(headerSep, 0, 1, UILayout.CONSTRAINT_PIXELS);
		innerLayout
				.insert(titleLabel, 1, imgHeight, UILayout.CONSTRAINT_PIXELS);
		headerLayout.insert(new UILabel(lag), 0, lag.getWidth(),
				UILayout.CONSTRAINT_PIXELS);
		headerLayout
				.insert(innerLayout, 1, 100, UILayout.CONSTRAINT_PERCENTUAL);
		headerLayout.insert(new UILabel(rag), 2, rag.getWidth(),
				UILayout.CONSTRAINT_PIXELS);
		headerLayout.setBg_color(UIConfig.header_bg);
		titleLabel.setBg_color(UIConfig.header_bg);
		titleLabel.setFg_color(UIConfig.menu_title);
		titleLabel.setFont(UIConfig.font_title);
		titleLabel.setAnchorPoint(UIGraphics.HCENTER);
		headerLayout.setScreen(this);

		this.setItemList(new Vector());
		this.popupList = new Vector();
		this.canvas = canvasInstance;
		selectedIndex = -1;
		this.width = canvasInstance.getWidth();
		this.height = canvasInstance.getClipHeight();

		// #ifndef RIM
		footer.setGroup(false);
		footer.setFocusable(false);
		if (UICanvas.getInstance().hasPointerMotionEvents()) {
			footerLeft.setPaddings(0, 5);
			footerRight.setPaddings(0, 5);
		}
		footer.insert(footerLeft, 0, 50, UILayout.CONSTRAINT_PERCENTUAL);
		footer.insert(footerRight, 1, 50, UILayout.CONSTRAINT_PERCENTUAL);
		this.footerLeft.setScreen(this);
		this.footerRight.setScreen(this);
		footerLeft.setBg_color(UIConfig.header_bg);
		footerLeft.setFg_color(UIConfig.menu_title);
		footerLeft.setFont(UIConfig.font_title);
		footerLeft.setAnchorPoint(UIGraphics.LEFT);
		footerRight.setBg_color(UIConfig.header_bg);
		footerRight.setFg_color(UIConfig.menu_title);
		footerRight.setFont(UIConfig.font_title);
		footerRight.setAnchorPoint(UIGraphics.RIGHT);
		// #endif
		this.screen = this;

	}

	/**
	 * Adds the passed item to the bottom of the itemList.
	 * 
	 * @param ui
	 *            The item to add.
	 * @return The position which the item has been added in.
	 */
	public int append(UIItem ui) {
		getItems().addElement(ui);
		ui.setScreen(this);
		ui.setContainer(this);
		ui.setDirty(true);
		this.askRepaint();
		return getItems().size() - 1;
	}

	/**
	 * Adds a popUp to the popup vector.
	 * 
	 * @param popUp
	 *            The item to add.
	 */
	public void addPopup(UIMenu popUp) {
		this.popupList.addElement(popUp);
		popUp.setDirty(true);
		popUp.setScreen(this);
		this.firstPaint = true;
		this.askRepaint();
	}

	/**
	 * Remove a popUp from the popup vector.
	 * 
	 * @param popUp
	 *            The item to remove.
	 */
	public void removePopup(UIMenu popUp) {
		if (popUp != null && this.popupList.contains(popUp)) {
			this.firstPaint = true;
			popUp.setOpenedState(false);
			popUp.setSubmenu(null);
			this.popupList.removeElement(popUp);
			int[] coors = new int[] { popUp.getAbsoluteX(),
					popUp.getAbsoluteY(),
					popUp.getAbsoluteX() + popUp.getWidth(),
					popUp.getAbsoluteY() + popUp.getHeight(this.graphics) };
			this.invalidateArea(coors);
		}
		this.askRepaint();
	}

	public void removeAllPopups() {
		for (Enumeration en = popupList.elements(); en.hasMoreElements();) {
			UIMenu uim = (UIMenu) en.nextElement();
			uim.setOpenedState(false);
			uim.setSubmenu(null);
			int[] coors = new int[] { uim.getAbsoluteX(), uim.getAbsoluteY(),
					uim.getAbsoluteX() + uim.getWidth(),
					uim.getAbsoluteY() + uim.getHeight(this.graphics) };
			this.invalidateArea(coors);
		}
		this.popupList.removeAllElements();
		this.askRepaint();
	}

	public boolean popupIsPresent(UIMenu popUp) {
		return this.popupList.contains(popUp);
	}

	/**
	 * Adds the passed item at the pos-th position of the itemList.
	 * 
	 * @param pos
	 *            The position which the item has to be added in.
	 * @param ui
	 *            The item to add.
	 */
	public void insert(int pos, UIItem ui) {
		if (pos < 0 || pos > getItems().size()) { throw new ArrayIndexOutOfBoundsException(
				"Invalid menu pos: " + getItems() + ", " + getItems().size()); }
		int oldSelectedIndex = this.getSelectedIndex();
		getItems().insertElementAt(ui, pos);
		if (pos <= oldSelectedIndex) this.setSelectedIndex(++oldSelectedIndex);
		ui.setScreen(this);
		ui.setContainer(this);
		ui.setDirty(true);
		for (int i = pos; i < getItems().size(); i++) {
			((UIItem) getItems().elementAt(i)).setDirty(true);
		}
		this.askRepaint();
	}

	/**
	 * Removes the item at the pos-th position of the itemList.
	 * 
	 * @param pos
	 *            The position from which the item has to be removed.
	 * @return returns the removed Item.
	 */
	public UIItem remove(int pos) {
		UIItem ui = super.remove(pos);
		for (int i = pos; i < getItems().size(); i++) {
			((UIItem) getItems().elementAt(i)).setDirty(true);
		}
		if (selectedIndex >= pos) selectedIndex--;
		if (firstVisibleIndex > this.getItems().size() - 1) firstVisibleIndex = 0;
		if (lastVisibleIndex > this.getItems().size() - 1) lastVisibleIndex = this
				.getItems().size() - 1;
		this.askRepaint();
		return ui;
	}

	/**
	 * Removes the passed item from the itemList.
	 * 
	 * @param ui
	 *            The item to remove.
	 */
	public boolean remove(UIItem ui) {
		int tempIndex = this.indexOf(ui);
		if (tempIndex >= 0) {
			this.remove(tempIndex);
			return true;
		}
		return false;
	}

	/**
	 * Removes all the items.
	 */
	public void removeAll() {
		super.removeAll();
		firstVisibleIndex = 0;
		lastVisibleIndex = 0;
		this.askRepaint();
	}

	/**
	 * Replace the item at the passed inedx with the newly furnished one. If {@code pos} is not a valid value for the itemList an {@link ArrayIndexOutOfBoundsException} is thrown.
	 * 
	 * @param pos
	 *            The item position
	 * @param ui
	 *            The {@link UIItem} to replace
	 * @return the removed item
	 */
	public UIItem replace(int pos, UIItem ui) {
		boolean oldFreezed = this.isFreezed();
		this.setFreezed(true);
		UIItem posth = super.replace(pos, ui);
		this.setFreezed(oldFreezed);
		ui.setScreen(this);
		ui.setContainer(this);
		this.askRepaint();
		return posth;
	}

	public void replace(UIItem oldItem, UIItem newItem) {
		int oldPos = this.indexOf(oldItem);
		this.replace(oldPos, newItem);
	}

	/**
	 * Swaps two object in the list
	 * 
	 * @param firstIndex
	 * @param secondIndex
	 * @return
	 */
	public void swap(int firstIndex, int secondIndex) {
		if (firstIndex >= this.getItems().size()
				|| secondIndex >= this.getItems().size()) throw new ArrayIndexOutOfBoundsException(
				"Invalid itemList pos: " + firstIndex + " " + secondIndex
						+ ", " + getItems().size());

		Object temp = getItems().elementAt(firstIndex);
		getItems().setElementAt(getItems().elementAt(secondIndex), firstIndex);
		getItems().setElementAt(temp, secondIndex);
		UIItem dirtyItem = (UIItem) getItems().elementAt(firstIndex);
		dirtyItem.setDirty(true);
		UIItem dirtyItem2 = (UIItem) getItems().elementAt(secondIndex);
		dirtyItem2.setDirty(true);
		this.askRepaint();
	}

	/**
	 * Set this screen's menu. If the menu is {@code null} the menu is disabled.
	 * 
	 * @param _menu
	 *            the screen's menu
	 */
	public void setMenu(UIMenu _menu) {
		menu = _menu;
		if (menu != null) menu.setScreen(this);
	}

	/**
	 * @return The screen menu.
	 */
	public UIMenu getMenu() {
		return menu;
	}

	/*
	 * Raised when a drag is made
	 */
	public void startDrag(UIItem draggedItem) {

	}

	/*
	 * Raised when a drag is made
	 */
	public void endDrag() {

	}

	/**
	 * <p>
	 * Handle the key pressure.
	 * </p>
	 * Dispatches the key pressure to the menu or to the item depending on the
	 * state.
	 * 
	 * @param key
	 *            The pressed key.
	 * 
	 * @return <code>true</code> if the screen will keep the selection
	 */
	public boolean keyPressed(int key) {
		boolean selectionKept = false;
		try {
			if (this.popupList.size() > 0) {
				handleMenuKey((UIMenu) this.popupList.elementAt(this.popupList
						.size() - 1), key);
				selectionKept = true;
				// if the key is propagated to a popup or a menu it can change 
				// the popup size and hence force a repaint
				this.firstPaint = true;
			} else if (this.menu != null && this.menu.isOpenedState()) {
				handleMenuKey(menu, key);
				selectionKept = true;
				// if the key is propagated to a popup or a menu it can can change 
				// the popup size and hence force a repaint
				this.firstPaint = true;
			} else {
				if (key == UICanvas.MENU_RIGHT) {
					/* open menu */
					if (menu != null) {
						int menuSize = menu.getItems().size();
						// #ifndef RIM
						UIMenu contMenu = null;
						UIItem selItem = null;
						if (selectedIndex >= 0
								&& selectedIndex < this.items.size()) {
							selItem = ((UIItem) this.getItems().elementAt(
									selectedIndex)).getSelectedItem();
						}
						if (selItem != null) contMenu = selItem.getSubmenu();
						else if (selectedIndex >= 0
								&& selectedIndex < this.items.size()) {
							contMenu = ((UIItem) this.getItems().elementAt(
									selectedIndex)).getSubmenu();
						}
						if (menuSize == 2 && contMenu == null) {
							this.menuAction(menu, (UIItem) menu.getItems()
									.elementAt(0));
						} else if (menuSize > 1) {
							menu.setOpenedState(true);
							this.askRepaint();
						} else if (menuSize == 1) {
							this.menuAction(menu, (UIItem) menu.getItems()
									.elementAt(0));
						}
						// #endif
					}
					selectionKept = true;
				} else if (key == UICanvas.MENU_LEFT
				) {
					if (selectedIndex >= 0 && selectedIndex < this.items.size()) {
						// A "contextual menu" has been asked
						UIItem selItem = ((UIItem) this.getItems().elementAt(
								selectedIndex)).getSelectedItem();
						// An UIitem like UICombobox can have no selectedItem but a subMenu
						UIMenu contMenu = null;
						if (selItem != null) contMenu = selItem.getSubmenu();
						else
							contMenu = ((UIItem) this.getItems().elementAt(
									selectedIndex)).getSubmenu();
						if (this.selectedIndex >= 0 && contMenu != null) {
							// if menu has 0 width centers it on the screen
							if (contMenu.getWidth() == 0) contMenu.width = UICanvas
									.getInstance().getWidth()
									- contMenu.getAbsoluteX() * 2;
							if (contMenu.getItems().size() > 1) this
									.addPopup(contMenu);
							else if (contMenu.getItems().size() == 1) menuAction(
									contMenu, (UIItem) contMenu.getItems()
											.elementAt(0));
							// #ifndef RIM
						} else if (menu != null) {
							// second items of normal menu has been asked
							int menuSize = menu.getItems().size();
							if (menuSize == 2) {
								this.menuAction(menu, (UIItem) menu.getItems()
										.elementAt(1));
							}
							// #endif
						}
						// #ifndef RIM
					} else if (menu != null) {
						// second items of normal menu has been asked
						int menuSize = menu.getItems().size();
						if (menuSize == 2) {
							this.menuAction(menu, (UIItem) menu.getItems()
									.elementAt(1));
						}
						// #endif
					}
				}

				// first let the item receive the keyPressure
				if (this.selectedIndex >= 0) selectionKept = ((UIItem) this
						.getItems().elementAt(this.selectedIndex))
						.keyPressed(key);

				int ka = canvas.getGameAction(key);
				int newSelectedIndex = 0;
				// then take the "movement"
				if (selectionKept == false) {
					/* no menu opened, handle key normally */
					switch (ka) {
						case UICanvas.UP:
							if (selectedIndex == 0) {
								/* first item selected, can't go up further */
								break;
							}
							newSelectedIndex = 0;
							if (selectedIndex >= 0) {
								newSelectedIndex = selectedIndex - 1;
							}
							newSelectedIndex = traverseFocusable(
									newSelectedIndex, false);
							if (newSelectedIndex >= 0
									&& newSelectedIndex < this.getItems()
											.size()) {
								UIItem selectedItem = ((UIItem) this.getItems()
										.elementAt(newSelectedIndex));
								if (selectedItem.isFocusable()) {
									if (selectedIndex >= 0) {
										((UIItem) this.getItems().elementAt(
												selectedIndex))
												.setSelected(false);
									}
									selectedIndex = newSelectedIndex;
									((UIItem) this.getItems().elementAt(
											selectedIndex)).setSelected(true);
								} else {
									this.firstVisibleIndex = 0;
									for (int i = 0; i <= this.lastVisibleIndex; i++) {
										((UIItem) this.getItems().elementAt(i))
												.setDirty(true);
									}
								}
							}

							this.askRepaint();
							break;
						case UICanvas.DOWN:
							if (selectedIndex == this.getItems().size() - 1) {
								/* last item selected, can't go down further */
								break;
							}
							newSelectedIndex = 0;
							if (selectedIndex >= 0) {
								/* move selection down */
								newSelectedIndex = selectedIndex + 1;
							}
							newSelectedIndex = traverseFocusable(
									newSelectedIndex, true);
							if (newSelectedIndex >= 0
									&& newSelectedIndex < this.getItems()
											.size()) {
								if (((UIItem) this.getItems().elementAt(
										newSelectedIndex)).isFocusable()) {
									if (selectedIndex >= 0) {
										((UIItem) this.getItems().elementAt(
												selectedIndex))
												.setSelected(false);
									}
									selectedIndex = newSelectedIndex;
									((UIItem) this.getItems().elementAt(
											selectedIndex)).setSelected(true);
								} else if (lastVisibleIndex < this.getItems()
										.size() - 1) {
									int gapHeight = 0;
									for (int i = this.lastVisibleIndex + 1; i < this
											.getItems().size(); i++)
										gapHeight += ((UIItem) this.getItems()
												.elementAt(i))
												.getHeight(this.graphics);
									do {
										UIItem ithElem = ((UIItem) this
												.getItems().elementAt(
														firstVisibleIndex));
										gapHeight -= ithElem
												.getHeight(this.graphics);
										this.firstVisibleIndex++;
									} while (gapHeight > 0);
									for (int i = firstVisibleIndex; i < this
											.getItems().size(); i++) {
										((UIItem) this.getItems().elementAt(i))
												.setDirty(true);
									}
								}
							}
							this.askRepaint();
							break;
						default:
							break;
					}
				}

				// then raise the "fire" event
				if (ka == UICanvas.FIRE && this.selectedIndex >= 0) {
					UIItem selectedItem = ((UIItem) this.getItems().elementAt(
							selectedIndex)).getSelectedItem();
					if (selectedItem != null) this.itemAction(selectedItem);
					selectionKept = true;
				}

				// #ifdef UI_DEBUG
				//@				System.out.println("moved: " + firstVisibleIndex + "/"
				//@						+ lastVisibleIndex + "/" + selectedIndex);
				// #endif 
			}
		} catch (Exception e) {
			// #mdebug
									System.out.println("In keyPressed:" + e.getClass() + " "
											+ e.getMessage());
									e.printStackTrace();
			// #enddebug
		}
		return selectionKept;
	}

	void handleMenuKey(UIMenu openMenu, int key) {
		Object[] oldPopups = new Object[popupList.size()];
		this.popupList.copyInto(oldPopups);
		if (key == UICanvas.MENU_RIGHT
				|| canvas.getGameAction(key) == UICanvas.FIRE) {
			/* select, close menu */
		} else if (key == UICanvas.MENU_LEFT) {
			/* cancel, close menu */
			openMenu.setOpenedState(false);
			if (openMenu != null) {
				// the menu was here -> invalidate all:

				int x1 = openMenu.getAbsoluteX();
				int y1 = openMenu.getAbsoluteY();
				int x2 = x1 + openMenu.getWidth();
				int y2 = y1 + openMenu.getHeight(this.graphics);
				int[] coors = new int[] { x1, y1, x2, y2 };
				this.invalidateArea(coors);
			}
			if (openMenu.getParentMenu() != null) {
				openMenu.getParentMenu().setSubmenu(null);
			}
			this.removePopup((UIMenu) openMenu);

			this.askRepaint();
			return;
		}
		int ga = canvas.getGameAction(key);

		UIItem um = openMenu.keyPressed(key, ga);

		if (um != null) {
			// if a UIMenuItem has been selected and it has not
			// a submenu i need to close the menu
			if (um.getSubmenu() == null) {
				if (openMenu.isAutoClose()) {
					openMenu.setOpenedState(false);
					// the menu was here -> invalidate all:
					int menuY = openMenu.getAbsoluteY();
					int cumulativeHeight = this.headerLayout
							.getHeight(this.graphics);
					for (int i = this.firstVisibleIndex; i < this.getItems()
							.size(); i++) {
						UIItem ith = (UIItem) this.getItems().elementAt(i);
						cumulativeHeight += ith.getHeight(this.graphics);
						if (cumulativeHeight >= menuY) {
							ith.setDirty(true);
						}
					}
					if (this.menu != null) {
						this.menu.setOpenedState(false);
						this.menu.setSubmenu(null);
					}
				}
				this.askRepaint();
			}

			// first close the menu and then complete the action 
			if ((key == UICanvas.MENU_RIGHT || ga == UICanvas.FIRE)) {
				this.menuAction(openMenu, um);
			}

			if (um.getSubmenu() == null) {
				if (openMenu.isAutoClose()) {
					// we cannot use: this.removeAllPopups();
					// because if a menu has been added meanwhile (for example in
					// response
					// to a keypression) it is removed suddenly !!!
					//					for (int i = 0; i < oldPopups.length; i++) {
					//						this.removePopup((UIMenu) oldPopups[i]);
					//					}

					// I want to close only the last visible menu
					this.removePopup(openMenu);
				}
				this.askRepaint();
			}
		}
	}

	private boolean painting = false;
	private boolean bookRepaint = false;

	// #mdebug 
			private static int paintCount = 0;
		
	// #enddebug

	/**
	 * Used by items and menus to ask the containing screen a repaint.
	 */
	public boolean askRepaint() {
		if (painting == false) {
			// #mdebug 
									paintCount++;
									if (paintCount >= 2) {
										System.out.println("Multiple paint: lock problem");
									}
			// #enddebug
			try {
				painting = true;
				this.canvas.askRepaint(this, null);
			} catch (Exception e) {
				bookRepaint = false;
				// #mdebug 
												System.out.println("in screen painting");
												e.printStackTrace();
				// #enddebug
			} finally {
				painting = false;
				// #mdebug 
												paintCount--;
				// #enddebug
			}
			if (bookRepaint) {
				bookRepaint = false;
				return this.askRepaint();
			}
		} else {
			bookRepaint = true;
		}
		return true;
	}

	public boolean paint(UIGraphics g) {
// #ifndef RIM_5.0
		return paintJ2ME(g);
		// #endif
	}


	/**
	 * Paints the screen on the canvas graphics; in case the subclasses override
	 * this method they *MUST* provide to call super.paint (...); .
	 * 
	 * @param g
	 *            The graphics on which to paint.
	 */
	public boolean paintJ2ME(UIGraphics g) {
		changed = false;
		this.graphics = g;
		// in many devices the height may vary during execution
		this.height = UICanvas.getInstance().getClipHeight();
		int footerHeight = 0;
		// #ifndef RIM
		footerHeight = (footer != null) ? footer.getHeight(g) : 0;
		// #endif
		/* calculate clip area for content */
		int canvasHeight = this.canvas.getClipHeight();
		// subtract footer height
		int canvasWidth = this.canvas.getWidth();
		canvasHeight -= footerHeight;
		int headerHeight = (headerLayout != null) ? headerLayout.getHeight(g)
				: 0;
		/* draw title */
		UIFont cfont = g.getFont();
		if (headerLayout != null && headerLayout.isDirty()) {
			paintHeader(g, canvasWidth, headerHeight);
		}

		// subtract header height (if present)
		g.setClip(0, headerHeight, canvasWidth, canvasHeight - headerHeight);
		g.translate(0, headerHeight);

		// #ifdef UI_DEBUG 
		//@		System.out.println("paint0: " + canvasWidth + "/"
		//@				+ (canvasHeight - headerHeight) + "/" + g.getClipHeight() + "/"
		//@				+ g.getTranslateY());
		// #endif

		// a trick used to avoid painting of screen when a popup is opened
		/* draw menu if opened */
		boolean aMenuIsOpened = false;
		if ((this.menu != null && this.menu.isOpenedState())
				|| this.popupList.size() > 0) {
			aMenuIsOpened = true;
		}

		if (aMenuIsOpened == false || firstPaint) {
			firstPaint = false;
			int availableHeight;
			if (UICanvas.getInstance().hasPointerMotionEvents()) {
				availableHeight = 10000;
			} else {
				availableHeight = canvasHeight - headerHeight;
			}
			this.paint0(g, canvasWidth, availableHeight);
			this.removePaintedItem(this);

			// clean the gap
			if (this.lastVisibleIndex == this.getItems().size() - 1) {
				cleanGap(g, headerHeight, canvasHeight, canvasWidth);
			}
		}

		/* draw menu if opened */
		if (this.menu != null && this.menu.isOpenedState()
				&& this.menu.isDirty()) {
			paintMenu(g, headerHeight, canvasHeight, canvasWidth);
		}

		// first draw the popup list
		boolean hasPopup = paintPopups(g, footerHeight, headerHeight,
				canvasHeight, canvasWidth);

		paintBorders(g, headerHeight, canvasHeight, canvasWidth);

		/* footer height */
		g
				.translate(0 - g.getTranslateX(), canvasHeight + 1
						- g.getTranslateY());
		// #ifndef RIM
		paintFooter(g, footerHeight, canvasWidth, hasPopup);
		// #endif

		// #ifdef UI_DEBUG 
		//@		System.out.println("screenPaint done: " + g.getClipWidth() + "/"
		//@				+ g.getClipHeight() + "/" + g.getTranslateX() + "/"
		//@				+ g.getTranslateY());
		// #endif
		g.setFont(cfont);
		return changed;
	}

	/**
	 * @param g
	 * @param h
	 * @return
	 */
	protected int resizeHeight(UIGraphics g, int h) {
		// is it too big and it doesn't fit ?
		if (this.height > h) {
			// can we move it ?
			// use an offset from up
			int Y = this.getAbsoluteY();
			int clipY = g.getClipY();
			int translateY = g.getTranslateY();
			if ((Y - clipY - translateY) > (this.height - h)) {
				setAbsoluteY(this.height - h);
				// System.out.println("Translate 1: 0, " + (h - this.height));
				g.translate(0, (h - this.height));
				h = this.height;
			} else {
				int offset = 0;
				setAbsoluteY(translateY + clipY + offset);
				this.height = g.getClipHeight() - offset;
				g.translate(0, getAbsoluteY() - g.getTranslateY());// +
				setNeedScrollbar(true);
			}
		}
		// #ifndef RIM
		// check for touch screen
		if (UICanvas.getInstance().hasPointerMotionEvents()) {
			int hHeight = headerLayout.getHeight(g);
			int fHeight = footer.getHeight(g);
			int avHeight = UICanvas.getInstance().getClipHeight() - hHeight
					- fHeight - 1;
			setNeedScrollbar(avHeight < this.height ? true : false);
		}
		// #endif
		return h;
	}

	// #ifndef RIM

	/**
	 * @param h
	 * @param neededHeight
	 * @return
	 */
	protected int getScrollbarHeight(int h, int neededHeight) {
		if (UICanvas.getInstance().hasPointerMotionEvents() == false) return super
				.getScrollbarHeight(h, neededHeight);
		else {
			UIGraphics g = getGraphics();
			int hHeight = headerLayout.getHeight(g);
			int fHeight = footer.getHeight(g);
			int avHeight = UICanvas.getInstance().getClipHeight() - hHeight
					- fHeight - 1;
			return super.getScrollbarHeight(avHeight, neededHeight);
		}
	}

	protected int getScrollbarPosition(UIGraphics g, int h, int scrollbarHeight) {
		if (UICanvas.getInstance().hasPointerMotionEvents()) {
			int scrollbarPosition = 0;
			int hHeight = headerLayout.getHeight(g);
			int fHeight = footer.getHeight(g);
			int avHeight = UICanvas.getInstance().getClipHeight() - hHeight
					- fHeight - 1;
			if (this.paintOffset == avHeight - this.height) {
				scrollbarPosition = (avHeight - scrollbarHeight);
			} else {
				scrollbarPosition = (-paintOffset * avHeight) / height;
			}
			return scrollbarPosition;
		} else {
			return super.getScrollbarPosition(g, h, scrollbarHeight);
		}
	}

	private void paintFooter(UIGraphics g, int footerHeight, int canvasWidth,
			boolean hasPopup) {
		/* draw menu buttons - LEFT */
		String left = "";
		if (hasPopup || (menu != null && menu.isOpenedState())) {
			if (hasPopup) {
				left = ((UIMenu) this.popupList
						.elementAt(this.popupList.size() - 1)).cancelMenuString;
			}
			if ((menu != null && menu.isOpenedState())) {
				left = menu.cancelMenuString;
			}
		} else if (selectedIndex >= 0) {
			UIItem selItem = ((UIItem) this.getItems().elementAt(selectedIndex))
					.getSelectedItem();
			UIMenu contMenu = null;
			if (selItem != null) contMenu = selItem.getSubmenu();
			else
				contMenu = ((UIItem) this.getItems().elementAt(selectedIndex))
						.getSubmenu();
			if (contMenu != null) {
				if (contMenu.getItems().size() == 1) {
					UIItem firstItem = (UIItem) contMenu.getItems()
							.elementAt(0);
					if (firstItem instanceof UILabel) left = ((UILabel) firstItem)
							.getText();
					else
						left = UIConfig.optionsString;
				} else
					left = UIConfig.optionsString;
			}
		}

		String right = "";
		if (menu != null && !menu.isOpenedState() && !hasPopup) {
			if (menu.getItems().size() == 1) right = ((UILabel) menu.getItems()
					.elementAt(0)).getText();
			else if (menu.getItems().size() == 2 && left.length() == 0) {
				right = ((UILabel) menu.getItems().elementAt(0)).getText();
				left = ((UILabel) menu.getItems().elementAt(1)).getText();
			} else
				right = UIConfig.menuString;
		} else if ((menu != null && menu.isOpenedState()) || hasPopup) {
			if (hasPopup) {
				right = ((UIMenu) this.popupList.elementAt(this.popupList
						.size() - 1)).selectMenuString;
			}
			if ((menu != null && menu.isOpenedState())) {
				right = menu.selectMenuString;
			}
		}
		right = right.toUpperCase();
		left = left.toUpperCase();
		if (right.compareTo(this.footerRight.getText()) != 0) {
			this.footerRight.setText(right);
			changed = true;
		}
		if (left.compareTo(this.footerLeft.getText()) != 0) {
			this.footerLeft.setText(left);
			changed = true;
		}
		if (footer.isDirty()) {
			footer.paint0(g, canvasWidth, footer.getHeight(g));
			g.setColor(UIConfig.header_fg);
			g.drawLine(canvasWidth / 2, 0, canvasWidth / 2, footerHeight);
		}
		// footere elements must be at the top of the painted items list
		addPaintedItem(footerLeft);
		addPaintedItem(footerRight);
	}

	// #endif

	private void paintBorders(UIGraphics g, int headerHeight, int canvasHeight,
			int canvasWidth) {
		// draw the border (just in case something has overwritten it)
		g.translate(-g.getTranslateX(), headerHeight - g.getTranslateY());
		g.setClip(0, 0, canvas.getWidth(), canvas.getClipHeight());
		g.setColor(0x223377);
		g.drawRect(0, 0, canvasWidth - 1, canvasHeight - headerHeight);
	}

	private boolean paintPopups(UIGraphics g, int footerHeight,
			int headerHeight, int canvasHeight, int canvasWidth) {
		boolean hasPopup = false;
		for (Enumeration en = popupList.elements(); en.hasMoreElements();) {
			hasPopup = true;
			changed = true;
			int translatedX = g.getTranslateX();
			int translatedY = g.getTranslateY();
			UIMenu ithPopup = (UIMenu) en.nextElement();
			g.translate(-translatedX, -translatedY);
			// move the popup in order to have it on the screen
			if (ithPopup.getAbsoluteY() < headerHeight) {
				ithPopup.setAbsoluteY(headerHeight);
			}

			// subtract footer height and header for the available area
			int availableHeight = this.canvas.getClipHeight() - footerHeight
					- ithPopup.getAbsoluteY();
			// set a clip for the popup in order to avoid overprinting
			// header and footer;
			g
					.setClip(0, headerHeight, canvasWidth, canvasHeight
							- headerHeight);
			g.translate(ithPopup.getAbsoluteX(), ithPopup.getAbsoluteY());
			// if a popup is dirty must be redrawn and
			// all the subsequent
			// if (ithPopup.isDirty() && i < (this.popupList.size() - 1)) {
			// ((UIMenu) this.popupList.elementAt(i + 1)).setDirty(true);
			// }

			if (ithPopup.isDirty()) {
				// force repaint of all the menu items
				ithPopup.setDirty(true);
				ithPopup.paint0(g, ithPopup.width, availableHeight);
				ithPopup.setOpenedState(true);
			}
		}
		return hasPopup;
	}

	/**
	 * @param h
	 * @param lastHeight
	 * @return
	 */
	protected boolean heightReached(int h, int lastHeight) {
		return lastHeight > h - borderSize;
	}

	/**
	 * @param h
	 * @param lastHeight
	 * @return
	 */
	protected boolean lastItemReached(int h, int lastHeight) {
		return lastHeight == h;
	}

	private void paintMenu(UIGraphics g, int headerHeight, int canvasHeight,
			int canvasWidth) {
		// force repaint of all the menu items
		this.menu.setDirty(true);
		int x0 = 29;
		int y0 = canvasHeight / 2;
		int x1 = canvasWidth;
		int y1 = canvasHeight;
		int translatedX = g.getTranslateX();
		int translatedY = g.getTranslateY();
		g.translate(-translatedX, -translatedY);
		g.setClip(0, headerHeight, canvasWidth, canvasHeight - headerHeight);
		// I choose a maximum height for the menus
		int menuHeight = menu.getHeight(g);
		if (menuHeight > y0) menuHeight = y0;
		g.translate(x0, y1 - menuHeight);
		menu.setAbsoluteX(x0);
		menu.setAbsoluteY(y1 - menuHeight);
		menu.paint0(g, x1 - x0, y1 - y0);
		changed = true;
	}

	protected void computeClip(UIGraphics g, int height, int width) {

	}

	private void cleanGap(UIGraphics g, int headerHeight, int canvasHeight,
			int canvasWidth) {
		g.setColor(getBg_color() >= 0 ? getBg_color() : UIConfig.bg_color);
		int yGapHeight = canvasHeight - headerHeight;
		int usedHeight = 0;
		for (int i = firstVisibleIndex; i <= lastVisibleIndex; i++) {
			UIItem ui = (UIItem) getItems().elementAt(i);
			usedHeight += ui.getHeight(g);
		}
		g.translate(-g.getTranslateX(), headerHeight + usedHeight
				- g.getTranslateY());
		int gap = yGapHeight - usedHeight;
		int avWidth = canvasWidth;
		if (this.isNeedScrollbar() == true) {
			avWidth -= (UIConfig.scrollbarWidth + 1);
		}
		if (this.getBgImage() != null) {
			paintBGRegion(g, getBgImage(), 1, usedHeight - 1, avWidth, gap, 1,
					1);
		} else {
			g.fillRect(1, 1, avWidth, gap);
		}
		// if the gap is filled menu and popups can be dirty
		// and must be redrawn
		if (menu != null) {
			menu.setDirty(true);
		}
		for (Enumeration en = popupList.elements(); en.hasMoreElements();) {
			UIMenu ithPopup = (UIMenu) en.nextElement();
			ithPopup.setDirty(true);
		}
		changed = true;
	}

	private void paintHeader(UIGraphics g, int headerWidth, int headerHeight) {
		headerLayout.paint0(g, headerWidth, headerHeight);
		changed = true;
	}

	/**
	 * @return This screen's title
	 */
	public String getTitle() {
		return this.titleLabel.getText();
	}

	/**
	 * Sets this screen's title
	 * 
	 * @param title
	 *            the screen title
	 */
	public void setTitle(String title) {
		this.titleLabel.setText(title);
	}

	public UICanvas getCanvas() {
		return canvas;
	}

	/*
	 * Invalidates the portion of the screen between (0,0) and (w,h)
	 */
	public void invalidate(int w, int h) {
		int cumulativeHeight = 0;
		// remove footer height
		int fh = this.headerLayout.getHeight(this.graphics);
		h = h - fh;

		// reset the Items
		this.firstVisibleIndex = 0;
		for (Enumeration en = getItems().elements(); en.hasMoreElements();) {
			UIItem ithItem = (UIItem) en.nextElement();
			cumulativeHeight += ithItem.getHeight(this.graphics);
			ithItem.setDirty(true);
		}
		// reset the popups
		for (Enumeration en = popupList.elements(); en.hasMoreElements();) {
			UIMenu ithMenu = (UIMenu) en.nextElement();
			int ithMenuHeight = ithMenu.getAbsoluteY()
					+ ithMenu.getHeight(this.graphics);
			if (ithMenuHeight > h) {
				ithMenu.setAbsoluteY(ithMenu.getAbsoluteY()
						- (ithMenuHeight - h));
			}
			int ithMenuWidth = ithMenu.getAbsoluteX() + ithMenu.getWidth();
			if (ithMenuWidth > w) {
				ithMenu.setAbsoluteX(ithMenu.getAbsoluteX()
						- (ithMenuWidth - w));
			}

			if (ithMenu.getAbsoluteX() < w || ithMenu.getAbsoluteY() < h) {
				ithMenu.setDirty(true);
			}
		}
		this.headerLayout.setDirty(true);
		// #ifndef RIM
		this.footer.setDirty(true);
		// #endif
	}

	/**
	 * This method is called each time a {@link UIMenuItem} of a {@link UIMenu} receives the pressure of a FIRE. The subclasses of screen SHOULD override
	 * this method in order to couple an action to the pressure of an action
	 * key.
	 * 
	 * @param menu
	 *            The selected Menu
	 * @param item
	 *            The {@link UIMenuItem} that received the key pressure
	 */
	public void menuAction(UIMenu menu, UIItem item) {
	}

	/**
	 * This method is called each time a {@link UIItem} of this {@link UIScreen} receives the pressure of a FIRE. The subclasses of screen SHOULD override
	 * this method in order to couple an action to the pressure of an action
	 * key.
	 * 
	 * @param item
	 *            The {@link UIItem} that received the key pressure
	 */
	public void itemAction(UIItem item) {
	}

	/**
	 * @param graphics
	 *            the graphics to set
	 */
	public void setGraphics(UIGraphics graphics) {
		this.graphics = graphics;
	}

	/**
	 * @return the graphics
	 */
	public UIGraphics getGraphics() {
		return graphics;
	}

	/**
	 * @return Return true if this {@link UIScreen} is freezed
	 */
	synchronized public boolean isFreezed() {
		return freezed;
	}

	/**
	 * @param Freezes
	 *            or Unfreezes this {@link UIScreen}.
	 */
	public void setFreezed(boolean freezed) {
		this.freezed = freezed;
	}

	/**
	 * All the menuItems and popups that intersects this area are set as dirty
	 */
	public void invalidateArea(int[] coors) {
		// first check if it intersects any item in itemList
		int cumulativeHeight = this.headerLayout.getHeight(this.graphics);
		for (int i = this.firstVisibleIndex; i < this.getItems().size(); i++) {
			UIItem ith = (UIItem) this.getItems().elementAt(i);
			int yb = invalidateItem(ith, coors, cumulativeHeight);
			cumulativeHeight = yb;
		}
		// then the popup
		for (Enumeration en = popupList.elements(); en.hasMoreElements();) {
			UIMenu ithMenu = (UIMenu) en.nextElement();
			invalidateMenu(ithMenu, coors);
		}
		// and then the menu if opened
		if (this.menu != null && this.menu.isOpenedState()) {
			invalidateMenu(this.menu, coors);
		}

	}

	/**
	 * @param ith
	 * @param coors
	 * @param cumulativeHeight
	 * @return
	 */
	private int invalidateItem(UIItem ith, int[] coors, int cumulativeHeight) {
		int xa = 0, xb = this.width;
		int ya = cumulativeHeight;
		int yb = cumulativeHeight + ith.getHeight(this.graphics);
		if (intersect(coors, new int[] { xa, ya, xb, yb })) {
			ith.setDirty(true);
		}
		return yb;
	}

	private boolean intersect(int[] recta, int[] rectb) {
		int x1 = recta[0];
		int y1 = recta[1];
		int x2 = recta[2];
		int y2 = recta[3];
		int xa = rectb[0];
		int ya = rectb[1];
		int xb = rectb[2];
		int yb = rectb[3];
		if ((xb < x1) || (x2 < xa) || (yb < y1) || (y2 < ya)) return false;
		return true;
	}

	public void invalidatePopups(UIMenu drawnMenu, int[] coors) {

		// reset the clips in order to invalidate correctely 
		// all the objects in the screen
		UIGraphics g = this.graphics;
		int originalClipWidth = g.getClipWidth();
		int originalClipHeight = g.getClipHeight();
		int originalClipX = g.getClipX();
		int originalClipY = g.getClipY();
		int w = UICanvas.getInstance().getWidth();
		int h = UICanvas.getInstance().getClipHeight();
		g.setClip(0, 0, w, h);

		int cumulativeHeight = this.headerLayout.getHeight(g);
		for (int i = this.firstVisibleIndex; i < items.size(); i++) {
			UIItem ith = (UIItem) this.getItems().elementAt(i);
			int yb = invalidateItem(ith, coors, cumulativeHeight);
			cumulativeHeight = yb;
		}

		if (this.menu != null && this.menu != drawnMenu
				&& this.menu.isOpenedState()) {
			invalidateMenu(this.menu, coors);
		}
		// then the popup
		for (Enumeration en = popupList.elements(); en.hasMoreElements();) {
			UIMenu ithMenu = (UIMenu) en.nextElement();
			if (ithMenu == drawnMenu) continue;
			invalidateMenu(ithMenu, coors);
		}

		g.setClip(originalClipX, originalClipY, originalClipWidth,
				originalClipHeight);
	}

	/**
	 * @param coors
	 */
	private void invalidateMenu(UIMenu menuToInvalidate, int[] coors) {
		int xa = menuToInvalidate.getAbsoluteX(), xb = xa
				+ menuToInvalidate.getWidth();
		int ya = menuToInvalidate.getAbsoluteY();
		int yb = ya + menuToInvalidate.getHeight(this.graphics);
		if (intersect(coors, new int[] { xa, ya, xb, yb })) {
			menuToInvalidate.setDirty(true);
		}
	}

	public void setDirty(boolean dirty) {
		super.setDirty(dirty);
		for (Enumeration en = popupList.elements(); en.hasMoreElements();) {
			UIItem ithItem = (UIItem) en.nextElement();
			ithItem.setDirty(dirty);
		}
		if (this.menu != null) this.menu.setDirty(dirty);
		this.headerLayout.setDirty(true);
		// #ifndef RIM
		this.footer.setDirty(true);
		// #endif
		this.paintedItems.removeAllElements();
		this.firstPaint = true;
	}

	public void setSelectedIndex(int selectedIndex) {
		super.setSelectedIndex(selectedIndex);
		if (selectedIndex > lastVisibleIndex) {
			// forcing it to be the last visible to avoid useless
			// redraw
			lastVisibleIndex = selectedIndex;
		}
	}

	/*
	 * The keyRepeated is usually handled at the sam manner
	 * moving the screen / control up and down
	 */
	public void keyRepeated(int key) {
		int ga = UICanvas.getInstance().getGameAction(key);
		switch (ga) {
			case UICanvas.DOWN:
			case UICanvas.UP: {
				this.setFreezed(true);
				for (int i = 0; i < 5; i++) {
					this.keyPressed(key);
				}
				this.setFreezed(false);
				this.askRepaint();
			}
		}
	}

	/*
	 * The coordinates plus height and width of each item in this screen
	 */
	public void addPaintedItem(UIItem item) {
		removePaintedItem(item);
		paintedItems.insertElementAt(item, 0);
	}

	/*
	 * Remove a painted item from screen paintItems
	 */
	public void removePaintedItem(UIItem item) {
		paintedItems.removeElement(item);
	}

	public Vector getPaintedItems() {
		return paintedItems;
	}

	public void setPopupList(Vector popupList) {
		this.popupList = popupList;
	}

	public Vector getPopupList() {
		return popupList;
	}

	public boolean isRollEnabled() {
		return rollEnabled;
	}

	public void setRollEnabled(boolean rollEnabled) {
		this.rollEnabled = rollEnabled;
	}

	public void setReturnScreen(UIScreen returnScreen) {
		this.returnScreen = returnScreen;
	}

	public UIScreen getReturnScreen() {
		return returnScreen;
	}

	/*
	 * Closes the Screen; subclasses may override the method to do additional logic:
	 * like popup confirmation
	 * 
	 * @return false if the Screen does not want to close
	 */
	public boolean askClose() {
		return true;
	}

	public void setRightArrow(UIImage rag) {
		((UILabel) this.headerLayout.layoutItems[2]).setImg(rag);
	}

	public void setLeftArrow(UIImage lag) {
		((UILabel) this.headerLayout.layoutItems[0]).setImg(lag);
	}

	/**
	 * @return The height of the item
	 * @param g
	 *            the {@link Graphics} on which to paint into
	 */
	public int getHeight(UIGraphics g) {
		int originalClipY = g.getClipY();
		int originalClipX = g.getClipX();
		int originalClipHeight = g.getClipHeight();
		int originalClipWidth = g.getClipWidth();
		int originalX = g.getTranslateX();
		int originalY = g.getTranslateY();
		int maxClipHeight = originalClipHeight;
		this.height = 0;
		for (Enumeration en = items.elements(); en.hasMoreElements();) {
			UIItem ithItem = (UIItem) en.nextElement();
			g.setClip(0, 0, 0, maxClipHeight);
			int ithHeight = ithItem.getHeight(g);
			g.translate(0, ithHeight);
			maxClipHeight -= ithHeight;
			this.height += ithHeight;
		}
		g.translate(originalX, originalY - g.getTranslateY());
		g.setClip(originalClipX, originalClipY, originalClipWidth,
				originalClipHeight);
		if (height < originalClipHeight) height = originalClipHeight;
		return height;
	}

	public void sizeChanged(int w, int h) {
		this.firstPaint = true;
		this.setDirty(true);
	}

	// #ifndef RIM
	/**
	 * @param paintOffset the paintOffset to set
	 */
	protected void addPaintOffset(int paintOffset) {
		if (getMenu() != null && getMenu().isOpenedState()) {
			getMenu().addPaintOffset(paintOffset);
			return;
		}
		if (popupList.size() > 0) {
			((UIMenu) this.popupList.elementAt(this.popupList.size() - 1))
					.addPaintOffset(paintOffset);
			return;
		}
		this.paintOffset += paintOffset;
		if (this.paintOffset > 0) this.paintOffset = 0;
		UIGraphics g = getGraphics();
		int hHeight = headerLayout.getHeight(g);
		int fHeight = footer.getHeight(g);
		int avHeight = UICanvas.getInstance().getClipHeight() - hHeight
				- fHeight - 1;
		if (this.height <= avHeight) {
			this.paintOffset = 0;
			return;
		}
		int printedHeight = this.height + this.paintOffset;
		if (printedHeight <= avHeight) {
			this.paintOffset = avHeight - this.height;
		}
	}

	/**
	 * @return the paintOffset
	 */
	protected int getPaintOffset() {
		return paintOffset;
	}

	// #endif

}
