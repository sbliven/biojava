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
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

class SingleDP extends DP {
  /**
    * Scores the SymbolList from symbol 1 to symbol columns with a weight
    * matrix.
    *
    * @param matrix  the weight matrix used to evaluate the sequences
    * @param resList the SymbolList to assess
    * @return  the log probability or likelyhood of this weight matrix
    *          having generated symbols 1 to columns of resList
    */
  public static double scoreWeightMatrix(WeightMatrix matrix, SymbolList resList)
  throws IllegalSymbolException {
    double score = 0;
    int cols = matrix.columns();

    for (int c = 0; c < cols; c++)
      score += matrix.getWeight(resList.symbolAt(c + 1), c);

    return score;
  }

  public SingleDP(MarkovModel flat)
  throws IllegalSymbolException, IllegalTransitionException, BioException {
    super(flat);
  }
  
  public double forward(SymbolList [] seq)
  throws IllegalSymbolException, IllegalAlphabetException, IllegalSymbolException {
    if(seq.length != 1) {
      throw new IllegalArgumentException("seq must be 1 long, not " + seq.length);
    }
    lockModel();
    DPCursor dpCursor = new SmallCursor(
      getStates(),
      seq[0],
      seq[0].iterator()
    );
    double score = forward(dpCursor);
    unlockModel();

    return score;
  }
  
  public double backward(SymbolList [] seq)
  throws IllegalSymbolException, IllegalAlphabetException, IllegalSymbolException {
    if(seq.length != 1) {
      throw new IllegalArgumentException("seq must be 1 long, not " + seq.length);
    }
    lockModel();
    DPCursor dpCursor = new SmallCursor(
      getStates(),
      seq[0],
      new ReverseIterator(seq[0])
    );
    double score = backward(dpCursor);
    unlockModel();
    
    return score;
  }

  public DPMatrix forwardMatrix(SymbolList [] seq)
  throws IllegalSymbolException, IllegalAlphabetException, IllegalSymbolException {
    if(seq.length != 1) {
      throw new IllegalArgumentException("seq must be 1 long, not " + seq.length);
    }
    
    lockModel();
    SingleDPMatrix matrix = new SingleDPMatrix(this, seq[0]);
    DPCursor dpCursor = new MatrixCursor(matrix, seq[0].iterator(), +1);
    matrix.score = forward(dpCursor);
    unlockModel();
    
    return matrix;
  }
  
  public DPMatrix backwardMatrix(SymbolList [] seq)
  throws IllegalSymbolException, IllegalAlphabetException, IllegalSymbolException {
    if(seq.length != 1) {
      throw new IllegalArgumentException("seq must be 1 long, not " + seq.length);
    }
    
    lockModel();
    SingleDPMatrix matrix = new SingleDPMatrix(this, seq[0]);
    DPCursor dpCursor = new MatrixCursor(matrix, new ReverseIterator(seq[0]), -1);
    matrix.score = backward(dpCursor);
    unlockModel();
    
    return matrix;
  }
  
  public DPMatrix forwardMatrix(SymbolList [] seq, DPMatrix matrix)
  throws IllegalArgumentException, IllegalSymbolException,
  IllegalAlphabetException, IllegalSymbolException {
    if(seq.length != 1) {
      throw new IllegalArgumentException("seq must be 1 long, not " + seq.length);
    }
    
    lockModel();
    SingleDPMatrix sm = (SingleDPMatrix) matrix;
    DPCursor dpCursor = new MatrixCursor(sm, seq[0].iterator(), +1);
    sm.score = forward(dpCursor);
    unlockModel();
    
    return sm;
  }

  public DPMatrix backwardMatrix(SymbolList [] seq, DPMatrix matrix)
  throws IllegalArgumentException, IllegalSymbolException,
  IllegalAlphabetException, IllegalSymbolException {
    if(seq.length != 1) {
      throw new IllegalArgumentException("seq must be 1 long, not " + seq.length);
    }
    
    lockModel();
    SingleDPMatrix sm = (SingleDPMatrix) matrix;
    DPCursor dpCursor = new MatrixCursor(sm, new ReverseIterator(seq[0]), -1);
    sm.score = backward(dpCursor);
    unlockModel();
    
    return sm;
  }

  private double forward(DPCursor dpCursor)
  throws IllegalSymbolException {
    forward_initialize(dpCursor);
    forward_recurse(dpCursor);
    return forward_termination(dpCursor);
  }

  private double backward(DPCursor dpCursor)
  throws IllegalSymbolException {
    backward_initialize(dpCursor);
    backward_recurse(dpCursor);
    return backward_termination(dpCursor);
  }

