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

import org.biojava.bio.seq.*;

/**
 * Storage structure for intermediate values from a pairwise
 * dynamic programming run.
 *
 * @author Thomas Down
 */

public class PairDPMatrix implements DPMatrix {
    private State[] states;
    private ResidueList[] seqs;
    private double[][][] scores;
    private MarkovModel model;
    private double finalScore;

    public PairDPMatrix(DP dp, ResidueList seq0, ResidueList seq1) {
        model = dp.getModel();
	states = dp.getStates();
	seqs = new ResidueList[2];
	seqs[0] = seq0;
	seqs[1] = seq1;
	finalScore = Double.NEGATIVE_INFINITY;
	scores = new double[seq0.length() + 2][seq1.length() + 2][states.length];
    }

    public State[] States() {
	return states;
    }

    public MarkovModel model() {
	return model;
    }

    public ResidueList[] resList() {
	return seqs;
    }

    public double getScore() {
	return finalScore;
    }

    public double getCell(int[] indxs) {
	if (indxs.length != 3) {
	    throw new IndexOutOfBoundsException("Index array must be length 3");
	}
	return scores[indxs[1]][indxs[2]][indxs[0]];
    }

    double[][][] getScoreArray() {
	return scores;
    }

    void setScore(double score) {
	this.finalScore = score;
    }
}
