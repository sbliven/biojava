package org.biojava.bio.taxa;

import java.util.*;
import org.biojava.utils.*;
import org.biojava.bio.*;

/**
 * A no-frills implementatation of Taxon.
 *
 * <p>A TaxonFactory implementation will probably wish to sub-class
 * this and add package-private accessors for the parent and children
 * fields as well as a pacakge-private constructor.</p>
 *
 * @author Matthew Pocock
 */
public class SimpleTaxon extends AbstractTaxon {
  protected Taxon parent;
  protected Set children;
  
  protected SimpleTaxon() { super(); }
  
  /**
   * Create a new instance with no parent, no children and given
   * scientific and common names.
   */
  protected SimpleTaxon(String scientificName, String commonName) {
    super(scientificName, commonName);
  }
  
  public Taxon getParent() {
    return parent;
  }
  
  void setParent(Taxon parent) {
    this.parent = parent;
  }
  
  public Set getChildren() {
    if(children != null) {
      return children;
    } else {
      return Collections.EMPTY_SET;
    }
  }
}
