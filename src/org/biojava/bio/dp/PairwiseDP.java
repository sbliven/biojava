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

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;

/**
 * Algorithms for dynamic programming (alignments) between pairs
 * of SymbolLists.
 * Based on a single-head DP implementation by Matt Pocock.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */

public class PairwiseDP extends DP implements Serializable {
    private EmissionState magicalState;
    private HashMap emissions;

    public PairwiseDP(MarkovModel mm) throws IllegalSymbolException,
                                           IllegalTransitionException,
                                           BioException
    {
	super(mm);
	magicalState = mm.magicalState();
  emissions = new HashMap();
    }

    private final static int[] ia00 = {0, 0};

    //
    // BACKWARD
    //

  public void updateTransitions() {
    super.updateTransitions();
    // workaround for bug in vm
    if(emissions != null) {
      emissions.clear();
    }
  }
  
  private AlphabetManager.ListWrapper gopher =
    new AlphabetManager.ListWrapper();


  protected double [] getEmission(List symList, CrossProductAlphabet alpha)
  throws IllegalSymbolException {
    gopher.setList(symList);
    double [] emission = (double []) emissions.get(gopher);
    if(emission == null) {
      //System.out.print(".");
      Symbol sym[][] = new Symbol[2][2];
      List ll = new ArrayList(symList);
      Symbol gap = sym[0][0] = AlphabetManager.getGapSymbol();
      sym[1][1] = alpha.getSymbol(Arrays.asList(new Symbol [] {
        (Symbol) symList.get(0),
        (Symbol) symList.get(1)
      }));
      sym[1][0] = alpha.getSymbol(Arrays.asList(new Symbol [] {
        (Symbol) symList.get(0),
        gap
      }));
      sym[0][1] = alpha.getSymbol(Arrays.asList(new Symbol [] {
        gap,
        (Symbol) symList.get(1)
      }));
      int dsi = getDotStatesIndex();
      emission = new double[dsi];
      State [] states = getStates();
      for(int i = 0; i < dsi; i++) {
        EmissionState es = (EmissionState) states[i];
        int [] advance = es.getAdvance();
        Distribution dis = es.getDistribution();
        Symbol s = sym[advance[0]][advance[1]]; 
        emission[i] = Math.log(dis.getWeight(s));
        /*System.out.println(
          s.getName() + " " +
          es.getName() + " " +
          emission[i]
        );*/
      }
      emissions.put(new AlphabetManager.ListWrapper(ll), emission);
    } else {
      //System.out.print("-");
    }
    return emission;
  }

