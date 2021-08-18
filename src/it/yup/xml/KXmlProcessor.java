/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: KXmlProcessor.java 1858 2009-10-16 22:42:29Z luca $
 */
package it.yup.xml;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class KXmlProcessor {

	public static Element parseDocument(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		//BElement el = new BElement();
//		parser.require(XmlPullParser.START_DOCUMENT, null, null);
		int tt = 0;
		while (true) {
			tt = parser.nextToken();
			if (tt == XmlPullParser.START_DOCUMENT
					|| tt == XmlPullParser.START_TAG)
				break;
		}
		return _parse(parser);
	}

	public static Element pullElement(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		return _parse(parser);
	}

	public static Element pullDocumentStart(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		Element el = new Element(parser.getNamespace(), parser.getName());
		el.attributes = new String[parser.getAttributeCount()][2];
		el.nattributes = el.attributes.length;
		for (int i = 0; i < parser.getAttributeCount(); i++) {
			el.attributes[i][0] = parser.getAttributeName(i);
			el.attributes[i][1] = parser.getAttributeValue(i);
		}
		return el;
	}

	private static Element _parse(XmlPullParser parser)
			throws XmlPullParserException, IOException {

		Element el = new Element(parser.getNamespace(), parser.getName());
		el.attributes = new String[parser.getAttributeCount()][3];
		el.nattributes = el.attributes.length;
		for (int i = 0; i < parser.getAttributeCount(); i++) {
			el.attributes[i][0] = parser.getAttributeNamespace(i);
			el.attributes[i][1] = parser.getAttributeName(i);
			el.attributes[i][2] = parser.getAttributeValue(i);
		}

		boolean finished = false;

		do {
			int type = parser.nextToken();
			switch (type) {
			case XmlPullParser.START_TAG:
				el.addElement(_parse(parser));
				break;
			case XmlPullParser.END_TAG:
				finished = true;
				break;
			case XmlPullParser.COMMENT:
				break;
			default:
				if (parser.getText() != null) {
					el.addText(parser.getText());
				}
			}

		} while (!finished);

		return el;
	}

}
