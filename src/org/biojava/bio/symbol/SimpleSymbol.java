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
 * A no-frills implementation of Symbol.
 *
 * @author Matthew Pocock
 */
class SimpleSymbol extends AbstractSymbol
implements Symbol, Serializable {
  private final char token;
  private final String name;
  private final Annotation annotation;
  protected Alphabet matches;
  
  protected SimpleSymbol(
    char token, String name, Annotation annotation
  ) {
    this.token = token;
    this.name = name;
    this.annotation = new SimpleAnnotation(annotation);
  }
  
  public SimpleSymbol(
    char token, String name, Annotation annotation,
    Alphabet matches
  ) {
    this(token, name, annotation);
    if(matches == null) {
      throw new NullPointerException(
        "Can't construct SimpleSymbol with a null matches alphabet"
      );
    } else {
      this.matches = matches;
    }
  }
  
  public char getToken() {
    return token;
  }
  
  public String getName() {
    return name;
  }
  
  public Annotation getAnnotation() {
    return annotation;
  }
  
  public Alphabet getMatches() {
    if(matches == null) {
      matches = createMatches();
    }
    return matches;
  }
  
  protected Alphabet createMatches() {
    throw new BioError(
      "Assertion Failure: Matches alphabet is null in " + this
    );
  }
}
