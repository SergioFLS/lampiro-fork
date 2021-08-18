/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: CommandExecutor.java 2432 2011-01-30 15:26:48Z luca $
*/

package it.yup.xmpp;

import java.util.Date;

import it.yup.xml.Element;
import it.yup.util.Alerts;

import it.yup.xmpp.packets.DataForm;
import it.yup.xmpp.packets.Iq;
import it.yup.xmpp.packets.Message;

public class CommandExecutor extends IQResultListener implements
		DataFormListener, Task {

	public interface CommandExecutorListener {
		public void executed(Object screen);
	}

	private static final String STATUS_EXECUTING = "executing";
	private static final String STATUS_COMPLETED = "completed";
	private static final String STATUS_CANCELED = "canceled";

	protected static final String PREV = "prev";
	protected static final String NEXT = "next";
	protected static final String CANCEL = "cancel";
	protected static final String COMPLETE = "complete";
	protected static final String EXECUTE = "execute";

	public boolean enableDisplay = false;
	public boolean enableNew = false;

	public static final int MSG_BROKEN_DF = 1101;

	/** the command information (node, label) */
	private String[] cmd;
	/** the sid associated with this command iteration */
	protected String sid;
	/** the data form associated with this command */
	private DataForm df;
	/** the status of this command */
	protected byte status;

	protected Element current_element = null;

	protected Object screen;

	private String note = null;

	private Date last_modify;

	private int step;

	private String chosenResource;

	/*
	 * The delayed registration
	 */
	protected CommandExecutorListener cel;
	protected Roster roster;
	

	/**
	 * @param cmd the parameters for the command execution
	 * @param chosenResource the resource for which to execute the command
	 * @param eqr the delayed registration to execute
	 * 
	 */
	public CommandExecutor(Roster roster,String[] cmd, String chosenResource,
			CommandExecutorListener cel) {
		this.cmd = cmd;
		this.chosenResource = chosenResource;
		this.cel = cel;
		this.status = Task.CMD_EXECUTING;
		step = 1;
		last_modify = new Date();
		this.roster= roster;
		roster.updateTask(this);
	}

	/**
	 * @param cmd
	 * @param chosenResource
	 */
	public void setupCommand() {
		Iq iq = new Iq(chosenResource, Iq.T_SET);
		Element commandEl = iq.addElement(XmppConstants.NS_COMMANDS,
				XmppConstants.COMMAND);
		commandEl.setAttribute("node", cmd[0]);
		commandEl.setAttribute(XmppConstants.ACTION, EXECUTE);

		sendPacket(iq);
	}

	private void sendPacket(Iq iq) {
		iq.send(roster.getXmlStream(),this);
	}

	public void packetReceived(Element el) {
		current_element = el;
		Element command = (Element) el.getChildByName(XmppConstants.NS_COMMANDS,
				XmppConstants.COMMAND);
		if (command == null) {
			/* ? possible ? */
			return;
		}
		/* every time this is copied, not a problem, SHOULD stay the same */
		sid = command.getAttribute("sessionid");
		updateFSM(el);
	}

	/**
	 * @param el
	 * @param command
	 */
	protected void updateFSM(Element el) {
		/* Parse the dataform if present */
		Element command = (Element) el.getChildByName(XmppConstants.NS_COMMANDS,
				XmppConstants.COMMAND);
		Element form = command.getChildByName(DataForm.NAMESPACE, DataForm.X);
		if (form != null) {
			df = new DataForm(form);
		} else {
			df = null;
		}
		/*
		 * Some implementations seem to send the result of a completed form
		 * using a DataForm of type "form" instead of "result", so let's check
		 * the "status" and not the type of the form
		 */
		String el_status = command.getAttribute("status");
		if (STATUS_CANCELED.equals(el_status)) {
			/*
			 * the server has canceled the command. could this happen? yes, as
			 * aswer of a cancel
			 */
			this.status = Task.CMD_CANCELED;
		} else if (STATUS_COMPLETED.equals(el_status)) {
			this.status = Task.CMD_FINISHED; // the command is finished
		} else if (STATUS_EXECUTING.equals(el_status)) {
			this.status = Task.CMD_INPUT;
		} else {
			// unexpected status, discard the message, and notify?
			this.status = Task.CMD_ERROR;
			// XXX is this enough?
		}
		if (el.getAttribute(Iq.ATT_TYPE).equals(Iq.T_ERROR)) this.status = Task.CMD_ERROR;
		if (df == null && this.status != Task.CMD_ERROR) {
			if (this.status != Task.CMD_FINISHED) {
				this.status = Task.CMD_FORM_LESS;
			} else {
				this.status = Task.CMD_DESTROY;
			}
		}
		Element note_element = command.getChildByName(XmppConstants.NS_COMMANDS,
				"note");
		if (note_element != null) {
			this.note = note_element.getText();
		} else {
			this.note = null;
		}
		roster.updateTask(this);
		roster.getXmppListener().handleTask(this);
		boolean enableEvent = isEventEnabled(el);
		if (enableEvent) {
			this.cel.executed(screen);
		}
	}

	/**
	 * @param el
	 * @return
	 */
	protected boolean isEventEnabled(Element el) {
		boolean enableEvent = (this.cel != null && this.status != Task.CMD_ERROR);
		return enableEvent;
	}

	public void execute(int cmd) {
		/*
		 * not checking if the cmd is in the allowed ones, as I have built the
		 * screen accordingly...
		 */
		last_modify = new Date();
		switch (cmd) {
			case DataFormListener.CMD_CANCEL:
				status = Task.CMD_CANCELING;
				sendReply(CANCEL, null);
				break;
			case DataFormListener.CMD_PREV:
				step--;
				status = Task.CMD_EXECUTING;
				sendReply(PREV, null);
				break;
			case DataFormListener.CMD_NEXT:
				step++;
				status = Task.CMD_EXECUTING;
				if (df != null) {
					df.type = DataForm.TYPE_SUBMIT;
					sendReply(NEXT, df.getResultElement());
				} else
					sendReply(NEXT, null);
				break;
			case DataFormListener.CMD_SUBMIT:
				step++;
				status = Task.CMD_EXECUTING;
				if (df != null) {
					df.type = DataForm.TYPE_SUBMIT;
					sendReply(EXECUTE, df.getResultElement());
				} else
					sendReply(EXECUTE, null);
				break;
			case DataFormListener.CMD_DELAY:
				// do nothing, just display the next screen
				break;
			case DataFormListener.CMD_DESTROY:
				status = Task.CMD_DESTROY;
		}

		// update command status
		roster.updateTask(this);
	}

	protected Element[] buildReply(String action, Element dfel) {
		Iq iq = new Iq(chosenResource, Iq.T_SET);
		Element commandEl = iq.addElement(XmppConstants.NS_COMMANDS,
				XmppConstants.COMMAND);
		commandEl.setAttribute("node", cmd[0]);
		if (sid != null) {
			commandEl.setAttribute("sessionid", sid);
		}
		if (action != null) {
			commandEl.setAttribute(XmppConstants.ACTION, action);
		}
		if (dfel != null) {
			commandEl.addElement(dfel);
		}
		return new Element[] { iq, commandEl };
	}

	protected void sendReply(String action, Element dfel) {
		Element[] reply = buildReply(action, dfel);
		Iq iq = (Iq) reply[0];
		sendPacket(iq);
	}

	public void display() {
		switch (status) {
			case Task.CMD_INPUT:
				Element command = (Element) current_element.getChildByName(
						XmppConstants.NS_COMMANDS, XmppConstants.COMMAND);
				Element actions = command.getChildByName(
						XmppConstants.NS_COMMANDS, "actions");
				if (actions == null) {
					actions = new Element(XmppConstants.NS_COMMANDS, "actions");
					actions.addElement(null, "complete");
				}
				// add the available actions
				if (df.type == DataForm.TYPE_FORM) {
					int cmds = 0;
					Element[] children = actions.getChildren();
					if (children.length == 0) {
						cmds |= DataFormListener.CMD_SUBMIT;
					}
					for (int i = 0; i < children.length; i++) {
						Element iel = children[i];
						if (NEXT.equals(iel.name)) {
							cmds |= DataFormListener.CMD_NEXT;
						} else if (PREV.equals(iel.name)) {
							cmds |= DataFormListener.CMD_PREV;
						} else if (COMPLETE.equals(iel.name)) {
							cmds |= DataFormListener.CMD_SUBMIT;
						}
					}
					screen = roster.getXmppListener().handleDataForm(df,
								Task.CMD_INPUT, this, cmds);
				}
				break;
			case Task.CMD_EXECUTING:
				roster.getXmppListener().showAlert(Alerts.INFO, XmppConstants.ALERT_COMMAND_INFO,
						XmppConstants.ALERT_WAIT_COMMAND, null);
				break;
			case Task.CMD_CANCELING:
				roster.getXmppListener().showAlert(Alerts.INFO, XmppConstants.ALERT_COMMAND_INFO,
						XmppConstants.ALERT_CANCELING_COMMAND, null);
				break;
			case Task.CMD_CANCELED:
				roster.getXmppListener().showAlert(Alerts.INFO, XmppConstants.ALERT_COMMAND_INFO,
						XmppConstants.ALERT_CANCELED_COMMAND, getLabel());
				break;
			case Task.CMD_FINISHED:
				if (df != null) {
					screen = roster.getXmppListener().handleDataForm(df,
								Task.CMD_FINISHED, this, -1);
				} else {
					// XXX handle note here, if present
					status = Task.CMD_DESTROY;
					roster.updateTask(this);

					new Thread() {
						public void run() {
							roster.getXmppListener().handleTask(CommandExecutor.this);
							roster.getXmppListener().showAlert(Alerts.INFO,
									XmppConstants.ALERT_COMMAND_INFO,
									XmppConstants.ALERT_FINISHED_COMMAND, null);
						}
					}.start();
				}
				break;
			case Task.CMD_ERROR:
				String errorText = null;
				Element error = (Element) current_element.getChildByName(null,
						Message.ERROR);
				if (error != null) {
					String code = error.getAttribute(XmppConstants.CODE);
					if (code != null) errorText = (XmppConstants
							.getErrorString(code));
					Element text = error.getChildByName(null, XmppConstants.TEXT);
					if (text != null) {
						errorText += ". " + text.getText();
					}
				}
				roster.getXmppListener().showAlert(Alerts.ERROR, XmppConstants.ALERT_COMMAND_INFO,
						XmppConstants.ALERT_ERROR_COMMAND, errorText);
				break;
		}

		if (status != Task.CMD_ERROR) {
			if (screen != null) {
				showScreen();
			}
			if (note != null) {
				roster.getXmppListener().showAlert(Alerts.INFO, XmppConstants.ALERT_NOTE,
						XmppConstants.ALERT_NOTE, note);
			}
		}
	}

	/**
	 * 
	 */
	protected void showScreen() {
		roster.getXmppListener().showCommand(screen);
	}

	public String getLabel() {
		return "[" + last_modify.toString().substring(11, 16) + "][" + step
				+ "] " + cmd[1];
	}

	public byte getStatus() {
		return status;
	}

	public String getFrom() {
		return this.chosenResource;
	}

	/**
	 * @return the sid
	 */
	public String getSid() {
		return sid;
	}

	public void setEnableDisplay(boolean enableDisplay) {
		this.enableDisplay = enableDisplay;
	}

	public void setEnableNew(boolean enableNew) {
		this.enableNew = enableNew;
	}

	public boolean getEnableDisplay() {
		return this.enableDisplay;
	}

	public boolean getEnableNew() {
		return this.enableNew;
	}

	public boolean needWaiting(int comm) {
		boolean setWaiting = false;
		if (comm == DataFormListener.CMD_SUBMIT) {
			setWaiting = true;
		} else if (comm == DataFormListener.CMD_NEXT) {
			setWaiting = true;
		} else if (comm == DataFormListener.CMD_PREV) {
			setWaiting = true;
		} else if (comm == DataFormListener.CMD_DELAY) {
			setWaiting = true;
		}
		return setWaiting;
	}

	public void setCel(CommandExecutorListener cel) {
		this.cel = cel;

	}

	public void handleError(Element e) {
		packetReceived(e);
	}

	public void handleResult(Element e) {
		packetReceived(e);
	}
}
