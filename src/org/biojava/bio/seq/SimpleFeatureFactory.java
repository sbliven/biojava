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
 * A simple implementation of a feature factory for creating SimpleFeature
 * objects.
 *
 * @author Matthew Pocock
 */
public class SimpleFeatureFactory implements FeatureFactory {
  /**
   * Creates a new feature for a sequence.
   * <P>
   * This implementation creates SimpleFeature objects within a SimpleSequence.
   * <P>
   *
   * @param seq    the Sequence within which the feature ultimately resides
   * @param loc    the Location of the feature
   * @param type   the type property
   * @param source the source property
   * @param annotation a hint as to the annotations that should be added
   * @return       the newly created and added Feature
   */
  public Feature createFeature(Sequence seq, Location loc,
                               String type, String source,
                               Annotation annotation) {
    Feature f = new SimpleFeature(seq, loc, type, source, annotation);
    return f;
  }
}
