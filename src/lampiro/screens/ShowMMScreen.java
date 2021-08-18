/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: ShowMMScreen.java 1858 2009-10-16 22:42:29Z luca $
 */

package lampiro.screens;

import java.io.ByteArrayInputStream;
import java.util.Vector;
import it.yup.ui.UIButton;
import it.yup.ui.UICanvas;
import it.yup.ui.UIHLayout;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UIMenu;
import it.yup.ui.UIPanel;
import it.yup.ui.UIScreen;
import it.yup.ui.UITextField;
import it.yup.ui.UIUtils;
import it.yup.ui.wrappers.UIImage;

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.util.Utils;
import it.yup.client.Config;
import it.yup.xmpp.XmppConstants;

import javax.microedition.lcdui.TextField;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;

public class ShowMMScreen extends UIScreen {
	/**
	 * 
	 */
	protected static ResourceManager rm = ResourceManager.getManager();

	protected UIPanel mainPanel = new UIPanel(true, false);
	protected byte[] originalImage = null;

	protected UILabel cmd_exit = new UILabel(rm
			.getString(ResourceIDs.STR_CLOSE).toUpperCase());

	private UILabel playLabel = new UILabel(rm.getString(ResourceIDs.STR_PLAY)
			.toUpperCase());

	protected UITextField fileNameTextField;

	protected UITextField fileDescTextField;

	/*
	 * The default file description used; in case it is not modified the default
	 * file description is not sent
	 */
	String defaultFileDescription = "<" + rm.getString(ResourceIDs.STR_DESC)
			+ ">";

	protected UIButton cmd_save = new UIButton(rm
			.getString(ResourceIDs.STR_SAVE));

	private UILabel uil;

	/*
	 * the type of the multimedia file
	 */
	private int mmType;

	protected byte[] fileData;

	String fileType;

	/*
	 * The name of the file to show
	 */
	protected String fileName;

	protected String fileDescription;

	protected UIHLayout cmd_layout;

	public static int getFileType(String fileName) {
		Vector tokens = Utils.tokenize(fileName, '.');
		String fileType = (String) tokens.elementAt(tokens.size() - 1);
		int mmType = XmppConstants.IMG_TYPE;
		for (int i = 0; i < XmppConstants.AUDIO_TYPE; i++) {
			if (fileType.compareTo(Config.audioTypes[i]) == 0) {
				mmType = XmppConstants.AUDIO_TYPE;
				break;
			}
		}
		return mmType;
	}

	public ShowMMScreen(byte[] fileData, String fileName, String fileDescription) {
		int mmType = getFileType(fileName);

		this.fileName = fileName;
		this.fileDescription = fileDescription;
		this.mmType = mmType;
		this.fileData = fileData;
		init();
	}

	public ShowMMScreen(byte[] fileData, String fileType, int mmType) {
		this.mmType = mmType;
		this.fileType = fileType;
		this.fileData = fileData;
		init();

	}

	private void init() {
		UIImage resImg;
		int count = AlbumScreen.getCount(mmType);
		if (fileType == null && fileName != null) {
			Vector v = Utils.tokenize(fileName, '.');
			if (v.size() >= 2)
				fileType = (String) v.elementAt(1);
		}
		if (mmType == XmppConstants.IMG_TYPE) {
			setTitle(rm.getString(ResourceIDs.STR_IMAGE));
			UIImage convImage = null;
			convImage = UIImage.createImage(fileData, 0, fileData.length);
			fileNameTextField = new UITextField(rm
					.getString(ResourceIDs.STR_FILE_NAME), rm
					.getString(ResourceIDs.STR_IMAGE)
					+ count + "." + fileType, 255, TextField.ANY);
			resImg = convImage.imageResize(
					UICanvas.getInstance().getWidth() - 10, -1, false);
		} else {
			setTitle(rm.getString(ResourceIDs.STR_AUDIO));
			resImg = UICanvas.getUIImage("/icons/mic.png");
			fileNameTextField = new UITextField(rm
					.getString(ResourceIDs.STR_FILE_NAME), rm
					.getString(ResourceIDs.STR_AUDIO)
					+ count + "." + fileType, 255, TextField.ANY);
		}
		fileDescTextField = new UITextField(rm.getString(ResourceIDs.STR_DESC),
				defaultFileDescription, 255, TextField.ANY);
		if (fileName != null) {
			fileNameTextField.setText(fileName);
		}

		if (fileDescription != null) {
			fileDescTextField.setText(fileDescription);
		}

		UIMenu thisMenu = new UIMenu("");
		thisMenu.append(this.cmd_exit);
		this.setMenu(thisMenu);
		this.originalImage = fileData;
		this.append(mainPanel);
		mainPanel.setMaxHeight(-1);
		uil = new UILabel(resImg, "");
		uil.setFocusable(true);
		UIHLayout ehl = UIUtils.easyCenterLayout(uil, resImg.getWidth());
		if (mmType == XmppConstants.AUDIO_TYPE) {
			UIMenu sub = new UIMenu("");
			sub.append(playLabel);
			uil.setSubmenu(sub);
		}
		mainPanel.addItem(ehl);
		this.setSelectedItem(mainPanel);
		mainPanel.setSelectedItem(ehl);

		this.mainPanel.addItem(this.fileNameTextField);
		this.mainPanel.addItem(this.fileDescTextField);
		cmd_layout = UIUtils.easyCenterLayout(cmd_save, 120);
		this.mainPanel.addItem(cmd_layout);
	}

	public void menuAction(UIMenu menu, UIItem c) {
		if (c == cmd_exit) {
			UICanvas.getInstance().close(this);
			// UICanvas.getInstance().open(RosterScreen.getInstance(), true);
		} else if (c == this.playLabel) {
			itemAction(this.uil);
		}
	}

	public void itemAction(UIItem c) {
		if (c == this.uil && this.mmType == XmppConstants.AUDIO_TYPE) {
			this.playAudio();
		} else if (c == cmd_save) {
			AlbumScreen.addAlbum(this.fileData, fileNameTextField.getText(),
					fileDescTextField.getText(), mmType);
			AlbumScreen alb = AlbumScreen.getInstance();
			UICanvas.getInstance().close(this);
			UICanvas.getInstance().open(alb, true);
		}
	}

	public void showNotify() {
		// if (mmType == Config.audioType) playAudio();
	}

	public synchronized void playAudio() {
		// #mdebug
		Logger.log("playing audio file");
		// #enddebug
		Thread t = new Thread() {
			public void run() {
				try {
					synchronized (UICanvas.getLock()) {
						ByteArrayInputStream recordedInputStream = new ByteArrayInputStream(
								fileData);
						Player p2 = Manager.createPlayer(recordedInputStream,
								"audio/" + fileType);
						p2.prefetch();
						p2.start();
					}
				} catch (Exception e) {
					// #mdebug
					e.printStackTrace();
					Logger.log("In setup video 1" + e.getClass().getName()
							+ "\n" + e.getMessage());
					// #enddebug
				}
			}
		};
		t.start();

	}

	public void setMmType(int mmType) {
		this.mmType = mmType;
	}

	public int getMmType() {
		return mmType;
	}
}
