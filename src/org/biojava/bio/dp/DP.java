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
import org.biojava.bio.dist.*;

/**
 * <p>
 * Objects that can perform dymamic programming operations upon sequences with
 * HMMs.
 * </p>
 *
 * <p>
 * The three main DP operations are Forwards, Backwards and Viterbi. Forwards
 * and Backwards calculate the probability of the sequences having been made in
 * any way by the model. Viterbi finds the most supported way that the sequence
 * could have been made.
 * </p>
 *
 * <p>
 * Each of the functions can return the dynamic-programming matrix containing
 * the intermediate results. This may be useful for model training, or for
 * visualisation.
 * </p>
 *
 * <p>
 * Each of the funcitons can be calculated using the model probabilities, the
 * null-model probabilities or the odds (ratio between the two). For Forwards
 * and Backwards, the odds calculations produce numbers with questionable basis
 * in reality. For Viterbi with odds, you will recieve the path through the
 * model that is most different from the null model, and supported by the
 * probabilities.
 * </p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */
public abstract class DP {
  /**
    * Scores the SymbolList from symbol start to symbol (start+columns) with a
    * weight matrix.
    *
    * @param matrix  the weight matrix used to evaluate the sequences
    * @param symList the SymbolList to assess
    * @param start   the index of the first symbol in the window to evaluate
    * @return  the log probability or likelyhood of this weight matrix
    *          having generated symbols start to (start + columns) of symList
    */
  public static double scoreWeightMatrix(
    WeightMatrix matrix, SymbolList symList, int start)
  throws IllegalSymbolException {
    double score = 0;
    int cols = matrix.columns();

    for (int c = 0; c < cols; c++) {
      score += Math.log(matrix.getColumn(c).getWeight(symList.symbolAt(c + start)));
    }
    
    return score;
  }
  
  public static MarkovModel flatView(MarkovModel model)
  throws IllegalAlphabetException, IllegalSymbolException {
    for(Iterator i = model.stateAlphabet().iterator(); i.hasNext(); ) {
      State s = (State) i.next();
      if(
        !(s instanceof DotState) &&
        !(s instanceof EmissionState)
      ) {
        return new FlatModel(model);
      }
    }
    
    return model;
  }
  
  public static State[] stateList(MarkovModel mm)
  throws IllegalSymbolException, IllegalTransitionException,
  BioException
  {
    FiniteAlphabet alpha = mm.stateAlphabet();

    List emissionStates = new ArrayList();
    HMMOrderByTransition comp = new HMMOrderByTransition(mm);
    List dotStates = new LinkedList();
    for (Iterator addStates = alpha.iterator(); addStates.hasNext(); ) {
      Object state = addStates.next();
      if(state instanceof MagicalState) {
        emissionStates.add(0, state);
      } else if (state instanceof EmissionState) {
        emissionStates.add(state);
      } else {
        ListIterator checkOld = dotStates.listIterator();
        int insertPos = -1;
        while (checkOld.hasNext() && insertPos == -1) {
          Object oldState = checkOld.next();
          if (comp.compare(state, oldState) == comp.LESS_THAN) {
            insertPos = checkOld.nextIndex() - 1;
          }
        }
        if (insertPos >= 0) {
          dotStates.add(insertPos, state);
        } else {
          dotStates.add(state);
        }
      }
    }
    
    State[] sl = new State[emissionStates.size() + dotStates.size()];
    int i = 0;
    for (Iterator si = emissionStates.iterator(); si.hasNext(); ) {
      EmissionState ex = (EmissionState) si.next();
      int [] ad = ex.getAdvance();
      if(ad.length != mm.heads()) {
        throw new BioException(
          "State " + ex.getName() + " advances " + ad.length + " heads. " +
          " however, the model " + mm.stateAlphabet().getName() +
          " advances " + mm.heads() + " heads."
        );
      }
      for(int adi = 0; ad != null && adi < ad.length; adi++) {
        if(ad[adi] != 0) {
          ad = null;
        }
      }
      if(ad != null) {
        throw new Error(
          "State " + ex.getName() + " has advance " + ad
        );
      }
      sl[i++] = ex;
    }
    for (Iterator si = dotStates.iterator(); si.hasNext(); ) {
      sl[i++] = (State) si.next();
    }
    return sl;
  }

