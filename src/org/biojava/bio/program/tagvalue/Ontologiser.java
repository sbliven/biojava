package org.biojava.bio.program.tagvalue;

import org.biojava.ontology.Ontology;
import org.biojava.ontology.Term;
import org.biojava.ontology.ReasoningDomain;

import java.util.Set;
import java.util.Iterator;

/**
 * Replace properties with ontology terms.
 *
 * <p>
 * If a tag name matches the name of an ontolgy term in a reasoning domain, then
 * the term is substituted for the tag. Otherwise, the tag is passed on as-is.
 * If there are multiple terms with the same name, then currently an
 * arbitrary choice is made.
 * </p>
 *
 * @author Matthew Pocock
 * @since 1.4
 */
public class Ontologiser implements PropertyChanger {
  private final ReasoningDomain rDom;

  public Ontologiser(ReasoningDomain rDom) {
    this.rDom = rDom;
  }

  public ReasoningDomain getRDom() {
    return rDom;
  }

  public Object getNewTag(Object oldTag) {
    Set terms = rDom.getTerms(oldTag.toString());
    Iterator ti = terms.iterator();

    if(ti.hasNext()) {
      return ti.next();
    } else {
      return oldTag;
    }
  }
}
