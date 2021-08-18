/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: Mediaretriever.java 1858 2009-10-16 22:42:29Z luca $
*/
package it.yup.xmpp;

//#ifdef MIDP

import org.bouncycastle.util.encoders.Base64;

//#endif
// #ifndef MIDP
//@
//@import it.yup.util.encoders.Base64;
//@
//#endif

import it.yup.util.HTTPRetriever;
import it.yup.util.Utils;
import it.yup.xml.Element;
import it.yup.xmlstream.BasicXmlStream;
import it.yup.xmpp.packets.Iq;
import it.yup.xmpp.packets.Media;

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

public class MediaRetriever extends IQResultListener {

	private Media media;
	private String address;
	private int mediaType = XmppConstants.IMG_TYPE;
	private boolean forceCid = false;

	/**
	 * The listener that will receive the data
	 */
	private MediaRetrieveListener mrl;
	private BasicXmlStream xmlStream;

	public MediaRetriever(BasicXmlStream xmlStream, String address,
			Media media, MediaRetrieveListener mrl) {
		this.address = address;
		this.media = media;
		this.mrl = mrl;
		this.forceCid = false;
		this.xmlStream=xmlStream;
	}

	public MediaRetriever(BasicXmlStream xmlStream, String address,
			Media media, MediaRetrieveListener mrl, boolean forceCid) {
		this(xmlStream,address, media, mrl);
		this.forceCid = forceCid;
	}

	public void handleError(Element e) {
		// #mdebug
				Logger.log("In retrieving cid");
		// #enddebug
	}

	public void handleResult(Element e) {
		Element data = e.getChildByName(null, XmppConstants.DATA);
		if (data == null) return;
		String text = data.getText();
		byte[] decodedData = Base64.decode(text);

		try {
			mrl.showMedia(media, mediaType, decodedData, false);
		} catch (Exception ex) {
			// #mdebug
						Logger.log("In retrieving media2");
						System.out.println(ex.getMessage());
						ex.printStackTrace();
			// #enddebug
		}
	}

	public void retrieveMedia() {
		for (int i = 0; i < media.urisTypes.length; i++) {
			Object[] uriType = (Object[]) media.urisTypes[i];
			String uri = (String) uriType[0];
			if (uri.indexOf("cid:") == 0 || forceCid) {
				mediaType = ((Integer) uriType[1]).intValue();
				cidRetrieve(uri);
				break;
			} else if (uri.indexOf("http://") == 0
					|| uri.indexOf("https://") == 0) {
				String httpUri = uri;
				mediaType = ((Integer) uriType[1]).intValue();
				httpRetrieve(httpUri);
			}
		}
	}

	private void cidRetrieve(String cidUri) {
		Iq iq = new Iq(this.address, Iq.T_GET);
		Element data = new Element(XmppConstants.NS_BOB, XmppConstants.DATA);
		iq.addElement(data);
		String uri = Utils.replace(cidUri, "cid:", "");
		data.setAttribute("cid", uri);
		iq.send(this.xmlStream,this);
	}

	private void httpRetrieve(String httpUri) {
		byte[] data = (new HTTPRetriever(httpUri)).readAll();
		if (data != null) {
			mrl.showMedia(media, mediaType, data, true);
		}
	}
}
