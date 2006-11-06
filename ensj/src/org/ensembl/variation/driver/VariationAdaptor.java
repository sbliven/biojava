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

import org.ensembl.driver.Adaptor;
import org.ensembl.driver.AdaptorException;
import org.ensembl.variation.datamodel.Variation;
import org.ensembl.variation.datamodel.impl.VariationImpl;

/**
 * The point of this class is ...
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface VariationAdaptor extends Adaptor {

  final static String TYPE = "variation";

  /**
   * Fetch variation by internal id.
   * @param internalID internal ID of the variation
   * @return variation with specified internal id or null if none found.
   */
  Variation fetch(long internalID) throws AdaptorException;
  
  /**
   * Fetch variations by their internal ids.
   * @return zero or more Variations with the specified internalIDs.
   */
  List fetch(long[] internalIDs) throws AdaptorException;

  /**
   * Fetch variation by name.
   * @param name name of the variation
   * @return variation with specified name or null if none found.
   */
  Variation fetch(String name) throws AdaptorException;

  /**
   * Loads flanking sequence for variation.
   * @param variation variation to load flanking sequence for.
   */
  void fetchFlankingSequence(Variation variation)  throws AdaptorException;
}
