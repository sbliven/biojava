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
 * of interface SeqSimilaritySearchResult. Objects of this class are
 * immutable.
 *
 * @author Gerald Loeffler
 * @author Keith James
 */
public class SimpleSeqSimilaritySearchResult
    implements SeqSimilaritySearchResult, Cloneable
{
    private SymbolList querySequence;
    private SequenceDB sequenceDB;
    private Map        searchParameters;
    private List       hits;

    // Hashcode is cached after first calculation because the data on
    // which is is based do not change
    private int hc;
    private boolean hcCalc;

  /** 
   * Construct an immutable object by giving all its properties.
   * @param searcher the sequence similarity searcher that produced
   * this search result, which may not be null.
   * @param querySequence the query sequence that gave rise to this
   * search result, which may not be null.
   * @param sequenceDB the sequence database against which the search
   * was conducted, which may not be null.
   * @param searchParameters the search parameters used in the search
   * that produced this result, which may be null. If not null, the
   * getter for this property returns an unmodifiable view of this
   * object.
   * @param hits the list of SeqSimilaritySearchHit objects that make
   * up this result, which may not null. The getter for this property
   * returns an unmodifiable view of this object.
   */
    public SimpleSeqSimilaritySearchResult(SymbolList querySequence,
                                           SequenceDB sequenceDB,
                                           Map        searchParameters,
                                           List       hits)
    {
        if (querySequence == null)
        {
            throw new IllegalArgumentException("querySequence was null");
        }

        if (sequenceDB == null)
        {
            throw new IllegalArgumentException("sequenceDB was null");
        }

        if (searchParameters != null)
        {
            this.searchParameters =
                Collections.unmodifiableMap(searchParameters);
        }

        if (hits == null)
        {
            throw new IllegalArgumentException("hits was null");
        }

        this.querySequence = querySequence;
        this.sequenceDB    = sequenceDB;
        this.hits          = Collections.unmodifiableList(hits);

        hcCalc = false;
    }

    public SymbolList getQuerySequence()
    {
        return querySequence;
    }

    public SequenceDB getSequenceDB()
    {
        return sequenceDB;
    }

    /**
     * Return an unmodifiable view of the search parameters map.
     */
    public Map getSearchParameters()
    {
        return searchParameters;
    }

    /**
     * return an unmodifiable view of the hits list.
     */
    public List getHits()
    {
        return hits;
    }

    public String toString()
    {
        return "SimpleSeqSimilaritySearchResult of "
            + getQuerySequence()
            + " against "
            + getSequenceDB().getName();
    }
  
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;

        if (! o.getClass().equals(this.getClass())) return false;

        SimpleSeqSimilaritySearchResult that = (SimpleSeqSimilaritySearchResult) o;

        if (! ObjectUtil.equals(this.querySequence, that.querySequence))
            return false;
        if (! ObjectUtil.equals(this.sequenceDB, that.sequenceDB))
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
            hc = ObjectUtil.hashCode(hc, querySequence);
            hc = ObjectUtil.hashCode(hc, sequenceDB);
            hc = ObjectUtil.hashCode(hc, searchParameters);
            hc = ObjectUtil.hashCode(hc, hits);
            hcCalc = true;
        }

        return hc;
    }
  
    public Object clone()
    {
        return this;
    }
}
