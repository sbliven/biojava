package org.ensembl.probemapping;

import java.util.Comparator;

import org.ensembl.datamodel.Locatable;
import org.ensembl.datamodel.Location;

public class LocationOverlapComparator implements Comparator{

  /**
   * Return whether o1 and o2 overlap otherwise their correct ordering.
   * 
   * Like location.compareTo but considers overlapping locations as equal. 
   *  
   * @return 0 if the locations of o1 and o2 overlap, otherwise 
   * return o1.location.compareTo(b.location).
   */
  public int compare(Object o1, Object o2) {
    final Location a = ((Locatable) o1).getLocation();
    final Location b = ((Locatable) o2).getLocation();
    
    if (a.overlaps(b,false)) return 0;
    else return a.compareTo(b);
  }

}
