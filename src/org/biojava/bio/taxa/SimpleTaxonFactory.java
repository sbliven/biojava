package org.biojava.bio.taxa;

import java.util.*;
import org.biojava.utils.*;

/**
 * A no-frills implementation of TaxaFactory that builds an in-memory Taxa tree.
 *
 * @author Matthew Pocock
 */
public class SimpleTaxonFactory implements TaxonFactory {
  /**
   * The TaxonFactory that the biojava system should use for storing
   * the taxonomy used by swissprot and embl as in-memory objects.
   */
  public static final SimpleTaxonFactory GLOBAL
    = new SimpleTaxonFactory("GLOBAL");
  
  private final Taxon root;
  private final String name;
  private final Map taxonBySciName = new HashMap();
  
  public SimpleTaxonFactory(String name) {
    this.name = name;
    this.root = createTaxon("ROOT", "");
  }
  
  public Taxon getRoot() {
    return root;
  }
  
  public String getName() {
    return name;
  }
  
  public Taxon importTaxon(Taxon taxon) {
    SimpleTaxon can = canonicalize(taxon);
    if(can == null) {
      can = new SimpleTaxon(taxon.getScientificName(), taxon.getCommonName());
      
      for(Iterator i = taxon.getChildren().iterator(); i.hasNext(); ) {
        Taxon child = (Taxon) i.next();
        addChild(can, child);
      }
      
      return can;
    } else {
      return can;
    }
  }
  
  public Taxon createTaxon(String scientificName, String commonName) {
    Taxon taxon = new SimpleTaxon(scientificName, commonName);
    taxonBySciName.put(scientificName, taxon);
    return taxon;
  }
  
  public Taxon addChild(Taxon parent, Taxon child) {
    if(canonicalize(parent) == null) {
      throw new IllegalArgumentException("Parent taxon not owned by this TaxonFactory");
    }
    
    SimpleTaxon sparent = (SimpleTaxon) parent;
    SimpleTaxon schild = (SimpleTaxon) importTaxon(child);
    
    if(sparent.children == null) {
      sparent.children = new SmallSet();
    }
    
    sparent.children.add(schild);
    schild.setParent(sparent);
    
    return schild;
  }
  
  public Taxon removeChild(Taxon parent, Taxon child) {
    SimpleTaxon sparent = canonicalize(parent);
    SimpleTaxon schild = canonicalize(child);
    
    if(sparent == null) {
      throw new IllegalArgumentException("Don't know about parent Taxon");
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
  
  public Taxon search(Object id) {
    return (Taxon) taxonBySciName.get(id);
  }
  
  private SimpleTaxon canonicalize(Taxon taxon) {
    return (SimpleTaxon) search(taxon.getScientificName());
  }
}
