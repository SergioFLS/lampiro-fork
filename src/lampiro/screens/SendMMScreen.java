/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: SendMMScreen.java 1858 2009-10-16 22:42:29Z luca $
*/

package lampiro.screens;

import it.yup.ui.UIButton;
import it.yup.ui.UICanvas;
import it.yup.ui.UICombobox;
import it.yup.ui.UIHLayout;
import it.yup.ui.UIItem;
import it.yup.ui.UILayout;
import it.yup.ui.UIUtils;
import it.yup.util.Digests;
import it.yup.util.ResourceIDs;
import it.yup.xml.Element;
import it.yup.xmpp.Contact;
import it.yup.xmpp.FTSender;
import it.yup.xmpp.MUC;
import it.yup.client.XMPPClient;
import it.yup.xmpp.FTSender.FTSEventHandler;
import it.yup.xmpp.packets.Message;
import it.yup.xmpp.packets.Presence;

import java.util.Enumeration;
import java.util.Vector;

public class SendMMScreen extends ShowMMScreen {

	class UploadHandler implements FTSEventHandler {

		private String recipientJid;
		private String originalFileName;

		public UploadHandler(String receipientJid, String originalFileName) {
			this.recipientJid = receipientJid;
			this.originalFileName = originalFileName;
		}

		public void fileAcceptance(String jid, String fileName, boolean accept,
				FTSender sender) {
			RosterScreen rs = RosterScreen.getInstance();
			XMPPClient xmppClient = XMPPClient.getInstance();
			Contact recipientContact = xmppClient.getRoster().getContactByJid(
					recipientJid);
			if (recipientContact != null) {
				rs.fileAcceptance(jid, originalFileName, accept, sender);
			}

		}

		public void fileError(String jid, String fileName, Element e) {
			RosterScreen rs = RosterScreen.getInstance();
			XMPPClient xmppClient = XMPPClient.getInstance();
			Contact recipientContact = xmppClient.getRoster().getContactByJid(
					recipientJid);
			if (recipientContact != null) {
				rs.fileError(recipientJid, originalFileName, e);
			}
		}

		public void fileSent(String jid, String fileName, boolean b,
				FTSender sender) {
			RosterScreen rs = RosterScreen.getInstance();
			XMPPClient xmppClient = XMPPClient.getInstance();
			Contact recipientContact = xmppClient.getRoster().getContactByJid(
					recipientJid);
			if (recipientContact != null) {
				rs.fileSent(recipientJid, originalFileName, b, sender);
			}
			Message m = new Message(this.recipientJid,
					recipientContact instanceof MUC ? Message.GROUPCHAT
							: Message.CHAT);
			m.setBody(rm.getString(ResourceIDs.STR_UPLOAD_TEXT) + " "
					+ rs.basePath + fileName + rs.uploadSuffix);
			XMPPClient.getInstance().sendPacket(m);
		}

		public void sessionInitated(String jid, String fileName, FTSender sender) {
			RosterScreen rs = RosterScreen.getInstance();
			XMPPClient xmppClient = XMPPClient.getInstance();
			Contact recipientContact = xmppClient.getRoster().getContactByJid(
					recipientJid);
			if (recipientContact != null) {
				rs.sessionInitated(recipientJid, originalFileName, sender);
			}
		}

		public void chunkSent(int sentBytes, int length, FTSender ftSender) {
			RosterScreen rs = RosterScreen.getInstance();
			rs.chunkSent(sentBytes, length, ftSender);
		}
	}

	private UICombobox rosterCombo = new UICombobox(rm
			.getString(ResourceIDs.STR_CONTACTS), false);
	private Vector sendCandidates = new Vector();

	UIButton cmd_send = new UIButton(rm.getString(ResourceIDs.STR_SEND));
	protected UIButton cmd_send_save = new UIButton(rm
			.getString(ResourceIDs.STR_SEND)
			+ " & " + rm.getString(ResourceIDs.STR_SAVE));
	//	private UILabel label_send = new UILabel(rm.getString(ResourceIDs.STR_SEND)
	//			.toUpperCase());

	protected UIHLayout cmd_layout_send_save = UIUtils.easyCenterLayout(
			cmd_send_save, 150);

	private Vector orderedContacts;

	/*
	 * The fullJid of the Contact to send the tile to
	 */
	private String fullJid = null;

