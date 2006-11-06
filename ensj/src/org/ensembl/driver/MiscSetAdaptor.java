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

import org.ensembl.datamodel.MiscSet;

/**
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface MiscSetAdaptor extends Adaptor {

  final static String TYPE = "misc_set";

  /**
   * @return all MiscSets
   * @throws AdaptorException if problem occurs retrieving data from databases.
   */
  List fetch() throws AdaptorException;
  
  /**
   * 
   * @param internalID internalID of MiscSet
   * @return MiscSet with specified internalID, or null if none found.
   * @throws AdaptorException if problem occurs retrieving data from databases.
   */
  MiscSet fetch(long internalID) throws AdaptorException;
  
  /**
   * 
   * @param code MiscSet's code
   * @return MiscSets matching with the specified code or null if non found.
   * @throws AdaptorException if problem occurs retrieving data from databases.
   */
  MiscSet fetch(String code) throws AdaptorException;

  /**
   * Fetches all MiscSets with a code matching the regular expression.
   * @param regexp pattern to match against misc_set.code.
   * @return zero or more MiscSets
   */
  List fetchByCodePattern(String regexp)  throws AdaptorException;
  
  
}
