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

/**
 * A model that exposes some translated view of another model.
 * <P>
 * At the moment it is assumed that ModelView contains a 1-2-1 mapping between
 * the states and transitions in the view and the underlying model.
 *
 * @author Matthew Pocock
 */
public abstract class ModelView implements MarkovModel {
  private final Map transFrom;
  private final Map transTo;
  private final Map transModelTrans;
  private Transition _trans;

  {
    transFrom = new HashMap();
    transTo = new HashMap();
    transModelTrans = new HashMap();
    _trans = new Transition(null, null);
  }
  
  public abstract MarkovModel getSource();
  
  public void addState(State s) {
    transFrom.put(s, new HashSet());
    transTo.put(s, new HashSet());
  }
  
  public State sampleTransition(State from) throws IllegalSymbolException {
    stateAlphabet().validate(from);
    
    double p = Math.random();
    try {
      for(Iterator i = transitionsFrom(from).iterator(); i.hasNext(); ) {
        State s = (State) i.next();
        if( (p -= Math.exp(getTransitionScore(from, s))) <= 0 )
        return s;
      }
    } catch (IllegalSymbolException ire) {
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
    throw new IllegalSymbolException("Could not find transition from state " +
                                      from.getName() +
                                      ". Do the probabilities sum to 1?" +
                                      "\np=" + p + "\n" + sb.toString());
  }


  protected ModelTransition getMT(State from, State to)
  throws IllegalSymbolException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);
    
    _trans.from = from;
    _trans.to = to;
    
    return (ModelTransition) transModelTrans.get(_trans);
  }
  
  public boolean containsTransition(State from, State to)
  throws IllegalSymbolException {
    return getMT(from, to) != null;
  }
  
  public Set transitionsFrom(State from) throws IllegalSymbolException {
    return (Set) transFrom.get(from);
  }
  
  public Set transitionsTo(State to) throws IllegalSymbolException {
    return (Set) transTo.get(to);
  }
  
  public double getTransitionScore(State from, State to)
  throws IllegalSymbolException, IllegalTransitionException {
    ModelTransition mt = getMT(from, to);
    if(mt != null) {
      return mt.model.getTransitionScore(mt.from, mt.to);
    }
    throw new IllegalTransitionException(from, to);
  }

  public void registerWithTrainer(ModelTrainer modelTrainer)
  throws BioException {
    TransitionTrainer thisT = modelTrainer.getTrainerForModel(this);
    if(thisT == null) {
      System.out.println(
        "Registering " + getSource().stateAlphabet().getName() +
        " with trainer"
      );
      getSource().registerWithTrainer(modelTrainer);
      TransitionTrainer sourceT = modelTrainer.getTrainerForModel(getSource());
      thisT = new ViewTransitionTrainer();
      modelTrainer.registerTrainerForModel(this, thisT);
      
      for(Iterator i = stateAlphabet().iterator(); i.hasNext(); ) {
        State s = (State) i.next();
        if(s instanceof EmissionState && ! (s instanceof MagicalState) ) {
          EmissionState es = (EmissionState) s;
          es.registerWithTrainer(modelTrainer);
        }
        try {
          for(Iterator j = transitionsFrom(s).iterator(); j.hasNext(); ) {
            State t = (State) j.next();
            ModelTransition mt = getMT(s, t);
            TransitionTrainer tt = modelTrainer.getTrainerForModel(mt.model);
            if(tt == null) {
              throw new BioException(
                "Couldn't find transition trainer for " +
                mt.model.stateAlphabet().getName() + " from " + s.getName() +
                " to " + t.getName()
              );
            }
            modelTrainer.registerTrainerForTransition(
              s, t, tt, mt.from, mt.to
            );
          }
        } catch (IllegalSymbolException ire) {
          throw new BioException(ire, "Symbol dissapeard on me: " + s.getName());
        }
      }
    }
  }
  
  protected void createTransition(
    State from, State to,
    MarkovModel within, State source, State dest
  ) throws IllegalSymbolException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);
    
//    System.out.println("Adding: " + from.getName() + " -> " + to.getName());
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
    } catch (IllegalSymbolException ire) {
      throw new BioError(
        ire,
        "Something is fucked up in ModelView.\nCreating " +
        stateAlphabet().getName() + " " +
        ((from == null) ? "null" : from.getName()) + " -> " +
        ((to == null) ? "null" : to.getName()) +
        "\nwhich corresponds to a transition:\n" +
        ((within == null) ? "null" : within.stateAlphabet().getName()) + " " +
        ((source == null) ? "null" : source.getName()) + " -> " +
        ((dest == null) ? "null" : dest.getName())
      );
    } catch (NullPointerException npe) {
      throw new BioError(
        npe,
        "From or To set was null.\n" +
        ((from == null) ? "null" : from.getName()) + " -> " +
        ((to == null) ? "null" : to.getName()) + "\n" +
        "from set = " + transFrom.get(from) + "\n" +
        "to set = " + transTo.get(to) + "\n"
      );
    }
  }
  
  
  private static class ModelTransition implements Serializable {
    public final MarkovModel model;
    public final State from;
    public final State to;
    
    public ModelTransition(MarkovModel model, State from, State to)
    throws IllegalSymbolException, IllegalArgumentException {
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
  
  public class ViewTransitionTrainer implements TransitionTrainer, Serializable {
    public void addCount(State from, State to, double count)
    throws IllegalSymbolException, IllegalTransitionException {
      return;
    }
    
    public void train(double nullModel, double weight)
    throws IllegalSymbolException {
      return;
    }
    
    public void clearCounts() {
      return;
    }
  }
}

