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

import org.biojava.bio.BioError;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

/**
 * Algorithms for dynamic programming (alignments) between pairs
 * of ResidueLists.
 * Based on a single-head DP implementation by Matt Pocock.
 *
 * @author Thomas Down
 */

public class PairwiseDP extends DP {
    private EmissionState magicalState;
    private Residue magicalResidue;

    public PairwiseDP(MarkovModel mm) throws IllegalResidueException,
                                           IllegalTransitionException
    {
	super(mm);
	magicalState = mm.magicalState();
	magicalResidue = MagicalState.MAGICAL_RESIDUE;
    }

    private final static int[] ia00 = {0, 0};

    //
    // BACKWARD
    //

    public double backward(ResidueList[] seqs) 
        throws IllegalResidueException, IllegalAlphabetException, IllegalTransitionException
    {
//  	if (seqs.length != 2)
//  	    throw new IllegalArgumentException("This DP object only runs on pairs.");

//  	Backward f = new Backward();
//  	PairDPCursor cursor = new LightPairDPCursor(seqs[0], seqs[1], getStates().length, false);
//  	return f.runBackward(seqs[0], seqs[1], cursor);

	return backwardMatrix(seqs).getScore();
    }

    public DPMatrix backwardMatrix(ResidueList[] seqs) 
	throws IllegalResidueException, IllegalAlphabetException, IllegalTransitionException
    {
	if (seqs.length != 2)
	    throw new IllegalArgumentException("This DP object only runs on pairs.");

	Backward f = new Backward();
	PairDPMatrix matrix = new PairDPMatrix(this, seqs[0], seqs[1]);
	PairDPCursor cursor = new BackMatrixPairDPCursor(seqs[0], seqs[1], matrix);
	double score = f.runBackward(seqs[0], seqs[1], cursor);
	matrix.setScore(score);
	return matrix;
    }

    public DPMatrix backwardMatrix(ResidueList[] seqs, DPMatrix d) 
	throws IllegalResidueException, IllegalAlphabetException, IllegalTransitionException
    {
	return backwardMatrix(seqs);
    }


private class Backward {
    private int[][] transitions;
    private double[][] transitionScores;
    private State[] states;
    private PairDPCursor cursor;
    private CrossProductAlphabet alpha;

    public double runBackward(ResidueList seq0, ResidueList seq1, PairDPCursor curs) 
        throws IllegalResidueException, IllegalAlphabetException, IllegalTransitionException
    {
	states = getStates();
	cursor = curs;
	alpha = (CrossProductAlphabet) getModel().emissionAlphabet();

	// initialization

	double[] col = cursor.getCurrentColumn();
	for (int l = 0; l < states.length; ++l)
	    col[l] = (states[l] == magicalState) ? 0.0 :
	                Double.NEGATIVE_INFINITY;

	// Recurse

	transitions = getBackwardTransitions();
	transitionScores = getBackwardTransitionScores();

	while (cursor.canAdvance(0) || cursor.canAdvance(1)) {
	    if (cursor.canAdvance(0)) {
		cursor.advance(0);
		for (int i = seq1.length() + 1; i >= cursor.getPos(1); --i) {
		    backwardPrepareCol(cursor.getPos(0), i);
		}
	    }

	    if (cursor.canAdvance(1)) {
		cursor.advance(1);
		for (int i = seq0.length() + 1; i >= cursor.getPos(0); --i) {
		    backwardPrepareCol(i, cursor.getPos(1));
		}
	    }
	}  

	// Terminate!

	col = cursor.getColumn(ia00);
	int l = 0;
	while (states[l] != magicalState)
	    ++l;

	return col[l];
    }

    private List ress = new ArrayList();
    private Object[][] matrix = new Object[2][2];
    private Residue[][] resMatrix = new Residue[2][2];
    private int[] colId = new int[2];

