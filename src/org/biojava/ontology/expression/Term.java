package org.biojava.ontology.expression;

/**
 * Every component in the part-whole hierachy used to express logic extends
 * Term.
 *
 * @author Matthew Pocock
 */
public interface Term {
  public void host(Visitor visitor);
}
