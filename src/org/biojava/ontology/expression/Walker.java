package org.biojava.ontology.expression;

/**
 *
 *
 * @author Matthew Pocock
 */
public interface Walker
{
  public void walk(Expression expression, Visitor visitor);

  public static class DepthFirst
          implements Walker
  {
    public void walk(Expression expression, final Visitor visitor)
    {
      Visitor dfv = new Visitor.Base() {
        public void visitTriple(Triple val)
        {
          recurse(val.getSubject());
          recurse(val.getObject());
          recurse(val.getPredicate());
        }

        public void visitTerm(Term val)
        {
          val.host(visitor);
        }

        private void recurse(Term term) {
          term.host(this);
          term.host(visitor);
        }
      };

      expression.host(dfv);
    }
  }
}
