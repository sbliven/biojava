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

import org.biojava.bio.Annotatable;
import org.biojava.bio.Annotation;
import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeListener;
import org.biojava.utils.ObjectUtil;

/**
 * <p>
 * <code>SequenceDBSearchHit</code> objects represent a similarity
 * search hit of a query sequence to a sequence referenced in a
 * SequenceDB object. The core data (score, E-value, P-value) have
 * accessors, while supplementary data are stored in the Annotation
 * object. Supplementary data are typically the more loosely formatted
 * details which vary from one search program to another (and between
 * versions of those programs).
 * </p>
 *
 * <p>
 * It is up to the user to define the meaning of the hit's
 * query/subject start/end/strand with respect to its constituent
 * sub-hits. One approach could be:
 * </p>
 *
 * <ul>
 * <li>Hit query/subject start == start of first sub-hit</li>
 * <li>Hit query/subject   end == end of last sub-hit</li>
 * <li>Hit strand == POSITIVE if all sub-hits have strand POSITIVE</li>
 * <li>Hit strand == NEGATIVE if all sub-hits have strand NEGATIVE</li>
 * <li>Hit strand == UNKNOWN if sub-hits have either mixed or any UNKNOWN
 *     strands</li>
 * <li>Hit strand == null if the concept of strandedness is inappropriate
 *     for the sequence type i.e. for protein</li>
 * </ul>
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.1
 * @see AbstractChangeable
 * @see SeqSimilaritySearchHit
 * @see Annotatable
 */
public class SequenceDBSearchHit extends AbstractChangeable
    implements SeqSimilaritySearchHit, Annotatable
{
    private String     sequenceID;
    private double     score;
    private double     pValue;
    private double     eValue;
    private int        queryStart;
    private int        queryEnd;
    private Strand     queryStrand;
    private int        subjectStart;
    private int        subjectEnd;
    private Strand     subjectStrand;
    private List       subHits;
    private Annotation annotation;

    /**
     * Creates a new <code>SequenceDBSearchHit</code> object.
     *
     * @param sequenceID a <code>String</code> representing the ID in
     * the SequenceDB of the sequence which was hit, which may not be
     * null.
     * @param score a <code>double</code> value; the score of the hit,
     * which may not be NaN.
     * @param eValue a <code>double</code> value; the E-value of the
     * hit, which may be NaN.
     * @param pValue a <code>double</code> value; the P-value of the
     * hit, which may be NaN.
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
     * @param subHits a <code>List</code> object containing the
     * subhits, which may not be null. They should be sorted in the
     * order specified by the search program.
     * @param annotation an <code>Annotation</code> object, which may
     * not be null.
     */
    public SequenceDBSearchHit(final double     score,
			       final double     eValue,
			       final double     pValue,
                               final int        queryStart,
                               final int        queryEnd,
                               final Strand     queryStrand,
                               final int        subjectStart,
                               final int        subjectEnd,
                               final Strand     subjectStrand,
                               final String     sequenceID,
			       final Annotation annotation,
                               final List       subHits)
    {
        if (Double.isNaN(score)) {
	    throw new IllegalArgumentException("score was NaN");
	}
	// pValue may be NaN
	// eValue may be NaN
	if (sequenceID == null) {
	    throw new IllegalArgumentException("sequenceID was null");
	}
	if (annotation == null) {
	    throw new IllegalArgumentException("annotation was null");
	}
	if (subHits == null) {
	    throw new IllegalArgumentException("subHits was null");
	}

	this.sequenceID    = sequenceID;
	this.score         = score;
	this.eValue        = eValue;
	this.pValue        = pValue;
        this.queryStart    = queryStart;
        this.queryEnd      = queryEnd;
        this.queryStrand   = queryStrand;
        this.subjectStart  = subjectStart;
        this.subjectEnd    = subjectEnd;
        this.subjectStrand = subjectStrand;
	this.subHits       = subHits;
	this.annotation    = annotation;

	// Lock the annotation by vetoing all changes
	this.annotation.addChangeListener(ChangeListener.ALWAYS_VETO);
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

    public String getSequenceID()
    {
	return sequenceID;
    }

    public List getSubHits()
    {
	return Collections.unmodifiableList(subHits);
    }

    /**
     * <code>getAnnotation</code> returns the Annotation associated
     * with this hit.
     *
     * @return an <code>Annotation</code> object.
     */
    public Annotation getAnnotation()
    {
	return annotation;
    }

    public boolean equals(final Object other)
    {
	if (other == this) return true;
	if (other == null) return false;

	// Eliminate other if its class is not the same
	if (! other.getClass().equals(this.getClass())) return false;
    
	// Downcast and compare fields
	SequenceDBSearchHit that = (SequenceDBSearchHit) other;

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
    
	return true;
    }
  
    public int hashCode()
    {
	int hc = 0;

	hc = ObjectUtil.hashCode(hc, score);
	hc = ObjectUtil.hashCode(hc, pValue);
	hc = ObjectUtil.hashCode(hc, eValue);
	hc = ObjectUtil.hashCode(hc, sequenceID);
	hc = ObjectUtil.hashCode(hc, subHits);

	return hc;
    }

    public String toString()
    {
	return "SequenceDBSearchHit to " + getSequenceID()
	    + " with score " + getScore();
    }
}
