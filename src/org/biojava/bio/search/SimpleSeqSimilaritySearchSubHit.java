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

/**
 * A simple implementation of interface SeqSimilaritySearchSubHit that
 * takes care of all the house-keeping. Objects of this class are
 * immutable.
 * 
 * @author Gerald Loeffler
 * @author Keith James
 */
public class SimpleSeqSimilaritySearchSubHit
    implements SeqSimilaritySearchSubHit, Cloneable
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

    // Hashcode is cached after first calculation because the data on
    // which is is based do not change
    private int hc;
    private boolean hcCalc;

    /**
     * Construct an immutable object of this class by providing all
     * properties.
     *
     * @param score a <code>double</code> value; the score of the
     * subhit, which may not be NaN.
     * @param eValue a <code>double</code> the E-value of the
     * subhit, which may be NaN.
     * @param pValue a <code>double</code> value; the P-value of the
     * hit, which may be NaN.
     * @param queryStart an <code>int</code> value indicating the
     * start coordinate of the hit on the query sequence.
     * @param queryEnd an <code>int</code> value indicating the end
     * coordinate of the hit on the query sequence.
     * @param queryStrand a <code>Strand</code> object indicating the
     * strand of the hit with respect to the query sequence, which may
     * be null for protein similarities.
     * @param subjectStart an <code>int</code> value indicating the
     * start coordinate of the hit on the subject sequence.
     * @param subjectEnd an <code>int</code> value indicating the end
     * coordinate of the hit on the query sequence.
     * @param subjectStrand a <code>Strand</code> object indicating
     * the strand of the hit with respect to the query sequence, which
     * may be null for protein similarities.
     * @param alignment an <code>Alignment</code> object containing
     * the alignment described by the subhit region, which may not be
     * null.
     */
    public SimpleSeqSimilaritySearchSubHit(double    score,
                                           double    eValue,
                                           double    pValue,
                                           int       queryStart,
                                           int       queryEnd,
                                           Strand    queryStrand,
                                           int       subjectStart,
                                           int       subjectEnd,
                                           Strand    subjectStrand,
                                           Alignment alignment)
    {
        if (Double.isNaN(score))
        {
            throw new IllegalArgumentException("score was NaN");
        }

        // pValue may be NaN
        // eValue may be NaN
        if (alignment == null)
        {
            throw new IllegalArgumentException("alignment was null");
        }

        this.score         = score;
        this.pValue        = pValue;
        this.eValue        = eValue;
        this.queryStart    = queryStart;
        this.queryEnd      = queryEnd;
        this.queryStrand   = queryStrand;
        this.subjectStart  = subjectStart;
        this.subjectEnd    = subjectEnd;
        this.subjectStrand = subjectStrand;
        this.alignment     = alignment;

        // Lock alignment by vetoing all changes
        this.alignment.addChangeListener(ChangeListener.ALWAYS_VETO);

        hcCalc = false;
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

    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;

        if (! o.getClass().equals(this.getClass())) return false;

        SimpleSeqSimilaritySearchSubHit that = (SimpleSeqSimilaritySearchSubHit) o;

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
        if (! hcCalc)
        {
            hc = ObjectUtil.hashCode(hc, score);
            hc = ObjectUtil.hashCode(hc, pValue);
            hc = ObjectUtil.hashCode(hc, eValue);
            hc = ObjectUtil.hashCode(hc, queryStart);
            hc = ObjectUtil.hashCode(hc, queryEnd);
            hc = ObjectUtil.hashCode(hc, queryStrand);
            hc = ObjectUtil.hashCode(hc, subjectStart);
            hc = ObjectUtil.hashCode(hc, subjectEnd);
            hc = ObjectUtil.hashCode(hc, subjectStrand);
            hcCalc = true;
        }

        return hc;
    }

    public String toString()
    {
        return "SimpleSeqSimilaritySearchSubHit with score " + getScore();
    }

    public Object clone()
    {
        return this;
    }
}
