/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: Config.java 2325 2010-11-15 20:07:28Z luca $
*/

package it.yup.client;

//#mdebug

import it.yup.util.log.Logger;

// #enddebug

import it.yup.xml.BProcessor;
import it.yup.xml.Element;
import it.yup.xmpp.XmppConstants;
import it.yup.xmpp.packets.Presence;
import it.yup.util.Utils;
import it.yup.util.storage.KeyStore;
import it.yup.util.storage.KeyStoreFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

// TODO 1: delete any reference "old record store" (we don't need to upgrade from it anymore)
// TODO 2: check the return value in all the ste methods, since now everything returns false 

/**
 * Client Configuration
 */
public class Config {

	public static String CLIENT_NAME = "Lampiro";

	public static String CLIENT_ADDRESS = "http://lampiro.bluendo.com ";

	private static String version = "11.3";

	//#expand public static String lang = "%LANG%";
	public static String lang = "en";
	
	public static final String ATOM = "atom";
	public static final String ATOM_JID = "rss.ooros.com";
	public static final String VANADIS_JID = "testff.jabber.bluendo.com";
	public static final String NIELS_JID = "niels.moneiro.biz";
	public static final short NEWS_COUNT = 0x0033;

	/** config index in the record store */
	public static final int RNUM_CONFIG = 1;

	/** roster index in the record store */
	public static final int RNUM_ROSTER = 2;

	private Hashtable properties = new Hashtable();

	/** Connection url used by the GPRS connector */
	// public static final String GPRS_CONNECTION_URL =
	// "socket://www.bluendo.com:5280";
	public static final String GPRS_CONNECTION_URL = "socket://localhost:10080";
	// public static final String GPRS_CONNECTION_URL =
	// "socket://bosh.bluendo.com:10080";

	/** URL (host/port) of the GPRS/HTTP gateway */
	// public static final String HTTP_GW_HOST = "dalek";
	public static final String HTTP_GW_HOST = "bosh.bluendo.com";

	/** path of the GPRS/HTTP gateway */
	public static final String HTTP_GW_PATH = "/httpb";

	/** path of the GPRS/HTTP gateway */
	public static String SRV_QUERY_PATH = "http://services.bluendo.com/srv/?domain=";

	public static String DEFAULT_SERVER = "jabber.bluendo.com";

	/**
	 * time the server should wait before sending a response if no data is
	 * available
	 */
	public static final int WAIT_TIME = 30;

	/**
	 * The time after which iq answer not arrived are considered as expired and
	 * removed
	 */
	public static int MAX_PERM_TIME = 120000;

	// /** default value keepalive of the plain socket */
	// XXX We may keep this but the transport should not read it from Config
	private static final int SO_KEEPALIVE = 60 * 2 * 1000;

	/** Config instance */
	private static Config instance;

	public static String TRUE = "t";
	public static String FALSE = "f";

	// Constants for keys saved in the rms
	public static String CONFIG = "config";

	public static String VCARDS = "vcards";

	public static String GLIDER_COMMANDS = "GLIDER_COMMANDS";
	public static String DISCO_CACHE = "disco_cache";

	/*
	 * The db of all the known capabilities
	 */
	public static String KNOWN_CAPS = "known_caps";

	public static String CAPS_PREFIX = "caps";

	public static String GROUPS_POSITION = "groups_position";

	/** the bluendo assistent */
	public static String CLIENT_AGENT = "lampiro@golem.jabber.bluendo.com";

	/** the bluendo assistent */
	public static String CLIENT_SERVICES = "lampiro@golem.jabber.bluendo.com";

	/** the bluendo assistent */
	public static String DEFAULT_COLOR = "0";

	/** the configuration for client and trusted services gotten from server **/
	public static String CLIENT_CONFIG = "lampiro_config";

	public static String RESOURCE_PREFIX = "";

	public static String UUID_SERVER = "lampiro@golem.jabber.bluendo.com/lampiro";

	public static String audioTypes[] = new String[] { "amr", "amr-xb", "pcm",
			"ulaw", "gsm", "wav", "au", "raw" };

	public static String imageTypes[] = new String[] { "jpeg", "png", "jfif",
			"bmp" };

