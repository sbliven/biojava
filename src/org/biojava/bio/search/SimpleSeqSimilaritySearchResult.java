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
 * @author <a href="mailto:Gerald.Loeffler@vienna.at">Gerald
 * Loeffler</a> for the <a href="http://www.imp.univie.ac.at">IMP</a>
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 */
public class SimpleSeqSimilaritySearchResult
    implements SeqSimilaritySearchResult, Cloneable
{
    private SymbolList querySequence;
    private SequenceDB sequenceDB;
    private Map        searchParameters;
    private List       hits;

  /** 
   * Construct an immutable object by giving all its properties.
   * @param searcher the sequence similarity searcher that produced
   * this search result, which may not be null.
   * @param querySequence the query sequence that gave rise to this
   * search result, which may not be null.
   * @param sequenceDB the sequence database against the search that
   * produced this results was done, which may not be null.
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
	if (querySequence == null) {
	    throw new IllegalArgumentException("querySequence was null");
	}
	if (sequenceDB == null) {
	    throw new IllegalArgumentException("sequenceDB was null");
	}
	// searchParameters may be null
	if (hits == null) {
	    throw new IllegalArgumentException("hits was null");
	}

	this.querySequence    = querySequence;
	this.sequenceDB       = sequenceDB;
	this.searchParameters = searchParameters;
	this.hits             = hits;
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
	return (searchParameters == null ? null : Collections.unmodifiableMap(searchParameters));
    }

    /**
     * return an unmodifiable view of the hits list.
     */
    public List getHits()
    {
	return Collections.unmodifiableList(hits);
    }

    public String toString()
    {
	return "SimpleSeqSimilaritySearchResult of " + getQuerySequence() + " against " + getSequenceDB();
    }
  
    public boolean equals(Object o)
    {
	if (o == this) return true;
    
	// if this class is a direct sub-class of Object:
	if (o == null) return false;
	if (! o.getClass().equals(this.getClass())) return false;
    
	SimpleSeqSimilaritySearchResult that = (SimpleSeqSimilaritySearchResult) o;
    
	// only compare fields of this class (not of super-classes):
	if (! ObjectUtil.equals(this.querySequence, that.querySequence))
	    return false;
	if (! ObjectUtil.equals(this.sequenceDB, that.sequenceDB))
	    return false;
	if (! ObjectUtil.equals(this.searchParameters, that.searchParameters))
	    return false;
	if (! ObjectUtil.equals(this.hits, that.hits))
	    return false;
    
	// this and that are identical if we made it 'til here
	return true;
    }
  
    public int hashCode()
    {
	// if this class is a direct sub-class of Object:
	int hc = 0;

	// only take into account fields of this class (not of super-class):
	hc = ObjectUtil.hashCode(hc, querySequence);
	hc = ObjectUtil.hashCode(hc, sequenceDB);
	hc = ObjectUtil.hashCode(hc, searchParameters);
	hc = ObjectUtil.hashCode(hc, hits);

	return hc;
    }
  
  public Object clone()
    {
	// this is an immutable class so we can return ourselves
	return this;
    }
}
