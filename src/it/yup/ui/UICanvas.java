// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UICanvas.java 2441 2011-01-31 20:28:26Z luca $
 */

/**
 * 
 */
package it.yup.ui;

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

import it.yup.util.Utils;
import it.yup.ui.wrappers.UIFont;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.ui.wrappers.UIImage;
import it.yup.util.Alerts;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;

// #ifndef RIM

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

// #endif

/**
 * UICanvas is the class that holds all the open screens and shows them. Screens
 * are held in a stack-like structure, showing only the one at the top of the
 * stack. Screens may be opened, closed, showed and hidden; by pressing and
 * holding the '*' key, the user can switch from a screen to another.
 * 
 * The UICanvas is singleton: only one may exist per midlet.
 */
// #ifndef RIM
public class UICanvas extends GameCanvas {
	// #endif

	/** the key used to activate the left key */
	public static int MENU_LEFT = -6;
	/** the key used to activate the right key */
	public static int MENU_RIGHT = -7;
	public static int MENU_CANCEL = -8;

	/*
	 * Caching information about weather the canvas is shown or not
	 */
	private static boolean active = false;

	private static final Object[][] keyMaps = {
			{ "Nokia", new Integer(-6), new Integer(-7) },
			{ "ricsson", new Integer(-6), new Integer(-7) },
			{ "iemens", new Integer(-1), new Integer(-4) },
			{ "otorola", new Integer(-21), new Integer(-22) },
			{ "harp", new Integer(-21), new Integer(-22) },
			{ "j2me", new Integer(-6), new Integer(-7) },
			{ "SunMicrosystems_wtk", new Integer(-6), new Integer(-7) },
			{ "lackberry", new Integer(-6), new Integer(-7) }

	};

	// #ifndef RIM
	/** The alert used to show errors (if any) */
	private static Alert alert;

	/** the display */
	private static Display display;

	private static Object lockObject = new Object();

	// #endif

	public static Object getLock() {
// #ifndef RIM
		return lockObject;
		// #endif
	}

	/** singleton instance */
	private static UICanvas _instance;

	/** a timer to schedule tasks */
	private static Timer timer;

	/** l'elenco degli screen */
	private Vector screenList = new Vector();

	private int viewedIndex = 0;

	private boolean qwerty = false;

	/**
	 * @return the screenList
	 */
	public Vector getScreenList() {
		return screenList;
	}

	/** il "popup" che contiene le finestre aperte */
	private UIMenu wlist;

	/*
	 * The images used in tabs
	 */
	UIImage rag = UICanvas.getUIImage("/icons/rag.png");
	UIImage lag = UICanvas.getUIImage("/icons/lag.png");
	private UIImage rab = UICanvas.getUIImage("/icons/rab.png");
	private UIImage lab = UICanvas.getUIImage("/icons/lab.png");

	// #ifdef UI_DEBUG
	//@	/*
	//@	 * The time at which the pointer is pressed
	//@	 */
	//@	private long pressedTime;
	//@
	//@	/*
	//@	 * The time at which the pointer is released
	//@	 */
	//@	private long releasedTime;
	//@
	// #endif

	private int pressedX;
	private int pressedY;
	private int draggedY;
	private int bufferedOffsetX = 0;
	private int bufferedOffsetY = 0;
	private Timer dragTimer = null;
	private long lastDragTime = 0;
	private long dragInterval = 100;
	// #ifndef RIM

	private long lastPressedTime = 0;
	private long lastReleaseTime = 0;
	private int step = 0;
	private int inertia;

	// #endif

	/*
	 * Set to true when dragging an object
	 */
	private boolean dragged = false;

	/*
	 * A boolean used to know if repeated events are generated. If it is false
	 * the repeated events are generated artificially.
	 */
	public boolean hasRE = true;

	private long keyReleasedTime = Long.MAX_VALUE;
	private long keyPressedTime = Long.MAX_VALUE;

	TimerTask longPressedTask = initLongPress(null);
	boolean longPressRun = false;

	/**
	 * The constructor for {@link UICanvas}.
	 * 
	 * @param suppressKeyEvents
	 *            True to suppress the regular key event mechanism for game
	 *            keys, otherwise False.
	 */
	private UICanvas() {
		super(false);
		// #ifndef RIM
		setFullScreenMode(true);
		// #endif
		this.hasRE = this.hasRepeatEvents();
		// not initialized
		setupdefaultKeyCode();
		if (this.hasPointerMotionEvents()) {
			int newWidth = ((int) (rab.getWidth() * 1.5));
			int newHeight = ((int) (rab.getHeight() * 1.5));
			this.rab = rab.imageResize(newWidth, newHeight, false);
			this.rag = rag.imageResize(newWidth, newHeight, false);
			this.lag = lag.imageResize(newWidth, newHeight, false);
			this.lab = lab.imageResize(newWidth, newHeight, false);
		}
	}

	private boolean isMotorola() {
		try {
			String imei = System.getProperty("IMEI");
			// #mdebug
			Logger.log("IMEI :" + imei);
			// #enddebug
			if (imei != null) return true;
			imei = System.getProperty("com.motorola.IMEI");
			// #mdebug
			Logger.log("com.motorola.IMEI :" + imei);
			// #enddebug
			if (imei != null) return true;
		} catch (Exception e) {

		}
		return false;
	}

