/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: CountInputStream.java 1858 2009-10-16 22:42:29Z luca $
*/

package it.yup.transport;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author luca
 *
 */
public class CountInputStream extends InputStream {

	private int count = 0;

	private InputStream inputStream;

	public CountInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	public int read(byte b[], int off, int len) throws IOException {
		int tempInt = inputStream.read(b, off, len);
		if (tempInt > 0) count += tempInt;
		return tempInt;
	}

	public long skip(long n) throws IOException {
		return inputStream.skip(n);
	}

	public int available() throws IOException {
		return inputStream.available();
	}

	public int read() throws IOException {
		int tempInt = inputStream.read();
		if (tempInt >= 0) count++;
		return tempInt;
	}

	public void close() throws IOException {
		inputStream.close();
	}

	public void mark(int i) {
		inputStream.mark(i);
	}

	public void reset() throws IOException {
		inputStream.reset();
	}

	public boolean markSupported() {
		return inputStream.markSupported();
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

}
