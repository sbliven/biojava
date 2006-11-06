/*
    Copyright (C) 2002 EBI, GRL

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.ensembl.datamodel;

import java.util.Date;

/**
* Object is capable of having an accession (stable id) which is stable across
* Ensembl data releases and has a version, creation and last modified date.
* */
public interface Accessioned extends Persistent {
  
  /**
   * Return the accession (stable id) for this object.
   * @return accession (stable id) for this object.
   */
	String getAccessionID();
	
	/**
   * Set the accession (stable id) for this object.
   * @param accessionID accession (stable id) for this object.
   */
	void setAccessionID(String accessionID);

	/**
	 * Set the version of this object.
	 * Different versions of the object have the same accessionID and 
	 * different versions.
	 * @param version version of this object.
	 * @see #getAccessionID() 
	 */
	void setVersion(int version);
	/**
	 * Return the version of this object.
	 * Different versions of the object have the same accessionID and 
	 * different versions.
	 * @return version of this object.
	 * @see #getAccessionID()
	 */
	int getVersion();

	/**
	 * Return the date this object was created.
	 * 
	 * Note: currently this date might be incorrect due to production reasons.
	 *  
	 * @return date the accession was created. 
	 */
	public Date getCreatedDate();
	
	/**
	 * Set the date the object was created . 
	 * @param createdDate the date the object was created. 
	 */
	public void setCreatedDate(Date createdDate);

	/**
	 * Return the date the object was last modified. 
	 * @return date the object was last modified. 
	 */
	public Date getModifiedDate();

	/**
	 * Set the date the object was last modified. 
	 * @param modifiedDate the date the object was last modified. 
	 */
	public void setModifiedDate(Date modifiedDate);

}
