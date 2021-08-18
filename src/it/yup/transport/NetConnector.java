/**
 * 
 */
package it.yup.transport;

import java.io.IOException;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;


//#mdebug

import it.yup.util.log.Logger;


//#enddebug

/**
 * @author pinturic
 *
 */
public class NetConnector {


	public static Connection open(String connectionString) throws IOException {
		// #ifndef RIM
		return Connector.open(connectionString);
		// #endif
	}

}
