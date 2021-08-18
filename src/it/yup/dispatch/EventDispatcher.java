package it.yup.dispatch;

import it.yup.xmlstream.BasicXmlStream;
import it.yup.xmlstream.PacketListener;

import java.util.Vector;

public class EventDispatcher {
	public static Vector eventListeners = new Vector(10);
	/* EVENT CONSTANTS */
	public static String STREAM_CONNECTED = "_01";
	public static String STREAM_INITIALIZED = "_02";
	// public static String STREAM_DISCONNECTED = "_03";
	public static String STREAM_ERROR = "_04";
	// public static String STREAM_RESOURCE_BOUND = "_05";
	// public static String STREAM_SESSION_OPENED = "_06";
	public static String STREAM_ACCOUNT_REGISTERED = "_07";
	public static String CONNECTION_LOST = "_08";
	public static String STREAM_TERMINATED = "_09";
	// public static String AUTHENTICACTION_FAILED = "_09";
	public static String REGISTRATION_FAILED = "_10";
	public static String CONNECTION_FAILED = "_11";
	public static String NOT_AUTHORIZED = "_12";
	//public static String STREAM_REGISTRATION_ERROR = "_08";
	public static String TLS_INITIALIZED = "_13";
	public static String STREAM_AUTHENTICATED = "_14";
	public static String COMPRESSION_INITIALIZED = "_15";
	public static String UNMATCHED_STANZA = "_16";
	private static String DELAYED_OPERATION = "_17";

	/**
	 * Add an event listener, it may be a {@link EventListener}
	 * @param query
	 * @param listener a {@link EventListener}
	 * @return the registration object that may be used for unregistering the listener
	 */
	public static EventQueryRegistration addEventListener(EventQuery query,
			EventListener eventListener) {
		return addListener(query, eventListener, false);
	}

	/**
	 * Add an event listener that can be fired only one, it may be either a 
	 * {@link PacketListener} or a {@link EventListener}
	 * @param query
	 * @param listener either a {@link PacketListener} or a {@link EventListener}
	 * @return the registration object that may be used for unregistering the listener
	 */
	public static EventQueryRegistration addOnetimeEventListener(
			EventQuery query, EventListener listener) {
		return addListener(query, listener, true);
	}

	public static EventQueryRegistration addDelayedListener(
			EventListener listener, boolean onetime) {
		EventQuery eq = new EventQuery(EventDispatcher.DELAYED_OPERATION
				+ System.currentTimeMillis(), null, null);
		return addListener(eq, listener, onetime);
	}
	
	public static void dispatchDelayed(EventQueryRegistration eqr, Object source) {
		String event = ((ListenerRegistration) eqr.o).query.event;
		dispatchEvent(event, source);
	}

	private static EventQueryRegistration addListener(EventQuery query,
			EventListener listener, boolean onetime) {
		ListenerRegistration ld = new ListenerRegistration(query, listener, onetime);
		synchronized (eventListeners) {
			eventListeners.addElement(ld);
		}
		
		return new EventQueryRegistration(ld, eventListeners);
	}

	/**
	 * Dispatch an XmlStream event
	 * */
	public static void dispatchEvent(String event, Object source) {
		ListenerRegistration[] regs = null;
		synchronized (eventListeners) {
			regs = new ListenerRegistration[eventListeners.size()];
			eventListeners.copyInto(regs);
		}
		for (int i = 0; i < regs.length; i++) {
			ListenerRegistration listenerData = regs[i];
			if (listenerData.query.event.equals(EventQuery.ANY_EVENT)
					|| event.equals(listenerData.query.event)) {
				EventListener l = (EventListener) listenerData.listener;
				l.gotStreamEvent(event, source);
				if (listenerData.oneTime) {
					synchronized (eventListeners) {
						eventListeners.removeElement(listenerData);
					}
				}
			}
		}
	}

	/**
	 * Remove an event listener passing the {@link EventQueryRegistration} received from 
	 * {@link BasicXmlStream#addPacketListener(EventQuery, Object)} or
	 * {@link BasicXmlStream#addOnetimePacketListener(EventQuery, Object)} 
	 * @param registration
	 */
	public static void removeEventListener(EventQueryRegistration registration) {
		registration.remove();
	}

	public static void removeAllElements() {
		eventListeners.removeAllElements();
	}
}
