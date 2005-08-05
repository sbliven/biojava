/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

/*
 * BioDBUtils.java
 *
 * Created on August 1, 2005, 9:33 AM
 */

package org.biojavax.bio.db;

import java.util.HashMap;
import java.util.Map;
import org.biojavax.Namespace;
import org.biojavax.SimpleNamespace;
import org.biojavax.ontology.ComparableOntology;
import org.biojavax.ontology.ComparableTerm;
import org.biojavax.ontology.SimpleComparableOntology;

/**
 *
 * @author Richard Holland
 */
public class BioDBUtils {
    
    private static Map namespaces = new HashMap();
    
    private static Map ontologies = new HashMap();
    
    /**
     * The default namespace used for internal terms.
     */
    public static final Namespace DEFAULT_NAMESPACE = getNamespace("lcl");
    
    /**
     * Generates an namespace instance, or returns the existing one if it already exists.
     * @param name the name of the namespace to generate
     * @return the generated namespace
     */
    public static Namespace getNamespace(String name) {
        if (!namespaces.containsKey(name)) {
            namespaces.put(name, new SimpleNamespace(name));
        }
        return (Namespace)namespaces.get(name);
    }
    
    /**
     * The default ontology used for internal terms.
     */
    public static final ComparableOntology DEFAULT_ONTOLOGY = getOntology("biojavax");
    
    /**
     * Generates an ontology instance, or returns the existing one if it already exists.
     * @param name the name of the ontology to generate
     * @return the generated ontology
     */
    public static ComparableOntology getOntology(String name) {
        if (!ontologies.containsKey(name)) {
            ontologies.put(name, new SimpleComparableOntology(name));
        }
        return (ComparableOntology)ontologies.get(name);
    }
    
    /**
     * Generates a term instance, or returns the existing one if it already exists.
     * @param onto the ontology to generate the term in
     * @param name the term to create
     * @return the generated term
     */
    public static ComparableTerm getOntologyTerm(ComparableOntology onto, String name) {
        if (!onto.containsTerm(name)) {
            try {
                onto.createTerm(name, "automatically created by biojavax");
            } catch (Exception e) {
                throw new RuntimeException("Huh? Doesn't contain it but can't create it??");
            }
        }
        return (ComparableTerm)onto.getTerm(name);
    }
}
