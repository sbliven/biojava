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


package org.biojavax.ga.functions;
import org.biojavax.ga.Population;
import org.biojavax.ga.Organism;
import org.biojavax.ga.GeneticAlgorithm;

/**
 * Calculates the fitness of an <code>Organism</code>
 * in a <code>Population</code> of <code>Organisms</code>
 *
 * @author Mark Schreiber
 * @version 1.0
 */

public interface FitnessFunction {

  /**
   * Calculates the fitness of <code>org</code>. This can be done independently
   * of the Population pop (by ignoring the argument in your implementation) or
   * dependent on the other members of the <code>Population pop</code>.
   *
   * @param org The <code>Organism</code> to score
   * @param pop The <code>Population</code> to consider
   * @param genAlg the parent<code>GeneticAlgorithm</code>
   * @return the fitness score.
   */
  public double fitness(Organism org, Population pop, GeneticAlgorithm genAlg);
}