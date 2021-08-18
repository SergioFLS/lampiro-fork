// #condition !UI
//@/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
//@ * See about.html for details about license.
//@ *
//@ * $Id: SimpleComposerScreen.java 2329 2010-11-16 14:12:50Z luca $
//@*/
//@
//@package it.yup.screens;
//@
//@import javax.microedition.lcdui.Command;
//@import javax.microedition.lcdui.CommandListener;
//@import javax.microedition.lcdui.Displayable;
//@import javax.microedition.lcdui.TextBox;
//@import javax.microedition.lcdui.TextField;
//@
//@import lampiro.LampiroMidlet;
//@import it.yup.util.ResourceIDs;
//@import it.yup.util.ResourceManager;
//@import it.yup.xmpp.Contact;
//@import it.yup.client.XMPPClient;
//@import it.yup.xmpp.packets.Message;
//@
//@public class SimpleComposerScreen extends TextBox implements CommandListener {
//@
//@	private static ResourceManager rm = ResourceManager.getManager();
//@
//@	private Displayable next;
//@	private Contact user;
//@	private Command cmd_send = new Command(rm.getString(ResourceIDs.STR_SEND),
//@			Command.OK, 1);
//@	private Command cmd_exit = new Command(rm.getString(ResourceIDs.STR_CLOSE),
//@			Command.CANCEL, 1);
//@
//@	public SimpleComposerScreen(Displayable d, Contact u) {
//@		super("", null, 2000, TextField.ANY);
//@		this.next = d;
//@		this.user = u;
//@		setTitle(rm.getString(ResourceIDs.STR_MESSAGE_TO) + " "
//@				+ user.getPrintableName());
//@		addCommand(cmd_send);
//@		addCommand(cmd_exit);
//@		setCommandListener(this);
//@
//@	}
//@
//@	public void commandAction(Command cmd, Displayable d) {
//@		if (cmd == cmd_send) {
//@			// Send the message and add it to the conversation thread
//@			// XXX I must mantain the the thread ID, check the subject
//@			String msgText = getString();
//@			//Config cfg = Config.getInstance();
//@			//String myjid = cfg.user + "@" + cfg.server + "/" + Config.YUP_RESOURCE_NAME;
//@			Message msg = new Message(user.jid, "chat");
//@			msg.setBody(msgText);
//@			XMPPClient.getInstance().sendPacket(msg);
//@			user.addMessageToHistory(null,msg);
//@			LampiroMidlet.disp.setCurrent(next);
//@		} else if (cmd == cmd_exit) {
//@			LampiroMidlet.disp.setCurrent(next);
//@		}
//@	}
//@
//@}
