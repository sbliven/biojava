package org.biojava.ontology.expression;

/**
 *
 *
 * @author Matthew Pocock
 */
public class ImportedAtomImpl
        extends AtomImpl
        implements ImportedAtom
{
  private final String namespaceName;

  public ImportedAtomImpl(String name, String namespaceName)
  {
    super(name, null);
    this.namespaceName = namespaceName;
  }

  public String getNamespaceName()
  {
    return namespaceName;
  }

  public void host(Visitor visitor)
  {
    visitor.visitImportedAtom(this);
  }
}
