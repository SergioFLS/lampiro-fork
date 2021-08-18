/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: Group.java 2328 2010-11-16 14:11:30Z luca $
*/

package it.yup.xmpp;

import it.yup.xml.Element;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * A group of contatcs
 */
public class Group {

	/** the group name */
	public String name;
	/** the contacts of this group */
	private Vector contacts;

	/*
	 * This is a Hashtable contaning groupName <---> group associations 
	 */
	private static Hashtable groups = new Hashtable();

	public static Hashtable getGroups() {
		return groups;
	}

	private Group(String name) {
		this.name = name;
		contacts = new Vector();
	}

	public static Group getGroup(String name) {
		if (groups.containsKey(name) == false) groups
				.put(name, new Group(name));
		return (Group) groups.get(name);
	}

	public Element store(Roster roster) {
		Element groupEl = new Element(XmppConstants.NS_IQ_ROSTER, "group");
		Enumeration en = this.contacts.elements();
		while (en.hasMoreElements()) {
			Contact c = roster.getContactByJid((String) en.nextElement());
			if (c != null) {
				Element serContact = c.store();
				if (serContact != null) groupEl.addElement(serContact);
			}
		}
		return groupEl;
	}

	public void removeElement(String jid) {
		this.contacts.removeElement(jid);
	}

	public void addElement(String jid) {
		if (!this.contacts.contains(jid)) this.contacts.addElement(jid);
	}

	public Enumeration getContacts() {
		// TODO Auto-generated method stub
		return this.contacts.elements();
	}
}
