/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: AlbumScreen.java 1858 2009-10-16 22:42:29Z luca $
*/
package lampiro.screens;

import java.util.Enumeration;
import java.util.Vector;

import it.yup.ui.UIButton;
import it.yup.ui.UICanvas;
import it.yup.ui.UIHLayout;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UILayout;
import it.yup.ui.UIMenu;
import it.yup.ui.UIPanel;
import it.yup.ui.UIScreen;
import it.yup.ui.UISeparator;
import it.yup.ui.UIUtils;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.ui.wrappers.UIImage;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.util.storage.KeyStore;
import it.yup.util.storage.KeyStoreFactory;
import it.yup.xml.BProcessor;
import it.yup.xml.Element;
import it.yup.xmpp.XmppConstants;

/**
 * @author luca
 *
 */
public class AlbumScreen extends UIScreen {

	private static String ALBUM = "album";

	private static KeyStore album = KeyStoreFactory.getStore(ALBUM);

	private static Element albumEl;

	public static int getCount(int type) {
		synchronized (album) {
			String sType = (type == XmppConstants.IMG_TYPE ? "img" : "snd");
			Element child = albumEl.getChildByName(null, sType);
			if (child != null) return Integer.parseInt(child.getText());
			return 0;
		}
	}

	private UILabel cmd_capture_img = new UILabel(rm
			.getString(ResourceIDs.STR_CAPTURE_IMAGE));
	private UILabel cmd_capture_aud = new UILabel(rm
			.getString(ResourceIDs.STR_CAPTURE_AUDIO));

	private UIMenu actionMenu = new UIMenu("");

	private static AlbumScreen _instance = null;

	static {
		synchronized (album) {
			loadAlbum();
		}
	}

	private UIPanel albumPanel;

	private static ResourceManager rm = ResourceManager.getManager();

	private UILabel cmd_exit;

	private UILabel detailLabel = new UILabel(rm
			.getString(ResourceIDs.STR_SEE_DETAILS));

	private UILabel sendLabel = new UILabel(rm.getString(ResourceIDs.STR_SEND));

	private UILabel deleteLabel = new UILabel(rm
			.getString(ResourceIDs.STR_DELETE));

	private UIButton prevCmd = new UIButton(rm.getString(ResourceIDs.STR_PREV));

	private UIButton nextCmd = new UIButton(rm.getString(ResourceIDs.STR_NEXT));

	private Vector itemsToRemove = new Vector();

	/* 
	 * To avoid heavy CPU loads not all the album file are loaded when opening the album screen
	 * Some of them are loaded later when exploring the album;
	 * maxLoadFile is the maximum number of file that are to be loaded at start.
	 */
	private int maxLoadFile = 3;

	private int offset = 0;

	private static Element[] children;

	private UIHLayout buttonLayout = null;

	private UISeparator dummySeparator = new UISeparator(0);

	/*
	 * The resource for the contact that will be the default one to receive the selected
	 * file
	 */
	private String fullJid = null;

	public boolean keyPressed(int key) {
		boolean keepFocus = false;
		boolean rolled = false;
		if (albumPanel.getSelectedIndex() >= 0
				&& albumPanel.getItems().elementAt(
						albumPanel.getSelectedIndex()) == this.buttonLayout) keepFocus = true;
		if (keepFocus == false) rolled = RosterScreen.makeRoll(key, this);
		if (rolled == false) return super.keyPressed(key);
		return true;
	}

	public void menuAction(UIMenu menu, UIItem cmd) {
		if (cmd == cmd_capture_img) {
			RosterScreen.getInstance().captureMedia(null, XmppConstants.IMG_TYPE);
		} else if (cmd == cmd_capture_aud) {
			RosterScreen.getInstance().captureMedia(null, XmppConstants.AUDIO_TYPE);
		} else if (cmd == cmd_exit) {
			UICanvas.getInstance().close(this);
			_instance = null;
		} else if (cmd == deleteLabel) {
			itemAction(cmd);
		} else if (cmd == detailLabel || cmd == sendLabel) {
			itemAction(detailLabel);
		}
	}