	private void setupdefaultKeyCode() {
		String platform = System.getProperty("microedition.platform");

		// hack to detect if is a "strange motorola"
		if (platform.indexOf("otorola") != -1 || isMotorola()) {
			platform = "Motorola";
		}

		// #mdebug
		Logger.log("platform:" + platform);
		// #enddebug
		for (int i = 0; i < keyMaps.length; ++i) {
			String manufacturer = (String) keyMaps[i][0];

			if (platform.indexOf(manufacturer) != -1) {
				if (i == 1) {
					if (platform.indexOf("P900") != -1
							|| platform.indexOf("P908") != -1) {
						UICanvas.MENU_LEFT = ((Integer) keyMaps[i][2])
								.intValue();
						UICanvas.MENU_RIGHT = ((Integer) keyMaps[i][1])
								.intValue();
					} else {
						UICanvas.MENU_LEFT = ((Integer) keyMaps[i][1])
								.intValue();
						UICanvas.MENU_RIGHT = ((Integer) keyMaps[i][2])
								.intValue();
					}
				} else {
					UICanvas.MENU_LEFT = ((Integer) keyMaps[i][1]).intValue();
					UICanvas.MENU_RIGHT = ((Integer) keyMaps[i][2]).intValue();
				}
				break;
			}
		}
	}

	/**
	 * Used to get the "effective" height of the canvas. getHeight() sometimes
	 * computes the menu bar space even if it is not displayed.
	 * 
	 * @return The height of the clip.
	 */
	public int getClipHeight() {
		return clipHeight; // this.getGraphics().getClipHeight();
	}

	protected void keyReleased(int key) {
		try {
			// #ifndef RIM
			synchronized (UICanvas.getLock()) {
				// #endif
				_keyReleased(key);
				// #ifndef RIM
			}
			// #endif
		} catch (Exception e) {
			// #mdebug
			Logger.log("In key pressed:" + e.getMessage() + " "
					+ e.getClass().getName());
			e.printStackTrace();
			// #enddebug
		} finally {

		}
	}

	/**
	 * @param key
	 */
	private void _keyReleased(int key) {
		if (this.hasRE == false) {
			if (System.currentTimeMillis() - keyPressedTime > 1000) {
				_keyRepeated(key);
			}
			keyReleasedTime = System.currentTimeMillis();
			keyPressedTime = keyReleasedTime - 1;
		}
	}

	/**
	 * <p>
	 * Handle the key pressure.
	 * </p>
	 * Dispatches the key pressure to the shown screen.
	 * 
	 * @param key
	 *            The pressed key.
	 */
	protected void keyPressed(int key) {
		// #mdebug
		// Logger.log("key pressed:" + key);
		// #enddebug

		try {
			// #ifndef RIM
			synchronized (UICanvas.getLock()) {
				// #endif
				this.lastGameAction = this.getGameAction(key);
				_keyPressed(key);
				return;
				// #ifndef RIM
			}
			// #endif
		} catch (Exception e) {
			// #mdebug
			Logger.log("In key pressed:" + e.getMessage() + " "
					+ e.getClass().getName());
			e.printStackTrace();
			// #enddebug
		}
	}

	/**
	 * @param key
	 */
	private void _keyPressed(int key) {
		if (this.hasRE == false) {
			if (keyPressedTime < keyReleasedTime) {
				keyPressedTime = System.currentTimeMillis();
			}
			if (System.currentTimeMillis() - keyPressedTime > 1000) {
				// to be sure the time is reset
				try {
					_keyRepeated(key);
				} catch (Exception e) {
					// #mdebug
					Logger.log("In key pressed:" + e.getMessage() + " "
							+ e.getClass().getName());
					e.printStackTrace();
					// #enddebug
				}
				keyPressedTime = Long.MAX_VALUE;
				keyReleasedTime = 0;
				return;
			}
		}

		if (screenList.size() == 0 || viewedIndex > screenList.size()
				|| viewedIndex < 0) { return; }
		// wlist key pressure cannot be handled in UIscreen
		// because it has no knowledge of it
		// it must be handled here
		if (wlist != null && wlist.isOpenedState() == true) {
			int ga = this.getGameAction(key);
			if (key == UICanvas.MENU_RIGHT || ga == Canvas.FIRE) {
				int selectedIndex = wlist.getSelectedIndex();
				getCurrentScreen().removePopup(wlist);
				change(selectedIndex);
				return;
			}
		}

		UIScreen writeScreen = (UIScreen) screenList.elementAt(viewedIndex);
		boolean selectionKept = writeScreen.keyPressed(key);
	}

	public void pointerPressed(int x, int y) {
		// #ifdef UI_DEBUG
		//@				Logger.log("pointerPressed: " + x + " " + y);
		// #endif

		// #ifndef RIM
		this.lastPressedTime = System.currentTimeMillis();
		inertia = 0;
		step = 0;
		// #endif
		this.pressedX = x;
		this.pressedY = y;
		this.draggedY = y;
		this.dragged = false;
		UIScreen paintedScreen = getCurrentScreen();
		if (paintedScreen == null) return;

		boolean hasSubMenu = false;
		try {
			// #ifndef RIM
			synchronized (UICanvas.getLock()) {
				// #endif
				this.lastGameAction = -1;
				hasSubMenu = handlePointEvent(x, y, paintedScreen, false);
				// #ifndef RIM
			}
			// #endif
		} catch (Exception e) {
			// #mdebug
			Logger.log("In pointerPressed");
			System.out.println(e.getMessage());
			e.printStackTrace();
			// #enddebug
		} finally {

		}

		if (hasSubMenu) {
			longPressRun = false;
			longPressedTask = initLongPress(paintedScreen);
			Utils.tasks.schedule(longPressedTask, 750);
		}
	}

	private TimerTask initLongPress(final UIScreen paintedScreen) {
		return new TimerTask() {
			public void run() {
				longPress(paintedScreen);
			}
		};
	}

