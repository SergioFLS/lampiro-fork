// #condition MIDP
/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: MetaVector.java 1858 2009-10-16 22:42:29Z luca $
*/
package it.yup.ui;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

public class MetaVector extends Vector {

	private class MultiVectorEnumeration implements Enumeration {
		private Object next = null;
		private Object innerNext = null;
		private Enumeration superEnumeration = MetaVector.this
				.superEnumeration();
		private Enumeration innerEnumeration = null;

		public MultiVectorEnumeration() {
			checkNext();
		}

		private void checkNext() {
			if (superEnumeration.hasMoreElements()) {
				next = superEnumeration.nextElement();
				if (next != null && next instanceof Vector
						&& (((Vector) next).size() == 0)) {
					checkNext();
				}
			} else {
				next = null;
			}
		}

		public boolean hasMoreElements() {
			return (next != null);

			//			return (next != null && (next instanceof Vector ? (((Vector) next)
			//					.size() > 0) : true));

		}

		public Object nextElement() {
			if (next == null) throw new NoSuchElementException();
			if (next instanceof Vector == false) {
				Object retVal = next;
				checkNext();
				return retVal;
			} else {
				if (innerEnumeration == null) innerEnumeration = ((Vector) next)
						.elements();
				if (innerEnumeration.hasMoreElements()) {
					innerNext = innerEnumeration.nextElement();
					if (innerEnumeration.hasMoreElements() == false) {
						innerEnumeration = null;
						checkNext();
					}
					return innerNext;
				} else
					innerEnumeration = null;
				checkNext();
				return nextElement();
			}
		}
	}

	/*
	 * The inner vector that "mimics" a vector behaviour 
	 */
	private Vector innerVector = new Vector();

	private static final int REMOVE = 0;
	private static final int INSERT = 1;
	private static final int SET = 2;

	private Enumeration superEnumeration() {
		return innerVector.elements();
	}

	public MetaVector() {
		innerVector = new Vector();
	}

	public MetaVector(int initialCapacity) {
		innerVector = new Vector(initialCapacity);
	}

	public MetaVector(int initialCapacity, int capacityIncrement) {
		innerVector = new Vector(initialCapacity, capacityIncrement);
	}

	public void addElement(Object obj) {
		innerVector.addElement(obj);
	}

	public boolean contains(Object elem) {
		Enumeration en = this.elements();
		while (en.hasMoreElements()) {
			if (en.nextElement().equals(elem)) return true;
		}
		return false;
	}

	public void copyInto(Object[] anArray) {
		Enumeration en = this.elements();
		int index = 0;
		while (en.hasMoreElements()) {
			anArray[index] = en.nextElement();
			index++;
		}
	}

	public Object elementAt(int index) {
		//		int i = 0;
		//		Enumeration en = this.elements();
		//		while (i < index) {
		//			en.nextElement();
		//			i++;
		//		}
		//		return en.nextElement();

		int i = index;
		Enumeration en = this.innerVector.elements();
		while (en.hasMoreElements()) {
			Object object = (Object) en.nextElement();
			if (object instanceof Vector == false) {
				if (i == 0) return object;
				i--;
			} else {
				Vector oVector = ((Vector) object);
				if (oVector.size() > i) return oVector.elementAt(i);
				i -= oVector.size();
			}
		}
		throw (new ArrayIndexOutOfBoundsException());
	}

	public Enumeration elements() {
		return new MultiVectorEnumeration();
	}

	public Object firstElement() {
		return elementAt(0);
	}

	public int indexOf(Object elem) {
		return indexOf(elem, 0);
	}

	public int indexOf(Object elem, int index) {
		int i = 0;
		Enumeration en = this.elements();
		while (i < index) {
			en.nextElement();
			i++;
		}
		while (en.hasMoreElements()) {
			Object o = en.nextElement();
			if (o.equals(elem)) return i;
			i++;
		}
		return -1;
	}

	public void insertElementAt(Object obj, int index) {
		boolean inserted = this.operateElementAt(obj, index, INSERT);
		if (index == this.size()) {
			this.innerVector.addElement(obj);
			return;
		}
		if (inserted == false) throw new ArrayIndexOutOfBoundsException();
	}

	public boolean isEmpty() {
		return !(this.elements().hasMoreElements());
	}

	public Object lastElement() {
		Object o = null;
		Enumeration en = this.elements();
		o = en.nextElement();
		while (en.hasMoreElements()) {
			o = en.nextElement();
		}
		return o;
	}

	public int lastIndexOf(Object elem) {
		return lastIndexOf(elem, 0);
	}

	public int lastIndexOf(Object elem, int startIndex) {
		Object o = null;
		Enumeration en = this.elements();
		int lastIndex = -1;
		int index = 0;
		try {
			while (index < startIndex) {
				en.nextElement();
				index++;
			}
		} catch (NoSuchElementException e) {
			throw new IndexOutOfBoundsException();
		}
		while (en.hasMoreElements()) {
			o = en.nextElement();
			if (o == elem) lastIndex = index;
			index++;
		}
		return lastIndex;
	}

	public boolean removeElement(Object obj) {
		boolean retVal = innerVector.removeElement(obj);
		if (retVal) return true;
		Enumeration en = superEnumeration();
		while (en.hasMoreElements()) {
			Object o = en.nextElement();
			if (o instanceof Vector) {
				retVal = ((Vector) o).removeElement(obj);
				if (retVal) return true;
			}
		}
		return false;
	}

	public void removeAllElements() {
		innerVector.removeAllElements();
	}

	public void removeElementAt(int index) {
		boolean removed = operateElementAt(null, index, REMOVE);
		if (removed == false) throw new ArrayIndexOutOfBoundsException();
	}

	public void setElementAt(Object obj, int index) {
		boolean inserted = operateElementAt(obj, index, MetaVector.SET);
		if (inserted == false) throw new ArrayIndexOutOfBoundsException();
	}

	/**
	 * @param obj
	 * @param index
	 */
	private boolean operateElementAt(Object obj, int index, int operation) {
		int count = 0;
		Enumeration en = superEnumeration();
		while (en.hasMoreElements()) {
			Object o = en.nextElement();
			if (o instanceof Vector == false) {
				if (count == index) {
					switch (operation) {
						case REMOVE:
							innerVector.removeElement(o);
							break;
						case INSERT:
							innerVector.insertElementAt(obj, innerVector
									.indexOf(o));
							break;
						case SET:
							innerVector.setElementAt(obj, innerVector
									.indexOf(o));
					}
					return true;
				}
				count++;
			} else {
				Vector vectorObject = (Vector) o;
				int vectorObjectSize = vectorObject.size();
				if (vectorObjectSize + count > index) {
					switch (operation) {
						case REMOVE:
							vectorObject.removeElementAt(index - count);
							break;
						case INSERT:
							vectorObject.insertElementAt(obj, index - count);
							break;
						case SET:
							vectorObject.setElementAt(obj, index - count);
					}
					return true;
				}
				count += vectorObjectSize;
			}
		}
		return false;
	}

	public int size() {
		int count = innerVector.size();
		Enumeration en = superEnumeration();
		while (en.hasMoreElements()) {
			Object o = en.nextElement();
			if (o instanceof Vector) {
				count--;
				count += ((Vector) o).size();
			}
		}
		return count;
	}

	public String toString() {
		String retString = "Multivector: ";
		Enumeration en = this.elements();
		while (en.hasMoreElements()) {
			retString += (" " + en.nextElement());
		}
		return retString;
	}
}
