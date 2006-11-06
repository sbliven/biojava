/*
	Copyright (C) 2003 EBI, GRL

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

package org.ensembl.datamodel.impl;

import org.ensembl.datamodel.MiscSet;

/**
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class MiscSetImpl extends PersistentImpl implements MiscSet {

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



  /**
   * @param internalID
   * @param code
   * @param name
   * @param description
   * @param maxFeatureLength
   */
  public MiscSetImpl(
    long internalID,
    String code,
    String name,
    String description,
    int maxFeatureLength) {

    this.internalID = internalID;
    this.code = code;
    this.name = name;
    this.description = description;
    this.maxFeatureLength = maxFeatureLength;
  }

  private String code;
  private String name;
  private String description;
  private int maxFeatureLength;


  public MiscSetImpl() {
    super();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getMaxFeatureLength() {
    return maxFeatureLength;
  }

  public void setMaxFeatureLength(int maxFeatureLength) {
    this.maxFeatureLength = maxFeatureLength;

  }

  public String getCode() {
    return code;
  }

  public void setCode(String string) {
    code = string;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();

    buf.append("[");
    buf.append("name=").append(name);
    buf.append(", code=").append(code);
    buf.append(", description=").append(description);
    buf.append(", maxFeatureLength=").append(maxFeatureLength);
    buf.append("]");

    return buf.toString();
  }

}
