package org.biojava.ontology.expression;

/**
 *
 *
 * @author Matthew Pocock
 */
public interface Visitor
{
  public void visitImportedAtom(ImportedAtom val);
  public void visitAtom(Atom val);
  public void visitVariable(Variable val);
  public void visitTerminal(Terminal val);
  public void visitExpression(Expression val);
  public void visitSubExpression(SubExpression val);
  public void visitTriple(Triple val);
  public void visitTerm(Term val);

  public static class Base
          implements Visitor
  {
    public void visitImportedAtom(ImportedAtom val)
    {
      visitAtom(val);
    }

    public void visitAtom(Atom val)
    {
      visitTerminal(val);
    }

    public void visitVariable(Variable val)
    {
      visitTerminal(val);
    }

    public void visitTerminal(Terminal val)
    {
      visitTerm(val);
    }

    public void visitExpression(Expression val)
    {
      visitTriple(val);
    }

    public void visitSubExpression(SubExpression val)
    {
      visitTriple(val);
    }

    public void visitTriple(Triple val)
    {
      visitTerm(val);
    }

    public void visitTerm(Term val)
    {
      // do nothing
    }
  }
}
