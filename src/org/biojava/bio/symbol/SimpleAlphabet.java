/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */


package org.biojava.bio.symbol;

import java.util.*;
import org.biojava.bio.*;

/**
 * A simple no-frills implementation of the Alphabet interface.
 *
 * @author Matthew Pocock
 */
public class SimpleAlphabet extends AbstractAlphabet {
  /**
   * The name of this alphabet.
   */
  private String name;
  
  /**
   * The annotation associated with this alphabet.
   */
  private Annotation annotation;
  
  /**
   * A set of all symbols within the alphabet.
   */
  private Set symbols;

  /**
   * Initialize the symbols set.
   */
  {
    symbols	= new HashSet();
  }

  public Iterator iterator() {
    return symbols.iterator();
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
    
  public Annotation getAnnotation() {
    if(annotation == null)
      annotation = new SimpleAnnotation();
    return annotation;
  }

  public int size() {
    return symbols.size();
  }
  
  public SymbolList symbols() {
    return new SimpleSymbolList(this, new ArrayList(symbols));
  }

  public boolean contains(Symbol r) {
    return symbols.contains(r);
  }

  public void addSymbol(Symbol r)
  throws IllegalSymbolException {
    if(r == null) {
      throw new IllegalSymbolException("You can not add null as a symbol");
    }
    symbols.add(r);
  }
  
  public void removeSymbol(Symbol r)
  throws IllegalSymbolException {
    validate(r);
    symbols.remove(r);
  }

  public void validate(Symbol r) throws IllegalSymbolException {
    if(!contains(r)) {
      if(r == null) {
        throw new IllegalSymbolException("NULL is an illegal symbol");
      } else {
        throw new IllegalSymbolException("Symbol " + r.getName() +
                                          " not found in alphabet " +
                                          getName());
      }
    }
  }
}
