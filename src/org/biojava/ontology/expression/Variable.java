package org.biojava.ontology.expression;

/**
 * A terminal symbol that can be substituted for a range of Atoms.
 *
 * @author Matthew Pocock
 */
public interface Variable extends Terminal
{
  Expression getAcceptance();
}
