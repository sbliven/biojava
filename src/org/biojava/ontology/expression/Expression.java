package org.biojava.ontology.expression;

import java.util.Set;

/**
 * A top-level Triple. Expressions never contain expressions in their subject
 * or objects (directly or indirectly).
 *
 * @author Matthew Pocock
 */
public interface Expression
        extends Triple
{
  public Expression getSourceExpression();
  public BindingList getBinding();
  public Set getVariables();
  public String getName();
  public String getDescription();
}
