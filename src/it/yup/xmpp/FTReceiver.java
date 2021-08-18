/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: FTReceiver.java 1858 2009-10-16 22:42:29Z luca $
*/
package it.yup.xmpp;

// #ifdef MIDP

import org.bouncycastle.util.encoders.Base64;

//#endif
// #ifndef MIDP
//@
//@import it.yup.util.encoders.Base64;
//@ 
//#endif

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

import it.yup.dispatch.EventQuery;
import it.yup.dispatch.EventQueryRegistration;
import it.yup.xml.Element;
import it.yup.xmlstream.BasicXmlStream;
import it.yup.xmlstream.PacketListener;
import it.yup.xmpp.packets.Iq;
import it.yup.xmpp.packets.Stanza;

public class FTReceiver implements PacketListener {

	/*
	 * A file transfer receiver event handler
	 */
	private FTREventHandler eh;
	private BasicXmlStream xmlStream;

	public interface FTREventHandler {

		public void dataReceived(byte[] data, String fileName, String fileDesc,
				OpenListener ftrp);

		public void reqFT(String contactName, OpenListener ftrp);

		public void chunkReceived(int length, int fileSize,
				OpenListener openListener);
	}

	public class OpenListener implements PacketListener {
		public Element e_jingle;

		private int block_size = 4096;
		//private StringBuffer encodedData= new StringBuffer(block_size);
		private byte[] decodedData;
		private int offset = 0;

		private EventQueryRegistration dataListenerEq;

		// the file size
		public int fileSize;

		// the file size
		public String fileName;

		public String fileDesc = "";

		public void answerFT(boolean accept) {
			// If accept send a correct reply and register a listener
			// to open the jingle stream
			if (accept) {
				this.acceptSession();
			} else {
				Iq reply = new Iq(this.e_jingle.getAttribute(Iq.ATT_FROM),
						Iq.T_SET);
				Element jingle = reply.addElement(XmppConstants.JINGLE,
						FTSender.JINGLE);
				jingle.setAttribute(XmppConstants.ACTION,
						FTSender.SESSION_TERMINATE);
				jingle.addElement(null, FTSender.DECLINE);
				xmlStream.send(reply);
			}
		}

		public void packetReceived(Element e) {
			Iq replIq = Iq.easyReply(e);
			xmlStream.send(replIq);

			Element child = e.getChildByName(null, FTSender.JINGLE);
			if (child != null) {
				handleClose(e);
				return;
			}

			child = e.getChildByName(null, XmppConstants.DATA);
			if (child != null) {
				handleData(e);
				return;
			}

			//			child = e.getChildByName(null, FTSender.CLOSE);
			//			if (child != null) {
			//				handleClose(e);
			//				return;
			//			}

			//			child = e.getChildByName(null, FTSender.OPEN);
			//			if (child != null) {
			//				handleOpen(e);
			//				return;
			//			}
		}

		//		private void handleOpen(Element e) {
		//			EventQuery eq = new EventQuery(Iq.IQ, new String[] { Iq.ATT_FROM,
		//					Iq.ATT_TYPE }, new String[] { e.getAttribute(Iq.ATT_FROM),
		//					Iq.T_SET });
		//			Element openElement = e.getChildByName(null, FTSender.OPEN);
		//			eq.child = new EventQuery(XMPPClient.DATA, new String[] {
		//					FTSender.SID, "xmlns" }, new String[] {
		//					openElement.getAttribute(FTSender.SID), XMPPClient.NS_IBB });
		//			this.dataListenerEq = BasicXmlStream.addEventListener(eq, this);
		//
		//			eq = new EventQuery(Iq.IQ,
		//					new String[] { Iq.ATT_FROM, Iq.ATT_TYPE }, new String[] {
		//							e.getAttribute(Iq.ATT_FROM), Iq.T_SET });
		//			eq.child = new EventQuery(FTSender.CLOSE, new String[] {
		//					FTSender.SID, "xmlns" }, new String[] {
		//					openElement.getAttribute(FTSender.SID), XMPPClient.NS_IBB });
		//			BasicXmlStream.addOnetimeEventListener(eq, this);
		//
		//			block_size = Integer.parseInt(openElement
		//					.getAttribute(FTSender.BLOCK_SIZE));
		//
		//			Stanza reply = new Iq(e.getAttribute(Iq.ATT_FROM), Iq.T_RESULT);
		//			reply.setAttribute(Iq.ATT_ID, e.getAttribute(Iq.ATT_ID));
		//			xmppClient.sendPacket(reply);
		//		}

		private void handleClose(Element e) {
			try {
				// when finishing file transfer 
				// the registration is removed by myself
				BasicXmlStream.removePacketListener(dataListenerEq);
				// #mdebug 
								Logger.log("File received kb: " + decodedData.length);
								// System.out.println(decString);
				// #enddebug
				eh.dataReceived(decodedData, fileName, fileDesc,
						OpenListener.this);
			} catch (Exception ex) {
				// #mdebug
								ex.printStackTrace();
								Logger.log("In closing session" + ex.getClass().getName()
										+ "\n" + ex.getMessage());
				// #enddebug
			}
		}

