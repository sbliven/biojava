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
 * The interface for objects that contain features and can be modified.
 *
 * It is not required that both addFeature and removeFeature work.
 *
 * @author Matthew Pocock
 */
public interface MutableFeatureHolder extends FeatureHolder {
  /**
   * Add a feature to this holder.
   * <P>
   * Adding the same feature multiple times should only cause it to be contained
   * once. This is a 'loose' restriction - it may need enforcing for particular
   * situations and not for others.
   *
   * @param f the Feature to add
   * @throws UnsupportedOperationException if this feature holder is immutable
   * @throws IllegalArgumentException if this feature holder is mutable but
   *         can't contain the specific feature
   */
  void addFeature(Feature f)
  throws UnsupportedOperationException, IllegalArgumentException;
  
  /**
   * Remove a feature from this holder.
   *
   * @param f the feature to remove
   * @throws UnsupportedOperationException if this feature holder is immutable
   * @throws IllegalArgumentException if the feature is not contained within the
   *         feature holder
   */
  void removeFeature(Feature f)
  throws UnsupportedOperationException, IllegalArgumentException;
}
