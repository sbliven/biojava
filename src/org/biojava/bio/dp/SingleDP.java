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
import org.biojava.bio.seq.*;

class SingleDP extends DP {
  /**
    * Scores the ResidueList from residue 1 to residue columns with a weight
    * matrix.
    *
    * @param matrix  the weight matrix used to evaluate the sequences
    * @param resList the ResidueList to assess
    * @return  the log probability or likelyhood of this weight matrix
    *          having generated residues 1 to columns of resList
    */
  public static double scoreWeightMatrix(WeightMatrix matrix, ResidueList resList)
  throws IllegalResidueException {
    double score = 0;
    int cols = matrix.columns();

    for (int c = 0; c < cols; c++)
      score += matrix.getWeight(resList.residueAt(c + 1), c);

    return score;
  }

  public SingleDP(FlatModel flat)
  throws IllegalResidueException, IllegalTransitionException {
    super(flat);
  }
  
  public double forward(ResidueList [] seq)
  throws IllegalResidueException, IllegalAlphabetException, IllegalResidueException {
    if(seq.length != 1) {
      throw new IllegalArgumentException("seq must be 1 long, not " + seq.length);
    }
    DPCursor dpCursor = new SmallCursor(
      getStates(),
      seq[0],
      seq[0].iterator()
    );
    return forward(dpCursor);
  }
  
  public double backward(ResidueList [] seq)
  throws IllegalResidueException, IllegalAlphabetException, IllegalResidueException {
    if(seq.length != 1) {
      throw new IllegalArgumentException("seq must be 1 long, not " + seq.length);
    }
    DPCursor dpCursor = new SmallCursor(
      getStates(),
      seq[0],
      new ReverseIterator(seq[0])
    );
    return backward(dpCursor);
  }

  public DPMatrix forwardMatrix(ResidueList [] seq)
  throws IllegalResidueException, IllegalAlphabetException, IllegalResidueException {
    if(seq.length != 1) {
      throw new IllegalArgumentException("seq must be 1 long, not " + seq.length);
    }
    
    SingleDPMatrix matrix = new SingleDPMatrix(this, seq[0]);
    DPCursor dpCursor = new MatrixCursor(matrix, seq[0].iterator(), +1);
    matrix.score = forward(dpCursor);
    
    return matrix;
  }
  
  public DPMatrix backwardMatrix(ResidueList [] seq)
  throws IllegalResidueException, IllegalAlphabetException, IllegalResidueException {
    if(seq.length != 1) {
      throw new IllegalArgumentException("seq must be 1 long, not " + seq.length);
    }
    
    SingleDPMatrix matrix = new SingleDPMatrix(this, seq[0]);
    DPCursor dpCursor = new MatrixCursor(matrix, new ReverseIterator(seq[0]), -1);
    matrix.score = backward(dpCursor);
    
    return matrix;
  }
  
  public DPMatrix forwardMatrix(ResidueList [] seq, DPMatrix matrix)
  throws IllegalArgumentException, IllegalResidueException,
  IllegalAlphabetException, IllegalResidueException {
    if(seq.length != 1) {
      throw new IllegalArgumentException("seq must be 1 long, not " + seq.length);
    }
    
    SingleDPMatrix sm = (SingleDPMatrix) matrix;
    DPCursor dpCursor = new MatrixCursor(sm, seq[0].iterator(), +1);
    sm.score = forward(dpCursor);
    
    return sm;
  }

  public DPMatrix backwardMatrix(ResidueList [] seq, DPMatrix matrix)
  throws IllegalArgumentException, IllegalResidueException,
  IllegalAlphabetException, IllegalResidueException {
    if(seq.length != 1) {
      throw new IllegalArgumentException("seq must be 1 long, not " + seq.length);
    }
    
    SingleDPMatrix sm = (SingleDPMatrix) matrix;
    DPCursor dpCursor = new MatrixCursor(sm, new ReverseIterator(seq[0]), -1);
    sm.score = backward(dpCursor);
    
    return sm;
  }