	private void longPress(UIScreen paintedScreen) {
		try {
			synchronized (UICanvas.getLock()) {
				// #mdebug
				Logger.log("longPressed:");
				// #enddebug
				longPressRun = true;
				if (paintedScreen != null) {
					paintedScreen.keyPressed(UICanvas.MENU_LEFT);
				}
			}
		} catch (Exception e) {
			// #mdebug
			Logger.log("In long press:" + e.getMessage() + " "
					+ e.getClass().getName());
			e.printStackTrace();
			// #enddebug
		}
	}

	public void pointerDragged(final int x, final int y) {
		// #ifdef UI_DEBUG
		//@				Logger.log("pointerDragged: " + x + " " + y);
		// #endif
		int yOffset = 0;
		int xOffset = 0;
		final int totalYOffset;
		final int totalXOffset;
		xOffset = x - pressedX;
		yOffset = y - draggedY;
		draggedY = y;

		// if a little movement is made reset the longpressTask
		if (Math.abs(yOffset + bufferedOffsetY) >= 5
				|| Math.abs(xOffset + bufferedOffsetX) >= 5) {
			longPressedTask.cancel();
		}
		long now = System.currentTimeMillis();
		if (dragTimer == null && now - lastDragTime > dragInterval
				&& Math.abs(yOffset + bufferedOffsetY) > 10) {
			lastDragTime = now;
			yOffset += bufferedOffsetY;
			xOffset += bufferedOffsetX;
			bufferedOffsetY = 0;
			bufferedOffsetX = 0;
		} else {
			bufferedOffsetX += xOffset;
			bufferedOffsetY += yOffset;
			if (Math.abs(yOffset) >= 5 || Math.abs(xOffset) >= 5) {
				longPressedTask.cancel();
			}
			return;
		}
		totalYOffset = yOffset;
		totalXOffset = xOffset;
		if (Math.abs(totalYOffset) >= 1 || Math.abs(totalXOffset) >= 10) {
			dragged = true;
		}

		dragTimer = UICanvas.getTimer();
		dragTimer.schedule(new TimerTask() {
			public void run() {
				synchronized (UICanvas.getLock()) {
					doDrag(x, y, totalYOffset, totalXOffset);
				}
			}
		}, 10);
	}

	/**
	 * @param x
	 * @param y
	 * @param totalYOffset
	 * @param totalXOffset
	 */
	private void doDrag(final int x, final int y, int totalYOffset,
			int totalXOffset) {
		UIScreen paintedScreen = null;
		try {
			paintedScreen = getCurrentScreen();
			if (paintedScreen == null) return;

			// #ifndef RIM
			if (Math.abs(inertia) > 10) {
				while (Math.abs(inertia) > 10) {
					paintedScreen.addPaintOffset(inertia);
					paintedScreen.setDirty(true);
					paintedScreen.askRepaint();
					step = step > 0 ? step + 1 : step - 1;
					inertia = inertia + step;
				}
				return;
			}
			paintedScreen.addPaintOffset(totalYOffset);
			paintedScreen.setDirty(true);
			paintedScreen.askRepaint();
			if (Math.abs(bufferedOffsetY) > 5) {
				int temp = bufferedOffsetY;
				bufferedOffsetY = 0;
				doDrag(x, y, temp, totalXOffset);
				return;
			}
			// #endif
		} catch (Exception e) {
			// #mdebug
			//	Logger.log("in doDrag "  e.getClass());
			// #enddebug
		} finally {
			dragTimer = null;
		}

		int ka = -1;
		UIItem foundItem = findItem(x, y, paintedScreen, null);
		if (foundItem != null && this.dragged == false) {
			if (Math.abs(totalYOffset) >= 5) {
				paintedScreen.startDrag(foundItem);
			}
		}
		ka = -1;

		if (totalXOffset >= 15) {
			if (foundItem != null) {
				ka = getKeyCode(UICanvas.RIGHT);
				foundItem.keyPressed(ka);
			}
			return;
		}
		if (totalXOffset <= -15) {
			if (foundItem != null) {
				ka = getKeyCode(UICanvas.LEFT);
				foundItem.keyPressed(ka);
			}
			return;
		}
	}

	//	public void pointerDraggedOld(int x, int y) {
	//		try {
	//			// #ifndef RIM
	//			synchronized (UICanvas.getLock()) {
	//				// #endif
	//				// #mdebug
	//								// Logger.log("pointerDragged:" + x + " " + y + ":");
	//				// #enddebug
	//				UIScreen paintedScreen = getCurrentScreen();
	//				if (paintedScreen == null) return;
	//
	//				int yOffset = y - pressedY;
	//				int xOffset = x - pressedX;
	//				UIItem foundItem = findItem(x, y, paintedScreen, null);
	//				// if a little movement is made reset the longpressTask
	//				if (Math.abs(yOffset) > 5 || Math.abs(xOffset) > 5) {
	//					longPressedTask.cancel();
	//				}
	//
	//				if (foundItem != null && this.dragged == false) {
	//					if (yOffset >= 5 || yOffset <= -5) {
	//						paintedScreen.startDrag(foundItem);
	//					}
	//				}
	//
	//				int ka = -1;
	//				if (yOffset >= 20) {
	//					ka = this.getKeyCode(Canvas.DOWN);
	//					this.dragged = true;
	//					paintedScreen.keyRepeated(ka);
	//					this.pressedX = x;
	//					this.pressedY = y;
	//					return;
	//				}
	//				if (yOffset <= -20) {
	//					ka = this.getKeyCode(Canvas.UP);
	//					this.dragged = true;
	//					paintedScreen.keyRepeated(ka);
	//					this.pressedX = x;
	//					this.pressedY = y;
	//					return;
	//				}
	//				if (yOffset >= 15) {
	//					ka = this.getKeyCode(UICanvas.DOWN);
	//					this.dragged = true;
	//					paintedScreen.keyPressed(ka);
	//					this.pressedX = x;
	//					this.pressedY = y;
	//					return;
	//				}
	//				if (yOffset <= -15) {
	//					ka = this.getKeyCode(UICanvas.UP);
	//					this.dragged = true;
	//					paintedScreen.keyPressed(ka);
	//					this.pressedX = x;
	//					this.pressedY = y;
	//					return;
	//				}
	//
	//				ka = -1;
	//				if (xOffset >= 15) {
	//					this.dragged = true;
	//					if (foundItem != null) {
	//						ka = this.getKeyCode(UICanvas.RIGHT);
	//						foundItem.keyPressed(ka);
	//					}
	//					this.pressedX = x;
	//					this.pressedY = y;
	//					return;
	//				}
	//				if (xOffset <= -15) {
	//					this.dragged = true;
	//					if (foundItem != null) {
	//						ka = this.getKeyCode(UICanvas.LEFT);
	//						foundItem.keyPressed(ka);
	//					}
	//					this.pressedX = x;
	//					this.pressedY = y;
	//					return;
	//				}
	//				// #ifndef RIM
	//			}
	//			// #endif
	//		} catch (Exception e) {
	//			// #mdebug
	//						Logger.log("In key dragged");
	//						e.printStackTrace();
	//			// #enddebug
	//		}
	//	}