    private void backwardPrepareCol(int i, int j)
	throws IllegalResidueException, IllegalAlphabetException, IllegalTransitionException
    {
	// System.out.println("*** (" + i + "," + j + ")");

	ress.clear();
	ress.add(cursor.residue(0, i+1));
	ress.add(cursor.residue(1, j+1));
	// resMatrix[1][1] = alpha.getResidue(ress);
	resMatrix[1][1] = getResWrapper(alpha, ress);
	ress.clear();
	ress.add(cursor.residue(0, i + 1));
	ress.add(AlphabetManager.instance().getGapResidue());
	// resMatrix[1][0] = alpha.getResidue(ress);
	resMatrix[1][0] = getResWrapper(alpha, ress);
	ress.clear();
	ress.add(AlphabetManager.instance().getGapResidue());
	ress.add(cursor.residue(1, j + 1));
	// resMatrix[0][1] = alpha.getResidue(ress);
	resMatrix[0][1] = getResWrapper(alpha, ress);

	colId[0] = i;
	colId[1] = j;
	// System.out.println("***A ("+ colId[0] + "," + colId[1] +")");
	matrix[0][0] = cursor.getColumn(colId);
	colId[0]++;
	matrix[1][0] = cursor.getColumn(colId);
	colId[1]++;
	matrix[1][1] = cursor.getColumn(colId);
	colId[0]--;
	matrix[0][1] = cursor.getColumn(colId);

	backwardCalcStepMatrix();
    }

    private void backwardCalcStepMatrix()
	throws IllegalResidueException, IllegalAlphabetException, IllegalTransitionException
    {
	double[] curCol = (double[]) matrix[0][0];
	int[] advance;
	for (int l = states.length - 1; l >= 0; --l) {
	    // System.out.println("State = " + states[l].getName());

	    if (states[l] instanceof EmissionState)
		advance = ((EmissionState) states[l]).getAdvance();
	    else
		advance = ia00;


//  	    Residue res = resMatrix[advance[0]][advance[1]];
//  	    double weight = Double.NEGATIVE_INFINITY;
//  	    if (res == null) {
//  		weight = Double.NEGATIVE_INFINITY;
//  	    } else if (! (states[l] instanceof EmissionState)) {
//  		weight = 0.0;
//  	    } else if (res == MagicalState.MAGICAL_RESIDUE) {
//  		try {
//  		    weight = ((EmissionState) states[l]).getWeight(res);
//  		} catch (Exception ex) {}
//  	    } else {
//  		weight = ((EmissionState) states[l]).getWeight(res);
//  	    }


	    // System.out.println("weight = " + weight);
	    double score = 0.0;
	    int [] tr = transitions[l];
	    double[] trs = transitionScores[l];

	    // Calculate probabilities for states with transitions
	    // here.
		

	    double[] sourceScores = new double[tr.length];
	    for (int ci = 0; ci < tr.length; ++ci) {
		double[] sCol;
		double weight = 0.0;

		if (states[tr[ci]] instanceof EmissionState) {
		    advance = ((EmissionState)states[tr[ci]]).getAdvance();
		    Residue res = resMatrix[advance[0]][advance[1]];
		    if (res == null) {
			weight = Double.NEGATIVE_INFINITY;
		    } else if (! (states[tr[ci]] instanceof EmissionState)) {
			weight = 0.0;
		    } else if (res == MagicalState.MAGICAL_RESIDUE) {
			try {
			    weight = ((EmissionState) states[tr[ci]]).getWeight(res);
			} catch (Exception ex) {}
		    } else {
			weight = ((EmissionState) states[tr[ci]]).getWeight(res);
		    }
		    sCol = (double[]) matrix[advance[0]][advance[1]];
		} else {
		    sCol = (double[]) matrix[0][0];
		}
		sourceScores[ci] = sCol[tr[ci]] + weight;
	    }

	    // Find base for addition
	    int ci = 0;
	    while (ci < tr.length && sourceScores[ci] == Double.NEGATIVE_INFINITY)
		++ci;
	    double constant = (ci < tr.length) ? sourceScores[ci] : 0.0;
	    
	    for (int kc = 0; kc < tr.length; ++kc) {
		// System.out.println("In from " + states[kc].getName());
		// System.out.println("prevScore = " + sourceScores[kc]);

		int k = tr[kc];
		if (sourceScores[kc] != Double.NEGATIVE_INFINITY) {
		    double t = trs[kc];
		    score += Math.exp(t + sourceScores[kc] - constant);
		}
	    }
	    curCol[l] = Math.log(score) + constant;
	    // System.out.println(curCol[l]);
	}
    }
}

    //
    // FORWARD
    // 

    public double forward(ResidueList[] seqs) 
        throws IllegalResidueException, IllegalAlphabetException, IllegalTransitionException
    {
	if (seqs.length != 2)
	    throw new IllegalArgumentException("This DP object only runs on pairs.");

	Forward f = new Forward();
	PairDPCursor cursor = new LightPairDPCursor(seqs[0], seqs[1], getStates().length, false);
	return f.runForward(seqs[0], seqs[1], cursor);
    }

