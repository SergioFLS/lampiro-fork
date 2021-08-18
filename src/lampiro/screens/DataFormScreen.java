/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: DataFormScreen.java 2329 2010-11-16 14:12:50Z luca $
*/

/**
 * 
 */
package lampiro.screens;

import java.util.Enumeration;
import java.util.Vector;

import it.yup.dispatch.EventDispatcher;
import it.yup.dispatch.EventQueryRegistration;
import it.yup.ui.UIButton;
import it.yup.ui.UICanvas;
import it.yup.ui.UICheckbox;
import it.yup.ui.UICombobox;
import it.yup.ui.UIHLayout;
import it.yup.ui.UIItem;
import it.yup.ui.UIJidCombo;
import it.yup.ui.UILabel;
import it.yup.ui.UILayout;
import it.yup.ui.UIMenu;
import it.yup.ui.UIPanel;
import it.yup.ui.UIScreen;
import it.yup.ui.UISeparator;
import it.yup.ui.UITextField;
import it.yup.ui.UIUtils;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.util.Utils;
import it.yup.xmlstream.BasicXmlStream;
import it.yup.xmpp.Contact;
import it.yup.xmpp.DataFormListener;
import it.yup.xmpp.MediaRetrieveListener;
import it.yup.xmpp.MediaRetriever;
import it.yup.client.XMPPClient;
import it.yup.xmpp.packets.DataForm;
import javax.microedition.lcdui.TextField;
import it.yup.xmpp.CommandExecutor.CommandExecutorListener;



/**
 * <p>
 * This class handles the data form input from the user, rendering a given Data
 * Form using the base controls offered by J2ME UI. The input result is then
 * sent to a DataFormListener that handles the form outcome.
 * </p>
 * 
 * <p>
 * DataForms are rendered as follows.
 * <ul>
 * <li><b>hidden</b>: are skipped.</li>
 * <li><b>boolean</b>: are rendered with a ChoiceGroup flagged with "MULTIPLE"
 * and with a single choice item that may be checked (true) or unchecked
 * (false).</li>
 * <li><b>list-multi and list-single</b>: they show a button that opens a
 * separate List, List is "EXCLUSIVE" (a single voice can be selected) or
 * "MULTIPLE" (more than one voice selected) resp. for list-single and
 * list-multi.</li>
 * <li><b>jid-single</b>, <b>jid-multi</b>, <b>text-single</b>,
 * <b>text-multi</b>, <b>text-private</b>, <b>fixed</b>: are shown as a
 * single TextField, *-multi are split on '\n' chars when sending data;
 * text-private are flagged as PASSWORD fields, jid-single are flagged as
 * EMAILADDRESS fields. fixed are uneditable.</li>
 * </ul>
 * 
 * All fields have a Label before if the DataForm field has one. Commands are
 * placed on the menu. Instructions, if present, make a command "instructions"
 * appear on the menu and that voice pops up an alert showing the instructions.
 * Field desc are ignored.
 * 
 * At the bottom of the forms the button for the available actions are placed.
 * Available actions are passed via the method setActions()
 * 
 * <i>TODO LIST:
 * <ul>
 * <li>list-single and list-multi should be changed: they should show a non
 * editable control that exposes the label and the current selected content plus
 * a button that spawns the pop-up screen for the selection</li>
 * <li>text-multi and jid-multi should open a separate TextBox item (note. on
 * SonyEricsson it seems that there's no difference between the two...).</li>
 * <li>jid-multi should be checked for emailaddress format (emailaddress is not
 * enforceable for multiline TextBoxes</li>
 * <li>check '\n' in fields that are not *-multi and pop up error.</li>
 * <li>add a voice "field description" on the menu (or place a button with (?))
 * to honour the "desc" for each field.</li>
 * <li>Heuristics: when a form has a single list-* item or the list-* item has
 * no more than 2 or 3 options, there shouldn't be a need for a pop up screen.
 * </ul>
 * </i>
 * 
 */
public class DataFormScreen extends UIScreen implements CommandExecutorListener {

	private static ResourceManager rm = ResourceManager.getManager();

	/** The handled data form */
	private DataForm df;

	/** the listener to be notified of commands */
	private DataFormListener dfl;

	/** the available actions */
	private int actions;

	private UIButton cmd_submit;
	private UIButton cmd_cancel;
	private UILabel menu_cancel = new UILabel(rm.getString(
			ResourceIDs.STR_CLOSE).toUpperCase());
	private UIButton cmd_prev;
	private UIButton cmd_next;
	private UILabel cmd_delay = new UILabel(rm
			.getString(ResourceIDs.STR_FILLLATER));

