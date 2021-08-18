package it.yup.util.encoders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

//#ifdef MIDP

import org.bouncycastle.util.encoders.Base64Encoder; 

//#endif

// #mdebug
import it.yup.util.log.Logger;

// #enddebug

public class Base64BufferedEncoder extends Base64Encoder {

	private ByteArrayOutputStream baos;
	private String oldBuffer = null;
	private String newBuffer = null;

	public Base64BufferedEncoder(ByteArrayOutputStream baos) {
		this.baos = baos;
	}

	private boolean ignore(char c) {
		return (c == '\n' || c == '\r' || c == '\t' || c == ' ');
	}

	private int nextI(int i, int finish) {
		while ((i < finish) && ignore(getCharAt(i))) {
			i++;
		}
		return i;
	}

	private int getDataLength() {
		int length = 0;
		if (oldBuffer != null) length += oldBuffer.length();
		if (newBuffer != null) length += newBuffer.length();
		return length;
	}

	private char getCharAt(int i) {
		if (oldBuffer != null) {
			if (i < oldBuffer.length()) return oldBuffer.charAt(i);
			else
				i -= oldBuffer.length();
		}
		return newBuffer.charAt(i);
	}
	
	public int decode(String data) {
		this.newBuffer = data;
		int length = 0;
		int end;
		for (end = getDataLength(); end > 0; end--)
			if (!ignore(getCharAt(end - 1))) break;

		int i = 0;
		int finish = end - 4;
		for (i = nextI(i, finish); i < finish; i = nextI(i, finish)) {
			byte b1 = decodingTable[getCharAt(i++)];
			i = nextI(i, finish);
			byte b2 = decodingTable[getCharAt(i++)];
			i = nextI(i, finish);
			byte b3 = decodingTable[getCharAt(i++)];
			i = nextI(i, finish);
			byte b4 = decodingTable[getCharAt(i++)];
			baos.write(b1 << 2 | b2 >> 4);
			baos.write(b2 << 4 | b3 >> 2);
			baos.write(b3 << 6 | b4);
			length += 3;
		}

		//		int newLength = (length * 4) / 3;
		//		if (oldBuffer == null) {
		//			oldBuffer = newBuffer.substring(newLength);
		//		} else {
		//			oldBuffer = (oldBuffer + newBuffer).substring(newLength);
		//		}
		//		newBuffer = null;

		try {
			int newLength = finish;
			if (oldBuffer == null) {
				oldBuffer = newBuffer.substring(newLength);
			} else {
				int oldLength = oldBuffer.length();
				if (newLength > oldLength) {
					oldBuffer = newBuffer.substring(newLength - oldLength);
				} else {
					oldBuffer = (oldBuffer + newBuffer).substring(newLength);
				}
			}
			newBuffer = null;
		} catch (StringIndexOutOfBoundsException e) {
			// #mdebug
			e.printStackTrace();
			Logger.log(e.getClass().toString());
			// #enddebug
		}
		newBuffer = null;
		return length;
	}

	public int endDecode() {
		int length = 0;
		try {
			if (getDataLength() >= 4) {
				length = decodeLastBlock(getCharAt(0), getCharAt(1),
						getCharAt(2), getCharAt(3));
			}
		} catch (IOException e) {
			// #mdebug
			e.printStackTrace();
			// #enddebug
		}
		newBuffer = null;
		return length;
	}

	private int decodeLastBlock(char c1, char c2, char c3, char c4)
			throws IOException {
		if (c3 == padding) {
			byte b1 = decodingTable[c1];
			byte b2 = decodingTable[c2];
			baos.write(b1 << 2 | b2 >> 4);
			return 1;
		}
		if (c4 == padding) {
			byte b1 = decodingTable[c1];
			byte b2 = decodingTable[c2];
			byte b3 = decodingTable[c3];
			baos.write(b1 << 2 | b2 >> 4);
			baos.write(b2 << 4 | b3 >> 2);
			return 2;
		} else {
			byte b1 = decodingTable[c1];
			byte b2 = decodingTable[c2];
			byte b3 = decodingTable[c3];
			byte b4 = decodingTable[c4];
			baos.write(b1 << 2 | b2 >> 4);
			baos.write(b2 << 4 | b3 >> 2);
			baos.write(b3 << 6 | b4);
			return 3;
		}
	}

}