	/**
	 * <p>
	 * Handle the pointer pressure.
	 * </p>
	 * Determines where the pointer were pressed
	 * 
	 * @param key
	 *            The pressed key.
	 * 
	 * @return <code>true</code> if the screen will keep the selection
	 */
	public void pointerReleased(int x, int y) {
		// #ifdef UI_DEBUG
		//@				Logger.log("pointerReleased: " + x + " " + y);
		// #endif
		// #ifndef RIM
		this.lastReleaseTime = System.currentTimeMillis();
		// #endif
		try {
			// #ifndef RIM
			synchronized (UICanvas.getLock()) {
				// #endif
				longPressedTask.cancel();
				if (longPressRun == true) {
					this.dragged = false;
					longPressRun = false;
					return;
				}
				UIScreen paintedScreen = getCurrentScreen();
				if (paintedScreen == null) {
					this.dragged = false;
					return;
				}
				if (this.dragged == true) {
					this.dragged = false;
					paintedScreen.endDrag();
					//  #ifndef RIM
					long timeDiff = lastReleaseTime - lastPressedTime;
					int spaceDiff = y - pressedY;
					int speed = (int) ((100 * spaceDiff) / timeDiff);
					if (Math.abs(speed) > 30) {
						step = speed > 0 ? -1 : 1;
						inertia = speed + step;
						doDrag(x, y, 0, 0);
					}
					return;
					// #endif

				}
				// #ifdef UI_DEBUG
				//@				Logger.log("paintedScreen found:");
				// #endif
				handlePointEvent(x, y, paintedScreen, true);
				// #ifndef RIM
			}
			// #endif
		} catch (Exception e) {
			// #mdebug
			Logger.log("In pointerReleased");
			System.out.println(e.getMessage());
			e.printStackTrace();
			// #enddebug
		}
	}