	public void itemAction(UIItem c) {
		if (c == prevCmd) {
			offset -= maxLoadFile;
			if (offset < 0) {
				offset = (children.length - (children.length % maxLoadFile));
				if (offset == children.length) offset -= 3;
			}
			synchronized (album) {
				album.open();
				loadFiles();
				album.close();
			}
			this.buttonLayout.setSelectedItem(prevCmd);
		} else if (c == nextCmd) {
			offset += maxLoadFile;
			if (offset >= children.length) {
				offset = 0;
			}
			synchronized (album) {
				album.open();
				loadFiles();
				album.close();
			}
			this.buttonLayout.setSelectedItem(nextCmd);
		} else if (c == deleteLabel) {
			UIHLayout selLayout = (UIHLayout) this.albumPanel.getSelectedItem();
			String code = (String) selLayout.getStatus();
			AlbumScreen.deleteAlbum(code);
			// remove item and separator
			int index = albumPanel.getItems().indexOf(selLayout);
			albumPanel.removeItem(selLayout);
			albumPanel.removeItemAt(index);

			if (offset >= children.length && offset > 0) {
				offset -= this.maxLoadFile;
			}
			synchronized (album) {
				album.open();
				loadFiles();
				album.close();
			}

		} else if (c == detailLabel || c == sendLabel || c instanceof UIHLayout) {
			UIHLayout selLayout = (UIHLayout) this.albumPanel.getSelectedItem();
			String code = (String) selLayout.getStatus();
			album.open();
			Element[] children = albumEl.getChildrenByName(null, "file");
			for (int i = 0; i < children.length; i++) {
				Element ithChild = children[i];
				Element idEl = ithChild.getChildByName(null, "id");

				String fileId = idEl.getText();
				if (fileId.equals(code)) {
					Element nameEl = ithChild.getChildByName(null, "name");
					Element descEl = ithChild.getChildByName(null, "desc");
					Element typeEl = ithChild.getChildByName(null, "type");
					String fileName = nameEl.getText();
					String fileDesc = descEl.getText();
					byte[] fileData = album.load(fileId.getBytes());
					int mmType = Integer.parseInt(typeEl.getText());

					UIScreen newScreen;
					if (RosterScreen.isOnline()) {
						SendMMScreen mms = new SendMMScreen(fileData, fileName,
								null, fileDesc, mmType, fullJid);
						int oldIndex = mms.mainPanel.removeItem(mms.cmd_layout);
						mms.cmd_layout = UIUtils.easyCenterLayout(mms.cmd_send,
								90);
						mms.mainPanel.insertItemAt(mms.cmd_layout, oldIndex);
						mms.mainPanel.removeItem(mms.cmd_layout_send_save);
						newScreen = mms;
					} else {
						ShowMMScreen mms = new ShowMMScreen(fileData, fileName,
								fileDesc);
						mms.mainPanel.removeItem(mms.cmd_layout);
						newScreen = mms;
					}

					UICanvas.getInstance().open(newScreen, true);
					break;
				}
			}
			album.close();
		}
	}

	public static AlbumScreen getInstance() {
		if (_instance == null) {
			_instance = new AlbumScreen();
		}
		return _instance;
	}

	/* 
	 * The album is opened and the contact passed in the constructor
	 * will be the default one to receive the selected file
	 */
	public static AlbumScreen getInstance(String contactToSend) {
		AlbumScreen album = AlbumScreen.getInstance();
		album.fullJid = contactToSend;
		return album;
	}

	/**
	 * 
	 */
	private AlbumScreen() {
		cmd_exit = new UILabel(rm.getString(ResourceIDs.STR_CLOSE));
		setMenu(new UIMenu(""));
		UIMenu menu = getMenu();
		if (RosterScreen.getInstance().cameraOn) menu.append(cmd_capture_img);
		if (RosterScreen.getInstance().micOn) menu.append(cmd_capture_aud);
		menu.append(cmd_exit);
		this.setTitle(rm.getString(ResourceIDs.STR_MM_ALBUM));

		setupScreen();
	}

