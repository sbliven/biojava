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

import org.biojava.bio.*;

/**
 * Concrete implementation of CrossProductSymbol, as returned
 * by a SimpleCrossProductAlphabet and InfiniteCrossProductAlphabet.
 * <P>
 * You should not normaly have to instantiate one of these directly - rather
 * you would retrieve a CrossProductAlphabet from the AlphabetManager and use
 * the CrossProductSymbol objects that it creates.
 * <P>
 * You will need to use this class if you write your own implementation of
 * CrossProductAlphabet.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */

public class SimpleCrossProductSymbol
implements CrossProductSymbol, Serializable {
  private final List l;
  private final char token;

  public SimpleCrossProductSymbol(List l, char token) {
    this.l = l;
    this.token = token;
  }

  public List getSymbols() {
    return l;
  }

  public String getName() {
    StringBuffer name = new StringBuffer("(");
    for (int i = 0; i < l.size(); ++i) {
      Symbol r = (Symbol) l.get(i);
      name.append(r.getName());
      if (i < l.size() - 1) {
        name.append(", ");
      }
    }
    name.append(")");
    return name.toString();
  }

  public char getToken() {
    return token;
  }

  public Annotation getAnnotation() {
    return Annotation.EMPTY_ANNOTATION;
  }
}