  private void forward_initialize(DPCursor dpCursor)
    throws IllegalSymbolException {
    double [] v = dpCursor.currentCol();
    State [] states = getStates();
    
    for (int l = 0; l < getDotStatesIndex(); l++) {
      if(states[l] == getModel().magicalState()) {
        v[l] = 0.0;
      } else {
        v[l] = Double.NEGATIVE_INFINITY;
      }
    }
    
    int [][] transitions = getForwardTransitions();
    double [][] transitionScore = getForwardTransitionScores();
    double [] currentCol = dpCursor.currentCol();
    for (int l = getDotStatesIndex(); l < states.length; l++) {
      double score = 0.0;
      int [] tr = transitions[l];
      double [] trs = transitionScore[l];
        
      int ci = 0;
      while(
        ci < tr.length  &&
        currentCol[tr[ci]] == Double.NEGATIVE_INFINITY
      ) {
        ci++;
      }
      double constant = (ci < tr.length) ? currentCol[tr[ci]] : 0.0;
        
      for(int kc = 0; kc < tr.length; kc++) {
        int k = tr[kc];

        if(currentCol[k] != Double.NEGATIVE_INFINITY) {
          double t = trs[kc];
          score += Math.exp(t + (currentCol[k] - constant));
        } else {
        }
      }
      currentCol[l] = Math.log(score) + constant;
    }
  }

  private void backward_initialize(DPCursor dpCursor)
    throws IllegalSymbolException {
    double [] v = dpCursor.currentCol();
    State [] states = getStates();

    for (int l = 0; l < states.length; l++) {
      if(states[l] == getModel().magicalState()) {
        v[l] = 0.0;
      } else {
        v[l] = Double.NEGATIVE_INFINITY;
      }
    }
  }

  private void forward_recurse(DPCursor dpCursor)
    throws IllegalSymbolException {
    State [] states = getStates();
    int stateCount = states.length;
    int [][] transitions = getForwardTransitions();
    double [][] transitionScore = getForwardTransitionScores();

    // int _index = 0;
    while (dpCursor.canAdvance()) {
      // _index++;
      // System.out.println("\n*** Index=" + _index + " ***");
      dpCursor.advance();
      Symbol res = dpCursor.currentRes();
//      System.out.println("Consuming " + res.getName());
      double [] currentCol = dpCursor.currentCol();
      double [] lastCol = dpCursor.lastCol();
      for (int l = 0; l < getDotStatesIndex(); l++) { //any -> emission
        double weight = ((EmissionState) states[l]).getWeight(res);
        if (weight == Double.NEGATIVE_INFINITY) {
          // System.out.println("*");
          currentCol[l] = Double.NEGATIVE_INFINITY;
        } else {
          double score = 0.0;
          int [] tr = transitions[l];
          double [] trs = transitionScore[l];
          // System.out.println("l=" + states[l].getName());
          int ci = 0;
          while (
            ci < tr.length &&
            lastCol[tr[ci]] == Double.NEGATIVE_INFINITY
          ) {
            ci++;
          }
          double constant = (ci < tr.length) ? lastCol[tr[ci]] : 0.0;

          for (int kc = 0; kc < tr.length; kc++) {
            int k = tr[kc];
            // System.out.println("k=" + states[k].getName());
            if (lastCol[k] != Double.NEGATIVE_INFINITY) {
              double t = trs[kc];
              //System.out.println("t=" + t);
              //System.out.println("lastCol[k]=" + lastCol[k]);
              score += Math.exp(t + (lastCol[k] - constant));
            } else {
              // System.out.println("-");
            }
          }
          // new_l = emission_l(res) * sum_k(transition(k, l) * old_k)
          currentCol[l] = (weight + Math.log(score)) + constant;
          //System.out.println("Weight " + weight);
          //System.out.println("score " + score + " = " + Math.log(score));
          //System.out.println("constant " + constant);
          //System.out.println("currentCol[" + states[l].getName() + "]=" + currentCol[l]);
        }
      }
      for(int l = getDotStatesIndex(); l < states.length; l++) { // all dot states from emissions
        double score = 0.0;
        int [] tr = transitions[l];
        double [] trs = transitionScore[l];
        
        int ci = 0;
        while(
          ci < tr.length  &&
          currentCol[tr[ci]] == Double.NEGATIVE_INFINITY
        ) {
          ci++;
        }
        double constant = (ci < tr.length) ? currentCol[tr[ci]] : 0.0;
        
        for(int kc = 0; kc < tr.length; kc++) {
          int k = tr[kc];

          if(currentCol[k] != Double.NEGATIVE_INFINITY) {
            double t = trs[kc];
            score += Math.exp(t + (currentCol[k] - constant));
          } else {
          }
        }
        currentCol[l] = Math.log(score) + constant;
        //System.out.println("currentCol[" + states[l].getName() + "]=" + currentCol[l]);
      }
    }
  }

