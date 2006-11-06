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

import org.ensembl.datamodel.OligoArray;

/**
 * Adaptor for retrieving microarrays.
 *
 * @see org.ensembl.datamodel.OligoArray
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public interface OligoArrayAdaptor extends Adaptor {

	final String TYPE = "oligo_array";
	
	/**
	 * Retrieve OligoArray with specified internal ID.
	 * @param internalID internal ID of OligoArray in database.
	 * @return OligoArray with specified internal ID, or null if
	 * none found.
	 * @throws AdaptorException
	 */
	OligoArray fetch(long internalID) throws AdaptorException;

	/**
	 * Retrieve OligoArray with specified name.
	 * @param name name of an OligoArray in database.
	 * @return OligoArray with specified name, or null if
	 * none found.
	 * @throws AdaptorException
	 */
	OligoArray fetch(String name) throws AdaptorException;
	
	/**
	 * Fetch all microarrays from the database.
	 * @return zero or more microarrays.
	 * @see OligoArray
	 */
	List fetch() throws AdaptorException;
	
	
}
