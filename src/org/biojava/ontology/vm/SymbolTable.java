package org.biojava.ontology.vm;

import org.biojava.ontology.Variable;
import org.biojava.ontology.Term;
import org.biojava.utils.SmallSet;

import java.util.Set;

/**
 *
 *
 * @author Matthew Pocock
 */
public class SymbolTable {
  public static final SymbolTable EMPTY;

  static {
    EMPTY = new SymbolTable(null, null, null);
  }

  private final SymbolTable prev;
  private final Variable var;
  private final Term val;

  private SymbolTable(SymbolTable prev, Variable var, Term val) {
    this.prev = prev;
    this.var = var;
    this.val = val;
  }

  public Term getValue(Variable var) {
    if(this.var == var) {
      return val;
    }

    if(this.prev != null) {
      return prev.getValue(var);
    }

    return null;
  }

  public SymbolTable bind(Variable var, Term val) {
    return new SymbolTable(this, var, val);
  }

  /**
   * A set of all variables with bindings in this SymbolTable.
   *
   * <p>
   * The set returned by this method will always return an iterator that loops
   * over the variables in the order they where bound.
   * </p>
   *
   * @return
   */
  public Set variables() {
    Set vars = new SmallSet();
    populateVars(vars);
    return vars;
  }

  private void populateVars(Set vars) {
    if(this != EMPTY) {
      vars.add(var);
      prev.populateVars(vars);
    }
  }

  public String toString() {
    if(this == EMPTY) {
      return "[]";
    }

    String main = "[" + var + " -> " + val + "]";

    if(prev != null) {
      return main + ", " + prev.toString();
    }

    return main;
  }
}