    public DPMatrix forwardMatrix(ResidueList[] seqs) 
	throws IllegalResidueException, IllegalAlphabetException, IllegalTransitionException
    {
	if (seqs.length != 2)
	    throw new IllegalArgumentException("This DP object only runs on pairs.");

	Forward f = new Forward();
	PairDPMatrix matrix = new PairDPMatrix(this, seqs[0], seqs[1]);
	PairDPCursor cursor = new MatrixPairDPCursor(seqs[0], seqs[1], matrix);
	double score = f.runForward(seqs[0], seqs[1], cursor);
	matrix.setScore(score);
	return matrix;
    }

    public DPMatrix forwardMatrix(ResidueList[] seqs, DPMatrix d) 
	throws IllegalResidueException, IllegalAlphabetException, IllegalTransitionException
    {
	return forwardMatrix(seqs);
    }

    private static List gappedResList = new ArrayList();

    private static Residue getResWrapper(CrossProductAlphabet a, List l) 
        throws IllegalAlphabetException
    {
//      System.out.println(a.getName() + " getting:");
//      for (Iterator i = l.iterator(); i.hasNext(); ) {
//          Residue blah = (Residue) i.next();
//          System.out.println(blah.getName());
//      }

	if (l.contains(null))
	    return null;

        if (l.contains(MagicalState.MAGICAL_RESIDUE)) {
	    Residue gr = AlphabetManager.instance().getGapResidue();

	    gappedResList.clear();
	    boolean gotOther = false;
            for (Iterator i = l.iterator(); i.hasNext(); ) {
		Object o = i.next();
		if (o == MagicalState.MAGICAL_RESIDUE || o == gr) {
		    gappedResList.add(gr);
		} else {
		    gappedResList.add(o);
		    gotOther = true;
		}   
	    }
	    if (gotOther)
		return a.getResidue(gappedResList);
	    else
		return MagicalState.MAGICAL_RESIDUE;
        }
        return a.getResidue(l);
    }

private class Forward {
    private int[][] transitions;
    private double[][] transitionScores;
    private State[] states;
    private PairDPCursor cursor;
    private CrossProductAlphabet alpha;

    public double runForward(ResidueList seq0, ResidueList seq1, PairDPCursor curs) 
        throws IllegalResidueException, IllegalAlphabetException, IllegalTransitionException
    {
	states = getStates();
	cursor = curs;
	alpha = (CrossProductAlphabet) getModel().emissionAlphabet();

	// Forward initialization

	double[] col = cursor.getColumn(ia00);
	for (int l = 0; l < states.length; ++l)
	    col[l] = (states[l] == magicalState) ? 0.0 :
	                Double.NEGATIVE_INFINITY;

	// Recurse

	transitions = getForwardTransitions();
	transitionScores = getForwardTransitionScores();

	while (cursor.canAdvance(0) || cursor.canAdvance(1)) {
	    if (cursor.canAdvance(0)) {
		cursor.advance(0);
		for (int i = 0; i <= cursor.getPos(1); ++i) {
		    forwardPrepareCol(cursor.getPos(0), i);
		}
	    }

	    if (cursor.canAdvance(1)) {
		cursor.advance(1);
		for (int i = 0; i <= cursor.getPos(0); ++i) {
		    forwardPrepareCol(i, cursor.getPos(1));
		}
	    }
	}  

	// Terminate!

	int[] colId = new int[2];
	colId[0] = cursor.getPos(0);
	colId[1] = cursor.getPos(1);
	col = cursor.getColumn(colId);
	int l = 0;
	while (states[l] != magicalState)
	    ++l;

	return col[l];
    }

    private List ress = new ArrayList();
    private Object[][] matrix = new Object[2][2];
    private Residue[][] resMatrix = new Residue[2][2];
    private int[] colId = new int[2];

