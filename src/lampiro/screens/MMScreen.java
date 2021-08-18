/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: MMScreen.java 1858 2009-10-16 22:42:29Z luca $
 */

package lampiro.screens;

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

import java.util.Vector;

import it.yup.ui.UICanvas;

// #ifndef RIM

import javax.microedition.lcdui.Canvas;

// #endif

import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.util.Utils;
import it.yup.util.Alerts;
import it.yup.client.Config;
import it.yup.xmpp.XmppConstants;

import javax.microedition.lcdui.Graphics;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;
import javax.microedition.media.control.VideoControl;

/*
 * MultimediaScreen 
 */
// #ifndef RIM
public class MMScreen extends Canvas {
	// #endif	

	private static ResourceManager rm = ResourceManager.getManager();

	/*
	 * The default jid for the user to send the multimedia file
	 */
	private String contactToSend = null;

	public MMScreen(String contactToSend) {
		this.contactToSend = contactToSend;
	}

	protected void paint(Graphics g) {
		int width = getWidth();
		int height = getHeight();

		// Draw a green border around the VideoControl.
		g.setColor(0x000000);
		g.drawRect(0, 0, width - 1, height - 1);
		g.drawRect(1, 1, width - 3, height - 3);
	}

	private Player getPlayer() {
		Player mPlayer = null;
		String platform = System.getProperty("microedition.platform");
		String firstString = "capture://video";
		String secondString = "capture://image";
		if (platform.toLowerCase().indexOf("nokia") != -1) {
			String temp = firstString;
			firstString = secondString;
			secondString = temp;
		}
		try {
			mPlayer = Manager.createPlayer(firstString);
		} catch (Exception ex1) {
			//#mdebug
			Logger.log("In setup video 1a" + ex1.getClass().getName() + "\n"
					+ ex1.getMessage());
			//#enddebug
			try {
				mPlayer = Manager.createPlayer(secondString);
			} catch (Exception e2) {
				//#mdebug
				Logger.log("In setup video 1b" + e2.getClass().getName() + "\n"
						+ e2.getMessage());
				//#enddebug
			}
		}
		return mPlayer;
	}

	public void showCamera() {
		try {
			Player mPlayer = null;
			try {
				//String camResolution= MMScreen.getVideoRes(true);
				mPlayer = getPlayer();
				mPlayer.realize();
			} catch (Exception e) {
				//#mdebug
				e.printStackTrace();
				Logger.log("In setup video 1" + e.getClass().getName() + "\n"
						+ e.getMessage());
				//#enddebug
			}
			if (mPlayer == null) {
				RosterScreen rs = RosterScreen.getInstance();
				UICanvas.getInstance().open(rs, true);
				UICanvas.display(null);
				rs.showAlert(Alerts.ERROR, rm.getString(ResourceIDs.STR_ERROR),
						rm.getString(ResourceIDs.STR_CAMERA_ERROR), null);
				return;
			}
			VideoControl mControl = (VideoControl) mPlayer
					.getControl("VideoControl");
			InnerMMScreen ics = new InnerMMScreen(mPlayer, mControl,
					XmppConstants.IMG_TYPE, contactToSend);
			UICanvas.display(ics);
			mPlayer.start();
			// #ifndef RIM
			repaint();
			// #endif
		} catch (Exception e) {
			// #mdebug
			e.printStackTrace();
			Logger.log("In starting player " + e.getClass().getName() + "\n"
					+ e.getMessage());
			//#enddebug
		}
	}

	public void showAudio() {
		// find the best audio format
		String prop = System.getProperty("audio.encodings");
		Vector encs = Utils.tokenize(prop, new String[] { " " }, false);
		String foundType = "amr";
		int typeIndex = 0;
		boolean found = false;
		for (int i = 0; !found && i < Config.audioTypes.length; i++) {
			for (int l = 0; !found && l < encs.size(); l++) {
				if (((String) encs.elementAt(l)).indexOf(Config.audioTypes[i]) >= 0) {
					foundType = ((String) Config.audioTypes[i]);
					typeIndex = i;
					found = true;
					break;
				}
			}
		}
		Player p = null;
		RecordControl rc;
		try {
			//p = Manager.createPlayer("capture://audio");
			p = Manager.createPlayer("capture://audio?encoding=" + foundType);
			p.realize();
		} catch (Exception e) {
			// #mdebug
			e.printStackTrace();
			Logger.log("In allocate audio player " + e.getClass().getName()
					+ "\n" + e.getMessage());
			//#enddebug
			RosterScreen.getInstance().setFreezed(false);
			UICanvas.getInstance().open(RosterScreen.getInstance(), true);
			UICanvas.display(null);
			return;
		}
		try {
			rc = (RecordControl) p.getControl("RecordControl");
		} catch (Exception e) {
			// #mdebug
			e.printStackTrace();
			Logger.log("In allocate audio player " + e.getClass().getName()
					+ "\n" + e.getMessage());
			//#enddebug
			return;
		}
		InnerMMScreen ics = new InnerMMScreen(p, rc, XmppConstants.AUDIO_TYPE,
				this.contactToSend);
		ics.setTypeIndex(typeIndex);
		UICanvas.display(ics);
		// #ifndef RIM
		repaint();
		// #endif
	}
}