  private void backward_recurse(DPCursor dpCursor)
    throws IllegalSymbolException {
    State [] states = getStates();
    int stateCount = states.length;
    int [][] transitions = getBackwardTransitions();
    double [][] transitionScore = getBackwardTransitionScores();

    while (dpCursor.canAdvance()) {
      dpCursor.advance();
      Symbol res = dpCursor.lastRes();
      double [] currentCol = dpCursor.currentCol();
      double [] lastCol = dpCursor.lastCol();
//System.out.println(res.getName());
      for (int k = stateCount-1; k >= 0; k--) {
//System.out.println("State " + k + " of " + stateCount + ", " + transitions.length);
//System.out.println(states[k].getName());
        int [] tr = transitions[k];
        double [] trs = transitionScore[k];
        double score = 0.0;
        int ci = 0;
        while (
          ci < tr.length &&
          lastCol[tr[ci]] == Double.NEGATIVE_INFINITY
        ) {
          ci++;
        }
        double constant = (ci < tr.length) ? lastCol[tr[ci]] : 0.0;
//System.out.println("Chosen constant: " + constant);
        for (int lc = tr.length-1; lc >= 0; lc--) { // any->emission
          int l = tr[lc];
          if(l >= getDotStatesIndex()) {
            continue;
          }
//System.out.println(states[k].getName() + " -> " + states[l].getName());
          double weight = ((EmissionState) states[l]).getWeight(res);
//System.out.println("weight = " + weight);
          if (
            lastCol[l] != Double.NEGATIVE_INFINITY &&
            weight != Double.NEGATIVE_INFINITY
          ) {
            double t = trs[lc];
            score += Math.exp(t + weight + (lastCol[l] - constant));
          }
        }
//System.out.println("Score = " + score);
        for(int lc = tr.length-1; lc >= 0; lc--) { // any->dot
          int l = tr[lc];
          if(l < getDotStatesIndex() || l <= k) {
            break;
          }
          /*System.out.println(
            "Processing dot-state transition " +
            states[k].getName() + " -> " + states[l].getName()
          );*/
          if(currentCol[l] != Double.NEGATIVE_INFINITY) {
            score += Math.exp(trs[lc] + (currentCol[l] - constant));
          }
        }
//System.out.println("Score = " + score);
        currentCol[k] = Math.log(score) + constant;
//System.out.println("currentCol = " + currentCol[k]);
      }
    }
  }

  private double forward_termination(DPCursor dpCursor)
    throws IllegalSymbolException {
    double [] scores = dpCursor.currentCol();
    State [] states = getStates();

    int l = 0;
    while (states[l] != getModel().magicalState())
      l++;

    return scores[l];
  }

  private double backward_termination(DPCursor dpCursor)
    throws IllegalSymbolException {
    double [] scores = dpCursor.currentCol();
    State [] states = getStates();

    int l = 0;
    while (states[l] != getModel().magicalState())
      l++;

    return scores[l];
  }
  
  public StatePath viterbi(SymbolList [] resList)
  throws IllegalSymbolException {
    SymbolList r = resList[0];
    DPCursor dpCursor = new SmallCursor(getStates(), r, r.iterator());
    return viterbi(dpCursor);
  }

