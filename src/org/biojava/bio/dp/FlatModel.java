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

/**
 * A model that guarantees to only contain emission states and dot states.
 * <P>
 * A flat model is essentialy a view onto a more complicated model that makes it
 * appear to only contain emission states and dot states. States that emit models
 * and other exotica are expressed purely in terms of these two state types.
 * <P>
 * You can train the resulting flat model, and the underlying models will be altered.
 */
public class FlatModel implements MarkovModel {
  private final MarkovModel source;
  private final MarkovModel flat;
  
  public Alphabet stateAlphabet() {
    return flat.stateAlphabet();
  }
  
  public Alphabet emissionAlphabet() {
    return flat.emissionAlphabet();
  }
  
  public MagicalState magicalState() {
    return flat.magicalState();
  }
  
  public int heads() {
    return flat.heads();
  }
  
  public boolean containsTransition(State from, State to)
  throws IllegalResidueException {
    return flat.containsTransition(from, to);
  }
  
  public void createTransition(State from, State to)
  throws IllegalResidueException, UnsupportedOperationException {
    Alphabet a = stateAlphabet();
    a.validate(from);
    a.validate(to);
    throw new UnsupportedOperationException("createTransition not supported by FlatModel");
  }

  public void destroyTransition(State from, State to)
  throws IllegalResidueException, UnsupportedOperationException {
    Alphabet a = stateAlphabet();
    a.validate(from);
    a.validate(to);
    throw new UnsupportedOperationException("destroyTransition not supported by FlatModel");
  }

  public void setTransitionScore(State from, State to, double score)
  throws IllegalResidueException, IllegalTransitionException,
  UnsupportedOperationException {
    throw new UnsupportedOperationException("setTransitionScore not supported by FlatModel");
  }

  public State sampleTransition(State from) throws IllegalResidueException {
    return flat.sampleTransition(from);
  }
  
  public Set transitionsFrom(State from) throws IllegalResidueException {
    return flat.transitionsFrom(from);
  }
  
  public Set transitionsTo(State to) throws IllegalResidueException {
    return flat.transitionsTo(to);
  }

