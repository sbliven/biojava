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
  
  protected SimpleBasisSymbol(
    char token, String name, Annotation annotation,
    List symbols
  ) throws IllegalSymbolException {
    this(token, name, annotation);
    if(symbols == null) {
      throw new NullPointerException("symbols can't be null");
    }
    if(symbols.size() == 0) {
      throw new IllegalSymbolException(
        "Can't create BasisSymbol for an empty list. Use the Gap symbol."
      );
    }
    this.symbols = Collections.unmodifiableList(new ArrayList(symbols));
  }
  
  protected SimpleBasisSymbol(
    char token, String name, Annotation annotation
  ) {
    super(token, name, annotation);
  }
  
  public SimpleBasisSymbol(
    char token, String name, Annotation annotation,
    Alphabet matches
  ) {
    this(token, name, annotation);
    this.matches = matches;
  }
  
  public SimpleBasisSymbol(
    char token, String name, Annotation annotation,
    List symbols, Alphabet matches
  ) throws IllegalSymbolException {
    this(token, name, annotation, symbols);
    this.symbols = symbols;
    this.matches = matches;
  }
  
  public final List getSymbols() {
    if(symbols == null) {
      symbols = createSymbols();
    }
    if(symbols.size() == 0) {
      throw new BioError(
        "Assertion Failure: symbols array is of length 0 in " + this +
        "\n\ttoken: " + getToken() +
        "\n\tname: " + getName() +
        "\n\tsymbols: " + this.symbols +
        "\n\tmatches: " + this.matches
      );
    }
    return symbols;
  }
  
  protected List createSymbols() {
    throw new BioError("Assertion Failure: Symbols list is null");
  }
}
