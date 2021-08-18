/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: AboutScreen.java 2329 2010-11-16 14:12:50Z luca $
*/

package lampiro.screens;

import it.yup.ui.UICanvas;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UIMenu;
import it.yup.ui.UIScreen;
import it.yup.ui.UIUtils;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.ui.wrappers.UIImage;
import it.yup.client.Config;

public class AboutScreen extends UIScreen {

	UIImage logo;

	private static String[] lines = {
			"Mobile Messaging",
			"",
// #ifndef GLIDER
						"(c) 2007-2010 Bluendo srl",
						"http://www.bluendo.com",
						"Source code is available at",
						"http://code.google.com/p/lampiro/",
			// #endif
			"",
			Config.getInstance().getProperty(Config.VERSION),
			"Available/Total memory:",
			Runtime.getRuntime().freeMemory() / 1000 + "/"
					+ Runtime.getRuntime().totalMemory() / 1000 + " Kb" };

	private static UILabel cmd_ok = new UILabel("OK");

	public AboutScreen() {
		setTitle("ABOUT");
		try {
// #ifndef GLIDER
						logo = UIImage.createImage("/icons/lampiro_icon.png");
			// #endif
		} catch (Exception ex) {
		}
		UILabel uimg = new UILabel(logo);
		uimg.setAnchorPoint(UIGraphics.HCENTER | UIGraphics.VCENTER);
		append(uimg);
		for (int i = 0; i < lines.length; i++) {
			UILabel ul = new UILabel(lines[i]);
			ul.setAnchorPoint(UIGraphics.HCENTER | UIGraphics.VCENTER);
			ul.setWrappable(true, (UICanvas.getInstance().getWidth()*2/3));
			append(ul);
		}
		setMenu(UIUtils.easyMenu("", -1, -1, -1, cmd_ok));
	}

	public void menuAction(UIMenu menu, UIItem cmd) {
		if (cmd == cmd_ok) {
			UICanvas.getInstance().show(RosterScreen.getInstance());
			UICanvas.getInstance().close(this);
		}
	}

}
