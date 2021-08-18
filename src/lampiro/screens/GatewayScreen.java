/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: GatewayScreen.java 1858 2009-10-16 22:42:29Z luca $
 */
package lampiro.screens;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.TextField;



import it.yup.dispatch.EventDispatcher;
import it.yup.dispatch.EventQueryRegistration;
import it.yup.ui.UIButton;
import it.yup.ui.UICanvas;
import it.yup.ui.UIGauge;
import it.yup.ui.UIHLayout;
import it.yup.ui.UIItem;
import it.yup.ui.UILabel;
import it.yup.ui.UIMenu;
import it.yup.ui.UIPanel;
import it.yup.ui.UIScreen;
import it.yup.ui.UISeparator;
import it.yup.ui.UITextField;
import it.yup.ui.UIUtils;
import it.yup.ui.wrappers.UIGraphics;
import it.yup.ui.wrappers.UIImage;
import it.yup.util.ResourceIDs;
import it.yup.util.ResourceManager;
import it.yup.client.Config;
import it.yup.xmpp.Contact;
import it.yup.client.XMPPClient;
import it.yup.xmpp.XmppConstants;
import it.yup.xmpp.packets.Iq;
import lampiro.screens.rosterItems.UIGateway;

public class GatewayScreen extends UIScreen {

	private UIPanel mainPanel;

	private UILabel cmd_exit = new UILabel(rm.getString(ResourceIDs.STR_CLOSE)
			.toUpperCase());

	private static ResourceManager rm = ResourceManager.getManager();

	private UITextField txt_myServer;
	private UITextField txt_bluendoServer;

	private UIButton refresh_gateways = new UIButton(rm
			.getString(ResourceIDs.STR_REFRESH));

	private int[] myServerGateways = new int[] { 0 };
	private int[] defaultServerGateways = new int[] { 0 };

	private static String cachedServer = null;

	RosterScreen rs = RosterScreen.getInstance();

	UIGauge progressGauge = null;

	/*
	 * the container for the refresh gateways button
	 */
	private UIHLayout refresh_container = null;

	private Vector transports = new Vector();

	public GatewayScreen(String myServer) {
		super();
		this.setMenu(new UIMenu(""));
		this.getMenu().append(cmd_exit);
		this.mainPanel = new UIPanel(true, false);
		this.mainPanel.setMaxHeight(-1);
		this.append(mainPanel);
		this.setTitle(rm.getString(ResourceIDs.STR_GATEWAYS));
		if (myServer == null) {
			if (cachedServer != null) {
				myServer = cachedServer;
			} else {
				myServer = Contact.domain(XMPPClient.getInstance()
						.getMyContact().jid);
				cachedServer = myServer;
			}
		}

		int privilege = TextField.ANY;
		txt_myServer = new UITextField(rm
				.getString(ResourceIDs.STR_SERVER_EXPLORE), myServer, 255,
				privilege, UITextField.FORMAT_LOWER_CASE);
		this.mainPanel.addItem(txt_myServer);

		this.addServerGateways(txt_myServer, myServer);
		if (myServer.equals(Config.DEFAULT_SERVER) == false) {
			txt_bluendoServer = new UITextField(rm
					.getString(ResourceIDs.STR_SERVER_EXPLORE),
					Config.DEFAULT_SERVER, 255, TextField.UNEDITABLE);
			this.mainPanel.addItem(txt_bluendoServer);
			this.addServerGateways(txt_bluendoServer, Config.DEFAULT_SERVER);
		}
		this.mainPanel.addItem(new UISeparator(2, 0x888888));
		refresh_container = UIUtils.easyCenterLayout(refresh_gateways, 150);
		this.mainPanel.addItem(refresh_container);
		refresh_gateways.setAnchorPoint(UIGraphics.HCENTER);
	}

	private void addServerGateways(UITextField txt_server, String serverJid) {

		Enumeration en = rs.gateways.keys();
		while (en.hasMoreElements()) {
			String from = (String) en.nextElement();
			Object[] data = (Object[]) rs.gateways.get(from);
			String name = (String) data[0];
			UIImage img = (UIImage) UIGateway.getGatewayIcons((String) data[1]);
			String savedServerJid = (String) data[2];
			if (serverJid.equals(savedServerJid)) {
				//UILabel ithTransport = new UILabel(img, name);
				UIButton ithTransport = new UIButton(img, name);
				ithTransport.setButtonColor(0xeeeeee);
				UIHLayout buttonLayout = UIUtils.easyCenterLayout(ithTransport,
						(UICanvas.getInstance().getWidth() * 2) / 3);
				ithTransport.setAnchorPoint(UIGraphics.LEFT);
				ithTransport.setFocusable(true);
				ithTransport.setStatus(from);
				this.transports.addElement(ithTransport);
				this.mainPanel.addItem(buttonLayout);
			}
		}
	}

	public void menuAction(UIMenu menu, UIItem c) {
		UICanvas.getInstance().close(this);
	}

	public void itemAction(UIItem cmd) {

		if (this.transports.contains(cmd)) {
			this.openRegisterScreen(cmd);
		} else if (cmd == refresh_gateways) {
			rs.gateways.clear();
			rs.gatewaysServer = this.txt_myServer.getText();
			myServerGateways[0] = Integer.MAX_VALUE;
			rs.queryDiscoItems(rs.gatewaysServer, myServerGateways, 60000);
			// serverGateways could be null if not authenticated yet
			String localServer = Config.DEFAULT_SERVER;
			if (rs.gatewaysServer.equals(localServer) == false) {
				rs.queryDiscoItems(localServer, defaultServerGateways, 60000);
			}
			this.mainPanel.removeItem(refresh_container);
			progressGauge = new UIGauge(rm.getString(ResourceIDs.STR_WAIT),
					false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
			this.mainPanel.addItem(progressGauge);
			this.askRepaint();
			cachedServer = txt_myServer.getText();
			new Thread() {
				public void run() {
					updateScreen();
				}
			}.start();
		} else if (cmd == txt_myServer) {
			if (cachedServer.equals(txt_myServer.getText()) == false) {
				itemAction(refresh_gateways);
			}
		}
	}

	private void updateScreen() {
		int count = 15;
		// At most 15 seconds or when the iq are finished
		while (count-- > 0
				&& (myServerGateways[0] + defaultServerGateways[0]) > 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		synchronized (UICanvas.getLock()) {
			progressGauge.cancel();
			refresh_gateways.setSelected(false);
			this.remove(progressGauge);
			this.append(refresh_container);
			UICanvas.getInstance().close(this);
			UICanvas.getInstance().open(
					new GatewayScreen(txt_myServer.getText()), true,
					RosterScreen.getInstance());
		}
	}

	private void openRegisterScreen(UIItem cmd) {
		String from = (String) cmd.getStatus();
		WaitScreen ws = new WaitScreen(this.getTitle(), null);
		EventQueryRegistration eqr = EventDispatcher.addDelayedListener(ws,
				true);
		RosterScreen.RegisterHandler rh = rs.new RegisterHandler(eqr);
		UICanvas.getInstance().open(ws, true);
		Iq iq = new Iq(from, Iq.T_GET);
		iq.addElement(XmppConstants.IQ_REGISTER, Iq.QUERY);
		// from this point on all the subscription 
		// "from" and "username@from"
		// will be autoaccepted from this
		XMPPClient xmppClient = XMPPClient.getInstance();
		xmppClient.autoAcceptGateways.addElement(from);
		iq.send(xmppClient.getXmlStream(), rh);
		UICanvas.getInstance().close(this);
	}


}