	public SendMMScreen(byte[] img, String fileName, String fileType,
			String fileDesc, int multimediaType, String fullJid) {
		super(img, fileType, multimediaType);
		this.fullJid = fullJid;
		this.fileType = fileType;
		this.fileName = fileName;
		if (fileName != null) {
			fileNameTextField.setText(fileName);
		}

		if (fileDesc != null) {
			fileDescTextField.setText(fileDesc);
		}

		//UIMenu thisMenu = this.getMenu();
		//thisMenu.append(this.label_send);
		mainPanel.insertItemAt(rosterCombo, mainPanel.getItems().indexOf(
				cmd_layout));
		mainPanel.insertItemAt(cmd_layout_send_save, mainPanel.getItems()
				.indexOf(cmd_layout) + 1);
		this.setSelectedItem(mainPanel);

		this.orderedContacts = RosterScreen.getOrderedContacts(true);

		for (Enumeration en = orderedContacts.elements(); en.hasMoreElements();) {
			Contact c = (Contact) en.nextElement();
			String printableName = c.getPrintableName();
			Presence[] ps = c.getAllPresences();
			// if it is a MUC i wish to send the data to the correct jid
			boolean isMUC = c instanceof MUC;
			if ((ps == null || ps.length == 1) || isMUC) {
				this.rosterCombo.append(printableName);
				String tempJid = c.jid;
				if (isMUC == false && ps != null) {
					tempJid = ps[0].getAttribute(Message.ATT_FROM);
				}
				this.sendCandidates.addElement(tempJid);
			} else if (ps.length >= 2) {
				for (int i = 0; i < ps.length; i++) {
					String psFrom = ps[i].getAttribute(Message.ATT_FROM);
					this.rosterCombo.append(printableName + " "
							+ Contact.resource(psFrom));
					this.sendCandidates.addElement(psFrom);
				}
			}
		}

		Enumeration en = this.sendCandidates.elements();
		int index = 0;
		while (fullJid != null && en.hasMoreElements()) {
			String ithString = (String) en.nextElement();
			if (ithString.equals(this.fullJid)) {
				this.rosterCombo.setSelectedIndex(index);
				break;
			}
			index++;
		}

		//		cmd_send.setAnchorPoint(Graphics.HCENTER);
		//		cmd_send_save.setAnchorPoint(Graphics.HCENTER);

		mainPanel.removeItem(cmd_layout);

		cmd_layout = new UIHLayout(2);
		cmd_layout.setGroup(false);
		cmd_layout.insert(cmd_send, 0, 50, UILayout.CONSTRAINT_PERCENTUAL);
		cmd_layout.insert(cmd_save, 1, 50, UILayout.CONSTRAINT_PERCENTUAL);

		mainPanel.insertItemAt(cmd_layout, mainPanel.getItems().indexOf(
				cmd_layout_send_save) + 1);

	}

	//	public void menuAction(UIMenu menu, UIItem c) {
	//		if (c == label_send) {
	//			this.itemAction(cmd_send);
	//		} else {
	//			super.menuAction(menu, c);
	//		}
	//	}

	public void itemAction(UIItem c) {
		if (c == this.cmd_send) {
			String contact = (String) this.sendCandidates
					.elementAt(this.rosterCombo.getSelectedIndex());
			String effectiveReceiver = contact;
			FTSEventHandler ftsHandler = RosterScreen.getInstance();
			String uploadJid = RosterScreen.getInstance().uploadJid;
			String ftFileName = this.fileNameTextField.getText();
			String ftFileDesc = this.fileDescTextField.getText().equals(
					this.defaultFileDescription) ? null
					: this.fileDescTextField.getText();

			// if the contact does not support FT i check if I have an 
			// upload component on the server and send to him.
			if (RosterScreen.supportFT(contact) == false && uploadJid != null) {
				effectiveReceiver = uploadJid;
				ftsHandler = new UploadHandler(contact, ftFileName);
				ftFileName = Digests.hexDigest(
						System.currentTimeMillis() + ftFileName, "sha1")
						.substring(0, 10)
						+ "-" + ftFileName;
			}

			XMPPClient xmppClient = XMPPClient.getInstance();
			FTSender fts = new FTSender(xmppClient.my_jid, xmppClient
					.getXmlStream(), ftFileName, this.originalImage,
					effectiveReceiver, ftFileDesc, ftsHandler);
			fts.sessionInitiate();
			FTScreen frs = FTScreen.getInstance();
			UICanvas.getInstance().open(frs, true, RosterScreen.getInstance());
			menuAction(null, cmd_exit);

		} else if (c == this.cmd_save) {
			super.itemAction(c);
			//menuAction(null, cmd_exit);
		} else if (c == this.cmd_send_save) {
			super.itemAction(cmd_save);
			this.itemAction(cmd_send);
		} else {
			super.itemAction(c);
		}
	}

}