    private void forwardPrepareCol(int i, int j)
	throws IllegalResidueException, IllegalAlphabetException, IllegalTransitionException
    {
	// System.out.println("*** (" + i + "," + j + ")");

	ress.clear();
	ress.add(cursor.residue(0, i));
	ress.add(cursor.residue(1, j));
	// resMatrix[1][1] = alpha.getResidue(ress);
	resMatrix[1][1] = getResWrapper(alpha, ress);
	ress.clear();
	ress.add(cursor.residue(0, i));
	ress.add(AlphabetManager.instance().getGapResidue());
	// resMatrix[1][0] = alpha.getResidue(ress);
	resMatrix[1][0] = getResWrapper(alpha, ress);
	ress.clear();
	ress.add(AlphabetManager.instance().getGapResidue());
	ress.add(cursor.residue(1, j));
	// resMatrix[0][1] = alpha.getResidue(ress);
	resMatrix[0][1] = getResWrapper(alpha, ress);

	colId[0] = i;
	colId[1] = j;
	// System.out.println("***A ("+ colId[0] + "," + colId[1] +")");
	matrix[0][0] = cursor.getColumn(colId);
	colId[0]--;
	matrix[1][0] = cursor.getColumn(colId);
	colId[1]--;
	matrix[1][1] = cursor.getColumn(colId);
	colId[0]++;
	matrix[0][1] = cursor.getColumn(colId);

	forwardCalcStepMatrix();
    }

    private void forwardCalcStepMatrix()
	throws IllegalResidueException, IllegalAlphabetException, IllegalTransitionException
    {
	double[] curCol = (double[]) matrix[0][0];
	int[] advance;
	for (int l = 0; l < states.length; ++l) {
	    // System.out.println("State = " + states[l].getName());

	    if (states[l] instanceof EmissionState)
		advance = ((EmissionState) states[l]).getAdvance();
	    else
		advance = ia00;
	    Residue res = resMatrix[advance[0]][advance[1]];
	    double weight = Double.NEGATIVE_INFINITY;
	    if (res == null) {
		weight = Double.NEGATIVE_INFINITY;
	    } else if (! (states[l] instanceof EmissionState)) {
		weight = 0.0;
	    } else if (res == MagicalState.MAGICAL_RESIDUE) {
		try {
		    weight = ((EmissionState) states[l]).getWeight(res);
		} catch (Exception ex) {}
	    } else {
		weight = ((EmissionState) states[l]).getWeight(res);
	    }

	    if (weight == Double.NEGATIVE_INFINITY) {
		curCol[l] = Double.NEGATIVE_INFINITY;
	    } else {
		// System.out.println("weight = " + weight);
		double score = 0.0;
		int [] tr = transitions[l];
		double[] trs = transitionScores[l];

		// Calculate probabilities for states with transitions
		// here.
		
//		double[] lastCol = new double[states.length];
//  		for (int ci = 0; ci < states.length; ++ci) {
//  		    advance = states[ci].getAdvance();
//  		    double[] sCol = (double[]) matrix[advance[0]][advance[1]];
//  		    lastCol[ci] = sCol[ci];
//  		}

		double[] sourceScores = new double[tr.length];
		for (int ci = 0; ci < tr.length; ++ci) {
		    double[] sCol;
		    if (states[tr[ci]] instanceof EmissionState) {
			advance = ((EmissionState)states[tr[ci]]).getAdvance();
		        sCol = (double[]) matrix[advance[0]][advance[1]];
		    } else {
			sCol = (double[]) matrix[0][0];
		    }
		    sourceScores[ci] = sCol[tr[ci]];
		}

		// Find base for addition
		int ci = 0;
		while (ci < tr.length && sourceScores[ci] == Double.NEGATIVE_INFINITY)
		    ++ci;
		double constant = (ci < tr.length) ? sourceScores[ci] : 0.0;

		for (int kc = 0; kc < tr.length; ++kc) {
		    // System.out.println("In from " + states[kc].getName());
		    // System.out.println("prevScore = " + sourceScores[kc]);

		    int k = tr[kc];
		    if (sourceScores[kc] != Double.NEGATIVE_INFINITY) {
			double t = trs[kc];
			score += Math.exp(t + sourceScores[kc] - constant);
		    }
		}
		curCol[l] = weight + Math.log(score) + constant;
		// System.out.println(curCol[l]);
	    }
	}
    }
}

    //
    // VITERBI!
    //

    public StatePath viterbi(ResidueList[] seqs) 
        throws IllegalResidueException, IllegalAlphabetException, IllegalTransitionException
    {
	if (seqs.length != 2)
	    throw new IllegalArgumentException("This DP object only runs on pairs.");

	Viterbi v = new Viterbi();
	return v.runViterbi(seqs[0], seqs[1]);
    }


private class Viterbi { 
    private int[][] transitions;
    private double[][] transitionScores;
    private State[] states;
    private PairDPCursor cursor;
    private CrossProductAlphabet alpha;

