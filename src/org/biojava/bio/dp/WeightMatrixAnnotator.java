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

package org.biojava.bio.dp;

import java.util.*;
import java.io.Serializable;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Annotates a sequence with hits to a weight-matrix.
 *
 * <p>
 * This SequenceAnnotator implementation returns a new
 * ViewSequence wrapping the underlying Sequence
 * </p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */
public class WeightMatrixAnnotator implements SequenceAnnotator, Serializable {
    private WeightMatrix matrix;
    private double threshold;

    public Sequence annotate(Sequence seq)
	throws IllegalAlphabetException, BioException, ChangeVetoException
    {
	seq = new ViewSequence(seq);
    
	int cols = matrix.columns();
	Feature.Template template = new Feature.Template();
	template.source = "WeightMatrixAnnotator";
	template.type = "hit";
	for(int offset = 1;
	    offset <= seq.length() - cols + 1;
	    offset++) {
	    double score = DP.scoreWeightMatrix(matrix, seq, offset);
	    double q = Math.exp(score);
	    if(q >= threshold) {
		template.location = new RangeLocation(offset, offset+cols-1);
		SimpleAnnotation ann = new SimpleAnnotation();
		ann.setProperty("score", new Double(q));
		ann.setProperty("weightMatrix", matrix);
		template.annotation = ann;
		seq.createFeature(template);
	    }
	}
	return seq;
    }

    public WeightMatrixAnnotator(WeightMatrix wm, double threshold) {
	this.matrix = wm;
	this.threshold = threshold;
    }
}


