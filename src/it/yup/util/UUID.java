package it.yup.util;

public class UUID {

	private static long lastTs = 0;

	public synchronized static UUID getUUID(String jid) {
		return new UUID(jid);
	}

	private long timeStamp;
	private String jid;

	private UUID(String jid) {
		while (System.currentTimeMillis() == lastTs) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
		lastTs = System.currentTimeMillis();
		this.timeStamp = lastTs;
		this.jid = jid;
	}

	public String toString() {
		String res = "uuid" + ":" + "xmpp" + this.timeStamp;
		if (this.jid != null) {
			res += ("+" + jid);
		}
		return res;
	}
}
