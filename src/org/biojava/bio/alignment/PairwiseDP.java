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


package org.biojava.bio.alignment;

import java.util.*;

import org.biojava.bio.BioError;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

/**
 * First pass at pairwise dynamic programming.
 * Based on a single-head DP implementation by Matt Pocock.
 *
 * @author Thomas Down
 */

public class PairwiseDP {
    public final static State MAGICAL_STATE =  new MagicalState('?', DP.MAGICAL_RESIDUE);

    public static EmissionState [] stateList(Alphabet alpha)
	throws IllegalResidueException 
    {
	return (EmissionState [])
            alpha.residues().toList().toArray(new EmissionState[0]);
    }

    public static int [][] forwardTransitions(MarkovModel model,
      EmissionState [] states) throws IllegalResidueException {
    int stateCount = states.length;
    int [][] transitions = new int[stateCount][];

    for (int i = 0; i < stateCount; i++) {
      int [] tmp = new int[stateCount];
      int len = 0;
      Set trans = model.transitionsTo(states[i]);
      // System.out.println("trans has size " + trans.size());
      for (int j = 0; j < stateCount; j++) {
        if (trans.contains(states[j])) {
          // System.out.println(states[j].getName() + " -> " + states[i].getName());
          tmp[len++] = j;
        }
      }
      int [] tmp2 = new int[len];
      for (int j = 0; j < len; j++)
        tmp2[j] = tmp[j];
      transitions[i] = tmp2;
    }

    return transitions;
  }

      public static double [][] forwardTransitionScores(MarkovModel model,
      EmissionState [] states,
      int [][] transitions) throws IllegalResidueException {
    int stateCount = states.length;
    double [][] scores = new double[stateCount][];

    for (int i = 0; i < stateCount; i++) {
      State is = states[i];
      scores[i] = new double[transitions[i].length];
      for (int j = 0; j < scores[i].length; j++) {
        try {
          scores[i][j] = model.getTransitionScore(states[transitions[i][j]], is);
        } catch (IllegalTransitionException ite) {
          throw new BioError(ite,
            "Transition listed in transitions array has dissapeared.");
        }
        // System.out.println(states[transitions[i][j]].getName() + " -> " + states[i].getName()
        //                   + " = " + scores[i][j]);
      }
    }

    return scores;
  }

    private static Residue getResWrapper(CrossProductAlphabet a, List l) 
        throws IllegalAlphabetException
    {
//  	System.out.println(a.getName() + " getting:");
//  	for (Iterator i = l.iterator(); i.hasNext(); ) {
//  	    Residue blah = (Residue) i.next();
//  	    System.out.println(blah.getName());
//  	}

	if (l.contains(DP.MAGICAL_RESIDUE)) {
	    for (Iterator i = l.iterator(); i.hasNext(); )
		if (i.next() != DP.MAGICAL_RESIDUE)
		    return null;
	    return DP.MAGICAL_RESIDUE;
	}
	return a.getResidue(l);
    }

    private final static int[] ia00 = {0, 0};

    public double forward(MarkovModel mm, ResidueList seq0, ResidueList seq1) 
        throws Exception
    {
	Forward f = new Forward();
	return f.runForward(mm, seq0, seq1);
    }

private class Forward {
    private int[][] transitions;
    private double[][] transitionScores;
    private EmissionState[] states;
    private PairDPCursor cursor;
    private CrossProductAlphabet alpha;

    public double runForward(MarkovModel mm, ResidueList seq0, ResidueList seq1) 
        throws Exception
    {
	states = stateList(mm.stateAlphabet());
	cursor = new PairDPCursor(seq0, seq1, states.length, false);
	alpha = (CrossProductAlphabet) mm.queryAlphabet();

	// Forward initialization

	double[] col = cursor.getColumn(ia00);
	for (int l = 0; l < states.length; ++l)
	    col[l] = (states[l] == PairwiseDP.MAGICAL_STATE) ? 0.0 :
	                Double.NEGATIVE_INFINITY;

	// Recurse

	transitions = forwardTransitions(mm, states);
	transitionScores = forwardTransitionScores(mm, states, transitions);

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
	while (states[l] != PairwiseDP.MAGICAL_STATE)
	    ++l;

	return col[l];
    }

