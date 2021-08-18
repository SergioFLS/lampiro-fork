// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UIConfig.java 2407 2011-01-20 23:38:44Z luca $
*/

package it.yup.ui;

import it.yup.ui.wrappers.UIFont;

public class UIConfig {
	
	// the color used Button Border Color
	public static int bb_color = 0x223377;
	
	// the color used Button Border Selected Color
	public static int bbs_color = 0x223377;
	
	// the color used Button Darker Border  Color
	public static int bdb_color = 0x223377;
	
	// the color used Button Lighter Border Color
	public static int blb_color = 0x425397;
	
	// the color used Button Darker Border Selected Color
	public static int bdbs_color = 0x223377;
	
	// the color used Button Lighter Border Selected Color
	public static int blbs_color = 0x425397;

	// the color used for textbox borders color
	public static int button_color = 0x2407db;
	
	// the color used for textbox borders color
	public static int button_selected_color = 0x4427fb;
	
	// the color used for textbox borders color
	public static int tbb_color = 0xb0c2c8;
	
	// the color used for textbox selection color
	public static int tbs_color = 0xddddff;
	
	// the color used for the inner color in UITextField
	public static int input_color = 0xFFFFFF;
	
	// the color used for the background in controls
	public static int selected_color = 0x888888;
	
	// the color used for the background in controls
	public static int bg_color = 0xddddff;
	
	// the color used for the font in controls
	public static int fg_color = 0x000000;
	
	// the color used for the background in the header and footer
	public static int header_bg = 0x567cfe;
	
	// the color used for the font in the header and footer
	public static int header_fg = 0xDDE7EC;
	
	public static int menu_title = 0xDDE7EC;
	
	public static int scrollbar_bg = 0x444444;
	public static int scrollbar_fg = 0xaaaaaa;
	
	// the color used for the outside menu border
	public static int menu_border = 0x223377;
	
	// the color used for inside menu color
	public static int menu_color = 0xacc2d8;
	
	// the color used for the outside menu border
	public static boolean menu_3d = false;
	
	public static byte menu_padding = 5;
	
	/** the width of the scrollBar */
	public static int scrollbarWidth = 7;

	/** the title font */
	public static UIFont font_title = UIFont.getFont(UIFont.FACE_PROPORTIONAL,
			UIFont.STYLE_BOLD, UIFont.SIZE_MEDIUM);

	public static UIFont gauge_body = UIFont.getFont(UIFont.FACE_PROPORTIONAL,
			UIFont.STYLE_BOLD, UIFont.SIZE_SMALL);

	public static UIFont font_body = UIFont.getFont(UIFont.FACE_PROPORTIONAL,
			UIFont.STYLE_PLAIN, UIFont.SIZE_MEDIUM);
	
	public static UIFont font_menu = UIFont.getFont(UIFont.FACE_PROPORTIONAL,
			UIFont.STYLE_PLAIN, UIFont.SIZE_MEDIUM);
	
	public static UIFont small_font = UIFont.getFont(UIFont.FACE_PROPORTIONAL,
			UIFont.STYLE_PLAIN, UIFont.SIZE_SMALL);
	
	// configurations strings for UIMenu and UIScreen default values
	public static String cancelMenuString = "CANCEL";
	public static String selectMenuString = "SELECT";
	public static String optionsString = "ACTIONS";
	public static String menuString = "MENU";

}