    public double backward(SymbolList[] seqs) 
        throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
      return backwardMatrix(seqs).getScore();
    }

    public DPMatrix backwardMatrix(SymbolList[] seqs) 
	throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
	if (seqs.length != 2)
	    throw new IllegalArgumentException("This DP object only runs on pairs.");

  lockModel();
	Backward f = new Backward();
	PairDPMatrix matrix = new PairDPMatrix(this, seqs[0], seqs[1]);
	PairDPCursor cursor = new BackMatrixPairDPCursor(seqs[0], seqs[1], matrix);
	double score = f.runBackward(seqs[0], seqs[1], cursor);
  unlockModel();
	matrix.setScore(score);
	return matrix;
    }

    public DPMatrix backwardMatrix(SymbolList[] seqs, DPMatrix d) 
	throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
	return backwardMatrix(seqs);
    }


  private class Backward {
    private int[][] transitions;
    private double[][] transitionScores;
    private State[] states;
    private PairDPCursor cursor;
    private CrossProductAlphabet alpha;

    public double runBackward(SymbolList seq0, SymbolList seq1, PairDPCursor curs) 
        throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
      states = getStates();
      cursor = curs;
      alpha = (CrossProductAlphabet) getModel().emissionAlphabet();

      // initialization

      double[] col = cursor.getCurrentColumn();
      for (int l = 0; l < states.length; ++l) {
        col[l] = (states[l] == magicalState)
          ? 0.0
	        : Double.NEGATIVE_INFINITY;
      }

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
      while (states[l] != magicalState) {
        ++l;
      }

      return col[l];
    }

    private List symL = new ArrayList();
    private double[][][] matrix = new double [2][2][];
    private double[][][] emission = new double [2][2][];
    private Symbol[][] symMatrix = new Symbol[2][2];
    private Symbol[][] symMatrixF = new Symbol[2][2];
    private int[] colId = new int[2];

    private void backwardPrepareCol(int i, int j)
    throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
      // System.out.println("*** (" + i + "," + j + ")");
      Symbol gap = AlphabetManager.getGapSymbol();
      symL.clear();

      symL.add(cursor.symbol(0, i+1));
      symL.add(cursor.symbol(1, j+1));
      symMatrix[1][1] = alpha.getSymbol(symL);
      symL.set(0, cursor.symbol(0, i + 1));
      symL.set(1, gap);
      symMatrix[1][0] = alpha.getSymbol(symL);
      symL.set(0, gap);
      symL.set(1, cursor.symbol(1, j + 1));
      symMatrix[0][1] = alpha.getSymbol(symL);

      symL.set(0, cursor.symbol(0, i));
      symL.set(1, cursor.symbol(1, j));
      symMatrixF[1][1] = alpha.getSymbol(symL);
      symL.add(cursor.symbol(0, i));
      symL.add(gap);
      symMatrixF[1][0] = alpha.getSymbol(symL);
      symL.add(gap);
      symL.add(cursor.symbol(1, j));
      symMatrixF[0][1] = alpha.getSymbol(symL);

      colId[0] = i;
      colId[1] = j;
      // System.out.println("***A ("+ colId[0] + "," + colId[1] +")");
      matrix[0][0] = cursor.getColumn(colId);
      emission[0][0] = cursor.getEmission(colId);
      colId[0]++;
      matrix[1][0] = cursor.getColumn(colId);
      emission[1][0] = cursor.getEmission(colId);
      colId[1]++;
      matrix[1][1] = cursor.getColumn(colId);
      emission[1][1] = cursor.getEmission(colId);
      colId[0]--;
      matrix[0][1] = cursor.getColumn(colId);
      emission[0][1] = cursor.getEmission(colId);

      double[] curECol = emission[0][0];
      for(int l = 0; l < getDotStatesIndex(); l++) {
        EmissionState es = (EmissionState) states[l];
        int [] advance = es.getAdvance();
        curECol[l] = Math.log(es.getDistribution().getWeight(symMatrixF[advance[0]][advance[1]]));
      }

      backwardCalcStepMatrix();
    }

    private void backwardCalcStepMatrix()
    throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
      double[] curCol = matrix[0][0];
      int[] advance;
      
      for (int l = states.length - 1; l >= 0; --l) {
        // System.out.println("State = " + states[l].getName());

        if (states[l] instanceof EmissionState) {
          advance = ((EmissionState) states[l]).getAdvance();
        } else {
        advance = ia00;
        }

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

          int destI = tr[ci];
          State destS = states[destI];
          if (destS instanceof EmissionState) {
            advance = ((EmissionState)destS).getAdvance();
            Symbol res = symMatrix[advance[0]][advance[1]];
            if (res == null) {
              weight = Double.NEGATIVE_INFINITY;
            } else if (! (destS instanceof EmissionState)) {
              weight = 0.0;
            } else {
              weight = emission[advance[0]][advance[1]][destI];
            }
            sCol = matrix[advance[0]][advance[1]];
          } else {
            sCol = matrix[0][0];
          }
          sourceScores[ci] = sCol[destI] + weight;
        }

        // Find base for addition
        int ci = 0;
        while (ci < tr.length && sourceScores[ci] == Double.NEGATIVE_INFINITY) {
          ++ci;
        }
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

    public double forward(SymbolList[] seqs) 
        throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
	if (seqs.length != 2)
	    throw new IllegalArgumentException("This DP object only runs on pairs.");
      lockModel();
	Forward f = new Forward();
	PairDPCursor cursor = new LightPairDPCursor(seqs[0], seqs[1], getStates().length, false);
  unlockModel();
	return f.runForward(seqs[0], seqs[1], cursor);
    }

    public DPMatrix forwardMatrix(SymbolList[] seqs) 
	throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
	if (seqs.length != 2)
	    throw new IllegalArgumentException("This DP object only runs on pairs.");
      lockModel();
	Forward f = new Forward();
	PairDPMatrix matrix = new PairDPMatrix(this, seqs[0], seqs[1]);
	PairDPCursor cursor = new MatrixPairDPCursor(seqs[0], seqs[1], matrix);
	double score = f.runForward(seqs[0], seqs[1], cursor);
	matrix.setScore(score);
  unlockModel();
	return matrix;
    }

    public DPMatrix forwardMatrix(SymbolList[] seqs, DPMatrix d) 
	throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
	return forwardMatrix(seqs);
    }

    private static List gappedResList = new ArrayList();

  private class Forward {
    private int[][] transitions;
    private double[][] transitionScores;
    private State[] states;
    private PairDPCursor cursor;
    private CrossProductAlphabet alpha;
    private boolean initializationHack = true;

    public double runForward(SymbolList seq0, SymbolList seq1, PairDPCursor curs) 
        throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
	states = getStates();
	cursor = curs;
	alpha = (CrossProductAlphabet) getModel().emissionAlphabet();

	transitions = getForwardTransitions();
	transitionScores = getForwardTransitionScores();

	// Forward initialization

	double[] col = cursor.getColumn(ia00);
	for (int l = 0; l < states.length; ++l)
	    col[l] = (states[l] == magicalState) ? 0.0 :
	                Double.NEGATIVE_INFINITY;
	forwardPrepareCol(0, 0);
	initializationHack = false;

	// Recurse

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

    private List symL = new ArrayList();
    private double[][][] matrix = new double[2][2][];
    private Symbol[][] symMatrix = new Symbol[2][2];
    private int[] colId = new int[2];

    private void forwardPrepareCol(int i, int j)
	throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
	// System.out.println("*** (" + i + "," + j + ")");
  Symbol gap = AlphabetManager.getGapSymbol();
	symL.clear();
  
	symL.add(cursor.symbol(0, i));
	symL.add(cursor.symbol(1, j));
	symMatrix[1][1] = alpha.getSymbol(symL);;
	symL.set(0, cursor.symbol(0, i));
	symL.set(1, gap);
	symMatrix[1][0] = alpha.getSymbol(symL);;
	symL.set(0, gap);
	symL.set(1, cursor.symbol(1, j));
	symMatrix[0][1] = alpha.getSymbol(symL);

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
    throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
      double[] curCol = matrix[0][0];
      int[] advance;

      for (int l = 0; l < states.length; ++l) {
        if (initializationHack && states[l] == magicalState) {
          continue;
        }

        // System.out.println("State = " + states[l].getName());

        double weight = Double.NEGATIVE_INFINITY;

        if (states[l] instanceof EmissionState) {
          advance = ((EmissionState) states[l]).getAdvance();
          Symbol res = symMatrix[advance[0]][advance[1]];
          weight = Math.log(((EmissionState) states[l]).getDistribution().getWeight(res));
        } else {
          advance = ia00;
          weight = 0.0;
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
		
      		double[] sourceScores = new double[tr.length];
          double[] sCol = matrix[advance[0]][advance[1]];
          for (int ci = 0; ci < tr.length; ++ci) {
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

    public StatePath viterbi(SymbolList[] seqs) 
        throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
	if (seqs.length != 2)
	    throw new IllegalArgumentException("This DP object only runs on pairs.");

      lockModel();
	Viterbi v = new Viterbi();
	StatePath sp = v.runViterbi(seqs[0], seqs[1]);
  unlockModel();
  return sp;
    }


private class Viterbi { 
    private int[][] transitions;
    private double[][] transitionScores;
    private State[] states;
    private PairDPCursor cursor;
    private CrossProductAlphabet alpha;
    private boolean initializationHack = true;

    public StatePath runViterbi(SymbolList seq0, SymbolList seq1) 
        throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
      State magic = getModel().magicalState();
	states = getStates();
	cursor = new LightPairDPCursor(seq0, seq1, states.length, true);
	alpha = (CrossProductAlphabet) getModel().emissionAlphabet();
  
	// Forward initialization

	transitions = getForwardTransitions();
	transitionScores = getForwardTransitionScores();

	double[] col = cursor.getColumn(ia00);
  initializationHack = true;
  viterbiPrepareCol(0, 0);
  initializationHack = false;
  
	// Recurse
  
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
	List scorel = new ArrayList();
  GappedSymbolList gap0 = new GappedSymbolList(seq0);
  GappedSymbolList gap1 = new GappedSymbolList(seq1);
  int i0 = seq0.length();
  int i1 = seq1.length();
  DoubleAlphabet dAlpha = DoubleAlphabet.getInstance();
  
  // parse 1
  System.out.println("Got back-pointer " + bp);
  System.out.println("Got back " + bp.back);
  System.out.println("Parse 1");
	for(BackPointer bpi =	bp.back; bpi.back != TERMINAL_BP; bpi = bpi.back) {
    System.out.println("Processing " + bpi.state.getName() + " at + " + i0 + ", " + i1);
	  statel.add(bpi.state);
    if(bpi.state instanceof EmissionState) {
      int [] advance = ((EmissionState) bpi.state).getAdvance();
      if(advance[0] == 0) {
        gap0.addGapInSource(i0);
      } else {
        i0--;
      }
      if(advance[1] == 0) {
        gap1.addGapInSource(i1);
      } else {
        i1--;
      }
    }
	}
  
  double [] scoreA = new double[statel.size()];
  Map aMap = new HashMap();
  aMap.put(seq0, gap0);
  aMap.put(seq1, gap1);
  Alignment ali = new SimpleAlignment(aMap);
  GappedSymbolList gappedAli = new GappedSymbolList(ali);
  // parse 2
  System.out.println("Parse 2");
  int di = statel.size()-1;
  int dj = ali.length();
	for(BackPointer bpi =	bp.back; bpi.back != TERMINAL_BP; bpi = bpi.back) {
    scoreA[di] = bpi.score;
    if(!(bpi.state instanceof EmissionState)) {
      gappedAli.addGapInSource(dj);
      dj--;
    }
    di--;
  }
  
	Collections.reverse(statel);
  SymbolList states =	new SimpleSymbolList(getModel().stateAlphabet(), statel);
  SymbolList scores = DoubleAlphabet.fromArray(scoreA);
	return new SimpleStatePath(col[l], gappedAli, states, scores);
    }

    private List symL = new ArrayList();
    private double[][][] matrix = new double[2][2][];
    private BackPointer[][][] bpMatrix = new BackPointer[2][2][];
    private int[] colId = new int[2];
    private BackPointer TERMINAL_BP = new BackPointer();

    private void viterbiPrepareCol(int i, int j)
	throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
      //System.out.println("Calculating " + i + ", " + j);
	symL.clear();
	symL.add(cursor.symbol(0, i));
	symL.add(cursor.symbol(1, j));
  /*System.out.println(
    "Symbols " + cursor.symbol(0, i).getName() +
    ", " + cursor.symbol(1, j).getName()
  );*/
  double [] col = getEmission(symL, alpha);

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

  try {
	viterbiCalcStepMatrix(col);
  } catch (Exception e) {
    throw new BioError(e, "Couldn't calculate dp cell " + i + ", " + j);
  } catch (BioError e) {
    throw new BioError(e, "Error calculating dp cell " + i + ", " + j);
  }
    }

    private void viterbiCalcStepMatrix(double [] emissions)
    throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
      double[] curCol = matrix[0][0];
      BackPointer[] curBPs = bpMatrix[0][0];
      
      //System.out.println("curCol=" + curCol);
     STATELOOP:
      for (int l = 0; l < states.length; ++l) {
  	    //System.out.println("State = " + l + "=" + states[l].getName());

        try {
          //System.out.println("trying initialization");
          if(initializationHack && (states[l] instanceof EmissionState)) {
            if(states[l] == magicalState) {
              curCol[l] = 0.0;
              curBPs[l] = TERMINAL_BP;
            } else {
              curCol[l] = Double.NEGATIVE_INFINITY;
              curBPs[l] = null;
            }
            //System.out.println("Initialized state to " + curCol[l]);
            continue STATELOOP;
          }
    
          //System.out.println("Calculating weight");
          double weight = Double.NEGATIVE_INFINITY;
          if (! (states[l] instanceof EmissionState)) {
            weight = 0.0;
          } else {
            weight = emissions[l];
          }
          //System.out.println("weight = " + weight);

          if (weight == Double.NEGATIVE_INFINITY) {
            //System.out.println("Not reachable");
            curCol[l] = Double.NEGATIVE_INFINITY;
            curBPs[l] = null;
          } else {
            double score = Double.NEGATIVE_INFINITY;
            int [] tr = transitions[l];
            double[] trs = transitionScores[l];

		        double[] sourceScores = new double[tr.length];
            BackPointer[] oldBPs = new BackPointer[tr.length];
            int [] advance = (states[l] instanceof EmissionState)
              ? ((EmissionState)states[l]).getAdvance()
              : ia00;
            sourceScores = matrix[advance[0]][advance[1]];
            oldBPs = bpMatrix[advance[0]][advance[1]];

            int bestKC = -1; // index into states[l]
            for (int kc = 0; kc < tr.length; ++kc) {
              int k = tr[kc]; // actual state index

              //System.out.println("kc is " + kc);
              //System.out.println("with from " + k + "=" + states[k].getName());
              //System.out.println("prevScore = " + sourceScores[k]);
              if (sourceScores[k] != Double.NEGATIVE_INFINITY) {
                double t = trs[kc];
                double newScore = t + sourceScores[k];
                if (newScore > score) {
                  score = newScore;
                  bestKC = k;
                  //System.out.println("New best source at " + kc);
                }
              }
            }
            if (bestKC != -1) {
              /*System.out.println(
                "Using index " + bestKC +
                " with backpointer at " + oldBPs[bestKC] +
                " from state " + states[bestKC]
              );*/
              curCol[l] = weight + score;
              try {
                State s = states[l];
                curBPs[l] = new BackPointer(
                  s,
                  oldBPs[bestKC],
                  curCol[l]
                );
              } catch (Throwable t) {
                throw new BioError(
                  t,
                  "Couldn't generate backpointer for " + states[l].getName() +
                  " back to " + states[tr[bestKC]].getName()
                );
              }
            } else {
              curBPs[l] = null;
              curCol[l] = Double.NEGATIVE_INFINITY;
            }
            // System.out.println(curCol[l]);
          }
        } catch (Exception e) {
          throw new BioError(
            e,
            "Problem with state " + l + " -> " + states[l].getName()
          );
        } catch (BioError e) {
          throw new BioError(
            e,
            "Error  with state " + l + " -> " + states[l].getName()
          );
        }
      }
    }
  }

  private static class BackPointer {
    final public State state;
    final public BackPointer back;
    final public double score;
    
    public BackPointer(State state, BackPointer back, double score) {
      this.state = state;
      this.back = back;
      this.score = score;
      if(back == null) {
        throw new NullPointerException(
          "Can't construct backpointer for state " + state.getName() +
          " with a null source"
        );
      }
    }
    
    public BackPointer() {
      this.state = null;
      this.back = null;
      this.score = Double.NaN;
    }
  }


  private static interface PairDPCursor {
    int getPos(int dim);
    boolean canAdvance(int dim);
    void advance(int dim);
    Symbol symbol(int dim, int poz);
    double[] getColumn(int[] coords);
    double[] getCurrentColumn();
    BackPointer[] getBackPointers(int[] coords);
    double[] getEmission(int[] coords);
  }

  private static class LightPairDPCursor implements PairDPCursor {
    private double[][] s1cur;
    private double[][] s1prev;
    private double[][] s2cur;
    private double[][] s2prev;

    private BackPointer[][] s1curBP;
    private BackPointer[][] s1prevBP;
    private BackPointer[][] s2curBP;
    private BackPointer[][] s2prevBP;
    
    private int[] pos;
    private SymbolList[] seqs;
    private int numStates;

    private double[] zeroCol;

    private boolean storeBPs;
    private BackPointer[] zeroColBP;

    public LightPairDPCursor(
      SymbolList seq1,
			SymbolList seq2,
			int states, boolean bp
    ) {
      this.storeBPs = bp;
      numStates = states;
	
    	s1cur = new double[seq2.length() + 2][];
      s1prev = new double[seq2.length() + 2][];
      s2cur = new double[seq1.length() + 2][];
      s2prev = new double[seq1.length() + 2][];

      if (storeBPs) {
        s1curBP = new BackPointer[seq2.length() + 2][];
        s1prevBP = new BackPointer[seq2.length() + 2][];
        s2curBP = new BackPointer[seq1.length() + 2][];
        s2prevBP = new BackPointer[seq1.length() + 2][];
        zeroColBP = new BackPointer[numStates];
        s1curBP[0] = s2curBP[0] = new BackPointer[numStates];
      }

      zeroCol = new double[numStates]; // don't touch this, please...
      for (int i = 0; i < zeroCol.length; ++i) {
        zeroCol[i] = Double.NEGATIVE_INFINITY;
      }

      s1cur[0] = s2cur[0] = new double[numStates];
	

    	pos = new int[2];
      pos[0] = pos[1] = 0;
      seqs = new SymbolList[2];
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

      double[][] tmpS;
      BackPointer[][] tmpBP;

      if (dim == 0) {
        tmpS = s1cur;
        s1cur = s1prev;
        s1prev = tmpS;
        for (int i = 0; i <= pos[1]; ++i) {
          if (s1cur[i] == null) {
            s1cur[i] = new double[numStates];
          }
        }
	    
  	    if (storeBPs) {
          tmpBP = s1curBP;
          s1curBP = s1prevBP;
          s1prevBP = tmpBP;
          for (int i = 0; i <= pos[1]; ++i) {
            if (s1curBP[i] == null) {
              s1curBP[i] = new BackPointer[numStates];
            }
          }
        }
      } else if (dim == 1) {
        tmpS = s2cur;
        s2cur = s2prev;
        s2prev = tmpS;
        for (int i = 0; i <= pos[0]; ++i) {
          if (s2cur[i] == null) {
            s2cur[i] = new double[numStates];
          }
        }

        if (storeBPs) {
          tmpBP = s2curBP;
          s2curBP = s2prevBP;
          s2prevBP = tmpBP;
          for (int i = 0; i <= pos[0]; ++i) {
            if (s2curBP[i] == null) {
              s2curBP[i] = new BackPointer[numStates];
            }
          }
        }
      }
    }

    public Symbol symbol(int dim, int poz) {
      if (poz == 0 || poz > seqs[dim].length()) {
        return AlphabetManager.getGapSymbol();
      } else {
        return seqs[dim].symbolAt(poz);
      }
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

      if (coords[0] == -1 || coords[1] == -1) {
        return zeroCol;
      }

//    	if (heavyMatrix[coords[0]][coords[1]] == null)
//    	    heavyMatrix[coords[0]][coords[1]] = new double[numStates];
//    	return (double[]) heavyMatrix[coords[0]][coords[1]];

    	if (coords[0] == pos[0] && (s1cur[coords[1]] != null)) {
  	    // System.out.println("??? s1cur");
    	  return s1cur[coords[1]];
    	} else if (coords[1] == pos[1] && (s2cur[coords[0]] != null)) {
  	    // System.out.println("??? s2cur");
    	  return s2cur[coords[0]];
    	} else if (coords[0] == pos[0] - 1 && (s1prev[coords[1]] != null)) {
  	    // System.out.println("??? s1prev");
    	  return s1prev[coords[1]];
    	} else if (coords[1] == pos[1] - 1) {
  	    // System.out.println("??? s2prev");
    	  return s2prev[coords[0]];
    	}
	
     	throw new NoSuchElementException();
    }

    public BackPointer[] getBackPointers(int[] coords) {
      if (!storeBPs) {
        throw new NoSuchElementException(
          "This cursor isn't storing BackPointers."
        );
      }

//	System.out.println("!!! getting " + coords[0] + "," + coords[1]);

    	if (coords[0] == -1 || coords[1] == -1) {
        return zeroColBP;
      }

    	if (coords[0] == pos[0] && (s1curBP[coords[1]] != null)) {
    	  return s1curBP[coords[1]];
    	} else if (coords[1] == pos[1] && (s2curBP[coords[0]] != null)) {
    	  return s2curBP[coords[0]];
    	} else if (coords[0] == pos[0] - 1 && (s1prevBP[coords[1]] != null)) {
    	  return s1prevBP[coords[1]];
    	} else if (coords[1] == pos[1] - 1) {
    	  return s2prevBP[coords[0]];
    	}
	
     	throw new NoSuchElementException();
    }
    
    public double[] getCurrentColumn() {
      throw new UnsupportedOperationException();
    }
    
    public double[] getEmission(int[] coords) {
      throw new UnsupportedOperationException();
    }
  }

  private static class MatrixPairDPCursor implements PairDPCursor {
    private int[] pos;
    private SymbolList[] seqs;
    private int numStates;

    private double[] zeroCol;

    private double[][][] sMatrix;

    public MatrixPairDPCursor(
      SymbolList seq1,
			SymbolList seq2,
			PairDPMatrix matrix
    ) {
      numStates = matrix.states().length;

      zeroCol = new double[numStates]; // don't touch this, please...
      for (int i = 0; i < zeroCol.length; ++i) {
        zeroCol[i] = Double.NEGATIVE_INFINITY;
      }
	
    	sMatrix = matrix.getScoreArray();

      pos = new int[2];
      pos[0] = pos[1] = 0;
      seqs = new SymbolList[2];
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

    public Symbol symbol(int dim, int poz) {
      if (poz == 0 || poz > seqs[dim].length()) {
        return AlphabetManager.getGapSymbol();
      } else {
        return seqs[dim].symbolAt(poz);
      }
    }

    public double[] getCurrentColumn() {
      throw new UnsupportedOperationException();
    }

    public double[] getColumn(int[] coords) {
      //	System.out.println("!!! getting " + coords[0] + "," + coords[1]);

      if (coords[0] == -1 || coords[1] == -1) {
        return zeroCol;
      } else {
        return (double[]) sMatrix[coords[0]][coords[1]];
      }
    }

    public BackPointer[] getBackPointers(int[] coords) {
      throw new NoSuchElementException("This cursor isn't storing BackPointers.");
    }
    
    public double[] getEmission(int[] coords) {
      throw new UnsupportedOperationException();
    }
  }

  private class BackMatrixPairDPCursor implements PairDPCursor {
    private int[] pos;
    private SymbolList[] seqs;
    private int numStates;

    private double[] zeroCol;
    private double[] zeroECol;

    private double[][][] sMatrix;
    private double[][][] eMatrix;

    public BackMatrixPairDPCursor(
      SymbolList seq1,
      SymbolList seq2,
      PairDPMatrix matrix
    ) {
      State[] states = matrix.states();
      numStates = states.length;

      zeroCol = new double[numStates]; // don't touch this, please...
      zeroECol = new double[getDotStatesIndex()];
      
      for (int i = 0; i < zeroCol.length; ++i) {
        zeroCol[i] = Double.NEGATIVE_INFINITY;
      }
      for (int i = 0; i < zeroECol.length; i++) {
        zeroECol[i] = Double.NEGATIVE_INFINITY;
      }

    	sMatrix = matrix.getScoreArray();
      eMatrix = new double[sMatrix.length][sMatrix[0].length][getDotStatesIndex()];
      
      pos = new int[2];
      pos[0] = seq1.length() + 1;
      pos[1] = seq2.length() + 1;
      seqs = new SymbolList[2];
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

    public Symbol symbol(int dim, int poz) {
      if (poz > seqs[dim].length() + 1) {
        return null;
      }
      if (poz == 0 || poz > seqs[dim].length()) {
        return AlphabetManager.getGapSymbol();
      }
      return seqs[dim].symbolAt(poz);
    }

    public double[] getColumn(int[] coords) {
      //	System.out.println("!!! getting " + coords[0] + "," + coords[1]);

      if (
        coords[0] == -1 ||
        coords[1] == -1 ||
        coords[0] == seqs[0].length() + 2 ||
        coords[1] == seqs[1].length() + 2
      ) {
        return zeroCol;
      } else {
        return sMatrix[coords[0]][coords[1]];
      }
    }
    
    public double[] getEmission(int[] coords) {
      if(
        coords[0] == -1 ||
        coords[1] == -1 ||
        coords[0] == seqs[0].length() + 2 ||
        coords[1] == seqs[1].length() + 2
      ) {
        return zeroECol;
      } else {
        return eMatrix[coords[0]][coords[1]];
      }
    }

    public double[] getCurrentColumn() {
      return (double[]) sMatrix[pos[0]][pos[1]];
    }

    public BackPointer[] getBackPointers(int[] coords) {
      throw new NoSuchElementException("This cursor isn't storing BackPointers.");
    }
  }
}
