package org.biojava.bio.taxa;

import java.util.*;
import org.biojava.utils.*;

/**
 * A no-frills implementation of TaxaFactory that builds an in-memory Taxa tree.
 *
 * @author Matthew Pocock
 */
public class SimpleTaxaFactory implements TaxaFactory {
  /**
   * The TaxaFactory that the biojava system should use for stooring the
   * taxonomy used by swissprot and embl as in-memory objects.
   */
  public static final SimpleTaxaFactory GLOBAL
    = new SimpleTaxaFactory("GLOBAL");
  
  private final Taxa root;
  private final String name;
  private final Map taxaBySciName = new HashMap();
  
  public SimpleTaxaFactory(String name) {
    this.name = name;
    this.root = createTaxa("ROOT", "");
  }
  
  public Taxa getRoot() {
    return root;
  }
  
  public String getName() {
    return name;
  }
  
  public Taxa importTaxa(Taxa taxa) {
    SimpleTaxa can = canonicallize(taxa);
    if(can == null) {
      can = new SimpleTaxa(taxa.getScientificName(), taxa.getCommonName());
      
      for(Iterator i = taxa.getChildren().iterator(); i.hasNext(); ) {
        Taxa child = (Taxa) i.next();
        addChild(can, child);
      }
      
      return can;
    } else {
      return can;
    }
  }
  
  public Taxa createTaxa(String scientificName, String commonName) {
    Taxa taxa = new SimpleTaxa(scientificName, commonName);
    taxaBySciName.put(scientificName, taxa);
    return taxa;
  }
  
  public Taxa addChild(Taxa parent, Taxa child) {
    if(canonicallize(parent) == null) {
      throw new IllegalArgumentException("Parent taxa not owned by this TaxaFactory");
    }
    
    SimpleTaxa sparent = (SimpleTaxa) parent;
    SimpleTaxa schild = (SimpleTaxa) importTaxa(child);
    
    if(sparent.children == null) {
      sparent.children = new SmallSet();
    }
    
    sparent.children.add(schild);
    schild.setParent(sparent);
    
    return schild;
  }
  
  public Taxa removeChild(Taxa parent, Taxa child) {
    SimpleTaxa sparent = canonicallize(parent);
    SimpleTaxa schild = canonicallize(child);
    
    if(sparent == null) {
      throw new IllegalArgumentException("Don't know about parent taxa");
    }
    
    if(
      (schild != null) &&
      (sparent.children != null) &&
      (sparent.children.remove(schild))
    ) {
      return schild;
    } else {
      return null;
    }
  }
  
  public Taxa search(Object id) {
    return (Taxa) taxaBySciName.get(id);
  }
  
  private SimpleTaxa canonicallize(Taxa taxa) {
    return (SimpleTaxa) search(taxa.getScientificName());
  }
}