	private void setupScreen() {
		this.removeAll();
		albumPanel = new UIPanel(true, false);
		albumPanel.setMaxHeight(-1);
		albumPanel.setFocusable(true);
		albumPanel.setModal(true);
		this.append(albumPanel);

		actionMenu.removeAll();
		actionMenu.append(this.detailLabel);
		if (RosterScreen.isOnline()) actionMenu.append(this.sendLabel);
		actionMenu.append(this.deleteLabel);

		buttonLayout = new UIHLayout(3);
		prevCmd.setAnchorPoint(UIGraphics.HCENTER);
		nextCmd.setAnchorPoint(UIGraphics.HCENTER);
		buttonLayout.setGroup(false);
		buttonLayout.insert(prevCmd, 0, 50, UILayout.CONSTRAINT_PERCENTUAL);
		buttonLayout.insert(dummySeparator, 1, 20, UILayout.CONSTRAINT_PIXELS);
		buttonLayout.insert(nextCmd, 2, 50, UILayout.CONSTRAINT_PERCENTUAL);

		synchronized (album) {
			album.open();
			updateChildren();
			loadFiles();
			album.close();
		}
	}

	private static void updateChildren() {
		Element[] children = albumEl.getChildrenByName(null, "file");
		AlbumScreen.children = children;
	}

	private void loadFiles() {
		Enumeration en = itemsToRemove.elements();
		while (en.hasMoreElements()) {
			UIItem object = (UIItem) en.nextElement();
			this.albumPanel.removeItem(object);
		}
		itemsToRemove.removeAllElements();
		albumPanel.removeItem(buttonLayout);

		for (int i = offset; i < Math
				.min(children.length, offset + maxLoadFile); i++) {
			Element ithChild = children[i];
			Element idEl = ithChild.getChildByName(null, "id");
			String fileId = idEl.getText();
			UIHLayout ithLayout = new UIHLayout(2);
			ithLayout.setSubmenu(actionMenu);
			ithLayout.setFocusable(true);

			String fileName = "";
			int mmType = 0;
			Element typeEl = ithChild.getChildByName(null, "type");
			mmType = Integer.parseInt(typeEl.getText());
			Element nameEl = ithChild.getChildByName(null, "name");
			fileName = nameEl.getText();
			UIImage resImg = null;
			int imgWidth = -1;
			int imgHeight = -1;
			byte[] fileData = album.load(fileId.getBytes());
			// this happens when a file cannot be loaded 
			// XXX: delete the file in album ?
			if (fileData == null) continue;
			if (mmType == XmppConstants.IMG_TYPE) {
				UIImage convImage = null;
				convImage = UIImage.createImage(fileData, 0, fileData.length);
				imgWidth = convImage.getWidth();
				imgHeight = convImage.getHeight();
				resImg = convImage.imageResize(UICanvas.getInstance()
						.getWidth() / 2, -1, false);
			} else {
				resImg = UICanvas.getUIImage("/icons/mic.png");
			}
			UILabel ithLabel = new UILabel(resImg);
			ithLayout.insert(ithLabel, 0,
					UICanvas.getInstance().getWidth() / 2,
					UILayout.CONSTRAINT_PIXELS);
			String fileString = fileName + " " + fileData.length / 1000 + "Kb";
			if (mmType == XmppConstants.IMG_TYPE) {
				fileString += " ";
				fileString += (imgWidth + "x" + imgHeight);
			}
			UILabel filenameLabel = new UILabel(fileString);
			filenameLabel.setAnchorPoint(UIGraphics.HCENTER);
			ithLayout.insert(filenameLabel, 1, 100,
					UILayout.CONSTRAINT_PERCENTUAL);
			filenameLabel.setWrappable(true, UICanvas.getInstance().getWidth()
					- resImg.getWidth() - 20);
			ithLayout.setStatus(fileId);

			itemsToRemove.addElement(ithLayout);
			this.albumPanel.addItem(ithLayout);
			UISeparator sep = new UISeparator(2);
			sep.setFg_color(0x777777);
			this.albumPanel.addItem(sep);
			itemsToRemove.addElement(sep);
		}

		if (children.length == 0) {
			UILabel emptyLabel = new UILabel(rm
					.getString(ResourceIDs.STR_EMPTY_ALBUM));
			//emptyLabel.setFocusable(true);
			UIScreen currentScreen = UICanvas.getInstance().getCurrentScreen();
			if (currentScreen != null) emptyLabel.setWrappable(true,
					currentScreen.getWidth() - 10);
			this.albumPanel.addItem(emptyLabel);
			itemsToRemove.addElement(emptyLabel);
		}

		if (children.length > maxLoadFile) {
			albumPanel.addItem(buttonLayout);

			buttonLayout.setSelected(true);
			buttonLayout.setSelected(false);
			nextCmd.setSelected(false);
			prevCmd.setSelected(false);
			//		if (offset > 0) {
			buttonLayout.insert(prevCmd, 0, 50, UILayout.CONSTRAINT_PERCENTUAL);
			buttonLayout.setSelectedItem(prevCmd);
			//		} else
			//			buttonLayout.insert(dummySeparator, 0, 50,
			//					UILayout.CONSTRAINT_PERCENTUAL);

			//		if (offset + maxLoadFile < children.length) {
			buttonLayout.insert(nextCmd, 2, 50, UILayout.CONSTRAINT_PERCENTUAL);
			buttonLayout.setSelectedItem(nextCmd);
			//		} else
			//			buttonLayout.insert(dummySeparator, 2, 50,
			//					UILayout.CONSTRAINT_PERCENTUAL);
		}
		albumPanel.setSelectedIndex(-1);
		albumPanel.setFirstVisible(0);
		this.askRepaint();
	}

