package org.biojava.bio.search;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.BioException;

import java.util.*;

/**
 * Objects of this type represent one particular result of a sequence
 * similarity search.
 *
 * @author <a href="mailto:Gerald.Loeffler@vienna.at">Gerald
 * Loeffler</a> for the <a href="http://www.imp.univie.ac.at">IMP</a>
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 */
public interface SeqSimilaritySearchResult extends Cloneable
{

    /**
     * Return the sequence with which the search that produced this
     * search result was performed.
     * @return the SymbolList object used to search the
     * SequenceDB. Never returns null.
     */
    SymbolList getQuerySequence();

    /**
     * Return the sequence database against which the search that
     * produced this search result was performed.
     * @return the SequenceDB object against which the search was
     * carried out. Never returns null.
     */
    SequenceDB getSequenceDB();

    /**
     * Return the search parameters used in the search that produced
     * this search result.
     * @return the (immutable) search parameter Map object. May return
     * null.
     */
    Map getSearchParameters();

    /**
     * Return all hits in this sequence similarity search result. The
     * hits are sorted from best to worst.
     * @return an (immutable) List of SeqSimilaritySearchHit objects
     * containing all hits in the search result. Never returns null
     * but may return an empty list.
     */
    List getHits();

    Object clone();
}
