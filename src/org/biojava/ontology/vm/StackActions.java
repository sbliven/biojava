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

  public static final Action RESULT_TO_AXIOM;
  public static final Action RESULT_TO_PROP;

  public static final Action AXIOM_SUBJECT_REPLACE;
  public static final Action AXIOM_OBJECT_REPLACE;

  static {
    AXIOM_SUBJECT = new AxiomSubject();
    AXIOM_OBJECT = new AxiomObject();
    AXIOM_PREDICATE = new AxiomPredicate();

    PROP_SUBJECT = new PropSubject();
    PROP_OBJECT = new PropObject();
    PROP_PREDICATE = new PropPredicate();

    RESULT_TO_AXIOM = new ResultToAxiom();
    RESULT_TO_PROP = new ResultToProp();

    AXIOM_SUBJECT_REPLACE = new AxiomSubjectReplace();
    AXIOM_OBJECT_REPLACE = new AxiomObjectReplace();
  }

  public static class Macro
          implements Action {
    private final List actions;
    private final String name;

    public Macro(String name, List actions) {
      for(Iterator i = actions.iterator(); i.hasNext(); ) {
        if(i.next() == null) {
          throw new NullPointerException("Null action in: " + actions);
        }
      }

      this.name = name;
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
      return "MACRO:" + name + actions.toString();
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

  private static final class AxiomSubjectReplace implements Action {
    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame parent = interpreter.popFrame();

      Triple pTrip = (Triple) parent.getAxiom();
      Triple result = frame.getRd().createVirtualTerm(
              frame.getResult(),
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

  private static final class AxiomObjectReplace implements Action {
    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame parent = interpreter.popFrame();

      Triple pTrip = (Triple) parent.getAxiom();
      Triple result = frame.getRd().createVirtualTerm(
              pTrip.getSubject(),
              frame.getResult(),
              pTrip.getPredicate(),
              null,
              null);

      interpreter.pushFrame(parent.changeAxiom(result));
    }

    public String toString()
    {
      return "AXIOM_OBJECT_REPLACE";
    }
  }

  public static class SubstituteProposition
          implements Action {
    private final Term val;

    public SubstituteProposition(Term val)
    {
      this.val = val;
    }

    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame parent = interpreter.popFrame();

      Term axiom = frame.getAxiom();
      Term prop = frame.getProp();

      axiom = ReasoningTools.substitute(axiom,
                                        prop,
                                        val,
                                        frame.getRd());
      interpreter.pushFrame(parent
                            .changeAxiom(axiom)
                            .changeProp(val));
    }

    public String toString()
    {
      return "SUBSTITUTE_PROPOSITION";
    }
  }

  private static class ResultToAxiom
          implements Action
  {
    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      interpreter.pushFrame(interpreter.popFrame()
                            .changeAxiom(frame.getResult()));
    }

    public String toString()
    {
      return "RESULT_TO_AXIOM";
    }
  }

  private static class ResultToProp
          implements Action {
    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      interpreter.pushFrame(interpreter.popFrame()
                            .changeAxiom(frame.getProp()));
    }

    public String toString()
    {
      return "RESULT_TO_PROP";
    }
  }

  public static class Cutback
          implements Action {
    private final Action cutBackTo;

    public Cutback(Action cutBackTo) {
      this.cutBackTo = cutBackTo;
    }

    public Action getCutBackTo()
    {
      return cutBackTo;
    }

    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame topFrame = frame;
      while(frame.getAction() != cutBackTo) {
        frame = interpreter.popFrame();
      }

      interpreter.pushFrame(frame.changeResult(topFrame.getResult()));
    }
  }
}
