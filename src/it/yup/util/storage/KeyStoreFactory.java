package it.yup.util.storage;

// #ifdef MIDP
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

// #endif

public class KeyStoreFactory {
	public static KeyStore getStore(String name) {
		// #ifdef MIDP
		return new RMSIndex(name);
		// #endif
// #ifndef MIDP
//@				throw new RuntimeException("Not implemented yet");
		// #endif
	}

	public static boolean storeExist(String name) {
		// #ifdef MIDP
		return RMSIndex.rmExist(name);
		// #endif
// #ifndef MIDP
//@				throw new RuntimeException("Not implemented yet");
		// #endif
	}

	public static void deleteStore(String storeName) throws Exception {
		// #ifdef MIDP
		try {
			RecordStore.deleteRecordStore(storeName);
		} catch (RecordStoreNotFoundException e) {
			throw new Exception("Not found: " + storeName);
		} catch (RecordStoreException e) {
			throw new Exception("Error in deleting: " + storeName);
		}
		// #endif
// #ifndef MIDP
//@				throw new RuntimeException("Not implemented yet");
		// #endif
	}
}
