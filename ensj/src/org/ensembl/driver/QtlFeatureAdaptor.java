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

package org.ensembl.driver;

import java.util.List;

import org.ensembl.datamodel.Qtl;

/**
 * Retrieves QtlFeatures from the database. Works in conjunction with the
 * QtlAdaptor. 
 *
 * @see org.ensembl.datamodel.QtlFeature
 * @see org.ensembl.driver.QtlAdaptor
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface QtlFeatureAdaptor extends FeatureAdaptor {


  /**
   * Adaptor type. TYPE = "qtl_feature".
   */
  final static String TYPE = "qtl_feature";


  /**
   * Fetch the QtlFeatures related to the qtl.
   * @see org.ensembl.datamodel.QtlFeature
   * @return list of zero or more QtlFeatures.
   */
  List fetch(Qtl qtl) throws AdaptorException;

}
