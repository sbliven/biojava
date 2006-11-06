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
package org.ensembl.compara.driver;

import java.util.List;

import org.ensembl.datamodel.Location;
import org.ensembl.driver.Adaptor;
import org.ensembl.driver.AdaptorException;

public interface DnaDnaAlignFeatureAdaptor extends Adaptor{
  /**
   * Returns DnaDnaAlignFeature instances for input species, chromosome location
   * and hit species for ALL method-link-types.
  **/
  List fetch(String species, Location location, String hitSpecies) throws AdaptorException;

  /**
   * Returns DnaDnaAlignFeature instances for input species, chromosome location
   * and hit species for the specified method-link-type.
  **/
  List fetch(String species, Location location, String hitSpecies, String methodLinkType) throws AdaptorException;
  
  static final String TYPE = "dna_dna_align_feature";

}//end DnaDnaAlignFeatureAdaptor
