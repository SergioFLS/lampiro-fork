package it.yup.xmpp;

import it.yup.xmpp.packets.Media;

public interface MediaRetrieveListener {

	void showMedia(Media media, int mediaType, byte[] decodedData, boolean sync);

}
