/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UTFReader.java 1858 2009-10-16 22:42:29Z luca $
*/
package it.yup.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Wrapper of the input stream capable of reading UTF characters
 * (the default reader hangs at least on nokia phones)
 *
 */
public class UTFReader extends Reader {

	private InputStream is;
//	private byte buf[];
//	private int offset = -1;
//	private int available = -1;
	
	public UTFReader(InputStream is) {
		this.is = is;
//		this.buf = new byte[512];
	}

	public void close() throws IOException {
		is.close();
	}

	public int read(char[] arg0, int arg1, int arg2) throws IOException {
		throw new IOException("Unsupported method");
	}

	public int read(char[] arg0) throws IOException {
		throw new IOException("Unsupported method");
	}

	private int getByte() throws IOException {
		return 0xFF & is.read();

		//			System.out.println("CCCCC" + compressed);
		//			int b=-1;
		//			int charAvailable ;
		//			while ( (charAvailable = is.available()) <=0) {
		//				System.out.println("+++++:"+charAvailable );
		//				try {
		//					Thread.sleep(1000);
		//				} catch (InterruptedException e) {
		//					// TODO Auto-generated catch block
		//					e.printStackTrace();
		//				}
		//			}
		//			System.out.println("-----: "+ charAvailable);
		//			b=is.read();
		//			System.out.println("*****:" +b);
		//			return b;

		//			
		//			int b = -1;
		//			// buffered reading
		//			if (false && offset > 0 && offset < available) {
		//				// #mdebug
		//				System.out.println("+++:" + offset + "/" + available);
		//				// #enddebug
		//				b = buf[offset++];
		//			} else {
		//				available = is.available();
		//				available = 0;
		//				if (available > 0) {
		//					//					do {
		//					//						Logger.log("read");
		//					available = is.read(buf, 0,
		//							(available < buf.length) ? available : buf.length);
		//					//						if(available == -1) {
		//					//							try {
		//					////								Logger.log("tick");
		//					//								Thread.sleep(1000);
		//					//							} catch (InterruptedException e) {
		//					//								// TODO Auto-generated catch block
		//					//								e.printStackTrace();
		//					//							}
		//					//						}
		//					//					}while(available == -1);
		//					// #mdebug
		//					System.out.println(">>:" + available);
		//					// #enddebug
		//					b = buf[0];
		//					offset = 1;
		//				} else {
		//					// block waiting for the first char
		//					try {
		//						b = is.read();
		//					}
		//					catch (Exception e) {
		//						// #mdebug
		//						Logger.log("Aveva ragione Fabio:" + e.getClass()
		//								+ e.getMessage());
		//						throw new IOException(e.getClass().getName());
		//						// #enddebug
		//					}
		//
		//				}
		//			}
		//			return b & 0xFF;
	}


	public int read() throws IOException {
		int b;
		int ch = 0;
		b = getByte();
		
		if (b == 0xff) {
			throw new IOException("Invalid byte received on text stream: " + b);
			//return -1;
		} else if (b <= 0x07F) {
			ch = b;
		} else {
			byte b1 = 0;
			byte b2 = 0;
			byte b3 = 0;
			byte b4 = 0;

			if (b >= 0x0C2 && b <= 0x0DF) {
				b3 = (byte) (b & 0x1F);
				b4 = (byte) (getByte() & 0x3F);
			} else if (b >= 0x0E0 && b <= 0x0EF) {
				b2 = (byte) (b & 0x0F);
				b3 = (byte) (getByte() & 0x3F);
				b4 = (byte) (getByte() & 0x3F);
			} else if (b >= 0x0F0 && b <= 0x0F4) {
				b1 = (byte) (b & 0x07);
				b2 = (byte) (getByte() & 0x3F);
				b3 = (byte) (getByte() & 0x3F);
				b4 = (byte) (getByte() & 0x3F);
			}

			ch = (b1 << 18) + (b2 << 12) + (b3 << 6) + b4;
		}
		return ch;
	}
}
