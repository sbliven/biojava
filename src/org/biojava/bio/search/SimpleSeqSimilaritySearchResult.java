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
import org.biojava.utils.ObjectUtil;

/**
 * A class that implements the trivial (housekeeping) responsibilities
 * of interface <code>SeqSimilaritySearchResult</code>. Objects of
 * this class are immutable.
 *
 * @author Gerald Loeffler
 * @author Keith James
 */
public class SimpleSeqSimilaritySearchResult
    implements SeqSimilaritySearchResult, Cloneable
{
    private String queryID;
    private String databaseID;
    private Map    searchParameters;
    private List   hits;

    // Hashcode is cached after first calculation because the data on
    // which is is based do not change
    private int hc;
    private boolean hcCalc;

    /** 
     * 
     * @param queryID the ID of the query sequence that gave rise to
     * this search result, which may not be null.
     * @param databaseID the ID of the sequence database against which
     * the search was conducted, which may not be null.
     * @param searchParameters the search parameters used in the search
     * that produced this result, which may be null. If not null, the
     * getter for this property returns an unmodifiable view of this
     * object.
     * @param hits the list of SeqSimilaritySearchHit objects that make
     * up this result, which may not null. The getter for this property
     * returns an unmodifiable view of this object.
     */
    public SimpleSeqSimilaritySearchResult(String queryID,
                                           String databaseID,
                                           Map    searchParameters,
                                           List   hits)
    {
        if (queryID == null)
        {
            throw new IllegalArgumentException("queryID was null");
        }

        if (databaseID == null)
        {
            throw new IllegalArgumentException("databaseID was null");
        }

        // searchParameters may be null
        if (hits == null)
        {
            throw new IllegalArgumentException("hits was null");
        }

        this.queryID          = queryID;
        this.databaseID       = databaseID;
        this.searchParameters = searchParameters;
        this.hits             = Collections.unmodifiableList(hits);

        hcCalc = false;
    }

    public String getQueryID()
    {
        return queryID;
    }

    public String getDatabaseID()
    {
        return databaseID;
    }

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
    public SymbolList getQuerySequence()
    {
        throw new UnsupportedOperationException();
    }

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
    public SequenceDB getSequenceDB()
    {
        throw new UnsupportedOperationException();
    }

    public Map getSearchParameters()
    {
        return (searchParameters == null ? null : Collections.unmodifiableMap(searchParameters));
    }

    public List getHits()
    {
        return Collections.unmodifiableList(hits);
    }
  
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;

        if (! o.getClass().equals(this.getClass())) return false;

        SimpleSeqSimilaritySearchResult that = (SimpleSeqSimilaritySearchResult) o;

        if (! ObjectUtil.equals(this.queryID, that.queryID))
            return false;
        if (! ObjectUtil.equals(this.databaseID, that.databaseID))
            return false;
        if (! ObjectUtil.equals(this.searchParameters, that.searchParameters))
            return false;
        if (! ObjectUtil.equals(this.hits, that.hits))
            return false;

        return true;
    }
  
    public int hashCode()
    {
        if (! hcCalc)
        {
            hc = ObjectUtil.hashCode(hc, queryID);
            hc = ObjectUtil.hashCode(hc, databaseID);
            hc = ObjectUtil.hashCode(hc, searchParameters);
            hc = ObjectUtil.hashCode(hc, hits);
            hcCalc = true;
        }

        return hc;
    }

    public String toString()
    {
        return "SimpleSeqSimilaritySearchResult of " + getQueryID() +
            " against " + getDatabaseID();
    }
  
    public Object clone()
    {
        return this;
    }
}
