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
package org.ensembl.variation.driver;

import java.util.List;

import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.FeatureAdaptor;
import org.ensembl.variation.datamodel.Variation;
import org.ensembl.variation.datamodel.VariationFeature;

/**
 * Adaptor for retrieving VariationFeatures.
 * 
 * @see org.ensembl.variation.datamodel.VariationFeature
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface VariationFeatureAdaptor extends FeatureAdaptor {

  /** 
   * Name of the default variation feature adaptor available from a driver. 
   */
  final static String TYPE = "variation_feature";

  /**
   * Fetches the VariationFeature with the specified internal ID.
   * @param internalID internal ID of the VariationFeature.
   * @return the VariationFeature with the specified internal ID or null
   * if none found.
   * @throws AdaptorException
   */
  VariationFeature fetch(long internalID) throws AdaptorException;

  /**
   * Fetches the VariationFeatures that show where the Variation
   * appears on the genome.
   * @param variation variation to search for.
   * @return zero or more VariationFeature's showing where the
   * variation hits the genome.
   * @throws AdaptorException
   */
  List fetch(Variation variation) throws AdaptorException;
}
