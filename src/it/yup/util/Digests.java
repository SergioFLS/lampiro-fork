package it.yup.util;

//#ifndef MIDP
//@
//@import java.security.MessageDigest;
//@import java.security.NoSuchAlgorithmException;
//@
//#endif
// #ifdef MIDP

import org.bouncycastle.crypto.digests.GeneralDigest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA1Digest; 

//#endif

public class Digests {

	/**
	 * 
	 * @param data
	 * @param digestType
	 * @return the digest or null if the requested digest is not supported
	 */
	static public byte[] digest(byte data[], String digestType) {

		//#ifndef MIDP
//@		MessageDigest mdigest;
//@		try {
//@			mdigest = MessageDigest.getInstance(digestType);
//@			return mdigest.digest(data);
//@		} catch (NoSuchAlgorithmException e) {
//@			// TODO Auto-generated catch block
//@			e.printStackTrace();
//@		}
//@		return null;
//@
		//#endif 
// #ifdef MIDP
		
				GeneralDigest digest = null;
				if (digestType.equals("sha1")) {
					digest = new SHA1Digest();
				} else if (digestType.equals("md5")) {
					digest = new MD5Digest();
				} else {
					return null;
				}
		
				// XXX too many copies of data, modify the hash functions so that they write
				// the result to a byte array
				digest.update(data, 0, data.length);
				// some emulators fail on calling getByteLength  
				byte out[] = null;
				try {
					out = new byte[digest.getByteLength()];
				} catch (Error e) {
					out = new byte[64];
				}
		
				int len = digest.doFinal(out, 0);
				byte result[] = new byte[len];
				System.arraycopy(out, 0, result, 0, len);
				return result;
		//#endif
	}

	static public byte[] digest(String msg, String digestType) {
		return digest(Utils.getBytesUtf8(msg), digestType);
	}

	/**
	 * Compute a digest of a message
	 * 
	 * @param msg
	 *            The message whose digest must be computed. The encoding must
	 *            be utf-8
	 * @param digestType
	 *            sha1 or md5
	 * @return a string representing the digest
	 */
	public static String hexDigest(String msg, String digestType) {
		return Utils.bytesToHex(digest(msg, digestType));
	}

}
