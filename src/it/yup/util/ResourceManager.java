/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: ResourceManager.java 2325 2010-11-15 20:07:28Z luca $
*/

package it.yup.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

public class ResourceManager {

	private static ResourceManager manager = null;

	private static String defaultLang = "en";

	private static String[] defaultFiles = new String[] { "/locale/common" };

	private static String[][] replacePatterns = {};

	private Hashtable resources = new Hashtable();

	public static void setDefaultProperties(String lang, String[] commonFiles,
			String[][] replacePatterns) {
		ResourceManager.defaultLang = lang;
		ResourceManager.defaultFiles = commonFiles;
		ResourceManager.replacePatterns = replacePatterns;
	}

	private ResourceManager(String[] files, String locale) throws Exception {
		for (int i = 0; i < files.length; i++) {
			InputStream is = this.getClass().getResourceAsStream(
					files[i] + "." + locale);
			try {

				// the max length that the common file can contain
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int b;
				while ((b = is.read()) != -1) {
					byte c = (byte) b;
					if (b == '\n') {
						String str = Utils.getStringUTF8(baos.toByteArray());
						Vector tokens = Utils.tokenize(str, '\t');
						Object key = tokens.elementAt(0);
						Object element = tokens.elementAt(1);
						element = filter(element);
						resources.put(key, element);
						baos.reset();
					} else {
						baos.write(c);
					}
				}
				is.close();
			} catch (IOException e) {
				throw new Exception("Error in reading conf: " + e.getClass());
			}
		}
	}

	private Object filter(Object element) {
		String newEl = ((String) element);
		for (int i = 0; replacePatterns != null && i < replacePatterns.length; i++) {
			String[] ithPattern = replacePatterns[i];
			newEl = Utils.replace(newEl, ithPattern[0], ithPattern[1]);
		}
		return newEl;
	}

	public static ResourceManager getManager(String[] name, String locale) {
		if (ResourceManager.manager == null) {
			try {
				manager = new ResourceManager(name, locale);
			} catch (Exception e) {
				return null;
			}
		}
		return manager;
	}

	public static ResourceManager getManager(String[] name) {
		return getManager(name, defaultLang);
	}

	public static ResourceManager getManager() {
		return getManager(defaultFiles, defaultLang);
	}

	public String getString(int id) {
		return (String) resources.get("" + id);
	}
}
