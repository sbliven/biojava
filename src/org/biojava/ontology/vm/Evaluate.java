package org.biojava.ontology.vm;

import org.biojava.ontology.*;

import java.util.*;

/**
 *
 *
 * @author Matthew Pocock
 */
public final class Evaluate {
  private Evaluate() {}

  public static class CheckTrueFalse
          implements Action {
    private final Action onSuccess;
    private final Action onFailure;

    private final Action SUBSTITUTE_PROP_TRUE;
    private final Action SUBSTITUTE_PROP_FALSE;
    private final Action TRUE_EVAL;
    private final Action FALSE_EVAL;
    private final Action AND_EVAL;
    private final Action CONDITIONAL;

    public CheckTrueFalse(Action onSuccess,
                          Action onFailure,
                          Action EVALUATE_FULLY)
    {
      this.onSuccess = onSuccess;
      this.onFailure = onFailure;

      SUBSTITUTE_PROP_TRUE = new StackActions.SubstituteProposition(
              OntoTools.TRUE);
      SUBSTITUTE_PROP_FALSE = new StackActions.SubstituteProposition(
              OntoTools.FALSE);

      TRUE_EVAL = new StackActions.Macro("TRUE_EVAL", Arrays.asList(new Action[]{
        LogicalActions.TRUE_VALUE,
        EVALUATE_FULLY,
        SUBSTITUTE_PROP_TRUE
      }));

      FALSE_EVAL = new StackActions.Macro("FALSE_EVAL", Arrays.asList(new Action[]{
        LogicalActions.FALSE_VALUE,
        EVALUATE_FULLY,
        SUBSTITUTE_PROP_FALSE
      }));

      AND_EVAL = new LogicalActions.And(TRUE_EVAL, FALSE_EVAL, false);
      CONDITIONAL = new LogicalActions.ConditionalAction(onSuccess, onFailure);
    }

    public void evaluate(Interpreter interpreter)
    {
      // we will do this:
      //
      // if( and( (prop -> true, evaluate, is True),
      //          (prop -> false, evaluate, isFalse) ) )
      //    onSuccess
      // else
      //    onFailure

      Frame frame = interpreter.popFrame();
      interpreter.pushFrame(frame.changeAction(CONDITIONAL));
      interpreter.pushFrame(frame.changeAction(AND_EVAL));
    }

    public String toString()
    {
      return "CHECK_TRUE_FALSE(" + onSuccess + ", " + onFailure + ")";
    }
  }

  public static class EvaluateFully implements Action {
    private final Action EF_SUB;
    private final Action EF_OBJ;
    private final Action EF_EVAL;

    public EvaluateFully(ReasoningDomain rd, Action RECURSIVE_EVAL)
    {
      EF_SUB = new StackActions.Macro("EF_SUB", Arrays.asList(new Action[]{
        StackActions.AXIOM_SUBJECT_REPLACE,
        this,
        StackActions.AXIOM_SUBJECT,
      }));

      EF_OBJ = new StackActions.Macro("EF_OBJ", Arrays.asList(new Action[]{
        StackActions.AXIOM_OBJECT_REPLACE,
        this,
        StackActions.AXIOM_OBJECT,
      }));

      // replace subject & object with their evaluated value
      //
      // if they are
      EF_EVAL = new StackActions.Macro("EF_EVAL", Arrays.asList(new Action[]{
        RECURSIVE_EVAL,
        EF_OBJ,
        EF_SUB
      }));
    }

    public void evaluate(Interpreter interpreter)
    {
      // Atom -> Atom
      //
      // Triple ->
      //  t'.subject = EF:Triple.subject
      //  t'.object = EF:Triple.object
      //  discover if (t') is supported by this ontology (recursive call)

      Frame frame = interpreter.popFrame();
      Term axiom = frame.getAxiom();

      if(axiom instanceof Triple) {
        interpreter.pushFrame(frame.changeAction(EF_EVAL));
      } else {
        Frame parent = interpreter.popFrame();
        interpreter.pushFrame(parent.changeResult(axiom));
      }
    }

    public String toString()
    {
      return "EVALUATE_FULLY";
    }
  }

