package org.biojava.ontology.vm;

import org.biojava.ontology.*;

import java.util.List;
import java.util.Iterator;

/**
 *
 *
 * @author Matthew Pocock
 */
public final class StackActions {
  private StackActions() {}

  public static final Action AXIOM_SUBJECT;
  public static final Action AXIOM_OBJECT;
  public static final Action AXIOM_PREDICATE;

  public static final Action PROP_SUBJECT;
  public static final Action PROP_OBJECT;
  public static final Action PROP_PREDICATE;

  static {
    AXIOM_SUBJECT = new AxiomSubject();
    AXIOM_OBJECT = new AxiomObject();
    AXIOM_PREDICATE = new AxiomPredicate();

    PROP_SUBJECT = new PropSubject();
    PROP_OBJECT = new PropObject();
    PROP_PREDICATE = new PropPredicate();
  }

  public static class Macro
          implements Action {
    private final List actions;

    public Macro(List actions) {
      for(Iterator i = actions.iterator(); i.hasNext(); ) {
        if(i.next() == null) {
          throw new NullPointerException("Null action in: " + actions);
        }
      }

      this.actions = actions;
    }

    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();

      for(Iterator i = actions.iterator(); i.hasNext(); ) {
        Action act = (Action) i.next();
        interpreter.pushFrame(frame.changeAction(act));
      }
    }

    public String toString()
    {
      return "MACRO" + actions.toString();
    }
  }

  public static class BindValue implements Action {
    private final Variable var;
    private Term val;

    public BindValue(Variable var, Term val)
    {
      this.var = var;
      this.val = val;
    }

    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame parent = interpreter.popFrame();

      interpreter.pushFrame(parent.changeSymbolTable(
              frame.getSymbolTable().bind(var, val)));
    }

    public String toString()
    {
      return "BIND_VAULE(" + var + ", " + val + ")";
    }
  }

  private static class AxiomSubject
          implements Action {
    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame parent = interpreter.popFrame();

      Triple trip = (Triple) frame.getAxiom();
      Term val = ReasoningTools.resolveRemote(trip.getSubject());

      interpreter.pushFrame(parent.changeAxiom(val));
    }

    public String toString()
    {
      return "AXIOM_SUBJECT";
    }
  }

  private static class AxiomObject
          implements Action {
    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame parent = interpreter.popFrame();

      Triple trip = (Triple) frame.getAxiom();
      Term val = ReasoningTools.resolveRemote(trip.getObject());

      interpreter.pushFrame(parent.changeAxiom(val));
    }

    public String toString()
    {
      return "AXIOM_OBJECT";
    }
  }

  private static class AxiomPredicate
          implements Action {
    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame parent = interpreter.popFrame();

      Triple trip = (Triple) frame.getAxiom();
      Term val = ReasoningTools.resolveRemote(trip.getPredicate());

      interpreter.pushFrame(parent.changeAxiom(val));
    }

    public String toString()
    {
      return "AXIOM_PREDICATE";
    }
  }

  private static class PropSubject
          implements Action {
    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame parent = interpreter.popFrame();

      Triple trip = (Triple) frame.getProp();
      Term val = ReasoningTools.resolveRemote(trip.getSubject());

      interpreter.pushFrame(parent.changeAxiom(val));
    }

    public String toString()
    {
      return "PROP_SUBJECT";
    }
  }

  private static class PropObject
          implements Action {
    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame parent = interpreter.popFrame();

      Triple trip = (Triple) frame.getProp();
      Term val = ReasoningTools.resolveRemote(trip.getObject());

      interpreter.pushFrame(parent.changeAxiom(val));
    }

    public String toString()
    {
      return "PROP_OBJECT";
    }
  }

  private static class PropPredicate
          implements Action {
    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame parent = interpreter.popFrame();

      Triple trip = (Triple) frame.getProp();
      Term val = ReasoningTools.resolveRemote(trip.getPredicate());

      interpreter.pushFrame(parent.changeAxiom(val));
    }

    public String toString()
    {
      return "PROP_PREDICATE";
    }
  }

  public static final class AxiomSubjectReplace implements Action {
    private final ReasoningDomain rd;

    public AxiomSubjectReplace(ReasoningDomain rd)
    {
      this.rd = rd;
    }

    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame parent = interpreter.popFrame();

      Triple pTrip = (Triple) parent.getAxiom();
      Triple result = rd.createVirtualTerm(frame.getResult(),
                                           pTrip.getObject(),
                                           pTrip.getPredicate(),
                                           null,
                                           null);

      interpreter.pushFrame(parent.changeAxiom(result));
    }

    public String toString()
    {
      return "AXIOM_SUBJECT_REPLACE";
    }
  }

  public static final class AxiomObjectReplace implements Action {
    private final ReasoningDomain rd;

    public AxiomObjectReplace(ReasoningDomain rd)
    {
      this.rd = rd;
    }

    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame parent = interpreter.popFrame();

      Triple pTrip = (Triple) parent.getAxiom();
      Triple result = rd.createVirtualTerm(pTrip.getSubject(),
                                           frame.getResult(),
                                           pTrip.getPredicate(),
                                           null,
                                           null);

      interpreter.pushFrame(parent.changeAxiom(result));
    }

    public String toString()
    {
      return "AXIOM_SUBJECT_REPLACE";
    }
  }
}
