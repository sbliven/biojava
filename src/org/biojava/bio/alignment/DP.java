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

public class DP {
  public static Residue MAGICAL_RESIDUE;
  public static EmissionState MAGICAL_STATE;
  public static Alphabet MAGICAL_ALPHABET;

  static {
    MAGICAL_RESIDUE = new SimpleResidue('!', "mMagical", null);
    MAGICAL_STATE = new MagicalState('!', MAGICAL_RESIDUE);
    MAGICAL_ALPHABET = new SimpleAlphabet();

    try {
      ((SimpleAlphabet) MAGICAL_ALPHABET).addResidue(MAGICAL_RESIDUE);
      ((SimpleAlphabet) MAGICAL_ALPHABET).setName("Magical Alphabet");
    } catch (IllegalResidueException ire) {
    }
  }

  public static EmissionState [] stateList(Alphabet alpha)
    throws IllegalResidueException {
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

  public static int [][] backwardTransitions(MarkovModel model,
      EmissionState [] states) throws IllegalResidueException {
    int stateCount = states.length;
    int [][] transitions = new int[stateCount][];

    for (int i = 0; i < stateCount; i++) {
      int [] tmp = new int[stateCount];
      int len = 0;
      Set trans = model.transitionsFrom(states[i]);
      for (int j = 0; j < stateCount; j++) {
        if (trans.contains(states[j]))
          tmp[len++] = j;
      }
      int [] tmp2 = new int[len];
      for (int j = 0; j < len; j++)
        tmp2[j] = tmp[j];
      transitions[i] = tmp2;
    }

    return transitions;
  }

  public static double [][] backwardTransitionScores(MarkovModel model,
      EmissionState [] states,
      int [][] transitions) throws IllegalResidueException {
    int stateCount = states.length;
    double [][] scores = new double[stateCount][];

    for (int i = 0; i < stateCount; i++) {
      State is = states[i];
      scores[i] = new double[transitions[i].length];
      for (int j = 0; j < scores[i].length; j++) {
        try {
          scores[i][j] = model.getTransitionScore(is, states[transitions[i][j]]);
        } catch (IllegalTransitionException ite) {
          throw new BioError(ite,
            "Transition listed in transitions array has dissapeared");
        }
      }
    }

    return scores;
  }

  private FlatModel model;
  private EmissionState [] states;
  private int [][] forwardTransitions;
  private double [][] forwardTransitionScores;
  private int [][] backwardTransitions;
  private double [][] backwardTransitionScores;

  public FlatModel getModel() {
    return model;
  }
  
  public EmissionState [] getStates() {
    return states;
  }
  
  public int [][] getForwardTransitions() {
    return forwardTransitions;
  }
  
  public double [][] getForwardTransitionScores() {
    return forwardTransitionScores;
  }
  
  public int [][] getBackwardTransitions() {
    return backwardTransitions;
  }
  
  public double [][] getBackwardTransitionScores() {
    return backwardTransitionScores;
  }
  
  public DP(FlatModel model) throws IllegalResidueException {
    this.model = model;
    this.states = stateList(model.stateAlphabet());
    this.forwardTransitions = forwardTransitions(model, states);
    this.forwardTransitionScores = forwardTransitionScores(model, states,
      forwardTransitions);
    this.backwardTransitions = backwardTransitions(model, states);
    this.backwardTransitionScores = backwardTransitionScores(model, states,
      backwardTransitions);
  }
  
  public double forward(MarkovModel model, ResidueList seq)
  throws IllegalResidueException {
    DPCursor dpCursor = new SmallCursor(states, seq.length(),
                                        seq.iterator());
    return forward(dpCursor);
  }

  public double backward(MarkovModel model, ResidueList seq)
  throws IllegalResidueException {
    DPCursor dpCursor = new SmallCursor(states, seq.length(),
                                        new ReverseIterator(seq));
    return backward(dpCursor);
  }

  public double forward(DPCursor dpCursor)
  throws IllegalResidueException {
    forward_initialize(dpCursor);
    forward_recurse(dpCursor);
    return forward_termination(dpCursor);
  }

  public double backward(DPCursor dpCursor)
  throws IllegalResidueException {
    backward_initialize(dpCursor);
    backward_recurse(dpCursor);
    return backward_termination(dpCursor);
  }

  public void forward_initialize(DPCursor dpCursor)
    throws IllegalResidueException {
    double [] v = dpCursor.currentCol();

    // new_l = transition(start, l)
    for (int l = 0; l < states.length; l++) {
      v[l] = (states[l] == DP.MAGICAL_STATE) ? 0.0 : Double.NEGATIVE_INFINITY;
    }
  }

  public void backward_initialize(DPCursor dpCursor)
    throws IllegalResidueException {
    double [] v = dpCursor.currentCol();

    // new_l = transition(start, l)
    for (int l = 0; l < states.length; l++) {
      v[l] = (states[l] == DP.MAGICAL_STATE) ? 0.0 : Double.NEGATIVE_INFINITY;
    }
  }

  public void forward_recurse(DPCursor dpCursor)
    throws IllegalResidueException {
    int stateCount = states.length;
    int [][] transitions = forwardTransitions;
    double [][] transitionScore = forwardTransitionScores;

    // int _index = 0;
    while (dpCursor.canAdvance()) {
      // _index++;
      // System.out.println("\n*** Index=" + _index + " ***");
      dpCursor.advance();
      Residue res = dpCursor.currentRes();
      double [] currentCol = dpCursor.currentCol();
      double [] lastCol = dpCursor.lastCol();
      for (int l = 0; l < stateCount; l++) { // state l for residue i  sum(p(k->l))
        double weight = states[l].getWeight(res);
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
    }
  }

  public void backward_recurse(DPCursor dpCursor)
    throws IllegalResidueException {
    int stateCount = states.length;
    int [][] transitions = backwardTransitions;
    double [][] transitionScore = backwardTransitionScores;

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
        while (ci < tr.length &&
            lastCol[tr[ci]] == Double.NEGATIVE_INFINITY) {
          ci++;
        }
        double constant = (ci < tr.length) ? lastCol[tr[ci]] : 0.0;

        for (int lc = 0; lc < tr.length; lc++) {
          int l = tr[lc];
          double weight = states[l].getWeight(res);
          if (lastCol[l] != Double.NEGATIVE_INFINITY &&
              weight != Double.NEGATIVE_INFINITY) {
            double t = trs[lc];
            score += Math.exp(t + lastCol[l] + weight - constant);
          }
        }
        // new_k = sum_l( transition(k, l) * old_l * emission_l(res) )
        currentCol[k] = Math.log(score) + constant;
      }
    }
  }