  private StatePath viterbi(DPCursor dpCursor)
  throws IllegalSymbolException {
    lockModel();
    
    int seqLength = dpCursor.length();
    State [] states = getStates();

    int [][] transitions = getForwardTransitions();
    double [][] transitionScore = getForwardTransitionScores();
    int stateCount = states.length;

    BackPointer [] oldPointers = new BackPointer[stateCount];
    BackPointer [] newPointers = new BackPointer[stateCount];

    // initialize
    {
      double [] vc = dpCursor.currentCol();
      double [] vl = dpCursor.lastCol();
      for (int l = 0; l < stateCount; l++) {
        if(states[l] == getModel().magicalState()) {
          //System.out.println("Initializing start state to 0.0");
          vc[l] = vl[l] = 0.0;
          oldPointers[l] = newPointers[l] = new BackPointer(states[l], 1.0);
        } else {
          vc[l] = vl[l] = Double.NEGATIVE_INFINITY;
        }
      }
    }

    // viterbi
    while (dpCursor.canAdvance()) { // symbol i
      dpCursor.advance();
      Symbol res = dpCursor.currentRes();
      //System.out.println(res.getName());
      double [] currentCol = dpCursor.currentCol();
      double [] lastCol = dpCursor.lastCol();
      for (int l = 0; l < states.length; l++) {
        double emission;
        if(l < getDotStatesIndex()) {
          emission = ((EmissionState) states[l]).getWeight(res);
        } else {
          emission = 0.0;
        }
        int [] tr = transitions[l];
        //System.out.println("Considering " + tr.length + " alternatives");
        double [] trs = transitionScore[l];
        if (emission == Double.NEGATIVE_INFINITY) {
          //System.out.println(states[l].getName() + ": impossible emission");
          currentCol[l] = Double.NEGATIVE_INFINITY;
          newPointers[l] = null;
        } else {
          double transProb = Double.NEGATIVE_INFINITY;
          double trans = Double.NEGATIVE_INFINITY;
          int prev = -1;
          for (int kc = 0; kc < tr.length; kc++) {
            int k = tr[kc];
            double t = trs[kc];
            double s = (l < getDotStatesIndex()) ? lastCol[k] : currentCol[k];
            double p = t + s;
            /*System.out.println("Looking at scores from " + states[k].getName());
            System.out.println("Old = " + lastCol[k]);
            System.out.println("New = " + currentCol[k]);
            System.out.println(
              "Considering " + states[k].getName() + " -> " +
              states[l].getName() + ", " + t + " + " + s + " = " + p
            );*/
            if (p > transProb) {
              transProb = p;
              prev = k;
              trans = t;
            }
          }
          if(prev != -1) {
            currentCol[l] = transProb + emission;
            /*System.out.println(
              states[prev].getName() + "->" + states[l].getName() + ", " +
              (trans + emission)
            );*/
            newPointers[l] = new BackPointer(
              states[l],
              (l < getDotStatesIndex()) ? oldPointers[prev] : newPointers[prev],
              trans + emission
            );
            /*System.out.println("Succesfully completed " + states[l].getName());
            System.out.println("Old = " + lastCol[l]);
            System.out.println("New = " + currentCol[l]);*/
          } else {
            //System.out.println(states[l].getName() + ": Nowhere to come from");
            currentCol[l] = Double.NEGATIVE_INFINITY;
            newPointers[l] = null;
          }
        }
      }
      
      BackPointer [] bp = newPointers;
      newPointers = oldPointers;
      oldPointers = bp;
    }

    // find max in last row
    BackPointer best = null;
    double bestScore = Double.NaN;
    for (int l = 0; l < stateCount; l++) {
      if (states[l] == getModel().magicalState()) {
        best = oldPointers[l].back;
        bestScore = dpCursor.currentCol()[l];
        break;
      }
    }

    int len = 0;
    BackPointer b2 = best;
    int dotC = 0;
    int emC = 0;
    // trace back ruit to check out size of path
    while(b2.back != b2) {
      len++;
      if(b2.state instanceof EmissionState) {
        emC++;
      } else {
        dotC++;
      }
      b2 = b2.back;
    };

    GappedSymbolList resView = new GappedSymbolList(dpCursor.resList());
    double [] scores = new double[len];
    List stateList = new ArrayList(len);
    for (int j = 0; j < len; j++) {
      stateList.add(null);
    }

    b2 = best;
    int ri = dpCursor.resList().length()+1;
    int lc = len;
    int gaps = 0;
    while(b2.back != b2) {
      lc--;
      //System.out.println("At " + lc + " state=" + b2.state.getName() + ", score=" + b2.score + ", back=" + b2.back);
      if(b2.state instanceof MagicalState) {
        b2 = b2.back;
        continue;
      }
      stateList.set(lc, b2.state);
      if(b2.state instanceof DotState) {
        resView.addGapInSource(ri);
        gaps++;
      } else {
        ri--;
      }
      scores[lc] = b2.score;
      b2 = b2.back;
    }

    /*System.out.println("Counted " + emC + " emissions and " + dotC + " dots");
    System.out.println("Counted backpointers. Alignment of length " + len);
    System.out.println("Input list had length " + dpCursor.resList().length());
    System.out.println("Added gaps: " + gaps);
    System.out.println("Gapped view has length " + resView.length());*/

    Map labelToResList = new HashMap();
    labelToResList.put(StatePath.SEQUENCE, resView);
    labelToResList.put(StatePath.STATES,
                       new SimpleSymbolList(getModel().stateAlphabet(), stateList));
    labelToResList.put(StatePath.SCORES,
                       DoubleAlphabet.fromArray(scores));

    unlockModel();
    return new SimpleStatePath(bestScore, labelToResList);
  }

  private static class BackPointer {
    public State state;
    public BackPointer back;
    public double score;

    public BackPointer(State state, BackPointer back, double score) {
      if(back == null) {
        throw new BioError(
          "Attempted to make a backpointer with a null 'back' from state " +
          state.getName() + ", " + score
        );
      }
      this.state = state;
      this.back = back;
      this.score = score;
    }
    
    public BackPointer(State state, double score) {
      this.state = state;
      this.score = score;
      this.back = this;
    }
  }
}
