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

package org.biojava.bio.search;

import java.util.List;

import org.biojava.bio.seq.StrandedFeature.Strand;

/**
 * Objects of this type represent one particular hit (sequence and
 * associated information) from a sequence similarity search.
 *
 * @author <a href="mailto:Gerald.Loeffler@vienna.at">Gerald
 * Loeffler</a> for the <a href="http://www.imp.univie.ac.at">IMP</a>
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 */
public interface SeqSimilaritySearchHit
{
  /**
   * Return the overall score of this hit in the units defined by the
   * search algorithm.
   * @return the overall score of this hit. This is a mandatory piece
   * of information and may hence not be NaN.
   */
    public double getScore();

    /**
     * Return the overall P-value of this hit.
     * @return the overall P-value of this hit. This is an optional
     * (but desired) piece of information and implementations of this
     * interface may return NaN if a P-value is not available for this
     * hit.
     */
    public double getPValue();
  
    /**
     * Return the overall E-value of this hit.
     * @return the overall E-value of this hit. This is an optional
     * (but desired) piece of information and implementations of this
     * interface may return NaN if an E-value is not available for
     * this hit.
     */
    public double getEValue();

    /**
     * Return the start position of the first sub-hit in the query
     * sequence.
     *
     * @return an <code>int</code> value.
     */
    public int getQueryStart();

    /**
     * Return the end position of the last sub-hit in the query
     * sequence.
     *
     * @return an <code>int</code> value.
     */
    public int getQueryEnd();

    /**
     * Return the strand of the hit with respect to the query
     * sequence. If the sub-hits are not all on the same strand this
     * should return the unknown strand.
     *
     * @return a <code>Strand</code> value.
     */
    public Strand getQueryStrand();

    /**
     * Return the start position of the first sub-hit in the subject
     * sequence.
     *
     * @return an <code>int</code> value.
     */
    public int getSubjectStart();

    /**
     * Return the end position of the last sub-hit in the subject
     * sequence.
     *
     * @return an <code>int</code> value.
     */
    public int getSubjectEnd();

    /**
     * Return the strand of the sub-hit with respect to the subject
     * sequence. If the sub-hits are not all on the same strand this
     * should return the unknown strand.
     *
     * @return a <code>Strand</code> value.
     */
    public Strand getSubjectStrand();

    /**
     * The sequence identifier of this hit within the sequence
     * database against which the search was performed.
     * @return the (unique) sequence identifier for this hit, valid
     * within the sequence database against which this search was
     * performed. Never returns null.
     */
    public String getSequenceID();
  
    /**
     * Return all sub-hits for this sequence similarity search
     * hit. The sub-hits contain concrete alignments (and scores) for
     * sequence stretches from the sequence of this hit. The sub-hits
     * in the list returned by this method are sorted from best to
     * worst.
     * @return a List of SeqSimilaritySearchSubHit objects containing
     * all sub-hits for this hit.  Never returns null and the List is
     * guaranteed to contain at least 1 entry.
     */
    public List getSubHits();
}
