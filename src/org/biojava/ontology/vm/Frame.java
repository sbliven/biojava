package org.biojava.ontology.vm;

import org.biojava.ontology.vm.Action;
import org.biojava.ontology.*;

/**
 *
 *
 * @author Matthew Pocock
 */
public class Frame {
  private final ReasoningDomain rd;
  private final Action action;
  private final Term axiom;
  private final Term prop;
  private final Term result;
  private final SymbolTable symTable;

  public Frame(ReasoningDomain rd, Action action, Term axiom, Term prop) {
    this(rd, action, axiom, prop, null, SymbolTable.EMPTY);
  }

  private Frame(ReasoningDomain rd, Action action, Term axiom, Term prop, Term result, SymbolTable symTable) {
    this.rd = rd;
    this.action = action;
    this.axiom = axiom;
    this.prop = prop;
    this.result = result;
    this.symTable = symTable;
  }

  public ReasoningDomain getRd()
  {
    return rd;
  }

  public Action getAction() {
    return action;
  }

  public Term getAxiom()
  {
    return projectTerm(axiom);
  }

  public Term getProp() {
    return projectTerm(prop);
  }

  public Term getResult() {
    return result;
  }

  public SymbolTable getSymbolTable() {
    return symTable;
  }

  public Frame changeAction(Action action) {
    return new Frame(rd, action, axiom, prop, result, symTable);
  }

  public Frame changeAxiom(Term axiom) {
    return new Frame(rd, action, axiom, prop, result, symTable);
  }

  public Frame changeProp(Term prop) {
    return new Frame(rd, action, axiom, prop, result, symTable);
  }

  public Frame changeResult(Term result) {
    return new Frame(rd, action, axiom, prop, result, symTable);
  }

  public Frame bind(Variable var, Term val) {
    return new Frame(rd, action, axiom, prop, result, symTable.bind(var, val));
  }

  public Frame changeSymbolTable(SymbolTable symTable) {
    return new Frame(rd, action, axiom, prop, result, symTable);
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

  private Term projectTerm(Term term) {
    term = ReasoningTools.resolveRemote(term);

    if(term instanceof Variable) {
      Term val = symTable.getValue((Variable) term);
      while(val instanceof Variable) {
        val = symTable.getValue((Variable) val);
      }
      if(val != null) {
        return val;
      } else {
        return term;
      }
    }

    if(term instanceof Triple) {
      Triple trip = (Triple) term;
      Term sub = projectTerm(trip.getSubject());
      Term obj = projectTerm(trip.getObject());
      Term pred = projectTerm(trip.getPredicate());

      if(sub == trip.getSubject() && obj == trip.getObject() && pred == trip.getPredicate()) {
        return trip;
      } else {
        return rd.createVirtualTerm(sub, obj, pred, null, null);
      }
    }

    return term;
  }
}
