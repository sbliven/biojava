package org.biojava.bio.search;

import org.biojava.utils.contract.Contract;
import org.biojava.utils.ObjectUtil;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.Alignment;

import java.util.*;

/**
 * A simple implementation of interface SeqSimilaritySearchHit that
 * takes care of all the housekeeping. Objects of this class are
 * immutable.
 *
 * @author <a href="mailto:Gerald.Loeffler@vienna.at">Gerald
 * Loeffler</a> for the <a href="http://www.imp.univie.ac.at">IMP</a>
 *
 */
public class SimpleSeqSimilaritySearchHit
    implements SeqSimilaritySearchHit, Cloneable
{
    private SeqSimilaritySearchResult searchResult;
    private double                    score;
    private double                    pValue;
    private double                    eValue;
    private String                    sequenceID;
    private List                      subHits;

    /**
     * Construct an immutable object from the values of all
     * properties.
     * @param searchResult the search result of which this search hit
     * is a part. Not null.
     * @param score the overall score of this hit. This is a mandatory
     * piece of information and may hence not be NaN.
     * @param pValue the overall P-value of this hit. May be NaN.
     * @param eValue the overall E-value of this hit. May be NaN.
     * @param sequenceID the (unique) sequence identifier for this
     * hit, valid within the sequence database against which this
     * search was performed. Not null.
     * @param subHits a List of SeqSimilaritySearchSubHit objects
     * containing all sub-hits for this hit. Not null.
     */
    public SimpleSeqSimilaritySearchHit(SeqSimilaritySearchResult searchResult,
					double                    score,
					double                    pValue,
					double                    eValue,
					String                    sequenceID,
					List                      subHits)
    {
	Contract.pre(searchResult != null, "searchResult was null");
	Contract.pre(! Double.isNaN(score), "score was NaN");
	// pValue may be NaN
	// eValue may be NaN
	Contract.pre(sequenceID != null, "sequenceID was null");
	Contract.pre(subHits != null, "subHits was null");

	this.searchResult = searchResult;
	this.score        = score;
	this.pValue       = pValue;
	this.eValue       = eValue;
	this.sequenceID   = sequenceID;
	this.subHits      = subHits;
    }

    public double getScore()
    {
	return score;
    }

    public double getPValue()
    {
	return pValue;
    }
  
    public double getEValue()
    {
	return eValue;
    }

    public String getSequenceID()
    {
	return sequenceID;
    }
  
    /**
     * Return an unmodifiable view of the sub-hits list.
     */
    public List getSubHits()
    {
	return Collections.unmodifiableList(subHits);
    }

    public String toString()
    {
	return "SequenceDBSearchHit to " + getSequenceID()
	    + " with score " + getScore();
    }
  
    public boolean equals(Object o)
    {
	if (o == this) return true;
    
	// if this class is a direct sub-class of Object:
	if (o == null) return false;
	if (! o.getClass().equals(this.getClass())) return false;
    
	SimpleSeqSimilaritySearchHit that = (SimpleSeqSimilaritySearchHit) o;
    
	// only compare fields of this class (not of super-classes):
	if (! ObjectUtil.equals(this.searchResult, that.searchResult))
	    return false;
	if (! ObjectUtil.equals(this.score, that.score))
	    return false;
	if (! ObjectUtil.equals(this.pValue, that.pValue))
	    return false;
	if (! ObjectUtil.equals(this.eValue, that.eValue))
	    return false;
	if (! ObjectUtil.equals(this.sequenceID, that.sequenceID))
	    return false;
	if (! ObjectUtil.equals(this.subHits, that.subHits))
	    return false;
    
	// this and that are identical if we made it 'til here
	return true;
    }
  
    public int hashCode()
    {
	// if this class is a direct sub-class of Object:
	int hc = 0;

	// only take into account fields of this class (not of super-class):
	hc = ObjectUtil.hashCode(hc, searchResult);
	hc = ObjectUtil.hashCode(hc, score);
	hc = ObjectUtil.hashCode(hc, pValue);
	hc = ObjectUtil.hashCode(hc, eValue);
	hc = ObjectUtil.hashCode(hc, sequenceID);
	hc = ObjectUtil.hashCode(hc, subHits);

	return hc;
    }
  
    public Object clone()
    {
	// this is an immutable class so we can return ourselves
	return this;
    }
}
