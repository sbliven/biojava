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

package org.ensembl.probemapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ensembl.datamodel.OligoFeature;
import org.ensembl.datamodel.Location;

/**
 * Data structure containing an OligoFeature and other useful information
 * usefull for mapping purposes.
 * 
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class MappableOligoFeature implements Comparable, Serializable {

  private static final long serialVersionUID = 1L;

  public MappableOligoFeature(OligoFeature oligoFeature, ProbeSet probeSet) {
		this.oligoFeature = oligoFeature;
		this.probeSet = probeSet;
		this.location = oligoFeature.getLocation();
	}
	
	
	public void addTranscript(MappableTranscript transcript) {
		transcripts.add(transcript);
	}
	
	public final OligoFeature oligoFeature;
	/**
	 * Same as oligoFeature.getLocation(). Denormalised for speed.
	 */
	public final Location location;
	public final ProbeSet probeSet;
	public final List transcripts = new ArrayList();
	
	/**
	 * Compares the location and probeset.
	 * 
	 * Can be be used to sort instances of this class by location.
	 * 
	 * @param o other object
	 * @return -1 if the o is not an instance of this class, otherwise returns
	 * location.compareTo(o.location)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		
	  if (! (o instanceof MappableOligoFeature) ) return -1;
		
	  MappableOligoFeature other = (MappableOligoFeature)o;
		int l = location.compareTo(other.location);
		if (l!=0)
		  return l;
		
		return probeSet.probeSetName.compareTo(other.probeSet.probeSetName); 
	}
	
	
  public String toString() {
    return "["+probeSet.probeSetName+", "+location+"]";
  }
  
}
