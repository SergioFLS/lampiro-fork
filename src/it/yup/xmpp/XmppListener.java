package it.yup.xmpp;

import it.yup.xml.Element;
import it.yup.xmpp.packets.DataForm;
import it.yup.xmpp.packets.Presence;

/*
 * Few methods used to communicate to the application of xmpp events
 * 
 */
public interface XmppListener {

	/**
	 * Removes the contact from the roster; the contact may be removed for
	 * many reasons (temporary modifications, contact to MUC upgrade, roster
	 * recreation, unsubscription )
	 * 
	 * @param c
	 *            the contact that has been removed
	 * @param unsubscribed
	 *            true if the contact has unsibscribed
	 */
	void removeContact(Contact c, boolean unsubscribed);

	void removeAllContacts();

	void updateContact(Contact c, int chStatus);

	void authenticated();

	void rosterXsubscription(Element e);

	void playSmartTone();

	void askSubscription(Contact u);

	void connectionLost();

	void showAlert(int type, int titleCode, int textCode,
			String additionalText);

	void handleTask(Task task);

	Object handleDataForm(DataForm df, byte type, DataFormListener dfl,
			int cmds);

	void handleSecured(Object screen, boolean encrypted);

	void showCommand(Object screen);

	void rosterRetrieved();

	void handlePresenceError(Presence presence);
}