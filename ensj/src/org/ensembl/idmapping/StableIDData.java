/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.idmapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Holds information about gene stable ids classified by type.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class StableIDData {
  
  public class GeneTypeData {
    public Set stableIDs = new HashSet();
    public Set mapped = new HashSet();
    public Set deleted = new HashSet();
    public Set deletedSimilar = new HashSet();
    public Set deletedDefinately = new HashSet();
      
  }
  
  private Set stableIDs = new HashSet();
  private Map type2GeneTypeData = new HashMap();
  
  
  
  public Set getStableIDs() {
    return stableIDs;
  }

  /**
   * Add gene stableID to type category.
   * 
   * @param type
   * @param stableID
   */
  public void add(String type, String stableID) {
    stableIDs.add(stableID);
    get(type).stableIDs.add(stableID);
  }

  /**
   * Convenience method for getting GeneTypeData for gene type.
   * @param type gene type.
   * @return data for type.
   */
  public GeneTypeData get(String type) {
    GeneTypeData d = (GeneTypeData) type2GeneTypeData.get(type);
    if (d==null) {
      d = new GeneTypeData();
      type2GeneTypeData.put(type, d);
    }
    return d;
  }
  
  public Set types() {
    return type2GeneTypeData.keySet();
  }
}
