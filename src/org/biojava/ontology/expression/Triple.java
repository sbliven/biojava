package org.biojava.ontology.expression;

/**
 * A term that represents a subject, object, predicate triple. These can be
 * built up into complex expressions.
 *
 * Subject and Object can never be instances of Expression.
 *
 * @author Matthew Pocock
 */
public interface Triple
        extends Term
{
  public ExpressionPart getSubject();
  public ExpressionPart getObject();
  public Terminal getPredicate();
}
