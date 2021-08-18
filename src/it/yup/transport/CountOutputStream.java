/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: CountOutputStream.java 1858 2009-10-16 22:42:29Z luca $
*/
package it.yup.transport;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author luca
 *
 */
public class CountOutputStream extends OutputStream {
	private int count = 0;
	private OutputStream outputStream;

	public CountOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public void flush() throws IOException {
		outputStream.flush();
	}

	public void close() throws IOException {
		outputStream.close();
	}

	public void write(int i) throws IOException {
		outputStream.write(i);
		count++;
	}

	public void write(byte b[]) throws IOException {
		write (b,0,b.length);
	}

	public void write(byte b[], int off, int len) throws IOException {
		outputStream.write(b, off, len);
		count += len;
	}
	
	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

}
