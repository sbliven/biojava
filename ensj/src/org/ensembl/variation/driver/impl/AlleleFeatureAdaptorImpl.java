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

package org.ensembl.variation.driver.impl;

import java.util.ArrayList;
import java.util.List;

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Transcript;
import org.ensembl.driver.AdaptorException;
import org.ensembl.variation.datamodel.VariationFeature;
import org.ensembl.variation.datamodel.impl.AlleleFeatureImpl;
import org.ensembl.variation.driver.AlleleFeatureAdaptor;
import org.ensembl.variation.driver.VariationDriver;

/**
 * Retrieves allele features from the variation database.
 * 
 * In this implementation Allele features are extracted from VariationFeatures. 
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class AlleleFeatureAdaptorImpl implements AlleleFeatureAdaptor {

  private VariationDriver variationDriver;
  
  public AlleleFeatureAdaptorImpl(VariationDriver variationDriver) {
    this.variationDriver = variationDriver;
  }
  
  
  /**
   * @see org.ensembl.variation.driver.AlleleFeatureAdaptor#fetch(org.ensembl.datamodel.Location)
   */
  public List fetch(Location location) throws AdaptorException {
    
    // Optimise: create the AlleleFeatures directly from the variation_feature table.

    // fetch VariationFeatures hitting location
    List vfs = variationDriver.getVariationFeatureAdaptor().fetch(location);
    if (vfs.size()==0)
      return vfs;
    
    // create AlleleFeatures from the VariationFeatures.
    List afs = new ArrayList();
    for (int i = 0; i < vfs.size(); i++) {
      final VariationFeature vf = (VariationFeature) vfs.get(i);
      final Location loc = vf.getLocation();
      final String alleleStrings = vf.getAlleleString();
      final String[] alleles = alleleStrings.split("/");
      final long variationID = vf.getVariationInternalID();
      for (int j = 0; j < alleles.length; j++) {
        afs.add(new AlleleFeatureImpl(variationDriver,loc, alleles[j], variationID));
      }
    }
    
    return afs;
  }

  /**
   * @see org.ensembl.variation.driver.AlleleFeatureAdaptor#fetch(org.ensembl.datamodel.Transcript, int)
   */
  public List fetch(Transcript transcript, int flank) throws AdaptorException {
    Location l = transcript.getLocation().transform(-flank, flank);
    return fetch(l);
  }


  /**
   * @see org.ensembl.driver.Adaptor#getType()
   */
  public String getType() throws AdaptorException {
    return TYPE;
  }


  /**
   * @see org.ensembl.driver.Adaptor#closeAllConnections()
   */
  public void closeAllConnections() throws AdaptorException {
    variationDriver.getVariationFeatureAdaptor().closeAllConnections();
  }


  /**
   * @see org.ensembl.driver.Adaptor#clearCache()
   */
  public void clearCache() throws AdaptorException {
    variationDriver.getVariationFeatureAdaptor().clearCache();
  }

}