  public static int [][] forwardTransitions(
    MarkovModel model,
    State [] states
  ) throws IllegalSymbolException {
    int stateCount = states.length;
    int [][] transitions = new int[stateCount][];

    for (int i = 0; i < stateCount; i++) {
      int [] tmp = new int[stateCount];
      int len = 0;
      FiniteAlphabet trans = model.transitionsTo(states[i]);
      for (int j = 0; j < stateCount; j++) {
        if (trans.contains(states[j])) {
          tmp[len++] = j;
        }
      }
      int [] tmp2 = new int[len];
      for (int j = 0; j < len; j++) {
        tmp2[j] = tmp[j];
      }
      transitions[i] = tmp2;
    }

    return transitions;
  }

  public static double [][] forwardTransitionScores(
    MarkovModel model,
    State [] states,
    int [][] transitions,
    ScoreType scoreType
  ) throws IllegalSymbolException {
    int stateCount = states.length;
    double [][] scores = new double[stateCount][];

    for (int i = 0; i < stateCount; i++) {
      State is = states[i];
      scores[i] = new double[transitions[i].length];
      for (int j = 0; j < scores[i].length; j++) {
        try {
          scores[i][j] = Math.log(scoreType.calculateScore(
            model.getWeights(states[transitions[i][j]]),
            is
          ));
          /*System.out.println(
            states[transitions[i][j]] + "\t-> " +
            is.getName() + "\t = " +
            scores[i][j] + "\t(" +
            scoreType.calculateScore(
              model.getWeights(states[transitions[i][j]]),
              is
            )
          );*/
        } catch (IllegalSymbolException ite) {
          throw new BioError(ite,
            "Transition listed in transitions array has dissapeared.");
        }
      }
    }

    return scores;
  }

  public static int [][] backwardTransitions(
    MarkovModel model,
    State [] states
  ) throws IllegalSymbolException {
    int stateCount = states.length;
    int [][] transitions = new int[stateCount][];

    for (int i = 0; i < stateCount; i++) {
      int [] tmp = new int[stateCount];
      int len = 0;
      FiniteAlphabet trans = model.transitionsFrom(states[i]);
      for (int j = 0; j < stateCount; j++) {
        if (trans.contains(states[j])) {
          tmp[len++] = j;
        }
      }
      int [] tmp2 = new int[len];
      for (int j = 0; j < len; j++) {
        tmp2[j] = tmp[j];
      }
      transitions[i] = tmp2;
    }

    return transitions;
  }

  public static double [][] backwardTransitionScores(MarkovModel model,
    State [] states,
    int [][] transitions,
    ScoreType scoreType
  ) throws IllegalSymbolException {
    int stateCount = states.length;
    double [][] scores = new double[stateCount][];

    for (int i = 0; i < stateCount; i++) {
      State is = states[i];
      scores[i] = new double[transitions[i].length];
      for (int j = 0; j < scores[i].length; j++) {
        try {
          scores[i][j] = Math.log(scoreType.calculateScore(
            model.getWeights(is),
            states[transitions[i][j]]
          ));
        } catch (IllegalSymbolException ite) {
          throw new BioError(ite,
            "Transition listed in transitions array has dissapeared");
        }
      }
    }

    return scores;
  }

  private MarkovModel model;
  private State[] states;
  private int [][] forwardTransitions;
  private int [][] backwardTransitions;
  private int dotStatesIndex;
  private int lockCount = 0;
  
  public int getDotStatesIndex() {
    return dotStatesIndex;
  }

  public MarkovModel getModel() {
    return model;
  }
  
  public State[] getStates() {
    return states;
  }
  
  public int [][] getForwardTransitions() {
    return forwardTransitions;
  }
  
  private Map forwardTransitionScores;
  private Map backwardTransitionScores;
  
  public double [][] getForwardTransitionScores(ScoreType scoreType) {
    double [][] ts = (double [][]) forwardTransitionScores.get(scoreType);
    if(ts == null) {
      try {
        forwardTransitionScores.put(scoreType, ts = forwardTransitionScores(
          getModel(), getStates(), forwardTransitions, scoreType
        ));
      } catch (IllegalSymbolException ise) {
        throw new BioError(ise, "Inconsistency in model");
      }
    }
    return ts;
  }
  
  public int [][] getBackwardTransitions() {
    return backwardTransitions;
  }
  
  public double [][] getBackwardTransitionScores(ScoreType scoreType) {
    double [][] ts = (double [][]) backwardTransitionScores.get(scoreType);
    if(ts == null) {
      try {
        backwardTransitionScores.put(scoreType, ts = backwardTransitionScores(
          getModel(), getStates(), backwardTransitions, scoreType
        ));
      } catch (IllegalSymbolException ise) {
        throw new BioError(ise, "Inconsistency in model");
      }
    }
    return ts;
  }
  