  public double forward_termination(DPCursor dpCursor)
    throws IllegalResidueException {
    double [] scores = dpCursor.currentCol();

    int l = 0;
    while (states[l] != DP.MAGICAL_STATE)
      l++;

    return scores[l];
  }

  public double backward_termination(DPCursor dpCursor)
    throws IllegalResidueException {
    double [] scores = dpCursor.currentCol();

    int l = 0;
    while (states[l] != DP.MAGICAL_STATE)
      l++;

    return scores[l];
  }

  public StatePath viterbi(ResidueList seq)
  throws IllegalResidueException {
    DPCursor dpCursor = new SmallCursor(states, seq.length(), seq.iterator());
    return viterbi(dpCursor, seq);
  }

  public StatePath viterbi(DPCursor dpCursor, ResidueList seq)
  throws IllegalResidueException {
    int seqLength = dpCursor.length();

    int [][] transitions = forwardTransitions;
    double [][] transitionScore = forwardTransitionScores;
    int stateCount = states.length;

    BackPointer [] oldPointers = new BackPointer[stateCount];
    BackPointer [] newPointers = new BackPointer[stateCount];

    // initialize
    for (int l = 0; l < stateCount; l++) {
      double [] v = dpCursor.currentCol();
      v[l] = (states[l] == DP.MAGICAL_STATE) ? 0.0 : Double.NEGATIVE_INFINITY;
    }


    // viterbi
    while (dpCursor.canAdvance()) { // residue i
      dpCursor.advance();
      Residue res = dpCursor.currentRes();
      double [] currentCol = dpCursor.currentCol();
      double [] lastCol = dpCursor.lastCol();
      for (int l = 0; l < stateCount; l++) {
        double emission = states[l].getWeight(res);
        int [] tr = transitions[l];
        double [] trs = transitionScore[l];
        if (emission == Double.NEGATIVE_INFINITY) {
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
            newPointers[l] = new BackPointer(states[l], oldPointers[prev],
                                             trans + emission);
          }
        }
      }
      BackPointer [] bp = newPointers;
      newPointers = oldPointers;
      oldPointers = bp;
    }

