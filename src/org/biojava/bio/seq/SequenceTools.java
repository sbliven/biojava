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

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.impl.*;

public final class SequenceTools {
  public static Sequence subSequence(Sequence seq, int start, int end)
  throws IndexOutOfBoundsException {
    return new SubSequence(seq, start, end);
  }
  
  public static Sequence subSequence(Sequence seq, int start, int end, String name)
  throws IndexOutOfBoundsException {
    return new SubSequence(seq, start, end, name);
  }
  
  public static Sequence reverseComplement(Sequence seq)
  throws IllegalAlphabetException {
    return new RevCompSequence(seq);
  }
  
  public static Sequence view(Sequence seq) {
    return new ViewSequence(seq);
  }
  
  public static Sequence view(Sequence seq, String name) {
    return new ViewSequence(seq, name);
  }
  
  public static void addAllFeatures(Sequence seq, FeatureHolder fh)
  throws
    ChangeVetoException,
    BioException
  {
    addFeatures(seq, fh);
  }
  
  private static void addFeatures(FeatureHolder toAddTo, FeatureHolder thingsToAdd)
  throws
    ChangeVetoException,
    BioException
  {
    for(Iterator i = thingsToAdd.features(); i.hasNext(); ) {
      Feature f2add = (Feature) i.next();
      Feature added = toAddTo.createFeature(f2add.makeTemplate());
      addFeatures(added, f2add);
    }
  }
}
