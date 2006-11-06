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
import org.ensembl.datamodel.OligoFeature;
import org.ensembl.datamodel.OligoProbe;
import org.ensembl.datamodel.Location;

/**
 * Adaptor for retrieving OligoFeatures from a database.
 *
 * @see OligoFeature
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public interface OligoFeatureAdaptor extends FeatureAdaptor {

	final String TYPE = "oligo_feature";

	/**
	 * Fetch OligoFeature with specified internalID.
	 * @param internalID internalID of the OligoFeature.
	 * @return OligoFeature with specified internalID or null if none found.
	 */
	OligoFeature fetch(long internalID) throws AdaptorException;

	/**
	 * Fetches OligoFeatures corresponding the the oligoProbe.
	 * @param oligoProbe OligoProbe of interest.
	 * @return zero or more OligoFeatures representing where the oligoProbe hits 
	 * the genome.
	 * @throws AdaptorException
	 * @see org.ensembl.datamodel.OligoFeature
	 */
	List fetch(OligoProbe oligoProbe) throws AdaptorException;


}
