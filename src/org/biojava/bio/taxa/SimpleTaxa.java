package org.biojava.bio.taxa;

import java.util.*;
import org.biojava.utils.*;
import org.biojava.bio.*;

/**
 * A no-frills implementatation of Taxa.
 *
 * <p>
 * A TaxaFactory implementation will probably wish to sub-class this and add
 * package-private accessors for the parent and children fields as well as a
 * pacakge-private constructor.
 * </p>
 *
 * @author Matthew Pocock
 */
public class SimpleTaxa extends AbstractTaxa {
  private String commonName;
  private String scientificName;
  protected Taxa parent;
  protected Set children;
  
  /**
   * Create a new instance with no parent, no children and a given scientific
   * and common names.
   */
  protected SimpleTaxa(String scientificName, String commonName) {
    this.scientificName = scientificName;
    this.commonName = commonName;
  }
  
  public String getCommonName() {
    return commonName;
  }
  
  public String getScientificName() {
    return scientificName;
  }
  
  public Taxa getParent() {
    return parent;
  }
  
  void setParent(Taxa parent) {
    this.parent = parent;
  }
  
  public Set getChildren() {
    if(children != null) {
      return children;
    } else {
      return Collections.EMPTY_SET;
    }
  }
  
  public String toString() {
    if(parent != null) {
      return parent.toString() + " -> " + scientificName;
    } else {
      return scientificName;
    }
  }
}
