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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.io.*;
import java.lang.reflect.*;

import org.biojava.utils.*;

/**
 * An alphabet over a finite set of Symbols.
 * <P>
 * This interface makes the distinction between an alphabet over a finite (and
 * possibly small) number of symbols and an Alphabet over an infinite
 * (or extremely large) set of symbols. Within a FiniteAlphabet, the == operator
 * should be sufficient to decide upon equality for all AtomicSymbol instances.
 * <P>
 * The alphabet functions as the repository of objects in the fly-weight design
 * pattern. Only symbols within an alphabet should appear in object that claim
 * to use the alphabet - otherwise something is in error.
 *
 * @author Matthew Pocock
 */
public interface FiniteAlphabet extends Alphabet {
  
  /**
   * The number of symbols in the alphabet.
   *
   * @return the size of the alphabet
   */
  int size();
  
  /**
   * Retrieve an Iterator over the Symbols in this FiniteAlphabet.
   * <P>
   * Each AtomicSymbol as for which this.contains(as) is true will be returned
   * exactly once by this iterator in no specified order.
   *
   * @return an Iterator over the contained AtomicSymbol objects
   */
  Iterator iterator();
  
  /**
   * A list of symbols that make up this alphabet.
   * <P>
   * Subsequent calls to this method are not required to return either the same
   * symbol list, or even a symbol list with the symbols in the same order.
   *
   * @return  a SymbolList containing one AtomicSymbol for each AtomicSymbol in
   *          this alphabet
   */
  SymbolList symbols();
}
