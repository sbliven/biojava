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
import org.biojava.bio.BioError;

/**
 * Allows you to get efficient implementations of CrossProductAlphabets.
 * <P>
 * This class frees you from having to know all the ickieness that goes on
 * to make infinite cross product alphabets efficient, while making finite
 * cross product alphabets obey the residue identity constraints.
 *
 * @author Matthew Pocock
 */
public class CrossProductAlphabetFactory {
  private static Map cache;
  
  /**
   * Retrieve a CrossProductAlphabet instance over the alphabets in aList.
   * <P>
   * If all of the alphabets in aList implements FiniteAlphabet then the
   * method will return a FiniteAlphabet. Otherwise, it returns a non-finite
   * alphabet.
   * <P>
   * If you call this method twice with a list containing the same alphabets,
   * it will return the same alphabet. This promotes the re-use of alphabets
   * and helps to maintain the 'flyweight' principal for finite alphabet
   * residues.
   *
   * @param aList a list of Alphabet objects
   * @return a CrossProductAlphabet that is over the alphabets in aList
   */
  public static CrossProductAlphabet createAlphabet(List aList) {
    if(cache == null) {
      cache = new HashMap();
    }

    ListWrapper aw = new ListWrapper(aList);
        
    CrossProductAlphabet cpa = (CrossProductAlphabet) cache.get(aw);
    
    if(cpa == null) {
      for(Iterator i = aList.iterator(); i.hasNext(); ) {
        Alphabet aa = (Alphabet) i.next();
        if(! (aa instanceof FiniteAlphabet) ) {
          cpa =  new InfiniteCrossProductAlphabet(aList);
        }
      }
    }
    
    if(cpa == null) {
      try {
        cpa = new SimpleCrossProductAlphabet(aList);
      } catch (IllegalAlphabetException iae) {
        throw new BioError(
          "Could not create SimpleCrossProductAlphabet for " + aList +
          " even though we should be able to. No idea what is wrong."
        );
      }
    }
    
    cache.put(aw, cpa);
    return cpa;
  }
  
  /**
   * Simple wrapper to assist in list-comparisons.
   *
   * @author Thomas Down
   */

  public static class ListWrapper {
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
}