	private UIMenu show_instruction;
	private UILabel show_instruction_label = new UILabel(rm.getString(
			ResourceIDs.STR_INSTRUCTIONS).toUpperCase());

	private UILabel show_desc_label = new UILabel(rm.getString(
			ResourceIDs.STR_DESC).toUpperCase());

	private UIMenu instruction_menu = null;
	private UIMenu desc_menu = new UIMenu("");
	private UILabel si_instructions = new UILabel("");

	/** the item array created to represent the form */
	private UIItem[] items;

	private WaitScreen ws = null;
	private EventQueryRegistration eqr = null;

	/*
	 * To construct the "Expand" support
	 */
	UIMenu zoomSubmenu;
	UILabel zoomLabel = new UILabel("EXPAND");

	private UIHLayout mainLayout = new UIHLayout(3);
	UIPanel mainPanel = new UIPanel(true, false);

	public DataFormScreen(DataForm df, DataFormListener dfl, int cmds) {
		setTitle(rm.getString(ResourceIDs.STR_FILL_FORM));

		UISeparator separator = new UISeparator(0);
		mainLayout.setGroup(false);
		mainLayout.insert(separator, 0, 3, UILayout.CONSTRAINT_PIXELS);
		mainLayout.insert(mainPanel, 1, 100, UILayout.CONSTRAINT_PERCENTUAL);
		mainLayout.insert(separator, 2, 3, UILayout.CONSTRAINT_PIXELS);
		this.append(mainLayout);

		this.df = df;
		this.dfl = dfl;

		if (df.title != null) {
			setTitle(df.title);
		}

		setMenu(new UIMenu(""));
		UIMenu menu = getMenu();
		menu.append(menu_cancel);
		//menu.append(cmd_delay);
		actions = DataFormListener.CMD_SUBMIT | DataFormListener.CMD_CANCEL;
		instruction_menu = UIUtils.easyMenu(rm
				.getString(ResourceIDs.STR_INSTRUCTIONS), 10, 20, this
				.getWidth() - 10, null);
		//		desc_menu.setAbsoluteX(10);
		//		desc_menu.setAbsoluteY(20);
		//		desc_menu.setWidth(this.getWidth() - 10);
		desc_menu.append(show_desc_label);
		show_instruction = UIUtils.easyMenu("", 10, 20, this.getWidth() - 10,
				show_instruction_label);
		// prepare zoomSubMenu
		zoomSubmenu = UIUtils.easyMenu("", 10, 10, this.getWidth() - 30,
				zoomLabel);
		zoomLabel.setAnchorPoint(UIGraphics.HCENTER);
		if (cmds > 0) {
			this.setActions(cmds);
		}
		this.createControls();
	}

	/**
	 * Sets the available command buttons. Actions should be one of the CMD_*
	 * flags defined in the DataFormListener interface.
	 * 
	 * @param cmds
	 */
	private void setActions(int _ac) {
		/* submit and cancel are always shown */
		actions = _ac /*| DataFormListener.CMD_SUBMIT*/
				| DataFormListener.CMD_CANCEL;
	}

