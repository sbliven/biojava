package org.biojava.ontology.expression;

/**
 * A term that can not be decomposed into other terms.
 *
 * @author Matthew Pocock
 */
public interface Terminal
        extends Term, ExpressionPart
{
  public String getName();
}
