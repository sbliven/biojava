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

import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.bio.symbol.Alignment;
import org.biojava.utils.ChangeListener;
import org.biojava.utils.ObjectUtil;
import org.biojava.utils.contract.Contract;

/**
 * <code>SequenceDBSearchSubHit</code> objects.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.1
 * @see SeqSimilaritySearchSubHit
 */
public class SequenceDBSearchSubHit implements SeqSimilaritySearchSubHit
{
    private double    score;
    private double    pValue;
    private double    eValue;
    private int       queryStart;
    private int       queryEnd;
    private Strand    queryStrand;
    private int       subjectStart;
    private int       subjectEnd;
    private Strand    subjectStrand;
    private Alignment alignment;

    /**
     * Creates a new <code>SequenceDBSearchSubHit</code> object.
     *
     * @param queryStart an <code>int</code> value indicating the
     * start coordinate of the hit on the query sequence.
     * @param queryEnd an <code>int</code> value indicating the end
     * coordinate of the hit on the query sequence.
     * @param queryStrand a <code>Strand</code> object indicating the
     * strand of the hit with respect to the query sequence, which may
     * not be null.
     * @param subjectStart an <code>int</code> value indicating the
     * start coordinate of the hit on the subject sequence.
     * @param subjectEnd an <code>int</code> value indicating the end
     * coordinate of the hit on the query sequence.
     * @param subjectStrand a <code>Strand</code> object indicating
     * the strand of the hit with respect to the query sequence, which
     * may not be null.
     * @param score a <code>double</code> value; the score of the
     * subhit, which may not be NaN.
     * @param eValue a <code>double</code> the E-value of the
     * subhit, which may not be NaN.
     * @param pValue a <code>double</code> value; the P-value of the
     * hit, which may not be NaN.
     * @param alignment an <code>Alignment</code> object containing
     * the alignment described by the subhit region, which may not be
     * null. 
     */
    public SequenceDBSearchSubHit(final double    score,
				  final double    eValue,
				  final double    pValue,
                                  final int       queryStart,
				  final int       queryEnd,
				  final Strand    queryStrand,
				  final int       subjectStart,
				  final int       subjectEnd,
				  final Strand    subjectStrand,
				  final Alignment alignment)
    {
        Contract.pre(! Double.isNaN(score), "score was NaN");
        // pValue may be NaN
	// eValue may be NaN
	Contract.pre(queryStrand   != null, "queryStrand was null");
	Contract.pre(subjectStrand != null, "subjectStrand was null");
	Contract.pre(alignment     != null, "alignment was null");

	this.score         = score;
	this.pValue        = eValue;
	this.eValue        = pValue;
	this.queryStart    = queryStart;
	this.queryEnd      = queryEnd;
	this.queryStrand   = queryStrand;
	this.subjectStart  = subjectStart;
	this.subjectEnd    = subjectEnd;
	this.subjectStrand = subjectStrand;
	this.alignment     = alignment;

	// Lock alignment by vetoing all changes
	this.alignment.addChangeListener(ChangeListener.ALWAYS_VETO);
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

    public int getQueryStart()
    {
	return queryStart;
    }

    public int getQueryEnd()
    {
	return queryEnd;
    }

    public Strand getQueryStrand()
    {
	return queryStrand;
    }

    public int getSubjectStart()
    {
	return subjectStart;
    }

    public int getSubjectEnd()
    {
	return subjectEnd;
    }

    public Strand getSubjectStrand()
    {
	return subjectStrand;
    }

    public Alignment getAlignment()
    {
	return alignment;
    }

    public String toString()
    {
	return "SequenceDBSearchSubHit with score " + getScore();
    }
  
    public boolean equals(final Object other)
    {
	if (other == this) return true;
	if (other == null) return false;

	// Eliminate other if its class is not the same
	if (! other.getClass().equals(this.getClass())) return false;
    
	// Downcast and compare fields
	SequenceDBSearchSubHit that = (SequenceDBSearchSubHit) other;

	if (! ObjectUtil.equals(this.score, that.score))
	    return false;
	if (! ObjectUtil.equals(this.pValue, that.pValue))
	    return false;
	if (! ObjectUtil.equals(this.eValue, that.eValue))
	    return false;
	if (! ObjectUtil.equals(this.queryStart, that.queryStart))
	    return false;
	if (! ObjectUtil.equals(this.queryEnd, that.queryEnd))
	    return false;
	if (! ObjectUtil.equals(this.queryStrand, that.queryStrand))
	    return false;
	if (! ObjectUtil.equals(this.subjectStart, that.subjectStart))
	    return false;
	if (! ObjectUtil.equals(this.subjectEnd, that.subjectEnd))
	    return false;
	if (! ObjectUtil.equals(this.subjectStrand, that.subjectStrand))
	    return false;

	return true;
    }
  
    public int hashCode()
    {
	int hc = 0;

	hc = ObjectUtil.hashCode(hc, score);
	hc = ObjectUtil.hashCode(hc, pValue);
	hc = ObjectUtil.hashCode(hc, eValue);
	hc = ObjectUtil.hashCode(hc, queryStart);
	hc = ObjectUtil.hashCode(hc, queryEnd);
	hc = ObjectUtil.hashCode(hc, queryStrand);
	hc = ObjectUtil.hashCode(hc, subjectStart);
	hc = ObjectUtil.hashCode(hc, subjectEnd);
	hc = ObjectUtil.hashCode(hc, subjectStrand);

	return hc;
    }
}
