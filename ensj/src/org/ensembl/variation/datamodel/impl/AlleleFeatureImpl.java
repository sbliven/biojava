/*
	Copyright (C) 2005 EBI, GRL

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

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.impl.BaseFeatureImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.util.StringUtil;
import org.ensembl.variation.datamodel.AlleleFeature;
import org.ensembl.variation.datamodel.Variation;
import org.ensembl.variation.driver.VariationDriver;

/**
 * The point of this class is....
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class AlleleFeatureImpl extends BaseFeatureImpl implements AlleleFeature{

  private static final long serialVersionUID = 1L;
	private final VariationDriver variationDriver;
  private final String alleleString;
  private final long variationInternalID;
  private Variation variation;

  public AlleleFeatureImpl(VariationDriver variationDriver, Location location, String alleleString, long variationInternalID) {
    this.variationDriver = variationDriver;
    this.location = location;
    this.alleleString = alleleString;
    this.variationInternalID = variationInternalID;
  }
  
  /**
   * @see org.ensembl.variation.datamodel.AlleleFeature#getAlleleString()
   */
  public String getAlleleString() {
    return alleleString;
  }

  /**
   * @see org.ensembl.variation.datamodel.AlleleFeature#getVariationInternalID()
   */
  public long getVariationInternalID() {
    return variationInternalID;
  }

  /**
   * @throws AdaptorException
   * @see org.ensembl.variation.datamodel.AlleleFeature#getVariation()
   */
  public Variation getVariation() throws AdaptorException {
    if (variation==null && variationDriver!=null) 
      variation = variationDriver.getVariationAdaptor().fetch(variationInternalID);
    return variation;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();

    buf.append("[");
    buf.append(super.toString());
    buf.append(", alleleString=").append(alleleString);
    buf.append(", variationInternalID=").append(variationInternalID);
    buf.append(", variation=").append(StringUtil.setOrUnset(variation));
    buf.append("]");

    return buf.toString();
  }
}
