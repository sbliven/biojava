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

import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.utils.ObjectUtil;

/**
 * <p>A simple implementation of interface SeqSimilaritySearchHit
 * that takes care of all the housekeeping. Objects of this class are
 * immutable.</p>
 *
 * <p>It is up to the user to define the meaning of the hit's
 * query/subject start/end/strand with respect to its constituent
 * sub-hits.</p>
 * 
 * @author Gerald Loeffler
 * @author Keith James
 */
public class SimpleSeqSimilaritySearchHit
    implements SeqSimilaritySearchHit, Cloneable
{
    private double score;
    private double pValue;
    private double eValue;
    private int    qStart;
    private int    qEnd;
    private Strand qStrand;
    private int    sStart;
    private int    sEnd;
    private Strand sStrand;
    private String sequenceID;
    private List   subHits;

    // Hashcode is cached after first calculation because the data on
    // which is is based do not change
    private int hc;
    private boolean hcCalc;

    /**
     * Construct an immutable object from the values of all properties.
     *
     * @param score the overall score of this hit. This is a mandatory
     * piece of information and may hence not be NaN.
     * @param pValue the overall P-value of this hit, which may be
     * NaN.
     * @param eValue the overall E-value of this hit, which may be
     * NaN.
     * @param qStart the start of the first sub-hit on the query
     * sequence.
     * @param qEnd the end of the last sub-hit on the query
     * sequence.
     * @param qStrand the strand of the sub-hits on the query
     * sequence, which may be null for protein similarities. If they
     * are not all positive or all negative, then this should be the
     * unknown strand.
     * @param sStart the start of the first sub-hit on the subject
     * sequence.
     * @param sEnd the end of the last sub-hit on the subject
     * sequence.
     * @param sStrand the strand of the sub-hits on the subject
     * sequence, which may be null for protein similarities. If they
     * are no all positive or all negative, then this should be the
     * unknown strand.
     * @param sequenceID the (unique) sequence identifier for this
     * hit, valid within the sequence database against which this
     * search was performed, which may not be null.
     * @param subHits a List of SeqSimilaritySearchSubHit objects
     * containing all sub-hits for this hit, which may not be null.
     */
    public SimpleSeqSimilaritySearchHit(double score,
                                        double eValue,
                                        double pValue,
                                        int    qStart,
                                        int    qEnd,
                                        Strand qStrand,
                                        int    sStart,
                                        int    sEnd,
                                        Strand sStrand,
                                        String sequenceID,
                                        List   subHits)
    {
        if (Double.isNaN(score))
        {
            throw new IllegalArgumentException("score was NaN");
        }
        // pValue may be NaN
        // eValue may be NaN
        if (sequenceID == null)
        {
            throw new IllegalArgumentException("sequenceID was null");
        }
        if (subHits == null)
        {
            throw new IllegalArgumentException("subHits was null");
        }

        this.score      = score;
        this.pValue     = pValue;
        this.eValue     = eValue;
        this.sequenceID = sequenceID;
        this.qStart     = qStart;
        this.qEnd       = qEnd;
        this.qStrand    = qStrand;
        this.sStart     = sStart;
        this.sEnd       = sEnd;
        this.sStrand    = sStrand;
        this.subHits    = subHits;

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
        return qStart;
    }

    public int getQueryEnd()
    {
        return qEnd;
    }

    public Strand getQueryStrand()
    {
        return qStrand;
    }

    public int getSubjectStart()
    {
        return sStart;
    }

    public int getSubjectEnd()
    {
        return sEnd;
    }

    public Strand getSubjectStrand()
    {
        return sStrand;
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
        if (! hcCalc)
        {
            hc = ObjectUtil.hashCode(hc, score);
            hc = ObjectUtil.hashCode(hc, pValue);
            hc = ObjectUtil.hashCode(hc, eValue);
            hc = ObjectUtil.hashCode(hc, sequenceID);
            hc = ObjectUtil.hashCode(hc, subHits);
            hcCalc = true;
        }

        return hc;
    }
  
    public Object clone()
    {
        // this is an immutable class so we can return ourselves
        return this;
    }
}
