package org.biojava.ontology.expression;

/**
 *
 *
 * @author Matthew Pocock
 */
public abstract class TripleImpl implements Triple {
  private final ExpressionPart subject;
  private final ExpressionPart object;
  private final Terminal predicate;

  public TripleImpl(ExpressionPart subject,
                    ExpressionPart object,
                    Terminal predicate)
  {
    if(subject == null || object == null || predicate == null) {
      throw new NullPointerException(
              "Subject, object or predicate was null: subject:" +
              subject + " object:" + object + " predicate: " + predicate);
    }
    
    this.subject = subject;
    this.object = object;
    this.predicate = predicate;
  }

  public ExpressionPart getSubject()
  {
    return subject;
  }

  public ExpressionPart getObject()
  {
    return object;
  }

  public Terminal getPredicate()
  {
    return predicate;
  }

  public String toString()
  {
    return "(" + getSubject() + ", " + getObject() + ", " + getPredicate() + ")";
  }
}
