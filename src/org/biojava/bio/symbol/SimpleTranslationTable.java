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

/**
 * A no-frills implementation of TranslationTable that uses a Map to map from
 * symbols in a finite source alphabet into a target alphabet.
 *
 * @author Matthew Pocock
 */
public class SimpleTranslationTable implements TranslationTable, Serializable {
  private final Map transMap;
  private final FiniteAlphabet source;
  private final Alphabet target;

  public Alphabet getSourceAlphabet() {
    return source;
  }

  public Alphabet getTargetAlphabet() {
    return target;
  }

  public Symbol translate(final Symbol sym)
  throws IllegalSymbolException {
    Symbol s = (Symbol) transMap.get(sym);
    if(s == null) {
      if(sym instanceof AtomicSymbol) { //changed this from s to sym, since we already checked and s is null
        getSourceAlphabet().validate(sym);
        throw new IllegalSymbolException(
          "Unable to map " + sym.getName()
        );
      } else {
        if(sym == null) {
          throw new NullPointerException("Can't translate null");
        }
        Set syms = new HashSet();
        for(Iterator i = ((FiniteAlphabet) sym.getMatches()).iterator(); i.hasNext(); ) {
          Symbol is = (Symbol) i.next();
          syms.add(this.translate(is));
        }
        s = getTargetAlphabet().getAmbiguity(syms);
      }
    }
    return s;
  }

  /**
   * Alter the translation mapping.
   *
   * @param from source AtomicSymbol
   * @param to   target AtomicSymbol to be returned by translate(from)
   * @throws IllefalSymbolException if either from is not in the source
   *         alphabet or to is not in the target alphabet
   */
  public void setTranslation(AtomicSymbol from, AtomicSymbol to)
  throws IllegalSymbolException {
    source.validate(from);
    target.validate(to);
    transMap.put(from, to);
  }

  /**
   * Create a new translation table that will translate symbols from source to
   * target.
   * <P>
   * The source alphabet must be finite, as a Map object is used to associate
   * a source Symbol with a target Symbol.
   * The target alphabet need not be finite.
   *
   * @param source  the FiniteAlphabet to translate from
   * @param target  the Alphabet to translate into
   */
  public SimpleTranslationTable(FiniteAlphabet source, Alphabet target) {
    this.source = source;
    this.target = target;
    this.transMap = new HashMap();
  }

  /**
   * Create a new translation table that will translate symbols from source to
   * target.
   * <P>
   * The Map transMap should contain keys in the source alphabet with values in
   * the target alphabet. However, this is not currently checked.
   * <P>
   * The source alphabet must be finite, as a Map object is used to associate
   * a source Symbol with a target Symbol.
   * The target alphabet need not be finite.
   *
   * @param source  the FiniteAlphabet to translate from
   * @param target  the Alphabet to translate into
   */
  public SimpleTranslationTable(
    FiniteAlphabet source, Alphabet target, Map transMap
  ) {
    this.source = source;
    this.target = target;
    this.transMap = transMap;
  }
}
