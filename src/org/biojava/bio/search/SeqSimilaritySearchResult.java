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
 * @author Gerald Loeffler
 * @author Keith James
 */
public interface SeqSimilaritySearchResult
{
    /**
     * <code>getQueryID</code> returns an identifier for the query
     * sequence which was used to perform the search. This may be used
     * to locate the query <code>SymbolList</code> in the appropriate
     * <code>SequenceDB</code>.
     *
     * @return a <code>String</code> identifier.
     *
     * @see org.biojava.bio.symbol.SymbolList
     * @see org.biojava.bio.seq.db.SequenceDB
     */
    public String getQueryID();

    /**
     * <code>getDatabaseID</code> returns an identifier for the
     * sequence database against which the search was conducted. This
     * may be used to locate a <code>SequenceDB</code> in the
     * appropriate <code>SequenceDBInstallation</code>.
     *
     * @return a <code>String</code> identifier.
     *
     * @see org.biojava.bio.seq.db.SequenceDBInstallation
     */
    public String getDatabaseID();

    /**
     * Return the query sequence which was used to perform the search.
     *
     * @return the <code>SymbolList</code> object used to search the
     * <code>SequenceDB</code>. Never returns null.
     *
     * @deprecated use <code>getQueryID</code> to obtain a database
     * identifier which may then be used to locate the query
     * <code>SymbolList</code> in the appropriate
     * <code>SequenceDB</code>.
     */
    public SymbolList getQuerySequence();

    /**
     * Return the sequence database against which the search that
     * produced this search result was performed.
     *
     * @return the <code>SequenceDB</code> object against which the
     * search was carried out. Never returns null.
     *
     * @deprecated use <code>getDatabaseID</code> to obtain a database
     * identifier which may then be used to locate a
     * <code>SequenceDB</code> in the appropriate
     * <code>SequenceDBInstallation</code>.
     */
    public SequenceDB getSequenceDB();

    /**
     * Return the search parameters used in the search that produced
     * this search result.
     *
     * @return the (immutable) search parameter <code>Map</code>
     * object. May return null.
     */
    public Map getSearchParameters();

    /**
     * Return all hits in this sequence similarity search result. The
     * hits are sorted from best to worst.
     *
     * @return an (immutable) <code>List</code> of
     * <code>SeqSimilaritySearchHit</code> objects containing all hits
     * in the search result. Never returns null but may return an
     * empty list.
     */
    public List getHits();
}
