/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: GoogleToken.java 2218 2010-10-04 09:37:39Z luca $
*/

package it.yup.xmlstream;

import it.yup.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

// #mdebug

import java.util.Enumeration;
import it.yup.util.log.Logger;

// #enddebug

// #ifdef MIDP

import javax.microedition.io.HttpConnection;

import it.yup.transport.NetConnector;

// #endif
// #ifndef MIDP
//@
//@import java.net.HttpURLConnection;
//@import java.net.URL;
//@
// #endif

public class GoogleToken extends Thread {
	private Object rendezVous;
	private String username;
	private String password;
	public String token = null;
	public boolean error = false;

	/**
	 * Use this object for asynchronously getting a GoogleToken
	 * 
	 * @param username
	 * @param password
	 * @param rendezVous
	 *            a rendez vous object on which call a wait() for being awaken
	 *            when the result is ready. Test token and error for reading the
	 *            result
	 */
	public GoogleToken(String username, String password, Object rendezVous) {
		this.username = username;
		this.password = password;
		this.rendezVous = rendezVous;
	}

	public void run() {
		synchronized (rendezVous) {
			try {
				token = GoogleToken.getToken(this.username, this.password);
				if (token == null)
					error = true;
			} finally {
				rendezVous.notify();
			}
		}
	}

	/**
	 * Get a GOOGLE-TOKEN. This method is blocking, use the object wrapper for
	 * getting the tokenin a separate thread
	 * 
	 * @param username
	 * @param password
	 * @return the token, null upon error
	 */
	public static String getToken(String username, String password) {
		String answer = null;
		try {
			// (1) Get a session ID
			// prepare the request
			String postData = "Email=" + encode(username) + "&Passwd="
					+ encode(password)
					+ "&PersistentCookie=false&source=googletalk&service=mail";
			// #debug 
						Logger.log("[postdata] " + postData);
			
			String conString = "https://www.google.com/accounts/ClientLogin";
			// #ifdef MIDP
			HttpConnection conn = (HttpConnection) NetConnector.open(conString);
			//HttpConnection conn = (HttpConnection) Connector.open("https://jabber.bluendo.com:9111/google-token");
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", ""
					+ postData.getBytes().length);
			OutputStream os = conn.openOutputStream();
			os.write(postData.getBytes());
			os.close();
			// #endif
// #ifndef MIDP
//@			HttpURLConnection conn = (HttpURLConnection) (new URL(conString)).openConnection();
//@			conn.setDoOutput(true);
//@			conn.setRequestProperty("Content-Type",
//@					"application/x-www-form-urlencoded");
//@			conn.setRequestProperty("Content-Length", ""
//@					+ postData.getBytes().length);
//@			OutputStream os = conn.getOutputStream();
//@			os.write(postData.getBytes());
//@			os.close();
			// #endif
			if (conn.getResponseCode() == 200) {
				// read the answer
				//#ifdef MIDP
				InputStream is = conn.openInputStream();
				//#endif
// #ifndef MIDP
//@				InputStream is = conn.getInputStream();
				//#endif
				StringBuffer buffer = new StringBuffer();
				int b;
				while ((b = is.read()) != -1) {
					buffer.append((char) b);
				}
				// splitting the lines
				Vector lines = Utils.tokenize(buffer.toString(), '\n');
				// Getting the value of "Auth=" (the last line)
				answer = ((String) lines.elementAt(2)).substring(5).trim();
				// #mdebug 
								Enumeration en = lines.elements();
								while (en.hasMoreElements()) {
									Logger.log("[auth] " + en.nextElement());
								}
				// #enddebug
			}
		} catch (IOException e) {
			// answer remains null
			// #debug 
						Logger.log("[GoogleToken] IOException" + e.getMessage());
		}
		return answer;
	}

	private static String encode(String s) {
		StringBuffer buf = new StringBuffer();
		byte[] bytes = Utils.getBytesUtf8(s);

		for (int i = 0; i < bytes.length; i++) {
			buf.append("%" + Integer.toHexString(bytes[i]));
		}
		return buf.toString();
	}
}
