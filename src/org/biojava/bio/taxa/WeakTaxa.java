package org.biojava.bio.taxa;

import java.util.*;
import java.lang.ref.*;

/**
 * An implementation of Taxa that keeps only weak references to children, but
 * full references to parents.
 *
 * <p>
 * This may be suitable for deriving memory-savy implementations of TaxaFactory.
 * </p>
 *
 * <p>
 * To manipulate the children set, use the getChildrenRaw and setChildrenRaw
 * methods. These 'box' the actual weak reference, but recognize null to mean
 * that there are no children currently known. A code-fragment may wish to do
 * something like this:
 * <pre><code>
 * Set children = weakTaxa.getChildrenRaw();
 * if(children == null) {
 *   children = new HashSet();
 *   weakTaxa.setChildrenRaw(children);
 * }
 * // do stuff to update child set e.g. add children 
 * </code></pre>
 * </p>
 *
 * @author Matthew Pocock
 */
public class WeakTaxa extends AbstractTaxa {
  private String commonName;
  private String scientificName;
  protected Taxa parent;
  private WeakReference /*Set*/ children;
  
  public WeakTaxa(String scientificName, String commonName) {
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
    Set c = getChildrenRaw();
    if(c != null) {
      return c;
    } else {
      return Collections.EMPTY_SET;
    }
  }
  
  public Set getChildrenRaw() {
    if(children != null) {
      Set c = (Set) children.get();
      if(c != null) {
        return c;
      }
    }
    
    return null;
  }
  
  public void setChildrenRaw(Set children) {
    this.children = new WeakReference(children);
  }
  
  public String toString() {
    if(parent != null) {
      return parent.toString() + " -> " + scientificName;
    } else {
      return scientificName;
    }
  }
}
