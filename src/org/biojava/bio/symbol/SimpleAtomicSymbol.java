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
 * A no-frills implementation of AtomicSymbol.
 * <P>
 * In general, you should use the methods in AlphabetManager to instantiate
 * Symbol objects. However, this class is public with a protected constructor
 * to help out when you need to implement and extend AtomicSymbol.
 *
 * @author Matthew Pocock
 */
public class SimpleAtomicSymbol extends SimpleBasisSymbol
implements AtomicSymbol {  
  protected SimpleAtomicSymbol(
    char token, String name, Annotation annotation
  ) {
    super(token, name, annotation);
  }
  
  protected SimpleAtomicSymbol(
    char token, String name, Annotation annotation,
    List syms
  ) throws IllegalSymbolException {
    super(token, name, annotation, syms);
  }
  
  protected List createSymbols() {
    List syms = new SingletonList(this);
    return syms;
  }
  
  protected Alphabet createMatches() {
    return new SingletonAlphabet(this);
  }
}
