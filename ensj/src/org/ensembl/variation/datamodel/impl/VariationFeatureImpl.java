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

import java.util.logging.Logger;

import org.ensembl.datamodel.impl.BaseFeatureImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.RuntimeAdaptorException;
import org.ensembl.util.StringUtil;
import org.ensembl.variation.datamodel.Variation;
import org.ensembl.variation.datamodel.VariationFeature;
import org.ensembl.variation.driver.VariationDriver;

/**
 * The point of this class is ...
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class VariationFeatureImpl
  extends BaseFeatureImpl
  implements VariationFeature {

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



  private Logger logger =
    Logger.getLogger(VariationFeatureImpl.class.getName());

  private VariationDriver vdriver;

  private final static Variation FAILED_LAZY_LOAD_VARIATION =
    new VariationImpl();

  private long variationInternalID;

  private Variation variation;

  private int mapWeight;

  private String variationName;

  private String alleleString;

  public VariationFeatureImpl() {
  }
  public VariationFeatureImpl(long internalID) {
    super(internalID);
  }
  
  public VariationFeatureImpl(VariationDriver vdriver) {
    super(vdriver.getCoreDriver());
    this.vdriver = vdriver;
  }

  /**
   * @see org.ensembl.variation.datamodel.VariationFeature#getAlleleString()
   */
  public String getAlleleString() {
    return alleleString;
  }

  /**
   * @see org.ensembl.variation.datamodel.VariationFeature#getVariationName()
   */
  public String getVariationName() {
    return variationName;
  }

  /**
   * @see org.ensembl.variation.datamodel.VariationFeature#getMapWeight()
   */
  public int getMapWeight() {
    return mapWeight;
  }

  /**
   * @see org.ensembl.variation.datamodel.VariationFeature#getVariation()
   * @throws RuntimeAdaptorException if a problem occurs lazy loading variation.
   */
  public Variation getVariation() {
    if (variation == null && vdriver != null) {
      try {
        variation = vdriver.getVariationAdaptor().fetch(variationInternalID);
      } catch (AdaptorException e) {
        throw new RuntimeAdaptorException(
          "Failed to lazy load variation with internalID = "
            + variationInternalID,e);
      }

      if (variation == null) {
        logger.warning(
          "Failed to lazy load variation with internal ID = "
            + variationInternalID);
        variation = FAILED_LAZY_LOAD_VARIATION;
      }
    }
    if (variation == FAILED_LAZY_LOAD_VARIATION)
      return null;
    else
      return variation;
  }

  /**
   * @see org.ensembl.variation.datamodel.VariationFeature#getVariationInternalID()
   */
  public long getVariationInternalID() {
    return variationInternalID;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();

    buf.append("[");
    buf.append("{").append(super.toString()).append("}, ");
    buf.append("variationInternalID=").append(variationInternalID);
    buf.append(" ,variation=").append(StringUtil.setOrUnset(variation));
    buf.append(" ,variationName=").append(variationName);
    buf.append(" ,mapWeight=").append(mapWeight);
    buf.append(" ,alleleString=").append(alleleString);
    buf.append("]");

    return buf.toString();
  }
  /**
   * @param string
   */
  public void setAlleleString(String string) {
    alleleString = string;
  }

  /**
   * @param i
   */
  public void setMapWeight(int i) {
    mapWeight = i;
  }

  /**
   * @param variation
   */
  public void setVariation(Variation variation) {
    this.variation = variation;
  }

  /**
   * @param l
   */
  public void setVariationInternalID(long l) {
    variationInternalID = l;
  }

  /**
   * @param string
   */
  public void setVariationName(String string) {
    variationName = string;
  }

}
