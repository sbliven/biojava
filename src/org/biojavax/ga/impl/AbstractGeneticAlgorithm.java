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


package org.biojavax.ga.impl;

import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.ga.GeneticAlgorithm;
import org.biojavax.ga.Population;
import org.biojavax.ga.functions.CrossOverFunction;
import org.biojavax.ga.functions.MutationFunction;
import org.biojavax.ga.functions.SelectionFunction;

/**
 * Base class from which most implementations of GeneticAlgorithm will inherit.
 * 
 * @author Mark Schreiber
 * @version 1.0
 * @since 1.5
 */

public abstract class AbstractGeneticAlgorithm
    extends AbstractChangeable implements GeneticAlgorithm {

  protected Population population;
  private CrossOverFunction crossF;
  private SelectionFunction selectF;
  private MutationFunction mutF;

  protected AbstractGeneticAlgorithm(){
    population = new SimplePopulation();
  }

  public final void setPopulation(Population pop) throws ChangeVetoException {
    if(!hasListeners()){
      population = pop;
    }else{
      ChangeEvent ce = new ChangeEvent(this,
                                       GeneticAlgorithm.POPULATION,
                                       pop,
                                       this.population
                                       );
      ChangeSupport changeSupport = super.getChangeSupport(GeneticAlgorithm.POPULATION);
      synchronized(changeSupport){
        changeSupport.firePreChangeEvent(ce);
        population = pop;
        changeSupport.firePostChangeEvent(ce);
      }
    }
  }


  public final Population getPopulation(){
    return population;
  }

  public final void setSelectionFunction(SelectionFunction function) throws ChangeVetoException {
    if(!hasListeners()){
      this.selectF = function;
   }else{
     ChangeEvent ce = new ChangeEvent(this,
                                      GeneticAlgorithm.POPULATION,
                                      function,
                                      this.selectF
                                      );
     ChangeSupport changeSupport = super.getChangeSupport(GeneticAlgorithm.POPULATION);
     synchronized(changeSupport){
       changeSupport.firePreChangeEvent(ce);
       this.selectF = function;
       changeSupport.firePostChangeEvent(ce);
     }
    }
  }

  public final SelectionFunction getSelectionFunction() {
    return selectF;
  }
  public final void setCrossOverFunction(CrossOverFunction function) throws ChangeVetoException {
    if(!hasListeners()){
      this.crossF = function;
   }else{
     ChangeEvent ce = new ChangeEvent(this,
                                      GeneticAlgorithm.POPULATION,
                                      function,
                                      this.crossF
                                      );
     ChangeSupport changeSupport = super.getChangeSupport(GeneticAlgorithm.POPULATION);
     synchronized(changeSupport){
       changeSupport.firePreChangeEvent(ce);
       this.crossF = function;
       changeSupport.firePostChangeEvent(ce);
     }
    }
  }
  public final CrossOverFunction getCrossOverFunction() {
    return crossF;
   }
  public final void setMutationFunction(MutationFunction function) throws ChangeVetoException {
    if(!hasListeners()){
      this.mutF = function;
   }else{
     ChangeEvent ce = new ChangeEvent(this,
                                      GeneticAlgorithm.POPULATION,
                                      function,
                                      this.mutF
                                      );
     ChangeSupport changeSupport = super.getChangeSupport(GeneticAlgorithm.POPULATION);
     synchronized(changeSupport){
       changeSupport.firePreChangeEvent(ce);
       this.mutF = function;
       changeSupport.firePostChangeEvent(ce);
     }
    }
  }
  public final MutationFunction getMutationFunction() {
    return mutF;
 }
}