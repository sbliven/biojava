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
 * A sequence.
 * <P>
 * This interface is a residue list, so it contains residues. It is annotatable
 * so that you can add annotation to it, and it is a FeatureHolder so that you
 * can add information to regions of it.
 * <P>
 * It is expected that there may be several implementations of this interface,
 * each of which may be failry heavy-weight. It takes the ResidueList interface
 * that is nice mathematicaly, and turns it into a biologicaly useful object.
 *
 * @author Matthew Pocock
 */
public interface Sequence extends ResidueList, FeatureHolder, Annotatable {
  /**
   * The URN for this sequence. This will be something like
   * <code>urn:sequence/embl:U32766</code> or
   * <code>urn:sequence/fasta:sequences.fasta|hox3</code>.
   *
   * @return the urn as a String
   */
  String getURN();
  
  /**
   * The name of this sequence.
   * <P>
   * The name may contain spaces or odd characters.
   *
   * @return the name as a String
   */
  String getName();
  
  /**
   * Creates a feature within fh that refers to the location loc.
   * <P>
   * There is no guarantee that the getAnnotation method of the resulting
   * feature will be equal to the annotation passed in. However, it is a hint to
   * the factory that it should contain all of those keys and values if
   * possible. However, some implementations may use this annotation object. Be
   * careful untill the standards are nailed down.
   * <P>
   * This method replaces the FeatureFactory interface so as to provide proper
   * encapsulation.
   *
   * @param loc the Location of the feature, relative to the parent sequence
   * @param type  the type of feature
   * @param source the source of the feature
   * @param fh  the feature holder that will directly contain the feature
   * @param annotation  any annotation to add to the feature
   * @return  the newly created and added Feature
   * @throws  UnsupportedOperationException if this Sequence is immutable
   */
  Feature createFeature(MutableFeatureHolder fh, Location loc,
                        String type, String source, Annotation annotation)
  throws UnsupportedOperationException;
}
