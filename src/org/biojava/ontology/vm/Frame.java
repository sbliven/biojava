package org.biojava.ontology.vm;

import org.biojava.ontology.vm.Action;
import org.biojava.ontology.Term;
import org.biojava.ontology.Variable;

/**
 *
 *
 * @author Matthew Pocock
 */
public class Frame {
  private final Action action;
  private final Term axiom;
  private final Term prop;
  private final Term result;
  private final SymbolTable symTable;

  public Frame(Action action, Term axiom, Term prop) {
    this(action, axiom, prop, null, SymbolTable.EMPTY);
  }

  private Frame(Action action, Term axiom, Term prop, Term result, SymbolTable symTable) {
    this.action = action;
    this.axiom = axiom;
    this.prop = prop;
    this.result = result;
    this.symTable = symTable;
  }

  public Action getAction() {
    return action;
  }

  public Term getAxiom()
  {
    return axiom;
  }

  public Term getProp() {
    return prop;
  }

  public Term getResult() {
    return result;
  }

  public SymbolTable getSymbolTable() {
    return symTable;
  }

  public Frame changeAction(Action action) {
    return new Frame(action, axiom, prop, result, symTable);
  }

  public Frame changeAxiom(Term axiom) {
    return new Frame(action, axiom, prop, result, symTable);
  }

  public Frame changeProp(Term prop) {
    return new Frame(action, axiom, prop, result, symTable);
  }

  public Frame changeResult(Term result) {
    return new Frame(action, axiom, prop, result, symTable);
  }

  public Frame bind(Variable var, Term val) {
    return new Frame(action, axiom, prop, result, symTable.bind(var, val));
  }

  public Frame changeSymbolTable(SymbolTable symTable) {
    return new Frame(action, axiom, prop, result, symTable);
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(super.toString());
    sb.append("\n\taction:\t");
    sb.append(action);
    sb.append("\n\taxiom:\t");
    sb.append(axiom);
    sb.append("\n\tprop:\t");
    sb.append(prop);
    sb.append("\n\tresult:\t" + result);
    sb.append("\n\tsymbolTab:\t" + symTable);

    return sb.toString();
  }
}
