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
 * The interface for objects that will manufacture sequences.
 * <P>
 * The factory layer is in here as sequences are potentialy heavy-weight, so we
 * want to decouple their possibly complicated creation from the code that wants
 * to make them.
 *
 * @author Matthew Pocock
 */
public interface SequenceFactory {
  /**
   * Creates a sequence using these parameters.
   * <P>
   * The annotation is under the same contract as in FeatureFactory.
   *
   * @param resList the ResidueList defining the 'sequence'
   * @param uri the uri
   * @param name   the name
   * @param annotation  a hint for the annotation of the resulting sequence
   * @return  a new Sequence object
   */
  Sequence createSequence(ResidueList resList,
                          String uri, String name, Annotation annotation);
}
