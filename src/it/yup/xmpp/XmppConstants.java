package it.yup.xmpp;

public class XmppConstants {

	public static String NS_IQ_DISCO_INFO = "http://jabber.org/protocol/disco#info";
	public static String NS_IQ_DISCO_ITEMS = "http://jabber.org/protocol/disco#items";
	public static String NS_COMMANDS = "http://jabber.org/protocol/commands";
	public static String NS_CAPS = "http://jabber.org/protocol/caps";
	public static String NS_BLUENDO_CAPS = "http://bluendo.com/protocol/caps";
	public static String NS_BLUENDO_CFG = "xmpp:bluendo:cfg";
	public static String NS_BLUENDO_PUBLISH = "bluendo:http:publish:0";
	public static String NS_BOOKMARKS = "storage:bookmarks";
	public static String NS_IBB = "http://jabber.org/protocol/ibb";
	public static String NS_MUC = "http://jabber.org/protocol/muc";
	public static String NS_PRIVATE = "jabber:iq:private";
	public static String NS_JABBER_VERSION = "jabber:iq:version";
	public static String NS_PUBSUB = "http://jabber.org/protocol/pubsub";
	public static String NS_PUBSUB_EVENT = "http://jabber.org/protocol/pubsub#event";
	public static String NS_ROSTERX = "http://jabber.org/protocol/rosterx";
	public static String NS_MUC_USER = "http://jabber.org/protocol/muc#user";
	public static String NS_MUC_OWNER = "http://jabber.org/protocol/muc#owner";
	public static String NS_NICK = "http://jabber.org/protocol/nick";
	public static String NS_IQ_ROSTER = "jabber:iq:roster";
	//public static String NS_IQ_ROSTER_NEW = "jabber:iq:roster#new";
	public static String NS_URI_XMPP_GATEWAY_FBREG= "uri:xmpp:gateway#fbreg";
	public static String NS_ROSTERVER = "urn:xmpp:features:rosterver";
	public static String NS_STORAGE_LAMPIRO = "storage:lampiro";
	public static String NS_VCARD_UPDATE = "vcard-temp:x:update";
	public static String NS_UUID = "urn:bluendo:uuid:0";
	public static String NS_DELAY = "urn:xmpp:delay";
	public static String NS_MEDIA_ELEMENT = "urn:xmpp:media-element";
	public static String NS_BOB = "urn:xmpp:bob";
	//public static String NS_URI_XMPP_GATEWAY_NEW = "uri:xmpp:gateway#new";
	public static String NS_XHTML_IM = "http://jabber.org/protocol/xhtml-im";
	public static String NS_OOROS_GLIDER_MENU = "ns:ooros:glider:menu:0";
	public static String NS_OOROS_GLIDER_MENU_AUTOADD = "ns:ooros:glider:menu:0#autoadd";
	public static String NS_OOROS_GLIDER_MENU_NEWS = "ns:ooros:glider:news:0";
	public static String NS_OOROS_SERVICES = "ooros:services:0";
	public static String NS_XMPP_SUBSCRIPTIONS= "xmpp:subscriptions";
	public static String XML_NS = "http://www.w3.org/XML/1998/namespace";
	
