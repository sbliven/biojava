package org.biojava.ontology.expression;

/**
 *
 *
 * @author Matthew Pocock
 */
public class SubExpressionImpl
        extends TripleImpl
        implements SubExpression
{
  public SubExpressionImpl(ExpressionPart subject,
                           ExpressionPart object,
                           Terminal predicate)
  {
    super(subject, object, predicate);
  }

  public void host(Visitor visitor)
  {
    visitor.visitSubExpression(this);
  }
}