  public static class RecursiveEval
          implements Action {
    private final ReasoningDomain rd;
    private static Map cache = new HashMap();

    public RecursiveEval(ReasoningDomain rd) {
      this.rd = rd;
    }

    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Triple axT = (Triple) frame.getAxiom();

      TermWrapper tw = new TermWrapper(axT);

      if(cache.containsKey(tw)) {
        //System.err.println("Seen this before");
        Term value = (Term) cache.get(tw);
        Frame parent = interpreter.popFrame();
        interpreter.pushFrame(parent.changeAxiom(
                ReasoningTools.booleanToTerm(value == OntoTools.TRUE)));
      } else {
        Action populateCache = new PopulateCache();

        interpreter.pushFrame(frame.changeAction(StackActions.RESULT_TO_AXIOM));
        interpreter.pushFrame(frame.changeAction(populateCache));
        //interpreter.pushFrame(// the sub-search)
        //fixme: stuff is broken here!!! there should be a sub-vm
        try {
          cache.put(tw, null);
          Iterator i = rd.getMatching(axT.getSubject(),
                                      axT.getObject(),
                                      axT.getPredicate());
          Frame parent = interpreter.popFrame();
          Term hasMatch = ReasoningTools.booleanToTerm(i.hasNext());
          interpreter.pushFrame(parent.changeAxiom(hasMatch));
          cache.put(tw, hasMatch);
          //System.err.println("Done recursive evaluation:\n" + frame + "\n" + parent);
        } catch (InvalidTermException e) {
          throw new Error(e);
        }
      }
    }