	private boolean handlePointEvent(int x, int y, UIScreen paintedScreen,
			boolean up) {
		// check if it is the menucursor
		if (screenList.size() > 1 && up) {
			if (x < this.lag.getWidth() && y < this.lag.getHeight()) {
				// check if it is the screen right or left cursor
				int la = UICanvas.getInstance().getKeyCode(Canvas.LEFT);
				paintedScreen.keyPressed(la);
				return false;
			}
			if (x > canvasGraphics.getClipWidth() - 2 * this.lag.getWidth()
					&& y < this.lag.getHeight()) {
				// check if it is the screen right or left cursor
				int ra = UICanvas.getInstance().getKeyCode(Canvas.RIGHT);
				paintedScreen.keyPressed(ra);
				return false;
			}
		}

		UIItem foundItem = null;
		foundItem = findItem(x, y, paintedScreen, null);
		if (foundItem != null) {
			// #ifdef UI_DEBUG
			//@			try {
			//@				Logger.log("Found item in pointerReleased:");
			//@				Logger
			//@						.log("Pression time: " + releasedTime + " "
			//@								+ pressedTime);
			//@				if (foundItem instanceof UILabel) {
			//@					UILabel new_name = (UILabel) foundItem;
			//@					Logger.log("Label: " + new_name.getText()
			//@							+ foundItem.getWidth() + " "
			//@							+ foundItem.getHeight(canvasGraphics));
			//@				} else if (foundItem instanceof UILayout) {
			//@					UILayout new_name = (UILayout) foundItem;
			//@					Logger.log("Layout " + new_name.getWidth() + " "
			//@							+ new_name.getHeight(canvasGraphics));
			//@				} else if (foundItem instanceof UITextField) {
			//@					UITextField new_name = (UITextField) foundItem;
			//@					Logger.log("TextField " + new_name.getText());
			//@				} else if (foundItem instanceof UIPanel) {
			//@					UIPanel new_name = (UIPanel) foundItem;
			//@					Logger.log("Panel " + new_name.getWidth() + " "
			//@							+ new_name.getHeight(canvasGraphics));
			//@				} else if (foundItem instanceof UIMenu) {
			//@					Logger.log("Menu ");
			//@					UIMenu new_name = (UIMenu) foundItem;
			//@					try {
			//@						Logger.log(((UILabel) new_name.getItems().elementAt(0))
			//@								.getText()
			//@								+ " "
			//@								+ new_name.getWidth()
			//@								+ " "
			//@								+ new_name.getHeight(canvasGraphics));
			//@					} catch (Exception e) {
			//@						Logger.log(e.getMessage() + e.getClass().getName());
			//@					}
			//@				} else {
			//@					Logger.log("Found other ");
			//@					try {
			//@						Logger.log(foundItem.getWidth() + " "
			//@								+ foundItem.getHeight(canvasGraphics));
			//@					} catch (Exception e) {
			//@						Logger.log(e.getMessage() + e.getClass().getName());
			//@					}
			//@				}
			//@			} catch (Exception e) {
			//@			}
			// #endif

			// #ifndef RIM
			if (foundItem == paintedScreen.footerLeft) {
				if (up) {
					this._keyPressed(UICanvas.MENU_LEFT);
				}
				return false;
			}
			if (foundItem == paintedScreen.footerRight) {
				if (up) {
					this._keyPressed(UICanvas.MENU_RIGHT);
				}
				return false;
			}
			// #endif

			// First check if it's the the title
			if (foundItem == paintedScreen.titleLabel) {
				if (up) {
					this._keyRepeated(UICanvas.KEY_STAR);
				}
				return false;
			}

			// then if it is the screen menus list
			if (wlist != null && wlist.isOpenedState() == true) {
				int selectedIndex = wlist.getItems().indexOf(foundItem);
				if (selectedIndex >= 0) {
					if (up) {
						paintedScreen.removePopup(wlist);
						change(selectedIndex);
					}
					return false;
				}
			}

			// then check if its a menu
			UIMenu paintedMenu = paintedScreen.getMenu();
			if (paintedMenu != null && paintedMenu.contains(foundItem)) {
				if (!up) {
					foundItem.getContainer().setSelectedItem(foundItem);
					paintedScreen.askRepaint();
				} else {
					paintedScreen.firstPaint = true;
					paintedScreen.handleMenuKey(paintedMenu, UICanvas
							.getInstance().getKeyCode(UICanvas.FIRE));
				}
				return false;
			} else { // then a popup or an item
				Enumeration enPopup = paintedScreen.popupList.elements();
				while (enPopup.hasMoreElements()) {
					UIMenu ithMenu = (UIMenu) enPopup.nextElement();
					if (ithMenu.contains(foundItem)) {
						if (!up) {
							foundItem.getContainer().setSelectedItem(foundItem);
							paintedScreen.askRepaint();
						} else {
							paintedScreen.firstPaint = true;
							paintedScreen.handleMenuKey(ithMenu, UICanvas
									.getInstance().getKeyCode(UICanvas.FIRE));
						}
						return false;
					}
				}
			}
			if (foundItem.isFocusable() == false) return false;

			// if any kind of menu is opened return (menus are modals)
			if ((paintedMenu != null && paintedMenu.isOpenedState())
					|| paintedScreen.popupList.size() > 0) { return false; }

			if (foundItem.getContainer() != null) {
				if (!up) {
					// #ifdef UI_DEBUG
					//@					Logger.log("pressed");
					// #endif
					foundItem.getContainer().setSelectedItem(foundItem);
					paintedScreen.askRepaint();
					return true;
				} else {
					// #ifdef UI_DEBUG
					//@					Logger.log("released");
					// #endif
					paintedScreen.keyPressed(UICanvas.getInstance().getKeyCode(
							UICanvas.FIRE));
					return false;
				}
			}
		}
		return false;
	}

	private UIItem findItem(int x, int y, UIScreen paintedScreen,
			UIIContainer foundContainer) {
		UIItem foundItem = (UIItem) foundContainer;
		// if I find a container i must also check that the click
		// could be propagated to one of its subitems
		Enumeration en = paintedScreen.getPaintedItems().elements();
		while (en.hasMoreElements()) {
			UIItem ithItem = (UIItem) en.nextElement();
			int[] coors = ithItem.coors;
			int originalX = coors[0];
			int originalY = coors[1];
			int w = coors[2];
			int h = coors[3];

			// #mdebug
			//			Logger.log("OriginalX: " + originalX);
			//			Logger.log("OriginalY: " + originalY);
			//			Logger.log("w: " + w);
			//			Logger.log("h: " + h);
			//			Logger.log("x: " + x);
			//			Logger.log("y: " + y);
			// #enddebug

			if (x > originalX && x < originalX + w && y > originalY
					&& y <= originalY + h) {
				if ((foundContainer == null || ithItem.getContainer() == foundContainer)
						&& ithItem.isFocusable()) {
					foundItem = ithItem;
					break;
				}
			}
		}
		if (foundItem instanceof UIIContainer == true
				&& foundItem != foundContainer) foundItem = findItem(x, y,
				paintedScreen, (UIIContainer) foundItem);
		return foundItem;
	}

	public int getGameAction(int keyCode) {
		// some mobile phones throw an Exception when pressing
		// a key that is not associated to a game key
		// even if the keyCode is a valid key code
		int retVal = 0;
		try {
			retVal = super.getGameAction(keyCode);
		} catch (Exception e) {
			// #mdebug
			Logger.log("In getGameAction:" + keyCode + " "
					+ e.getClass().getName());
			// #enddebug
		}
		return retVal;
	}

	/**
	 * <p>
	 * Handle an event of keyRepeated.
	 * </p>
	 * If the key repeated is the '*', opens the "window list".
	 * 
	 * @param key
	 *            The pressed key.
	 */
	protected void keyRepeated(int key) {
		try {
			// #ifndef RIM
			synchronized (UICanvas.getLock()) {
				// #endif
				_keyRepeated(key);
				// #ifndef RIM
			}
			// #endif
		} catch (Exception e) {
			// #mdebug
			Logger.log("In key repeated");
			e.printStackTrace();
			// #enddebug
		} finally {

		}
	}

