// #condition MIDP
package it.yup.ui.wrappers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jzlib.ZInputStream;

import it.yup.ui.wrappers.UIFont;

public class UICustomFont extends UIFont {
	private class CharStruct {
		private byte width = 0;
		private byte height = 0;
		private byte offset_x = 0;
		private byte offset_y = 0;
		private byte advance = 0;

		private byte[] data = null;

		public CharStruct(InputStream is) {
			try {
				width = (byte) is.read();
				height = (byte) is.read();
				offset_x = (byte) is.read();
				offset_y = (byte) is.read();
				advance = (byte) is.read();
				ByteArrayOutputStream baos = new ByteArrayOutputStream(50);

				byte nTrans = (byte) is.read();
				baos.write(nTrans);
				if (nTrans > 0) {
					for (int i = 0; i < nTrans; i++) {
						byte ithTrans = (byte) is.read();
						baos.write(ithTrans);
						byte nColumns = (byte) is.read();
						baos.write(nColumns);
						for (int j = 0; j < nColumns; j++) {
							byte jThColumn = (byte) is.read();
							baos.write(jThColumn);
							byte nElem = (byte) is.read();
							baos.write(nElem);
							for (int k = 0; k < nElem * 2; k++) {
								byte readByte = (byte) is.read();
								baos.write(readByte);
							}
						}
					}
				}
				data = baos.toByteArray();
			} catch (Exception e) {
				// #mdebug
				e.printStackTrace();
				// #enddebug
			}

		}
	}

	private CharStruct[] charMap = new CharStruct[256];
	private byte size;
	private byte ascent;
	private byte descent;
	private byte charBlock = 10;

	private CharStruct getChar(char ithChar) {
		if (charMap.length > ithChar && charMap[ithChar] != null) return charMap[ithChar];
		else
			return charMap['x'];
	}

	public UICustomFont(String path) {
		super(null);
		InputStream is = UICustomFont.class.getResourceAsStream(path);
		ZInputStream zis = new ZInputStream(is);
		try {
			int c = 0;
			size = (byte) zis.read();
			ascent = (byte) zis.read();
			descent = (byte) zis.read();
			while (true) {
				c = zis.read();
				if (c < 0) break;
				charMap[c] = new CharStruct(zis);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getHeight() {
		return ascent;
	}

	public int getSize() {
		return UIFont.SIZE_MEDIUM;
	}

	public int stringWidth(String string) {
		return substringWidth(string, 0, string.length());
	}

	public int substringWidth(String str, int offset, int len) {
		int tempWidth = 0;
		for (int i = offset; i < offset + len; i++) {
			tempWidth += getChar(str.charAt(i)).advance;
		}
		return tempWidth;
	}

	public void drawString(UIGraphics g, String string, int x, int y, int anchor) {
		int orX = g.getTranslateX();
		int orY = g.getTranslateY();
		int vAnchor = UIGraphics.getVAnchor(anchor);
		int hAnchor = UIGraphics.getHAnchor(anchor);
		int sw = stringWidth(string);
		int newX = x;
		int newY = y;
		boolean update = false;
		if (hAnchor == UIGraphics.HCENTER) {
			newX = x - sw / 2;
			update = true;
		} else if (hAnchor == UIGraphics.RIGHT) {
			newX = x - sw;
			update = true;
		}
		if (vAnchor == UIGraphics.BOTTOM) {
			newY = y - this.getHeight();
			update = true;
		}
		if (update) {
			drawString(g, string, newX, newY, UIGraphics.LEFT | UIGraphics.TOP);
			return;
		}
		g.translate(x, y);
		if (string.length() > charBlock) {
			String leftString = string.substring(0, charBlock);
			String rightString = string.substring(charBlock, string.length());
			drawString(g, leftString, 0, 0, UIGraphics.TOP | UIGraphics.LEFT);
			g.translate(stringWidth(leftString), 0);
			drawString(g, rightString, 0, 0, UIGraphics.TOP | UIGraphics.LEFT);
			g.translate(orX - g.getTranslateX(), orY - g.getTranslateY());
			return;
		}
		int xOffset = 0;
		int index = 0;
		int color = g.getColor();
		int[] imgVals = new int[size * (ascent - descent)];
		short[] paintedIndex = new short[size * (ascent - descent)];
		for (int i = 0; i < string.length(); i++) {
			char ithChar = string.charAt(i);
			CharStruct ithCharMap = getChar(ithChar);
			byte[] data = ithCharMap.data;
			int bi = 0;
			short vOffset = (short) (ascent - (ithCharMap.height + ithCharMap.offset_y));
			byte transLength = data[bi++];
			if (data != null) {
				for (int i1 = 0; i1 < transLength; i1++) {
					byte transVal = data[bi++];
					int realTransVal = transVal << 5;
					realTransVal = realTransVal << 24;
					int realCol = realTransVal + color;
					g.setColor(realCol);
					byte ithTransLength = data[bi++];
					for (int l = 0; l < ithTransLength; l++) {
						byte colNumber = data[bi++];
						int computedOffset = colNumber + ithCharMap.offset_x;
						byte lthColLength = data[bi++];
						try {
							for (int k = 0; k < lthColLength; k++) {
								byte start = data[bi++];
								byte end = data[bi++];
								for (short m = start; m <= end; m++) {
									short computedIndex = (short) ((m + vOffset)
											* size + computedOffset);
									paintedIndex[index++] = computedIndex;
									imgVals[computedIndex] = realCol;
								}
							}
						} catch (Exception e) {
							// #mdebug
							e.printStackTrace();
							// #enddebug
						}
					}
				}
				g.drawRGB(imgVals, 0, size, xOffset, descent,
						ithCharMap.advance, ascent - descent, true);
				for (int j = 0; j < index; j++) {
					imgVals[paintedIndex[j]] = 0;
				}
				index = 0;
			}
			xOffset += ithCharMap.advance;
		}
		g.translate(orX - g.getTranslateX(), orY - g.getTranslateY());
	}

	public int getFace() {
		return UIFont.FACE_PROPORTIONAL;
	}

}
