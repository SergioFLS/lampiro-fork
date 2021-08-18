/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: GatewayRegisterScreen.java 1858 2009-10-16 22:42:29Z luca $
 */
package lampiro.screens;

import javax.microedition.lcdui.TextField;

import it.yup.ui.UIButton;
import it.yup.ui.UICanvas;
import it.yup.ui.UIHLayout;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UIMenu;
import it.yup.ui.UIPanel;
import it.yup.ui.UIScreen;
import it.yup.ui.UITextField;
import it.yup.ui.UIUtils;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.xml.Element;
import it.yup.xmpp.IQResultListener;
import it.yup.client.XMPPClient;
import it.yup.xmpp.XmppConstants;
import it.yup.xmpp.packets.Iq;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

public class GatewayRegisterScreen extends UIScreen {

	private class GatewayRegistrationHandler extends IQResultListener {

		UIMenu resPopup;

		public GatewayRegistrationHandler() {
			resPopup = UIUtils.easyMenu(rm.getString(ResourceIDs.STR_REGISTER),
					10, -1, UICanvas.getInstance().getWidth() - 20, textLabel,
					"", rm.getString(ResourceIDs.STR_CONTINUE).toUpperCase());
			String regText = rm.getString(ResourceIDs.STR_REG_GATEWAYS);
			String firstLetter = (regText.charAt(0) + "").toUpperCase();
			regText = firstLetter + regText.substring(1, regText.length());
			textLabel.setText(regText);
			textLabel.setFocusable(true);
			resPopup.setSelectedIndex(1);
			textLabel.setWrappable(true, resPopup.getWidth() - 5);
			UIGraphics cg = GatewayRegisterScreen.this.getGraphics();
			int offset = (cg.getClipHeight() - resPopup.getHeight(cg)) / 2;
			resPopup.setAbsoluteY(offset);
		}

		public void handleError(Element e) {
			regSuccessfull = false;
			handleAnswer(rm.getString(ResourceIDs.STR_REG_ERROR));
		}

		public void handleResult(Element e) {
			regSuccessfull = true;
			String errString = null;
			Element error = e.getChildByName(null, Iq.T_ERROR);
			if (error != null) {
				errString = rm.getString(ResourceIDs.STR_REG_ERROR);
				regSuccessfull = false;
			}
			handleAnswer(errString);
		}

		private void handleAnswer(String labelText) {
			try {
				synchronized (UICanvas.getLock()) {
					if (labelText != null) textLabel.setText(labelText);
					GatewayRegisterScreen.this.addPopup(resPopup);
				}
			} catch (Exception e) {
				// #mdebug
				Logger.log("In gateway reg:");
				e.printStackTrace();
				// #enddebug
			}
		}
	}

	static ResourceManager rm = ResourceManager.getManager();

	private Element iq = null;

	private UIPanel mainPanel;

	private UITextField usernameLabel = null;

	private UITextField passwordLabel = null;

	private UIButton submit = new UIButton(rm.getString(ResourceIDs.STR_SUBMIT));
	private UIButton cancel = new UIButton(rm.getString(ResourceIDs.STR_CANCEL));

	UILabel textLabel = new UILabel("");

	private boolean regSuccessfull = false;

	public GatewayRegisterScreen(Element iq) {
		this.iq = iq;
		Element q = iq.getChildByName(XmppConstants.IQ_REGISTER, Iq.QUERY);
		mainPanel = new UIPanel();
		mainPanel.setMaxHeight(-1);
		this.append(mainPanel);
		Element instructions = q.getChildByName(null, "instructions");
		if (instructions != null) {
			String instructionText = instructions.getText();
			if (instructionText == null) instructionText = "";
			UITextField instructionsLabel = new UITextField(rm
					.getString(ResourceIDs.STR_INSTRUCTIONS), instructionText,
					instructionText.length(), TextField.UNEDITABLE);
			mainPanel.addItem(instructionsLabel);
			instructionsLabel.setWrappable(true);
		}
		Element username = q.getChildByName(null, "username");
		if (username != null) {
			String usernameText = username.getText();
			if (usernameText == null) usernameText = "";
			usernameLabel = new UITextField(rm
					.getString(ResourceIDs.STR_USERNAME), usernameText, 50,
					TextField.ANY);
			mainPanel.addItem(usernameLabel);
		}
		Element password = q.getChildByName(null, "password");
		if (password != null) {
			String passwordText = password.getText();
			if (passwordText == null) passwordText = "";
			passwordLabel = new UITextField(rm
					.getString(ResourceIDs.STR_PASSWORD), passwordText, 50,
					TextField.PASSWORD);
			mainPanel.addItem(passwordLabel);
		}

		UIHLayout logLayout = UIUtils.easyButtonsLayout(submit, cancel);
		mainPanel.addItem(logLayout);
		this.titleLabel.setText(rm.getString(ResourceIDs.STR_REGISTER));
	}

	public void menuAction(UIMenu menu, UIItem c) {
		if (regSuccessfull) {
			UICanvas.getInstance().close(this);
			UICanvas.getInstance().show(RosterScreen.getInstance());
		}
	}

	public void itemAction(UIItem item) {

		if (item == this.submit) {
			String from = iq.getAttribute(Iq.ATT_FROM);
			Iq iq = new Iq(from, Iq.T_SET);
			Element query = iq.addElement(XmppConstants.IQ_REGISTER, Iq.QUERY);
			Element usr = query.addElement(XmppConstants.IQ_REGISTER, "username");
			usr.addText(this.usernameLabel.getText());
			Element pwd = query.addElement(XmppConstants.IQ_REGISTER, "password");
			pwd.addText(this.passwordLabel.getText());

			iq.send(XMPPClient.getInstance().getXmlStream(),new GatewayRegistrationHandler());

		} else if (item == this.cancel) {
			UICanvas.getInstance().close(this);
		}
	}

	public void packetReceived(Element e) {
		// TODO Auto-generated method stub

	}

}