    private static final class PopulateCache
            implements Action {
      public void evaluate(Interpreter interpreter)
      {
        Frame frame = interpreter.popFrame();
        cache.put(new TermWrapper(frame.getAxiom()),
                  frame.getResult());
        interpreter.pushFrame(interpreter.popFrame().changeResult(frame.getResult()));
      }
    }
  }

  private static class TermWrapper {
    private final Term term;

    public TermWrapper(Term term) {
      this.term = term;
    }

    public Term getTerm() {
      return term;
    }

    public int hashCode() {
      //System.err.println("Hashcode: " + term.hashCode() + " : " + this);
      return term.hashCode();
    }

    public boolean equals(Object obj)
    {
      //System.err.println("Comparing: " + this + " to " + obj);
      if(obj instanceof TermWrapper) {
        TermWrapper tw = (TermWrapper) obj;
        return ReasoningTools.areTermsEqual(tw.term, term);
      }

      return false;
    }

    public String toString()
    {
      return term.toString();
    }
  }

  public static class EachAxiom
          implements Action {
    private final Iterator axI;
    private final Action evaluateAxion;

    public EachAxiom(Iterator axI, Action evaluateAxiom)
    {
      this.axI = axI;
      this.evaluateAxion = evaluateAxiom;
    }

    public void evaluate(Interpreter interpreter)
    {
      if(axI.hasNext()) {
        Triple axiom = (Triple) axI.next();

        // got a new axiom to process - push it on to the stack
        Frame frame = interpreter.getFrame();
        interpreter.pushFrame(frame = frame
                                      .changeAction(evaluateAxion)
                                      .changeAxiom(axiom));
      } else {
        // no more axioms - pop me
        interpreter.popFrame();
      }
    }

    public String toString()
    {
      return "EACH_AXIOM";
    }
  }

  public static class ValueIterator
          implements Action {
    private final Variable var;
    private final Iterator vals;
    private final Action expandVariables;

    public ValueIterator(Variable var, Iterator vals, Action expandVariables)
    {
      this.var = var;
      this.vals = vals;
      this.expandVariables = expandVariables;
    }

    public void evaluate(Interpreter interpreter)
    {
      while(vals.hasNext()) {
        Term val = (Term) vals.next();
        if(val instanceof Variable || val instanceof Triple || val instanceof RemoteTerm) {
          continue;
        }

        Frame frame = interpreter.getFrame();
        interpreter.pushFrame(frame.changeAction(expandVariables));
        interpreter.pushFrame(frame.changeAction(
                new StackActions.BindValue(var, val)));
        return;
      }

      interpreter.popFrame();
    }

    public String toString()
    {
      return "VALUE_ITERATOR(" + var + ")";
    }
  }

  public static class EvaluateAxiom
          implements Action {
    private final Action evaluateIfTrue;
    private final Action checkImplication;

    public EvaluateAxiom(Action evaluateIfTrue, Action checkImplication) {
      this.evaluateIfTrue = evaluateIfTrue;
      this.checkImplication = checkImplication;
    }

    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      interpreter.pushFrame(frame.changeAction(evaluateIfTrue));
      interpreter.pushFrame(frame.changeAction(checkImplication));
    }

    public String toString()
    {
      return "EVALUATE_AXIOM";
    }
  }

  public static class ExpandVariables
          implements Action {
    private final Action checkTrueFalse;
    private final Set terms;

    public ExpandVariables(Action checkTrueFalse, Set terms) {
      this.checkTrueFalse = checkTrueFalse;
      this.terms = terms;
    }

    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Term axiom = frame.getAxiom();
      Term prop = frame.getProp();

      Variable var = ReasoningTools.findFirstVariable(axiom);
      if(var == null) {
        var = ReasoningTools.findFirstVariable(prop);
      }

      if(var == null) {
        interpreter.pushFrame(frame.changeAction(checkTrueFalse));
      } else {
        //Set vals = findValues(axiom, var);
        //vals.addAll(findValues(prop, var));

        interpreter.pushFrame(frame.changeAction(
                new Evaluate.ValueIterator(var, terms.iterator(), this)));
      }
    }

    public String toString()
    {
      return "EXPAND_VARIABLES";
    }
  }

  public static class CheckImplication implements Action {
    final Action EA_Object;
    final Action EA_Subject;
    final Action EA_S_or_O;
    final Action EA_S_and_O;
    final Action IMPL;

    public CheckImplication(Action evaluateAxiom, final Action checkImplication) {
      EA_Object = new StackActions.Macro("EA_OBJECT", Arrays.asList(new Action[]{
        evaluateAxiom,
        StackActions.AXIOM_OBJECT}));
      EA_Subject = new StackActions.Macro("EA_SUBJECT", Arrays.asList(new Action[]{
        evaluateAxiom,
        StackActions.AXIOM_SUBJECT}));
      EA_S_or_O = new StackActions.Macro("EA_S_or_O", Arrays.asList(new Action[]{
        new LogicalActions.Or(EA_Subject, EA_Object, false)}));
      EA_S_and_O = new StackActions.Macro("EA_S_and_O", Arrays.asList(new Action[]{
        new LogicalActions.And(EA_Subject, EA_Object, false)}));
      IMPL = new StackActions.Macro("IMPL", Arrays.asList(new Action[]{
        new LogicalActions.LazyRef() {
          protected Action getDelegate()
          {
            return checkImplication;
          }
        },
        StackActions.AXIOM_OBJECT}));
    }

    public void evaluate(Interpreter interpreter)
    {
      //
      // check for
      // - axiom = prop
      // - axiom -> prop
      // - axiom = x & y, x -> prop or y -> prop
      // - axiom = x || y, x -> prop and y -> prop
      //
      // push in reverse order so they get popped in the correct one

      Frame frame = interpreter.popFrame();
      Term axiom = ReasoningTools.resolveRemote(frame.getAxiom());

      if(!(axiom instanceof Triple)) {
        // if we've walked to a non-triple, things are bad
        throw new IllegalStateException("Term must be a triple: " + axiom);
      }

      Triple trip = (Triple) axiom;
      Term predicate = ReasoningTools.resolveRemote(trip.getPredicate());
      Action toPush = null;

      // (x & y) -> prop, x -> prop or y -> prop
      if(predicate == OntoTools.AND) {
        Term tripO = ReasoningTools.resolveRemote(trip.getObject());
        Term tripS = ReasoningTools.resolveRemote(trip.getSubject());

        boolean tto = tripO instanceof Triple;
        boolean tts = tripS instanceof Triple;

        if(tto && tts) {
          toPush = EA_S_or_O;
        }

        if(tripO instanceof Triple) {
          toPush = EA_Object;
        }

        if(tripS instanceof Triple) {
          toPush = EA_Subject;
        }
      }

      // (x || y) -> prop, x -> prop and y -> prop
      if(predicate == OntoTools.OR) {
        Term tripO = ReasoningTools.resolveRemote(trip.getObject());
        Term tripS = ReasoningTools.resolveRemote(trip.getSubject());

        boolean tto = tripO instanceof Triple;
        boolean tts = tripS instanceof Triple;

        if(tto && tts) {
          toPush = EA_S_and_O;
        }

        if(tripO instanceof Triple) {
          toPush = EA_Object;
        }

        if(tripS instanceof Triple) {
          toPush = EA_Subject;
        }
      }

      // axiom -> prop
      if(predicate == OntoTools.IMPLIES) {
        toPush = IMPL;
      }

      // axiom = prop
      if(toPush == null) {
        interpreter.pushFrame(frame.changeAction(EqualActions.EQUIVALENT));
      } else {
        interpreter.pushFrame(frame.changeAction(
                new LogicalActions.Or(EqualActions.EQUIVALENT,
                                      toPush,
                                      false)));
      }
    }

    public String toString()
    {
      return "CHECK_IMPLICATION";
    }
  }
 }
