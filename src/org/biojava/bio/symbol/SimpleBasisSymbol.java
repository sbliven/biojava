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
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.*;

/**
 * A no-frills implementation of BasisSymbol.
 *
 * @author Matthew Pocock
 */
class SimpleBasisSymbol extends SimpleSymbol
implements BasisSymbol {
  protected List symbols;
  
  public SimpleBasisSymbol(
    char token, String name, Annotation annotation,
    List symbols
  ) {
    this(token, name, annotation);
    this.symbols = symbols;
  }
  
  protected SimpleBasisSymbol(
    char token, String name, Annotation annotation
  ) {
    super(token, name, annotation, null);
  }
  
  public SimpleBasisSymbol(
    char token, String name, Annotation annotation,
    Set syms
  ) {
    this(token, name, annotation);
    this.symbols = new SingletonList(this);
    this.basies = Collections.singleton(this);
    this.matches = new SimpleAlphabet(syms);
  }
  
  public List getSymbols() {
    if(symbols == null) {
      symbols = createSymbols();
    }
    return symbols;
  }
  
  protected List createSymbols() {
    throw new BioError("Assertion Failure: Symbols list is null");
  }
  
  protected Set createBasies() {
    return Collections.singleton(this);
  }
  
  protected Alphabet createMatches() {
    return AlphabetManager.expand(this);
  }
}

