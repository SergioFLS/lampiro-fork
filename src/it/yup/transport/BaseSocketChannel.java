/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: SocketChannel.java 2212 2010-09-30 10:22:06Z luca $
*/

package it.yup.transport;

// TODO: test native reader under android
import it.yup.util.UTFReader;
import it.yup.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.TimerTask;

//#ifdef MIDP

import org.bouncycastle.crypto.tls.AlwaysValidVerifyer;
import org.bouncycastle.crypto.tls.TlsProtocolHandler;

//#endif

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZInputStream;
import com.jcraft.jzlib.ZOutputStream;

public abstract class BaseSocketChannel extends BaseChannel {

	/** String identifying the transport type */
	public static final String TRANSPORT_TYPE = "DIRECT_SOCKET";

	/** Keepalive interval for XML streams */
	// Please note that if we are using WiFi this KEEP_ALIVE must be very short
	// Many cellphone WiFi implementations in fact hangs after 20-30s of inactivity,
	// and they cannot receive packets any more
	public long KEEP_ALIVE = 300 * 1000;

	protected TransportListener listener;

	// set to true when connection is closing
	protected boolean exiting = false;


	private TimerTask ka_task = null;

	public BaseSocketChannel(TransportListener transportListener) {
		this.listener = transportListener;
		this.transportType = TRANSPORT_TYPE;
		inputStream = null;
		outputStream = null;
	}

	public abstract void open();

	public abstract void close();

	public boolean isOpen() {
		return inputStream != null;
	}

	public InputStream getInputStream() {
		return this.inputStream;
	}

	public OutputStream getOutputStream() {
		return this.outputStream;
	}

	public void sendContent(byte[] packetToSend) {
		synchronized (packets) {
			packets.addElement(packetToSend);
			packets.notify();
		}

		if (ka_task != null) {
			ka_task.cancel();
		}

		ka_task = new TimerTask() {
			public void run() {
				/* utf-8 space */
				sendContent(new byte[] { 0x20 });
			}

		};

		Utils.tasks.schedule(ka_task, KEEP_ALIVE);

	}

	protected boolean pollAlive() {
		return !exiting;
	}

	public Reader getReader() {
		return new UTFReader(inputStream);
	}

	public void startCompression() {
		synchronized (packets) {
			inputStream = new ZInputStream(inputStream);
			outputStream = new ZOutputStream(outputStream,
					JZlib.Z_DEFAULT_COMPRESSION);
			((ZOutputStream) outputStream).setFlushMode(JZlib.Z_PARTIAL_FLUSH);
			//sender.exiting = true;
			//packets.notify();
		}
	}

	public void startTLS() throws IOException {
		//#ifdef MIDP
		synchronized (packets) {
			TlsProtocolHandler handler = new TlsProtocolHandler(inputStream,
					outputStream);
			handler.connect(new AlwaysValidVerifyer());
			outputStream = handler.getOutputStream();
			inputStream = handler.getInputStream();
		}
		//#endif
	}

}