	// constants for values saved in the record store
	/** server name */
	public static short SERVER = 0x0000;
	/** user name */
	public static short USER = 0x0001;
	/** password */
	public static short PASSWORD = 0x0002;
	/** mail address */
	public static short EMAIL = 0x0003;
	/** connecting server (after a SRV_RECORD query ) */
	public static short CONNECTING_SERVER = 0x0004;
	/** sw version */
	public static short VERSION = 0x0005;
	/** sw version */
	public static short SILENT = 0x0006;
	/** logged once */
	public static short LOGGED_ONCE = 0x0007;
	/** keeplive for plain sockets */
	public static short KEEP_ALIVE = 0x0008;
	/** flag which is true after the first succesful login and roster update */
	public static short CLIENT_INITIALIZED = 0x0009;
	/** last "show" used in the presence */
	public static short LAST_PRESENCE_SHOW = 0x0010;
	/** last status message */
	public static short LAST_STATUS_MESSAGE = 0x0011;
	/** last compression settings used */
	public static short COMPRESSION = 0x0019;
	/** last TLS settings used */
	public static short TLS = 0x0020;
	/** last priority */
	public static short LAST_PRIORITY = 0x0017;
	/** XMPP resource */
	public static short YUP_RESOURCE = 0x0021;
	/** Has a qwerty keyboard */
	public static short QWERTY = 0x0022;

	/**
	 * Using bit masks
	 * 
	 * vibration settings:
	 * <ul>
	 * <li>0x00: none</li>
	 * <li>0x01: only when hidden</li>
	 * <li>0x02: only when shown</li>
	 * <li>0x03: always</li>
	 * </ul>
	 * 
	 * tone settings:
	 * <ul>
	 * <li>0x00: none</li>
	 * <li>0x04: only when hidden</li>
	 * <li>0x08: only when shown</li>
	 * <li>0x0C: always</li>
	 * </ul>
	 * *
	 * 
	 */
	public static short VIBRATION_AND_TONE_SETTINGS = 0x0012;

	/** The theme associated to the Application */
	public static short COLOR = 0x0013;

	/** tone volume */
	public static short TONE_VOLUME = 0x0014;

	/**
	 * UICanvas keys for left and right. String is a comma-separated couple of
	 * integers representing (in order) left and right key
	 */
	public static short CANVAS_KEYS = 0x0015;

	/*
	 * Font Size for roster and chat
	 */
	public static short FONT_SIZE = 0x0016;

	/*
	 * Font Size for menus
	 */
	public static short FONT_MENU_SIZE = 0x0027;

	/*
	 * Font Size for roster and chat
	 */
	public static short HISTORY_SIZE = 0x0018;

	/*
	 * The accepted gateways (i.e. the ones whose contacts do not need manual authorization)
	 */
	public static short ACCEPTED_GATEWAYS = 0x0023;

	/*
	 * The unique identifier associated to this mobile phone
	 */
	public static short UUID = 0x0024;

	/*
	 * the album data
	 */
	public static short MM_ALBUM = 0x0025;

	/*
	 * The resolution of the camera in capturing images 
	 */
	public static final short CAMERA_RESOLUTION = 0x0026;

	/*
	 * Autoreconnnect client after disconnection;
	 * 0 - no
	 * 1 - yes
	 * 2 - file not found
	 */
	public static final short AUTORECONNECT = 0x0028;

	/*
	 * Combination of allowed networks
	 * 1 - WIFI
	 * 2 - Direct
	 * 4 - BIBS
	 * 8 - BES/MDS 
	 */
	public static final short ALLOWED_NETWORKS = 0x0029;

	/** maximum wait time for a packet (should we let configure this ) */
	public static final int TIMEOUT = -1;

	private Hashtable cachedCaps = new Hashtable(7);

	/**
	 * Build the config, passing an appropriate store and using the stored
	 * values (if any), or use the default values
	 * 
	 * @param store
	 * @return
	 */
	public synchronized static Config makeInstance(KeyStore store) {
		if (instance == null) {
			instance = new Config(store);
			instance.loadFromStorage();
			instance.loadCapabilities();
		}
		return instance;
	}

