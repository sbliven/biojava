package org.biojava.bio.taxa;

import java.util.*;

import org.biojava.utils.*;

/**
 * Encapsulate the 'EBI' species format used in Embl, Genbank and Swissprot
 * files.
 *
 * @author Matthew Pocock
 */
public class EbiFormat implements TaxaParser {
  public static final String PROPERTY_NCBI_TAXA = EbiFormat.class + ":NCBI_TAXA";
  private static EbiFormat INSTANCE = new EbiFormat();
  
  public static final EbiFormat getInstance() {
    if(INSTANCE == null) {
      INSTANCE = new EbiFormat();
    }
    
    return INSTANCE;
  }
  
  public Taxa parse(TaxaFactory taxaFactory, String taxaString)
    throws
      ChangeVetoException,
      CircularReferenceException
  {
    String name = taxaString.trim();
    if(name.endsWith(".")) {
      name = name.substring(0, name.length() - 1);
    }
    
    Taxa taxa = taxaFactory.getRoot();
    StringTokenizer sTok = new StringTokenizer(name, ";");
    
    if(sTok.countTokens() == 1) {
      return taxaFactory.addChild(taxa, taxaFactory.createTaxa(name, null));
    }
    
    String tok = null;
    CLIMB_TREE:
    while(sTok.hasMoreTokens()) {
      tok = sTok.nextToken().trim();
      for(Iterator i = taxa.getChildren().iterator(); i.hasNext(); ) {
        Taxa child = (Taxa) i.next();
        if(child.getScientificName().equals(tok)) {
          taxa = child;
          continue CLIMB_TREE; // found child by name - go through loop again
        }
      }
      
      break; // couldn't find a child by than name - stop this and move on
    }
    
    for(; sTok.hasMoreTokens(); tok = sTok.nextToken().trim()) {
      taxa = taxaFactory.addChild(
        taxa,
        taxaFactory.createTaxa(tok, null)
      );
    }
    
    return taxa;
  }
  
  public String serialize(Taxa taxa) {
    String name = null;
    
    do {
      String sci = taxa.getScientificName();
      if(name == null) {
        name = sci + ".";
      } else {
        name = sci + "; " + name;
      }
      taxa = taxa.getParent();
    } while(taxa != null && taxa.getParent() != null);
    
    return name;
  }
}
