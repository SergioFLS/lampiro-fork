// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: UIContainer.java 1858 2009-10-16 22:42:29Z luca $
*/
package it.yup.ui;

import java.util.Vector;

public interface UIIContainer {

	public void setSelectedItem(UIItem item);
	
	public boolean contains (UIItem item);
	
	public Vector getItems ();
	
}
