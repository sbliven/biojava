package org.biojava.ontology.expression;

/**
 *
 *
 * @author Matthew Pocock
 */
public abstract class TerminalImpl implements Terminal {
  private final String name;

  public TerminalImpl(String name)
  {
    this.name = name;
  }

  public final String getName()
  {
    return name;
  }
}
