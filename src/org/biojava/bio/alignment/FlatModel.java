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

/**
 * A model that guarantees to only contain emission states.
 */
public class FlatModel implements MarkovModel {
  private Transition _tran = new Transition(null, null);

  private MarkovModel flattenedModel;  
  private Alphabet stateAlphabet;
  private Alphabet queryAlphabet;
  
  /**
   * Map (from, to) -> Set <model, f, t>
   */
  private Map statesToTransitions = new HashMap();
  
  /**
   * Map from -> Set <to>
   */
  private Map transitionsFrom = new HashMap();
  
  /**
   * Map to -> Set <from>
   */
  private Map transitionsTo = new HashMap();
  
  public MarkovModel getFlattenedModel() {
    return flattenedModel;
  }
  
  public Set getStateTransitions(State from, State to)
  throws IllegalResidueException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);
    _tran.from = from;
    _tran.to = to;
    Set transSet = (Set) statesToTransitions.get(_tran);
    return (transSet == null) ? Collections.EMPTY_SET : transSet;
  }
  
  public Alphabet stateAlphabet() {
    return stateAlphabet;
  }
  
  public Alphabet queryAlphabet() {
    return queryAlphabet;
  }

  public Set transitionsFrom(State from) {
    Set set = (Set) transitionsFrom.get(from);
    if(set == null)
      return Collections.EMPTY_SET;
    return set;
  }
  
  public Set transitionsTo(State to) {
    Set set = (Set) transitionsTo.get(to);
    if(set == null)
      return Collections.EMPTY_SET;
    return set;
  }
  
  public void registerWithTrainer(ModelTrainer modelTrainer) {
    getFlattenedModel().registerWithTrainer(modelTrainer);
    for(Iterator i = stateAlphabet().residues().iterator(); i.hasNext();) {
      State s = (State) i.next();
      System.out.println("Registering state " + s.getName());
      s.registerWithTrainer(modelTrainer);
    }
    for(Iterator i = statesToTransitions.keySet().iterator(); i.hasNext();) {
      Transition trans = (Transition) i.next();
      Set transSet = (Set) statesToTransitions.get(trans);
      for(Iterator j = transSet.iterator(); j.hasNext();) {
        Object [] oa = (Object []) j.next();
        MarkovModel model = (MarkovModel) oa[0];
        TransitionTrainer tt = modelTrainer.getTrainerForModel(model);
        try {
          modelTrainer.registerTrainerForTransition(trans.from, trans.to, tt,
                                                    (State) oa[1], (State) oa[2]);
        } catch (SeqException se) {
          throw new BioError(se,
            "Trainer rejected even though I retrieved it myself");
        }
      }
    }
  }
  
  public double getTransitionScore(State from, State to)
  throws IllegalResidueException, IllegalTransitionException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);
    _tran.from = from;
    _tran.to = to;
    Set transSet = (Set) statesToTransitions.get(_tran);
    if(transSet == null) {
      /*
      System.out.println("Can't find transition.");
      System.out.println(from.getName() + " -> ");
      for(Iterator i = transitionsFrom(from).iterator(); i.hasNext();) {
        System.out.println("\t" + ((State) i.next()).getName());
      }
      System.out.println(to.getName() + " <- ");
      for(Iterator i = transitionsTo(to).iterator(); i.hasNext();) {
        System.out.println("\t" + ((State) i.next()).getName());
      }
      */
      throw new IllegalTransitionException(from, to);
    }
    
    double w = 0.0;
    for(Iterator i = transSet.iterator(); i.hasNext();) {
      Object [] trans = (Object []) i.next();
      MarkovModel m = (MarkovModel) trans[0];
      State f = (State) trans[1];
      State t = (State) trans[2];
      w += m.getTransitionScore(f, t);
    }
    return w;
  }
  
  public void setTransitionScore(State from, State to, double score)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
      "setTransitionScore not supported by " + getClass()
    );
  }
  
  public State sampleTransition(State from)
  throws IllegalResidueException {
    stateAlphabet().validate(from);
    
    double p = Math.random();
    try {
      for(Iterator i = transitionsFrom(from).iterator(); i.hasNext();) {
        State to = (State) i.next();
        p -= getTransitionScore(from, to);
        if(p <= 0) {
          return to;
        }
      }
    } catch (IllegalResidueException ire) {
      throw new BioError(ire, "Transition involving " + from.getName() +
                         "dissapeared");
    } catch (IllegalTransitionException ite) {
      throw new BioError(ite, "Transition listed in transitionsFrom(" +
                         from.getName() + ") has dissapeared");
    }
    
    StringBuffer sb = new StringBuffer();

    for(Iterator i = transitionsFrom(from).iterator(); i.hasNext(); ) {
      try {
        State s = (State) i.next();
        double t = getTransitionScore(from, s);
        if(t > 0.0)
          sb.append("\t" + s.getName() + " -> " + t + "\n");
      } catch (IllegalTransitionException ite) {
        throw new BioError(ite, "Transition listed in transitionsFrom(" +
                           from.getName() + ") has dissapeared");
      }
    }
    throw new IllegalResidueException("Could not find transition from state " +
                                      from.getName() + 
                                      ". Do the probabilities sum to 1?" +
                                      "\np=" + p + "\n" + sb.toString());
  }
  
    
  public void createTransition(State from, State to)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
      "destroyTransition not supported by " + getClass());
  }

  public void destroyTransition(State from, State to)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
      "destroyTransition not supported by " + getClass());
  }
  
  public boolean containsTransition(State from, State to)
  throws IllegalResidueException {
    Alphabet sAlpha = stateAlphabet();
    sAlpha.validate(from);
    sAlpha.validate(to);
    
    return transitionsFrom(from).contains(to);
  }
  
  protected void addTransition(Map stateToFlatModel, State f, State t,
                               MarkovModel model, State rf, State rt)
  throws IllegalResidueException {
    // modify from listing
    Set fromSet = (Set) transitionsFrom.get(f);
    if(fromSet == null)
      fromSet = new HashSet();
    fromSet.add(t);
    transitionsFrom.put(f, fromSet);
    
    // modify to listing
    Set toSet = (Set) transitionsTo.get(t);
    if(toSet == null)
      toSet = new HashSet();
    toSet.add(f);
    transitionsTo.put(t, toSet);
    
    // add transition
    _tran.from = f;
    _tran.to = t;
    Set transSet = (Set) statesToTransitions.get(_tran);
    if(transSet == null) {
      transSet = new HashSet();
      statesToTransitions.put(new Transition(f, t), transSet);
    }
    Object [] transition = { model, rf, rt };
    transSet.add(transition);
    
    // wrapper state for a wrapper state, add the recursive transitions
    if(stateToFlatModel.containsKey(f) && f instanceof StateWrapper) {
      FlatModel fm = (FlatModel) stateToFlatModel.get(f);
      StateWrapper fStateWrapper = (StateWrapper) f;
      State wrapped = fStateWrapper.getWrappedState();
      Set transitions = fm.getStateTransitions(wrapped, DP.MAGICAL_STATE);
      transSet.addAll(transitions);
    }

    if(stateToFlatModel.containsKey(t) && t instanceof StateWrapper) {
      FlatModel tm = (FlatModel) stateToFlatModel.get(t);
      StateWrapper tStateWrapper = (StateWrapper) t;
      State wrapped = tStateWrapper.getWrappedState();
      Set transitions = tm.getStateTransitions(DP.MAGICAL_STATE, wrapped);
      transSet.addAll(transitions);
    }

  }
  
  public Set getWrappedStates(ModelInState modelInState) {
    Set wrapped = new HashSet();
    for(Iterator i = stateAlphabet().residues().iterator(); i.hasNext();) {
      EmissionState wrappedState = (EmissionState) i.next();
      if(wrappedState != DP.MAGICAL_STATE) {
        wrapped.add(new SimpleStateWrapper(wrappedState, modelInState));
      }
    }
    return wrapped;
  }
  
  public FlatModel(MarkovModel model)
  throws IllegalResidueException {
    flattenedModel = model;
    queryAlphabet = model.queryAlphabet();
    SimpleAlphabet stateAlphabet = new SimpleAlphabet();
    stateAlphabet.setName("FlatModel states");
    stateAlphabet.addResidue(DP.MAGICAL_STATE);
    
    Map stateToFlatModel = new HashMap();
    
    Alphabet mStates = model.stateAlphabet();
    for(Iterator si = mStates.residues().iterator(); si.hasNext();) {
      State s = (State) si.next();
      if(s instanceof EmissionState) {
        stateAlphabet.addResidue(s);
      } else if(s instanceof ModelInState) {
        ModelInState mis = (ModelInState) s;
        MarkovModel mism = mis.getModel();
        FlatModel fm = new FlatModel(mism);
        for(Iterator js = fm.getWrappedStates(mis).iterator();
            js.hasNext();) {
          State jState = (State) js.next();
          stateAlphabet.addResidue(jState);
          stateToFlatModel.put(jState, fm);
        }
      }
    }

    // for each of our new states s
    for(Iterator si = stateAlphabet.residues().iterator(); si.hasNext();) {
      State s = (State) si.next();
      // for each of our new states t
      for(Iterator sj = stateAlphabet.residues().iterator(); sj.hasNext();) {
        State t = (State) sj.next();
        if(mStates.contains(s) && mStates.contains(t)) {
          // if they are original emission states, then chech their connectivity
          if(model.transitionsFrom(s).contains(t)) {
            addTransition(stateToFlatModel, s, t, model, s, t);
          }
        } else if(mStates.contains(s)) {
          // s is original state. t is wrapper from contained model
          StateWrapper twrapper = (StateWrapper) t;
          State twrapped = twrapper.getWrappedState();
          ModelInState tstate = twrapper.getContainingState();
          MarkovModel tmodel = tstate.getModel();
          if(model.transitionsFrom(s).contains(tstate) &&
             tmodel.transitionsFrom(DP.MAGICAL_STATE).contains(twrapped)) {
            addTransition(stateToFlatModel, s, t, model, s, tstate);
            addTransition(stateToFlatModel, s, t, tmodel, DP.MAGICAL_STATE, twrapped);
            FlatModel fm = (FlatModel) stateToFlatModel.get(twrapped);
          }
        } else if(mStates.contains(t)) {
          // s is wrapper state, t is original.
          StateWrapper swrapper = (StateWrapper) s;
          State swrapped = swrapper.getWrappedState();
          ModelInState sstate = swrapper.getContainingState();
          MarkovModel smodel = sstate.getModel();
          if(model.transitionsFrom(sstate).contains(t) &&
             smodel.transitionsFrom(swrapped).contains(DP.MAGICAL_STATE)) {
            addTransition(stateToFlatModel, s, t, model, sstate, t);
            addTransition(stateToFlatModel, s, t, smodel, swrapped, DP.MAGICAL_STATE);
            FlatModel fm = (FlatModel) stateToFlatModel.get(swrapped);
          }
        } else {
          // both wrapped states
          StateWrapper twrapper = (StateWrapper) t;
          State twrapped = twrapper.getWrappedState();
          ModelInState tstate = twrapper.getContainingState();
          MarkovModel tmodel = tstate.getModel();
          StateWrapper swrapper = (StateWrapper) s;
          State swrapped = swrapper.getWrappedState();
          ModelInState sstate = swrapper.getContainingState();
          MarkovModel smodel = sstate.getModel();
          // System.out.println(s.getName() + " -> " + t.getName());
          if(sstate == tstate) {
            // both from the same ModelInState
            //System.out.println("Same model in state");
            if(smodel.transitionsFrom(swrapped).contains(twrapped)) {
              // System.out.println("Joined by sub-moddel join");
              addTransition(stateToFlatModel, s, t, smodel, swrapped, twrapped);
            } else if(model.transitionsFrom(sstate).contains(tstate) &&
                      smodel.transitionsFrom(swrapped).contains(DP.MAGICAL_STATE) &&
                      tmodel.transitionsFrom(DP.MAGICAL_STATE).contains(twrapped)
            ){
              // System.out.println("Joined by containing-state to self transition");
              addTransition(stateToFlatModel, s, t, model, sstate, tstate);
              addTransition(stateToFlatModel, s, t, smodel, swrapped, DP.MAGICAL_STATE);
              addTransition(stateToFlatModel, s, t, tmodel, DP.MAGICAL_STATE, twrapped);
            }
          } else {
            // different models
            // System.out.println("Different model in state");
            if(smodel.transitionsFrom(swrapped).contains(DP.MAGICAL_STATE) &&
               model.transitionsFrom(sstate).contains(tstate) &&
               tmodel.transitionsFrom(DP.MAGICAL_STATE).contains(twrapped)) {
              addTransition(stateToFlatModel, s, t, smodel, swrapped, DP.MAGICAL_STATE);
              addTransition(stateToFlatModel, s, t, model, sstate, tstate);
              addTransition(stateToFlatModel, s, t, tmodel, DP.MAGICAL_STATE, twrapped);
            }
          }
        }
      }
    }
    
    this.stateAlphabet = stateAlphabet;
  }
  
  private class SimpleStateWrapper extends SimpleResidue implements StateWrapper {
    private EmissionState wrappedState;
    private ModelInState modelInState;
    
    public EmissionState getWrappedState() {
      return wrappedState;
    }
    
    public ModelInState getContainingState() {
      return modelInState;
    }
    
    public Residue sampleResidue()
    throws SeqException {
      return wrappedState.sampleResidue();
    }
    
    public double getWeight(Residue res)
    throws IllegalResidueException {
      return wrappedState.getWeight(res);
    }
    
    public void setWeight(Residue res, double weight)
    throws IllegalResidueException {
      wrappedState.setWeight(res, weight);
    }
    
    public Alphabet alphabet() {
      return wrappedState.alphabet();
    }
    
    public void registerWithTrainer(ModelTrainer modelTrainer) {
      System.out.println("registering wrapper " + this.getName());
      wrappedState.registerWithTrainer(modelTrainer);
      for(Iterator i = modelTrainer.trainersForState(wrappedState).iterator(); i.hasNext();) {
        StateTrainer st = (StateTrainer) i.next();
        modelTrainer.registerTrainerForState(this, st);
      }
    }
    
      public int[] getAdvance() {
	  return wrappedState.getAdvance();
      }

    public SimpleStateWrapper(EmissionState wrappedState, ModelInState modelInState) {
      super(modelInState.getSymbol(), modelInState.getName() + "-" + wrappedState.getName(), null);
      this.wrappedState = wrappedState;
      this.modelInState = modelInState;
    }
  }
}
