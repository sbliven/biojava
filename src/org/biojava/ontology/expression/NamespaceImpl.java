package org.biojava.ontology.expression;

import java.util.*;

/**
 *
 *
 * @author Matthew Pocock
 */
public class NamespaceImpl
        implements Namespace
{
  private final String name;
  private final String description;
  private final Set atoms;
  private final Set expressions;
  private final Map atomByName;
  private final Map expressionByName;

  public NamespaceImpl(String name, String description)
  {
    this.name = name;
    this.description = description;
    atoms = new HashSet();
    expressions = new HashSet();
    atomByName = new HashMap();
    expressionByName = new HashMap();
  }

  public String getName()
  {
    return name;
  }

  public String getDescription()
  {
    return description;
  }

  public void addAtom(Atom atom)
  {
    this.atoms.add(atom);
    this.atomByName.put(atom.getName(), atom);
  }

  public void removeAtom(Atom atom)
  {
    this.atoms.remove(atom);
    this.atomByName.remove(atom.getName());
  }

  public Set getAtoms()
  {
    return Collections.unmodifiableSet(atoms);
  }

  public Atom getAtomByName(String name) {
    return (Atom) atomByName.get(name);
  }

  public void addExpression(Expression expr)
  {
    this.expressions.add(expr);
    this.expressionByName.put(expr.getName(), expr);
  }

  public void removeExpression(Expression expr)
  {
    this.expressions.remove(expr);
    this.expressionByName.remove(expr.getName());
  }

  public Set getExpressions() {
    return Collections.unmodifiableSet(expressions);
  }

  public Expression getExpressionByName(String name)
  {
    return (Expression) expressionByName.get(name);
  }

  public Atom createAtom(String name, String description)
  {
    return new AtomImpl(name, description);
  }

  public ImportedAtom createImportedAtom(String name, String namespaceName)
  {
    return new ImportedAtomImpl(name, namespaceName);
  }

  public Variable createVariable(String name)
  {
    return new VariableImpl(name);
  }

  public Expression createExpression(ExpressionPart subject,
                                     ExpressionPart object,
                                     Terminal predicate,
                                     String name,
                                     String description)
  {
    return new ExpressionImpl(subject, object, predicate, name, description);
  }

  public SubExpression createSubExpression(ExpressionPart subject,
                                           ExpressionPart object,
                                           Terminal predicate)
  {
    return new SubExpressionImpl(subject, object, predicate);
  }
}
