package org.biojava.ontology;


/**
 * This is a mix-in interface for optimizing ontology operators.
 *
 * <p>
 * Some ontology implementations will be able to compute some derived properties
 * very quickly because of how they store their data. This is likely to out-
 * perform generic implementations of algorithms using the Ontology interface
 * to get the same result. Ontology instances provide an instance of
 * OntologyOps, publishing optimizations of some common operations. The reasoner
 * may then choose to call OntologyOps methods on the Ontology instance rather
 * than using its fall-back implementations.
 * </p>
 * 
 * @author Matthew Pocock
 * @since 1.4
 */
public interface OntologyOps {

}
