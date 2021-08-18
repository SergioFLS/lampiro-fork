/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: CommandListScreen.java 2336 2010-11-17 11:00:12Z luca $
*/

package lampiro.screens;

import it.yup.dispatch.EventDispatcher;
import it.yup.dispatch.EventQueryRegistration;
import it.yup.ui.UIButton;
import it.yup.ui.UICanvas;
import it.yup.ui.UIHLayout;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UIMenu;
import it.yup.ui.UIPanel;
import it.yup.ui.UIScreen;
import it.yup.ui.UIUtils;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.xmpp.CommandExecutor;
import it.yup.xmpp.Contact;
import it.yup.client.XMPPClient;
import it.yup.xmpp.CommandExecutor.CommandExecutorListener;


/**
 * XXX: maybe not necessary anymore with submenus
 */
public class CommandListScreen extends UIScreen implements
		CommandExecutorListener {

	private static ResourceManager rm = ResourceManager.getManager();

	private UILabel cmd_select = new UILabel(rm.getString(
			ResourceIDs.STR_EXECUTE).toUpperCase());
	private UILabel cmd_cancel = new UILabel(rm
			.getString(ResourceIDs.STR_CLOSE).toUpperCase());

	private UIButton cmd_close = new UIButton(rm
			.getString(ResourceIDs.STR_CLOSE));

	private Contact usr;

	private UIPanel mainList = new UIPanel(true, false);

	private WaitScreen ws = null;
	private EventQueryRegistration eqr = null;

	/*
	 * The chosen resource for this command   
	 */
	private String chosenResource;

	public CommandListScreen(Contact _usr, String chosenResource) {
		setMenu(new UIMenu(""));
		UIMenu menu = getMenu();
		menu.append(cmd_select);
		menu.append(cmd_cancel);

		setTitle(rm.getString(ResourceIDs.STR_CMDSCREEN_TITLE));
		usr = _usr;
		this.chosenResource = chosenResource;
		int buttonSize = Math.min(Math.max(150, (UICanvas.getInstance()
				.getWidth() * 3) / 4), UICanvas.getInstance().getWidth());

		UILabel avCommands = new UILabel(rm
				.getString(ResourceIDs.STR_AVAILABLE_COMMANDS)
				+ " " + _usr.getPrintableName());
		avCommands.setWrappable(true, buttonSize);
		mainList.addItem(UIUtils.easyCenterLayout(avCommands, buttonSize));

		for (int i = 0; i < usr.cmdlist.length; i++) {
			String[] cmd = usr.cmdlist[i];
			UIButton ithCommLabel = new UIButton(cmd[1]);
			ithCommLabel.setStatus(cmd);
			ithCommLabel.setFocusable(true);
			ithCommLabel.setWrappable(true, buttonSize - 15);
			mainList
					.addItem(UIUtils.easyCenterLayout(ithCommLabel, buttonSize));
		}
		if (usr.cmdlist.length == 0) {
			UILabel ithCommLabel = new UILabel(rm
					.getString(ResourceIDs.STR_NO_COMMAND));
			ithCommLabel.setFocusable(false);
			mainList.addItem(ithCommLabel);
			menu.remove(cmd_select);
		} else {
			UIHLayout firstCommandLayout = (UIHLayout) this.mainList.getItems()
					.elementAt(1);
			firstCommandLayout.setSelectedItem(firstCommandLayout.getItem(1));
		}
		mainList.addItem(UIUtils.easyCenterLayout(cmd_close, buttonSize));

		mainList.setMaxHeight(-1);
		this.append(mainList);
		this.setSelectedItem(mainList);
	}

	public void menuAction(UIMenu menu, UIItem cmd) {
		if (cmd == cmd_cancel) {
			UICanvas.getInstance().close(this);
		} else if (cmd == cmd_select) {
			this.itemAction(mainList.getSelectedItem());
		}
	}

	public boolean keyPressed(int kc) {
		if (super.keyPressed(kc)) return true;

		return RosterScreen.makeRoll(kc, this);
	}

	public void itemAction(final UIItem item) {
		if (item == null) return;
		if (item == cmd_close) {
			menuAction(null, cmd_cancel);
			return;
		} else if (item instanceof UIButton) {
			this.getMenu().remove(cmd_select);
			String[] selCmd = (String[]) item.getStatus();
			ws = new WaitScreen(this.getTitle(), this.getReturnScreen());
			eqr = EventDispatcher.addDelayedListener(ws, true);
			CommandExecutor cmdEx = null;
			XMPPClient client = XMPPClient.getInstance();
// #ifndef BLUENDO_SECURE
						cmdEx = new CommandExecutor(client.getRoster(), selCmd,
								chosenResource, this);
			// #endif
			UICanvas.getInstance().open(ws, true);
			RosterScreen.getInstance()._handleTask(cmdEx);
			cmdEx.setupCommand();
			UICanvas.getInstance().close(this);
		}
	}

	public void executed(Object screen) {
		if (eqr != null) {
			if (ws != null) ((UIScreen) screen).setReturnScreen(ws
					.getReturnScreen());
			EventDispatcher.dispatchDelayed(eqr, this);
			eqr = null;
		}
	}

	public boolean askClose() {
		return false;
	}
}
