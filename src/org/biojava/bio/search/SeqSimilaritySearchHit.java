package org.biojava.bio.search;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.Alignment;

import java.util.List;

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
    double getScore();

    /**
     * Return the overall P-value of this hit.
     * @return the overall P-value of this hit. This is an optional
     * (but desired) piece of information and implementations of this
     * interface may return NaN if a P-value is not available for this
     * hit.
     */
    double getPValue();
  
    /**
     * Return the overall E-value of this hit.
     * @return the overall E-value of this hit. This is an optional
     * (but desired) piece of information and implementations of this
     * interface may return NaN if an E-value is not available for
     * this hit.
     */
    double getEValue();

    /**
     * The sequence identifier of this hit within the sequence
     * database against which the search was performed.  Use
     * getSearchResult().getSequenceDB().getSequence(id) to actually
     * retrieve the sequence with this ID.
     * @return the (unique) sequence identifier for this hit, valid
     * within the sequence database against which this search was
     * performed. Never returns null.
     */
    String getSequenceID();
  
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
    List getSubHits();
}