	/**
	 * Get the configuration
	 */
	public synchronized static Config getInstance() {
		return instance;
	}

	/** Make the constructur private -> singleton */
	private Config(KeyStore store) {
		this.rms = store;
	}

	/*
	 * The rmsIndex containing all the data
	 */
	private KeyStore rms = null;

	/*
	 * The configuration has been updated; i.e. Lampiro has been updated
	 */
	public boolean updatedConfig = false;

	/**
	 * Load the the configuration from the RMS TODO: check the return value
	 * where load is called
	 * 
	 * @return false in case of error
	 */
	private synchronized boolean loadFromStorage() {
		try {
			byte[] b = null;
			boolean needSave = false;

			// first check for existence of the "new recordStore"
			if (rms.open()) {
				b = rms.load(Config.CONFIG.getBytes());
				rms.close();
			} else {
				setDefaults();
			}
			if (b != null && b.length != 0) {
				DataInputStream in = new DataInputStream(
						new ByteArrayInputStream(b));
				while (in.available() > 0) {
					short code = in.readShort();
					String val = in.readUTF();
					properties.put(String.valueOf(code), val);
				}
				in.close();
			}
			if (needSave == true) this.saveToStorage();
			String _version = getProperty(Config.VERSION);

			// new installation
			if (_version == null) {
				setDefaults();
			}
			// updated installation
			else if (_version.compareTo(Config.version) != 0) {
				// the software has been updated, handle here the "update" logic
				// setDefaults();
				setProperty(Config.VERSION, Config.version);
				this.saveToStorage();
				this.updatedConfig = true;
			}
		} catch (Exception e) {
			this.resetStorage(true);
			//			XMPPClient.getInstance().showAlert(AlertType.ERROR,
			//					Config.ALERT_DATA, Config.ALERT_DATA, e.getMessage());
			// TODO log the error
			return false;
		}
		return true;
	}

	private void setDefaults() {
		setProperty(Config.VERSION, Config.version);
		setDefault(Config.USER, "");
		setDefault(Config.SERVER, "");
		setDefault(Config.EMAIL, "");
		setDefault(Config.CONNECTING_SERVER, "");
		setDefault(Config.SILENT, "y");
		setDefault(Config.LOGGED_ONCE, "0");
		setDefault(Config.KEEP_ALIVE, "" + SO_KEEPALIVE);
		setDefault(Config.CLIENT_INITIALIZED, Config.FALSE);
		setDefault(Config.LAST_STATUS_MESSAGE, Config.CLIENT_NAME + " ("
				+ Config.CLIENT_ADDRESS + ")");
		setDefault(Config.LAST_PRESENCE_SHOW, Presence.SHOW_ONLINE);
		setDefault(Config.FONT_SIZE, "1");
		setDefault(Config.COLOR, Config.DEFAULT_COLOR);
		saveToStorage();
	}

	/**
	 * Reset the options. If hard is set to true even the login credentials are
	 * reset
	 * 
	 * @param hard
	 */
	public synchronized boolean resetStorage(boolean hard) {
		Config cfg = this;
		String user = null;
		String password = null;
		String server = null;
		String email = null;
		String connectingServer = null;
		String uid = null;
		if (hard == false) {
			// cfg = Config.getInstance(); XXX why?
			try {
				user = cfg.getProperty(Config.USER);
				server = cfg.getProperty(Config.SERVER);
				password = cfg.getProperty(Config.PASSWORD);
				email = cfg.getProperty(Config.EMAIL);
				connectingServer = cfg.getProperty(Config.CONNECTING_SERVER);
				uid = cfg.getProperty(Config.UUID);
			} catch (Exception e) {
				resetStorage(true);
				return false;
			}
		}
		properties.clear();
		if (hard == false) {
			// cfg = Config.getInstance(); XXX why?
			cfg.setProperty(Config.USER, user);
			cfg.setProperty(Config.PASSWORD, password);
			cfg.setProperty(Config.SERVER, server);
			cfg.setProperty(Config.EMAIL, email);
			cfg.setProperty(Config.CONNECTING_SERVER, connectingServer);
			cfg.setProperty(Config.UUID, uid);
		}
		// reload a new rms since it has to be cleaned
		if (rms != null && rms.getName() != null) {
			String name = rms.getName();
			try {
				KeyStoreFactory.deleteStore(name);
			} catch (Exception e) {
				e.printStackTrace();
			}
			rms = KeyStoreFactory.getStore(name);
		}
		// so that the new options are automatically reloaded
		return this.saveToStorage() && this.loadFromStorage();
	}

