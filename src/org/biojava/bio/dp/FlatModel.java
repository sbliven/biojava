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
 * A model that guarantees to only contain emission states and dot states.
 * <P>
 * A flat model is essentialy a view onto a more complicated model that makes it
 * appear to only contain emission states and dot states. States that emit models
 * and other exotica are expressed purely in terms of these two state types.
 * <P>
 * You can train the resulting flat model, and the underlying models will be altered.
 */
class FlatModel extends ModelView implements Serializable {
  private MarkovModel source;
  private SimpleAlphabet stateAlpha;
  
  private List transitionListeners;
  
  public MarkovModel getSource() {
    return source;
  }

  public FiniteAlphabet stateAlphabet() {
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
  
  private void addAState(State s) {
    //System.out.println("Adding state: " + s.getName());
    try {
      stateAlpha.addSymbol(s);
      super.addState(s);
    } catch (Exception e) {
      throw new BioError(e, "Something got stuffed up while adding state " + s.getName());
    }
  }
  
  public void addTransitionListener(TransitionListener tl) {
    transitionListeners.add(tl);
  }
  
  public void removeTransitionListener(TransitionListener tl) {
    transitionListeners.remove(tl);
  }
  
  public FlatModel(MarkovModel model)
  throws IllegalSymbolException, IllegalAlphabetException {
    this.source = model;
    this.stateAlpha = new SimpleAlphabet();
    this.transitionListeners = new ArrayList();
    
    stateAlpha.setName("flat " + model.stateAlphabet().getName());
    
    addAState(model.magicalState());
    
    // add all the states
    //System.out.println("Adding states");
    Map toM = new HashMap();
    Map inModel = new HashMap();
    Map misStart = new HashMap();
    Map misEnd = new HashMap();
    Map modelStart = new HashMap();
    Map modelEnd = new HashMap();
    
    for(Iterator i = model.stateAlphabet().iterator(); i.hasNext(); ) {
      State s = (State) i.next();
      if(s instanceof DotState) { // simple dot state in model
        DotStateWrapper dsw = new DotStateWrapper(s);
        addAState(dsw);
        inModel.put(s, model);
        toM.put(s, dsw);
        //System.out.println("Added dot state " + dsw.getName());
      } else if(s instanceof EmissionState) {  // simple emission state in model
        if(s instanceof MagicalState) {
          modelStart.put(model, model.magicalState());
          modelEnd.put(model, model.magicalState());
        } else {
          EmissionWrapper esw =
            new EmissionWrapper((EmissionState) s);
          addAState(esw);
          inModel.put(s, model);
          toM.put(s, esw);
          //System.out.println("Added emission state " + esw.getName());
        }
      } else if(s instanceof ModelInState) { // complex model inside state
        //System.out.println("Adding a model-in-state");
        ModelInState mis = (ModelInState) s;
        MarkovModel flatM = DP.flatView(mis.getModel());

        DotStateWrapper start = new DotStateWrapper(mis, "start");
        DotStateWrapper end = new DotStateWrapper(mis, "end");
        addAState(start);
        addAState(end);
        inModel.put(mis, model);
        modelStart.put(flatM, start);
        modelEnd.put(flatM, end);
        misStart.put(mis, start);
        misEnd.put(mis, end);
        //System.out.println("Added " + start.getName() + " and " + end.getName());

        for(Iterator j = flatM.stateAlphabet().iterator(); j.hasNext(); ) {
          State t = (State) j.next();
          if(t instanceof DotState) {
            DotStateWrapper dsw = new DotStateWrapper(t);
            addAState(dsw);
            inModel.put(t, flatM);
            toM.put(t, dsw);
            toM.put(((Wrapper) t).getWrapped(), dsw);
            //System.out.println("Added wrapped dot state " + dsw.getName());
          } else if(t instanceof EmissionState) {
            if(t instanceof MagicalState) {
              continue;
            }
            EmissionWrapper esw =
              new EmissionWrapper((EmissionState) t);
            addAState(esw);
            inModel.put(t, flatM);
            toM.put(t, esw);
            //toM.put(((Wrapper) t).getWrapped(), esw);
            //System.out.println("Added wrapped emission state " + esw.getName());
          } else { // unknown eventuality
            throw new IllegalSymbolException(s, "Don't know how to handle state: " + s.getName());
          }
        }
      } else { // unknown eventuality
        throw new IllegalSymbolException(s, "Don't know how to handle state: " + s.getName());
      }
    }

    // wire
    for(Iterator i = stateAlpha.iterator(); i.hasNext(); ) {
      State s = (State) i.next();

      //System.out.println("Processing transitions from " + s.getName());

      if(s instanceof MagicalState) { // from magic
        for(Iterator j = model.transitionsFrom(s).iterator(); j.hasNext(); ) {
          State twrapped = (State) j.next();
          if(twrapped instanceof ModelInState) { // Magic -> ModelInState
            State to = (State) misStart.get(twrapped);
            createTransition(
              s, to, 
              model, s, twrapped
            );
          } else { // Magic -> normal
            State to = (State) toM.get(twrapped);
            createTransition(
              s, to,
              model, model.magicalState(), twrapped
            );
          }
        }
      } else { // from not Magic
        Wrapper swrapper = (Wrapper) s;
        State swrapped = swrapper.getWrapped();
        MarkovModel sModel = (MarkovModel) inModel.get(swrapped);
        if(sModel == model) { // state from model, not sub-model
          if(swrapped instanceof ModelInState) { // from ModelInState
            State from = (State) misEnd.get(swrapped);
            if(from == swrapper) { // only consider the from half
              for(
                Iterator j = model.transitionsFrom(swrapped).iterator();
                j.hasNext();
              ) { // deal with transitions with top-level model
                State t = (State) j.next();
                if(t instanceof ModelInState) { // MIS -> MIS
                  State to = (State) misStart.get(t);
                  createTransition(from, to, model, swrapped, t);
                } else if(t instanceof MagicalState) { // MIS -> magic
                  createTransition(from, t, model, swrapped, t);
                } else { // MIS -> normal
                  State to = (State) toM.get(t);
                  createTransition(from, to, model, swrapped, t);
                }
              }
              MarkovModel fromM = ((ModelInState) swrapped).getModel();
              from = (State) misStart.get(swrapped);
              for(
                Iterator j = fromM.transitionsFrom(fromM.magicalState()).iterator();
                j.hasNext();
              ) { // deal with transitions down into wrapped model
                State t = (State) j.next();
                State to = (State) toM.get(t);
                createTransition(from, to, fromM, fromM.magicalState(), t);
              }
            }
          } else { // from normal
            for(
              Iterator j = model.transitionsFrom(swrapped).iterator();
              j.hasNext();
            ) {
              State t = (State) j.next();
              if(t instanceof ModelInState) { // normal -> MIS
                State to = (State) misStart.get(t);
                createTransition(swrapper, to, model, swrapped, t);
              } else if(t instanceof MagicalState) { // normal -> magic
                createTransition(swrapper, t, model, swrapped, t);
              } else { // normal -> normal
                State to = (State) toM.get(t);
                createTransition(swrapper, to, model, swrapped, t);
              }
            }
          }
        } else { // state from sub-model
          for(
            Iterator j = sModel.transitionsFrom(swrapped).iterator();
            j.hasNext();
          ) {
            State t = (State) j.next();
            if(t instanceof MagicalState) { // sub state -> magical
              State to = (State) modelEnd.get(sModel);
              createTransition(swrapper, to, sModel, swrapped, t);
            } else {
              State to = (State) toM.get(t);
              createTransition(swrapper, to, sModel, swrapped, t);
            }
          }
        }
      }
    }
    
    source.addTransitionListener(new TransitionForwarder());
    //System.out.println("Done");
  }
  
  public void createTransition(State from, State to)
  throws IllegalSymbolException, UnsupportedOperationException {
    Alphabet a = stateAlphabet();
    a.validate(from);
    a.validate(to);
    throw new UnsupportedOperationException("createTransition not supported by FlatModel");
  }

  public void destroyTransition(State from, State to)
  throws IllegalSymbolException, UnsupportedOperationException {
    Alphabet a = stateAlphabet();
    a.validate(from);
    a.validate(to);
    throw new UnsupportedOperationException("destroyTransition not supported by FlatModel");
  }

  public void setTransitionScore(State from, State to, double score)
  throws IllegalSymbolException, IllegalTransitionException,
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

  private static class Wrapper implements State, Serializable {
    private final State wrapped;
    private final String extra;

    public char getToken() {
      return wrapped.getToken();
    }
    
    public String getName() {
      return wrapped.getName() + "-" + extra;
    }
    
    public Annotation getAnnotation() {
      return wrapped.getAnnotation();
    }
    
    public State getWrapped() {
      return wrapped;
    }

    public Wrapper(State wrapped, String extra) {
      if(wrapped == null) {
        throw new NullPointerException("Can't wrap null");
      }
      this.wrapped = wrapped;
      this.extra = extra;
    }
  }
  
  private static class DotStateWrapper
  extends Wrapper implements DotState {
    public DotStateWrapper(State wrapped, String extra) {
      super(wrapped, extra);
    }
      
    public DotStateWrapper(State wrapped)
    throws NullPointerException {
      this(wrapped, "f");
    }
  }

  private static class EmissionWrapper
  extends Wrapper implements EmissionState {
    private EmissionState getWrappedES() {
      return (EmissionState) getWrapped();
    }
    
    public int [] getAdvance() {
      return getWrappedES().getAdvance();
    }

    public Distribution getDistribution() {
      return getWrappedES().getDistribution();
    }
    
    public void setDistribution(Distribution dis) {
      getWrappedES().setDistribution(dis);
    }
    
    public void registerWithTrainer(ModelTrainer trainer) {}
    
    public EmissionWrapper(EmissionState wrapped) {
      this(wrapped, "-f");
    }
    
    public EmissionWrapper(EmissionState wrapped, String extra) {
      super(wrapped, extra);
    }
  }
  
  private class TransitionForwarder implements TransitionListener, Serializable {
    public void preCreateTransition(TransitionEvent te)
    throws ModelVetoException {
      synchronized(transitionListeners) {
        /*TransitionEvent te = new TransitionEvent(
          this, from, to, getTransitionScore(from, to), value
        );*/
        for(Iterator i = transitionListeners.iterator(); i.hasNext(); ) {
          ((TransitionListener) i.next()).preCreateTransition(te);
        }
      }
    }
  
    public void postCreateTransition(TransitionEvent te) {
      synchronized(transitionListeners) {
        /*TransitionEvent te = new TransitionEvent(
          this, from, to, getTransitionScore(from, to), value
        );*/
        for(Iterator i = transitionListeners.iterator(); i.hasNext(); ) {
          ((TransitionListener) i.next()).postCreateTransition(te);
        }
      }
    }
  
    public void preDestroyTransition(TransitionEvent te)
    throws ModelVetoException {
      synchronized(transitionListeners) {
        /*TransitionEvent te = new TransitionEvent(
          this, from, to, getTransitionScore(from, to), value
        );*/
        for(Iterator i = transitionListeners.iterator(); i.hasNext(); ) {
          ((TransitionListener) i.next()).preDestroyTransition(te);
        }
      }
    }
  
    public void postDestroyTransition(TransitionEvent te) {
      synchronized(transitionListeners) {
        /*TransitionEvent te = new TransitionEvent(
          this, from, to, getTransitionScore(from, to), value
        );*/
        for(Iterator i = transitionListeners.iterator(); i.hasNext(); ) {
          ((TransitionListener) i.next()).postDestroyTransition(te);
        }
      }
    }
  
    public void preChangeTransitionScore(TransitionEvent te)
    throws ModelVetoException {
      synchronized(transitionListeners) {
        /*TransitionEvent te = new TransitionEvent(
          this, from, to, getTransitionScore(from, to), value
        );*/
        for(Iterator i = transitionListeners.iterator(); i.hasNext(); ) {
          ((TransitionListener) i.next()).preChangeTransitionScore(te);
        }
      }
    }
  
    public void postChangeTransitionScore(TransitionEvent te) {
      synchronized(transitionListeners) {
        /*TransitionEvent te = new TransitionEvent(
          this, from, to, getTransitionScore(from, to), value
        );*/
        for(Iterator i = transitionListeners.iterator(); i.hasNext(); ) {
          ((TransitionListener) i.next()).postChangeTransitionScore(te);
        }
      }
    }
  }
}