    // find max in last row
    BackPointer best = null;
    double bestScore = 0.0;
    for (int l = 0; l < stateCount; l++) {
      if (states[l] == DP.MAGICAL_STATE) {
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
    labelToResList.put(StatePath.SEQUENCE, seq);
    labelToResList.put(StatePath.STATES,
                       new SimpleResidueList(model.stateAlphabet(), stateList));
    labelToResList.put(StatePath.SCORES,
                       new SimpleResidueList(DoubleAlphabet.INSTANCE, scoreList));
    return new SimpleStatePath(bestScore, labelToResList);
  }

  /**
    * Generates an alignment from a model.
    * <P>
    * If the length is set to -1 then the model length will be sampled
    * using the model's transition to the end state. If the length is
    * fixed using length, then the transitions to the end state are implicitly
    * invoked.
    *
    * @param length  the length of the sequence to generate
    * @return  a StatePath generated at random
    */
  public StatePath generate(int length)
  throws IllegalResidueException, SeqException {
    List scoreList = new ArrayList();
    SimpleResidueList tokens = new SimpleResidueList(model.queryAlphabet());
    SimpleResidueList states = new SimpleResidueList(model.stateAlphabet());

    double totScore = 0.0;
    double resScore = 0.0;
    int i = length;
    State oldState;
    Residue token;

    oldState = model.sampleTransition(DP.MAGICAL_STATE);
    try {
      resScore += model.getTransitionScore(DP.MAGICAL_STATE, oldState);
    } catch (IllegalTransitionException ite) {
      throw new BioError(ite,
        "Transition returned from sampleTransition is invalid");
    }
    if (oldState instanceof EmissionState) {
      EmissionState eState = (EmissionState) oldState;
      token = eState.sampleResidue();
      resScore += eState.getWeight(token);
      states.addResidue(oldState);
      tokens.addResidue(token);
      scoreList.add(DoubleAlphabet.getResidue(resScore));
      totScore += resScore;
      resScore = 0.0;
      i--;
    }

    while (i != 0) {
      State newState;
      do {
        newState = model.sampleTransition(oldState);
      } while (newState == DP.MAGICAL_STATE && i > 0);
      try {
        resScore += model.getTransitionScore(oldState, newState);
      } catch (IllegalTransitionException ite) {
        throw new BioError(ite,
          "Transition returned from sampleTransition is invalid");
      }

      if (newState == DP.MAGICAL_STATE)
        break;

      if (newState instanceof EmissionState) {
        EmissionState eState = (EmissionState) newState;
        token = eState.sampleResidue();
        resScore += eState.getWeight(token);
        states.addResidue(newState);
        tokens.addResidue(token);
        scoreList.add(DoubleAlphabet.getResidue(resScore));
        totScore += resScore;
        resScore = 0.0;
        i--;
      }
      oldState = newState;
    }

    List resListList = new ArrayList(3);
    resListList.add(tokens);
    resListList.add(states);
    resListList.add(new SimpleResidueList(DoubleAlphabet.INSTANCE, scoreList));
    
    Map labelToResList = new HashMap();
    labelToResList.put(StatePath.SEQUENCE, tokens);
    labelToResList.put(StatePath.STATES, states);
    labelToResList.put(StatePath.SCORES,
                       new SimpleResidueList(DoubleAlphabet.INSTANCE, scoreList));
    return new SimpleStatePath(totScore, labelToResList);
  }

  /**
    * Scores the ResidueList from residue 1 to residue columns with a weight
    * matrix.
    *
    * @param matrix  the weight matrix used to evaluate the sequences
    * @param resList the ResidueList to assess
    * @return  the log probability or likelyhood of this weight matrix
    *          having generated residues 1 to columns of resList
    */
  public static double score(WeightMatrix matrix, ResidueList resList)
  throws IllegalResidueException {
    double score = 0;
    int cols = matrix.columns();

    for (int c = 0; c < cols; c++)
      score += matrix.getWeight(resList.residueAt(c + 1), c);

    return score;
  }

  private static class MagicalState implements EmissionState {
    private char c;
    private Residue r;
      private int[] advance = {1};

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
      return MAGICAL_ALPHABET;
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

  private static class BackPointer {
    State state;
    BackPointer back;
    double score;

    BackPointer(State state, BackPointer back, double score) {
      this.state = state;
      this.back = back;
      this.score = score;
    }
  }

  public static class ReverseIterator implements Iterator {
    private ResidueList res;
    private int index;

    public ReverseIterator(ResidueList res) {
      this.res = res;
      index = res.length();
    }

    public boolean hasNext() {
      return index > 0;
    }

    public Object next() {
      return res.residueAt(index--);
    }

    public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException("This itterator can not cause modifications");
    }
  }
}

