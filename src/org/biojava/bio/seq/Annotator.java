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

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * Annotates a database of annotated sequences, returning all sequences
 * modified.
 * <P>
 * It is hoped that this interface would be implemented by searchers to mark up
 * domains in proteins, genes in genomes and all sorts of other things.
 *
 * @author Matthew Pocock
 */
public interface Annotator {
  /**
   * Loop over each sequence of sdb adding features.
   * <P>
   * Only the sequences implementing MutableFeatureHolder can be annotated.
   *
   * @param sdb the sequence database to loop over
   * @return a database of all modified sequences
   */
  public SequenceDB annotate(SequenceDB sdb)
  throws IllegalSymbolException, BioException;
  
  /**
   * Annotate this single sequence.
   * <P>
   * If it does not implement MutableFeatureHolder then it will not be
   * annotated.
   */
  public boolean annotate(Sequence seq)
  throws IllegalSymbolException, BioException;
}
