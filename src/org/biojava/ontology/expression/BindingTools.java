package org.biojava.ontology.expression;

/**
 *
 *
 * @author Matthew Pocock
 */
public class BindingTools {
  public static final BindingList TERMINAL_BINDING_LIST;

  static
  {
    TERMINAL_BINDING_LIST = new BindingList.Impl(null, null, null);
  }

  /**
   * Get the binding for a variable.
   * This will handle the case where a variable is bound to another variable,
   * resolving the chain if possible to an atom.
   *
   * @param var     the variable to bind
   * @param binding the <code>BindingList</code> giving the complete mapping
   *    from variables to values
   * @return        the most specific <code>Terminal</code> this is bound to or
   *    null if there is no binding
   */
  public static Terminal getBinding(Variable var, BindingList binding)
  {
    if(binding == null) {
      return null;
    }

    // loop over linked list elements
    Terminal res = null;
    BindingList curr = binding;
    while(curr != null) {
      // current variable matches the one we're checking
      if(curr.getVariable() == var) {
        res = curr.getValue();
        // if we've bound to a variable, then start the search again
        if(res instanceof Variable) {
          var = (Variable) res;
          curr = binding;
        }
        // bound to an atom - we can return now
        else {
          curr = null;
        }
      } else {
        // not found a match - try the next element
        curr = curr.getNext();
      }
    }

    return res;
  }

  public static BindingList makeBinding(Variable var, Terminal val)
  {
    return new BindingList.Impl(var, val, null);
  }
}
