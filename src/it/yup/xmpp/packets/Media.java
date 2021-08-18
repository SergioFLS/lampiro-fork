/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: Media.java 1858 2009-10-16 22:42:29Z luca $
*/
package it.yup.xmpp.packets;

import it.yup.xml.Element;
import it.yup.xmpp.XmppConstants;

/**
 * A media data type
 */
public class Media {
	/**
	 * 
	 */

	public Object[] urisTypes;
	public int width = -1;
	public int height = -1;

	public Media(Element f) {
		Element[] uriEls = f.getChildrenByName(null, XmppConstants.URI);
		urisTypes = new Object[uriEls.length];
		String tempWidth = f.getAttribute("width");
		String tempHeight = f.getAttribute("height");
		if (tempWidth != null && tempHeight != null) {
			this.width = Integer.parseInt(tempWidth);
			this.height = Integer.parseInt(tempHeight);
		}
		for (int i = 0; i < uriEls.length; i++) {
			Element uriEl = uriEls[i];
			String type = uriEl.getAttribute(Stanza.ATT_TYPE);
			String uri = uriEl.getText();
			int mediaType;
			if (type.indexOf(XmppConstants.AUDIO) == 0) mediaType = XmppConstants.AUDIO_TYPE;
			else if (type.indexOf(XmppConstants.VIDEO) == 0) mediaType = XmppConstants.VIDEO_TYPE;
			else if (type.indexOf(XmppConstants.IMAGE) == 0) mediaType = XmppConstants.IMG_TYPE;
			else
				mediaType = XmppConstants.IMG_TYPE;
			urisTypes[i] = new Object[] { uri, new Integer(mediaType) };
		}
	}
}