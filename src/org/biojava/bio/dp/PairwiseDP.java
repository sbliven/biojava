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
          advance[0] + ", " + advance[1] + " " +
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
  throws IllegalSymbolException,
  IllegalAlphabetException,
  IllegalTransitionException {
    return backwardMatrix(seqs).getScore();
  }

  public DPMatrix backwardMatrix(SymbolList[] seqs) 
  throws IllegalSymbolException,
  IllegalAlphabetException,
  IllegalTransitionException {
    if (seqs.length != 2) {
      throw new IllegalArgumentException("This DP object only runs on pairs.");
    }
    lockModel();
    Backward b = new Backward();
    PairDPMatrix matrix = new PairDPMatrix(this, seqs[0], seqs[1]);
    PairDPCursor cursor = new BackMatrixPairDPCursor(
      seqs[0], seqs[1],
      (CrossProductAlphabet) getModel().emissionAlphabet(),
      2, 2,
      matrix
    );
    double score = b.runBackward(cursor);
    unlockModel();
    matrix.setScore(score);
    return matrix;
  }

  public DPMatrix backwardMatrix(SymbolList[] seqs, DPMatrix d) 
  throws IllegalSymbolException,
  IllegalAlphabetException,
  IllegalTransitionException {
    return backwardMatrix(seqs);
  }

  private class Backward {
    private int[][] transitions;
    private double[][] transitionScores;
    private State[] states;
    private PairDPCursor cursor;
    private CrossProductAlphabet alpha;
    private boolean initializationHack;

    public double runBackward(
      PairDPCursor curs
    ) throws
      IllegalSymbolException,
      IllegalAlphabetException,
      IllegalTransitionException
    {
      states = getStates();
      cursor = curs;
      alpha = (CrossProductAlphabet) getModel().emissionAlphabet();
      transitions = getBackwardTransitions();
      transitionScores = getBackwardTransitionScores();


      // initialization
      initializationHack = true;
      Cell [][] cells = cursor.press();
      if(cursor.hasNext()) {
        cursor.next(cells);
        calcCell(cells);
      }
      initializationHack = false;
      while(cursor.hasNext()) {
        cursor.next(cells);
        calcCell(cells);
      }
      Cell currentCell = cells[0][0];
      // Terminate!

      int l = 0;
      while (states[l] != magicalState) {
        ++l;
      }

      return currentCell.scores[l];
    }

    private void calcCell(Cell [][] cells)
    throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
      double[] curCol = cells[0][0].scores;

     STATELOOP:      
      for (int l = states.length - 1; l >= 0; --l) {
        //System.out.println("State = " + states[l].getName());
        if(initializationHack && (states[l] instanceof EmissionState)) {
          if(states[l] == magicalState) {
            curCol[l] = 0.0;
          } else {
            curCol[l] = Double.NEGATIVE_INFINITY;
          }
          continue STATELOOP;
        }
        double score = 0.0;
        int [] tr = transitions[l];
        double[] trs = transitionScores[l];

        // Calculate probabilities for states with transitions
        // here.

  	    double[] sourceScores = new double[tr.length];
        for (int ci = 0; ci < tr.length; ++ci) {
          double[] sCol;
          double weight;

          int destI = tr[ci];
          State destS = states[destI];
          Cell targetCell;
          if (destS instanceof EmissionState) {
            int [] advance = ((EmissionState)destS).getAdvance();
            targetCell = cells[advance[0]][advance[1]];
            weight = targetCell.emissions[destI];
            sCol = targetCell.scores;
          } else {
            targetCell = cells[0][0];
            weight = 0.0;
          }
          sourceScores[ci] = targetCell.scores[destI] + weight;
        }

        // Find base for addition
        int ci = 0;
        while (ci < tr.length && sourceScores[ci] == Double.NEGATIVE_INFINITY) {
          ++ci;
        }
        double constant = (ci < tr.length) ? sourceScores[ci] : 0.0;
	    
  	    for (int kc = 0; kc < tr.length; ++kc) {
          //System.out.println("In from " + states[kc].getName());
          //System.out.println("prevScore = " + sourceScores[kc]);

          int k = tr[kc];
          if (sourceScores[kc] != Double.NEGATIVE_INFINITY) {
            double t = trs[kc];
            score += Math.exp(t + sourceScores[kc] - constant);
          }
        }
        curCol[l] = Math.log(score) + constant;
        //System.out.println(curCol[l]);
      }
    }
  }

    //
    // FORWARD
    // 

  public double forward(SymbolList[] seqs) 
  throws IllegalSymbolException,
  IllegalAlphabetException,
  IllegalTransitionException {
    if (seqs.length != 2) {
      throw new IllegalArgumentException("This DP object only runs on pairs.");
    }
    lockModel();
    Forward f = new Forward();
    PairDPCursor cursor = new LightPairDPCursor(
      seqs[0], seqs[1], (CrossProductAlphabet) getModel().emissionAlphabet(),
      2, 2, getStates().length
    );
    unlockModel();
    return f.runForward(cursor);
  }

  public DPMatrix forwardMatrix(SymbolList[] seqs) 
  throws
    IllegalSymbolException,
    IllegalAlphabetException,
    IllegalTransitionException
  {
    if (seqs.length != 2) {
      throw new IllegalArgumentException("This DP object only runs on pairs.");
    }
    lockModel();
    Forward f = new Forward();
    PairDPMatrix matrix = new PairDPMatrix(this, seqs[0], seqs[1]);
    PairDPCursor cursor = new MatrixPairDPCursor(
      seqs[0], seqs[1], (CrossProductAlphabet) getModel().emissionAlphabet(),
      2, 2, matrix
    );
    double score = f.runForward(cursor);
    matrix.setScore(score);
    unlockModel();
    return matrix;
  }

  public DPMatrix forwardMatrix(SymbolList[] seqs, DPMatrix d) 
  throws
    IllegalSymbolException,
    IllegalAlphabetException,
    IllegalTransitionException
  {
    return forwardMatrix(seqs);
  }

  private class Forward {
    private int[][] transitions;
    private double[][] transitionScores;
    private State[] states;
    private PairDPCursor cursor;
    private boolean initializationHack;

    public double runForward(PairDPCursor curs) 
        throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException
    {
      states = getStates();
      cursor = curs;

      transitions = getForwardTransitions();
      transitionScores = getForwardTransitionScores();

      Cell[][] cells = cursor.press();
      initializationHack = true;
      if(cursor.hasNext()) {
        cursor.next(cells);
        calcCell(cells);
      }
      initializationHack = false;
      while(cursor.hasNext()) {
        cursor.next(cells);
        calcCell(cells);
      }
      Cell currentCell = cells[0][0];

      // Terminate!

      int l = 0;
      while (states[l] != magicalState) {
        ++l;
      }
      
      return currentCell.scores[l];
    }

    private void calcCell(Cell [][] cells)
    throws
      IllegalSymbolException,
      IllegalAlphabetException,
      IllegalTransitionException
    {
      Cell curCell = cells[0][0];
      double[] curCol = curCell.scores;
      double[] emissions = curCell.emissions;

     STATELOOP:
      for (int l = 0; l < states.length; ++l) {
        //System.out.println("State = " + states[l].getName());
        try {
          if(initializationHack && (states[l] instanceof EmissionState)) {
            if(states[l] == magicalState) {
              curCol[l] = 0.0;
            } else {
              curCol[l] = Double.NEGATIVE_INFINITY;
            }
            //System.out.println("Initialized state to " + curCol[l]);
            continue STATELOOP;
          }
          
          int[] advance = (states[l] instanceof EmissionState)
            ? ((EmissionState)states[l]).getAdvance()
            : ia00;
          double[] sourceScores = cells[advance[0]][advance[1]].scores;
          
          //System.out.println("Calculating weight");
          double weight;
          if (! (states[l] instanceof EmissionState)) {
            weight = 0.0;
          } else {
            weight = emissions[l];
          }
          //System.out.println("weight = " + weight);

          if (weight == Double.NEGATIVE_INFINITY) {
            curCol[l] = Double.NEGATIVE_INFINITY;
          } else {
            double score = 0.0;
            int [] tr = transitions[l];
            double[] trs = transitionScores[l];

            // Calculate probabilities for states with transitions
            // here.
		
            // Find base for addition
            int ci = 0;
            while (ci < tr.length && sourceScores[ci] == Double.NEGATIVE_INFINITY) {
              ++ci;
            }
            double constant = (ci < tr.length) ? sourceScores[ci] : 0.0;

            for (int kc = 0; kc < tr.length; ++kc) {
              //System.out.println("In from " + states[kc].getName());
              //System.out.println("prevScore = " + sourceScores[kc]);

              int k = tr[kc];
              if (sourceScores[k] != Double.NEGATIVE_INFINITY) {
                double t = trs[kc];
                score += Math.exp(t + sourceScores[k] - constant);
              }
            }
            curCol[l] = weight + Math.log(score) + constant;
            //System.out.println("Setting col score to " + curCol[l]);
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

  //
  // VITERBI!
  //

  public StatePath viterbi(SymbolList[] seqs) 
  throws
    IllegalSymbolException,
    IllegalAlphabetException,
    IllegalTransitionException
  {
    if (seqs.length != 2) {
      throw new IllegalArgumentException("This DP object only runs on pairs.");
    }
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
    private boolean initializationHack = true;
    private BackPointer TERMINAL_BP;

    public StatePath runViterbi(SymbolList seq0, SymbolList seq1) 
    throws
      IllegalSymbolException,
      IllegalAlphabetException,
      IllegalTransitionException
    {
      State magic = getModel().magicalState();
      TERMINAL_BP = new BackPointer(magic);
      states = getStates();
      cursor = new LightPairDPCursor(
        seq0, seq1, (CrossProductAlphabet) getModel().emissionAlphabet(),
        2, 2, states.length
      );
      
      transitions = getForwardTransitions();
      transitionScores = getForwardTransitionScores();
      
      initializationHack = true;
      Cell[][] cells = cursor.press();
      if(cursor.hasNext()) {
        cursor.next(cells);
        calcCell(cells);
      }
      initializationHack = false;
      while(cursor.hasNext()) {
        cursor.next(cells);
        calcCell(cells);
      }
      Cell currentCell = cells[0][0];
  
      // Terminate!

      int l = 0;
      while (states[l] != magicalState) {
        ++l;
      }

      // Traceback...  
	
      BackPointer[] bpCol = currentCell.backPointers;
      BackPointer bp = bpCol[l];
      List statel = new ArrayList();
      List scorel = new ArrayList();
      GappedSymbolList gap0 = new GappedSymbolList(seq0);
      GappedSymbolList gap1 = new GappedSymbolList(seq1);
      int i0 = seq0.length()+1;
      int i1 = seq1.length()+1;
      DoubleAlphabet dAlpha = DoubleAlphabet.getInstance();
  
      // parse 1
      //System.out.println("Parse 1");
      for(BackPointer bpi =	bp.back; bpi != TERMINAL_BP; bpi = bpi.back) {
        try {
        /*System.out.print(
          "Processing " + bpi.state.getName()
        );*/
        statel.add(bpi.state);
        if(bpi.state instanceof EmissionState) { 
          int [] advance = ((EmissionState) bpi.state).getAdvance();
          //System.out.print( "\t" + advance[0] + " " + advance[1]);
          if(advance[0] == 0) {
            gap0.addGapInSource(i0);
            //System.out.println(gap0.seqString());
            //System.out.print("\t-");
          } else {
            i0--;
      	    //System.out.print("\t" + seq0.symbolAt(i0).getToken());
          }
          if(advance[1] == 0) {
            gap1.addGapInSource(i1);
            //System.out.println(gap1.seqString());
            //System.out.print(" -");
          } else {
            i1--;
      	    //System.out.print(" " + seq1.symbolAt(i1).getToken());
          }
        }
        //System.out.println("\tat " + i0 + ", " + i1);
        } catch (IndexOutOfBoundsException ie) {
          while(bpi != TERMINAL_BP) {
            //System.out.println(bpi.state.getName());
            bpi = bpi.back;
          }
          throw new BioError(ie); 
        }
      }
      //System.out.println(gap0.seqString());
      //System.out.println(gap1.seqString());
      double [] scoreA = new double[statel.size()];
      Map aMap = new HashMap();
      aMap.put(seq0, gap0);
      aMap.put(seq1, gap1);
      Alignment ali = new SimpleAlignment(aMap);
      GappedSymbolList gappedAli = new GappedSymbolList(ali);

      // parse 2
      //System.out.println("Parse 2");
      int di = statel.size()-1;
      int dj = ali.length()+1;
      for(BackPointer bpi = bp.back; bpi != TERMINAL_BP; bpi = bpi.back) {
        scoreA[di] = bpi.score;
        if(!(bpi.state instanceof EmissionState)) {
          gappedAli.addGapInSource(dj);
          dj--;
        }
        di--;
      }
  
      Collections.reverse(statel);
      SymbolList states = new SimpleSymbolList(getModel().stateAlphabet(), statel);
      SymbolList scores = DoubleAlphabet.fromArray(scoreA);
      return new SimpleStatePath(currentCell.scores[l], gappedAli, states, scores);
    }

    private void calcCell(Cell [][] cells)
    throws
      IllegalSymbolException,
      IllegalAlphabetException,
      IllegalTransitionException
    {
      Cell curCell = cells[0][0];
      double[] curCol = curCell.scores;
      BackPointer[] curBPs = curCell.backPointers;
      double[] emissions = curCell.emissions;
      /*(System.out.println(
        "Got symbols " + cells[0][0] + "->" +
        cells[0][0].symbols[0].getName() + ", " +
        cells[0][0].symbols[1].getName()
      );*/
      //System.out.println("Scores " + curCol);
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

          int [] advance = (states[l] instanceof EmissionState)
            ? ((EmissionState)states[l]).getAdvance()
            : ia00;
          Cell oldCell = cells[advance[0]][advance[1]];
          double[] sourceScores = oldCell.scores;
          BackPointer[] oldBPs = oldCell.backPointers;
          //System.out.println("sourceScores = " + sourceScores);
          //System.out.println("Calculating weight");
          double weight;
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

            int bestK = -1; // index into states[l]
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
                  bestK = k;
                  //System.out.println("New best source at " + kc);
                }
              }
            }
            if (bestK != -1) {
              curCol[l] = weight + score;
              /*System.out.println(
                "Creating " + states[bestK].getName() +
                " -> " + states[l].getName() +
                " (" + curCol[l] + ")"
              );*/
              try {
                State s = states[l];
                curBPs[l] = new BackPointer(
                  s,
                  oldBPs[bestK],
                  curCol[l]
                );
              } catch (Throwable t) {
                throw new BioError(
                  t,
                  "Couldn't generate backpointer for " + states[l].getName() +
                  " back to " + states[bestK].getName()
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
      /*System.out.println("backpointers:");
      for(int l = 0; l < states.length; l++) {
        BackPointer b = curBPs[l];
        if(b != null) {
          for(BackPointer bb = b; bb.back != bb; bb = bb.back) {
	    System.out.print(bb.state.getName() + " -> ");
          }
          System.out.println("!");
        }
      }*/
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
    
    public BackPointer(State s) {
      this.state = s;
      this.back = this;
      this.score = Double.NaN;
    }
  }

  /**
   * A single cell in the DP matrix;
   */
  private final static class Cell {
    public double [] scores;
    public BackPointer [] backPointers;
    public double [] emissions;
    
    public Cell() {
    }
  }

  /**
   * A cursor over a DP matrix
   */
  private static interface PairDPCursor {
    /** test wether the cursor can be advanced further */
    boolean hasNext();
    /** retrieve the next block of cells */
    void next(Cell [][] cells) throws IllegalSymbolException;
    /** press out a new correctly sized cell array */
    Cell [][] press();
    /** retrieve the depth of this cursor */
    int [] getDepth();
  }

  private class LightPairDPCursor implements PairDPCursor {
    protected CrossProductAlphabet alpha;
    protected int[] pos;
    protected boolean flip;
    protected SymbolList[] seqs;
    protected double[][][] columns;
    protected double[][][] emissions;
    protected BackPointer[][][] bPointers;
    protected int numStates;
    protected double[] zeroCol;
    protected BackPointer[] emptyBP;
    protected int[] depth;
    
    public LightPairDPCursor(
      SymbolList seq1,
      SymbolList seq2,
      CrossProductAlphabet alpha,
      int depth1,
      int depth2,
      int numStates
    ) throws IllegalSymbolException {
      this.numStates = numStates;
      this.alpha = alpha;
      this.zeroCol = new double[this.numStates]; // don't touch this, please...
      for (int i = 0; i < zeroCol.length; ++i) {
        this.zeroCol[i] = Double.NEGATIVE_INFINITY;
      }
      this.emptyBP = new BackPointer[numStates];
      this.pos = new int[2];
      this.pos[0] = 0;
      this.pos[1] = 0;
      this.seqs = new SymbolList[2];
      this.seqs[0] = seq1;
      this.seqs[1] = seq2;
      this.depth = new int[2];
      this.depth[0] = depth1;
      this.depth[1] = depth2;
      
      this.flip = this.seqs[1].length() > this.seqs[0].length();
      //System.out.println("flip=" + this.flip);
      if(flip) {
        this.columns =
          new double[depth[0]][seqs[1].length()+2][numStates];
        this.emissions =
          new double[depth[0]][seqs[1].length()+2][];
        this.bPointers =
          new BackPointer[depth[0]][seqs[1].length()+2][numStates];
      } else {
        this.columns =
          new double[depth[1]][seqs[0].length()+2][numStates];
        this.emissions =
          new double[depth[1]][seqs[0].length()+2][];
        this.bPointers =
          new BackPointer[depth[1]][seqs[0].length()+2][numStates];
      }
      
      for(int i = 0; i < columns.length; i++) {
        double [][] ci = columns[i];
        for(int j = 0; j < ci.length; j++) {
          double [] cj = ci[j];
          for(int k = 0; k < cj.length; k++) {
            cj[k] = Double.NEGATIVE_INFINITY;
          }
        }
      }
/*      for(int i = 1; i < emissions.length; i++) {
        double [][] ei = emissions[i];
        for(int j = 0; j < ei.length; j++) {
          double [] ej = ei[j];
          for(int k = 0; k < ej.length; k++) {
            ej[k] = Double.NEGATIVE_INFINITY;
          }
        }
      }*/
      calcEmissions(emissions[0]);
    }

    public int[] getDepth() {
      return depth;
    }
    
    public boolean hasNext() {
      int i = flip ? 0 : 1;
      return
        pos[i] <= seqs[i].length()+1;
    }
    
    public Cell[][] press() {
      Cell [][] cells = new Cell[depth[0]][depth[1]];
      for(int i = 0; i < cells.length; i++) {
        Cell [] ci = cells[i];
        for(int j = 0; j < ci.length; j++) {
          ci[j] = new Cell();
        }
      }
      return cells;
    }
    
    public void next(Cell[][] cells) throws IllegalSymbolException {
      //System.out.println("Pos=" + pos[0] + ", " + pos[1] + " " + flip);
      if(flip) {
        for(int i = 0; i < depth[0]; i++) {
          int ii = pos[0] - i;
          boolean outI = (ii < 0) || (ii > seqs[0].length()+1);
          Cell[] cellI = cells[i]; // lucky in this case - can pre-fetch cells
          double[][] columnsI = columns[i];
          double[][] emissionsI = emissions[i];
          BackPointer[][] bPointersI = bPointers[i];
          for(int j = 0; j < depth[1]; j++) {
            int jj = pos[1] - j;
            boolean outJ = (jj < 0) || (jj > seqs[1].length()+1);
            //System.out.println("at " + i + "->" + ii + ", " + j + "->" + jj);
            Cell c = cellI[j];
            if(outI || outJ) {
              c.scores = zeroCol;
              c.emissions = zeroCol;
              c.backPointers = emptyBP;
            } else {
              c.scores = columnsI[jj];
              c.emissions = emissionsI[jj];
              c.backPointers = bPointersI[jj];
            }
          }
        }
        if(pos[1] <= seqs[1].length()) {
          pos[1]++;
        } else {
          pos[1] = 0;
          pos[0]++;

          // advance arrays
          int depth = this.depth[0];
          double [][] tempC = columns[depth-1];
          double [][] tempE = emissions[depth-1];
          BackPointer [][] tempBP = bPointers[depth-1];
          for(int i = 1; i < depth; i++) {
            columns[i] = columns[i-1];
            emissions[i] = emissions[i-1];
            bPointers[i] = bPointers[i-1];
          }
          columns[0] = tempC;
          emissions[0] = tempE;
          bPointers[0] = tempBP;
          
          calcEmissions(tempE);
        }
      } else { // flip == false
        for(int i = 0; i < depth[1]; i++) {
          int ii = pos[1] - i;
          boolean outI = (ii < 0) || (ii > seqs[1].length()+1);
          //Cell[] cellI = cells[i]; // can't pre-fetch as loop wrong way
          double[][] columnsI = columns[i];
          double[][] emissionsI = emissions[i];
          BackPointer[][] bPointersI = bPointers[i];
          for(int j = 0; j < depth[0]; j++) {
            int jj = pos[0] - j;
            boolean outJ = (jj < 0) || (jj > seqs[0].length()+1);
            //System.out.println("at " + i + "->" + ii + ", " + j + "->" + jj);
            Cell c = cells[j][i];
            if(outI || outJ) {
              c.scores = zeroCol;
              c.emissions = zeroCol;
              c.backPointers = emptyBP;
            } else {
              c.scores = columnsI[jj];
              c.emissions = emissionsI[jj];
              c.backPointers = bPointersI[jj];
            }
          }
        }
        if(pos[0] <= seqs[0].length()) {
          pos[0]++;
        } else {
          pos[0] = 0;
          pos[1]++;
          
          // advance arrays
          int depth = this.depth[1];
          double [][] tempC = columns[depth-1];
          double [][] tempE = emissions[depth-1];
          BackPointer [][] tempBP = bPointers[depth-1];
          for(int i = 1; i < depth; i++) {
            columns[i] = columns[i-1];
            emissions[i] = emissions[i-1];
            bPointers[i] = bPointers[i-1];
          }
          columns[0] = tempC;
          emissions[0] = tempE;
          bPointers[0] = tempBP;

          calcEmissions(tempE);
        }
      }
    }
    
    private void calcEmissions(double[][] em)
    throws IllegalSymbolException {
      if(flip) {
        //System.out.println("Calculating emissions at " + pos[0] + ", " + pos[1]);
        Symbol [] symL = new Symbol[2];
        List symList = Arrays.asList(symL);
        int i = pos[0];
        symL[0] = (i < 1 || i > seqs[0].length())
          ? AlphabetManager.getGapSymbol()
          : seqs[0].symbolAt(i);
        for(int j = 0; j <= seqs[1].length()+1; j++) {
          symL[1] = (j < 1 || j > seqs[1].length())
            ? AlphabetManager.getGapSymbol()
            : seqs[1].symbolAt(j);
          em[j] = getEmission(symList, alpha);
          //System.out.println("symbol " + symL[0].getName() + ", " + symL[1].getName() + "->" + em[j]);
        }
      } else {
        //System.out.println("Calculating emissions at " + pos[0] + ", " + pos[1]);
        Symbol [] symL = new Symbol[2];
        List symList = Arrays.asList(symL);
        int j = pos[1];
        symL[1] = (j < 1 || j > seqs[1].length())
          ? AlphabetManager.getGapSymbol()
          : seqs[1].symbolAt(j);
        for(int i = 0; i <= seqs[0].length()+1; i++) {
          symL[0] = (i < 1 || i > seqs[0].length())
            ? AlphabetManager.getGapSymbol()
            : seqs[0].symbolAt(i);
          //System.out.println("symbol " + symL[0].getName() + ", " + symL[1].getName());
          em[i] = getEmission(symList, alpha);
        }
      }
    }
  }
  
  private abstract class AbstractMatrixPairDPCursor
  implements PairDPCursor {
    protected int[] pos;
    protected SymbolList[] seqs;
    protected double[][][] columns;
    protected BackPointer[][][] bPointers;
    protected double[][][] emissions;
    protected int numStates;
    protected double[] zeroCol;
    protected BackPointer[] emptyBP;
    protected int[] depth;
    protected double[][][] sMatrix;

    public AbstractMatrixPairDPCursor(
      SymbolList seq1,
      SymbolList seq2,
      CrossProductAlphabet alpha,
      int start1,
      int start2,
      int depth1,
      int depth2,
      PairDPMatrix matrix
    ) throws IllegalSymbolException {
      this.numStates = matrix.states().length;

      this.zeroCol = new double[this.numStates]; // don't touch this, please...
      for (int i = 0; i < zeroCol.length; ++i) {
        this.zeroCol[i] = Double.NEGATIVE_INFINITY;
      }
      this.emptyBP = new BackPointer[numStates];
      
      this.sMatrix = matrix.getScoreArray();

      this.pos = new int[2];
      this.pos[0] = start1;
      this.pos[1] = start2;
      this.seqs = new SymbolList[2];
      this.seqs[0] = seq1;
      this.seqs[1] = seq2;
      this.depth = new int[2];
      this.depth[0] = depth1;
      this.depth[1] = depth2;
      this.bPointers = new BackPointer[seq1.length()+2][seq2.length()+2][numStates];
      this.emissions = new double[seq1.length()+2][seq2.length()+2][];
      
      Symbol [] symArray = new Symbol[2];
      List symList = Arrays.asList(symArray);
      for(int i = 0; i <= seq1.length()+1; i++) {
        symArray[0] = (i < 1 || i > seq1.length())
          ? AlphabetManager.getGapSymbol()
          : seq1.symbolAt(i);
        double [][] ei = emissions[i];
        for(int j = 0; j <= seq2.length()+1; j++) {
          symArray[1] = (j < 1 || j > seq2.length())
            ? AlphabetManager.getGapSymbol()
            : seq2.symbolAt(j);
          ei[j] = getEmission(symList, alpha);
        }
      }
    }
    
    public int [] getDepth() {
      return depth;
    }

    public boolean hasNext() {
      return
        pos[0] <= (seqs[0].length()+1) ||
        pos[1] <= (seqs[1].length()+1);
    }
    
    public Cell[][] press() {
      Cell [][] cells = new Cell[depth[0]][depth[1]];
      for(int i = 0; i < cells.length; i++) {
        Cell [] ci = cells[i];
        for(int j = 0; j < ci.length; j++) {
          ci[j] = new Cell();
        }
      }
      return cells;
    }
  }
  
  private class MatrixPairDPCursor
  extends AbstractMatrixPairDPCursor {
    public MatrixPairDPCursor(
      SymbolList seq1,
      SymbolList seq2,
      CrossProductAlphabet alpha,
      int depth1,
      int depth2,
      PairDPMatrix matrix
    ) throws IllegalSymbolException {
      super(seq1, seq2, alpha, 0, 0, depth1, depth2, matrix);
    }
    
    public boolean hasNext() {
      return
        pos[1] <= (seqs[1].length()+1);
    }
    
    public void next(Cell[][] cells) {
      for(int i = 0; i < depth[0]; i++) {
        Cell[] cellI = cells[i];
        int ii = pos[0] - i;
        boolean outI = ii < 0 || ii > seqs[0].length()+1;
        if(outI) {
          for(int j = 0; j < depth[1]; j++) {
            Cell c = cellI[j];
            c.scores = zeroCol;
            c.emissions = zeroCol;
            c.backPointers = emptyBP;
          }
        } else {
          double[][] sMatI = this.sMatrix[ii];
          double[][] emisI = this.emissions[ii];
          BackPointer[][] bPI = this.bPointers[ii];
          for(int j = 0; j < depth[1]; j++) {
            int jj = pos[1] - j;
            boolean outJ = jj < 0 || jj > seqs[1].length()+1;
            Cell c = cellI[j];
            if(outJ) {
              c.scores = zeroCol;
              c.emissions = zeroCol;
              c.backPointers = emptyBP;
            } else {
              c.scores = sMatI[jj];
              c.emissions = emisI[jj];
              c.backPointers = bPI[jj];
            }
          }
        }
      }
      if(pos[0] <= seqs[0].length()) {
        pos[0]++;
      } else {
        pos[0] = 0;
        pos[1]++;
      }
    }
  }

  private class BackMatrixPairDPCursor
  extends AbstractMatrixPairDPCursor {
    public BackMatrixPairDPCursor(
      SymbolList seq1,
      SymbolList seq2,
      CrossProductAlphabet alpha,
      int depth1,
      int depth2,
      PairDPMatrix matrix
    ) throws IllegalSymbolException {
      super(
        seq1, seq2, alpha,
        seq1.length()+1, seq2.length()+1,
        depth1, depth2,
        matrix
      );
    }

    public boolean hasNext() {
      return
        pos[1] >= 0;
    }
    
    
    public void next(Cell[][] cells) {
      for(int i = 0; i < depth[0]; i++) {
        Cell[] cellI = cells[i];
        int ii = pos[0] + i;
        boolean outI = ii < 0 || ii > seqs[0].length()+1;
        if(outI) {
          for(int j = 0; j < depth[1]; j++) {
            Cell c = cellI[j];
            c.scores = zeroCol;
            c.emissions = zeroCol;
            c.backPointers = emptyBP;
          }
        } else {
          double[][] sMatI = this.sMatrix[ii];
          double[][] emisI = this.emissions[ii];
          BackPointer[][] bPI = this.bPointers[ii];
          for(int j = 0; j < depth[1]; j++) {
            int jj = pos[1] + j;
            boolean outJ = jj < 0 || jj > seqs[1].length()+1;
            Cell c = cellI[j];
            if(outJ) {
              c.scores = zeroCol;
              c.emissions = zeroCol;
              c.backPointers = emptyBP;
            } else {
              c.scores = sMatI[jj];
              c.emissions = emisI[jj];
              c.backPointers = bPI[jj];
            }
          }
        }
      }
      if(pos[0] > 0) {
        pos[0]--;
      } else {
        pos[0] = seqs[0].length()+1;
        pos[1]--;
      }
    }
  }
}
