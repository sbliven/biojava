package org.biojava.ontology.vm;

import org.biojava.ontology.*;

/**
 *
 *
 * @author Matthew Pocock
 */
public final class EqualActions {
  private EqualActions() {}

  public final static Action EQUIVALENT;
  public final static Action EQUIVALENT_ATOM;
  public final static Action EQUIVALENT_TRIPLE;
  public final static Action EQUIVALENT_PREDICATE;
  public final static Action EQUIVALENT_SUBJECT;
  public final static Action EQUIVALENT_OBJECT;

  static {
    EQUIVALENT = new Equivalent();
    EQUIVALENT_ATOM = new EquivalentAtom();
    EQUIVALENT_TRIPLE = new EquivalentTriple();
    EQUIVALENT_PREDICATE = new EquivalentPredicate();
    EQUIVALENT_SUBJECT = new EquivalentSubject();
    EQUIVALENT_OBJECT = new EquivalentObject();
  }

  private static class Equivalent
          implements Action {
    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();

      boolean pt = frame.getProp() instanceof Triple;
      boolean at = frame.getAxiom() instanceof Triple;

      if(pt != at) {
        frame = interpreter.popFrame();
        interpreter.pushFrame(frame.changeResult(OntoTools.FALSE));
      } else if(pt) {
        interpreter.pushFrame(frame.changeAction(EQUIVALENT_TRIPLE));
      } else {
        interpreter.pushFrame(frame.changeAction(EQUIVALENT_ATOM));
      }
    }

    public String toString() {
      return "EQUIVALENT";
    }
  }

  private static class EquivalentAtom
          implements Action {
    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame parent = interpreter.popFrame();

      Term axiom = ReasoningTools.resolveRemote(frame.getAxiom());
      Term prop = ReasoningTools.resolveRemote(frame.getProp());

      // variables involved - resolve them
      boolean propV = prop instanceof Variable;
      boolean axiomV = axiom instanceof Variable;

      if(propV) {
        interpreter.pushFrame(parent
                              .bind((Variable) prop, axiom)
                              .changeResult(OntoTools.TRUE));

      } else if(axiomV) {
        interpreter.pushFrame(parent
                              .bind((Variable) axiom, prop)
                              .changeResult(OntoTools.TRUE));
      } else {
        // no variables - do a straight equivalence test
        interpreter.pushFrame(parent.changeResult(
                ReasoningTools.booleanToTerm(prop == axiom)));
      }
    }

    public String toString()
    {
      return "EQUIVALENT_ATOM";
    }
  }

  private static class EquivalentTriple
          implements Action {
    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();

      // check predicate
      interpreter.pushFrame(frame = frame.changeAction(EQUIVALENT_PREDICATE));
    }

    public String toString()
    {
      return "EQUIVALENT_TRIPLE";
    }
  }

  private static class EquivalentPredicate
          implements Action {
    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();

      Term prPred = ((Triple) ReasoningTools.resolveRemote(frame.getProp())).getPredicate();
      Term axPred = ((Triple) ReasoningTools.resolveRemote(frame.getAxiom())).getPredicate();
      interpreter.pushFrame(frame = frame.changeAction(EQUIVALENT_SUBJECT));
      interpreter.pushFrame(frame.changeAction(EQUIVALENT)
                            .changeProp(prPred)
                            .changeAxiom(axPred));
    }

    public String toString()
    {
      return "EQUIVALENT_PREDICATE";
    }
  }

  private static class EquivalentSubject
          implements Action {
    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();

      if(ReasoningTools.isFalse(frame.getResult())) {
        frame = interpreter.popFrame();
        interpreter.pushFrame(frame.changeResult(OntoTools.FALSE));
      } else {
        Term prSub = ((Triple) ReasoningTools.resolveRemote(frame.getProp())).getSubject();
        Term axSub = ((Triple) ReasoningTools.resolveRemote(frame.getAxiom())).getSubject();
        interpreter.pushFrame(frame = frame.changeAction(EQUIVALENT_OBJECT));
        interpreter.pushFrame(frame.changeAction(EQUIVALENT)
                              .changeProp(prSub)
                              .changeAxiom(axSub));
      }
    }

    public String toString()
    {
      return "EQUIVALENT_SUBJECT";
    }

  }

  private static class EquivalentObject
          implements Action {
    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();

      if(ReasoningTools.isFalse(frame.getResult())) {
        frame = interpreter.popFrame();
        interpreter.pushFrame(frame.changeResult(OntoTools.FALSE));
      } else {
        Term prObj = ((Triple) ReasoningTools.resolveRemote(frame.getProp())).getObject();
        Term axObj = ((Triple) ReasoningTools.resolveRemote(frame.getAxiom())).getObject();
        interpreter.pushFrame(frame = frame.changeAction(LogicalActions.RETURN_RESULT));
        interpreter.pushFrame(frame.changeAction(EQUIVALENT)
                              .changeProp(prObj)
                              .changeAxiom(axObj));
      }
    }

    public String toString()
    {
      return "EQUIVALENT_OBJECT";
    }

  }
}
