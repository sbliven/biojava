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
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.symbol.*;

/**
 * Useful functionality for processing DNA sequences.
 *
 * @author Matthew Pocock
 * @author Keith James (docs)
 * @author Mark Schreiber
 */
public final class DNATools {
  private static final ReversibleTranslationTable complementTable;
  static private final FiniteAlphabet dna;
    private static final SymbolTokenization dnaTokens;

  static private final AtomicSymbol a;
  static private final AtomicSymbol g;
  static private final AtomicSymbol c;
  static private final AtomicSymbol t;
  static private final Symbol n;


  static private Map symbolToComplement;

  static {
    try {
      dna = (FiniteAlphabet) AlphabetManager.alphabetForName("DNA");
      dnaTokens = dna.getTokenization("token");
      SymbolList syms = new SimpleSymbolList(dnaTokens, "agctn");
      a = (AtomicSymbol) syms.symbolAt(1);
      g = (AtomicSymbol) syms.symbolAt(2);
      c = (AtomicSymbol) syms.symbolAt(3);
      t = (AtomicSymbol) syms.symbolAt(4);
      n = syms.symbolAt(5);

      symbolToComplement = new HashMap();

      // add the gap symbol
      Symbol gap = dna.getGapSymbol();
      symbolToComplement.put(gap, gap);

      // add all other ambiguity symbols
      for(Iterator i = AlphabetManager.getAllSymbols(dna).iterator(); i.hasNext();) {
          Symbol as = (Symbol) i.next();
          FiniteAlphabet matches = (FiniteAlphabet) as.getMatches();
          if (matches.size() > 1) {   // We've hit an ambiguous symbol.
              Set l = new HashSet();
              for(Iterator j = matches.iterator(); j.hasNext(); ) {
                  l.add(complement((Symbol) j.next()));
              }
              symbolToComplement.put(as, dna.getAmbiguity(l));
          }
      }


      complementTable = new DNAComplementTranslationTable();
    } catch (Throwable t) {
      throw new BioError(t, "Unable to initialize DNATools");
    }
  }

  public static AtomicSymbol a() { return a; }
  public static AtomicSymbol g() { return g; }
  public static AtomicSymbol c() { return c; }
  public static AtomicSymbol t() { return t; }
  public static Symbol n() { return n; }

  /**
   * Return the DNA alphabet.
   *
   * @return a flyweight version of the DNA alphabet
   */
  public static FiniteAlphabet getDNA() {
    return dna;
  }

  /**
   * Gets the (DNA x DNA x DNA) Alphabet
   * @return a flyweight version of the (DNA x DNA x DNA) alphabet
   */
  public static FiniteAlphabet getCodonAlphabet(){
    return (FiniteAlphabet)AlphabetManager.generateCrossProductAlphaFromName("(DNA x DNA x DNA)");
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
      SymbolTokenization p = getDNA().getTokenization("token");
      return new SimpleSymbolList(p, dna);
    } catch (BioException se) {
      throw new BioError(se, "Something has gone badly wrong with DNA");
    }
  }

  /**
   * Return a new DNA <span class="type">Sequence</span> for
   * <span class="arg">dna</span>.
   *
   * @param dna a <span class="type">String</span> to parse into DNA
   * @param name a <span class="type">String</span> to use as the name
   * @return a <span class="type">Sequence</span> created form
   *         <span class="arg">dna</span>
   * @throws IllegalSymbolException if <span class="arg">dna</span> contains
   *         any non-DNA characters
   */
  public static Sequence createDNASequence(String dna, String name)
  throws IllegalSymbolException {
    try {
      return new SimpleSequenceFactory().createSequence(
        createDNA(dna),
        "", name, new SimpleAnnotation()
      );
    } catch (BioException se) {
      throw new BioError(se, "Something has gone badly wrong with DNA");
    }
  }

  /**
   * Return an integer index for a symbol - compatible with
   * <code>forIndex</code>.
   *
   * <p>
   * The index for a symbol is stable accross virtual machines &
   * invocations.
   * </p>
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
   * <p>
   * The index for a symbol is stable accross virtual machines &
   * invocations.
   * </p>
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
   * @throws IllegalSymbolException if the char is not a valid IUB dna code
   */
  static public Symbol forSymbol(char token)
  throws IllegalSymbolException {
    String t = String.valueOf(token);
    SymbolTokenization toke;
    try{
      toke = getDNA().getTokenization("token");
    }catch(BioException e){
      throw new BioError(e, "Cannot find the 'token' Tokenization for DNA!?");
    }
    return toke.parseToken(t);
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
     * Get a single-character token for a DNA symbol
     *
     * @throws IllegalSymbolException if <code>sym</code> is not a member of the DNA alphabet
     */

    public static char dnaToken(Symbol sym)
        throws IllegalSymbolException
    {
        return dnaTokens.tokenizeSymbol(sym).charAt(0);
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

