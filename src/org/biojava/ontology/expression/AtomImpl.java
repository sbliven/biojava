package org.biojava.ontology.expression;

/**
 *
 *
 * @author Matthew Pocock
 */
public class AtomImpl
        extends TerminalImpl
        implements Atom
{
  private final String description;

  public AtomImpl(String name, String description)
  {
    super(name);
    this.description = description;
  }

  public String getDescription()
  {
    return description;
  }

  public void host(Visitor visitor)
  {
    visitor.visitAtom(this);
  }

  public String toString()
  {
    return "AtomImpl: " + getName() + " - " + getDescription();
  }

  public boolean equals(Object obj)
  {
    if(obj instanceof Atom) {
      Atom that = (Atom) obj;
      return this.getName() == that.getName();
    }

    return false;
  }

  public int hashCode()
  {
    return getName().hashCode();
  }
}
