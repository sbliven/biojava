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
import java.lang.reflect.*;

/**
 * Cross product of a list of arbitrary alphabets.  This is the
 * most flexible implementation of CrossProductAlphabet, but it
 * is likely to be possible to produce more efficient implementations
 * for specific tasks.
 * 
 * @author Thomas Down
 */

public class SimpleCrossProductAlphabet implements CrossProductAlphabet {
  private List alphas;
  private HashMap ourResidues;
  private char symbolSeed = 'A';

  /**
   * Create a cross-product alphabet over the list of alphabets in 'a'.
   */
  public SimpleCrossProductAlphabet(List a) {
    alphas = Collections.unmodifiableList(a);
    ourResidues = new HashMap();
    populateResidues(new ArrayList());
  }

  private void populateResidues(List r) {
    if (r.size() == alphas.size()) {
	    putResidue(r);
    } else {
	    int indx = r.size();
	    Alphabet a = (Alphabet) alphas.get(indx);
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
    Residue rr = new SCPAResidue(l, symbolSeed++);
    // System.out.println(rr.getName());
    ourResidues.put(new ListWrapper(l), rr);
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
    throw new NoSuchElementException("Currently no parsers are defined for SimpleCrossProductAlphabets");
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

  private ListWrapper gopher = new ListWrapper();

  public CrossProductResidue getResidue(List l) throws IllegalAlphabetException {
    gopher.l = l;
    CrossProductResidue r = (CrossProductResidue) ourResidues.get(gopher);
    if (r == null) {
	    throw new IllegalAlphabetException();
    }
    return r;
  }

  /**
   * Simple wrapper to assist in list-comparisons.
   */

  private static class ListWrapper {
    List l;

    ListWrapper(List l) {
      this.l = l;
    }

    ListWrapper() {
    }

    public boolean equals(Object o) {
      if (! (o instanceof ListWrapper)) {
        return false;
      }
      List ol = ((ListWrapper) o).l;
      if (ol.size() != l.size()) {
        return false;
      }
      Iterator i1 = l.iterator();
      Iterator i2 = ol.iterator();
      while (i1.hasNext()) {
        if (i1.next() != i2.next()) {
          return false;
        }
      }
      return true;
    }

    public int hashCode() {
      int c = 0;
      for (Iterator i = l.iterator(); i.hasNext(); ) {
        c += i.next().hashCode();
      }
      return c;
    }
  }

  /**
   * Concrete implementation of CrossProductResidue, as returned
   * by a SimpleCrossProductAlphabet.
   */

  private class SCPAResidue implements CrossProductResidue {
    private List l;
    private char symbol;

    private SCPAResidue(List l, char symbol) {
      this.l = l;
      this.symbol = symbol;
    }

    public List getResidues() {
      return l;
    }

    public String getName() {
      StringBuffer name = new StringBuffer("(");
      for (int i = 0; i < l.size(); ++i) {
        Residue r = (Residue) l.get(i);
        name.append(r.getName());
        if (i < l.size() - 1) {
          name.append(" ");
        }
      }
      name.append(")");
      return name.toString();
    }

    public char getSymbol() {
      return symbol;
    }

    public Annotation getAnnotation() {
      return Annotation.EMPTY_ANNOTATION;
    }
  }
}
