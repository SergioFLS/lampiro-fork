/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: Task.java 2328 2010-11-16 14:11:30Z luca $
*/

package it.yup.xmpp;

/**
 * An object with some actions involved (e.g. an ad hoc command or a data form).
 * @author sciasbat
 * 
 */
public interface Task {

	static public byte CMD_MASK = 0x10;
	static public byte DF_MASK = 0x20;

	/** The command is completed and we have a result */
	static public byte CMD_FINISHED = 0x11;
	/** The command is executing and we are waiting for a response from the server */
	static public byte CMD_EXECUTING = 0x12;
	/** The command is executing and is waiting for user input */
	static public byte CMD_INPUT = 0x13;
	/** The command has been canceled */
	static public byte CMD_CANCELED = 0x14;
	/** The command is being canceled */
	static public byte CMD_CANCELING = 0x15;
	/** The command is in some error state */
	static public byte CMD_ERROR = 0x16;
	/** Remove the result */
	static public byte CMD_DESTROY = 0x17;
	/** A task without form*/
	static public byte CMD_FORM_LESS = 0x18;

	/** The form is awaiting for user imput */
	static public byte DF_FORM = 0x21;
	/** Form submitted and awaiting for a result */
	static public byte DF_SUBMITTED = 0x22;
	/** Form canceled */
	static public byte DF_CANCELED = 0x23;
	/** We have received a result */
	static public byte DF_RESULT = 0x24;
	/** The form id in some error state */
	static public byte DF_ERROR = 0x25;
	/** Remove the result */
	static public byte DF_DESTROY = 0x26;
 
	public void display();

	/** Get the label that must be displayed when listing the tasks */
	public String getLabel();

	/** Get the task status*/
	public byte getStatus();

	/** Get the jid originating the task */
	public String getFrom();

	public void setEnableDisplay(boolean display);

	public void setEnableNew(boolean b);
	
	public boolean getEnableDisplay();

	public boolean getEnableNew();
	
}
