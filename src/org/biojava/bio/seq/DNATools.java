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

package org.biojava.bio.seq;

import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;

/**
 * Useful functionality for processing DNA sequences.
 *
 * @author Matthew Pocock
 * @author Keith James (docs)
 */
public final class DNATools {
  private static final ReversibleTranslationTable complementTable;
  static private final FiniteAlphabet dna;
  
  static private final Symbol a;
  static private final Symbol g;
  static private final Symbol c;
  static private final Symbol t;
    
  static private Map symbolToComplement;

  static {
    try {
      dna = (FiniteAlphabet) AlphabetManager.alphabetForName("DNA");
      SymbolList syms = dna.getParser("token").parse("agct");
      a = syms.symbolAt(1);
      g = syms.symbolAt(2);
      c = syms.symbolAt(3);
      t = syms.symbolAt(4);
      
      symbolToComplement = new HashMap();

      // add the gap symbol
      Symbol gap = dna.getGapSymbol();
      symbolToComplement.put(gap, gap);
      
      // add all other ambiguity symbols
      for(Iterator i = ((SimpleAlphabet) dna).ambiguities(); i.hasNext();) {
        Symbol as = (Symbol) i.next();
        Set l = new HashSet();
        FiniteAlphabet fa = (FiniteAlphabet) as.getMatches();
        for(Iterator j = fa.iterator(); j.hasNext(); ) {
          l.add(complement((Symbol) j.next()));
        }
        symbolToComplement.put(as, dna.getAmbiguity(l));
      }
      complementTable = new DNAComplementTranslationTable();
    } catch (Throwable t) {
      throw new BioError(t, "Unable to initialize DNATools");
    }
  }
  
  public static Symbol a() { return a; }
  public static Symbol g() { return g; }
  public static Symbol c() { return c; }
  public static Symbol t() { return t; }

  /**
   * Return the DNA alphabet.
   *
   * @return a flyweight version of the DNA alphabet
   */
  public static FiniteAlphabet getDNA() {
    return dna;
  }

  /**
   * Return a new DNA <span class="type">SymbolList</span> for
   * <span class="arg">dna</span>.
   *
   * @param dna a <span class="type">String</span> to parse into DNA
   * @return a <span class="type">SymbolList</span> created form
   *         <span class="arg">dna</span>
   * @throws IllegalSymbolException if <span class="arg">dna</span> contains
   *         any non-DNA characters
   */
  public static SymbolList createDNA(String dna)
  throws IllegalSymbolException {
    try {
      SymbolParser p = getDNA().getParser("token");
      return p.parse(dna);
    } catch (BioException se) {
      throw new BioError(se, "Something has gone badly wrong with DNA");
    }
  }
  
  /**
   * Return an integer index for a symbol - compatible with
   * <code>forIndex</code>.
   *
   * <p>The index for a symbol is stable accross virtual machines &
   * invocations.</p>
   *
   * @param sym  the Symbol to index
   * @return the index for that symbol
   *
   * @throws IllegalSymbolException if sym is not a member of the DNA
   * alphabet
   */
  public static int index(Symbol sym) throws IllegalSymbolException {
    if(sym == a) {
      return 0;
    } else if(sym == g) {
      return 1;
    } else if(sym == c) {
      return 2;
    } else if(sym == t) {
      return 3;
    }
    getDNA().validate(sym);
    throw new IllegalSymbolException("Really confused. Can't find index for " +
                                      sym.getName());
  }
  
  /**
   * Return the symbol for an index - compatible with <code>index</code>.
   *
   * <p>The index for a symbol is stable accross virtual machines &
   * invocations.</p>
   *
   * @param index  the index to look up
   * @return       the symbol at that index
   *
   * @throws IndexOutOfBoundsException if index is not between 0 and 3
   */
  static public Symbol forIndex(int index)
  throws IndexOutOfBoundsException {
    if(index == 0)
      return a;
    else if(index == 1)
      return g;
    else if(index == 2)
      return c;
    else if(index == 3)
      return t;
    else throw new IndexOutOfBoundsException("No symbol for index " + index);
  }
  
  /**
   * Complement the symbol.
   *
   * @param sym  the symbol to complement
   * @return a Symbol that is the complement of sym
   * @throws IllegalSymbolException if sym is not a member of the DNA alphabet
   */
  static public Symbol complement(Symbol sym)
  throws IllegalSymbolException {
    if(sym == a) {
      return t;
    } else if(sym == g) {
      return c;
    } else if(sym == c) {
      return g;
    } else if(sym == t) {
      return a;
    }
    Symbol s = (Symbol) symbolToComplement.get(sym);
    if(s != null) {
      return s;
    } else {
      getDNA().validate(sym);
      throw new BioError(
        "Really confused. Can't find symbol " +
        sym.getName()
      );
    }
  }
  
  /**
   * Retrieve the symbol for a symbol.
   *
   * @param token  the char to look up
   * @return  the symbol for that char
   * @throws IllegalSymbolException if the char does not belong to {a, g, c, t}
   */
  static public Symbol forSymbol(char token)
  throws IllegalSymbolException {
    if(token == 'a') {
      return a;
    } else if(token == 'g') {
      return g;
    } else if(token == 'c') {
      return c;
    } else if(token == 't') {
      return t;
    }
    throw new IllegalSymbolException("Unable to find symbol for token " + token);
  }
  
  /**
   * Retrieve a complement view of list.
   *
   * @param list  the SymbolList to complement
   * @return a SymbolList that is the complement
   * @throws IllegalAlphabetException if list is not a complementable alphabet
   */
  public static SymbolList complement(SymbolList list)
  throws IllegalAlphabetException {
    return SymbolListViews.translate(list, complementTable());
  }

  /**
   * Retrieve a reverse-complement view of list.
   *
   * @param list  the SymbolList to complement
   * @return a SymbolList that is the complement
   * @throws IllegalAlphabetException if list is not a complementable alphabet
   */
  public static SymbolList reverseComplement(SymbolList list)
  throws IllegalAlphabetException {
    return SymbolListViews.translate(SymbolListViews.reverse(list), complementTable());
  }
  
  /**
   * Get a translation table for complementing DNA symbols.
   *
   * @since 1.1
   */

  public static ReversibleTranslationTable complementTable() {
    return complementTable;
  }
    
  /**
   * Sneaky class for complementing DNA bases.
   */

  private static class DNAComplementTranslationTable
  implements ReversibleTranslationTable {
    public Symbol translate(Symbol s) 
	  throws IllegalSymbolException {
	    return DNATools.complement(s);
	  }

    public Symbol untranslate(Symbol s) 
	  throws IllegalSymbolException	{
	    return DNATools.complement(s);
	  }

	  public Alphabet getSourceAlphabet() {
	    return DNATools.getDNA();
	  }

	  public Alphabet getTargetAlphabet() {
	    return DNATools.getDNA();
	  }
  }
}