	/**
	 * @param key
	 */
	private void _keyRepeated(int key) {
		if (screenList.size() == 0 || viewedIndex < 0
				|| viewedIndex >= screenList.size()) { return; }
		UIScreen s0 = getCurrentScreen();
		if (key == Canvas.KEY_STAR && s0 != null && s0.isRollEnabled()) {
			if (s0.popupIsPresent(this.wlist) == false && screenList.size() > 1) {
				wlist = UIUtils.easyMenu("", 20, this.getClipHeight() / 4,
						getWidth() - 40, null);
				for (int i = 0; i < screenList.size(); i++) {
					UIScreen si = (UIScreen) screenList.elementAt(i);
					if (si != null) {
						UILabel um = new UILabel(si.getTitle());
						wlist.append(um);
					}
				}
				s0.addPopup(wlist);
				return;
			}
		}
		s0.keyRepeated(key);
	}

	private UIGraphics canvasGraphics = new UIGraphics(getGraphics());
	private int clipHeight = canvasGraphics.getClipHeight();

	/*
	 * The last pressed key; -1 if the pointer was pressed
	 */
	private int lastGameAction;

	/**
	 * Used by screen to ask a repaint.
	 * 
	 * @param screen
	 *            The screen to repaint. It may be {@code null} to indicate that
	 *            the current shown screen should be repainted. Otherwise, the
	 *            given screen will be checked if it is the screen currently
	 *            shown.
	 */
	protected void askRepaint(UIScreen screen, Graphics osGraphics) {
		if (screenList.size() == 0) { return; }
		if (screen == null) {
			screen = (UIScreen) _instance.screenList.elementAt(viewedIndex);
			screen.setDirty(true);
		} else if (!isActive()
				|| screen != (UIScreen) screenList.elementAt(viewedIndex)) { return; }
		try {
			if (screen.isFreezed() == false) {
				screen.setFreezed(true);
				if (osGraphics != null) {
					canvasGraphics = new UIGraphics(osGraphics);
				}
				UIGraphics g = canvasGraphics; // this.getGraphics();
				g.setFont(UIConfig.font_body);

				// in case a sizeChanged has changed it
				this.clipHeight = canvasGraphics.getClipHeight();

				int originalX = g.getTranslateX();
				int originalY = g.getTranslateY();
				int originalClipX = g.getClipX();
				int originalClipY = g.getClipY();
				int originalClipWidth = g.getClipWidth();
				int originalClipHeight = g.getClipHeight();
				// #mdebug
				// Logger.log(g.getTranslateX() + " " + g.getTranslateY() + " "
				// + g.getClipX() + " " + g.getClipY() + " "
				// + g.getClipWidth() + " " + g.getClipHeight() + " "
				// + this.getWidth() + " " + this.getHeight());
				// #enddebug

				setTabs();
				boolean needFlush = screen.paint(g);
				g.translate(originalX - g.getTranslateX(), originalY
						- g.getTranslateY());
				g.setClip(originalClipX, originalClipY, originalClipWidth,
						originalClipHeight);

				if (needFlush) {
					// #ifndef RIM
					flushGraphics();
					// #endif
				}
				screen.setFreezed(false);
			}
		} catch (Exception ex) {
			// #mdebug
			Logger.log("In painting UI");
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			// #enddebug
		}
	}

	private void setTabs() {
		UIScreen cs = this.getCurrentScreen();
		if (cs == null) return;
		if (this.screenList.size() <= 1 || cs.isRollEnabled() == false) {
			cs.setRightArrow(rag);
			cs.setLeftArrow(lag);
		} else {
			cs.setRightArrow(rab);
			cs.setLeftArrow(lab);
		}
	}

	public void open(UIScreen screen, boolean show, UIScreen returnScreen) {
		screen.setReturnScreen(returnScreen);
		this.open(screen, show);
	}

	/**
	 * Open and shows the given screen, optionally the screen can be shown
	 * immediately. If the screen is immediately shown, it's inserted at
	 * position 0, otherwise it's placed at the end of the list.
	 * 
	 * @param screen
	 *            the screen to show
	 * @param show
	 *            if true, the screen is immediately shown otherwise it's left
	 *            hidden
	 */
	public void open(UIScreen screen, boolean show) {
		UIScreen cs = getCurrentScreen();
		if (cs != null && cs.isRollEnabled() == false) return;
		if (!show || (wlist != null && wlist.isOpenedState() == true)) {
			if (!screenList.contains(screen)) screenList.addElement(screen);
		} else {
			if (screenList.contains(screen)) screenList.removeElement(screen);
			screenList.insertElementAt(screen, screenList.size());
		}
		if (wlist != null) {
			wlist.append(new UILabel(screen.getTitle()));
		}
		// just added a new screen
		cs = null;
		if (screenList.size() == 2 && ((cs = getCurrentScreen()) != null)) {
			cs.headerLayout.setDirty(true);
		}
		if (show == false) { return; }
		if (viewedIndex >= 0) {
			((UIScreen) screenList.elementAt(viewedIndex)).setDirty(true);
			((UIScreen) screenList.elementAt(viewedIndex)).hideNotify();
		}
		/* if the screen is the only one, it's painted immediately */
		screen.setDirty(true);
		viewedIndex = this.screenList.indexOf(screen);
		// changing index -> mandatory to repaint the background
		screen.firstPaint = true;
		screen.askRepaint();
		screen.showNotify();
	}

