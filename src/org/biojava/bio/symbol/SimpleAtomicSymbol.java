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

import org.biojava.bio.*;

/**
 * A no-frills implementation of AtomicSymbol.
 *
 * @author Matthew Pocock
 */
public class SimpleAtomicSymbol implements AtomicSymbol {
  private final Symbol delegate;
  
  public SimpleAtomicSymbol(char token, String name, Annotation annotation) {
    delegate = new SimpleSymbol(token, name, new SingletonAlphabet(this), annotation);
  }
  
  public char getToken() {
    return delegate.getToken();
  }
  
  public String getName() {
    return delegate.getName();
  }
  
  public Annotation getAnnotation() {
    return delegate.getAnnotation();
  }
  
  public Alphabet getMatches() {
    return delegate.getMatches();
  }
}
