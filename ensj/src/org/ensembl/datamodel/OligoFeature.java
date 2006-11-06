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

package org.ensembl.datamodel;

/**
 * The location where an OligoProbe hits the genome.
 *
 * @see OligoArray
 * @see OligoProbe
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public interface OligoFeature extends Feature {

	/**
	 * The probe that hits the genome.
	 * @return The probe that hits the genome.
	 */
	OligoProbe getProbe();
	
	/**
	 * The number of bases by which the probe differs from the 
	 * genomic sequence.
	 * @return The number of bases by which the probe differs from the 
	 * genomic sequence.
	 */
	int getMisMatchSize();
	
	/**
	 * The name of the probe's probeset. 
	 * 
	 * Funcionally equivalent to getProbe().getProbeSetName().
	 * @return name of the probe's probeset.
	 */
	public String getProbeSetName();

	/**
	 * Database internal ID for the the probe.
	 * 
	 * Same as getProbe().getInternalID()
	 * @return database internal ID for te the probe.
	 * @see #getProbe()
	 */
	long getProbeInternalID();

}
