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

/**
 * A simple no-frills implementation of the Alphabet interface.
 *
 * @author Matthew Pocock
 */
public class SimpleAlphabet extends AbstractAlphabet {
  /**
   * The name of this alphabet.
   */
  private String name;
  
  /**
   * The annotation associated with this alphabet.
   */
  private Annotation annotation;
  
  /**
   * A set of all residues within the alphabet.
   */
  private Set residues;

  /**
   * Initialize the residues set.
   */
  {
    residues	= new HashSet();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
    
  public Annotation getAnnotation() {
    if(annotation == null)
      annotation = new SimpleAnnotation();
    return annotation;
  }

  public int size() {
    return residues.size();
  }
  
  public ResidueList residues() {
    return new SimpleResidueList(this, new ArrayList(residues));
  }

  public boolean contains(Residue r) {
    return residues.contains(r);
  }

  public void addResidue(Residue r)
         throws IllegalResidueException {
    if(r == null)
      throw new IllegalResidueException("You can not add null as a residue");
    residues.add(r);
  }
  
  public void removeResidue(Residue r)
  throws IllegalResidueException {
    if(r == null) {
      throw new IllegalResidueException("You can not add null as a residue");
    }
    residues.remove(r);
  }

  public void validate(Residue r) throws IllegalResidueException {
    if(!contains(r)) {
      if(r == null) {
        throw new IllegalResidueException("NULL is an illegal residue");
      } else {
        throw new IllegalResidueException("Residue " + r.getName() +
                                          " not found in alphabet " +
                                          getName());
      }
    }
  }
}