    public StatePath runViterbi(ResidueList seq0, ResidueList seq1) 
        throws IllegalResidueException, IllegalAlphabetException, IllegalTransitionException
    {
	states = getStates();
	cursor = new LightPairDPCursor(seq0, seq1, states.length, true);
	alpha = (CrossProductAlphabet) getModel().emissionAlphabet();

	// Forward initialization

	double[] col = cursor.getColumn(ia00);
	for (int l = 0; l < states.length; ++l)
	    col[l] = (states[l] == magicalState) ? 0.0 :
	                Double.NEGATIVE_INFINITY;

	// Recurse

	transitions = getForwardTransitions();
	transitionScores = getForwardTransitionScores();

	while (cursor.canAdvance(0) || cursor.canAdvance(1)) {
	    if (cursor.canAdvance(0)) {
		cursor.advance(0);
		for (int i = 0; i <= cursor.getPos(1); ++i) {
		    viterbiPrepareCol(cursor.getPos(0), i);
		}
	    }

	    if (cursor.canAdvance(1)) {
		cursor.advance(1);
		for (int i = 0; i <= cursor.getPos(0); ++i) {
		    viterbiPrepareCol(i, cursor.getPos(1));
		}
	    }
	}  

	// Terminate!

	int[] colId = new int[2];
	colId[0] = cursor.getPos(0);
	colId[1] = cursor.getPos(1);
	col = cursor.getColumn(colId);
	int l = 0;
	while (states[l] != magicalState)
	    ++l;

	// Traceback...  
	
	BackPointer[] bpCol = (BackPointer[]) cursor.getBackPointers(colId);
	BackPointer bp = bpCol[l];
	List statel = new ArrayList();
	List resl = new ArrayList();
	List scorel = new ArrayList();
	bp = bp.back; // skip final MagicalState match
	while (bp != null) {
	    statel.add(bp.state);
	    resl.add(bp.residue);
	    scorel.add(DoubleAlphabet.getResidue(bp.score));
	    bp = bp.back;
	}
	Collections.reverse(statel);
	Collections.reverse(resl);
	Collections.reverse(scorel);
	Map labelToList = new HashMap();
	labelToList.put(StatePath.SEQUENCE,
			new SimpleResidueList(alpha, resl));
	labelToList.put(StatePath.STATES, 
			new SimpleResidueList(getModel().stateAlphabet(), statel));
	labelToList.put(StatePath.SCORES,
			new SimpleResidueList(DoubleAlphabet.getInstance(),
					      scorel));
	return new SimpleStatePath(col[l], labelToList);
    }

    private List ress = new ArrayList();
    private Object[][] matrix = new Object[2][2];
    private Object[][] bpMatrix = new Object[2][2];
    private Residue[][] resMatrix = new Residue[2][2];
    private int[] colId = new int[2];

    private void viterbiPrepareCol(int i, int j)
	throws IllegalResidueException, IllegalAlphabetException, IllegalTransitionException
    {
	// System.out.println("*** (" + i + "," + j + ")");

	ress.clear();
	ress.add(cursor.residue(0, i));
	ress.add(cursor.residue(1, j));
	// resMatrix[1][1] = alpha.getResidue(ress);
	resMatrix[1][1] = getResWrapper(alpha, ress);
	ress.clear();
	ress.add(cursor.residue(0, i));
	ress.add(AlphabetManager.instance().getGapResidue());
	// resMatrix[1][0] = alpha.getResidue(ress);
	resMatrix[1][0] = getResWrapper(alpha, ress);
	ress.clear();
	ress.add(AlphabetManager.instance().getGapResidue());
	ress.add(cursor.residue(1, j));
	// resMatrix[0][1] = alpha.getResidue(ress);
	resMatrix[0][1] = getResWrapper(alpha, ress);

	colId[0] = i;
	colId[1] = j;
	matrix[0][0] = cursor.getColumn(colId);
	bpMatrix[0][0] = cursor.getBackPointers(colId);
	colId[0]--;
	matrix[1][0] = cursor.getColumn(colId);
	bpMatrix[1][0] = cursor.getBackPointers(colId);
	colId[1]--;
	matrix[1][1] = cursor.getColumn(colId);
	bpMatrix[1][1] = cursor.getBackPointers(colId);
	colId[0]++;
	matrix[0][1] = cursor.getColumn(colId);
	bpMatrix[0][1] = cursor.getBackPointers(colId);

	viterbiCalcStepMatrix();
    }

