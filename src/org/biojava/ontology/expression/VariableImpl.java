package org.biojava.ontology.expression;

/**
 * An implementation of <code>Variable</code>.
 *
 * @author Matthew Pocock
 */
public class VariableImpl
        extends TerminalImpl
        implements Variable
{
  private Expression accept;

  /**
   * Constructor that sets the name and with an accept expression that will
   * bind to all atoms.
   *
   * @param name  the variable's name
   */
  public VariableImpl(String name)
  {
    super(name);
  }

  /**
   * Constructor that sets the name and with the specified accept expression.
   *
   * @for.powerUser  You will have to be careful that <code>accept</code> maps
   *    <i>this</i> variable instance to values. It's safer to use the 3-arg
   *    constructor.
   * @param name    name of this variable
   * @param accept  an <code>Expression</code> that accepts all values for this
   *    variable
   */
  protected VariableImpl(String name, Expression accept)
  {
    super(name);

    this.accept = accept;
  }

  /**
   * Constructor that sets the name and also binds the accept expression to
   * this variable by replacing dummy with this variable.
   *
   * @param name    name of this variable
   * @param accept  an <code>Expression</code> that accepts all values for this
   *    variable
   * @param dummy   the <code>Variable</code> in <code>accept</code> to
   *    substitute with this object
   */
  public VariableImpl(String name, Expression accept, Variable dummy)
  {
    super(name);
    this.accept = ExpressionTools.bind(
            accept,
            BindingTools.makeBinding(dummy, this));
  }

  public void host(Visitor visitor)
  {
    visitor.visitVariable(this);
  }

  public Expression getAcceptance()
  {
    if(accept == null) {
      accept = ExpressionTools.bind(
              ReasoningTools.ANYTHING,
              BindingTools.makeBinding(ReasoningTools.ANYTHING_VAR, this));

    }

    return accept;
  }

  public String toString()
  {
    return "VariableImpl: " + getName();
  }
}
