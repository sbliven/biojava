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
import org.biojava.bio.seq.tools.*;

/**
 * A simple implementation of an Alignment.
 * <P>
 * This is a simple-stupid implementation that is made from a set of same-lengthed
 * ResidueList objects each with an associated label. It does not handle differently
 * lengthed sequences and doesn't contain any gap-editing concepts.
 *
 * @author Matthew Pocock
 */
public class SimpleAlignment extends AbstractResidueList implements Alignment {
  private Map labelToResidueList;
  private List labels;
  private CrossProductAlphabet alphabet;
  private int length;
  
  public int length() {
    return length;
  }
  
  public Alphabet alphabet() {
    return alphabet;
  }
  
  public Residue residueAt(int col) {
    try {
      return alphabet.getResidue(new ColAsList(col));
    } catch (IllegalAlphabetException iae) {
      throw new BioError(
        "Somehow my crossproduct alphabet is incompatible with column " + col
      );
    }
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
    
    int length = -1;
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
    
    this.alphabet = AlphabetManager.instance().getCrossProductAlphabet(alphaList);
    this.length = length;
  }
 
  /** 
   * Makes a column of the alignment behave like a list.
   *
   * @author Matthew Pocock
   */
  private final class ColAsList extends AbstractList {
    private final int col;
    
    public ColAsList(int col) {
      this.col = col;
    }
    
    public Object get(int indx) {
      return residueAt(labels.get(indx), col);
    }
    
    public int size() {
      return labels.size();
    }
  }    
}
