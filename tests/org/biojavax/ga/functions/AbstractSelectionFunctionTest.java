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

import junit.framework.TestCase;

import org.biojava.utils.ChangeVetoException;
import org.biojavax.ga.GeneticAlgorithm;
import org.biojavax.ga.Organism;
import org.biojavax.ga.Population;


/**
 * @author Mark Schreiber
 */
public class AbstractSelectionFunctionTest extends TestCase{

  private AbstractSelectionFunction func = null;

  public AbstractSelectionFunctionTest(String name){
    super(name);
  }

  public void setUp() throws Exception{
    super.setUp();
    func = new SelectionFunction.Threshold(1.0);
  }

  public void tearDown() throws Exception{
    super.tearDown();
    func = null;
  }

  public void testSetAndGetFitnessFunction(){
    FitnessFunction ff = new FitnessFunction(){
      public double fitness(Organism org, Population pop, GeneticAlgorithm genAlg){
        return 1.0;
      }
    };

    try {
      func.setFitnessFunction(ff);
      assertEquals( ff, func.getFitnessFunction());
    }
    catch (ChangeVetoException ex) {
      fail(ex.getMessage());
    }
  }
}
