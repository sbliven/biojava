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

package org.biojava.bio.seq.impl;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.homol.SimilarityPairFeature;
import org.biojava.bio.symbol.Alignment;
import org.biojava.bio.symbol.IllegalAlphabetException;

/**
 * <code>SimpleSimilarityPairFeature</code> represents a similarity
 * between a query sequence and a subject sequence as produced by a
 * search program.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class SimpleSimilarityPairFeature extends SimpleStrandedFeature
    implements SimilarityPairFeature
{
    private SimilarityPairFeature sibling;
    private Alignment             alignment;
    private double                score;

    /**
     * Creates a new <code>SimpleSimilarityPairFeature</code>.
     *
     * @param sourceSeq a <code>Sequence</code>.
     * @param parent a <code>FeatureHolder</code>.
     * @param template a <code>SimilarityPairFeature.Template</code>.
     */
    public SimpleSimilarityPairFeature(Sequence                       sourceSeq,
                                       FeatureHolder                  parent,
                                       SimilarityPairFeature.Template template)
        throws IllegalAlphabetException                 
    {
        super(sourceSeq, parent, template);

        this.sibling   = template.sibling;
        this.alignment = template.alignment;
        this.score     = template.score;
    }

    /**
     * <code>getSibling</code> returns the sibling feature of the
     * pair.
     *
     * @return a <code>Feature</code>.
     */
    public SimilarityPairFeature getSibling()
    {
        return sibling;
    }

    /**
     * <code>getAlignment</code> returns the alignment between the two
     * features.
     *
     * @return an <code>Alignment</code>.
     */
    public Alignment getAlignment()
    {
        return alignment;
    }

    /**
     * <code>getScore</code> returns the alignment score.
     *
     * @return a <code>double</code>.
     */
    public double getScore()
    {
        return score;
    }

    public String toString()
    {
        return super.toString() + " [score " + score + "]";
    }
}
