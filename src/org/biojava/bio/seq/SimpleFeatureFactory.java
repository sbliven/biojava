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
 * A simple implementation of a feature factory for adding SimpleFeature objects
 * to a sequence.
 *
 * @author Matthew Pocock
 */
public class SimpleFeatureFactory implements FeatureFactory {
  /**
   * Creates a new feature for a sequence and adds it to a feature holder.
   * <P>
   * This implementation creates SimpleFeature objects, and adds them to a
   * sequence, assuming it to be a SimpleSequence.
   * <P>
   * I have a niggeling suspician that this interface should become part of
   * Sequence, but I am not yet sure of the implications of adding features of
   * multiple implementations to a sequence. This is all up for argument.
   *
   * @param loc    the Location of the feature
   * @param type   the type property
   * @param source the source property
   * @param seq    the Sequence within which the feature ultimately resides
   * @param fh     the feature holder that will directly contain this feature
   * @param annotation a hint as to the annotations that should be added
   * @return       the newly created and added Feature
   */
  public Feature createFeature(Location loc, String type, String source,
                               Sequence seq, FeatureHolder fh, Annotation annotation) {
    Feature f = new SimpleFeature(loc, type, source, seq, annotation);
    fh.addFeature(f);
    return f;
  }
}