		/**
		 * @param e
		 */
		private void handleData(Element e) {
			try {
				String chunkData = e.getChildByName(null, XmppConstants.DATA)
						.getText();
				byte tempData[] = Base64.decode(chunkData);
				System.arraycopy(tempData, 0, decodedData, offset,
						tempData.length);
				// the data is base64 encoded
				offset += tempData.length;
				eh.chunkReceived(offset, fileSize, OpenListener.this);

			} catch (Exception ex) {
				// #mdebug
								ex.printStackTrace();
								Logger.log("In receiving an IBB packet"
										+ ex.getClass().getName() + "\n" + ex.getMessage());
				// #enddebug
			}
		}

		//		/**
		//		 * @param e
		//		 */
		//		private void handleClose(Element e) {
		//			try {
		//				BasicXmlStream.removeEventListener(dataListenerEq);
		//				Iq reply = Utils.easyReply(e);
		//				xmppClient.sendPacket(reply);
		//				Iq closeSession = new Iq(e.getAttribute(Iq.ATT_FROM), Iq.T_SET);
		//				Element jingleClose = closeSession.addElement(
		//						XMPPClient.JINGLE, FTSender.JINGLE);
		//				jingleClose.setAttribute(XmppConstants.ACTION,
		//						FTSender.SESSION_TERMINATE);
		//				jingleClose.setAttribute(FTSender.SID, e_jingle.getChildByName(
		//						null, FTSender.JINGLE).getAttribute(FTSender.SID));
		//				jingleClose.addElement(null, "reason").addElement(null,
		//						"success");
		//				xmppClient.sendPacket(closeSession);
		//				String decString = encodedData.toString();
		//				decodedData = Base64.decode(decString);
		//				// #mdebug 
		//				Logger.log("File received kb: " + decodedData.length);
		//				// System.out.println(decString);
		//				// #enddebug
		//				eh.dataReceived(decodedData, fileName, fileDesc,
		//						OpenListener.this);
		//			} catch (Exception ex) {
		//				// #mdebug
		//				ex.printStackTrace();
		//				Logger.log("In closing session" + ex.getClass().getName()
		//						+ "\n" + ex.getMessage());
		//				// #enddebug
		//			}
		//		}

		private void acceptSession() {
			Element e = this.e_jingle;
			Element session_accept = new Iq(e.getAttribute(Iq.ATT_FROM),
					Iq.T_SET);
			Element jingle = this.e_jingle
					.getChildByName(null, FTSender.JINGLE);
			jingle.setAttribute(XmppConstants.ACTION, FTSender.SESSION_ACCEPT);
			session_accept.addElement(jingle);
			xmlStream.send(session_accept);
		}
	};

	public FTReceiver(BasicXmlStream xmlStream,FTREventHandler eh) {
		this.xmlStream = xmlStream;
		this.eh = eh;
		EventQuery eq = new EventQuery(Iq.IQ, new String[] { Iq.ATT_TYPE },
				new String[] { Iq.T_SET });
		eq.child = new EventQuery(FTSender.JINGLE, new String[] { "xmlns",
				XmppConstants.ACTION }, new String[] { XmppConstants.JINGLE,
				FTSender.SESSION_INITIATE });
		BasicXmlStream.addPacketListener(eq, this);
	}

	public void packetReceived(Element e) {
		// file transfer receive protocol
		OpenListener ftrp = new OpenListener();
		ftrp.e_jingle = e;
		Element fileNode = e.getPath(new String[] { null, null, null, null,
				null }, new String[] { FTSender.JINGLE, FTSender.CONTENT,
				FTSender.DESCRIPTION, FTSender.OFFER, FTSender.FILE });

		ftrp.fileSize = Integer.parseInt(fileNode.getAttribute(FTSender.SIZE));
		ftrp.decodedData = new byte[ftrp.fileSize];
		ftrp.fileName = fileNode.getAttribute(FTSender.NAME);
		Element desc = fileNode.getChildByName(null, FTSender.DESC);
		if (desc != null)
			ftrp.fileDesc = desc.getText();
		Stanza reply = Iq.easyReply(e);
		xmlStream.send(reply);

		EventQuery eq = new EventQuery(Iq.IQ, new String[] { Iq.ATT_FROM,
				Iq.ATT_TYPE }, new String[] { e.getAttribute(Iq.ATT_FROM),
				Iq.T_SET });
		Element transport = e.getPath(new String[] { null, null, null },
				new String[] { FTSender.JINGLE, FTSender.CONTENT,
						FTSender.TRANSPORT });

		int block_size = Integer.parseInt(transport
				.getAttribute(FTSender.BLOCK_SIZE));

		ftrp.block_size = block_size;

		eq.child = new EventQuery(XmppConstants.DATA,
				new String[] { FTSender.SID }, new String[] { transport
						.getAttribute(FTSender.SID) });
		EventQueryRegistration eqr = BasicXmlStream.addPacketListener(eq, ftrp);
		ftrp.dataListenerEq = eqr;

		eq = new EventQuery(Iq.IQ, new String[] { Iq.ATT_FROM, Iq.ATT_TYPE },
				new String[] { e.getAttribute(Iq.ATT_FROM), Iq.T_SET });
		eq.child = new EventQuery(FTSender.JINGLE, new String[] {
				XmppConstants.ACTION, "xmlns" }, new String[] {
				FTSender.SESSION_TERMINATE, XmppConstants.JINGLE });
		BasicXmlStream.addOnetimePacketListener(eq, ftrp);

		// file transfer acceptance
		eh.reqFT(e.getAttribute(Iq.ATT_FROM), ftrp);
	}
}
