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
  private final SimpleAlphabet stateAlpha;
  private final Map transFrom;
  private final Map transTo;
  private final Map transModelTrans;
  private Transition _trans = new Transition(null, null);
  
  public Alphabet stateAlphabet() {
    return stateAlpha;
  }
  
  public Alphabet emissionAlphabet() {
    return source.emissionAlphabet();
  }
  
  public MagicalState magicalState() {
    return source.magicalState();
  }
  
  public int heads() {
    return source.heads();
  }
  
  public State sampleTransition(State from) throws IllegalResidueException {
    stateAlphabet().validate(from);
    
    double p = Math.random();
    try {
      for(Iterator i = transitionsFrom(from).iterator(); i.hasNext(); ) {
        State s = (State) i.next();
        if( (p -= Math.exp(getTransitionScore(from, s))) <= 0 )
        return s;
      }
    } catch (IllegalResidueException ire) {
    } catch (IllegalTransitionException ite) {
      throw new BioError(ite, "Transition listend in transitionsFrom(" +
                         from.getName() + "has dissapeared.");
    }
    
    StringBuffer sb = new StringBuffer();

    for(Iterator i = transitionsFrom(from).iterator(); i.hasNext(); ) {
      try {
      State s = (State) i.next();
      double t = getTransitionScore(from, s);
      if(t > 0.0)
        sb.append("\t" + s.getName() + " -> " + t + "\n");
      } catch (IllegalTransitionException ite) {
        throw new BioError(ite, "Transition listend in transitionsFrom(" +
                           from.getName() + "has dissapeared.");
      }
    }
    throw new IllegalResidueException("Could not find transition from state " +
                                      from.getName() +
                                      ". Do the probabilities sum to 1?" +
                                      "\np=" + p + "\n" + sb.toString());
  }

  protected ModelTransition getMT(State from, State to)
  throws IllegalResidueException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);
    
    _trans.from = from;
    _trans.to = to;
    
    return (ModelTransition) transModelTrans.get(_trans);
  }
  
  public boolean containsTransition(State from, State to)
  throws IllegalResidueException {
    return getMT(from, to) != null;
  }
  
  public Set transitionsFrom(State from) throws IllegalResidueException {
    return (Set) transFrom.get(from);
  }
  
  public Set transitionsTo(State to) throws IllegalResidueException {
    return (Set) transTo.get(to);
  }
  
  public double getTransitionScore(State from, State to)
  throws IllegalResidueException, IllegalTransitionException {
    ModelTransition mt = getMT(from, to);
    if(mt != null) {
      return mt.model.getTransitionScore(mt.from, mt.to);
    }
    throw new IllegalTransitionException(from, to);
  }

  public void registerWithTrainer(ModelTrainer modelTrainer) {
  }
  
  private void createTransition(
    State from, State to,
    MarkovModel within, State source, State dest
  ) {
    try {
      ModelTransition mt = getMT(from, to);
      if(mt == null) {
        transModelTrans.put(
          new Transition(from, to),
          new ModelTransition(within, source, dest)
        );
      }
      Set f = (Set) transFrom.get(from);
      Set t = (Set) transTo.get(to);
      f.add(to);
      t.add(from);
    } catch (IllegalResidueException ire) {
      throw new BioError(ire, "Something is fucked up in FlatModel - a residue dissapeared");
    }
  }
  
  private void addAState(State s) {
    System.out.println("Adding state: " + s.getName());
    try {
      stateAlpha.addResidue(s);
      transFrom.put(s, new HashSet());
      transTo.put(s, new HashSet());
    } catch (Exception e) {
      throw new BioError(e, "Something got stuffed up while adding state " + s.getName());
    }
  }
  
  public FlatModel(MarkovModel model)
  throws IllegalResidueException, IllegalAlphabetException {
    this.source = model;
    this.stateAlpha = new SimpleAlphabet();
    this.transFrom = new HashMap();
    this.transTo = new HashMap();
    this.transModelTrans = new HashMap();
    
    stateAlpha.setName("flat " + model.stateAlphabet().getName());
    
    addAState(model.magicalState());
    
    // add all the states
    System.out.println("Adding states");
    Map fromM = new HashMap();
    Map toM = new HashMap();
    Map inModel = new HashMap();
    Map modelStart = new HashMap();
    Map modelEnd = new HashMap();
    
    modelStart.put(model, model.magicalState());
    modelEnd.put(model, model.magicalState());
    
    for(Iterator i = model.stateAlphabet().residues().iterator(); i.hasNext(); ) {
      State s = (State) i.next();
      if(s instanceof DotState) { // simple dot state in model
        DotStateWrapper dsw = new DotStateWrapper(s);
        addAState(dsw);
        inModel.put(s, model);
        fromM.put(s, dsw);
        toM.put(s, dsw);
        System.out.println("Added dot state " + dsw.getName());
      } else if(s instanceof EmissionState) {  // simple emission state in model
        if(s instanceof MagicalState) {
          continue;
        }
        EmissionWrapper esw =
          new EmissionWrapper((EmissionState) s);
        addAState(esw);
        inModel.put(s, model);
        fromM.put(s, esw);
        toM.put(s, esw);
        System.out.println("Added emission state " + esw.getName());
      } else if(s instanceof ModelInState) { // complex model inside state
        System.out.println("Adding a model-in-state");
        ModelInState mis = (ModelInState) s;
        MarkovModel mism = mis.getModel();
        FlatModel flatM = new FlatModel(mism);

        DotStateWrapper start = new DotStateWrapper(mis);
        DotStateWrapper end = new DotStateWrapper(mis);
        addAState(start);
        addAState(end);
        inModel.put(s, model);
        fromM.put(s, end);
        toM.put(s, start);
        modelStart.put(mism, start);
        modelEnd.put(mism, end);
        System.out.println("Added " + start.getName() + " and " + end.getName());

        for(Iterator j = flatM.stateAlphabet().residues().iterator(); j.hasNext(); ) {
          State t = (State) j.next();
          if(t instanceof DotState) {
            DotStateWrapper dsw = new DotStateWrapper(t);
            addAState(dsw);
            inModel.put(t, mism);
            fromM.put(t, dsw);
            toM.put(t, dsw);
            System.out.println("Added wrapped dot state " + dsw.getName());
          } else if(t instanceof EmissionState) {
            if(t instanceof MagicalState) {
              continue;
            }
            EmissionWrapper esw =
              new EmissionWrapper((EmissionState) t);
            addAState(esw);
            inModel.put(t, mism);
            fromM.put(t, esw);
            toM.put(t, esw);
            System.out.println("Added wrapped emission state " + esw.getName());
          } else { // unknown eventuality
            throw new IllegalResidueException(s, "Don't know how to handle state: " + s.getName());
          }
        }
      } else { // unknown eventuality
        throw new IllegalResidueException(s, "Don't know how to handle state: " + s.getName());
      }
    }

    // wire them
    for(Iterator i = stateAlpha.residues().iterator(); i.hasNext(); ) {
      State s = (State) i.next();
      System.out.println("Processing transitions involving " + s.getName());
      if(s instanceof MagicalState) {
        continue;
      }
      Wrapper sw = (Wrapper) s;
      State swrapped = sw.getWrapped();
      MarkovModel sModel = (MarkovModel) inModel.get(swrapped);
      if(sModel.containsTransition(sModel.magicalState(), swrapped)) {
        createTransition(
          magicalState(), s,
          model, (State) modelStart.get(model), swrapped
        );
      }
      for(Iterator j = sModel.transitionsFrom(swrapped).iterator(); j.hasNext();) {
        State tw = (State) j.next();
        if(tw instanceof MagicalState) {
          continue;
        }
        State t = (State) toM.get(tw);
        createTransition(s, t, sModel, swrapped, tw);
      }
      for(Iterator j = sModel.transitionsTo(swrapped).iterator(); j.hasNext();) {
        State tw = (State) j.next();
        if(tw instanceof MagicalState) {
          continue;
        }
        State t = (State) fromM.get(tw);
        createTransition(t, s, sModel, tw, swrapped);
      }
    }
    System.out.println("Done");
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

  public void addState(State toAdd)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException("addState not supported by FlatModel");
  }

  public void removeState(State toGo)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException("removeState not supported by FlatModel");
  }

  public static interface Wrapper extends State {
    public State getWrapped();
  }
  
  public static class DotStateWrapper extends DotState implements Wrapper {
    private final State wrapped;
    
    public State getWrapped() {
      return wrapped;
    }
    
    public DotStateWrapper(State wrapped)
    throws NullPointerException {
      super(wrapped.getName());
      if(wrapped == null) {
        throw new NullPointerException("Can't wrap null");
      }
      this.wrapped = wrapped;
    }
  }

  public static class EmissionWrapper
  implements Wrapper, EmissionState {
    private final EmissionState wrapped;
    
    public State getWrapped() {
      return wrapped;
    }

    public char getSymbol() {
      return wrapped.getSymbol();
    }
    
    public String getName() {
      return wrapped.getName() + "-f";
    }
    
    public Annotation getAnnotation() {
      return wrapped.getAnnotation();
    }
    
    public int [] getAdvance() {
      return wrapped.getAdvance();
    }
    
    public void registerWithTrainer(ModelTrainer trainer) {
      wrapped.registerWithTrainer(trainer);
    }
    
    public Residue sampleResidue() {
      return wrapped.sampleResidue();
    }
    
    public double getWeight(Residue r)
    throws IllegalResidueException {
      return wrapped.getWeight(r);
    }
    
    public void setWeight(Residue r, double weight)
    throws IllegalResidueException {
      wrapped.setWeight(r, weight);
    }
    
    public Alphabet alphabet() {
      return wrapped.alphabet();
    }
    
    public EmissionWrapper(EmissionState wrapped)
    throws NullPointerException {
      if(wrapped == null) {
        throw new NullPointerException("Can't wrap null");
      }
      this.wrapped = wrapped;
    }
  }
  
  private static class ModelTransition {
    public final MarkovModel model;
    public final State from;
    public final State to;
    
    public ModelTransition(MarkovModel model, State from, State to)
    throws IllegalResidueException, IllegalArgumentException {
      if(model == null) {
        throw new IllegalArgumentException("Can't use a null model");
      }
      model.stateAlphabet().validate(from);
      model.stateAlphabet().validate(to);

      this.model = model;
      this.from = from;
      this.to = to;      
    }
  }
}
