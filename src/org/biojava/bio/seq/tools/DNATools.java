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


package org.biojava.bio.seq.tools;

import java.util.*;

import org.biojava.bio.BioError;
import org.biojava.bio.seq.*;

/**
 * Usefull functionality for processing DNA sequences.
 *
 * @author Matthew Pocock
 */
public class DNATools {
  static private FiniteAlphabet alpha;
  static private FiniteAlphabet ambiguity;
  static private Residue a;
  static private Residue g;
  static private Residue c;
  static private Residue t;
  
  static private Map residueToMatches;
  static private Map residueToComplement;

  static {
    try {
      alpha = (FiniteAlphabet) AlphabetManager.instance().alphabetForName("DNA");
      ambiguity = (FiniteAlphabet) AlphabetManager.instance().alphabetForName("DNA-AMBIGUITY");
      ResidueList res = alpha.getParser("symbol").parse("agct");
      a = res.residueAt(1);
      g = res.residueAt(2);
      c = res.residueAt(3);
      t = res.residueAt(4);
      
      residueToMatches = new HashMap();
      residueToComplement = new HashMap();
      ResidueParser ambParser = ambiguity.getParser("symbol");
      // for 1.3
      /*
      residueToMatches.put(a, new HashableList(alpha,
                           Collections.singletonList(a)));
      residueToMatches.put(g, new HashableList(alpha, 
                           Collections.singletonList(g)));
      residueToMatches.put(c, new HashableList(alpha, 
                           Collections.singletonList(c)));
      residueToMatches.put(t, new HashableList(alpha, 
                           Collections.singletonList(t)));
      */
      // for 1.2
      HashableList hl;
      
      hl = new HashableList(alpha, new ArrayList(Collections.singleton(a)));
      residueToMatches.put(a, hl);
      residueToComplement.put(a, complementDNA(a));
      
      hl = new HashableList(alpha, new ArrayList(Collections.singleton(g)));
      residueToMatches.put(g, hl);
      residueToComplement.put(g, complementDNA(g));

      hl = new HashableList(alpha, new ArrayList(Collections.singleton(c)));
      residueToMatches.put(c, hl);
      residueToComplement.put(c, complementDNA(c));

      hl = new HashableList(alpha, new ArrayList(Collections.singleton(t)));
      residueToMatches.put(t, hl);
      residueToComplement.put(t, complementDNA(t));

      // add the gap residue
      hl = new HashableList(alpha, Collections.EMPTY_LIST);
      Residue gap = ambParser.parseToken("-");
      residueToMatches.put(gap, hl);
      residueToComplement.put(gap, gap);
      
      // add all other ambiguity residues
      Map matchesToResidue = new HashMap();
      for(Iterator i = ambiguity.iterator(); i.hasNext();) {
        Residue r = (Residue) i.next();
        if(!residueToMatches.keySet().contains(r)) {
          ResidueList rl = ambParser.parse(r.getName());
          hl = new HashableList(rl.alphabet(), rl.toList());
          residueToMatches.put(r, hl);
          matchesToResidue.put(hl, r);
        }
      }
      for(Iterator i = ambiguity.iterator(); i.hasNext();) {
        Residue r = (Residue) i.next();
        if(!residueToComplement.keySet().contains(r)) {
          hl = (HashableList) residueToMatches.get(r);
          residueToComplement.put(r,
                                (Residue) matchesToResidue.get(complement(hl)));
        }
      }
    } catch (Throwable t) {
      throw new BioError(t, "Unable to initialize DNATools");
    }
  }
  
  public static Residue a() { return a; }
  public static Residue g() { return g; }
  public static Residue c() { return c; }
  public static Residue t() { return t; }

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
   * Return a new DNA <span class="type">ResidueList</span> for
   * <span class="arg">dna</span>.
   *
   * @param dna a <span class="type">String</span> to parse into DNA
   * @return a <span class="type">ResidueList</span> created form
   *         <span class="arg">dna</span>
   * @throws IllegalResidueException if  <span class="arg">dna</span> contains
   *         any non-DNA characters
   */
  public static ResidueList createDNA(String dna)
  throws IllegalResidueException {
    try {
      ResidueParser p = getAlphabet().getParser("symbol");
      return p.parse(dna);
    } catch (SeqException se) {
      throw new BioError(se, "Something has gone badly wrong with DNA");
    }
  }
  
