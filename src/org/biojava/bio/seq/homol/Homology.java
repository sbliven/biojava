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
package org.biojava.bio.seq.homol;
 
import java.io.*;
import java.lang.reflect.*;

import org.biojava.bio.symbol.*;
import org.biojava.utils.*;
import org.biojava.bio.seq.*;

/**
 * Signifies that two or more features are homologous.
 * <P>
 * Blast hits or local multiple-sequence alignments can be represented as a set
 * of features on sequences that have an alignment. The features will probably
 * implement HomologyFeature.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public interface Homology {
  /**
   * Retrieve the set of features that mark homologous regions.
   *
   * @return the FeatureHolder containing each homologous region
   */
  FeatureHolder getFeatures();
  /**
   * Retrieve the Alignment that specifies how the homologous regions are
   * aligned. The labels of the alignment are the HomologyFeature objects.
   *
   * @return the Alignment between the HomologyFeatures
   */
  Alignment getAlignment();
}
