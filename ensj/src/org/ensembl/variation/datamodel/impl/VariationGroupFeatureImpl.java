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

package org.ensembl.variation.datamodel.impl;

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.impl.BaseFeatureImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.RuntimeAdaptorException;
import org.ensembl.variation.datamodel.VariationGroup;
import org.ensembl.variation.datamodel.VariationGroupFeature;
import org.ensembl.variation.driver.VariationDriver;

/**
 * The point of this class is....
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class VariationGroupFeatureImpl extends BaseFeatureImpl implements
		VariationGroupFeature {

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



	private VariationDriver vdriver;
	private VariationGroup variationGroup;
	private long variationGroupID;
	private String variationGroupName;


	/**
	 * 
	 * @param vdriver
	 * @param location
	 * @param variationGroupID
	 * @param variationGroupName
	 */public VariationGroupFeatureImpl(VariationDriver vdriver, Location location, long variationGroupID, String variationGroupName) {
		this.vdriver = vdriver;
		this.location = location;
		this.variationGroupID = variationGroupID;
		this.variationGroupName = variationGroupName;
	}

	
	
	/**
	 * Lazy loads variation group on demand.
	 * @see org.ensembl.variation.datamodel.VariationGroupFeature#getVariationGroup()
	 */
	public VariationGroup getVariationGroup() {
		if (variationGroup==null && variationGroupID>0 && vdriver!=null)
			try {
				variationGroup = vdriver.getVariationGroupAdaptor().fetch(variationGroupID);
			} catch (AdaptorException e) {
				throw new RuntimeAdaptorException("Failed to lazy load variationGroup: " + variationGroupID,e);
			}
		return variationGroup;
	}

	/**
	 * @see org.ensembl.variation.datamodel.VariationGroupFeature#getVariationGroupName()
	 */
	public String getVariationGroupName() {
	  // attempt lazy load if the value not already set (this value is not set
	  // in the variation_group_feature table so lazy loading is necessary).
	  if (variationGroupName==null && getVariationGroup()!=null)
	    variationGroupName = getVariationGroup().getName();
		return variationGroupName;
	}

}