  /**
   * Return a new DNA-AMBIGUITY <span class="type">ResidueList</span> for
   * <span class="arg">amb</span>.
   *
   * @param amb a <span class="type">String</span> to parse into DNA-AMBIGUITY
   * @return a <span class="type">ResidueList</span> created form
   *         <span class="arg">amb</span>
   * @throws IllegalResidueException if  <span class="arg">amb</span> contains
   *         any non-DNA-AMBIGUITY characters
   */
  public static ResidueList createDNAAmbiguity(String amb)
  throws IllegalResidueException {
    try {
      ResidueParser p = getAmbiguity().getParser("symbol");
      return p.parse(amb);
    } catch (SeqException se) {
      throw new BioError(
        se,
        "Something has gone badly wrong in the DNA ambibuity alphabet"
      );
    }
  }
  
  /**
   * Return an integer index for a residue - compatible with forIndex.
   * <P>
   * The index for a residue is stable accross virtual machines & invokations.
   *
   * @param res  the Residue to index
   * @return     the index for that residue
   * @throws IllegalResidueException if res is not a member of the DNA alphabet
   */
  final public static int index(Residue res) throws IllegalResidueException {
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
    throw new IllegalResidueException("Realy confused. Can't find index for " +
                                      res.getName());
  }
  
  /**
   * Return the residue for an index - compatible with index.
   * <P>
   * The index for a residue is stable accross virtual machines & invokations.
   *
   * @param index  the index to look up
   * @return       the residue at that index
   * @throws IndexOutOfBoundsException if index is not between 0 and 3
   */
  final static public Residue forIndex(int index)
  throws IndexOutOfBoundsException {
    if(index == 0)
      return a;
    else if(index == 1)
      return g;
    else if(index == 2)
      return c;
    else if(index == 3)
      return t;
    else throw new IndexOutOfBoundsException("No residue for index " + index);
  }
  
  /**
   * Complement the residue.
   *
   * @param res  the residue to complement
   * @return a Residue that is the complement of res
   * @throws IllegalResidueException if res is not a member of the DNA alphabet
   */
  final static public Residue complementDNA(Residue res)
  throws IllegalResidueException {
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
    throw new BioError("Realy confused. Can't find residue " +
                       res.getName());
  }
  
  /**
   * Complement residues even if they are ambiguity codes.
   *
   * @param res  the residue to complement
   * @return a Residue that is the complement of res
   * @throws IllegalResidueException if res is not a member of the DNA ambiguity
   *         alphabet
   */
  final static public Residue complement(Residue res)
  throws IllegalResidueException {
    getAmbiguity().validate(res);
    Residue r = (Residue) residueToComplement.get(res);
    if(r == null) {
      throw new BioError("Realy confused. Can't find complement for " +
                          res.getName());
    }
    return r;
  }
  
  /**
   * Retrieve the residue for a symbol.
   *
   * @param symbol  the char to look up
   * @return        the residue for that char
   * @throws IllegalResidueException if the char does not belong to {a, g, c, t}
   */
  final static public Residue forSymbol(char symbol)
  throws IllegalResidueException {
    if(symbol == 'a') {
      return a;
    } else if(symbol == 'g') {
      return g;
    } else if(symbol == 'c') {
      return c;
    } else if(symbol == 't') {
      return t;
    }
    throw new IllegalResidueException("Unknown residue " + symbol);
  }
  
  /**
   * Convert an ambiguity code to a list of residues it could match.
   *
   * @param res the residue to expand
   * @return a ResidueList containing each matching DNA residue
   * @throws IllegalResidueException if res is not a member of the DNA ambiguity
   *         alphabet
   */
  final static public ResidueList forAmbiguity(Residue res)
  throws IllegalResidueException {
    ResidueList resList = (ResidueList) residueToMatches.get(res);
    if(resList != null)
      return resList;
    getAmbiguity().validate(res);
    throw new BioError("Residue not mapped to residue list: " +
                       res.getName());
  }
 
  /**
   * Helps build the complement infomation.
   */
  static private HashableList complement(HashableList list)
  throws IllegalResidueException {
    List newList = new ArrayList();
    for(Iterator i = list.iterator(); i.hasNext();) {
      newList.add(complementDNA((Residue) i.next()));
    }
    return new HashableList(list.alphabet(), newList);
  }
  
  /**
   * Helps for building the ambiguity->resList information.
   *
   * @author Matthew Pocock
   */
  private static class HashableList extends SimpleResidueList {
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
