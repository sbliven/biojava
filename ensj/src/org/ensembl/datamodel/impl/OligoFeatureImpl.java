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

package org.ensembl.datamodel.impl;

import org.ensembl.datamodel.OligoFeature;
import org.ensembl.datamodel.OligoProbe;
import org.ensembl.datamodel.Location;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.RuntimeAdaptorException;

/**
 * Implementation of the OligoFeature type.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class OligoFeatureImpl extends BaseFeatureImpl implements OligoFeature {

  /**
   * Used by the (de)serialization system to determine if the data 
   * in a serialized instance is compatible with this class.
   *
   * It's presence allows for compatible serialized objects to be loaded when
   * the class is compatible with the serialized instance, even if:
   *
   * <ul>
   * <li> the compiler used to compile the "serializing" version of the class
   * differs from the one used to compile the "deserialising" version of the
   * class.</li>
   *
   * <li> the methods of the class changes but the attributes remain the same.</li>
   * </ul>
   *
   * Maintainers must change this value if and only if the new version of
   * this class is not compatible with old versions. e.g. attributes
   * change. See Sun docs for <a
   * href="http://java.sun.com/j2se/1.4.2/docs/guide/serialization/">
   * details. </a>
   *
   */
  private static final long serialVersionUID = 1L;



	private OligoProbe probe;
	private long probeID;
	private int misMatchSize;
	private String probeSetName;
	
	
	
	/**
	 * @param driver
	 * @param internalID
	 * @param location
	 * @param probeSetName
	 * @param probeID
	 * @param misMatchSize
	 */
	public OligoFeatureImpl(CoreDriver driver, long internalID, Location location,
			String probeSetName, long probeID, int misMatchSize) {
		super(driver, internalID, location);
		this.probeID = probeID;
		this.misMatchSize = misMatchSize;
		this.probeSetName = probeSetName;
	}
	
	
	
	
	/**
	 * The name of the probe's probeset. 
	 * 
	 * Funcionally equivalent to getProbe().getProbeSetName() but avoids
	 * lazy loading of probe if the probe set name has already been set.
	 * @return name of the probe's probeset.
	 */
	public String getProbeSetName() {
		if (probeSetName==null) probeSetName = getProbe().getProbeSetName();
		return probeSetName;
	}
	/**
	 * @see org.ensembl.datamodel.OligoFeature#getProbe()
	 */
	public OligoProbe getProbe() {
		if (probe==null && probeID>0)
			try {
				probe = driver.getOligoProbeAdaptor().fetch(probeID);
			} catch (AdaptorException e) {
				throw new RuntimeAdaptorException("Failed to lazy load Probe (internalID= "+probeID+") for OligoFeature (internalID="+internalID+") ",e);
			} 
		return probe;
	}

	/**
	 * @see org.ensembl.datamodel.OligoFeature#getMisMatchSize()
	 */
	public int getMisMatchSize() {
		return misMatchSize;
	}


	public String toString() {
		StringBuffer buf = new StringBuffer();

		buf.append("[").append(super.toString());
		buf.append(", probe=").append(getProbe());
		buf.append(", misMatchSize=").append(misMatchSize);
		buf.append("]");

		return buf.toString();
	}




	/**
	 * @see org.ensembl.datamodel.OligoFeature#getProbeInternalID()
	 */
	public long getProbeInternalID() {
		return probeID;
	}
	
	
	/**
	 * Compares this instance to another. Useful for sorting by location first
	 * and probeInternalID second.
	 * @return <0 if this instance is before _o_, 0 if it is equivalent
	 * and >0 otherwise.
	 */
	public int compareTo(Object o) {
		int r = super.compareTo(o);
		// sort by location first
		if (r!=0) return r;
		// sort by probe internalID second
		else return (int) (probeID-((OligoFeatureImpl)o).probeID);
	}
	
}
