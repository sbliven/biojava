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

class InfiniteCrossProductAlphabet implements CrossProductAlphabet {
  private final List alphas;
  private char symbolSeed = 'A';

  InfiniteCrossProductAlphabet(List alphas) {
    this.alphas = alphas;
  }
  
  public String getName() {
    StringBuffer name = new StringBuffer("(");
    for (int i = 0; i < alphas.size(); ++i) {
	    Alphabet a = (Alphabet) alphas.get(i);
	    name.append(a.getName());
	    if (i < alphas.size() - 1) {
        name.append(" x ");
      }
    }
    name.append(")");
    return name.toString();
  }

  public boolean contains(Residue r) {
    if(! (r instanceof CrossProductResidue)) {
      return false;
    }
    
    CrossProductResidue cr = (CrossProductResidue) r;
    
    List rl = cr.getResidues();
    if(rl.size() != alphas.size()) {
      return false;
    }
    
    Iterator ai = alphas.iterator();
    Iterator ri = rl.iterator();
    
    while(ai.hasNext() && ri.hasNext()) {
      Alphabet aa = (Alphabet) ai.next();
      Residue rr = (Residue) ri.next();
      if(!aa.contains(rr)) {
        return false;
      }
    }
    
    return true;
  }
  
  public void validate(Residue r) throws IllegalResidueException {
    if(! (r instanceof CrossProductResidue)) {
	    throw new IllegalResidueException(
        "CrossProductAlphabet " + getName() + " does not accept " + r.getName() +
        " as it is not an instance of CrossProductResidue"
      );
    }
    
    CrossProductResidue cr = (CrossProductResidue) r;
    List rl = cr.getResidues();
    if(rl.size() != alphas.size()) {
      throw new IllegalResidueException(
        "CrossProductAlphabet " + getName() + " does not accept " + r.getName() +
        " as it is of a different order to this (" + alphas.size() + ":" + rl.size() +
        ")"
      );
    }
    
    Iterator ai = alphas.iterator();
    Iterator ri = rl.iterator();
    
    while(ai.hasNext() && ri.hasNext()) {
      Alphabet aa = (Alphabet) ai.next();
      Residue rr = (Residue) ri.next();
      if(!aa.contains(rr)) {
        throw new IllegalResidueException(
          "CrossProductAlphabet " + getName() + " does not accept " + r.getName() +
          " as residue " + rr.getName() + " is not a member of the alphabet " +
          aa.getName()
        );
      }
    }
  }

  public Annotation getAnnotation() {
    return Annotation.EMPTY_ANNOTATION;
  }

  public List getAlphabets() {
    return alphas;
  }
  
  public CrossProductResidue getResidue(List rList)
  throws IllegalAlphabetException {
    if(rList.size() != alphas.size()) {
      throw new IllegalAlphabetException(
        "List of residues is the wrong length (" + alphas.size() +
        ":" + rList.size() + ")"
      );
    }
    
    Iterator ai = alphas.iterator();
    Iterator ri = rList.iterator();
    
    while(ai.hasNext() && ri.hasNext()) {
      Alphabet aa = (Alphabet) ai.next();
      Residue rr = (Residue) ri.next();
      if(!aa.contains(rr)) {
        throw new IllegalAlphabetException(
          "CrossProductAlphabet " + getName() + " does not accept " + rList +
          " as residue " + rr.getName() + " is not a member of the alphabet " +
          aa.getName()
        );
      }
    }
    
    return new SimpleCrossProductResidue(rList, symbolSeed++);
  }

  public ResidueParser getParser(String name) throws NoSuchElementException {
    throw new NoSuchElementException("Currently no parsers are defined for SimpleCrossProductAlphabets");
  }
}
