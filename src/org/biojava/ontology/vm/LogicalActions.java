package org.biojava.ontology.vm;

import org.biojava.ontology.ReasoningTools;
import org.biojava.ontology.Term;
import org.biojava.ontology.OntoTools;

/**
 *
 *
 * @author Matthew Pocock
 */
public final class LogicalActions {
  public static final Action NULL_OP;
  public static final Action RETURN_RESULT;
  public static final Action TRUE_VALUE;
  public static final Action FALSE_VALUE;

  private static final Action AND_RESULT_TRUE;
  private static final Action AND_RESULT_FALSE;
  private static final Action OR_RESULT_TRUE;
  private static final Action OR_RESULT_FALSE;

  static
  {
    NULL_OP = new NullOp();
    RETURN_RESULT = new ReturnResult();
    TRUE_VALUE = new EqualsValue(OntoTools.TRUE);
    FALSE_VALUE = new EqualsValue(OntoTools.FALSE);

    AND_RESULT_TRUE = new AndResult(true);
    AND_RESULT_FALSE = new AndResult(false);
    OR_RESULT_TRUE = new OrResult(true);
    OR_RESULT_FALSE = new OrResult(false);
  }

  private static final class NullOp
          implements Action {
    public void evaluate(Interpreter interpreter)
    {
      interpreter.popFrame();
    }

    public String toString()
    {
      return "NULL_OP";
    }
  }

  private static final class ReturnResult
          implements Action {
    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame parent = interpreter.popFrame();

      interpreter.pushFrame(parent
                            .changeResult(frame.getResult())
                            .changeSymbolTable(frame.getSymbolTable()));
    }

    public String toString()
    {
      return "RETURN_RESULT";
    }
  }

  public static abstract class LazyRef implements Action {
    protected abstract Action getDelegate();

    public void evaluate(Interpreter interpreter)
    {
      getDelegate().evaluate(interpreter);
    }

    public String toString()
    {
      return getDelegate().toString();
    }
  }

  public static final class ConditionalAction implements Action {
    private final Action onTrue;
    private final Action onFalse;

    public ConditionalAction(Action onTrue, Action onFalse)
    {
      this.onTrue = onTrue;
      this.onFalse = onFalse;
    }

    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Action action = (ReasoningTools.isTrue(frame.getResult())) ? onTrue : onFalse;
      interpreter.pushFrame(frame.changeAction(action));
    }

    public String toString()
    {
      return "CONDITIONAL_ACTION(" + onTrue + ", " + onFalse + ")";
    }
  }

  public static final class And implements Action {
    private final Action first;
    private final Action second;

    public And(Action first, Action second, boolean lazy)
    {
      this.first = first;
      this.second = new AndFirst(second, lazy);
    }

    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      interpreter.pushFrame(frame.changeAction(second));
      interpreter.pushFrame(frame.changeAction(first));
    }

    public String toString() {
      return "AND(" + first + ", " + second + ")";
    }
  }

  private static final class AndFirst
          implements Action {
    private final Action act;
    private final boolean lazy;

    public AndFirst(Action act, boolean lazy)
    {
      this.act = act;
      this.lazy = lazy;
    }

    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      boolean res = ReasoningTools.isTrue(frame.getResult());

      // skip un-needed compute
      if(lazy == true && res == false) {
        interpreter.pushFrame(frame.changeAction(RETURN_RESULT));
      } else {
        if(res == true) {
          interpreter.pushFrame(frame.changeAction(AND_RESULT_TRUE));
        } else {
          interpreter.pushFrame(frame.changeAction(AND_RESULT_FALSE));
        }
        interpreter.pushFrame(frame.changeAction(act));
      }
    }

    public String toString()
    {
      return "AND_FIRST(" + act + ", " + lazy + ")";
    }
  }

  private static final class AndResult
          implements Action {
    private final boolean state;

    AndResult(boolean state) {
      this.state = state;
    }

    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame parent = interpreter.popFrame();

      boolean res = ReasoningTools.isTrue(frame.getResult());

      interpreter.pushFrame(parent
                            .changeResult(
                                    ReasoningTools.booleanToTerm(res && state))
                            .changeSymbolTable(frame.getSymbolTable()));
    }

    public String toString() {
      return "AND_RESULT(" + state + ")";
    }
  }

  public static final class Or
          implements Action {
    private final Action first;
    private final Action second;

    public Or(Action first, Action second, boolean lazy)
    {
      this.first = first;
      this.second = new OrFirst(second, lazy);
    }

    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      interpreter.pushFrame(frame.changeAction(second));
      interpreter.pushFrame(frame.changeAction(first));
    }

    public String toString()
    {
      return "OR(" + first + ", " + second;
    }
  }

  private static final class OrFirst
          implements Action {
    private final Action act;
    private final boolean lazy;

    public OrFirst(Action act, boolean lazy)
    {
      this.act = act;
      this.lazy = lazy;
    }

    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      boolean res = ReasoningTools.isTrue(frame.getResult());

      // skip un-needed compute
      if(lazy == true && res == true) {
        interpreter.pushFrame(frame.changeAction(RETURN_RESULT));
      } else {
        if(res == true) {
          interpreter.pushFrame(frame.changeAction(OR_RESULT_TRUE));
        } else {
          interpreter.pushFrame(frame.changeAction(OR_RESULT_FALSE));
        }
        interpreter.pushFrame(frame.changeAction(act));
      }
    }

    public String toString()
    {
      return "OR_FIRST(" + act + ", " + lazy + ")";
    }
  }

  private static final class OrResult
          implements Action {
    private final boolean state;

    OrResult(boolean state)
    {
      this.state = state;
    }

    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame parent = interpreter.popFrame();

      boolean res = ReasoningTools.isTrue(frame.getResult());

      interpreter.pushFrame(parent
                            .changeResult(
                                    ReasoningTools.booleanToTerm(res || state))
                            .changeSymbolTable(frame.getSymbolTable()));
    }

    public String toString() {
      return "OR_RESULT(" + state + ")";
    }
  }

  public static final class EqualsValue
          implements Action {
    private final Term val;

    public EqualsValue(Term val) {
      this.val = val;
    }

    public void evaluate(Interpreter interpreter)
    {
      Frame frame = interpreter.popFrame();
      Frame parent = interpreter.popFrame();

      interpreter.pushFrame(parent.changeResult(
              ReasoningTools.booleanToTerm((frame.getResult() == val))));
    }

    public String toString()
    {
      return "EQUALS_VALUE(" + val + ")";
    }
  }
}
