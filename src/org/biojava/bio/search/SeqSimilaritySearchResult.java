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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.symbol.SymbolList;

/**
 * Objects of this type represent one particular result of a sequence
 * similarity search.
 *
 * @author <a href="mailto:Gerald.Loeffler@vienna.at">Gerald
 * Loeffler</a> for the <a href="http://www.imp.univie.ac.at">IMP</a>
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 */
public interface SeqSimilaritySearchResult
{

    /**
     * Return the query sequence which was used to perform the search.
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
}
