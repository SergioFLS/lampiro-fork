package lampiro.screens;

import it.yup.dispatch.EventListener;
import it.yup.ui.UICanvas;
import it.yup.ui.UIGauge;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UIMenu;
import it.yup.ui.UIPanel;
import it.yup.ui.UIScreen;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

import javax.microedition.lcdui.Gauge;

public class WaitScreen extends UIScreen implements EventListener {

	private static ResourceManager rm = ResourceManager.getManager();

	private UILabel cmd_cancel = new UILabel(rm
			.getString(ResourceIDs.STR_CLOSE).toUpperCase());

	private UIPanel mainList = new UIPanel(true, false);

	UIGauge progress_gauge = new UIGauge(rm.getString(ResourceIDs.STR_WAIT),
			false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);

	public WaitScreen(String waitTitle, UIScreen returnScreen) {
		this.setMenu(new UIMenu(""));
		UIMenu menu = this.getMenu();
		menu.append(cmd_cancel);
		this.setTitle(waitTitle);
		this.append(mainList);
		mainList.addItem(progress_gauge);
		progress_gauge.start();
		this.setReturnScreen(returnScreen);
	}

	public void menuAction(UIMenu menu, UIItem cmd) {
		if (cmd == cmd_cancel) {
			stopWaiting();
			UICanvas.getInstance().close(this);
		}
	}

	private void stopWaiting() {
		progress_gauge.cancel();
		UICanvas.getInstance().close(this);
	}

	public void gotStreamEvent(String event, Object source) {
		try {
			synchronized (UICanvas.getLock()) {
				stopWaiting();
			}
		} catch (Exception e) {
			// #mdebug
			Logger.log("In handling cmd error:");
			e.printStackTrace();
			// #enddebug
		}
	}
}
