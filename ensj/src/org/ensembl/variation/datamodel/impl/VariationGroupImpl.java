/*
    Copyright (C) 2001 EBI, GRL

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
package org.ensembl.variation.datamodel.impl;

import java.util.ArrayList;
import java.util.List;

import org.ensembl.datamodel.impl.PersistentImpl;
import org.ensembl.variation.datamodel.Variation;
import org.ensembl.variation.datamodel.VariationGroup;
import org.ensembl.variation.driver.VariationDriver;

/**
 * Implementation of VariationGroup.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class VariationGroupImpl extends PersistentImpl implements VariationGroup{

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



  private List variations = new ArrayList();

  private String type;

  private String source;

  private String name;

  private VariationDriver vdriver;

  public VariationGroupImpl(VariationDriver vdriver) {
    this.vdriver = vdriver;
  }

  /**
   * @see org.ensembl.variation.datamodel.VariationGroup#getName()
   */
  public String getName() {
    return name;
  }

  /* (non-Javadoc)
   * @see org.ensembl.variation.datamodel.VariationGroup#setName(java.lang.String)
   */
  public void setName(String name) {
    this.name = name;

  }

  /**
   * @see org.ensembl.variation.datamodel.VariationGroup#getSource()
   */
  public String getSource() {
    return source;
  }

  /**
   * @see org.ensembl.variation.datamodel.VariationGroup#setSource(java.lang.String)
   */
  public void setSource(String source) {
    this.source=source;
  }

  /**
   * @see org.ensembl.variation.datamodel.VariationGroup#getType()
   */
  public String getType() {
    return type;
  }

  /**
   * @see org.ensembl.variation.datamodel.VariationGroup#setType(java.lang.String)
   */
  public void setType(String type) {
    this.type=type;
  }

  /**
   * @see org.ensembl.variation.datamodel.VariationGroup#getVariations()
   */
  public List getVariations() {
    return variations;
  }

  /**
   * @see org.ensembl.variation.datamodel.VariationGroup#addVariation(org.ensembl.variation.datamodel.Variation)
   */
  public void addVariation(Variation variation) {
    variations.add(variation);
  }


}
