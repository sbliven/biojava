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
 * This is important, as some implementations of sequence may only allow certain
 * features to be created, or you may wish to generate java features on an ACeDB
 * object, or some other horrible thing. This is part of the machinery to
 * support layerd and distributed annotations.
 *
 * @author Matthew Pocock
 */
public interface FeatureFactory {
  /**
   * Creates a feature within fh that refers to the location loc within seq.
   * <P>
   * There is no guarantee that the getAnnotation method of the resulting
   * feature will be equal to the annotation passed in. However, it is a hint to
   * the factory that it should contain all of those keys and values if
   * possible.
   *
   * @param loc the Location of the feature, relative to the parent sequence
   * @param type  the type of feature
   * @param source the source of the feature
   * @param seq the sequence within which the feature ultimately resides
   * @param fh  the feature holder that directly contains the feature
   * @param annotation  any annotation to add to the feature
   */
  Feature createFeature(Location loc, String type, String source,
                        Sequence seq, FeatureHolder fh, Annotation annotation);
}
