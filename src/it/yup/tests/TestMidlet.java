// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: TestMidlet.java 2329 2010-11-16 14:12:50Z luca $
*/

package it.yup.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.midlet.MIDlet;

import it.yup.ui.MetaVector;

// #mdebug

import it.yup.util.log.Logger;
import it.yup.util.log.StderrConsumer;
import it.yup.util.Utils; 

// #enddebug

import it.yup.xml.KXmlParser;
import it.yup.xml.KXmlProcessor;
import it.yup.xml.KXmlSerializer;
import it.yup.client.XMPPClient;
import it.yup.xml.Element;
import it.yup.xml.BProcessor;
import it.yup.xmpp.XmppConstants;

/**
 * YUP Main midlet
 */
public class TestMidlet extends MIDlet {

	/** The main display */
	public static Display disp;

	/** The midlet instance */
	public static TestMidlet yup;

	private XMPPTestClient xmpp = null;

	private Form form = new Form("Test midlet");
	public StringItem log = new StringItem("Bytes", "offline");

	public TestMidlet() {

		// #debug		
		Logger.addConsumer(new StderrConsumer());

		disp = Display.getDisplay(this);
		//		xmpp = new XMPPTestClient();
		form.append(log);
		yup = this;

		//		MetaVector mv = new MetaVector();
		//		mv.addElement("a");
		//		Vector bVector = new Vector ();
		//		bVector.addElement("b");
		//		bVector.addElement("c");
		//		bVector.addElement("d");
		//		mv.addElement(bVector);
		//		mv.addElement("e");
		//		mv.addElement("f");
		//		bVector = new Vector ();
		//		bVector.addElement("g");
		//		bVector.addElement("h");
		//		bVector.addElement("i");
		//		mv.addElement(bVector);
		//		System.out.println(mv.toString());
		//		System.out.println(mv.size());
		//		System.out.println(mv.indexOf("e"));
		//		mv.removeElement("e");
		//		mv.removeElement("h");
		//		System.out.println(mv.toString());
		//		mv.insertElementAt("afterC", mv.indexOf("c")+1);
		//		mv.insertElementAt("afterG", mv.indexOf("g")+1);
		//		System.out.println(mv.toString());
		//		mv.setElementAt("instead-of-I", mv.indexOf("i"));
		//		mv.removeElement("a");
		//		mv.removeElement("i");
		//		System.out.println(mv.toString());
		//		mv.setElementAt("a", 0);
		//		mv.setElementAt("i", mv.size()-1);
		//		System.out.println(mv.toString());
		//		System.out.println("Contains d: "+ mv.contains("d"));
		//		int index = mv.lastIndexOf("d");
		//		System.out.println("Last index of d: "+ index);
		//		mv.removeElementAt(index);
		//		System.out.println(mv.toString());
		//		System.out.println(mv.firstElement());
		//		System.out.println(mv.lastElement());
		//		System.out.println(mv.elementAt(4));
		//		mv.addElement("c");
		//		System.out.println(mv.lastIndexOf("c"));
		//		Object [] tempArray = new Object [mv.size()];
		//		mv.copyInto(tempArray);
		//		for (int i = 0; i < tempArray.length; i++) {
		//			System.out.println(tempArray[i]);	
		//		}
		//		long time0 = System.currentTimeMillis();
		//		long count = 0;
		//		for (int k = 0; k <= 20; k++) {
		//			for (int i = 0; i < 100; i++) {
		//				for (int j = 0; j < 100; j++) {
		//					count += j * i;
		//				}
		//			}
		//			for (int i = 0; i < 100; i++) {
		//				for (int j = 0; j < 100; j++) {
		//					count -= ((j + 1) / (i + 1));
		//				}
		//			}
		//		}
		//		long time1 = System.currentTimeMillis();
		//		String tempString = "aa";
		//		for (int k = 0; k <= 1; k++) {
		//			for (int i = 0; i < 100; i++) {
		//				for (char j = 'a'; j <= 'z'; j++) {
		//					tempString += j;
		//					if (j % 2 == 0) {
		//						tempString = tempString.substring(1, tempString
		//								.length());
		//					}
		//				}
		//			}
		//		}
		//		tempString = tempString.substring(0, 20);
		//		long time2 = System.currentTimeMillis();
		//		for (int k = 0; k <= 1000; k++) {
		//			byte[] data = Utils.getBytesUtf8(tempString);
		//		}
		//		long time2a = System.currentTimeMillis();
		//		for (int j = 0; j <= 20; j++) {
		//			for (int k = 0; k <= 20; k++) {
		//				for (int i = 0; i < 100; i++) {
		//					int[] tempi = new int[2];
		//					byte[] tempb = new byte[10];
		//					String s = new String("abcdefghi");
		//				}
		//			}
		//		}
		long time3 = System.currentTimeMillis();
		Element el = new Element(XmppConstants.MIDP_PLATFORM, "element");
		for (int i = 0; i < 15; i++) {
			Element el2 = new Element(XmppConstants.MIDP_PLATFORM, "elementi" + i);
			el2.setAttribute("attr1", "val1");
			el2.setAttribute("attr2", "val2");
			el.addElement(el2);
			for (int j = 0; j < 25; j++) {
				Element el3 = new Element(XmppConstants.BLUENDO_PUBLISH,
						"elementj" + i + "-" + j);
				el3.setAttribute("attr1", "val1");
				el3.setAttribute("attr2", "val2");
				el3.setAttribute("attr3", "val3");
				el2.addElement(el3);
				String pervString = "" + ((char) (i * j * 10 + i + j));
				el3.addText(pervString);
			}
		}
		long time4 = System.currentTimeMillis();
		byte[] data = BProcessor.toBinary(el);
		long time5 = System.currentTimeMillis();
		Element newEl = BProcessor.parse(data);
		long time6 = System.currentTimeMillis();
		data = el.toXml();
		long time7 = System.currentTimeMillis();
		KXmlParser parser = new KXmlParser();
		try {
			parser.setFeature(KXmlParser.FEATURE_PROCESS_NAMESPACES, true);
			parser.setInput(new InputStreamReader(
					new ByteArrayInputStream(data)));
			newEl = KXmlProcessor.parseDocument(parser);
		} catch (Exception e) {
			// TODO: handle exception
		}
		long time8 = System.currentTimeMillis();
		String longText = "";
		//		longText += ("hasUTF8: " + Utils.has_utf8 + "\n");
		//		longText += ("time0: " + time1 + "\n");
		//		longText += ("time1: " + (time1 - time0) + "\n");
		//		longText += ("time2: " + (time2 - time1) + "\n");
		//		longText += ("time2a: " + (time2a - time2) + "\n");
		//		longText += ("time3: " + (time3 - time2a) + "\n");
		longText += ("time4: " + (time4 - time3) + "\n");
		longText += ("time5: " + (time5 - time4) + "\n");
		longText += ("time6: " + (time6 - time5) + "\n");
		longText += ("time7: " + (time7 - time6) + "\n");
		longText += ("time8: " + (time8 - time7) + "\n");
		log.setText(longText);

	}

	public void startApp() {
		//xmpp.startClient();
		disp.setCurrent(form);
	}

	protected void destroyApp(boolean param) {

	}

	protected void pauseApp() {
	}

	public void exit() {
		destroyApp(false);
		notifyDestroyed();
	}

	//	public void abort(String string, Exception e) {
	//		// XXX: notify the user?
	//	}
}
