/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: MediaRetrieveListener.java 1858 2009-10-16 22:42:29Z luca $
*/

package lampiro.screens;

import it.yup.ui.UICanvas;
import it.yup.ui.UILabel;
import it.yup.ui.UIScreen;
import it.yup.ui.wrappers.UIImage;
import it.yup.xmpp.MediaRetrieveListener;
import it.yup.xmpp.XmppConstants;
import it.yup.xmpp.packets.Media;

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

public class SMediaRetrieveListener implements MediaRetrieveListener {

	private UILabel objLabel;
	public UIScreen scr;

	private int maxWidth = -1;
	private int maxHeight = -1;

	public SMediaRetrieveListener(UIScreen scr, UILabel objLabel) {
		this.objLabel = objLabel;
		this.scr = scr;
	}

	/**
	 * @param media
	 *            the media file taht should be retrieved
	 * @param mediaType
	 *            the type of the file (i.e., Config.IMG_TYPE)
	 * @param data
	 *            the data to be represented
	 * @param synch
	 *            data could be retrieve synchronously (http) or asynchronously
	 *            (iq)
	 */
	public void showMedia(Media media, int mediaType, byte[] data, boolean synch) {
		UIImage img = null;
		if (mediaType == XmppConstants.IMG_TYPE) {
			img = UIImage.createImage(data, 0, data.length);
		} else if (mediaType == XmppConstants.AUDIO_TYPE) {
			img = UICanvas.getUIImage("/icons/mic.png");
		}

		// resize media
		int mWidth = media.width;
		int mHeight = media.height;

		// resize to fit screen
		int tempWidth = maxWidth != -1 ? maxWidth : UICanvas.getInstance()
				.getWidth() - 20;
		if (img.getWidth() >= tempWidth) {
			mHeight = (tempWidth * img.getHeight()) / img.getWidth();
			mWidth = tempWidth;
		}

		// resize to fit screen
		int tempHeight = maxHeight != -1 ? maxHeight : UICanvas.getInstance()
				.getHeight() - 20;
		if (img.getHeight() >= tempHeight) {
			mWidth = (tempHeight * img.getWidth()) / img.getHeight();
			mHeight = tempHeight;
		}

		if (mWidth > 0 && mHeight > 0 && mWidth != img.getWidth()
				&& mHeight != img.getHeight()) {
			img = img.imageResize(mWidth, mHeight, false);
		}

		this.objLabel.setImg(img);
		if (synch) {
			this.scr.askRepaint();
		} else {
			try {
				synchronized (UICanvas.getLock()) {
					this.scr.askRepaint();
				}
			} catch (Exception e) {
				// #mdebug
				Logger.log("Error in mediaretrieveListener" + e.getClass());
				// #enddebug
			}
		}
	}

	/**
	 * @param maxWidth
	 *            the maxWidth to set
	 */
	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	/**
	 * @return the maxWidth
	 */
	public int getMaxWidth() {
		return maxWidth;
	}

	/**
	 * @param maxHeight
	 *            the maxHeight to set
	 */
	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	/**
	 * @return the maxHeight
	 */
	public int getMaxHeight() {
		return maxHeight;
	}
}