  private double forward(DPCursor dpCursor)
  throws IllegalResidueException {
    forward_initialize(dpCursor);
    forward_recurse(dpCursor);
    return forward_termination(dpCursor);
  }

  private double backward(DPCursor dpCursor)
  throws IllegalResidueException {
    backward_initialize(dpCursor);
    backward_recurse(dpCursor);
    return backward_termination(dpCursor);
  }

  private void forward_initialize(DPCursor dpCursor)
    throws IllegalResidueException {
    double [] v = dpCursor.currentCol();
    State [] states = getStates();

    // new_l = transition(start, l)
    for (int l = 0; l < states.length; l++) {
      v[l] = (states[l] == getModel().magicalState()) ? 0.0 : Double.NEGATIVE_INFINITY;
    }
  }

  private void backward_initialize(DPCursor dpCursor)
    throws IllegalResidueException {
    double [] v = dpCursor.currentCol();
    State [] states = getStates();

    // new_l = transition(start, l)
    for (int l = 0; l < states.length; l++) {
      v[l] = (states[l] == getModel().magicalState()) ? 0.0 : Double.NEGATIVE_INFINITY;
    }
  }

  private void forward_recurse(DPCursor dpCursor)
    throws IllegalResidueException {
    State [] states = getStates();
    int stateCount = states.length;
    int [][] transitions = getForwardTransitions();
    double [][] transitionScore = getForwardTransitionScores();

    // int _index = 0;
    while (dpCursor.canAdvance()) {
      // _index++;
      // System.out.println("\n*** Index=" + _index + " ***");
      dpCursor.advance();
      Residue res = dpCursor.currentRes();
      double [] currentCol = dpCursor.currentCol();
      double [] lastCol = dpCursor.lastCol();
      for (int l = 0; l < getDotStatesIndex(); l++) { // all emission states
        // state l for residue i  sum(p(k->l))
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
          while (ci < tr.length &&
              lastCol[tr[ci]] == Double.NEGATIVE_INFINITY) {
            ci++;
          }
          double constant = (ci < tr.length) ? lastCol[tr[ci]] : 0.0;

          for (int kc = 0; kc < tr.length; kc++) {
            int k = tr[kc];
            // System.out.println("k=" + states[k].getName());
            if (lastCol[k] != Double.NEGATIVE_INFINITY) {
              double t = trs[kc];
              // System.out.println("t=" + t);
              score += Math.exp(t + lastCol[k] - constant);
            } else {
              // System.out.println("-");
            }
          }
          // new_l = emission_l(res) * sum_k(transition(k, l) * old_k)
          currentCol[l] = weight + Math.log(score) + constant;
          // System.out.println("currentCol[" + states[l].getName() + "]=" + currentCol[l]);
        }
      }
      for(int l = getDotStatesIndex(); l < states.length; l++) { // all dot states from emissions
        double score = 0.0;
        int [] tr = transitions[l];
        double [] trs = transitionScore[l];
        
        int ci = 0;
        while(
          ci < tr.length  &&
          tr[ci] < getDotStatesIndex() &&
          currentCol[tr[ci]] == Double.NEGATIVE_INFINITY
        ) {
          ci++;
        }
        double constant = (ci < tr.length) ? lastCol[tr[ci]] : 0.0;
        
        for(int kc = 0; kc < tr.length; kc++) {
          int k = tr[kc];
          if(k >= getDotStatesIndex()) {
            break;
          }

          if(currentCol[k] != Double.NEGATIVE_INFINITY) {
            double t = trs[kc];
            score += Math.exp(t + currentCol[k] - constant);
          } else {
          }
        }
        currentCol[l] = Math.log(score) + constant;
      }
      for(int l = getDotStatesIndex(); l < states.length; l++) { // all dot states to each other
        double score = 0.0;
        int [] tr = transitions[l];
        double [] trs = transitionScore[l];
        
        int ci = tr.length-1;
        while(
          ci >= getDotStatesIndex()  &&
          lastCol[tr[ci]] == Double.NEGATIVE_INFINITY
        ) {
          ci--;
        }
        double constant = (ci < tr.length) ? currentCol[tr[ci]] : 0.0;
        
        for(int kc = 0; kc < tr.length; kc++) {
          int k = tr[kc];
          if(k < getDotStatesIndex()) {
            continue;
          }
          if(k >= l) {
            break;
          }
          if(currentCol[k] != Double.NEGATIVE_INFINITY) {
            double t = trs[kc];
            score += Math.exp(t + currentCol[k] - constant);
          } else {
          }
        }
        currentCol[l] += Math.log(score) + constant;
      }
    }
  }

  private void backward_recurse(DPCursor dpCursor)
    throws IllegalResidueException {
    State [] states = getStates();
    int stateCount = states.length;
    int [][] transitions = getBackwardTransitions();
    double [][] transitionScore = getBackwardTransitionScores();

    while (dpCursor.canAdvance()) {
      dpCursor.advance();
      Residue res = dpCursor.lastRes();
      double [] currentCol = dpCursor.currentCol();
      double [] lastCol = dpCursor.lastCol();

      for (int k = 0; k < stateCount; k++) {
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

        for (int lc = 0; lc < tr.length; lc++) {
          int l = tr[lc];
          if(l >= getDotStatesIndex()) {
            break;
          }
          double weight = ((EmissionState) states[l]).getWeight(res);
          if (currentCol[l] != Double.NEGATIVE_INFINITY &&
              weight != Double.NEGATIVE_INFINITY) {
            double t = trs[lc];
            score += Math.exp(t + currentCol[l] + weight - constant);
          }
        }
        for(int lc = tr.length-1; lc >= 0; lc--) {
          int l = tr[lc];
          if(l < getDotStatesIndex()) {
            break;
          }
          if(currentCol[l] != Double.NEGATIVE_INFINITY) {
            score += Math.exp(trs[lc] + currentCol[l] - constant);
          }
        }
        // new_k = sum_l( transition(k, l) * old_l * emission_l(res) )
        currentCol[k] = Math.log(score) + constant;
      }
    }
  }

  private double forward_termination(DPCursor dpCursor)
    throws IllegalResidueException {
    double [] scores = dpCursor.currentCol();
    State [] states = getStates();

    int l = 0;
    while (states[l] != getModel().magicalState())
      l++;

    return scores[l];
  }

  private double backward_termination(DPCursor dpCursor)
    throws IllegalResidueException {
    double [] scores = dpCursor.currentCol();
    State [] states = getStates();

    int l = 0;
    while (states[l] != getModel().magicalState())
      l++;

    return scores[l];
  }
  
  public StatePath viterbi(ResidueList [] resList)
  throws IllegalResidueException {
    ResidueList r = resList[0];
    DPCursor dpCursor = new SmallCursor(getStates(), r, r.iterator());
    return viterbi(dpCursor);
  }

  private StatePath viterbi(DPCursor dpCursor)
  throws IllegalResidueException {
    int seqLength = dpCursor.length();
    State [] states = getStates();

    int [][] transitions = getForwardTransitions();
    double [][] transitionScore = getForwardTransitionScores();
    int stateCount = states.length;

    BackPointer [] oldPointers = new BackPointer[stateCount];
    BackPointer [] newPointers = new BackPointer[stateCount];

    // initialize
    for (int l = 0; l < stateCount; l++) {
      double [] v = dpCursor.currentCol();
      if(states[l] == getModel().magicalState()) {
        System.out.println("Initializing start state to 0.0");
      }
      v[l] = (states[l] == getModel().magicalState()) ? 0.0 : Double.NEGATIVE_INFINITY;
    }

    // viterbi
    while (dpCursor.canAdvance()) { // residue i
      dpCursor.advance();
      Residue res = dpCursor.currentRes();
      System.out.println(res.getName());
      double [] currentCol = dpCursor.currentCol();
      double [] lastCol = dpCursor.lastCol();
      for (int l = 0; l < getDotStatesIndex(); l++) { // emission states
        double emission = ((EmissionState) states[l]).getWeight(res);
        int [] tr = transitions[l];
        System.out.println("Considering " + tr.length + " alternatives");
        double [] trs = transitionScore[l];
        if (emission == Double.NEGATIVE_INFINITY) {
          System.out.println(states[l].getName() + ": impossible emission");
          currentCol[l] = Double.NEGATIVE_INFINITY;
          newPointers[l] = null;
        } else {
          double transProb = Double.NEGATIVE_INFINITY;
          double trans = Double.NEGATIVE_INFINITY;
          int prev = -1;
          for (int kc = 0; kc < tr.length; kc++) {
            int k = tr[kc];
            double t = trs[kc];
            double p = t + lastCol[k];
            if (p > transProb) {
              transProb = p;
              prev = k;
              trans = t;
            }
          }
          currentCol[l] = transProb + emission;
          if(prev != -1) {
            System.out.println(states[prev].getName() + "->" + states[l].getName());
            newPointers[l] = new BackPointer(states[l], oldPointers[prev],
                                             trans + emission);
          } else {
            System.out.println(states[l].getName() + ": Nowhere to come from");
          }
        }
      }
      for(int l = getDotStatesIndex(); l < states.length; l++) { // emission -> dot
        int [] tr = transitions[l];
        double [] trs = transitionScore[l];
        double transProb = Double.NEGATIVE_INFINITY;
        double trans = Double.NEGATIVE_INFINITY;
        int prev = -1;
        for (int kc = 0; kc < tr.length; kc++) {
          int k = tr[kc];
          if(k >= getDotStatesIndex()) {
            break;
          }
          double t = trs[kc];
          double p = t + currentCol[k];
          if (p > transProb) {
            transProb = p;
            prev = k;
            trans = t;
          }
          currentCol[l] = transProb;
          if(prev != -1) {
            System.out.println(states[prev].getName() + "->" + states[l]);
            newPointers[l] = new BackPointer(
              states[l],
              newPointers[prev],
              trans
            );
          }
        }
      }
      for(int l = getDotStatesIndex(); l < states.length; l++) { // dot -> dot
        int [] tr = transitions[l];
        double [] trs = transitionScore[l];
        double transProb = Double.NEGATIVE_INFINITY;
        double trans = Double.NEGATIVE_INFINITY;
        int prev = -1;
        for (int kc = 0; kc < tr.length; kc++) {
          int k = tr[kc];
          if(k < getDotStatesIndex()) {
            continue;
          }
          if(k >= l) {
            break;
          }
          double t = trs[kc];
          double p = t + currentCol[k];
          if (p > transProb) {
            transProb = p;
            prev = k;
            trans = t;
          }
          currentCol[l] = transProb;
          if(prev != -1) {
            System.out.println(states[prev].getName() + "->" + states[l]);
            newPointers[l] = new BackPointer(
              states[l],
              newPointers[prev],
              trans
            );
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

    // trace back ruit
    List stateList = new ArrayList(seqLength);
    List scoreList = new ArrayList(seqLength);
    for (int j = 0; j < seqLength; j++) {
      stateList.add(null);
      scoreList.add(null);
    }

    for (int j = seqLength - 1; j >= 0; j--) {
      stateList.set(j, best.state);
      scoreList.set(j, DoubleAlphabet.getResidue(best.score));
      best = best.back;
    };

    Map labelToResList = new HashMap();
    labelToResList.put(StatePath.SEQUENCE, dpCursor.resList());
    labelToResList.put(StatePath.STATES,
                       new SimpleResidueList(getModel().stateAlphabet(), stateList));
    labelToResList.put(StatePath.SCORES,
                       new SimpleResidueList(DoubleAlphabet.INSTANCE, scoreList));
    return new SimpleStatePath(bestScore, labelToResList);
  }

  private static class BackPointer {
    public State state;
    public BackPointer back;
    public double score;

    public BackPointer(State state, BackPointer back, double score) {
      this.state = state;
      this.back = back;
      this.score = score;
    }
  }
}