    private void viterbiCalcStepMatrix()
	throws IllegalResidueException, IllegalAlphabetException, IllegalTransitionException
    {
	double[] curCol = (double[]) matrix[0][0];
	int[] advance;
	BackPointer[] curBPs = (BackPointer[]) bpMatrix[0][0];
	for (int l = 0; l < states.length; ++l) {
	    // System.out.println("State = " + states[l].getName());

	    if (states[l] instanceof EmissionState)
		advance = ((EmissionState) states[l]).getAdvance();
	    else
		advance = ia00;
	    Residue res = resMatrix[advance[0]][advance[1]];
	    double weight = Double.NEGATIVE_INFINITY;
	    if (res == null) {
		weight = Double.NEGATIVE_INFINITY;
	    } else if (! (states[l] instanceof EmissionState)) {
		weight = 0.0;
	    } else if (res == MagicalState.MAGICAL_RESIDUE) {
		try {
		    weight = ((EmissionState) states[l]).getWeight(res);
		} catch (Exception ex) {}
	    } else {
		weight = ((EmissionState) states[l]).getWeight(res);
	    }

	    if (weight == Double.NEGATIVE_INFINITY) {
		curCol[l] = Double.NEGATIVE_INFINITY;
		curBPs[l] = null;
	    } else {
		// System.out.println("weight = " + weight);
		double score = Double.NEGATIVE_INFINITY;
		int [] tr = transitions[l];
		double[] trs = transitionScores[l];

		// Calculate probabilities for states with transitions
		// here.
		
//		double[] lastCol = new double[states.length];
//  		for (int ci = 0; ci < states.length; ++ci) {
//  		    advance = states[ci].getAdvance();
//  		    double[] sCol = (double[]) matrix[advance[0]][advance[1]];
//  		    lastCol[ci] = sCol[ci];
//  		}

		double[] sourceScores = new double[tr.length];
		BackPointer[] oldBPs = new BackPointer[tr.length];
		for (int ci = 0; ci < tr.length; ++ci) {
		    double[] sCol;
		    BackPointer[] bpCol;
		    if (states[tr[ci]] instanceof EmissionState) {
			advance = ((EmissionState)states[tr[ci]]).getAdvance();
		        sCol = (double[]) matrix[advance[0]][advance[1]];
			bpCol = (BackPointer[]) bpMatrix[advance[0]][advance[1]];
		    } else {
			sCol = (double[]) matrix[0][0];
			bpCol = (BackPointer[]) bpMatrix[0][0];
		    }
		    sourceScores[ci] = sCol[tr[ci]];
		    oldBPs[ci] = bpCol[tr[ci]];
		}

		int bestKC = -1;
		for (int kc = 0; kc < tr.length; ++kc) {
		    // System.out.println("In from " + states[kc].getName());
		    // System.out.println("prevScore = " + sourceScores[kc]);

		    int k = tr[kc];
		    if (sourceScores[kc] != Double.NEGATIVE_INFINITY) {
			double t = trs[kc];
			double newScore = t + sourceScores[kc];
			if (newScore > score) {
			    score = newScore;
			    bestKC = kc;
			}
		    }
		}
		curCol[l] = weight + score;
		if (bestKC >= 0) {
		    curBPs[l] = new BackPointer(states[l], oldBPs[bestKC], curCol[l], res);
		} else {
		    curBPs[l] = null;
		}
		// System.out.println(curCol[l]);
	    }
	}
    }
}

private static class BackPointer {
    State state;
    BackPointer back;
    double score;
    Residue residue;
    
    BackPointer(State state, BackPointer back, double score, Residue residue) {
	this.state = state;
	this.back = back;
	this.score = score;
	this.residue = residue;
    }
}


private static interface PairDPCursor {
    public int getPos(int dim);
    public boolean canAdvance(int dim);
    public void advance(int dim);
    public Residue residue(int dim, int poz);
    public double[] getColumn(int[] coords);
    public double[] getCurrentColumn();
    public BackPointer[] getBackPointers(int[] coords);
}


private static class LightPairDPCursor implements PairDPCursor {
    private Object[] s1cur;
    private Object[] s1prev;
    private Object[] s2cur;
    private Object[] s2prev;

    private Object[] s1curBP;
    private Object[] s1prevBP;
    private Object[] s2curBP;
    private Object[] s2prevBP;

    private int[] pos;
    private ResidueList[] seqs;
    private int numStates;

    private double[] zeroCol;

    private boolean storeBPs;
    private BackPointer[] zeroColBP;