  public void lockModel() {
    if(lockCount++ == 0) {
      getModel().addChangeListener(ChangeListener.ALWAYS_VETO);
    }
  }
  
  public void unlockModel() {
    if(--lockCount == 0) {
      getModel().removeChangeListener(ChangeListener.ALWAYS_VETO);
    }
  }
  
  public void update() {
    try {
      this.states = stateList(model);
      this.forwardTransitions = forwardTransitions(model, states);
      this.forwardTransitionScores.clear();
      this.backwardTransitions = backwardTransitions(model, states);
      this.backwardTransitionScores.clear();

      // Find first dot state
      int i;
      for (i = 0; i < states.length; ++i) {
        if (! (states[i] instanceof EmissionState)) {
          break;
        }
      }
      dotStatesIndex = i;
    } catch (Exception e) {
      throw new BioError(e, "Something is seriously wrong with the DP code");
    }
  }
  
  public DP(MarkovModel model)
  throws IllegalSymbolException, IllegalTransitionException, BioException {
    this.model = model;
    this.forwardTransitionScores = new HashMap();
    this.backwardTransitionScores = new HashMap();
    update();
    
    model.addChangeListener(UPDATER);
  }

  public abstract double forward(SymbolList [] symList, ScoreType scoreType)
  throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException;
  
  public abstract double backward(SymbolList [] symList, ScoreType scoreType)
  throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException;

  public abstract DPMatrix forwardMatrix(SymbolList [] symList, ScoreType scoreType)
  throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException;
  
  public abstract DPMatrix backwardMatrix(SymbolList [] symList, ScoreType scoreType)
  throws IllegalSymbolException, IllegalAlphabetException, IllegalTransitionException;

  public abstract DPMatrix forwardMatrix(SymbolList [] symList, DPMatrix matrix, ScoreType scoreType)
  throws IllegalArgumentException, IllegalSymbolException,
  IllegalAlphabetException, IllegalTransitionException;
  
  public abstract DPMatrix backwardMatrix(SymbolList [] symList, DPMatrix matrix, ScoreType scoreType)
  throws IllegalArgumentException, IllegalSymbolException,
  IllegalAlphabetException, IllegalTransitionException;
    
  public abstract StatePath viterbi(SymbolList [] symList, ScoreType scoreType)
  throws IllegalSymbolException, IllegalArgumentException, IllegalAlphabetException, IllegalTransitionException;
  
  public DPMatrix forwardsBackwards(SymbolList [] symList, ScoreType scoreType)
  throws BioException {
    try {
      System.out.println("Making backward matrix");
      final DPMatrix bMatrix = backwardMatrix(symList, scoreType);
      System.out.println("Making forward matrix");
      final DPMatrix fMatrix = forwardMatrix(symList, scoreType);
    
      System.out.println("Making forward/backward matrix");
      return new DPMatrix() {
        public double getCell(int [] index) {
          return fMatrix.getCell(index) + bMatrix.getCell(index);
        }
      
        public double getScore() {
          return fMatrix.getScore();
        }
      
        public MarkovModel model() {
          return fMatrix.model();
        }
      
        public SymbolList [] symList() {
          return fMatrix.symList();
        }
      
        public State [] states() {
          return fMatrix.states();
        }
      };
    } catch (Exception e) {
      throw new BioException(e, "Couldn't build forwards-backwards matrix");
    }
  }

