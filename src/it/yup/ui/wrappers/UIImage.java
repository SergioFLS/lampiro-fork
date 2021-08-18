// #condition MIDP
package it.yup.ui.wrappers;

import java.io.IOException;

// #ifndef RIM

import javax.microedition.lcdui.Image;

// #endif

public class UIImage {

// #ifndef RIM
			private Image img = null;
		
	// #endif

// #ifndef RIM
					public UIImage(Image img) {
		// #endif
		this.img = img;
	}

// #ifndef RIM
					public Image getImage() {
		// #endif
		return img;
	}

// #ifndef RIM
					protected void setImg(Image newImg) {
		// #endif
		this.img = newImg;
	}

	public int getWidth() {
		return img.getWidth();
	}

	public int getHeight() {
		return img.getHeight();
	}

	public static UIImage createImage(String name) throws IOException {
// #ifndef RIM
						return new UIImage(javax.microedition.lcdui.Image.createImage(name));
		// #endif
	}

	public static UIImage createImage(int width, int height) {
// #ifndef RIM
						return new UIImage(Image.createImage(width, height));
		// #endif
	}

	public static UIImage createImage(byte[] imageData, int imageOffset,
			int imageLength) {
// #ifndef RIM
						return new UIImage(Image.createImage(imageData, imageOffset,
								imageLength));
		// #endif
	}

	public UIImage imageResize(int finWidth, int finHeight, boolean grayScale) {
		int srcWidth = img.getWidth();
		int srcHeight = img.getHeight();
		if (finHeight == -1) finHeight = finWidth * srcHeight / srcWidth;
// #ifndef RIM
						int out[] = new int[finHeight * finWidth];
						rescaleImg(finWidth, finHeight, srcWidth, srcHeight, out);
						if (grayScale) {
							grayfy(out);
						}
						Image tmpImg = Image.createRGBImage(out, finWidth, finHeight, true);
						return new UIImage(tmpImg);
		// #endif
	}

	private void grayfy(int[] out) {
		for (int i = 0; i < out.length; i++) {
			int newVal = out[i];
			int alpha = (newVal & 0xff000000);
			newVal = (((newVal & 0xff0000) >> 16) * 30) / 100
					+ (((newVal & 0xff00) >> 8) * 59) / 100
					+ (((newVal & 0xff)) * 11) / 100;
			newVal = newVal * 0x10101 + alpha;
			out[i] = newVal;
		}
	}

	// #ifndef RIM_5.0
		private void rescalaArray(int out[], int[] ini, int x, int y, int x2,
				int y2, int destXOrigin, int destYOrigin, int finalWidth) {
			for (int yy = 0; yy < y2; yy++) {
				int dy = yy * y / y2;
				for (int xx = 0; xx < x2; xx++) {
					int dx = xx * x / x2;
					int index = (finalWidth * (yy + destYOrigin)) + xx
							+ destXOrigin;
					out[index] = ini[(x * dy) + dx];
				}
			}
		}
	
		private void rescaleImg(int finWidth, int finHeight, int srcWidth,
				int srcHeight, int[] out) {
			int rgb[] = new int[50 * 50];
			for (int srcXor = 0; srcXor < srcWidth; srcXor += 50) {
				for (int srcYor = 0; srcYor < srcHeight; srcYor += 50) {
					int srcBW = Math.min(50, srcWidth - srcXor);
					int srcBH = Math.min(50, srcHeight - srcYor);
					float dstXor = ((float) (srcXor * finWidth)) / srcWidth;
					float dstYor = ((float) (srcYor * finHeight)) / srcHeight;
					float dstBW = ((float) (srcBW * finWidth)) / srcWidth;
					float dstBH = ((float) (srcBH * finHeight)) / srcHeight;
					int dstBWI = (int) ((dstBW + dstXor) - ((srcXor * finWidth) / srcWidth));
					int dstBHI = (int) ((dstBH + dstYor) - ((srcYor * finHeight) / srcHeight));
// #ifndef RIM
						img.getRGB(rgb, 0, srcBW, srcXor, srcYor, srcBW, srcBH);
	// #endif
					rescalaArray(out, rgb, srcBW, srcBH, dstBWI, dstBHI,
							(int) dstXor, (int) dstYor, finWidth);
				}
			}
		}
	
	// #endif

	public UIGraphics getGraphics() {
		// #ifndef RIM
						return new UIGraphics(this.img.getGraphics());
		// #endif
	}

	public void getRGB(int[] rgbData, int offset, int scanlength, int x, int y,
			int width, int height) {
		this.img
		// #ifndef RIM
										.getRGB
		// #endif
		(rgbData, offset, scanlength, x, y, width, height);
	}

	public static UIImage createRGBImage(int[] rgb, int width, int height,
			boolean processAlpha) {
		// #ifndef RIM
						return new UIImage(Image.createRGBImage(rgb, width, height,
								processAlpha));
		// #endif 

	}
}