	/*
	 * This function add a new data to the multimedia album. Eventually it checks
	 * if the number of elements in the album exceeds the maximum allocated size.
	 * 
	 * @param fileData
	 *            the byte array that contain the data
	 * @param fileName
	 * 			  the name of the file to save
	 * @param fileData
	 *            the multimedia type of the file (Config.audioType or Config.imgType)
	 */
	public static void addAlbum(byte[] fileData, String fileName,
			String fileDescription, int mmType) {
		synchronized (album) {
			Element idChild = albumEl.getChildByName(null, "id");
			int id = Integer.parseInt(idChild.getText());

			album.open();
			String idCode = (ALBUM + id);
			album.store(idCode.getBytes(), fileData);
			idChild.removeAllElements();
			idChild.addText((id + 1) + "");
			Element fileEL = albumEl.addElement(null, "file");
			fileEL.addElementAndContent(null, "id", idCode);
			fileEL.addElementAndContent(null, "name", fileName);
			fileEL.addElementAndContent(null, "desc", fileDescription);
			fileEL.addElementAndContent(null, "type", mmType + "");

			String sType = (mmType == XmppConstants.IMG_TYPE ? "img" : "snd");
			Element child = albumEl.getChildByName(null, sType);
			if (child != null) {
				int count = Integer.parseInt(child.getText()) + 1;
				child.removeAllElements();
				child.addText(count + "");
			} else {
				albumEl.addElement(null, sType).addText("1");
			}

			saveAlbum();
			album.close();
			updateChildren();
		}
		if (AlbumScreen._instance != null
				&& UICanvas.getInstance().getScreenList().contains(
						AlbumScreen._instance)) {
			AlbumScreen._instance.setFreezed(true);
			AlbumScreen._instance.setupScreen();
			AlbumScreen._instance.setFreezed(false);
		}
	}

	public static void deleteAlbum(String fileCode) {
		synchronized (album) {
			Element[] children = albumEl.getChildrenByName(null, "file");
			for (int i = 0; i < children.length; i++) {
				Element ithChild = children[i];
				if (ithChild.getChildByName(null, "id").getText().compareTo(
						fileCode) == 0) {
					albumEl.removeChild(ithChild);
					album.open();
					album.delete(fileCode.getBytes());
					saveAlbum();
					album.close();
					break;
				}
			}
			updateChildren();
		}
	}

	private static void saveAlbum() {
		byte[] albumBytes = BProcessor.toBinary(albumEl);
		album.store(ALBUM.getBytes(), albumBytes);
	}

	private static void loadAlbum() {
		album.open();
		byte[] albumBytes = album.load(ALBUM.getBytes());
		if (albumBytes == null || albumBytes.length == 0) {
			// setup album and first id
			albumEl = new Element(ALBUM, ALBUM);
			albumEl.addElement(null, "id").addText("1");
			saveAlbum();
		} else
			albumEl = BProcessor.parse(albumBytes);
		album.close();
	}

}
