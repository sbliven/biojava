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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ensembl.datamodel.OligoArray;
import org.ensembl.datamodel.OligoFeature;
import org.ensembl.datamodel.OligoProbe;
import org.ensembl.datamodel.Location;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.RuntimeAdaptorException;
import org.ensembl.util.StringUtil;

/**
 * Implementation of the OligoProbe type.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class OligoProbeImpl extends PersistentImpl implements OligoProbe {

	private static final long serialVersionUID = 1L;
	
	private List arrays;
	private Map names = new HashMap();
	private String probeSetName;
	private List oligoFeatures;
  private int length;
  private String description;
	
	
	/**
	 * Creates an OligoProbe instance that will lazy load the OligoArrays it 
	 * appears in and it's OligoFeatures.
	 * @param internalID internalID in database.
	 * @param driver source driver.
	 * @param probeSetName name of the probeset the probe belongs to.
	 */
	public OligoProbeImpl(CoreDriver driver, long internalID, String probeSetName, int length, String description) {
		super(driver);
		this.internalID = internalID;
		this.probeSetName = probeSetName;
    this.length = length;
    this.description = description;
	}
	
	
	/**
	 * @see org.ensembl.datamodel.OligoProbe#getArraysContainingThisProbe()
	 */
	public List getArraysContainingThisProbe() {
		if (arrays==null) arrays = new ArrayList(names.keySet());
		return arrays;
	}

	/**
	 * @see org.ensembl.datamodel.OligoProbe#getQualifiedName(org.ensembl.datamodel.OligoArray)
	 */
	public String getQualifiedName(OligoArray array) {
		return array.getName()+":"+probeSetName+":"+names.get(array);
	}

	/**
	 * @see org.ensembl.datamodel.OligoProbe#getQualifiedNames()
	 */
	public String[] getQualifiedNames() {
		String[] qNames = new String[getArraysContainingThisProbe().size()];
		for (int i = 0; i < qNames.length; i++) 
			qNames[i] = getQualifiedName((OligoArray) arrays.get(i));
		return qNames;
	}

	
	public boolean isProbeInArray(OligoArray array) {
		return names.containsKey(array);
	}
	
	/**
	 * @see org.ensembl.datamodel.OligoProbe#getNameInArray(OligoArray)
	 */
	public String getNameInArray(OligoArray array) {
		return (String) names.get(array);
	}

	/**
	 * @see org.ensembl.datamodel.OligoProbe#getProbeSetName()
	 */
	public String getProbeSetName() {
		return probeSetName;
	}

	/**
	 * @see org.ensembl.datamodel.OligoProbe#getOligoFeatures()
	 */
	public List getOligoFeatures() {
		if (oligoFeatures==null)
			try {
				oligoFeatures = driver.getOligoFeatureAdaptor().fetch(this);
			} catch (AdaptorException e) {
				throw new RuntimeAdaptorException("Failed to lazy load OligoFeatures for OligoProbe: "+this,e);
			}
		return oligoFeatures;
	}

	
	public String toString() {
		StringBuffer buf = new StringBuffer();

		buf.append("[");
		buf.append(super.toString());
		buf.append(", names=").append(StringUtil.toString(getQualifiedNames()));
		buf.append(", probeSetName=").append(probeSetName);
		buf.append(", oligoFeatures=").append(StringUtil.sizeOrUnset(oligoFeatures));
		buf.append(", parentOligoArrays=").append(StringUtil.sizeOrUnset(arrays));
		buf.append("]");

		return buf.toString();
	}


	/**
	 * @see org.ensembl.datamodel.OligoProbe#addArrayWithProbeName(org.ensembl.datamodel.OligoArray, java.lang.String)
	 */
	public void addArrayWithProbeName(OligoArray array, String name) {
		names.put(array,name);		
		arrays = null;
	}


  /**
   * @see org.ensembl.datamodel.OligoProbe#getUniqueOligoFeatureLocations()
   */
  public List getUniqueOligoFeatureLocations() {
    Set s = new HashSet();
    List afs = getOligoFeatures();
    for (int i = 0; i < afs.size(); i++) 
      s.add(((OligoFeature)afs.get(i)).getLocation().toString());
      
    List l = new ArrayList(s.size());
    for (Iterator iter = s.iterator(); iter.hasNext();) 
      try {
      l.add(new Location(iter.next().toString()));
      } catch (ParseException e) {
        throw new RuntimeException(e);
      }
      
    return l;
  }

  /**
   * @see org.ensembl.datamodel.OligoProbe#getLength()
   */
  public int getLength() {
    
    return length;
  }


  public String getDescription() {
    return description;
  }
  
  
  
}
