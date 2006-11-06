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

package org.ensembl.datamodel;

/**
 * A generic attribute that can be associatted with a features such as
 * MiscFeature.
 * @see org.ensembl.datamodel.MiscFeature
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface Attribute {

  /**
   * Human readable name for this attribute.
   * @return Human readable name for this attribute.
   */
  String getName();

  /**
   * Human readable name for this attribute
   * @param name Human readable name for this attribute.
   */
  void setName(String name);
  
  /**
   * Description for this attribute.
   * @return Description for this attribute.
   */
  String getDescription();

  /**
   * Description for this attribute.
   * @param description Description for this attribute.
   */
  void setDescription(String description);
  
  /**
   * Code for this attribute
   * @return Code for this attribute.
   */
  String getCode();

  /**
   * Code for this attribute.
   * @param code Code for this attribute.
   */
  void setCode(String code);
  
  /**
   * Value of this attribute
   * @return Value of this attribute.
   */
  String getValue();

  /**
   * Value of this attribute.
   * @param value Value of this attribute.
   */
  void setValue(String value);
}
