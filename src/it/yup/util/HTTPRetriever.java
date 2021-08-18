package it.yup.util;

import it.yup.transport.NetConnector;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;

// #ifndef MIDP
//@
//@import java.net.HttpURLConnection;
//@import java.net.URL;
//@
// #endif
// #ifdef MIDP

// #ifndef RIM
import javax.microedition.io.Connector;
// #endif
import javax.microedition.io.ContentConnection;

// #endif

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

public class HTTPRetriever {

	private String url;

	public HTTPRetriever(String url) {
		this.url = url;
	}

	public byte[] readAll() {
		//#ifdef MIDP
		ContentConnection c = null;
		//#endif
// #ifndef MIDP
//@				HttpURLConnection c = null;
		//#endif

		DataInputStream dis = null;
		byte[] data = null;
		try {
			//#ifdef MIDP
			c = (ContentConnection) NetConnector.open(this.url);
			int len = (int) c.getLength();
			dis = c.openDataInputStream();
			//#endif
// #ifndef MIDP
//@						c = (HttpURLConnection) (new URL(this.url)).openConnection();
//@						int len = (int) c.getContentLength();
//@						dis = new DataInputStream(c.getInputStream());
			//#endif
			if (len > 0) {
				data = new byte[len];
				dis.readFully(data);
			} else {
				int ch;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				while ((ch = dis.read()) != -1) {
					bos.write(ch);
				}
				data = bos.toByteArray();
			}
		} catch (Exception e) {
			// #mdebug
						Logger.log("In retrieving media2");
						System.out.println(e.getMessage());
						e.printStackTrace();
			// #enddebug
		} finally {
			try {
				if (dis != null)
					dis.close();
				//#ifdef MIDP
				if (c != null)
					c.close();
				//#endif
// #ifndef MIDP
//@								if (c != null) c.disconnect();
				//#endif

			} catch (Exception e) {
				// #mdebug
								Logger.log("In retrieving media2");
								System.out.println(e.getMessage());
								e.printStackTrace();
				// #enddebug
			}
		}
		return data;
	}
}
