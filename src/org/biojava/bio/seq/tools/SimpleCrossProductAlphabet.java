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
import java.lang.reflect.*;

import org.biojava.bio.seq.*;

/**
 * Cross product of a list of arbitrary alphabets.  This is the
 * most flexible implementation of CrossProductAlphabet, but it
 * is likely to be possible to produce more efficient implementations
 * for specific tasks.
 * 
 * @author Thomas Down
 * @author Matthew Pocock
 */

class SimpleCrossProductAlphabet implements FiniteAlphabet, CrossProductAlphabet {
  private final List alphas;
  private final HashMap ourResidues;
  private char symbolSeed = 'A';

  /**
   * Create a cross-product alphabet over the list of alphabets in 'a'.
   */
  public SimpleCrossProductAlphabet(List a)
  throws IllegalAlphabetException {
    for(Iterator i = a.iterator(); i.hasNext(); ) {
      Alphabet aa = (Alphabet) i.next();
      if(! (aa instanceof FiniteAlphabet) ) {
        throw new IllegalAlphabetException(
          "Can't create a SimpleAlphabetManager over non-fininte alphabet " +
          aa.getName() + " of type " + aa.getClass()
        );
      }
    }
    alphas = Collections.unmodifiableList(a);
    ourResidues = new HashMap();
    populateResidues(new ArrayList());
  }

  private void populateResidues(List r) {
    if (r.size() == alphas.size()) {
	    putResidue(r);
    } else {
	    int indx = r.size();
	    FiniteAlphabet a = (FiniteAlphabet) alphas.get(indx);
	    Iterator i = a.residues().iterator();
	    r.add(i.next());
	    populateResidues(r);
	    while (i.hasNext()) {
        r.set(indx, i.next());
        populateResidues(r);
	    }
	    r.remove(indx);
    }
  }

  private void putResidue(List r) {
    List l = Collections.unmodifiableList(new ArrayList(r));
    Residue rr = new SimpleCrossProductResidue(l, symbolSeed++);
    // System.out.println(rr.getName());
    ourResidues.put(new AlphabetManager.ListWrapper(l), rr);
  }

  public boolean contains(Residue r) {
    return ourResidues.values().contains(r);
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

  public ResidueParser getParser(String name) throws NoSuchElementException {
    throw new NoSuchElementException("Currently no parsers are defined for SimpleAlphabetManagers");
  }

  public ResidueList residues() {
    return new SimpleResidueList(this, new ArrayList(ourResidues.values()));
  }

  public int size() {
    return ourResidues.size();
  }

  public void validate(Residue r) throws IllegalResidueException {
    if (!contains(r)) {
	    throw new IllegalResidueException("Alphabet " + getName() + " does not accept " + r.getName());
    }
  }

  public Annotation getAnnotation() {
    return Annotation.EMPTY_ANNOTATION;
  }

  public List getAlphabets() {
    return alphas;
  }

  private AlphabetManager.ListWrapper gopher =
    new AlphabetManager.ListWrapper();

  public CrossProductResidue getResidue(List l) throws IllegalAlphabetException {
    gopher.l = l;
    CrossProductResidue r = (CrossProductResidue) ourResidues.get(gopher);
    if (r == null) {
      throw new IllegalAlphabetException(
        "Unable to find CrossProduct residue for " +
        l + " in alphabet " + getName()
      );
    }
    return r;
  }
}
