package org.biojava.ontology.expression;

import java.util.Set;

/**
 * A namespace within which you find Atoms and Expressions over these Atoms.
 *
 * @author Matthew Pocock
 */
public interface Namespace {
  public String getName();
  public String getDescription();

  public void addAtom(Atom atom);
  public void removeAtom(Atom atom);


  /**
   * Set of all atoms in the namespace.
   *
   * @return
   */
  public Set getAtoms();

  public Atom getAtomByName(String name);

  public void addExpression(Expression expr);

  public void removeExpression(Expression expr);

  /**
   * Set of all expressions in the namespace.
   *
   * @return
   */
  public Set getExpressions();

  public Expression getExpressionByName(String name);

  public Atom createAtom(String name, String description);

  public ImportedAtom createImportedAtom(String name, String namespaceName);

  public Variable createVariable(String name);

  public Expression createExpression(ExpressionPart subject,
                                     ExpressionPart object,
                                     Terminal predicate,
                                     String name,
                                     String description);

  public SubExpression createSubExpression(ExpressionPart subject,
                                           ExpressionPart object,
                                           Terminal predicate);
}
