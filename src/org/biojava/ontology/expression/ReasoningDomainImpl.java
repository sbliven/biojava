package org.biojava.ontology.expression;

import java.util.*;

/**
 *
 *
 * @author Matthew Pocock
 */
public class ReasoningDomainImpl
        implements ReasoningDomain
{
  private Set namespaces;
  private Map namespaceByName;

  public ReasoningDomainImpl() {
    this.namespaces = new HashSet();
    namespaceByName = new HashMap();
  }

  public Set getNamespaces()
  {
    return Collections.unmodifiableSet(namespaces);
  }

  public void addNamespace(Namespace namespace)
  {
    namespaces.add(namespace);
    namespaceByName.put(namespace.getName(), namespace);
  }

  public void removeNamespace(Namespace namespace)
  {
    namespaces.remove(namespace);
    namespaceByName.remove(namespace.getName());
  }

  public Namespace getNamespaceByName(String name)
  {
    return (Namespace) namespaceByName.get(name);
  }

  public boolean isConsistent()
  {
    // take each expression
    // compare it against all previosly checked ones
    // barf if it is inconsistent with those
    // if all pass, we are consisitent.



    return true;
  }

  public boolean isConsistent(Expression expr)
  {
    return false;
  }

  public boolean isImplied(Expression expr)
  {
    return false;
  }

  public Iterator findProofs(Expression expr)
  {
    return null;
  }
}
