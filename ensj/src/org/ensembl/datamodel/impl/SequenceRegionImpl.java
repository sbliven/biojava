/*
 * Copyright (C) 2002 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.datamodel.impl;

import org.ensembl.datamodel.Attribute;
import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.SequenceRegion;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;

/**
 * Implementation of SequenceRegion interface.
 */
public class SequenceRegionImpl extends PersistentImpl implements SequenceRegion, Cloneable {

  /**
   * Used by the (de)serialization system to determine if the data 
   * in a serialized instance is compatible with this class.
   *
   * It's presence allows for compatible serialized objects to be loaded when
   * the class is compatible with the serialized instance, even if:
   *
   * <ul>
   * <li> the compiler used to compile the "serializing" version of the class
   * differs from the one used to compile the "deserialising" version of the
   * class.</li>
   *
   * <li> the methods of the class changes but the attributes remain the same.</li>
   * </ul>
   *
   * Maintainers must change this value if and only if the new version of
   * this class is not compatible with old versions. e.g. attributes
   * change. See Sun docs for <a
   * href="http://java.sun.com/j2se/1.4.2/docs/guide/serialization/">
   * details. </a>
   *
   */
  private static final long serialVersionUID = 1L;



  public SequenceRegionImpl(CoreDriver driver) {
    this.driver = driver;
  }

  private CoreDriver driver;
	private CoordinateSystem cs;
	private String name;
	private long length;
	private Attribute[] attribs = null;

	public CoordinateSystem getCoordinateSystem() {
		return cs;
	}

	public void setCoordinateSystem(CoordinateSystem cs) {
		this.cs = cs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long len) {
		this.length = len;
	}

	public Attribute[] getAttributes() {
    if ( attribs==null ) {
      if (driver!=null) lazyLoadAttributes();
      else attribs = new Attribute[0];
    }
		return attribs;
	}

	/**
   * 
   */
  private void lazyLoadAttributes() {
    try {
      driver.getSequenceRegionAdaptor().fetchComplete(this);
    } catch (AdaptorException e) {
      throw new RuntimeException("Failed to get attributes for :"+this, e);
    }
    
  }

  public String getAttributeValue(String code) {

		String result = null;
		for (int i = 0; i < attribs.length; i++) {
			if (attribs[i].getCode().equalsIgnoreCase(code)) {
				return attribs[i].getValue();
			}
		}

		return result;

	}

	public boolean hasAttributes() {
		
		return (attribs.length > 0);
		
	}
	
	public void addAttribute(Attribute attrib) {

    if ( attribs==null ) attribs = new Attribute[0];
		Attribute[] dummy = new Attribute[attribs.length + 1];

		System.arraycopy(attribs, 0, dummy, 0, attribs.length);

		dummy[attribs.length] = attrib;

		attribs = dummy;

	}

	/**
	   * Return a string representation of this object
	   */
	public String toString() {

		StringBuffer buf = new StringBuffer();
		buf.append(this.getClass() + " [");
		buf.append("name = " + getName());
		buf.append(" internalID = " + getInternalID());
		buf.append(" coordinate system = " + getCoordinateSystem().toString());
		buf.append(" length = " + getLength());
		buf.append(" attributes: ");
    Attribute[] tmp = getAttributes();
		for (int i = 0; tmp!=null && i < tmp.length; i++) {
			buf.append("<" + tmp[i].getCode() + ":" + tmp[i].getValue() + ">");
		}
		buf.append("]");

		return buf.toString();

	}

}