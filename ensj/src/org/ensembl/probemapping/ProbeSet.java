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
import java.util.Collections;

import org.ensembl.datamodel.OligoArray;
import org.ensembl.datamodel.Location;
import org.ensembl.util.IDSet;
import org.ensembl.util.PersistentSet;

/**
 * A partial representation of a microarray probe set / composite used for
 * probeset to transcript mapping.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 * @see org.ensembl.datamodel.OligoProbe
 * @see org.ensembl.datamodel.OligoArray
 */
public class ProbeSet implements Serializable {

  private static final long serialVersionUID = 1L;

  public final String probeSetName;

  /**
   * Set of zero or more AffyArrays.
   * 
   * @see org.ensembl.datamodel.OligoArray
   */
  private final PersistentSet arrays = new PersistentSet();

  /**
   * List of zero or more MappableAffyFeatures that belong to this probeset.
   * 
   * @see MappableOligoFeature
   */
  public final List oligoFeatures = new ArrayList();

  private List mappedTranscripts;

  /**
   * True if the probe set hits too many transcripts to be mapped to any of
   * them.
   */
  public boolean tooManyTranscripts = false;

  /**
   * @param probeSetName
   *          name of the probe sets
   */
  public ProbeSet(String probeSetName) {
    this.probeSetName = probeSetName;
  }


  /**
   * Adds a mappable oligo feature if it is has not already been added.
   * @param feature a mappable oligo feature
   * @return true if feature was added, otherwise false.
   */
  public boolean addMappableOligoFeatureIfUnique(MappableOligoFeature feature) {
    // Optimisation: Using binarySearch minimizes the number of location
    // comparisons required which is necessary for large datasets.
    int insertPoint = Collections.binarySearch(oligoFeatures,feature);
    // feature already known
    if (insertPoint>=0)
      return false;
    oligoFeatures.add(-insertPoint-1, feature);
    mappedTranscripts = null;
    return true;
  }


  /**
   * Returns a unique set of MappableTranscripts where each one hits at least
   * one of the probes in this probe set.
   * 
   * @return zero or more MappableTranscripts.
   * @see MappableTranscript
   */
  public List getOverlappingTranscripts() {
    if (mappedTranscripts == null) {
      IDSet uniqueIDs = new IDSet();
      mappedTranscripts = new ArrayList();
      for (int i = 0, n = oligoFeatures.size(); i < n; i++) {
        MappableOligoFeature af = (MappableOligoFeature) oligoFeatures.get(i);
        List transcripts = af.transcripts;
        for (int j = 0, z = transcripts.size(); j < z; j++) {
          MappableTranscript t = (MappableTranscript) transcripts.get(j);
          if (!uniqueIDs.contains(t)) {
            mappedTranscripts.add(t);
            uniqueIDs.add(t);
          }
        }
      }

    }
    return mappedTranscripts;
  }


  /**
   * Adds arrays.
   * 
   * @param arrays
   *          zero or more microArrays that contain this probe.
   */
  public void addOligoArrays(List arrays) {
    for (int i = 0; i < arrays.size(); i++) {
      OligoArray a = (OligoArray) arrays.get(i);
      this.arrays.add(a);
    }
  }
  
  /**
   * 
   * @return zero or more microarrays that contain this probe set.
   */
  public List getOligoArrays() {
    return new ArrayList(arrays);
  }
  
  
  public void clearMappableTranscripts() {
    mappedTranscripts.clear();
  }
  
  public void clearMappableOligoFeatures() {
    oligoFeatures.clear();
  }
}
