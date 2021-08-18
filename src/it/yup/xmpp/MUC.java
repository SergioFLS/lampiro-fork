/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: MUC.java 2325 2010-11-15 20:07:28Z luca $
*/

package it.yup.xmpp;

import java.util.Vector;

import it.yup.xml.Element;
import it.yup.xmpp.packets.Message;
import it.yup.xmpp.packets.Presence;
import it.yup.xmpp.packets.Stanza;

public class MUC extends Contact {

	public static final String MUC = "muc";
	public static final String MUC_OFFLINE = "muc-offline";
	public static final String AUTO_JOIN = "autojoin";
	public static final String LAMPIRO_AUTO_JOIN = "lampiro_autojoin";
	public static String GROUP_CHATS = "";
	
	// the topic can be null at start
	public String topic = "";
	public String nick=null;
	public String pwd=null;

	public MUC(String jid, String name,String nick,String pwd) {
		// auto assign a group with value MUCS
		super(jid, name, Contact.SUB_BOTH, new String []{GROUP_CHATS});
		this.nick=nick;
		this.pwd=pwd;
		
		// MUCs have only one conversation
		convs.addElement(new Object[] { this.jid, new Vector() });
	}
	
	public Element store() {
		return null;
	}
	
	public void addMessageToHistory(String preferredResource, Message msg) {
		// for the MUC all the messages must be stored together
		// so to mantain the order 
		// the default presence is the first one
		preferredResource = this.jid;
		String type = msg.getAttribute(Message.ATT_TYPE);
		if (type==null)
			type= Message.NORMAL;
		
		unread_msg=true;
		compileMessage(preferredResource, msg, type);
	}
	
	public boolean isVisible() {
		// MUCS are always shown in rosterscreen
		return true;
	}

	/**
	 * Update the presence for the resource that has sent it
	 * 
	 * @param p
	 */
	public void updatePresence(Presence p) {
		if (Presence.T_UNAVAILABLE.equals(p.getAttribute(Stanza.ATT_TYPE))) {
			String offlineResource = Contact.resource(p
					.getAttribute(Stanza.ATT_FROM));
			if (offlineResource.compareTo(nick) == 0) {
				availability = AV_UNAVAILABLE;
				this.resources = null;
			} else {
				updateExistingPresence(p);
			}
			return;
		} else {
			// available presence, update the list and resort
			if (resources == null) {
				// first resource create the list
				resources = new Presence[] { p };
			} else {
				
				addPresence(p);
			}
			availability = AV_ONLINE;
		}
	}
	
	public boolean supportsMUC(Presence p) {
		return false;
	}
}