	/**
	 * Show the form, dynamically adding all the controls
	 */
	private void createControls() {

		// as always: many operations on the gui need a freeze since
		// i love the battery life
		this.setFreezed(true);

		this.mainPanel.removeAllItems();

		/* do I create this only once? */
		items = new UIItem[df.fields.size()];

		for (int i = 0; i < df.fields.size(); i++) {
			DataForm.Field fld = (DataForm.Field) df.fields.elementAt(i);

			if (fld.type == DataForm.FLT_HIDDEN) {
				continue;
			}

			if (fld.type == DataForm.FLT_BOOLEAN) {
				// XXX: check how to render this
				String fldName = (fld.label == null ? fld.varName : fld.label);
				UICheckbox cgrp = new UICheckbox(fldName);
				if ("1".equals(fld.dValue) || "true".equals(fld.dValue)) {
					cgrp.setChecked(true);
				} else {
					cgrp.setChecked(false);
				}
				mainPanel.addItem(cgrp);
				items[i] = cgrp;
				continue;
			}

			if (fld.type == DataForm.FLT_LISTMULTI
					|| fld.type == DataForm.FLT_LISTSINGLE) {
				String title = (fld.label == null ? fld.varName : fld.label);
				UICombobox cgrp = new UICombobox(title,
						(fld.type == DataForm.FLT_LISTMULTI));
				boolean[] flags = new boolean[fld.options.size()];
				for (int j = 0; j < fld.options.size(); j++) {
					String[] opt = (String[]) fld.options.elementAt(j);
					cgrp.append(opt[1]);
					if (fld.dValue != null && fld.dValue.indexOf(opt[0]) != -1) {
						flags[j] = true;
					} else {
						flags[j] = false;
					}
				}
				cgrp.setSelectedFlags(flags);
				mainPanel.addItem(cgrp);
				items[i] = cgrp;
				continue;
			}

			if (fld.type == DataForm.FLT_JIDSINGLE
					|| fld.type == DataForm.FLT_JIDMULTI) {
				String title = (fld.label == null ? ""/* fld.varName */
				: fld.label);
				UIJidCombo combo = new UIJidCombo(title,
						fld.type != DataForm.FLT_JIDSINGLE, rm
								.getString(ResourceIDs.STR_NEW_CONTACT));
				if (fld.dValue != null && fld.dValue.length() > 0) {
					Vector tokens = Utils.tokenize(fld.dValue, '\n');
					Enumeration en = tokens.elements();
					while (en.hasMoreElements()) {
						combo.append((String) en.nextElement());
					}
				}
				Vector contacts = RosterScreen.getOrderedContacts(true);
				Enumeration en = contacts.elements();
				while (en.hasMoreElements()) {
					Contact c = (Contact) en.nextElement();
					if (c.isVisible()) {
						combo.append(c.jid);
					}
				}
				mainPanel.addItem(combo);
				items[i] = combo;
			}
			if (fld.type == DataForm.FLT_TXTPRIV
					|| fld.type == DataForm.FLT_TXTSINGLE
					|| fld.type == DataForm.FLT_TXTMULTI
					|| fld.type == DataForm.FLT_FIXED) {
				String title = (fld.label == null ? ""/* fld.varName */
				: fld.label);
				int flags = TextField.ANY;
				if (fld.type == DataForm.FLT_TXTPRIV) {
					flags |= TextField.PASSWORD;
				}
				if (fld.type == DataForm.FLT_FIXED) {
					flags |= TextField.UNEDITABLE;
				}
				// XXX: Which the maximum allowed length? We use 1k for the
				// moment
				UITextField tf = new UITextField(title, fld.dValue, 1024, flags);
				mainPanel.addItem(tf);
				if (fld.type == DataForm.FLT_TXTMULTI
						|| fld.type == DataForm.FLT_FIXED
						|| fld.type == DataForm.FLT_TXTSINGLE) {
					if (fld.type == DataForm.FLT_TXTMULTI) {
						tf.setMinLines(4);
					}
					tf.setWrappable(true);
				}
				items[i] = tf;

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
					MediaRetriever mr = new MediaRetriever(stream,this.dfl.getFrom(),
							fld.media, mrl);
					mr.retrieveMedia();
				}
				continue;
			}
		}

		if (df.instructions != null) {
			for (int i = 0; i < items.length; i++) {
				if (items[i] != null) {
					items[i].setSubmenu(show_instruction);
				}
			}
			si_instructions.setText(df.instructions);
			if (instruction_menu.getItems().contains(si_instructions) == false) instruction_menu
					.append(si_instructions);
			si_instructions
					.setWrappable(true, instruction_menu.getWidth() - 10);
		}

		// add the desc
		this.addDesc();

		/* Buttons: should be placed in-line */
		/* show actions. order is CANCEL, [PREV], [NEXT], SUBMIT */
		cmd_submit = new UIButton(rm.getString(ResourceIDs.STR_SUBMIT));
		cmd_cancel = new UIButton(rm.getString(ResourceIDs.STR_CANCEL));
		cmd_prev = new UIButton(rm.getString(ResourceIDs.STR_PREV));
		cmd_next = new UIButton(rm.getString(ResourceIDs.STR_NEXT));

