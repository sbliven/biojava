package org.biojava.bio.taxa;

import java.util.*;
import java.lang.ref.*;

/**
 * <p>An implementation of Taxon that keeps only weak references to
 * children, but full references to parents.</p>
 *
 * <p>This may be suitable for deriving memory-savy implementations
 * of TaxonFactory.</p>
 *
 * <p>To manipulate the children set, use the getChildrenRaw and
 * setChildrenRaw methods. These 'box' the actual weak reference, but
 * recognize null to mean that there are no children currently
 * known. A code-fragment may wish to do something like this:</p>
 *
 * <pre><code>
 * Set children = weakTaxon.getChildrenRaw();
 * if(children == null) {
 *   children = new HashSet();
 *   weakTaxon.setChildrenRaw(children);
 * }
 * // do stuff to update child set e.g. add children 
 * </code></pre>
 * </p>
 *
 * @author Matthew Pocock
 */
public class WeakTaxon extends AbstractTaxon {
  protected Taxon parent;
  private WeakReference /*Set*/ children;
  
  public WeakTaxon() {
    super();
  }
  
  public WeakTaxon(String scientificName, String commonName) {
    super(scientificName, commonName);
  }
  
  public Taxon getParent() {
    return parent;
  }
  
  void setParent(Taxon parent) {
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
}
