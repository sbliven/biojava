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
public class DNATools {
  static private FiniteAlphabet alpha;
  static private FiniteAlphabet ambiguity;
  static private Symbol a;
  static private Symbol g;
  static private Symbol c;
  static private Symbol t;
  
  static private Map symbolToMatches;
  static private Map symbolToComplement;

  static {
    try {
      alpha = (FiniteAlphabet) AlphabetManager.instance().alphabetForName("DNA");
      ambiguity = (FiniteAlphabet) AlphabetManager.instance().alphabetForName("DNA-AMBIGUITY");
      SymbolList res = alpha.getParser("token").parse("agct");
      a = res.symbolAt(1);
      g = res.symbolAt(2);
      c = res.symbolAt(3);
      t = res.symbolAt(4);
      
      symbolToMatches = new HashMap();
      symbolToComplement = new HashMap();
      SymbolParser ambParser = ambiguity.getParser("token");
      // for 1.3
      /*
      symbolToMatches.put(a, new HashableList(alpha,
                           Collections.singletonList(a)));
      symbolToMatches.put(g, new HashableList(alpha, 
                           Collections.singletonList(g)));
      symbolToMatches.put(c, new HashableList(alpha, 
                           Collections.singletonList(c)));
      symbolToMatches.put(t, new HashableList(alpha, 
                           Collections.singletonList(t)));
      */
      // for 1.2
      HashableList hl;
      
      hl = new HashableList(alpha, new ArrayList(Collections.singleton(a)));
      symbolToMatches.put(a, hl);
      symbolToComplement.put(a, complementDNA(a));
      
      hl = new HashableList(alpha, new ArrayList(Collections.singleton(g)));
      symbolToMatches.put(g, hl);
      symbolToComplement.put(g, complementDNA(g));

      hl = new HashableList(alpha, new ArrayList(Collections.singleton(c)));
      symbolToMatches.put(c, hl);
      symbolToComplement.put(c, complementDNA(c));

      hl = new HashableList(alpha, new ArrayList(Collections.singleton(t)));
      symbolToMatches.put(t, hl);
      symbolToComplement.put(t, complementDNA(t));

      // add the gap symbol
      hl = new HashableList(alpha, Collections.EMPTY_LIST);
      Symbol gap = ambParser.parseToken("-");
      symbolToMatches.put(gap, hl);
      symbolToComplement.put(gap, gap);
      
      // add all other ambiguity symbols
      Map matchesToSymbol = new HashMap();
      for(Iterator i = ambiguity.iterator(); i.hasNext();) {
        Symbol r = (Symbol) i.next();
        if(!symbolToMatches.keySet().contains(r)) {
          SymbolList rl = ambParser.parse(r.getName());
          hl = new HashableList(rl.alphabet(), rl.toList());
          symbolToMatches.put(r, hl);
          matchesToSymbol.put(hl, r);
        }
      }
      for(Iterator i = ambiguity.iterator(); i.hasNext();) {
        Symbol r = (Symbol) i.next();
        if(!symbolToComplement.keySet().contains(r)) {
          hl = (HashableList) symbolToMatches.get(r);
          symbolToComplement.put(r,
                                (Symbol) matchesToSymbol.get(complement(hl)));
        }
      }
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
  public static FiniteAlphabet getAlphabet() {
    return alpha;
  }

  /**
   * Return the ambiguity alphabet.
   *
   * @return a flyweight version of the DNA ambiguity alphabet
   */
  public static FiniteAlphabet getAmbiguity() {
    return ambiguity;
  }

  /**
   * Return a new DNA <span class="type">SymbolList</span> for
   * <span class="arg">dna</span>.
   *
   * @param dna a <span class="type">String</span> to parse into DNA
   * @return a <span class="type">SymbolList</span> created form
   *         <span class="arg">dna</span>
   * @throws IllegalSymbolException if  <span class="arg">dna</span> contains
   *         any non-DNA characters
   */
  public static SymbolList createDNA(String dna)
  throws IllegalSymbolException {
    try {
      SymbolParser p = getAlphabet().getParser("token");
      return p.parse(dna);
    } catch (BioException se) {
      throw new BioError(se, "Something has gone badly wrong with DNA");
    }
  }
  
  /**
   * Return a new DNA-AMBIGUITY <span class="type">SymbolList</span> for
   * <span class="arg">amb</span>.
   *
   * @param amb a <span class="type">String</span> to parse into DNA-AMBIGUITY
   * @return a <span class="type">SymbolList</span> created form
   *         <span class="arg">amb</span>
   * @throws IllegalSymbolException if  <span class="arg">amb</span> contains
   *         any non-DNA-AMBIGUITY characters
   */
  public static SymbolList createDNAAmbiguity(String amb)
  throws IllegalSymbolException {
    try {
      SymbolParser p = getAmbiguity().getParser("token");
      return p.parse(amb);
    } catch (BioException se) {
      throw new BioError(
        se,
        "Something has gone badly wrong in the DNA ambibuity alphabet"
      );
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
  final public static int index(Symbol res) throws IllegalSymbolException {
    if(res == a) {
      return 0;
    } else if(res == g) {
      return 1;
    } else if(res == c) {
      return 2;
    } else if(res == t) {
      return 3;
    }
    getAlphabet().validate(res);
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
  final static public Symbol forIndex(int index)
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
   * @param res  the symbol to complement
   * @return a Symbol that is the complement of res
   * @throws IllegalSymbolException if res is not a member of the DNA alphabet
   */
  final static public Symbol complementDNA(Symbol res)
  throws IllegalSymbolException {
    if(res == a) {
      return t;
    } else if(res == g) {
      return c;
    } else if(res == c) {
      return g;
    } else if(res == t) {
      return a;
    }
    getAlphabet().validate(res);
    throw new BioError("Realy confused. Can't find symbol " +
                       res.getName());
  }
  
  /**
   * Complement symbols even if they are ambiguity codes.
   *
   * @param res  the symbol to complement
   * @return a Symbol that is the complement of res
   * @throws IllegalSymbolException if res is not a member of the DNA ambiguity
   *         alphabet
   */
  final static public Symbol complement(Symbol res)
  throws IllegalSymbolException {
    getAmbiguity().validate(res);
    Symbol r = (Symbol) symbolToComplement.get(res);
    if(r == null) {
      throw new BioError("Realy confused. Can't find complement for " +
                          res.getName());
    }
    return r;
  }
  
  /**
   * Retrieve the symbol for a symbol.
   *
   * @param symbol  the char to look up
   * @return        the symbol for that char
   * @throws IllegalSymbolException if the char does not belong to {a, g, c, t}
   */
  final static public Symbol forSymbol(char symbol)
  throws IllegalSymbolException {
    if(symbol == 'a') {
      return a;
    } else if(symbol == 'g') {
      return g;
    } else if(symbol == 'c') {
      return c;
    } else if(symbol == 't') {
      return t;
    }
    throw new IllegalSymbolException("Unknown symbol " + symbol);
  }
  
  /**
   * Convert an ambiguity code to a list of symbols it could match.
   *
   * @param res the symbol to expand
   * @return a SymbolList containing each matching DNA symbol
   * @throws IllegalSymbolException if res is not a member of the DNA ambiguity
   *         alphabet
   */
  final static public SymbolList forAmbiguity(Symbol res)
  throws IllegalSymbolException {
    SymbolList resList = (SymbolList) symbolToMatches.get(res);
    if(resList != null)
      return resList;
    getAmbiguity().validate(res);
    throw new BioError("Symbol not mapped to symbol list: " +
                       res.getName());
  }
  
  /**
   * Retrieve a complement view of list.
   *
   * @param list  the ResidueList to complement
   * @return a SymbolList that is the complement
   * @throws IllegalAlphabetException if list is not a complementable alphabet
   */
  public static SymbolList complement(SymbolList list)
  throws IllegalAlphabetException {
    return new ComplementSymbolList(list);
  }
  
  /**
   * Helps build the complement infomation.
   */
  static private HashableList complement(HashableList list)
  throws IllegalSymbolException {
    List newList = new ArrayList();
    for(Iterator i = list.iterator(); i.hasNext();) {
      newList.add(complementDNA((Symbol) i.next()));
    }
    return new HashableList(list.alphabet(), newList);
  }
  
  /**
   * Helps for building the ambiguity->resList information.
   *
   * @author Matthew Pocock
   */
  private static class HashableList extends SimpleSymbolList {
    public int hashCode() {
      int hc = 0;
      for(Iterator i = iterator(); i.hasNext();) {
        hc = hc ^ i.next().hashCode();
      }
      return hc;
    }
    
    public HashableList(Alphabet alpha, List list) {
      super(alpha, list);
    }
    
    public boolean equals(Object o) {
      HashableList hl = (HashableList) o;
      return hashCode() == hl.hashCode();
    }
  }
}