	public void replace(UIScreen oldScreen, UIScreen newScreen) {
		if (newScreen != null) {
			// to be sure it is opened the new screen
			oldScreen.setRollEnabled(true);
			this.open(newScreen, false);
			int idx = screenList.indexOf(newScreen);
			if (idx == -1) { return; }
			change(idx);
		}
		close(oldScreen);
	}

	/**
	 * Shows a screen by placing it on top of the stack. If the screen is not in
	 * the stack of open screens, this method fails silently.
	 * 
	 * @param screen
	 *            the screen to show
	 */
	public void show(UIScreen screen) {
		UIScreen cs = getCurrentScreen();
		if (cs != null && cs.isRollEnabled() == false) return;
		int idx = screenList.indexOf(screen);
		if (idx == -1) { return; }
		change(idx);
	}

	public void show(int idx) {
		UIScreen cs = getCurrentScreen();
		if (cs != null && cs.isRollEnabled() == false) return;
		if (idx >= 0 && idx < this.screenList.size()) change(idx);
	}

	/**
	 * Hides a screen and shows the next in the stack. If the screen is not the
	 * visible one, this method fails silently. If this screen is the only one
	 * in the stack, this method does nothing.
	 * 
	 * @param screen
	 *            the screen to hide
	 */
	public void hide(UIScreen screen) {
		if (screenList.size() < 2) { return; }
		UIMenu s0 = (UIMenu) screenList.elementAt(viewedIndex);
		if (s0 != screen) { return; }
		change(0);
	}

	/**
	 * Closes a screen, removing it from the stack and showing the next one. If
	 * the screen is not in the stack of open screens, or is the last of the
	 * stack, this method fails silently.
	 * 
	 * @param screen
	 *            the screen to close
	 * @return true if the screen has been closed, false otherwhise
	 */
	public boolean close(UIScreen screen) {
		int idx = screenList.indexOf(screen);
		boolean needReopen = this.viewedIndex == idx;
		if (idx == -1) { return false; }
		((UIScreen) screenList.elementAt(idx)).setDirty(true);
		((UIScreen) screenList.elementAt(idx)).hideNotify();
		screenList.removeElementAt(idx);
		// if all the screens have been removed the list could be empty
		if (idx <= viewedIndex) viewedIndex--;
		if (viewedIndex < 0) viewedIndex = 0;
		if (needReopen && screenList.size() > 0) {
			int newIdx = idx;
			if (screen.getReturnScreen() != null) newIdx = screenList
					.indexOf(screen.getReturnScreen());
			if (newIdx < 0 || newIdx >= screenList.size()) newIdx = screenList
					.size() - 1;
			change(newIdx);
		}
		if (wlist != null && wlist.getItems().size() > 0) {
			wlist.remove(idx);
		}
		return true;
	}

	/**
	 * Change the visible screen to the one given and redraws everything.
	 * 
	 * @param i
	 *            the id of the screen to change to
	 */
	private void change(int i) {
		UIScreen si = (UIScreen) screenList.elementAt(viewedIndex);
		si.setDirty(true);
		si.hideNotify();
		si = (UIScreen) screenList.elementAt(i);
		si.setDirty(true);
		si.firstPaint = true;
		viewedIndex = i;
		si.askRepaint();
		si.showNotify();
	}

	/**
	 * Called when the Canvas changes size or even rotation
	 */
	protected void sizeChanged(int w, int h) {
		synchronized (UICanvas.getLock()) {
			try {
				// sometimes the graphics can be null
				// if the screen cannot be painted
				// i.e. in N95
				// #mdebug
				Logger.log("resizing " + w + " " + h);
				// #enddebug
				UIScreen activeScreen = this.getCurrentScreen();
				// to initialize it at least at start
				if (activeScreen != null || screenList == null
						|| screenList.size() == 0) {
					this.canvasGraphics = new UIGraphics(this.getGraphics());
					this.clipHeight = canvasGraphics.getClipHeight();
				}
				Enumeration en = this.screenList.elements();
				while (en.hasMoreElements()) {
					UIScreen screen = (UIScreen) en.nextElement();
					screen.sizeChanged(w, h);
				}
				if (activeScreen != null) {
					activeScreen.invalidate(w, h);
					activeScreen.askRepaint();
				}

			} catch (Exception e) {
				// #mdebug
				System.out.println(e.getMessage());
				e.printStackTrace();
				// #enddebug
			}
		}
	}

	/**
	 * Gets the currently displayed Screen
	 * 
	 * @return The currently displayed screen or {@code null} if no screen is
	 *         available
	 */
	public UIScreen getCurrentScreen() {
		// if (!isShown()) { return null; }
		if ((screenList.size() > 0 && viewedIndex >= 0 && viewedIndex < screenList
				.size())) {
			return (UIScreen) screenList.elementAt(viewedIndex);
		} else {
			return null;
		}
	}

	/**
	 * Singleton factory
	 */
	public static synchronized UICanvas getInstance() {
		if (_instance == null) {
			_instance = new UICanvas();
		}
		return _instance;
	}

	/**
	 * Sets the display to use for the change-screen operations
	 * 
	 * @param _display
	 *            The display to use.
	 */
	// #ifndef RIM
	public static void setDisplay(Display _display) {
		// #endif
		display = _display;
	}

	/**
	 * Sets the key code to use for the two buttons with which menu will be
	 * activated.
	 * 
	 * @param left_key
	 *            the key code for the left button
	 * @param right_key
	 *            the key code for the right button
	 */
	public static void setMenuKeys(int left_key, int right_key) {
		MENU_LEFT = left_key;
		MENU_RIGHT = right_key;
	}

