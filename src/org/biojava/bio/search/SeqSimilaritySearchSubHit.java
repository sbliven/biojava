package org.biojava.bio.search;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.Alignment;

/**
 * objects of this type represent one particular sub-hit (one concrete sequence stretch within a sequence and
 * associated information) from a sequence similarity search hit.
 *
 * @author <A href="mailto:Gerald.Loeffler@vienna.at">Gerald Loeffler</A> for the 
 *         <A href="http://www.imp.univie.ac.at">IMP</A>
 */
public interface SeqSimilaritySearchSubHit extends Cloneable {
  /**
   * this object is used as the label for the query sequence in the alignment of the query sequence with this sub-hit
   * sequence
   */
  public static final String QUERY_LABEL = "Query";

  /**
   * return the sequence similarity search hit to which this sequence similarity search sub-hit belongs.
   * @return the SeqSimilaritySearchHit object of which this object is a part. Never returns null.
   */
  SeqSimilaritySearchHit getHit();
  
  /**
   * return the score of this sub-hit in the units defined by the search algorithm.
   * @return the score of this sub-hit. This is a mandatory piece of information and may hence not be NaN.
   */
  double getScore();

  /**
   * return the P-value of this sub-hit.
   * @return the P-value of this sub-hit. This is an optional (but desired) piece of information and implementations of
   *         this interface may return NaN if a P-value is not available for this hit.
   */
  double getPValue();
  
  /**
   * return the E-value of this sub-hit.
   * @return the E-value of this sub-hit. This is an optional (but desired) piece of information and implementations of
   *         this interface may return NaN if an E-value is not available for this hit.
   */
  double getEValue();
  
  /**
   * return an alignment of (possibly part of) the query sequence against (possibly part of) this hit sequence. In this
   * alignment, the query is identified by the label given by the static field QUERY_LABEL.
   * @return the alignment of the query sequence against this hit sequence. May return null.
   */
  Alignment getAlignment();

  Object clone();
}
