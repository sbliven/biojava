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
import org.biojava.bio.symbol.*;

/**
 * Usefull functionality for processing DNA sequences.
 *
 * @author Matthew Pocock
 */
public final class RNATools {
  private static final ReversibleTranslationTable complementTable;
  private static final ReversibleTranslationTable transcriptionTable;
  static private final FiniteAlphabet rna;
  
  static private final Symbol a;
  static private final Symbol g;
  static private final Symbol c;
  static private final Symbol u;
  
  static private Map symbolToComplement;

  static {
    try {
      rna = (FiniteAlphabet) AlphabetManager.alphabetForName("RNA");
      SymbolList syms = rna.getParser("token").parse("agcu");
      a = syms.symbolAt(1);
      g = syms.symbolAt(2);
      c = syms.symbolAt(3);
      u = syms.symbolAt(4);
      
      symbolToComplement = new HashMap();

      // add the gap symbol
      Symbol gap = AlphabetManager.getGapSymbol();
      symbolToComplement.put(gap, gap);
      
      // add all other ambiguity symbols
      for(Iterator i = ((SimpleAlphabet) rna).ambiguities(); i.hasNext();) {
        Symbol as = (Symbol) i.next();
        List l = new ArrayList();
        FiniteAlphabet fa = (FiniteAlphabet) as.getMatches();
        for(Iterator j = fa.iterator(); j.hasNext(); ) {
          l.add(complement((Symbol) j.next()));
        }
        symbolToComplement.put(as, AlphabetManager.getAmbiguitySymbol(l));
      }
      complementTable = new RNAComplementTranslationTable();
      transcriptionTable = new TranscriptionTable();
    } catch (Throwable t) {
      throw new BioError(t, "Unable to initialize RNATools");
    }
  }
  
  public static Symbol a() { return a; }
  public static Symbol g() { return g; }
  public static Symbol c() { return c; }
  public static Symbol u() { return u; }

  /**
   * Return the RNA alphabet.
   *
   * @return a flyweight version of the RNA alphabet
   */
  public static FiniteAlphabet getRNA() {
    return rna;
  }

  /**
   * Return a new RNA <span class="type">SymbolList</span> for
   * <span class="arg">rna</span>.
   *
   * @param rna a <span class="type">String</span> to parse into RNA
   * @return a <span class="type">SymbolList</span> created form
   *         <span class="arg">rna</span>
   * @throws IllegalSymbolException if  <span class="arg">rna</span> contains
   *         any non-RNA characters
   */
  public static SymbolList createRNA(String rna)
  throws IllegalSymbolException {
    try {
      SymbolParser p = getRNA().getParser("token");
      return p.parse(rna);
    } catch (BioException se) {
      throw new BioError(se, "Something has gone badly wrong with RNA");
    }
  }
  
  /**
   * Return an integer index for a symbol - compatible with forIndex.
   * <P>
   * The index for a symbol is stable accross virtual machines & invokations.
   *
   * @param res  the Symbol to index
   * @return     the index for that symbol
   * @throws IllegalSymbolException if res is not a member of the DNA alphabet
   */
  public static int index(Symbol res) throws IllegalSymbolException {
    if(res == a) {
      return 0;
    } else if(res == g) {
      return 1;
    } else if(res == c) {
      return 2;
    } else if(res == u) {
      return 3;
    }
    getRNA().validate(res);
    throw new IllegalSymbolException("Realy confused. Can't find index for " +
                                      res.getName());
  }
  
  /**
   * Return the symbol for an index - compatible with index.
   * <P>
   * The index for a symbol is stable accross virtual machines & invokations.
   *
   * @param index  the index to look up
   * @return       the symbol at that index
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
      return u;
    else throw new IndexOutOfBoundsException("No symbol for index " + index);
  }
  
  /**
   * Complement the symbol.
   *
   * @param sym  the symbol to complement
   * @return a Symbol that is the complement of res
   * @throws IllegalSymbolException if res is not a member of the DNA alphabet
   */
  static public Symbol complement(Symbol sym)
  throws IllegalSymbolException {
    if(sym == a) {
      return u;
    } else if(sym == g) {
      return c;
    } else if(sym == c) {
      return g;
    } else if(sym == u) {
      return a;
    }
    Symbol s = (Symbol) symbolToComplement.get(sym);
    if(s != null) {
      return s;
    } else {
      getRNA().validate(sym);
      throw new BioError(
        "Realy confused. Can't find symbol " +
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
    } else if(token == 'u') {
      return u;
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
    return new TranslatedSymbolList(list, complementTable());
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
    return new TranslatedSymbolList(new ReverseSymbolList(list), complementTable());
  }
  
  /**
   * Transcribe DNA into RNA.
   *
   * @param list the SymbolList to transcribe
   * @return a SymbolList that is the transcribed view
   * @throws IllegalAlphabetException if the list is not DNA
   */
   public static SymbolList transcribe(SymbolList list)
   throws IllegalAlphabetException {
     return new TranslatedSymbolList(list, transcriptionTable());
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
   * Get a translation table for converting DNA to RNA.
   *
   * @since 1.1
   */
  public static ReversibleTranslationTable transcriptionTable() {
    return transcriptionTable;
  }
  
  /**
   * Sneaky class for complementing RNA bases.
   */

  private static class RNAComplementTranslationTable
  implements ReversibleTranslationTable {
    public Symbol translate(Symbol s) 
	  throws IllegalSymbolException {
	    return RNATools.complement(s);
	  }

	  public Symbol untranslate(Symbol s) 
	  throws IllegalSymbolException {
	    return RNATools.complement(s);
    }

	  public Alphabet getSourceAlphabet() {
	    return RNATools.getRNA();
	  }

	  public Alphabet getTargetAlphabet() {
	    return RNATools.getRNA();
	  }
  }
  
  /**
   * Sneaky class for converting DNA->RNA.
   */
   
  private static class TranscriptionTable
  implements ReversibleTranslationTable {
    public Symbol translate(Symbol s)
    throws IllegalSymbolException {
      if(s == DNATools.t()) {
        return u;
      }
      DNATools.getDNA().validate(s);
      return s;
    }
    
    public Symbol untranslate(Symbol s)
    throws IllegalSymbolException {
      if(s == u) {
        return DNATools.t();
      }
      rna.validate(s);
      return s;
    }
    
    public Alphabet getSourceAlphabet() {
      return DNATools.getDNA();
    }
    
    public Alphabet getTargetAlphabet() {
      return RNATools.getRNA();
    }
  }
}