	/**
	 * Display a different
	 * 
	 * @param disp
	 */
	// #ifndef RIM
	public static void display(Displayable disp) {
		// #endif
		// #ifndef RIM
		if (display == null) {
			active = false;
			return;
		}
		/*
		* using getInstance() instead of _instance as it may be null so it
		* // gets
		* created
		*/
		if (disp == null) {
			active = true;
			display.setCurrent(getInstance());
			_instance.askRepaint(null, null);
		} else {
			active = false;
			display.setCurrent(disp);
		}
		// #endif
	}

	private boolean isActive() {
		return active;
	}

	/**
	 * Show an error screen, if multiple errors occur only append the message
	 * 
	 * @param type
	 *            The alert type as per {@link AlertType}
	 * @param title
	 *            Title of the screen
	 * @param text
	 *            Displayed error message
	 */
	public static void showAlert(int type, String title, String text) {
		AlertType nativeType = AlertType.WARNING;
		switch (type) {
			case Alerts.CONFIRMATION:
				nativeType = AlertType.CONFIRMATION;
				break;

			case Alerts.ERROR:
				nativeType = AlertType.ERROR;
				break;

			case Alerts.INFO:
				nativeType = AlertType.INFO;
				break;

			case Alerts.WARNING:
				nativeType = AlertType.WARNING;
				break;

			case Alerts.ALARM:
				nativeType = AlertType.ALARM;
				break;

			default:
				break;
		}

		// a native alert screen may be available in case the UI has not
		// been set already
		// #ifndef RIM
		Displayable cur = display.getCurrent();
		if (cur.equals(alert)) {
			alert.setString(alert.getString() + "\n" + text);
			return;
		}
		// #endif

		UIImage img;
		try {
			if (Alerts.INFO == type) {
				img = UIImage.createImage("/icons/warning.png");
			} else if (Alerts.INFO == type) {
				img = UIImage.createImage("/icons/warning.png");
			} else if (Alerts.INFO == type) {
				img = UIImage.createImage("/icons/error.png");
			} else {
				img = UIImage.createImage("/icons/error.png");
			}

		} catch (IOException e) {
			img = null;
		}

		UIFont bigFont = UIFont.getFont(UIFont.FACE_PROPORTIONAL,
				UIFont.STYLE_PLAIN, UIFont.SIZE_MEDIUM);

		UIScreen currentScreen = UICanvas.getInstance().getCurrentScreen();
		// if no screen is available it means the UI is not "ready"
		// in that case we use a native alert screen
		if (currentScreen != null) {
			UILabel titleLabel = new UILabel(img, title);
			UIMenu alertMenu = UIUtils.easyMenu("", 10, 20, UICanvas
					.getInstance().getWidth() - 20, titleLabel);
			titleLabel.setFont(bigFont);
			titleLabel.setFocusable(false);
			UILabel textLabel = new UILabel(text);
			alertMenu.append(textLabel);
			textLabel.setWrappable(true, alertMenu.getWidth() - 5);
			textLabel.setFont(bigFont);
			alertMenu.setSelectedIndex(1);
			UIGraphics cg = UICanvas.getInstance().canvasGraphics;
			int offset = (cg.getClipHeight() - alertMenu.getHeight(cg)) / 2;
			alertMenu.setAbsoluteY(offset);
			alertMenu.cancelMenuString = "";
			alertMenu.selectMenuString = "OK";
			currentScreen.addPopup(alertMenu);
		} else {
			// #ifndef RIM
			Alert alert = new Alert(title, text, img.getImage(), nativeType);
			alert.setType(nativeType);
			alert.setTimeout(Alert.FOREVER);
			display.setCurrent(alert, getInstance());
			// #endif
		}
	}

	/**
	 * Used to get predefined images "internal" to the UI. XXX: Maybe it is even
	 * better to get a cache for them.
	 * 
	 * @param imgName
	 * @return
	 */
	public static UIImage getUIImage(String imgName) {
		try {
			return UIImage.createImage(imgName);
		} catch (IOException e) {
			System.out.println("Impossible to get : " + imgName);

		}
		return null;
	}

	public static Timer getTimer() {
		if (timer == null) {
			timer = new Timer();
		}
		return timer;
	}

	/**
	 * @return the viewedIndex
	 */
	public int getViewedIndex() {
		return viewedIndex;
	}

	// #mdebug
	private static UIMenu logMenu = new UIMenu("log");

	public static void clearLog() {
		logMenu.clear();
	}

	public static void log(String logString) {
		UILabel uil = new UILabel(logString);
		logMenu.append(uil);
		logMenu.setAbsoluteX(10);
		logMenu.setAbsoluteY(20);
		logMenu.setWidth(UICanvas.getInstance().getWidth());
		UICanvas.getInstance().getCurrentScreen().addPopup(logMenu);
	}

	public static void log(Vector logStrings) {
		logMenu.setAbsoluteX(10);
		logMenu.setAbsoluteY(20);
		logMenu.setWidth(UICanvas.getInstance().getWidth());
		for (Enumeration en = logStrings.elements(); en.hasMoreElements();) {
			String logString = (String) en.nextElement();
			if (logString != null) {
				UILabel uil = new UILabel(logString);
				logMenu.append(uil);
			}
		}
		UIScreen cs = UICanvas.getInstance().getCurrentScreen();
		if (cs != null) cs.addPopup(logMenu);
	}

	// #enddebug

	public boolean hasQwerty() {
		// TODO Auto-generated method stub
		return qwerty;
	}

	public void setQwerty(boolean qwerty) {
		this.qwerty = qwerty;
	}

	public int getLastGameAction() {
		// TODO Auto-generated method stub
		return this.lastGameAction;
	}

	public void setCursors(UIImage lab, UIImage rab, UIImage lag, UIImage rag) {
		this.lab = lab;
		this.lag = lag;
		this.rab = rab;
		this.rag = rag;
	}

}
