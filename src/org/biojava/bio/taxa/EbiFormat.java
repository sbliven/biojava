package org.biojava.bio.taxa;

import java.util.*;

import org.biojava.utils.*;

/**
 * Encapsulate the 'EBI' species format used in Embl, Genbank and Swissprot
 * files.
 *
 * @author Matthew Pocock
 */
public class EbiFormat implements TaxonParser {
  public static final String PROPERTY_NCBI_TAXON = EbiFormat.class + ":NCBI_TAXON";
  private static EbiFormat INSTANCE = new EbiFormat();
  
  public static final EbiFormat getInstance() {
    if(INSTANCE == null) {
      INSTANCE = new EbiFormat();
    }
    
    return INSTANCE;
  }
  
  public Taxon parse(TaxonFactory taxonFactory, String taxonString)
    throws
      ChangeVetoException,
      CircularReferenceException
  {
    String name = taxonString.trim();
    if(name.endsWith(".")) {
      name = name.substring(0, name.length() - 1);
    }
    
    Taxon taxon = taxonFactory.getRoot();
    StringTokenizer sTok = new StringTokenizer(name, ";");
    
    if(sTok.countTokens() == 1) {
      return taxonFactory.addChild(taxon, taxonFactory.createTaxon(name, null));
    }
    
    String tok = null;
    CLIMB_TREE:
    while(sTok.hasMoreTokens()) {
      tok = sTok.nextToken().trim();
      for(Iterator i = taxon.getChildren().iterator(); i.hasNext(); ) {
        Taxon child = (Taxon) i.next();
        if(child.getScientificName().equals(tok)) {
          taxon = child;
          continue CLIMB_TREE; // found child by name - go through loop again
        }
      }
      
      break; // couldn't find a child by than name - stop this and move on
    }
    
    for(; sTok.hasMoreTokens(); tok = sTok.nextToken().trim()) {
      taxon = taxonFactory.addChild(
        taxon,
        taxonFactory.createTaxon(tok, null)
      );
    }
    
    return taxon;
  }
  
  public String serialize(Taxon taxon) {
    String name = null;
    
    do {
      String sci = taxon.getScientificName();
      if(name == null) {
        name = sci + ".";
      } else {
        name = sci + "; " + name;
      }
      taxon = taxon.getParent();
    } while(taxon != null && taxon.getParent() != null);
    
    return name;
  }
}
