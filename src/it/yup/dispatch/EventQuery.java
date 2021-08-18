/* Copyright (c) 2008-2009-2010 Bluendo S.r.L.
 * See about.html for details about license.
 *
 * $Id: EventQuery.java 2218 2010-10-04 09:37:39Z luca $
*/

/**
 * 
 */
package it.yup.dispatch;

/**
 * @author pinturicchio
 *
 */
public class EventQuery 
{
    
    public String event;
    public String tagAttrNames[];
    public String tagAttrValues[];
    public EventQuery child = null;
    
    public static final String ANY_PACKET = "*";
    public static final String ANY_EVENT = "_*";
    
    /**
     * 
     * @param name name of the packet or of the XmlStream event we want to receive
     * @param attrNames
     * @param attrValues
     */
    public EventQuery(String name, String attrNames[], String attrValues[])
    {
        this.event = name;
        this.tagAttrNames = attrNames;
        this.tagAttrValues = attrValues;
    }

}
