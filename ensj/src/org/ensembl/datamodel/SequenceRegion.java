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

package org.ensembl.datamodel;

/**
 * Represents a region of sequence, which could be from any co-ordinate system.
 */

public interface SequenceRegion extends Persistent {

	public CoordinateSystem getCoordinateSystem();

	public void setCoordinateSystem(CoordinateSystem cs);

	public String getName();

	public void setName(String name);

	public long getLength();

	public void setLength(long len);
	
	public void addAttribute(Attribute attrib);
	
	public Attribute[] getAttributes();
	
	public boolean hasAttributes();
	
	/**
	 * Get the value of a particular attribute.
	 * @param code The code of the attribute to get.
	 * @return The value of the attribute, or null if it is not set.
	 */
	public String getAttributeValue(String code);

}
