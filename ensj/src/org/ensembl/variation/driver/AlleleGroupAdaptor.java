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
import org.ensembl.variation.datamodel.AlleleGroup;
import org.ensembl.variation.datamodel.VariationGroup;

/**
 * Retrieves Allele groups from an ensembl database.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface AlleleGroupAdaptor extends Adaptor {

  final static String TYPE = "allele_group";

  /**
   * Fetch Allele group with specified internal ID.
   * @return specified allele group or null if none found.
   */
  AlleleGroup fetch(long internalID) throws AdaptorException;

  /**
   * Fetch Allele group with specified name.
   * @return specified allele group or null if none found.
   */
  AlleleGroup fetch(String name) throws AdaptorException;

  /**
   * Fetch all allele groups that are part of the Variation group.
   * @return zero or more AlleleGroups.
   */
  List fetch(VariationGroup variationGroup) throws AdaptorException;
}
