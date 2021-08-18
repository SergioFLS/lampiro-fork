// #condition MIDP

/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: NetworkConsumer.java 2218 2010-10-04 09:37:39Z luca $
*/


package it.yup.util.log;

import it.yup.util.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

public class NetworkConsumer extends Thread implements LogConsumer {

	private Vector messages = new Vector();
	private boolean active = true;
	
	public NetworkConsumer()
	{
		start();
	}
	
	public void run() {
		try {
			SocketConnection conn = (SocketConnection) Connector.open("socket://localhost:1234");
			OutputStream os = conn.openOutputStream();
			
			while(active) {
				synchronized (messages) {
					try {
						messages.wait();
						if(messages.size()>0) {
							String message = (String) messages.firstElement();
							messages.removeElementAt(0);
							os.write(Utils.getBytesUtf8(message));
							os.write('\n');
							os.flush();
						}
					} catch (InterruptedException e) {
						// ignore
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			active = false;
		}
	}
	
	public void gotMessage(String message, int level) {
		synchronized (messages) {
			if(active) {
				messages.addElement(message);
				messages.notify();
			}
		}

	}

	public void setExiting() {
		synchronized (messages) {
			this.active = false;
			messages.notify();
		}	
	}
	
}
