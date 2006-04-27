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

import org.biojava.utils.*;

/**
 * <p> Abstract implementation of <code>FitnessFunction</code>.
 * All custom implementations should inherit from here to get access to
 * change support.</p>
 *
 * @author Mark Schreiber
 * @version 1.0
 * @since 1.5
 */

public abstract class AbstractSelectionFunction
    extends AbstractChangeable implements SelectionFunction{
  private FitnessFunction fit = null;

  protected AbstractSelectionFunction(){

  }

  public FitnessFunction getFitnessFunction(){
    return fit;
  }

  public final void setFitnessFunction(FitnessFunction func) throws ChangeVetoException{
    if(!hasListeners()){
      fit = func;
    }else{
      ChangeEvent ce = new ChangeEvent(this,
                                       SelectionFunction.FITNESS_FUNCTION,
                                       func,
                                       fit
                                       );
      ChangeSupport changeSupport = super.getChangeSupport(SelectionFunction.FITNESS_FUNCTION);
      synchronized(changeSupport){
        changeSupport.firePreChangeEvent(ce);
        fit = func;
        changeSupport.firePostChangeEvent(ce);
      }
    }
  }
}