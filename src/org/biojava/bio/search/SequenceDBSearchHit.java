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
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeListener;
import org.biojava.utils.ObjectUtil;
import org.biojava.utils.contract.Contract;

/**
 * <code>SequenceDBSearchHit</code> objects represent a similarity
 * search hit of a query sequence to a sequence referenced in a
 * SequenceDB object. The core data (score, E-value, P-value) have
 * accessors, while supplementary data are stored in the Annotation
 * object. Supplementary data are typically the more loosely formatted
 * details which vary from one search program to another (and between
 * versions of those programs).
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
     * hit, which may not be NaN.
     * @param pValue a <code>double</code> value; the P-value of the
     * hit, which may not be NaN.
     * @param subHits a <code>List</code> object containing the
     * subhits, which may not be null.
     * @param annotation an <code>Annotation</code> object. If null a
     * new SimpleAnnotation object is created internally.
     */
    public SequenceDBSearchHit(final String     sequenceID,
			       final double     score,
			       final double     eValue,
			       final double     pValue,
			       final List       subHits,
			       final Annotation annotation)
    {
	Contract.pre(sequenceID != null, "sequenceID was null");
	Contract.pre(! Double.isNaN(score), "score was NaN");
	// pValue may be NaN
	// eValue may be NaN
	Contract.pre(subHits != null, "subHits was null");

	this.sequenceID = sequenceID;
	this.score      = score;
	this.eValue     = eValue;
	this.pValue     = pValue;
	this.subHits    = subHits;
	this.annotation = annotation;

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
	int hCode = 0;

	hCode = ObjectUtil.hashCode(hCode, score);
	hCode = ObjectUtil.hashCode(hCode, pValue);
	hCode = ObjectUtil.hashCode(hCode, eValue);
	hCode = ObjectUtil.hashCode(hCode, sequenceID);
	hCode = ObjectUtil.hashCode(hCode, subHits);

	return hCode;
    }

    public String toString()
    {
	return "SequenceDBSearchHit to " + getSequenceID()
	    + " with score " + getScore();
    }
}
