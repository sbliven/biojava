package org.biojava.ontology.vm;

import java.util.*;

/**
 *
 *
 * @author Matthew Pocock
 */
public interface Interpreter {
  public Frame getFrame();
  public Frame popFrame();
  public void pushFrame(Frame frame);
  public boolean canAdvance();
  public void advance();

  public static class Impl
          implements Interpreter {
    protected final Stack stack;

    {
      stack = new Stack();
    }

    public Frame getFrame() {
      return (Frame) stack.peek();
    }

    public Frame popFrame() {
      return (Frame) stack.pop();
    }

    public void pushFrame(Frame frame) {
      stack.push(frame);
    }

    public boolean canAdvance() {
      return !stack.isEmpty();
    }

    public void advance() {
      Frame frame = getFrame();
      frame.getAction().evaluate(this);
    }
  }

  public static class Debug
          extends Impl {
    private int maxDepth;
    private Set debugOn;
    private LinkedList lastStacks;
    private int stacksToKeep;
    private int maxTries;
    private int currentTry;

    public Debug() {
      maxDepth = Integer.MAX_VALUE;
      lastStacks = new LinkedList();
      stacksToKeep = 0;
      maxTries = Integer.MAX_VALUE;
      currentTry = 0;
    }

    public void setMaxDepth(int maxDepth) {
      this.maxDepth = maxDepth;
    }

    public void setDebugOn(Set debugOn) {
      this.debugOn = debugOn;
    }

    public void setStacksToKeep(int stacksToKeep) {
      this.stacksToKeep = stacksToKeep;
    }

    public void setMaxTries(int maxTries) {
      this.maxTries = maxTries;
    }

    public void pushFrame(Frame frame)
    {
      if(frame.getAction() == null) {
        throw new NullPointerException("Action is null: " + frame + "\nStack:\n" + stack);
      }

      super.pushFrame(frame);
    }

    public Frame popFrame()
    {
      try {
        return super.popFrame();
      } catch (EmptyStackException e) {
        throw (IllegalStateException) new IllegalStateException(
                "Stack underflowed in interpreter:" + lastStacks).initCause(e);
      }
    }

    public void advance()
    {
      if(stack.size() > maxDepth) {
        throw new StackOverflowError("Maximum stack depth exceeded: " + maxDepth + "\n" + lastStacks);
      }

      currentTry++;

      if(currentTry > maxTries) {
        throw new NoSuchElementException("Tries capped at: " + maxTries + "\n" + lastStacks);
      }

      boolean shouldTrace = false;
      Action act = null;

      if(debugOn != null) {
        Frame frame = (Frame) stack.peek();
        if(debugOn.contains(frame.getAction()) || debugOn.contains(frame.getAction().getClass())) {
          act = frame.getAction();
          System.err.println("Before execution of: " + act);
          if(stacksToKeep > 0) {
            int c = 1;
            for(Iterator i = lastStacks.iterator(); i.hasNext(); c++) {
              System.err.println("Stack: -" + c);
              System.err.println(i.next());
            }
          } else {
            System.err.println(stack);
          }
          shouldTrace = true;
        }
      }

      shouldTrace = shouldTrace || (debugOn == null);

      try {
        super.advance();
      } catch (EmptyStackException e) {
        throw (IllegalStateException) new IllegalStateException(
                "Stack underflowed in interpreter:" + lastStacks).initCause(e);
      }

      if(shouldTrace) {
        if(act != null) System.err.println("After execution of: " + act);
        System.err.println(stack);
      }

      if(stacksToKeep != 0) {
        Stack s = new Stack();
        s.addAll(stack);
        lastStacks.addFirst(s);
        if(lastStacks.size() > stacksToKeep) {
          lastStacks.removeLast();
        }
      }
    }
  }
}
