package org.biojava.bio.search;

import org.biojava.utils.contract.Contract;
import org.biojava.utils.ObjectUtil;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.Alignment;

/**
 * A simple implementation of interface SeqSimilaritySearchSubHit that
 * takes care of all the house-keeping. Objects of this class are
 * immutable.
 * 
 * @author <a href="mailto:Gerald.Loeffler@vienna.at">Gerald
 * Loeffler</a> for the <a href="http://www.imp.univie.ac.at">IMP</a>
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 */
public class SimpleSeqSimilaritySearchSubHit
    implements SeqSimilaritySearchSubHit, Cloneable
{
    private SeqSimilaritySearchHit hit;
    private double                 score;
    private double                 pValue;
    private double                 eValue;
    private Alignment              alignment;

    /**
     * Construct an immutable object of this class by providing all
     * properties.
     * @param hit the SeqSimilaritySearchHit object of which this object
     * is a part. Not null.
     * @param score the score of this hit. This is a mandatory piece of
     * information and may hence not be NaN.
     * @param pValue the P-value of this hit. May be NaN.
     * @param eValue the E-value of this hit. May be NaN.
     * @param alignment the alignment of the query sequence against this
     * hit sequence. May be null.
     */
    public SimpleSeqSimilaritySearchSubHit(SeqSimilaritySearchHit hit,
					   double                 score,
					   double                 pValue,
					   double                 eValue,
					   Alignment              alignment)
    {
	Contract.pre(hit != null, "hit was null");
	Contract.pre(!Double.isNaN(score), "score was NaN");
	// pValue may be NaN
	// eValue may be NaN
	// alignment may be null

	this.hit       = hit;
	this.score     = score;
	this.pValue    = pValue;
	this.eValue    = eValue;
	this.alignment = alignment;
    }

    public SeqSimilaritySearchHit getHit()
    {
	return hit;
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
  
    public Alignment getAlignment()
    {
	return alignment;
    }

    public String toString()
    {
	return "SimpleSeqSimilaritySearchSubHit with score " + getScore();
    }
  
    public boolean equals(Object o)
    {
	if (o == this) return true;
    
	// if this class is a direct sub-class of Object:
	if (o == null) return false;
	if (! o.getClass().equals(this.getClass())) return false;
    
	SimpleSeqSimilaritySearchSubHit that = (SimpleSeqSimilaritySearchSubHit) o;
    
	// only compare fields of this class (not of super-classes):
	if (! ObjectUtil.equals(this.hit, that.hit))
	    return false;
	if (! ObjectUtil.equals(this.score, that.score))
	    return false;
	if (! ObjectUtil.equals(this.pValue, that.pValue))
	    return false;
	if (! ObjectUtil.equals(this.eValue, that.eValue))
	    return false;
	if (! ObjectUtil.equals(this.alignment, that.alignment))
	    return false;
    
	// this and that are identical if we made it 'til here
	return true;
    }
  
    public int hashCode()
    {
	// if this class is a direct sub-class of Object:
	int hc = 0;

	// only take into account fields of this class (not of super-class):
	hc = ObjectUtil.hashCode(hc, hit);
	hc = ObjectUtil.hashCode(hc, score);
	hc = ObjectUtil.hashCode(hc, pValue);
	hc = ObjectUtil.hashCode(hc, eValue);
	hc = ObjectUtil.hashCode(hc, alignment);

	return hc;
    }
  
    public Object clone()
    {
	// this is an immutable class so we can return ourselves
	return this;
    }
}
