/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: SimpleComposerScreen.java 2440 2011-01-31 17:32:28Z luca $
 */

package lampiro.screens;

import java.util.TimerTask;

import it.yup.ui.UICanvas;
import it.yup.ui.wrappers.UITextbox;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.xmpp.Contact;
import it.yup.client.XMPPClient;
import it.yup.xmpp.packets.Message;
import it.yup.xmpp.packets.Presence;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextField;

public class SimpleComposerScreen extends UITextbox implements CommandListener {

	private static ResourceManager rm = ResourceManager.getManager();

	private Contact user;
	protected Command cmd_send = new Command(
			rm.getString(ResourceIDs.STR_SEND), Command.OK, 1);
	private Command cmd_exit = new Command(rm.getString(ResourceIDs.STR_CLOSE),
			Command.CANCEL, 1);

	protected String preferredResource = null;

	private ChatScreen parentScreen;

	private String baseText = "";

	CharCounter charCounter = new CharCounter();

	class CharCounter extends TimerTask {

		public void run() {
			synchronized (UICanvas.getLock()) {
				int length = SimpleComposerScreen.this.getString().length();
				SimpleComposerScreen.this.setTitle(baseText + "(" + length
						+ ")");
			}
		}
	}

	public SimpleComposerScreen(ChatScreen parentScreen, Contact u,
			String preferredResource) {
		super("", parentScreen.defaultText, 2000, TextField.ANY);
		this.parentScreen = parentScreen;
		this.user = u;
		//		setTitle(rm.getString(ResourceIDs.STR_MESSAGE_TO) + " "
		//				+ user.getPrintableName());
		baseText = user.getPrintableName();
		setTitle(baseText);
		addCommand(cmd_send);
		addCommand(cmd_exit);
		setCommandListener(this);
		this.preferredResource = preferredResource;
		//an offline contact may have no resources in that case
		// I use its jid and nothing else
		if (preferredResource == null) {
			Presence presence = u.getPresence(null);
			if (presence != null)
				this.preferredResource = presence
						.getAttribute(Message.ATT_FROM);
		}
		UICanvas.getTimer().scheduleAtFixedRate(charCounter, 1000, 1000);
	}

	public void commandAction(Command cmd, Displayable d) {
		if (cmd == cmd_send) {
			// Send the message and add it to the conversation thread
			// XXX I must mantain the the thread ID, check the subject
			Message msg = sendMessage(preferredResource, Message.CHAT);

			user.addMessageToHistory(preferredResource, msg);
			parentScreen.updateConversation();
			// the screen must be changed after the message is added to the screen!
			try {
				// #ifndef RIM
				synchronized (UICanvas.getLock()){
				// #endif
				UICanvas.display(null);
				parentScreen.askRepaint();
				// #ifndef RIM
				}
				// #endif
			} finally {

			}
			//UICanvas.getInstance().askRepaint(
			//		UICanvas.getInstance().getCurrentScreen());
			charCounter.cancel();

		} else if (cmd == cmd_exit) {
			parentScreen.defaultText = this.getString();
			UICanvas.display(null);
			charCounter.cancel();
		}
	}

	Message sendMessage(String resource, String messageType) {
		this.removeCommand(cmd_send);
		String msgText = getString();
		Message msg = null;
		msg = new Message(resource, messageType);
		msg.setBody(msgText);
		XMPPClient.getInstance().sendPacket(msg);
		parentScreen.defaultText = "";
		return msg;
	}
}