	public synchronized boolean saveToStorage() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(baos);

			Enumeration en = properties.keys();
			while (en.hasMoreElements()) {
				String code = (String) en.nextElement();
				out.writeShort(Integer.parseInt(code));
				out.writeUTF((String) properties.get(code));
			}
			byte[] data = baos.toByteArray();
			this.rms.open();
			this.rms.store(Config.CONFIG.getBytes(), data);
			this.rms.close();
		} catch (Exception e) {
			// #mdebug
			Logger.log("Error in saving to storage: " + e.getMessage(),
					Logger.DEBUG);

			// #enddebug
			//			XMPPClient.getInstance().showAlert(AlertType.ERROR,
			//					Config.ALERT_DATA, Config.ALERT_DATA, e.getMessage());
			return false;
		}
		return true;
	}

	public synchronized byte[] getData(byte key[]) {
		this.rms.open();
		byte[] res = this.rms.load(key);
		this.rms.close();
		return res;
	}

	public synchronized void setData(byte key[], byte data[]) {
		this.rms.open();
		this.rms.store(key, data);
		this.rms.close();
	}

	public String getProperty(short code) {
		return (String) properties.get(String.valueOf(code));
	};

	public String getProperty(short code, String default_value) {
		String s = (String) properties.get(String.valueOf(code));
		return (s == null) ? default_value : s;
	};

	public void setProperty(short code, String value) {
		properties.put(String.valueOf(code), value);
	}

	/**
	 * Set the default value for a property if none is given
	 * 
	 * @param code
	 * @param default_value
	 */
	private void setDefault(short code, String default_value) {
		if (!this.properties.containsKey(String.valueOf(code))) {
			setProperty(code, default_value);
		}
	}

	public synchronized void loadCapabilities() {
		byte[] capsRaw = this.getData(Utils.getBytesUtf8(KNOWN_CAPS));
		Element el = null;
		try {
			el = BProcessor.parse(capsRaw);
			Element[] children = el.getChildren();
			for (int i = 0; i < children.length; i++) {
				Element element = children[i];
				String tempNode = element.getAttribute(XmppConstants.FULL_NODE);
				cachedCaps.put(tempNode, element);
			}
		} catch (Exception e) {
			// #mdebug
			Logger.log("Error in loading capabilities: received packet: "
					+ e.getClass(), Logger.DEBUG);
			// #enddebug
			// reset the capabilities
			cachedCaps.clear();
			el = new Element(XmppConstants.ITEMS, KNOWN_CAPS);
			capsRaw = BProcessor.toBinary(el);
			this.setData(Utils.getBytesUtf8(KNOWN_CAPS), capsRaw);
			return;
		}

	}

	public synchronized void saveCapabilities(String node, String ver,
			Element query) {
		String combi = node + ver;
		// should not happen
		if (cachedCaps.contains(combi)) return;

		Element el = new Element(XmppConstants.ITEMS, KNOWN_CAPS);
		query.setAttribute(XmppConstants.FULL_NODE, combi);
		cachedCaps.put(combi, query);
		Enumeration en = cachedCaps.keys();
		while (en.hasMoreElements()) {
			String tempNode = (String) en.nextElement();
			Element tempEl = (Element) cachedCaps.get(tempNode);
			el.addElement(tempEl);
		}

		byte[] capsRaw = BProcessor.toBinary(el);
		this.setData(Utils.getBytesUtf8(KNOWN_CAPS), capsRaw);
	}

	public synchronized Element getCapabilities(String node, String ver) {
		String combi = node + ver;
		Element cachedCap = (Element) cachedCaps.get(combi);
		return cachedCap;
	}

}
