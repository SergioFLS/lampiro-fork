/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: TaskListScreen.java 2310 2010-11-04 12:18:13Z luca $
*/

package lampiro.screens;

import it.yup.ui.UICanvas;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UIMenu;
import it.yup.ui.UIScreen;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.xmpp.Task;

/**
 * XXX: maybe not necessary anymore with screen switch
 */
public class TaskListScreen extends UIScreen {

	private static ResourceManager rm = ResourceManager.getManager();

	private UILabel cmd_cancel = new UILabel(rm.getString(
			ResourceIDs.STR_CANCEL).toUpperCase());

	public TaskListScreen(Task tasks[]) {
		setTitle(rm.getString(ResourceIDs.STR_TASKHISTORY_TITLE));
		for (int i = 0; i < tasks.length; i++) {
			UILabel ul = new UILabel(tasks[i].getLabel());
			ul.setFocusable(true);
			append(ul);
			ul.setStatus(tasks[i]);
		}

		setMenu(new UIMenu(""));
		UIMenu menu = getMenu();
		menu.append(cmd_cancel);
	}

	public void menuAction(UIMenu menu, UIItem cmd) {
		if (cmd == cmd_cancel) {
			UICanvas.getInstance().close(this);
		}
	}

	public void itemAction(UIItem item) {
		if (!(item instanceof UILabel)) { return; }
		if (item.getStatus() instanceof Task) {
			// #ifdef UI
			((Task) item.getStatus()).display();
			// #endif
		}
	}
}
