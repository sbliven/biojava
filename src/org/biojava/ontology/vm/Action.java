package org.biojava.ontology.vm;

import org.biojava.ontology.vm.Interpreter;

/**
 *
 *
 * @author Matthew Pocock
 */
public interface Action {
  void evaluate(Interpreter interpreter);
}
