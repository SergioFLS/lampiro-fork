// #condition MIDP
/**
 * 
 */
package it.yup.ui.wrappers;

// #ifndef RIM
import javax.microedition.lcdui.Font;

// #endif

/**
 * @author luca
 * 
 */
public class UIFont {

	private Font font;

	public static final int STYLE_BOLD = javax.microedition.lcdui.Font.STYLE_BOLD;
	public static final int STYLE_ITALIC = javax.microedition.lcdui.Font.STYLE_ITALIC;
	public static final int STYLE_UNDERLINED = javax.microedition.lcdui.Font.STYLE_UNDERLINED;
	public static final int STYLE_PLAIN = javax.microedition.lcdui.Font.STYLE_PLAIN;

	public static final int SIZE_SMALL = javax.microedition.lcdui.Font.SIZE_SMALL;
	public static final int SIZE_MEDIUM = javax.microedition.lcdui.Font.SIZE_MEDIUM;
	public static final int SIZE_LARGE = javax.microedition.lcdui.Font.SIZE_LARGE;

	public static final int FACE_PROPORTIONAL = javax.microedition.lcdui.Font.FACE_PROPORTIONAL;
	public static final int FACE_SYSTEM = javax.microedition.lcdui.Font.FACE_SYSTEM;
	public static final int FACE_MONOSPACE = javax.microedition.lcdui.Font.FACE_MONOSPACE;


	public UIFont(Font font) {
		this.font = font;
	}

	public Font getFont() {
		// TODO Auto-generated method stub
		return font;
	}

	public static UIFont getFont(int face, int style, int size) {
// #ifndef RIM
		return new UIFont(Font.getFont(face, style, size));
		// #endif
	}

	public int getHeight() {
		return font.getHeight();
	}

	public int getFace() {
// #ifndef RIM
		return font.getFace();
		// #endif
	}

	public int getSize() {
// #ifndef RIM
		return font.getSize();
		// #endif
	}

	public int stringWidth(String string) {
// #ifndef RIM
		return font.stringWidth(string);
		// #endif
	}

	public int substringWidth(String str, int offset, int len) {
// #ifndef RIM
		return font.substringWidth(str, offset, len);
		// #endif
	}
}
