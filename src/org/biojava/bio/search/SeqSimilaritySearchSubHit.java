package org.biojava.bio.search;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.Alignment;

/**
 * Objects of this type represent one particular sub-hit (one concrete
 * sequence stretch within a sequence and associated information) from
 * a sequence similarity search hit.
 *
 * @author <a href="mailto:Gerald.Loeffler@vienna.at">Gerald
 * Loeffler</a> for the <a href="http://www.imp.univie.ac.at">IMP</a>
 */
public interface SeqSimilaritySearchSubHit extends Cloneable
{

    /**
     * This object is used as the label for the query sequence in the
     * alignment of the query sequence with this sub-hit sequence
     */
    public static final String QUERY_LABEL = "Query";
  
    /**
     * Return the score of this sub-hit in the units defined by the
     * search algorithm.
     * @return the score of this sub-hit. This is a mandatory piece of
     * information and may hence not be NaN.
     */
    double getScore();

    /**
     * Return the P-value of this sub-hit.
     * @return the P-value of this sub-hit. This is an optional (but
     * desired) piece of information and implementations of this
     * interface may return NaN if a P-value is not available for this
     * hit.
     */
    double getPValue();
  
    /**
     * Return the E-value of this sub-hit.
     * @return the E-value of this sub-hit. This is an optional (but
     * desired) piece of information and implementations of this
     * interface may return NaN if an E-value is not available for
     * this hit.
     */
    double getEValue();
  
    /**
     * Return an alignment of (possibly part of) the query sequence
     * against (possibly part of) this hit sequence. In this
     * alignment, the query is identified by the label given by the
     * static field QUERY_LABEL.
     * @return the alignment of the query sequence against this hit
     * sequence. May return null.
     */
    Alignment getAlignment();

    Object clone();
}