  public void addState(State toAdd)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException("addState not supported by FlatModel");
  }

  public void removeState(State toGo)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException("removeState not supported by FlatModel");
  }
  
  public double getTransitionScore(State from, State to)
  throws IllegalResidueException, IllegalTransitionException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);
    if(!flat.containsTransition(from, to)) {
      throw new IllegalTransitionException(from, to, "Transition does not exist");
    }
    
    State from2, to2;
    MarkovModel mod;
    if(from instanceof StateProxy) {
      from2 = ((StateProxy) from).getWrappedState();
      mod = ((StateProxy) from).getSourceModel();
      if(to instanceof MagicalState) {
        to2 = to;
      } else if(to instanceof StateProxy) {
        to2 = ((StateProxy) to).getWrappedState();
      } else {
        to2 = ((StateWrapper) to).getModelState();
      }
    } else if(to instanceof StateProxy) {
      to2 = ((StateProxy) to).getWrappedState();
      mod = ((StateProxy) to).getSourceModel();
      if(from instanceof MagicalState) {
        from2 = from;
      } else if(from instanceof StateProxy) {
        from2 = ((StateProxy) from).getWrappedState();
      } else {
        from2 = ((StateWrapper) from).getModelState();
      }
    } else {
      from2 = ((StateWrapper) from).getModelState();
      to2 = ((StateWrapper) to).getModelState();
      mod = ((ModelInState) from2).getModel();
    }
    
    return mod.getTransitionScore(from2, to2);
  }

  public void registerWithTrainer(ModelTrainer modelTrainer) {
    if(modelTrainer.getTrainerForModel(flat) == null) {
      TransitionTrainer tTrainer = new SimpleTransitionTrainer(flat);
      try {
        modelTrainer.registerTrainerForModel(flat, tTrainer);
      } catch (SeqException se) {
        throw new BioError(se, "Can't register trainer for model, even though " + 
          " there is no trainer associated with the model");
      }
      for(Iterator i = stateAlphabet().residues().iterator(); i.hasNext(); ) {
        State s = (State) i.next();
        if(s instanceof EmissionState) {
          ((EmissionState) s).registerWithTrainer(modelTrainer);
        }
        if(s instanceof StateWrapper) {
          ((StateWrapper) s).getModelState().getModel().registerWithTrainer(modelTrainer);
        }
        try {
          for(Iterator j = transitionsFrom(s).iterator(); j.hasNext(); ) {
            State t = (State) j.next();
            modelTrainer.registerTrainerForTransition(s, t, tTrainer, s, t);
          }
        } catch (IllegalResidueException ire) {
          throw new BioError(ire, "State " + s.getName() +
                             " listed in alphabet " +
                             stateAlphabet().getName() + " dissapeared.");
        } catch (SeqException se) {
          throw new BioError(se, "Somehow, my trainer is not registered.");
        }
      }
    }
  }
  
  public FlatModel(MarkovModel model)
  throws IllegalResidueException, IllegalAlphabetException {
    this.source = model;
    
    flat = new SimpleMarkovModel(model.heads(), model.emissionAlphabet());

    // add all the states
    System.out.println("Adding states");
    Map old2from = new HashMap();
    Map old2to = new HashMap();
    for(Iterator i = model.stateAlphabet().residues().iterator(); i.hasNext(); ) {
      State s = (State) i.next();
      if(s instanceof DotState) { // simple dot state in model
        DotStateProxy dsw = new DotStateProxy(s, model, "." + s.getName());
        old2from.put(s, dsw);
        old2to.put(s, dsw);
        flat.addState(dsw);
        System.out.println("Added " + dsw.getName());
      } else if(s == model.magicalState()) { // magical state
        old2from.put(model.magicalState(), model.magicalState());
        old2to.put(model.magicalState(), model.magicalState());
      } else if(s instanceof EmissionState) {  // simple emission state in model
        EmissionStateProxy esw = new EmissionStateProxy(
          (EmissionState) s, model, s.getName()
        );
        old2from.put(s, esw);
        old2to.put(s, esw);
        flat.addState(esw);
        System.out.println("Added " + esw.getName());
      } else if(s instanceof ModelInState) { // complex model inside state
        ModelInState mis = (ModelInState) s;
        MarkovModel mism = mis.getModel();
        FlatModel flatM = new FlatModel(mism);
        for(Iterator j = flatM.stateAlphabet().residues().iterator(); j.hasNext(); ) {
          State t = (State) j.next();
          if(t instanceof EmissionState) {
            EmissionStateWrapper esw = new SimpleEmissionStateWrapper(
              (EmissionStateWrapper) t, mis
            );
            old2from.put(t, esw);
            old2to.put(t, esw);
            flat.addState(esw);
            System.out.println("Added " + esw.getName());
          } else if(t instanceof DotState) {
            DotStateWrapper dsw = new DotStateWrapper(t, mis, "." + t.getName());
            old2from.put(t, dsw);
            old2to.put(t, dsw);
            flat.addState(dsw);
            System.out.println("Added " + dsw.getName());
          } else if(t == mism.magicalState()) {
            DotStateWrapper start = new DotStateWrapper(mism.magicalState(), mis, "start");
            DotStateWrapper end = new DotStateWrapper(mism.magicalState(), mis, "end");
            old2from.put(model.magicalState(), start);
            old2to.put(model.magicalState(), end);
            old2from.put(s, end);
            old2to.put(s, start);
            flat.addState(start);
            flat.addState(end);
            System.out.println("Added " + start.getName() + " and " + end.getName());
          }
        }
      } else { // unknown eventuality
        throw new IllegalResidueException(s, "Don't know how to handle state: " + s.getName());
      }
    }

    // wire them
    for(Iterator i = flat.stateAlphabet().residues().iterator(); i.hasNext(); ) {
      State s = (State) i.next();
      System.out.println("Processing transitions involving " + s.getName());
      if(s instanceof MagicalState) {
      } else if(s instanceof StateProxy) {
        StateProxy sp = (StateProxy) s;
        State swrapped = sp.getWrappedState();
        MarkovModel smod = sp.getSourceModel();
        for(Iterator j = smod.transitionsFrom(swrapped).iterator(); j.hasNext(); ) {
          State twrapped = (State) j.next();
          State t = (State) old2to.get(twrapped);
          if(!flat.containsTransition(s, t)) {
            flat.createTransition(s, t);
            System.out.println("Created transition " + s.getName() + " -> " + t.getName());
          }
        }
        for(Iterator j = smod.transitionsTo(swrapped).iterator(); j.hasNext(); ) {
          State twrapped = (State) j.next();
          State t = (State) old2to.get(twrapped);
          if(!flat.containsTransition(t, s)) {
            flat.createTransition(t, s);
            System.out.println("Created transition " + t.getName() + " -> " + s.getName());
          }
        }
      } else if(s instanceof StateWrapper) {
        StateWrapper sw = (StateWrapper) s;
        State swrapped = sw.getWrappedState();
        MarkovModel wmod = sw.getModelState().getModel();
        for(Iterator j = wmod.transitionsFrom(swrapped).iterator(); j.hasNext(); ) {
          State twrapped = (State) j.next();
          flat.createTransition(s, (State) old2to.get(twrapped));
          System.out.println(
            "Transition " + s.getName() + " -> " +
            ((State) old2to.get(twrapped)).getName()
          );
        }
        for(Iterator j = wmod.transitionsTo(swrapped).iterator(); j.hasNext(); ) {
          State twrapped = (State) j.next();
          flat.createTransition((State) old2from.get(twrapped), s);
          System.out.println(
            "Transition " + ((State) old2from.get(twrapped)).getName() + " -> " +
            s
          );
        }
      }
    }
    System.out.println("Done");
  }
  
  public static interface StateProxy extends State {
    public State getWrappedState();
    public MarkovModel getSourceModel();
  }
  
  private static class DotStateProxy extends DotState implements StateProxy {
    private final State wrappedState;
    private final MarkovModel sourceModel;
    
    public State getWrappedState() {
      return wrappedState;
    }
    
    public MarkovModel getSourceModel() {
      return sourceModel;
    }
    
    public DotStateProxy(State wrappedState, MarkovModel sourceModel, String name) {
      super(name);
      this.wrappedState = wrappedState;
      this.sourceModel = sourceModel;
    }
  }
  
  private static class EmissionStateProxy extends SimpleResidue implements StateProxy, EmissionState {
    private final EmissionState wrappedState;
    private final MarkovModel sourceModel;
    
    public State getWrappedState() {
      return wrappedState;
    }
    
    public MarkovModel getSourceModel() {
      return sourceModel;
    }
    
    public double getWeight(Residue r) throws IllegalResidueException {
      return wrappedState.getWeight(r);
    }
    
    public void setWeight(Residue r, double score)
    throws IllegalResidueException {
      wrappedState.setWeight(r, score);
    }
    
    public void registerWithTrainer(ModelTrainer trainer) {
      wrappedState.registerWithTrainer(trainer);
    }
    
    public int [] getAdvance() {
      return wrappedState.getAdvance();
    }
    
    public Residue sampleResidue() {
      return wrappedState.sampleResidue();
    }
    
    public Alphabet alphabet() {
      return wrappedState.alphabet();
    }
    
    public EmissionStateProxy(EmissionState wrappedState, MarkovModel sourceModel, String name) {
      super(name.charAt(0), name, Annotation.EMPTY_ANNOTATION);
      this.wrappedState = wrappedState;
      this.sourceModel = sourceModel;
    }
  }
  
  private static class DotStateWrapper extends DotState implements StateWrapper {
    private final State wrappedState;
    private final ModelInState modelState;
    
    public State getWrappedState() {
      return wrappedState;
    }
    
    public ModelInState getModelState() {
      return modelState;
    }

    public DotStateWrapper(State wrappedState, ModelInState modelState, String name)
    throws IllegalArgumentException {
      super(name);
      if(
        wrappedState instanceof EmissionState ||
        wrappedState instanceof ModelInState
      ) {
        throw new IllegalArgumentException(
          "DotStateWrapper must wrap a silent state, not " + wrappedState
        );
      }
      this.wrappedState = wrappedState;
      this.modelState = modelState;
    }
  }

  private static class SimpleStateWrapper extends SimpleResidue implements StateWrapper {
    private final State wrappedState;
    private final ModelInState modelState;
    
    public State getWrappedState() {
      return wrappedState;
    }
    
    public ModelInState getModelState() {
      return modelState;
    }

    public SimpleStateWrapper(State wrappedState, ModelInState modelState) {
      super(modelState.getSymbol(), modelState.getName() + "-" + wrappedState.getName(), null);
      this.wrappedState = wrappedState;
      this.modelState = modelState;
    }
  }
  
  private static class SimpleEmissionStateWrapper extends SimpleStateWrapper implements EmissionStateWrapper {
    public Residue sampleResidue() {
      return ((EmissionState) getWrappedState()).sampleResidue();
    }
    
    public double getWeight(Residue res)
    throws IllegalResidueException {
      return ((EmissionState) getWrappedState()).getWeight(res);
    }
    
    public void setWeight(Residue res, double weight)
    throws IllegalResidueException {
      ((EmissionState) getWrappedState()).setWeight(res, weight);
    }
    
    public Alphabet alphabet() {
      return ((EmissionState) getWrappedState()).alphabet();
    }
    
    public void registerWithTrainer(ModelTrainer modelTrainer) {
      ((EmissionState) getWrappedState()).registerWithTrainer(modelTrainer);
      for(
        Iterator i = modelTrainer.trainersForState(
          ((EmissionState) getWrappedState())
        ).iterator();
        i.hasNext();
      ) {
        StateTrainer st = (StateTrainer) i.next();
        modelTrainer.registerTrainerForState(this, st);
      }
    }
    
    public int[] getAdvance() {
      return ((EmissionState) getWrappedState()).getAdvance();
    }

    public SimpleEmissionStateWrapper(EmissionState wrappedState, ModelInState modelState) {
      super(wrappedState, modelState);
    }
  }
}
