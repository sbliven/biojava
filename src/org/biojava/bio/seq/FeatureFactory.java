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

/**
 * Factory for creating sequences.
 * <P>
 * The responsibility of this interface is to create a new feature, but not to
 * wire it into the hierachy of feature objects. The factory method is passed
 * all the information necisary to set the feature properties.
 *
 * @author Matthew Pocock
 */
public interface FeatureFactory {
  /**
   * Creates a feature that refers to the location loc within seq.
   * <P>
   * There is no guarantee that the getAnnotation method of the resulting
   * feature will be equal to the annotation passed in. However, it is a hint to
   * the factory that it should contain all of those keys and values if
   * possible.
   *
   * @param seq the sequence within which the feature ultimately resides
   * @param loc the Location of the feature, relative to the parent sequence
   * @param type  the type of feature
   * @param source the source of the feature
   * @param annotation  any annotation to add to the feature
   * @return a new Feature, not yet wired into the hierachy
   */
  Feature createFeature(Sequence seq, Location loc,
                        String type, String source,
                        Annotation annotation);
}
