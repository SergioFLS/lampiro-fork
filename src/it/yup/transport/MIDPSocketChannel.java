// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: SocketChannel.java 2212 2010-09-30 10:22:06Z luca $
*/

package it.yup.transport;

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

import java.io.IOException;

import it.yup.transport.NetConnector;

import javax.microedition.io.StreamConnection;
import javax.microedition.io.SocketConnection;

public class MIDPSocketChannel extends BaseSocketChannel {

	protected String connectionUrl;
	
	protected StreamConnection connection;

	public MIDPSocketChannel(String connectionUrl,
			TransportListener transportListener) {
		super(transportListener);
		this.connectionUrl = connectionUrl;
		this.listener = transportListener;
		this.transportType = TRANSPORT_TYPE;
		inputStream = null;
		outputStream = null;
	}

	public void open() {

		exiting = false;

		// create the opener and start the connection
		Runnable starter = new Runnable() {
			public void run() {

				inputStream = null;
				outputStream = null;

				try {
					// #debug					
					Logger.log("Connecting to " + connectionUrl);
					String tempConnection = connectionUrl;
					connection = (SocketConnection) NetConnector.open(tempConnection);
					// #debug					
					Logger.log("Connected ");
					setupStreams(connection.openInputStream(), connection
							.openOutputStream());

					// start the sender after each new connection
					sender = new Sender(MIDPSocketChannel.this);
					sender.start();

					listener.connectionEstablished(MIDPSocketChannel.this);
				} catch (IOException e) {
					// #debug					
					Logger.log("Connection failed: " + e.getMessage());
					listener.connectionFailed(MIDPSocketChannel.this);
				} catch (Exception e) {
					// #debug		    		
					Logger.log("Unexpected exception: " + e.getMessage());
					listener.connectionFailed(MIDPSocketChannel.this);
					//		    		YUPMidlet.yup.reportException("Unexpected Exception on Channel start.", e, null);
				}
			}
		};
		new Thread(starter).start();
	}

	public void close() {
		if (pollAlive() == false)
			return;
		exiting = true;
		try {
			inputStream.close();
			outputStream.close();
			connection.close();
		} catch (IOException e) {
			// #mdebug 
			System.out.println("In closing strean");
			e.printStackTrace();
			// #enddebug
		} catch (Exception e) {
			// #mdebug 
			System.out.println("In closing strean");
			e.printStackTrace();
			// #enddebug
		}
	}
}