    public LightPairDPCursor(ResidueList seq1,
			ResidueList seq2,
			int states, boolean bp) {
	this.storeBPs = bp;
	numStates = states;
	
	s1cur = new Object[seq2.length() + 2];
	s1prev = new Object[seq2.length() + 2];
	s2cur = new Object[seq1.length() + 2];
	s2prev = new Object[seq1.length() + 2];

	if (storeBPs) {
	    s1curBP = new Object[seq2.length() + 2];
	    s1prevBP = new Object[seq2.length() + 2];
	    s2curBP = new Object[seq1.length() + 2];
	    s2prevBP = new Object[seq1.length() + 2];
	    zeroColBP = new BackPointer[numStates];
	    s1curBP[0] = s2curBP[0] = new BackPointer[numStates];
	}

	zeroCol = new double[numStates]; // don't touch this, please...
	for (int i = 0; i < zeroCol.length; ++i)
	    zeroCol[i] = Double.NEGATIVE_INFINITY;

	s1cur[0] = s2cur[0] = new double[numStates];
	

	pos = new int[2];
	pos[0] = pos[1] = 0;
	seqs = new ResidueList[2];
	seqs[0] = seq1;
	seqs[1] = seq2;
    }

    public int getPos(int dim) {
	return pos[dim];
    }

    public boolean canAdvance(int dim) {
	return (pos[dim] <= seqs[dim].length());
    }

    public void advance(int dim) {
	pos[dim]++;

	Object[] tmp;

	if (dim == 0) {
	    tmp = s1cur;
	    s1cur = s1prev;
	    s1prev = tmp;
	    for (int i = 0; i <= pos[1]; ++i) {
		if (s1cur[i] == null)
		    s1cur[i] = new double[numStates];
	    }
	    
	    if (storeBPs) {
		tmp = s1curBP;
		s1curBP = s1prevBP;
		s1prevBP = tmp;
		for (int i = 0; i <= pos[1]; ++i) {
		    if (s1curBP[i] == null)
			s1curBP[i] = new BackPointer[numStates];
		}
	    }
	} else if (dim == 1) {
	    tmp = s2cur;
	    s2cur = s2prev;
	    s2prev = tmp;
	    for (int i = 0; i <= pos[0]; ++i) {
		if (s2cur[i] == null)
		    s2cur[i] = new double[numStates];
	    }

	    if (storeBPs) {
		tmp = s2curBP;
		s2curBP = s2prevBP;
		s2prevBP = tmp;
		for (int i = 0; i <= pos[0]; ++i) {
		    if (s2curBP[i] == null)
			s2curBP[i] = new BackPointer[numStates];
		}
	    }
	}
    }

    public Residue residue(int dim, int poz) {
	if (poz == 0 || poz > seqs[dim].length())
	    return MagicalState.MAGICAL_RESIDUE;
	return seqs[dim].residueAt(poz);
    }


    public double[] getColumn(int[] coords) {
	double[] col = _getColumn(coords);
	if (col == null) {
	    System.out.println("getColumn Returning null: " + coords[0] + " " + coords[1]); 
	}
	return col;
    }

    public double[] _getColumn(int[] coords) {
//	System.out.println("!!! getting " + coords[0] + "," + coords[1]);

	if (coords[0] == -1 || coords[1] == -1)
	    return zeroCol;

//    	if (heavyMatrix[coords[0]][coords[1]] == null)
//    	    heavyMatrix[coords[0]][coords[1]] = new double[numStates];
//    	return (double[]) heavyMatrix[coords[0]][coords[1]];

    	if (coords[0] == pos[0] && (s1cur[coords[1]] != null)) {
  	    // System.out.println("??? s1cur");
    	    return (double[]) s1cur[coords[1]];
    	} else if (coords[1] == pos[1] && (s2cur[coords[0]] != null)) {
  	    // System.out.println("??? s2cur");
    	    return (double[]) s2cur[coords[0]];
    	} else if (coords[0] == pos[0] - 1 && (s1prev[coords[1]] != null)) {
  	    // System.out.println("??? s1prev");
    	    return (double[]) s1prev[coords[1]];
    	} else if (coords[1] == pos[1] - 1) {
  	    // System.out.println("??? s2prev");
    	    return (double[]) s2prev[coords[0]];
    	}
	
   	throw new NoSuchElementException();
    }

