package org.biojava.ontology.expression;

/**
 * Common utility methods for working with expressions.
 *
 * @author Matthew Pocock
 */
public class ExpressionTools
{
  private ExpressionTools() {}

  /**
   * Unify two triples. This will attempt to return a <code>BindingList</code>
   * that when applied to both tripples would make them equal.
   *
   * @param lhs   the left <code>Triple</code>
   * @param rhs   the right <code>Triple</code>
   * @return      the <code>BindingList</code> that would make them equal, or
   *    null if no such <code>BindingList</code> exists, the empty binding list
   *    if they are already equal
   */
  public static BindingList unify(Triple lhs, Triple rhs)
  {
    return unify(lhs, rhs, BindingTools.TERMINAL_BINDING_LIST);
  }

  private static BindingList unify(Triple lhs, Triple rhs, BindingList binding)
  {
    // predicates must match
    if(lhs.getPredicate() != rhs.getPredicate()) {
      return null;
    }

    binding = process(lhs.getSubject(), rhs.getSubject(), binding);
    if(binding == null) {
      return null;
    }

    binding = process(lhs.getObject(), rhs.getObject(), binding);
    if(binding == null) {
      return null;
    }

    return binding;
  }

  private static BindingList process(Term left, Term right, BindingList binding)
  {
    boolean lt = left instanceof Triple;
    boolean rt = right instanceof Triple;

    // must either both be triples, or neither be triples
    if(lt != rt) {
      return null;
    }

    // both triples
    if(lt) {
      return unify((Triple) left, (Triple) right, binding);
    }

    // both terminals
    Terminal tl;
    Terminal tr;

    // resolve tl to its current bindings
    tl = (Terminal) left;
    if(left instanceof Variable) {
      Terminal val = BindingTools.getBinding((Variable) left, binding);
      if(val != null) {
        tl = val;
      }
    }

    // resolve tr to its current bindings
    tr = (Terminal) right;
    if(right instanceof Variable) {
      Terminal val = BindingTools.getBinding((Variable) right, binding);
      if(val != null) {
        tr = val;
      }
    }

    // neither triples
    boolean la = tl instanceof Atom;
    boolean ra = tr instanceof Atom;

    // both atoms
    if(la && ra) {
      // same atom, do nothing
      if(left.equals(right)) {
        return binding;
      }
      // different atom, inconsistent, fail, return null
      else {
        return null;
      }
    }

    // both variables - replace with new var that is intersection
    if(!la && !ra) {
      Variable lVar = (Variable) tl;
      Variable rVar = (Variable) tr;
      Expression lAcc = lVar.getAcceptance();
      Expression rAcc = rVar.getAcceptance();

      Variable dummy = new VariableImpl("dummy");
      BindingList varB = BindingTools.TERMINAL_BINDING_LIST
              .bind(lVar, dummy)
              .bind(rVar, dummy);

      Expression acceptRaw = new ExpressionImpl(
              new SubExpressionImpl(
                      lAcc.getSubject(), lAcc.getObject(), lAcc.getPredicate()),
              new SubExpressionImpl(
                      rAcc.getSubject(), rAcc.getObject(), rAcc.getPredicate()),
              ReasoningTools.AND,
              null,
              "Intersection of " + lAcc.getName() + " and " + rAcc.getName());
      acceptRaw = bind(acceptRaw, varB);

      Variable newVar = new VariableImpl(
              "(" + tl.getName() + "-i-" + tr.getName() + ")",
              acceptRaw, dummy);

      return binding.bind(lVar, newVar).bind(rVar, newVar);
    }

    // left is variable, right is value - bind left to right
    if(ra) {
      return binding.bind((Variable) tl, tr);
    }

    // right is variable, left is value, bind right to left
    if(la) {
      return binding.bind((Variable) tr, tl);
    }

    // all bases should be covered by now - if we get here, my logic is flawed
    throw new AssertionError("We should never reach this point");
  }

  /**
   * Bind the variables in an expression to values from a
   * <code>BindingList</code>. All variables in <code>source</code> for which
   * there is a value in <code>binding</code> will be substituted. The resulting
   * <code>Expression</code> will have <code>getSourceExpression()</code> set
   * to <code>source</code>, and <code>getBinding()</code> set to
   * <code>binding</code>. Any variables in <code>source</code> with no
   * bindings will be left as-is. Any variables in <code>binding</code> that
   * are not in <code>source</code> will be silently ignored.
   *
   * @param source    the original <code>Expression</code>
   * @param binding   a <code>BindingList</code> containing the bindings to use
   * @return  the result of applying all the bindings to the source
   */
  public static Expression bind(Expression source, BindingList binding)
  {
    ExpressionPart sub = bind(source.getSubject(), binding);
    ExpressionPart obj = bind(source.getObject(), binding);
    Terminal prd = (Terminal) bind(source.getPredicate(), binding);

    return new ExpressionImpl(sub, obj, prd,
                              "", "",
                              source, binding);
  }

  private static ExpressionPart bind(ExpressionPart part, BindingList bl)
  {
    // recursive substitution
    if(part instanceof Triple) {
      Triple trip = (Triple) part;

      ExpressionPart sub = bind(trip.getSubject(), bl);
      ExpressionPart obj = bind(trip.getObject(), bl);
      Terminal prd = (Terminal) bind(trip.getPredicate(), bl);

      if(sub != trip.getSubject() ||
              obj != trip.getObject() ||
              prd != trip.getPredicate())
      {
        return new SubExpressionImpl(sub, obj, prd);
      }
    }

    // variable substitution
    if(part instanceof Variable) {
      Variable var = (Variable) part;
      Terminal val = BindingTools.getBinding(var, bl);
      if(val != null) {
        return val;
      } else {
        return var;
      }
    }

    // nothing to change
    return part;
  }
}
