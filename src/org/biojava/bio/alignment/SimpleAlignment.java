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


package org.biojava.bio.alignment;

import java.util.*;

import org.biojava.bio.seq.*;

/**
 * A simple implementation of an Alignment.
 */
public class SimpleAlignment extends AbstractResidueList implements Alignment {
  private List resList;
  private Alphabet alphabet;
  private int length;
  

  public int length() {
    return length;
  }
  
  public Alphabet alphabet() {
    return alphabet;
  }
  
  public Residue residueAt(int col) {
    return new ColAsResidue(col);
  }
  
  public List getResidueLists() {
    return resList;
  }
  
  public Residue getResidue(ResidueList seq, int column) {
    return seq.residueAt(column);
  }
  
  public Alignment subAlignment(List residueLists, Location loc) {
    List ress = new ArrayList();
    for(Iterator i = residueLists.iterator(); i.hasNext(); ) {
      ResidueList res = (ResidueList) i.next();
      ress.add(loc.residues(res));
    }
    return new SimpleAlignment(ress);
  }
  
  /**
   * Generate an alignment from a list of ResidueLists.
   * <P>
   * The ResidueLists must all be of the same length.
   *
   * @param resLists  the things to put into the alignment
   * @throws IllegalArgumentException if the ResidueLists are not the same
   *         length
   */
  public SimpleAlignment(List resLists) throws IllegalArgumentException {
    this.resList = Collections.unmodifiableList(resLists);
    
    length = 0;
    List alphaList = new ArrayList();
    for(Iterator ri = resLists.iterator(); ri.hasNext(); ) {
      ResidueList rl = (ResidueList) ri.next();
      alphaList.add(rl.alphabet());
      if(length == -1) {
        length = rl.length();
      } else {
        if(rl.length() != length) {
          throw new IllegalArgumentException(
            "All ResidueLists must be the same length (" + length +
            "), not " + rl.length()
          );
        }
      }
    }
    
    this.alphabet = new ValidatingAlphabet(alphaList);
  }
  
  private class ColAsResidue implements CrossProductResidue {
    private int col;
    
    public ColAsResidue(int col) {
      this.col = col;
    }
    
    public List getResidues() {
      return new AbstractList() {
        public Object get(int indx) {
          return getResidue((ResidueList) resList.get(indx), col);
        }
        public int size() {
          return resList.size();
        }
      };
    }
      
    public String getName() {
      StringBuffer sb = new StringBuffer("(");
      if(resList.size() != 0) {
        sb.append(getResidue((ResidueList) resList.get(0), col).getName());
        for(int i = 1; i < resList.size(); i++) {
          sb.append(" " + getResidue((ResidueList) resList.get(i), col).getName());
        }
      }
      sb.append(")");
      return sb.toString();
    }
      
    public char getSymbol() {
      return '?';
    }
    
    public Annotation getAnnotation() {
      return Annotation.EMPTY_ANNOTATION;
    }
  }
  
  private class ValidatingAlphabet implements CrossProductAlphabet {
    private List alphabets;
    
    public ValidatingAlphabet(List alphabets) {
      this.alphabets = Collections.unmodifiableList(alphabets);
    }
    
    public boolean contains(Residue r) {
      if(!(r instanceof CrossProductAlphabet)) {
        return false;
      }
      CrossProductResidue cr = (CrossProductResidue) r;
      
      Iterator subResI = cr.getResidues().iterator();
      Iterator alphaI = alphabets.iterator();
      while(subResI.hasNext() && alphaI.hasNext() ) {
        Residue res = (Residue) subResI.next();
        Alphabet alpha = (Alphabet) alphaI.next();
        if(!alpha.contains(res)) {
          return false;
        }
      }
      if(alphaI.hasNext() || subResI.hasNext()) {
        return false;
      }
      return true;
    }
    
    public String getName() {
      return "?";
    }
    
    public ResidueParser getParser(String name) {
      throw new NoSuchElementException(
        "No parsers associated with this alphabet"
      );
    }
    
    public ResidueList residues() {
      return ResidueList.EMPTY_LIST;
    }
    
    public int size() {
      return -1;
    }
    
    public void validate(Residue r) throws IllegalResidueException {
      if(!(r instanceof CrossProductAlphabet)) {
        throw new IllegalResidueException(
          "Residue " + r + " is not a CrossProduct residue"
        );
      }
      CrossProductResidue cr = (CrossProductResidue) r;
      Iterator subResI = cr.getResidues().iterator();
      Iterator alphaI = alphabets.iterator();
      while( subResI.hasNext() && alphaI.hasNext() ) {
        Residue res = (Residue) subResI.next();
        Alphabet alpha = (Alphabet) alphaI.next();
        alpha.validate(res);
      }
      if( alphaI.hasNext() || subResI.hasNext() ) {
        throw new IllegalResidueException(
          "Residue has a different number of dimensions to this alphabet: " + cr
        );
      }
    }
    
    public List getAlphabets() {
      return alphabets;
    }
    
    public CrossProductResidue getResidue(List rl)
    throws IllegalAlphabetException {
      final List rl2 = Collections.unmodifiableList(rl);
      return new CrossProductResidue() {
        public List getResidues() {
          return rl2;
        }
        public String getName() {
          StringBuffer sb = new StringBuffer("(");
          if(rl2.size() != 0) {
            sb.append(((Residue) rl2.get(0)).getName());
            for(int i = 1; i < size(); i++) {
              sb.append(((Residue) rl2.get(i)).getName());
            }
          }
          sb.append(")");
          return sb.toString();
        }
      
        public char getSymbol() {
          return '?';
        }
        
        public Annotation getAnnotation() {
          return Annotation.EMPTY_ANNOTATION;
        }
      }; 
    }
    public Annotation getAnnotation() {
      return Annotation.EMPTY_ANNOTATION;
    }
  }
}
