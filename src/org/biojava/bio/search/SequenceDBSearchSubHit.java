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
    private Alignment alignment;

    /**
     * Creates a new <code>SequenceDBSearchSubHit</code> object.
     *
     * @param score a <code>double</code> value; the score of the
     * subhit, which may not be NaN.
     * @param eValue a <code>double</code> value; the E-value of the
     * subhit, which may not be NaN.
     * @param pValue a <code>double</code> value; the P-value of the
     * hit, which may not be NaN.
     * @param alignment an <code>Alignment</code> object containing
     * the alignemnt described by the subhit region.
     */
    public SequenceDBSearchSubHit(final double    score,
				  final double    eValue,
				  final double    pValue,
				  final Alignment alignment)
    {
	this.score     = score;
	this.pValue    = eValue;
	this.eValue    = pValue;
	this.alignment = alignment;

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
	if (! ObjectUtil.equals(this.alignment, that.alignment))
	    return false;

	return true;
    }
  
    public int hashCode()
    {
	int hCode = 0;

	hCode = ObjectUtil.hashCode(hCode, score);
	hCode = ObjectUtil.hashCode(hCode, pValue);
	hCode = ObjectUtil.hashCode(hCode, eValue);
	hCode = ObjectUtil.hashCode(hCode, alignment);

	return hCode;
    }
}
