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

/**
 * A simple implementation of an Alignment.
 */
public class SimpleAlignment extends AbstractResidueList implements Alignment {
  private Map labelToResidueList;
  private List labels;
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
  
  public List getLabels() {
    return labels;
  }
  
  public Residue residueAt(Object label, int column) {
    return residueListForLabel(label).residueAt(column);
  }
  
  public Alignment subAlignment(Set labels, Location loc)
  throws NoSuchElementException {
    Map labelsToResList = new HashMap();
    Iterator i;
    if(labels != null) {
      i = labels.iterator();
    } else {
      i = getLabels().iterator();
    }
    while(i.hasNext()) {
      Object label = i.next();
      ResidueList res = residueListForLabel(label);
      if(loc != null) {
        res = loc.residues(res);
      }
      labelsToResList.put(label, res);
    }
    return new SimpleAlignment(labelsToResList);
  }
  
  public ResidueList residueListForLabel(Object label)
  throws NoSuchElementException {
    ResidueList rl = (ResidueList) labelToResidueList.get(label);
    if(rl == null) {
      throw new NoSuchElementException("No residue list associated with label " + label);
    }
    return rl;
  }
  
  /**
   * Generate an alignment from a list of ResidueLists.
   * <P>
   * The ResidueLists must all be of the same length.
   *
   * @param labelToResList  the label-to-residue list mapping
   * @throws IllegalArgumentException if the ResidueLists are not the same
   *         length
   */
  public SimpleAlignment(Map labelToResList) throws IllegalArgumentException {
    this.labels = Collections.unmodifiableList(new ArrayList(labelToResList.keySet()));
    this.labelToResidueList = labelToResList;
    
    length = -1;
    List alphaList = new ArrayList();
    for(Iterator li = labels.iterator(); li.hasNext(); ) {
      Object label = li.next();
      try {
        ResidueList rl = residueListForLabel(label);
        alphaList.add(rl.alphabet());
        if(length == -1) {
          length = rl.length();
        } else {
          if(rl.length() != length) {
            StringBuffer sb = new StringBuffer();
            for(Iterator labI = labels.iterator(); labI.hasNext(); ) {
              Object lab = labI.next();
              sb.append("\n\t" + lab + " (" + residueListForLabel(lab).length() + ")");
            }
            throw new IllegalArgumentException(
              "All ResidueLists must be the same length: " + sb.toString()
            );
          }
        }
      } catch (NoSuchElementException nsee) {
        if(labelToResidueList.containsKey(label)) {
          throw new IllegalArgumentException(
            "The residue list associated with " + label + " is null"
          );
        } else {
          throw new BioError(nsee, "Something is screwey - map is lieing about key/values");
        }
      }
    }
    
    this.alphabet = new ValidatingAlphabet(alphaList);
  }
  
  private class ColAsResidue implements Alignment.Column {
    private int col;
    
    public ColAsResidue(int col) {
      this.col = col;
    }
    
    public List getResidues() {
      return new AbstractList() {
        public Object get(int indx) {
          return residueAt(getLabels().get(indx), col);
        }
        public int size() {
          return getLabels().size();
        }
      };
    }
      
    public String getName() {
      List labels = getLabels();
      StringBuffer sb = new StringBuffer("(");
      if(labels.size() != 0) {
        sb.append(residueAt(labels.get(0), col).getName());
        for(int i = 1; i < labels.size(); i++) {
          sb.append(" " + residueAt(labels.get(i), col).getName());
        }
      }
      sb.append(")");
      return sb.toString();
    }
      
    public char getSymbol() {
      return '?';
    }
    
    public List getLabels() {
      return SimpleAlignment.this.getLabels();
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
