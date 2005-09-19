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

/*
 * Created on 03.08.2005
 *
 */
package org.biojava.bio.alignment;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.symbol.Alignment;

/** This Interface provides methods for the alignment of biosequences.
  *
  * @author Andreas Dr&auml;ger
  *
  */
public interface SequenceAlignment
{
  /**
    * @return a string representation of the alignment
    * @throws BioException
    */
  public String getAlignmentString() throws Exception;

  
  /**
    * @param source a SequenceIterator containing a set of sequences to be aligned with
    * @param subjectDB the SequenceDB containing another set of sequences.
    * @return a list containing the results of all single alignments performed by this
    *   method.
    * @throws NoSuchElementException
    * @throws Exception
    */
  public List alignAll(SequenceIterator source, SequenceDB subjectDB) throws Exception;
  
  
  /** Performs a pairwise sequence alignment of the two given sequences.
    * @param query
    * @param subject
    * @return score of the alignment or the distance.
    * @throws Exception
    */
  public double pairwiseAlignment(Sequence query, Sequence subject) throws Exception;
 
  
  /** This method also performs a sequence alignment of the two given sequences but it
    * returns an Alignment object instead of the score.
    * @param query
    * @param subject
    * @return Alignment
    */
  public Alignment getAlignment(Sequence query, Sequence subject) throws Exception;
  
}
