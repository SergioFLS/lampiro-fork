// #condition !UI
//@/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
//@ * See about.html for details about license.
//@ *
//@ * $Id: TaskListScreen.java 2310 2010-11-04 12:18:13Z luca $
//@*/
//@
//@package it.yup.screens;
//@
//@import lampiro.LampiroMidlet;
//@import it.yup.util.ResourceIDs;
//@import it.yup.util.ResourceManager;
//@import it.yup.xmpp.Task;
//@
//@import javax.microedition.lcdui.Choice;
//@import javax.microedition.lcdui.Command;
//@import javax.microedition.lcdui.CommandListener;
//@import javax.microedition.lcdui.Displayable;
//@import javax.microedition.lcdui.List;
//@
//@/**
//@ *
//@ */
//@public class TaskListScreen extends List implements CommandListener {
//@
//@	private static ResourceManager rm = ResourceManager.getManager();
//@
//@	private Command cmd_cancel = new Command(rm
//@			.getString(ResourceIDs.STR_CANCEL), Command.BACK, 1);
//@
//@	private Task tasks[];
//@
//@	public TaskListScreen(Task tasks[]) {
//@		super(rm.getString(ResourceIDs.STR_TASKHISTORY_TITLE), Choice.IMPLICIT);
//@		this.tasks = new Task[tasks.length];
//@		for (int i = 0; i < tasks.length; i++) {
//@			append(tasks[i].getLabel(), null);
//@			this.tasks[i] = tasks[i];
//@		}
//@
//@		setCommandListener(this);
//@		// setSelectCommand(cmd_select);
//@		addCommand(cmd_cancel);
//@	}
//@
//@	public void commandAction(Command cmd, Displayable disp) {
//@		if (cmd == cmd_cancel) {
//@			LampiroMidlet.disp.setCurrent(RosterScreen.getInstance());
//@			return;
//@		}
//@		//		if(cmd == cmd_select) {
//@		if (cmd == List.SELECT_COMMAND) {
			// #ifndef UI 
//@			tasks[getSelectedIndex()].display(LampiroMidlet.disp, RosterScreen
//@					.getInstance());
			// #endif
//@		}
//@	}
//@}
