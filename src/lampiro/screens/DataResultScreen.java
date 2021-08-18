/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: DataResultScreen.java 2329 2010-11-16 14:12:50Z luca $
*/

/**
 * 
 */
package lampiro.screens;

import it.yup.ui.UICanvas;
import it.yup.ui.UIHLayout;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UILayout;
import it.yup.ui.UIMenu;
import it.yup.ui.UIPanel;
import it.yup.ui.UIScreen;
import it.yup.ui.UISeparator;
import it.yup.ui.UITextField;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.xmlstream.BasicXmlStream;
import it.yup.xmpp.DataFormListener;
import it.yup.xmpp.MediaRetrieveListener;
import it.yup.xmpp.MediaRetriever;
import it.yup.client.XMPPClient;
import it.yup.xmpp.packets.DataForm;

import java.util.Hashtable;

import javax.microedition.lcdui.TextField;


/**
 * Class that shows the results in a data form.
 */
public class DataResultScreen extends UIScreen {

	private static ResourceManager rm = ResourceManager.getManager();

	private UILabel cmd_close = new UILabel(rm.getString(ResourceIDs.STR_CLOSE)
			.toUpperCase());
	// these are for <reported/> items
	private UILabel cmd_prev = new UILabel(rm.getString(ResourceIDs.STR_PREV)
			.toUpperCase());
	private UILabel cmd_next = new UILabel(rm.getString(ResourceIDs.STR_NEXT)
			.toUpperCase());

	/** The result to show. The value -1 means possible instructions */
	private int pos = 0;

	private UILabel si_instructions = new UILabel("");

	/** the data form */
	private DataForm df;

	private DataFormListener listener;

	UIHLayout mainLayout = new UIHLayout(3);
	UIPanel mainPanel = new UIPanel();

	private UIMenu desc_menu = new UIMenu("");

	private UILabel show_desc_label = new UILabel(rm.getString(
			ResourceIDs.STR_DESC).toUpperCase());

	public DataResultScreen(DataForm df, DataFormListener listener) {
		setTitle(df.title != null ? df.title : "");

		this.df = df;
		this.listener = listener;

		if (df.instructions != null) {
			si_instructions.setWrappable(true, 100);
			si_instructions.setText(df.instructions);
			//pos = -1;
		}

		UISeparator separator = new UISeparator(0);
		mainLayout.setGroup(false);
		mainLayout.insert(separator, 0, 3, UILayout.CONSTRAINT_PIXELS);
		mainLayout.insert(mainPanel, 1, 100, UILayout.CONSTRAINT_PERCENTUAL);
		mainLayout.insert(separator, 2, 3, UILayout.CONSTRAINT_PIXELS);
		this.append(mainLayout);
		mainPanel.setMaxHeight(-1);

		desc_menu.append(show_desc_label);

		setMenu(new UIMenu(""));
		UIMenu menu = getMenu();
		menu.append(cmd_close);

		showCurrent();
	}

	/**
	 * shows the currently selected result.
	 * 
	 */
	private void showCurrent() {

		if (df.results.size() <= pos) {
			/* XXX ??? error ??? */
			return;
		}

		mainPanel.removeAllItems();

		if (df.instructions != null) {
			mainPanel.addItem(new UILabel(rm
					.getString(ResourceIDs.STR_INSTRUCTIONS)));
			mainPanel.addItem(si_instructions);
		}

		Hashtable res = (Hashtable) df.results.elementAt(pos);

		for (int i = 0; i < df.fields.size(); i++) {
			DataForm.Field fld = (DataForm.Field) df.fields.elementAt(i);

			String val = (String) res.get(fld.varName);
			if (val == null) {
				/* ??? error */
				val = "";
			}
			String lbl = (fld.label != null ? fld.label : fld.varName);

			if (lbl.indexOf("fixed_") >= 0) {
				lbl = "";
			}

			UIItem ithItem = null;
			if (fld.type == DataForm.FLT_TXTMULTI
					|| fld.type == DataForm.FLT_FIXED
					|| fld.type == DataForm.FLT_TXTSINGLE) {
				UITextField uit = new UITextField(lbl, val, 1024,
						TextField.UNEDITABLE);
				mainPanel.addItem(uit);
				// must be done after append to have a screen for the uit
				uit.setWrappable(true);

				// get the images for media types
				if (fld.media != null) {
					UILabel objLabel = new UILabel(UICanvas
							.getUIImage("/icons/loading.png"));
					objLabel.setAnchorPoint(UIGraphics.HCENTER);
					objLabel.setFocusable(true);
					mainPanel.addItem(objLabel);
					MediaRetrieveListener mrl = new SMediaRetrieveListener(this,
							objLabel);
					XMPPClient client = XMPPClient.getInstance();
					BasicXmlStream stream = client.getXmlStream();
					MediaRetriever mr = new MediaRetriever(stream,this.listener
							.getFrom(), fld.media, mrl);
					mr.retrieveMedia();
				}

				ithItem = uit;
				//uit.setMaxHeight(50);
			} else if (fld.type != DataForm.FLT_HIDDEN) {
				if (lbl.length() > 0) {
					mainPanel.addItem(new UILabel(lbl));
				}
				ithItem = new UILabel(val);
				mainPanel.addItem(ithItem);
			}
			if (fld.desc != null && ithItem != null) {
				ithItem.setSubmenu(desc_menu);
			}
		}

		UIMenu menu = getMenu();
		menu.remove(cmd_prev);
		menu.remove(cmd_next);

		//int start = df.instructions == null ? 0 : -1;
		int start = 0;
		if (pos > start) {
			//menu.insert(1, cmd_prev);
			menu.append(cmd_prev);
		}
		if (pos < df.results.size() - 1) {
			menu.append(cmd_next);
		}
		this.askRepaint();
	}

	public boolean keyPressed(int kc) {
		if (super.keyPressed(kc)) return true;

		return RosterScreen.makeRoll(kc, this);
	}

	public void menuAction(UIMenu menu, UIItem cmd) {
		if (cmd == cmd_close) {
			listener.execute(DataFormListener.CMD_DESTROY);
			RosterScreen.getInstance()._handleTask(listener);
			// #ifdef UI
			UICanvas.getInstance().close(this);
			// #endif
			return;
		} else if (cmd == cmd_next) {
			pos++;
		} else if (cmd == cmd_prev) {
			pos--;
		} else if (cmd == show_desc_label) {
			DataFormScreen.openDescription(this, df);
		}

		showCurrent();
	}
}
