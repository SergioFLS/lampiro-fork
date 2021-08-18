/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: SimpleDataFormExecutor.java 2328 2010-11-16 14:11:30Z luca $
*/

package it.yup.xmpp;

import java.util.Date;

import it.yup.xml.Element;
import it.yup.xmlstream.BasicXmlStream;
import it.yup.xmpp.CommandExecutor.CommandExecutorListener;
import it.yup.xmpp.XmppListener;
import it.yup.xmpp.packets.DataForm;
import it.yup.xmpp.packets.Iq;
import it.yup.xmpp.packets.Message;
import it.yup.xmpp.packets.Stanza;

public class SimpleDataFormExecutor implements DataFormListener, Task {

	private Element form_element;

	private String label = new String();
	private DataForm df;
	private byte status;
	private Date arrive_time;
	public boolean enableDisplay = false;
	public boolean enableNew = false;

	private Roster roster;

	private BasicXmlStream xmlStream;

	private XmppListener xmppListener;

	public SimpleDataFormExecutor(XmppListener xmppListener,BasicXmlStream xmlStream,Roster roster,Element el) {
		this.xmppListener= xmppListener;
		this.xmlStream = xmlStream;
		this.roster=roster;
		this.form_element = el;
		this.df = new DataForm(form_element.getChildByName(DataForm.NAMESPACE,
				DataForm.X));
		this.status = (df.type == DataForm.TYPE_FORM) ? Task.DF_FORM
				: Task.DF_RESULT;

		// XXX get a better label perhaps
		//this.label =  df.title.substring(0, Math.min(df.title.length(), 15));
		if (df.title != null) {
			this.label = df.title;
		} else {
			this.label = "No title";
		}
		arrive_time = new Date();
	}

	public void execute(int cmd) {
		boolean send_reply = false;

		switch (cmd) {
			case DataFormListener.CMD_CANCEL:
				df.type = DataForm.TYPE_CANCEL;
				status = Task.DF_CANCELED;
				send_reply = true;
				break;
			case DataFormListener.CMD_SUBMIT:
				df.type = DataForm.TYPE_SUBMIT;
				status = Task.DF_SUBMITTED;
				send_reply = true;
				break;
			case DataFormListener.CMD_DELAY:
				// do nothing, keep status
				break;
			case DataFormListener.CMD_DESTROY:
				status = Task.DF_DESTROY;
				break;
		}

		if (send_reply) {
			Stanza reply = buildReply(form_element);
			reply.addElement(df.getResultElement());
			xmlStream.send(reply);
		}

		roster.updateTask(this);
	}

	/**
	 * Build the packet containing the answer
	 * The business logic for making an aswer could be set by modifying this method
	 * XXX I think this should be moved to packets package
	 * @param el the packet containing the form
	 */
	private Stanza buildReply(Element el) {
		Stanza reply = null;
		if (Message.MESSAGE.equals(el.name)) {
			Message msg = new Message(el.getAttribute(Stanza.ATT_FROM), el
					.getAttribute(Stanza.ATT_TYPE));
			Element e = el.getChildByName(Stanza.NS_JABBER_CLIENT,
					Message.THREAD);
			if (e != null) {
				msg.addElement(e);
			}
			reply = msg;
		} else if (Iq.IQ.equals(el.name)) {
			// XXX: Always type='set' because now we handle just the type='from'
			Iq iq = new Iq(el.getAttribute(Stanza.ATT_FROM), Iq.T_SET);
			iq.setAttribute(Stanza.ATT_FROM, el.getAttribute(Stanza.ATT_TO));
			reply = iq;
		}
		return reply;
	}

	public void display() {
		Element form = form_element.getChildByName(DataForm.NAMESPACE,
				DataForm.X);

		if (form == null) {
			/* ??? LOG ??? */
			return;
		}

		if (xmppListener != null) {
			Object screen = null;
			if (df.type == DataForm.TYPE_FORM) {
				screen = xmppListener.handleDataForm(df, Task.CMD_INPUT, this,
						-1);
			} else if (df.type == DataForm.TYPE_RESULT) {
				screen = xmppListener.handleDataForm(df, Task.CMD_FINISHED,
						this, -1);
			} else {
				/* should log it... */
				return;
			}
			if (screen != null) {
				xmppListener.showCommand(screen);
			}
		}
	}

	public String getLabel() {
		return "[" + arrive_time.toString().substring(11, 16) + "]" + label;
	}

	public byte getStatus() {
		return status;
	}

	public String getFrom() {
		return form_element.getAttribute(Stanza.ATT_FROM);
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

	public void setCel(CommandExecutorListener cel) {

	}

	public boolean needWaiting(int comm) {
		// TODO Auto-generated method stub
		return false;
	}
}
