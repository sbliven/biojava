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
  private final Annotation annotation;
  protected Alphabet matches;
  
  protected SimpleSymbol(char token, Annotation annotation) 
  {
    this.token = token;
    this.annotation = new SimpleAnnotation(annotation);
  }
  
  public SimpleSymbol(
    char token, Annotation annotation,
    Alphabet matches
  ) {
    this(token, annotation);
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

    public String getName() {
	Alphabet a = getMatches();

	if (this instanceof BasisSymbol) {
	    List l = ((BasisSymbol) this).getSymbols();
	    if (l.size() > 1) {
		StringBuffer sb = new StringBuffer();
		sb.append('(');
		for (Iterator si = l.iterator(); si.hasNext(); ) {
		    Symbol sym = (Symbol) si.next();
		    sb.append(sym.getName());
		    if (si.hasNext())
			sb.append(' ');
		}
		sb.append(')');
		return sb.toString();
	    }
	} 

	if (a instanceof FiniteAlphabet) {
	    FiniteAlphabet fa = (FiniteAlphabet) a;
	    if (fa.size() <= 1)
		return null;

	    StringBuffer sb = new StringBuffer();
	    sb.append('[');
	    for (Iterator si = fa.iterator();
		 si.hasNext(); )
		{
		    Symbol sym = (Symbol) si.next();
		    sb.append(sym.getName());
		    if (si.hasNext())
			sb.append(' ');
		}
	    sb.append(']');
	    return sb.toString();
	} else {
	    return "Infinite";
	}
    }
}