    private List ress = new ArrayList();
    private Object[][] matrix = new Object[2][2];
    private Residue[][] resMatrix = new Residue[2][2];
    private int[] colId = new int[2];

    private void forwardPrepareCol(int i, int j)
	throws Exception
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
	throws Exception
    {
	double[] curCol = (double[]) matrix[0][0];
	for (int l = 0; l < states.length; ++l) {
	    // System.out.println("State = " + states[l].getName());

	    int[] advance = states[l].getAdvance();
	    Residue res = resMatrix[advance[0]][advance[1]];
	    double weight = Double.NEGATIVE_INFINITY;
	    if (res == null) {
		weight = Double.NEGATIVE_INFINITY;
	    } else if (res == DP.MAGICAL_RESIDUE) {
		try {
		    weight = states[l].getWeight(res);
		} catch (Exception ex) {}
	    } else {
		weight = states[l].getWeight(res);
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
		    advance = states[tr[ci]].getAdvance();
		    double[] sCol = (double[]) matrix[advance[0]][advance[1]];
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


    // VITERBI!

    public double viterbi(MarkovModel mm, ResidueList s0, ResidueList s1) 
        throws Exception
    {
	Viterbi v = new Viterbi();
	return v.runViterbi(mm, s0, s1);
    }


private class Viterbi { 
    public double runViterbi(MarkovModel mm,
			      ResidueList seq0,
			      ResidueList seq1) 
        throws Exception
    {
	EmissionState[] states = stateList(mm.stateAlphabet());
	PairDPCursor cursor = new PairDPCursor(seq0, seq1, states.length, true);
	CrossProductAlphabet alpha = (CrossProductAlphabet) mm.queryAlphabet();

	// Forward initialization

	double[] col = cursor.getColumn(ia00);
	for (int l = 0; l < states.length; ++l)
	    col[l] = (states[l] == PairwiseDP.MAGICAL_STATE) ? 0.0 :
	                Double.NEGATIVE_INFINITY;

	// Recurse

	int[][] transitions = forwardTransitions(mm, states);
	double[][] transitionScores = forwardTransitionScores(mm, states, transitions);

	while (cursor.canAdvance(0) || cursor.canAdvance(1)) {
	    if (cursor.canAdvance(0)) {
		cursor.advance(0);
		for (int i = 0; i <= cursor.getPos(1); ++i) {
		    viterbiPrepareCol(cursor, cursor.getPos(0), i,
					 states, transitions,
					 transitionScores, alpha);
		}
	    }

	    if (cursor.canAdvance(1)) {
		cursor.advance(1);
		for (int i = 0; i <= cursor.getPos(0); ++i) {
		    viterbiPrepareCol(cursor, i, cursor.getPos(1),
					 states, transitions,
					 transitionScores, alpha);
		}
	    }
	}  

	// Terminate!

	int[] colId = new int[2];
	colId[0] = cursor.getPos(0);
	colId[1] = cursor.getPos(1);
	col = cursor.getColumn(colId);
	int l = 0;
	while (states[l] != PairwiseDP.MAGICAL_STATE)
	    ++l;

	// Traceback...  
	
	BackPointer[] bpCol = (BackPointer[]) cursor.getBackPointers(colId);
	BackPointer bp = bpCol[l];
	List statel = new ArrayList();
	while (bp != null) {
	    statel.add(bp.state);
	    bp = bp.back;
	}
	
	System.out.println("*** Viterbi traceback ***");
	for (int tb = statel.size() - 1; tb >= 0; --tb)
	    System.out.println(((State) statel.get(tb)).getName());
	System.out.println("*** End ***");

	return col[l];
    }

    private void viterbiPrepareCol(PairDPCursor cursor,
				   int i, int j,
				   EmissionState[] states,
				   int[][] transitions,
				   double[][] transitionScores,
				   CrossProductAlphabet alpha)
	throws Exception
    {
	List ress = new ArrayList();
	Object[][] stepMatrix = new Object[2][2];
	Object[][] bpMatrix = new Object[2][2];
	Residue[][] resMatrix = new Residue[2][2];
	int[] colId = new int[2];

	// System.out.println("*** (" + i + "," + j + ")");

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
	stepMatrix[0][0] = cursor.getColumn(colId);
	bpMatrix[0][0] = cursor.getBackPointers(colId);
	colId[0]--;
	stepMatrix[1][0] = cursor.getColumn(colId);
	bpMatrix[1][0] = cursor.getBackPointers(colId);
	colId[1]--;
	stepMatrix[1][1] = cursor.getColumn(colId);
	bpMatrix[1][1] = cursor.getBackPointers(colId);
	colId[0]++;
	stepMatrix[0][1] = cursor.getColumn(colId);
	bpMatrix[0][1] = cursor.getBackPointers(colId);

	viterbiCalcStepMatrix(stepMatrix, states, resMatrix, bpMatrix,
			      transitions, transitionScores);
    }

    private void viterbiCalcStepMatrix(Object[][] matrix,
				       EmissionState[] states,
				       Residue[][] resMatrix,
				       Object[][] bpMatrix,
				       int[][] transitions,
				       double[][] transitionScores)
	throws Exception
    {
	double[] curCol = (double[]) matrix[0][0];
	BackPointer[] curBPs = (BackPointer[]) bpMatrix[0][0];
	for (int l = 0; l < states.length; ++l) {
	    // System.out.println("State = " + states[l].getName());

	    int[] advance = states[l].getAdvance();
	    Residue res = resMatrix[advance[0]][advance[1]];
	    double weight = Double.NEGATIVE_INFINITY;
	    if (res == null) {
		weight = Double.NEGATIVE_INFINITY;
	    } else if (res == DP.MAGICAL_RESIDUE) {
		try {
		    weight = states[l].getWeight(res);
		} catch (Exception ex) {}
	    } else {
		weight = states[l].getWeight(res);
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
		    advance = states[tr[ci]].getAdvance();
		    double[] sCol = (double[]) matrix[advance[0]][advance[1]];
		    BackPointer[] bpCol = (BackPointer[]) bpMatrix[advance[0]][advance[1]];
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
		    curBPs[l] = new BackPointer(states[l], oldBPs[bestKC], curCol[l]);
		} else {
		    curBPs[l] = null;
		}
		// System.out.println(curCol[l]);
	    }
	}
    }
}


    private static class MagicalState implements EmissionState {
	private char c;
	private Residue r;
	private int[] advance = {1, 1};
	
	public MagicalState(char c, Residue r) {
	    this.c = c;
	    this.r = r;
	}

	public char getSymbol() {
	    return c;
	}

	public String getName() {
	    return c + "";
	}

	public Annotation getAnnotation() {
	    return Annotation.EMPTY_ANNOTATION;
	}

	public Alphabet alphabet() {
	    return DP.MAGICAL_ALPHABET;
	}

	public double getWeight(Residue r) throws IllegalResidueException {
	    if (r != this.r)
		return Double.NEGATIVE_INFINITY;
	    return 0;
	}

	public void setWeight(Residue r, double w) throws IllegalResidueException,
	UnsupportedOperationException {
	    alphabet().validate(r);
	    throw new UnsupportedOperationException(
						    "The weights are immutable: " + r.getName() + " -> " + w);
	}

	public Residue sampleResidue() {
	    return r;
	}

	public void registerWithTrainer(ModelTrainer modelTrainer) {
	}

	public int[] getAdvance() {
	    return advance;
	}
    }    
}

class BackPointer {
    State state;
    BackPointer back;
    double score;
    
    BackPointer(State state, BackPointer back, double score) {
	this.state = state;
	this.back = back;
	this.score = score;
    }
}

class PairDPCursor {
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

    public PairDPCursor(ResidueList seq1,
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
	    return DP.MAGICAL_RESIDUE;
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
    	} else if (coords[1] == pos[1]) {
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
    	} else if (coords[1] == pos[1]) {
    	    return (BackPointer[]) s2curBP[coords[0]];
    	} else if (coords[0] == pos[0] - 1 && (s1prevBP[coords[1]] != null)) {
    	    return (BackPointer[]) s1prevBP[coords[1]];
    	} else if (coords[1] == pos[1] - 1) {
    	    return (BackPointer[]) s2prevBP[coords[0]];
    	}
	
   	throw new NoSuchElementException();
    }
    
}