  /**
   * <p>
   * Generates an alignment from a model.
   * </p>
   *
   * <p>
   * If the length is set to -1 then the model length will be sampled
   * using the model's transition to the end state. If the length is
   * fixed using length, then the transitions to the end state are implicitly
   * invoked.
   * </p>
   *
   * @param length  the length of the sequence to generate
   * @return  a StatePath generated at random
   */
  public StatePath generate(int length)
  throws IllegalSymbolException, BioException {
    List tokenList = new ArrayList();
    List stateList = new ArrayList();
    List scoreList = new ArrayList();
    double totScore = 0.0;
    double symScore = 0.0;
    int i = length;
    State oldState;
    Symbol token;

    oldState = (State) model.getWeights(model.magicalState()).sampleSymbol();
    Distribution oldDist = model.getWeights(oldState);
    try {
      symScore += oldDist.getWeight(oldState);
    } catch (IllegalSymbolException ite) {
      throw new BioError(ite,
        "Transition returned from sampleTransition is invalid");
    }
    DoubleAlphabet dAlpha = DoubleAlphabet.getInstance();
    if (oldState instanceof EmissionState) {
      EmissionState eState = (EmissionState) oldState;
      token = eState.getDistribution().sampleSymbol();
      symScore += eState.getDistribution().getWeight(token);
      stateList.add(oldState);
      tokenList.add(token);
      scoreList.add(dAlpha.getSymbol(symScore));
      totScore += symScore;
      symScore = 0.0;
      i--;
    }

    while (i != 0) {
      State newState = null;
      Distribution dist = model.getWeights(oldState); 
      do {
        newState = (State) dist.sampleSymbol();
      } while (newState == model.magicalState() && i > 0);
      try {
        symScore += dist.getWeight(newState);
      } catch (IllegalSymbolException ise) {
        throw new BioError(ise,
          "Transition returned from sampleTransition is invalid");
      }

      if (newState == model.magicalState()) {
        break;
      }

      if (newState instanceof EmissionState) {
        EmissionState eState = (EmissionState) newState;
        token = eState.getDistribution().sampleSymbol();
        symScore += eState.getDistribution().getWeight(token);
        stateList.add(newState);
        tokenList.add(token);
        scoreList.add(dAlpha.getSymbol(symScore));
        totScore += symScore;
        symScore = 0.0;
        i--;
      }
      oldState = newState;
    }

    SymbolList tokens = new SimpleSymbolList(model.emissionAlphabet(), tokenList);
    SymbolList states = new SimpleSymbolList(model.stateAlphabet(), stateList);
    SymbolList scores = new SimpleSymbolList(dAlpha, scoreList);

    return new SimpleStatePath(
      totScore,
      tokens,
      states,
      new SimpleSymbolList(dAlpha, scoreList)
    );
  }

  public static class ReverseIterator implements Iterator, Serializable {
    private SymbolList sym;
    private int index;

    public ReverseIterator(SymbolList sym) {
      this.sym = sym;
      index = sym.length();
    }

    public boolean hasNext() {
      return index > 0;
    }

    public Object next() {
      return sym.symbolAt(index--);
    }

    public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException("This itterator can not cause modifications");
    }
  }

  private final ChangeListener UPDATER = new ChangeListener() {
    public void preChange(ChangeEvent ce)
    throws ChangeVetoException {}
  
    public void postChange(ChangeEvent ce) {
      if(
        (ce.getType() == MarkovModel.ARCHITECTURE) ||
        (ce.getType() == MarkovModel.PARAMETER)
      ) {
        update();
      }
    }
  };

  private static class HMMOrderByTransition {
    public final static Object GREATER_THAN = new Object();
    public final static Object LESS_THAN = new Object();
    public final static Object EQUAL = new Object();
    public final static Object DISJOINT = new Object();

    private MarkovModel mm;

    private HMMOrderByTransition(MarkovModel mm) {
	    this.mm = mm;
    }

    public Object compare(Object o1, Object o2)
    throws IllegalTransitionException, IllegalSymbolException {
	    if (o1 == o2) {
       return EQUAL;
      }
	    State s1 = (State) o1;
	    State s2 = (State) o2;
        
	    if (transitionsTo(s1, s2)) {
        return LESS_THAN;
      }
	    if (transitionsTo(s2, s1)) {
        return GREATER_THAN;
      }

	    return DISJOINT;
    }

    private boolean transitionsTo(State from, State to)
	  throws IllegalTransitionException, IllegalSymbolException {
	    Set checkedSet = new HashSet();
	    Set workingSet = new HashSet();
            for(
              Iterator i = mm.transitionsFrom(from).iterator();
              i.hasNext();
            ) {
              workingSet.add(i);
            }

	    while (workingSet.size() > 0) {
        Set newWorkingSet = new HashSet();
        for (Iterator i = workingSet.iterator(); i.hasNext(); ) {
          State s = (State) i.next();
          if (s instanceof EmissionState) {
            continue;
          }
          if (s == from) {
            throw new IllegalTransitionException(
              from, from, "Loop in dot states."
            );
          }
          if (s == to) {
            return true;
          }
          for (Iterator j = mm.transitionsFrom(s).iterator(); j.hasNext(); ) {
            State s2 = (State) j.next();
            if (!workingSet.contains(s2) && !checkedSet.contains(s2)) {
              newWorkingSet.add(s2);
            }
          }
          checkedSet.add(s);
        }
        workingSet = newWorkingSet;
	    }

	    return false;
    }
  }
}

