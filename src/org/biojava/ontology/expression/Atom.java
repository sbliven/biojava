package org.biojava.ontology.expression;

/**
 * A terminal symbol that is an Atom. Atoms are the tokens that we reason over.
 * Every 'thing' is an atom.
 *
 * @author Matthew Pocock
 */
public interface Atom
        extends Terminal
{
  public String getDescription();
}