		boolean addUhl1 = false;
		addUhl1 |= insertCommand(DataFormListener.CMD_NEXT, cmd_next);
		if (addUhl1 == false) addUhl1 |= insertCommand(
				DataFormListener.CMD_SUBMIT, cmd_submit);
		insertCommand(DataFormListener.CMD_PREV, cmd_prev);
		mainPanel.addItem(UIUtils.easyCenterLayout(cmd_cancel, 150));
		this.setFreezed(false);
		this.askRepaint();
	}

	private boolean insertCommand(int cmd_code, UIButton item) {
		boolean retVal = false;
		if ((actions & cmd_code) != 0) {
			retVal = true;
			if (df.instructions != null) {
				item.setSubmenu(show_instruction);
			}
			mainPanel.addItem(UIUtils.easyCenterLayout(item, 150));
		}
		return retVal;
	}

	private void addDesc() {
		for (int i = 0; i < df.fields.size(); i++) {
			DataForm.Field fld = (DataForm.Field) df.fields.elementAt(i);
			if (fld.desc != null) {
				items[i].setSubmenu(desc_menu);
			}
		}
	}

	protected void paint(UIGraphics g, int w, int h) {
		super.paint(g, w, h);

		// longest textfield handling 
		//		UIItem panelItem = mainPanel.getSelectedItem();
		//		if (panelItem instanceof UITextField) {
		//			Graphics tg = getGraphics();
		//			int labelHeight = panelItem.getHeight(tg);
		//			int availableHeight = UICanvas.getInstance().getClipHeight()
		//					- this.headerLayout.getHeight(tg)
		//					- this.footer.getHeight(tg);
		//			UIMenu itemSubMenu = panelItem.getSubmenu();
		//			if (labelHeight > availableHeight
		//					&& (itemSubMenu == null || itemSubMenu != zoomSubmenu)) {
		//				panelItem.setSubmenu(zoomSubmenu);
		//				// always reset these values when asking a "repaint" within a "paint"
		//				UICanvas ci = UICanvas.getInstance();
		//				g.translate(-g.getTranslateX(), -g.getTranslateY());
		//				g.setClip(0, 0, ci.getWidth(), ci.getHeight() + 1);
		//				this.askRepaint();
		//			}
		//		}
	}

	/**
	 * Command handler
	 */
	public void menuAction(UIMenu menu, UIItem cmd) {
		int comm = -1;
		boolean checkRequired = false;
		if (cmd == cmd_cancel || cmd == menu_cancel) {
			comm = DataFormListener.CMD_CANCEL;
		} else if (cmd == cmd_submit) {
			comm = DataFormListener.CMD_SUBMIT;
			checkRequired = true;
		} else if (cmd == cmd_next) {
			comm = DataFormListener.CMD_NEXT;
			checkRequired = true;
		} else if (cmd == cmd_prev) {
			comm = DataFormListener.CMD_PREV;
		} else if (cmd == cmd_delay) {
			comm = DataFormListener.CMD_DELAY;
		} else if (cmd == this.zoomLabel) {
			UITextField selLabel = (UITextField) this.getSelectedItem();
			selLabel.handleScreen();
		} else if (cmd == show_desc_label) {
			openDescription(this, df);
		} else if (cmd == show_instruction_label) {
			/* show/hide instructions */
			this.addPopup(this.instruction_menu);
			return;
		}

		if (comm == -1) {
			/* ???? */
			return;
		}

		int missingField = fillForm(checkRequired);
		if (missingField >= 0) {
			this.mainPanel.setSelectedIndex(missingField);
			UILabel label = new UILabel(rm
					.getString(ResourceIDs.STR_MISSING_FIELD));
			label.setWrappable(true, UICanvas.getInstance().getWidth() - 60);
			UIMenu missingMenu = UIUtils.easyMenu(rm
					.getString(ResourceIDs.STR_WARNING), 30, 30, UICanvas
					.getInstance().getWidth() - 60, label, rm
					.getString(ResourceIDs.STR_CANCEL), rm
					.getString(ResourceIDs.STR_SELECT));
			this.addPopup(missingMenu);
			return;
		}

		boolean setWaiting = dfl.needWaiting(comm);
		if (setWaiting == true) {
			ws = new WaitScreen(this.getTitle(), this.getReturnScreen());
			eqr = EventDispatcher.addDelayedListener(ws, true);
			UICanvas.getInstance().open(ws, true);
			dfl.setCel(this);
		}

		// if the dataform will have an answer, e.g. an IQ contained dataform
		// #ifndef BLUENDO_SECURE
						dfl.execute(comm);
		// #endif
		RosterScreen.getInstance()._handleTask(dfl);
		UICanvas.getInstance().close(this);
	}

	/**
	 * 
	 */
	public static void openDescription(UIScreen dataScreen, DataForm df) {
		int index = 0;

		if (dataScreen instanceof DataFormScreen) index = ((DataFormScreen) dataScreen).mainPanel
				.getSelectedIndex();
		else if (dataScreen instanceof DataResultScreen) index = ((DataResultScreen) dataScreen).mainPanel
				.getSelectedIndex();

		String desc = ((DataForm.Field) df.fields.elementAt(index)).desc;
		UITextField descField = new UITextField("", desc, desc.length(),
				TextField.UNEDITABLE);
		descField.setWrappable(true);
		UIMenu descriptionMenu = UIUtils.easyMenu(rm
				.getString(ResourceIDs.STR_DESC), 10, 20,
				dataScreen.getWidth() - 20, descField);
		//descPanel.setMaxHeight(UICanvas.getInstance().getClipHeight() / 2);

		descriptionMenu.cancelMenuString = "";
		descriptionMenu.selectMenuString = rm.getString(ResourceIDs.STR_CLOSE)
				.toUpperCase();
		descriptionMenu.setSelectedIndex(1);
		dataScreen.addPopup(descriptionMenu);
		descField.expand();
	}

	public boolean keyPressed(int kc) {
		if (super.keyPressed(kc)) return true;

		return RosterScreen.makeRoll(kc, this);
	}

	/**
	 * Command handler for on-screen buttons
	 */
	public void itemAction(UIItem cmd) {
		menuAction(null, cmd);
	}

	/**
	 * Called when submit is pressed
	 * @param checkRequired 
	 * 	used to indicated that the check for required fields must be accomplished
	 * @return 
	 * 	if greater or equal to zero the indicates the index of the empty field
	 */
	private int fillForm(boolean checkRequired) {
		int missingField = -1;
		for (int i = 0; i < df.fields.size(); i++) {
			DataForm.Field fld = (DataForm.Field) df.fields.elementAt(i);
			if (fld.type == DataForm.FLT_HIDDEN) {
				continue;
			}

			// if need check and field is required and the field
			// is empty then the form is not completely filled
			if (checkRequired && fld.required) {
				if (items[i] instanceof UITextField) {
					UITextField tf = (UITextField) items[i];
					String text = tf.getText();
					if (text == null || text.length() == 0) missingField = i;
				} else if (items[i] instanceof UICombobox) {
					UICombobox cf = (UICombobox) items[i];
					int selIndex = cf.getSelectedIndex();
					if (selIndex < 0) missingField = i;
				}
			}

			if (fld.type == DataForm.FLT_BOOLEAN) {
				UICheckbox cgrp = (UICheckbox) items[i];
				fld.dValue = (cgrp.isChecked() ? "true" : "false");
				continue;
			}

			if (fld.type == DataForm.FLT_LISTMULTI
					|| fld.type == DataForm.FLT_LISTSINGLE
					|| fld.type == DataForm.FLT_JIDSINGLE
					|| fld.type == DataForm.FLT_JIDMULTI) {
				UICombobox cmb = (UICombobox) items[i];
				boolean[] flags = cmb.getSelectedFlags();
				StringBuffer dtext = new StringBuffer();
				int scount = 0;
				for (int j = 0; j < flags.length; j++) {
					if (flags[j]) {
						scount++;
						String stringVal = "";
						if (fld.type == DataForm.FLT_LISTMULTI
								|| fld.type == DataForm.FLT_LISTSINGLE) {
							String[] opt = (String[]) fld.options.elementAt(j);
							stringVal = opt[0];
						} else if (fld.type == DataForm.FLT_JIDMULTI) {
							stringVal = cmb.getSelectedStrings()[scount - 1];
						} else if (fld.type == DataForm.FLT_JIDSINGLE) {
							stringVal = cmb.getSelectedString();
						}
						if (scount > 1) {
							dtext.append("\n");
						}
						dtext.append(stringVal);
					}
				}
				if (scount == 0) {
					fld.dValue = "";
				} else {
					fld.dValue = dtext.toString();
				}
				continue;
			}

			if (fld.type == DataForm.FLT_TXTPRIV
					|| fld.type == DataForm.FLT_TXTSINGLE
					|| fld.type == DataForm.FLT_TXTMULTI
					|| fld.type == DataForm.FLT_FIXED) {
				UITextField tf = (UITextField) items[i];
				fld.dValue = tf.getText();
				continue;
			}
		}
		return missingField;
	}

	public void executed(Object screen) {
		if (eqr != null) {
			if (ws != null) {
				UIScreen tempScreen = ws.getReturnScreen();
				((UIScreen) screen).setReturnScreen(tempScreen);
			}
			EventDispatcher.dispatchDelayed(eqr, this);
			eqr = null;
		}
	}
	
	public boolean askClose() {
		return false;
	}
}
