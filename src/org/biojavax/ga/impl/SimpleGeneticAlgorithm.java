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

import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.utils.*;
import org.biojavax.ga.GAStoppingCriteria;
import org.biojavax.ga.Population;
import org.biojavax.ga.Organism;
import org.biojavax.ga.functions.CrossOverFunction;
import org.biojavax.ga.functions.SelectionFunction;
import org.biojavax.ga.functions.MutationFunction;

/**
 * A simple implementation of the <code>GeneticAlgorithm</code> interface
 * it is not intended that this class be overidden, hence it is final.
 * It is much better to overide <code>AbstractGeneticAlgorithm</code>.
 * 
 * @author Mark Schreiber
 * @version 1.0
 * @since 1.5
 */

public final class SimpleGeneticAlgorithm extends AbstractGeneticAlgorithm {
  private int generation = 0;
  private LinkedList crossResults;


  public SimpleGeneticAlgorithm() {
    crossResults = new LinkedList();
  }

  public SimpleGeneticAlgorithm(Population pop,
                                MutationFunction mutFunc,
                                CrossOverFunction xFunc,
                                SelectionFunction selFunc){
    crossResults = new LinkedList();
    try {
      setPopulation(pop);
      setCrossOverFunction(xFunc);
      setMutationFunction(mutFunc);
      setSelectionFunction(selFunc);
    }
    catch (ChangeVetoException ex) {
      //can't veto changes on an unconstructed object
    }
  }

  /**
   * The current generation
   * @return an int giving the generation number
   */
  public int getGeneration() {
    return generation;
  }

  /**
   * Get a List containing details of all the cross over events during the run.
   * If <code>run(GAStoppingCriteria stoppingCriteria) has not yet been called
   * the list will be empty</code>. This implementation only stores a buffer of the
   * last 100 crosses for memory reasons.
   * @return a <code>List</code> of GACrossResult objects.
   */
  public List getCrossResults(){
    return crossResults;
  }

  public synchronized void run(GAStoppingCriteria stoppingCriteria)
           throws ChangeVetoException, IllegalAlphabetException, IllegalSymbolException{
    while(stoppingCriteria.stop(this) != true){
      //select the fit for reproduction
      getSelectionFunction().select(getPopulation(), this);

      //cross pairs of individuals
      Set partners = getPopulation().getOrganisms();
      for (Iterator it = partners.iterator(); it.hasNext(); ) {
        Organism a = (Organism)it.next();
        if(it.hasNext()){
          Organism b = (Organism)it.next();

          try {
            for(int i = 0;
                i < a.getChromosomes().length && i < b.getChromosomes().length;
                i++){

              crossResults.addLast(
                  getCrossOverFunction().performCrossOver(
                    a.getChromosomes()[i], b.getChromosomes()[i])
                  );

              while(crossResults.size() > 100){
                crossResults.removeFirst();
              }
            }
          }
          catch (ChangeVetoException ex) {
            //shouldn't happen as long as sensible implementations of SymbolList
            //are used.
            throw new BioError("Unmodifiable chromosome", ex);
          }
        }else{
          break;
        }
      }

      //mutate
      for (Iterator it = getPopulation().organisms(); it.hasNext(); ) {
        Organism o = (Organism)it.next();
        for (int i = 0; i < o.getChromosomes().length; i++) {
          getMutationFunction().mutate(o.getChromosomes()[i]);
        }

      }

      generation++;
    }
  }

}