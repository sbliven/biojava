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

import java.util.List;

/**
 * An microarray chip which contains probes.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 * @see OligoProbe
 * @see OligoFeature
 */
public interface OligoArray extends Persistent {

	/**
	 * Name of this micro array.
	 * @return Name of this micro array.
	 */
	String getName();
	
	/**
	 * Standard number of probes in a probeset on this array.
	 * @return standard number of probes in a probeset on this array.
	 */
	int getProbeSetSize();
	
	
	/**
	 * All probes in the array.
	 * @return zero or more probes in this array.
	 * @see OligoProbe
	 */
	List getOligoProbes();

	/**
	 * External database corresponding to this microarray. 
	 * @return external database corresponding to this microarray or null if
	 * none exists.
	 */
	ExternalDatabase getExternalDatabase();
  
  /**
   * Type of array.
   * 
   * e.g. AFFY, OLIGO
   * @return type of array.
   */
  String getType();
}
