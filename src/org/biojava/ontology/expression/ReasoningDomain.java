package org.biojava.ontology.expression;

import java.util.Set;
import java.util.Iterator;

/**
 *
 *
 * @author Matthew Pocock
 */
public interface ReasoningDomain {
  Set getNamespaces();

  Namespace getNamespaceByName(String name);

  void addNamespace(Namespace namespace);

  void removeNamespace(Namespace namespace);

  boolean isConsistent();

  boolean isConsistent(Expression expr);

  boolean isImplied(Expression expr);

  Iterator findProofs(Expression expr);
}
