package it.yup.dispatch;


/**
 * Class used for associating packet listeners and queries
 */
public class ListenerRegistration {
	public EventQuery query;
	public Object listener;
	public boolean oneTime;

	public ListenerRegistration(EventQuery query, Object listener,
			boolean oneTime) {
		this.query = query;
		this.listener = listener;
		this.oneTime = oneTime;
	}
}