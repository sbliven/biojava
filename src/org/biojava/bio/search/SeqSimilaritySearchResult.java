package org.biojava.bio.search;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.BioException;

import java.util.*;

/**
 * objects of this type represent one particular result of a sequence similarity search.
 *
 * @author <A href="mailto:Gerald.Loeffler@vienna.at">Gerald Loeffler</A> for the 
 *         <A href="http://www.imp.univie.ac.at">IMP</A>
 */
public interface SeqSimilaritySearchResult extends Cloneable {
  /**
   * return the sequence similarity searcher that produced this sequence similarity search result.
   * @return the SeqSimilaritySearcher object whose search() method produced this object. Never returns
   *         null.
   */
  SeqSimilaritySearcher getSearcher();

  /**
   * return the sequence with which the search that produced this search result was performed.
   * @return the SymbolList object passed to the searchers search() method that produced this object. Never
   *         returns null.
   */
  SymbolList getQuerySequence();

  /**
   * return the sequence database against which the search that produced this search result was performed.
   * @return the SequenceDB object passed to the searchers search() method that produced this object. Never returns
   * null.
   */
  SequenceDB getSequenceDB();

  /**
   * return the search parameters used in the search that produced this search result.
   * @return the (immutable) search parameter Map object passed to the searchers search() method that produced this
   *         object. May return null because SeqSimilaritySearcher.search() must accept a null value for
   *         searchParameters.
   */
  Map getSearchParameters();

  /**
   * return all hits in this sequence similarity search result. The hits are sorted from best to worst.
   * @return an (immutable) List of SeqSimilaritySearchHit objects containing all hits in the search result.
   *         Never returns null but may return an empty list.
   */
  List getHits();

  Object clone();
}
