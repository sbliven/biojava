package org.biojava.ontology.expression;

import org.biojava.utils.SmallSet;

import java.util.Set;

/**
 *
 *
 * @author Matthew Pocock
 */
public class ExpressionImpl
        extends TripleImpl
        implements Expression
{
  private final String name;
  private final String description;
  private final Expression source;
  private final BindingList binding;

  public ExpressionImpl(ExpressionPart subject,
                        ExpressionPart object,
                        Terminal predicate,
                        String name,
                        String description)
  {
    this(subject, object, predicate, name, description, null, null);
  }

  public ExpressionImpl(ExpressionPart subject,
                        ExpressionPart object,
                        Terminal predicate,
                        String name,
                        String description,
                        Expression source,
                        BindingList binding)
  {
    super(subject, object, predicate);
    this.name = name;
    this.description = description;
    this.source = source;
    this.binding = binding;
  }

  public String getName()
  {
    return name;
  }

  public String getDescription()
  {
    return description;
  }

  public Set getVariables()
  {
    Set vars = new SmallSet();

    getVariables(this, vars);

    return vars;
  }

  private void getVariables(Triple trip, Set vars) {
    // get stuff
    ExpressionPart subject = trip.getSubject();
    ExpressionPart object = trip.getObject();
    Terminal predicate = trip.getPredicate();

    // check for vars
    if(subject instanceof Variable) {
      vars.add(subject);
    }

    if(object instanceof Variable) {
      vars.add(object);
    }

    if(predicate instanceof Variable) {
      vars.add(predicate);
    }

    // check child terms
    if(subject instanceof Triple) {
      getVariables((Triple) subject, vars);
    }

    if(object instanceof  Triple) {
      getVariables((Triple) object, vars);
    }

    // done
  }

  public Expression getSourceExpression()
  {
    return source;
  }

  public BindingList getBinding()
  {
    return binding;
  }

  public void host(Visitor visitor)
  {
    visitor.visitExpression(this);
  }

  public String toString()
  {
    return "ExpressionImpl: " + getName() + " - " + getDescription() + " (" +
            getSubject() + ", " + getObject() + ", " + getPredicate() + ")";
  }
}