    public BackPointer[] getBackPointers(int[] coords) {
	if (!storeBPs)
	    throw new NoSuchElementException("This cursor isn't storing BackPointers.");

//	System.out.println("!!! getting " + coords[0] + "," + coords[1]);

	if (coords[0] == -1 || coords[1] == -1)
	    return zeroColBP;

    	if (coords[0] == pos[0] && (s1curBP[coords[1]] != null)) {
    	    return (BackPointer[]) s1curBP[coords[1]];
    	} else if (coords[1] == pos[1] && (s2curBP[coords[0]] != null)) {
    	    return (BackPointer[]) s2curBP[coords[0]];
    	} else if (coords[0] == pos[0] - 1 && (s1prevBP[coords[1]] != null)) {
    	    return (BackPointer[]) s1prevBP[coords[1]];
    	} else if (coords[1] == pos[1] - 1) {
    	    return (BackPointer[]) s2prevBP[coords[0]];
    	}
	
   	throw new NoSuchElementException();
    }
    

    public double[] getCurrentColumn() {
	throw new UnsupportedOperationException();
    }
}



private static class MatrixPairDPCursor implements PairDPCursor {
    private int[] pos;
    private ResidueList[] seqs;
    private int numStates;

    private double[] zeroCol;

    private double[][][] sMatrix;

    public MatrixPairDPCursor(ResidueList seq1,
			     ResidueList seq2,
			     PairDPMatrix matrix) {
	numStates = matrix.States().length;

	zeroCol = new double[numStates]; // don't touch this, please...
	for (int i = 0; i < zeroCol.length; ++i)
	    zeroCol[i] = Double.NEGATIVE_INFINITY;
	
	sMatrix = matrix.getScoreArray();

	pos = new int[2];
	pos[0] = pos[1] = 0;
	seqs = new ResidueList[2];
	seqs[0] = seq1;
	seqs[1] = seq2;
    }

    public int getPos(int dim) {
	return pos[dim];
    }

    public boolean canAdvance(int dim) {
	return (pos[dim] <= seqs[dim].length());
    }

    public void advance(int dim) {
	pos[dim]++;
    }

    public Residue residue(int dim, int poz) {
	if (poz == 0 || poz > seqs[dim].length())
	    return MagicalState.MAGICAL_RESIDUE;
	return seqs[dim].residueAt(poz);
    }

    public double[] getCurrentColumn() {
	throw new UnsupportedOperationException();
    }

    public double[] getColumn(int[] coords) {
//	System.out.println("!!! getting " + coords[0] + "," + coords[1]);

	if (coords[0] == -1 || coords[1] == -1)
	    return zeroCol;

    	return (double[]) sMatrix[coords[0]][coords[1]];
    }

    public BackPointer[] getBackPointers(int[] coords) {
	throw new NoSuchElementException("This cursor isn't storing BackPointers.");
    }
    
}

private static class BackMatrixPairDPCursor implements PairDPCursor {
    private int[] pos;
    private ResidueList[] seqs;
    private int numStates;

    private double[] zeroCol;

    private double[][][] sMatrix;

    public BackMatrixPairDPCursor(ResidueList seq1,
			     ResidueList seq2,
			     PairDPMatrix matrix) {
	numStates = matrix.States().length;

	zeroCol = new double[numStates]; // don't touch this, please...
	for (int i = 0; i < zeroCol.length; ++i)
	    zeroCol[i] = Double.NEGATIVE_INFINITY;
	
	sMatrix = matrix.getScoreArray();

	pos = new int[2];
	pos[0] = seq1.length() + 1;
	pos[1] = seq2.length() + 1;
	seqs = new ResidueList[2];
	seqs[0] = seq1;
	seqs[1] = seq2;
    }

    public int getPos(int dim) {
	return pos[dim];
    }

    public boolean canAdvance(int dim) {
	return (pos[dim] > 0);
    }

    public void advance(int dim) {
	pos[dim]--;
    }

    public Residue residue(int dim, int poz) {
	if (poz > seqs[dim].length() + 1)
	    return null;
	if (poz == 0 || poz > seqs[dim].length())
	    return MagicalState.MAGICAL_RESIDUE;
	return seqs[dim].residueAt(poz);
    }

    public double[] getColumn(int[] coords) {
//	System.out.println("!!! getting " + coords[0] + "," + coords[1]);

	if (coords[0] == -1 || coords[1] == -1 || coords[0] == seqs[0].length() + 2 || coords[1] == seqs[1].length() + 2)
	    return zeroCol;

    	return (double[]) sMatrix[coords[0]][coords[1]];
    }

    public double[] getCurrentColumn() {
	return (double[]) sMatrix[pos[0]][pos[1]];
    }

    public BackPointer[] getBackPointers(int[] coords) {
	throw new NoSuchElementException("This cursor isn't storing BackPointers.");
    }
    
}
}
