package org.biojava.ontology.expression;

/**
 * A Linked-List-like structure of variable/value bindings that functionally
 * resembles a Map. All variables are
 * <code>Variable</code>s, and all values are <code>Terminal</code>s, allowing
 * one variable to be substituted for one or more other variables or an atom.
 * <p>
 * This data-structure is explicitly designed so that you can re-use the tail
 * of a list with multiple heads. As such, all data must be immutable.
 *
 * @author Matthew Pocock
 */
public interface BindingList
{
  /**
   * Get the variable associated with this node in the list.
   *
   * @return the local Variable
   */
  public Variable getVariable();

  /**
   * Get the terminal associated with this node in the list.
   *
   * @return the local Terminal
   */
  public Terminal getValue();

  /**
   * Get the next node in the list.
   *
   * @return the next <code>BindingList</code> or null if this is the terminal
   */
  public BindingList getNext();

  /**
   * Bind a new variable/value pair.
   *
   * @param var   the <code>Variable</code> to bind
   * @param val   the <code>Terminal</code> value to bind it to
   * @return      a <code>BindingList</code> that includes this binding pair in
   *      addition to all those in this list
   */
  public BindingList bind(Variable var, Terminal val);

  public static class Impl
          implements BindingList
  {
    private final Variable var;
    private final Terminal val;
    private final BindingList next;

    Impl(Variable var, Terminal val, BindingList next)
    {
      this.var = var;
      this.val = val;
      this.next = next;
    }

    public Variable getVariable()
    {
      return var;
    }

    public Terminal getValue()
    {
      return val;
    }

    public BindingList getNext()
    {
      return next;
    }

    public BindingList bind(Variable var, Terminal val)
    {
      if(var == val) {
        throw new IllegalArgumentException("Attempted to bind a variable to itself");
      }
      
      return new Impl(var, val, this);
    }
  }
}
