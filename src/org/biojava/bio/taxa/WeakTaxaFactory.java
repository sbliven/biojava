package org.biojava.bio.taxa;

import java.util.*;
import java.lang.ref.*;
import org.biojava.utils.*;

/**
 * An implementation of TaxaFactory that builds a weak in-memory Taxa tree.
 *
 * <p>
 * This implementation holds only weak references to the Taxa instances it knows
 * about. This means that WeakTaxaFactory may not be apropreate for situations
 * where you wish to browse the taxa tree. It does, however, mean that
 * massive  taxas can be represented, by effectively reflecting the currently
 * usefull rooted sub-tree in memory.
 * </p>
 *
 * @author Matthew Pocock
 */
public class WeakTaxaFactory implements TaxaFactory {
  /**
   * The TaxaFactory that the biojava system should use for stooring the
   * taxonomy used by swissprot and embl as in-memory objects.
   */
  public static final WeakTaxaFactory GLOBAL
    = new WeakTaxaFactory("GLOBAL");
  
  private final Taxa root;
  private final String name;
  private final Map taxaBySciName = new WeakHashMap();
  
  public WeakTaxaFactory(String name) {
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
    WeakTaxa can = canonicallize(taxa);
    if(can == null) {
      can = new WeakTaxa(taxa.getScientificName(), taxa.getCommonName());
      
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
    Taxa taxa = new WeakTaxa(scientificName, commonName);
    taxaBySciName.put(scientificName, new WeakReference(taxa));
    return taxa;
  }
  
  public Taxa addChild(Taxa parent, Taxa child) {
    WeakTaxa sparent = (WeakTaxa) importTaxa(parent);
    WeakTaxa schild = (WeakTaxa) importTaxa(child);
    
    Set children = sparent.getChildrenRaw();
    if(children == null) {
      children = new SmallSet();
      sparent.setChildrenRaw(children);
    }
    
    children.add(schild);
    schild.setParent(sparent);
    
    return schild;
  }
  
  public Taxa removeChild(Taxa parent, Taxa child) {
    WeakTaxa sparent = canonicallize(parent);
    WeakTaxa schild = canonicallize(child);
    
    if(sparent == null) {
      throw new IllegalArgumentException("Don't know about parent taxa");
    }
    
    Set children = sparent.getChildrenRaw();
    if(
      (schild != null) &&
      (children != null) &&
      (children.remove(schild))
    ) {
      return schild;
    } else {
      return null;
    }
  }
  
  public Taxa search(Object id) {
    WeakReference wr = (WeakReference) taxaBySciName.get(id);
    if(wr != null) {
      return (Taxa) wr.get();
    } else {
      return null;
    }
  }
  
  private WeakTaxa canonicallize(Taxa taxa) {
    return (WeakTaxa) search(taxa.getScientificName());
  }
}