	public static String STORAGE = "storage";
	public static String ROSTER = "roster";
	public static String MIDP_PLATFORM = "http://bluendo.com/midp#platform";
	public static String JABBER_X_DATA = "jabber:x:data";
	public static String JABBER_IQ_GATEWAY = "jabber:iq:gateway";
	public static String IQ_REGISTER = "jabber:iq:register";
	public static String JINGLE = "urn:xmpp:jingle:1";
	public static String JINGLE_FILE_TRANSFER = "urn:xmpp:jingle:apps:file-transfer:1";
	public static String FILE_TRANSFER = "http://jabber.org/protocol/si/profile/file-transfer";
	public static String JINGLE_IBB_TRANSPORT = "urn:xmpp:jingle:transports:ibb:0";
	public static String BLUENDO_PUBLISH = "bluendo:http:publish:0";
	public static String USERID = "USERID";
	public static String VCARD_TEMP = "vcard-temp";
	public static String VCARD = "vCard";
	public static String BINVAL = "BINVAL";
	public static String NICKNAME = "NICKNAME";
	public static String PHOTO = "PHOTO";
	public static String FN = "FN";
	public static String EMAIL = "EMAIL";
	public static String IDENTITY = "identity";
	//public static String LOGON = "logon";
	public static String BLUENDO_XMLRPC = "bluendo:bxmlrpc:0";
	public static String BLUENDO_REGISTER = "bluendo:register:0";
	public static String DELAY = "delay";
	public static String CONFERENCE = "conference";
	public static String SECTION = "section";
	public static String PUBSUB = "pubsub";
	public static String PUBLISH = "publish";
	public static String ITEMS = "items";
	public static String ITEM = "item";
	public static String NODE = "node";
	public static String CATEGORY = "category";
	public static String EVENT = "event";
	public static String COMMAND = "command";
	public static String ACTION = "action";
	public static String DATA = "data";
	public static String UUID = "uuid";
	public static String FEATURE = "feature";
	public static String URI = "uri";
	public static String IMAGE = "image";
	public static String AUDIO = "audio";
	public static String VIDEO = "video";
	public static String MEDIA = "media";
	public static String NICK = "nick";
	public static String TEXT = "text";
	public static String CODE = "code";
	public static String ASK = "ask";
	public static String REMOVE = "remove";
	public static String SUBSCRIPTION = "subscription";
	public static String FULL_NODE = "fullnode";
	public static String XHTML_IM = "http://jabber.org/protocol/xhtml-im";
	public static String UPDATED = "updated";
	public static String XMPP = "xmpp";
	public static String VER = "ver";
	public static String VERSION = "version";
	public static String STATUS = "status";
	public static String STATE = "state";
	
	// The next ones are variables related to the management of multimedia data:
	// audioTypes and imageTypes are the suffix handled and recognized by the multimedia related
	// screens. audioType and imgType are constants used to identify a file in the
	// store.
	public static int IMG_TYPE = 0;
	public static int AUDIO_TYPE = 1;
	public static int VIDEO_TYPE = 2;
	
	// store related names
	
	public static final String SECURE_STORE = "secure";
	/*
	 * All the gateways to which I am registered to
	 */
	public static String REGISTERED_GATEWAYS = "reg_gateways";
	
	// alert constants
	public static final int ALERT_COMMAND_INFO = 1;
	public static final int ALERT_DATA = 2;
	public static final int ALERT_CONNECTION = 3;
	public static final int ALERT_WAIT_COMMAND = 4;
	public static final int ALERT_CANCELED_COMMAND = 5;
	public static final int ALERT_CANCELING_COMMAND = 6;
	public static final int ALERT_FINISHED_COMMAND = 7;
	public static final int ALERT_ERROR_COMMAND = 8;
	public static final int ALERT_NOTE = 9;
	
	public static String[][] errorCodes = new String[][] {
		{ "400", "Bad request" }, { "401", "Not authorized" },
		{ "403", "Forbidden" }, { "404", "Item not found" },
		{ "405", "Operation not allowed" }, { "406", "Not acceptable" },
		{ "408", "Request timeout" }, { "407", "Registration required" },
		{ "409", "Conflict" }, { "500", "Internal server error" },
		{ "501", "Feature not implemented" },
		{ "502", "Remote server error" }, { "503", "Service unavailable" },
		{ "504", "Remote server timeout" }, { "510", "Disconnected" }, };
	
	public static String getErrorString(String code) {
		for (int i = 0; i < XmppConstants.errorCodes.length; i++) {
			String[] ithCode = XmppConstants.errorCodes[i];
			if (ithCode[0].equals(code)) { return (ithCode[1]); }
		}
		return "";
	}
	
	

}
