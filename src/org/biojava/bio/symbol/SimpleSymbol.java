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
  protected Set basies;
  
  public SimpleSymbol(
    char token, String name, Annotation annotation,
    Set basies
  ) {
    this.token = token;
    this.name = name;
    this.annotation = new SimpleAnnotation(annotation);
    if(basies == null) {
      this.basies = null;
    } else {
      this.basies = Collections.unmodifiableSet(basies);
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
    Set basies = getBasies();
    Set mat = new HashSet();
    for(Iterator i = basies.iterator(); i.hasNext(); ) {
      BasisSymbol bs = (BasisSymbol) i.next();
      if(bs instanceof AtomicSymbol) {
        mat.add(bs);
      } else {
        FiniteAlphabet ma = (FiniteAlphabet) bs.getMatches();
        for(Iterator j = ma.iterator(); j.hasNext(); ) {
          mat.add((AtomicSymbol) j.next());
        }
      }
    }
    return new SimpleAlphabet(mat);
  }
  
  public Set getBasies() {
    if(basies == null) {
      basies = createBasies();
    }
    return basies;
  }
  
  protected Set createBasies() {
    throw new BioError("Assertion Failure: Basies set is null");
  }
}
