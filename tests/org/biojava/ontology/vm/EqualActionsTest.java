package org.biojava.ontology.vm;

import junit.framework.TestCase;
import org.biojava.ontology.ReasoningDomain;
import org.biojava.ontology.Term;
import org.biojava.ontology.OntoTools;

/**
 *
 *
 * @author Matthew Pocock
 */
public class EqualActionsTest extends TestCase {
  public void testAtomicEquals() {
    ReasoningDomain rd = new ReasoningDomain.Impl();
    Interpreter interp = new Interpreter.Debug();
    TestAction start;
    Frame frame;

    start = new TestAction();
    frame = new Frame(rd, start, OntoTools.TYPE, OntoTools.TYPE);
    interp.pushFrame(frame);
    interp.pushFrame(frame.changeAction(EqualActions.EQUIVALENT));
    while(interp.canAdvance()) {
      interp.advance();
    }
    assertEquals("Identical atoms", OntoTools.TRUE, start.getResult());

    start = new TestAction();
    frame = new Frame(rd, start, rd.createVariable("_x"), OntoTools.TYPE);
    interp.pushFrame(frame);
    interp.pushFrame(frame.changeAction(EqualActions.EQUIVALENT));
    while(interp.canAdvance()) {
      interp.advance();
    }
    assertEquals("Axiom variable", OntoTools.TRUE, start.getResult());

    start = new TestAction();
    frame = new Frame(rd, start, OntoTools.TYPE, rd.createVariable("_y"));
    interp.pushFrame(frame);
    interp.pushFrame(frame.changeAction(EqualActions.EQUIVALENT));
    while(interp.canAdvance()) {
      interp.advance();
    }
    assertEquals("Predicate variable", OntoTools.TRUE, start.getResult());

    start = new TestAction();
    frame = new Frame(rd, start, rd.createVariable("_x"), rd.createVariable("_y"));
    interp.pushFrame(frame);
    interp.pushFrame(frame.changeAction(EqualActions.EQUIVALENT));
    while(interp.canAdvance()) {
      interp.advance();
    }
    assertEquals("Both variable", OntoTools.TRUE, start.getResult());

    start = new TestAction();
    frame = new Frame(rd, start, OntoTools.SUB_TYPE_OF, OntoTools.XOR);
    interp.pushFrame(frame);
    interp.pushFrame(frame.changeAction(EqualActions.EQUIVALENT));
    while(interp.canAdvance()) {
      interp.advance();
    }
    assertEquals("Different atoms", OntoTools.FALSE, start.getResult());
  }


  public void testTripleEquals()
  {
    ReasoningDomain rd = new ReasoningDomain.Impl();
    Interpreter.Debug interp = new Interpreter.Debug();
    interp.setMaxDepth(20);
    TestAction start;
    Frame frame;

    start = new TestAction();
    frame = new Frame(rd, start,
                      rd.createVirtualTerm(OntoTools.SYMMETRIC, OntoTools.REFLEXIVE, OntoTools.OR, null, null),
                      rd.createVirtualTerm(OntoTools.SYMMETRIC, OntoTools.REFLEXIVE, OntoTools.OR, null, null));
    interp.pushFrame(frame);
    interp.pushFrame(frame.changeAction(EqualActions.EQUIVALENT));
    while(interp.canAdvance()) {
      interp.advance();
    }
    assertEquals("Identical triples", OntoTools.TRUE, start.getResult());

    start = new TestAction();
    frame = new Frame(rd, start,
                      rd.createVirtualTerm(OntoTools.SYMMETRIC, OntoTools.REFLEXIVE, OntoTools.OR, null, null),
                      rd.createVirtualTerm(OntoTools.SYMMETRIC, OntoTools.REFLEXIVE, OntoTools.AND, null, null));
    interp.pushFrame(frame);
    interp.pushFrame(frame.changeAction(EqualActions.EQUIVALENT));
    while(interp.canAdvance()) {
      interp.advance();
    }
    assertEquals("Different predicate", OntoTools.FALSE, start.getResult());

    start = new TestAction();
    frame = new Frame(rd, start,
                      rd.createVirtualTerm(OntoTools.SYMMETRIC, OntoTools.REFLEXIVE, OntoTools.OR, null, null),
                      rd.createVirtualTerm(OntoTools.SUB_TYPE_OF, OntoTools.REFLEXIVE, OntoTools.OR, null, null));
    interp.pushFrame(frame);
    interp.pushFrame(frame.changeAction(EqualActions.EQUIVALENT));
    while(interp.canAdvance()) {
      interp.advance();
    }
    assertEquals("Identical subject", OntoTools.FALSE, start.getResult());

    start = new TestAction();
    frame = new Frame(rd, start,
                      rd.createVirtualTerm(OntoTools.SYMMETRIC, OntoTools.REFLEXIVE, OntoTools.OR, null, null),
                      rd.createVirtualTerm(OntoTools.SYMMETRIC, OntoTools.TYPE, OntoTools.OR, null, null));
    interp.pushFrame(frame);
    interp.pushFrame(frame.changeAction(EqualActions.EQUIVALENT));
    while(interp.canAdvance()) {
      interp.advance();
    }
    assertEquals("Identical object", OntoTools.FALSE, start.getResult());


    start = new TestAction();
    frame = new Frame(rd, start,
                      rd.createVirtualTerm(OntoTools.SYMMETRIC, OntoTools.REFLEXIVE, OntoTools.OR, null, null),
                      rd.createVirtualTerm(OntoTools.SYMMETRIC, rd.createVariable("_obj"), OntoTools.OR, null, null));
    interp.pushFrame(frame);
    interp.pushFrame(frame.changeAction(EqualActions.EQUIVALENT));
    while(interp.canAdvance()) {
      interp.advance();
    }
    assertEquals("Variable object", OntoTools.TRUE, start.getResult());

  }

  class TestAction implements Action {
    private Term result;

    public void evaluate(Interpreter interpreter)
    {
      result = interpreter.popFrame().getResult();
    }

    public Term getResult() {
      return result;
    }

    public String toString()
    {
      return "EQUAL_ACTIONS_TEST";
    }
  }
}
