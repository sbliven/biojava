package org.biojava.bio.ontology;

import java.util.Set;

/**
 * This is a mix-in interface for optimizing ontology operators.
 *
 * <p>
 * Some ontology implementations will be able to compute some derived properties
 * very quicly because of how they store their data. This is likely to out
 * perform generic implementations of algorithms using the Ontology interface
 * to get the same result. If an Ontology implements OntologyOps, it is
 * publishing that it can provide some common operations. The ontology engine
 * may then choose to call OntologyOps methods on the Ontology instance rather
 * than using its fall-back implementations.
 * </p>
 * 
 * @author Matthew Pocock
 * @since 1.4
 */
public interface OntologyOps {
  /**
   * Calculate the transative closure on an ontology for all tripples where
   * they are castable to the terms given.
   *
   * <p>
   * Transitive closures are extremely usefull for many problems involving
   * interogation of ontologies. Given a relation, it is transitive if
   * <code>a R b and b R c implies a R c</code>. Normaly, the ontology will
   * store just the first two entries. It is the responsibility of the ontology
   * engine to prove the third one from the first two given that the relation R
   * is transitive.
   * </p>
   *
   * <p>
   * Tripples are considered as being worth returning if their subject, object
   * and relation fields all map to the corresponding parameters by an 
   * <code>is-a</code> relationship. The relation to expand is given as
   * toFollow. Again, the method should follow all relations that are instances
   * of toFollow.
   * </p>
   *
   * <p>
   * It is a restriction of this method that all relations matching
   * <code>toFollow</code> during the search of the ontology must be
   * instances of <code>transient</code>.
   * </p>
   *
   * <p>
   * One sure-fire way to compute this correctly is to build a complete list of
   * Triples that are in the transient closure of <code>toFollow</code> and then
   * to discard all entries in this set that would be rejected by the other
   * parameters.
   * </p>
   *
   * @param subject   the Term that all subjects must inherit from
   * @param object    the Term that all objects must inherit from
   * @param relation  the Term that all relations must inherit from
   * @return a Set of TripleTerm instances representing the complete transitive
   *         closure of this sub-ontology
   * @throws OntologyException if there was some error in accessing the ontology
   * @throws NullPointerException if any of the parameters are null - use
   *         the <code>any</code> core type to represent a catch-all
   */
  public Set transitiveClosure(
    Term subject,
    Term object,
    Term relation
  ) throws OntologyException;
  
  
  /**
   * Works out if <code>subject</code> is an instance of the type represented
   * by <code>object</code>.
   *
   * <p>
   * isa(a, b) is true if and only if there is a way to walk from subject to
   * object through the ontology by only passing through <code>is-a</code>
   * relationships. In the case that an ontology has remote terms, it is the
   * responsibility of the ontology reasoner to call isa on ontologies and
   * follow RemoteTerm instances between them. It follows that both subject and
   * object must come from the same ontology.
   * </p>
   *
   * <p>
   * It is important that the <code>is-a</code> can be calculated independantly
   * of transitiveClosure, as transitiveClosure relies upon <code>is-a</code>
   * to work and <code>is-a</code> needs to potentialy walk the inheritance tree
   * to evaluate.
   * </p>
   *
   * <p>
   * This isa method is expected to understand that every term is an any, and
   * that the sub-types of terms are instances of the relevant core types.
   * </p>
   *
   * @param subject  the subject Term
   * @param object   the Object Term
   * @return true if subject is an instance of object, false otherwise
   * @throws OntologyException if there was some error in accessing the ontology
   * @throws NullPointerException if either subject or object are null
   * @throws IllegalArgumentException if either subject or object are not in
   *         the current ontology
   */
  public boolean isa(Term subject, Term object)
  throws OntologyException;
  
  
}
