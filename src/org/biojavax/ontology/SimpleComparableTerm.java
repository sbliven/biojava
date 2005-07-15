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
 * SimpleComparableTerm.java
 *
 * Created on July 13, 2005, 10:22 AM
 */

package org.biojavax.ontology;

import org.biojava.ontology.Ontology;
import org.biojava.ontology.Term;

/**
 * A Term object that can be compared and thus sorted.
 *
 * Equality is inherited from Term.Impl.
 *
 * @author Richard Holland
 */
public class SimpleComparableTerm extends Term.Impl implements ComparableTerm {
    
    /**
     * Creates a new instance of SimpleComparableTerm with synonyms.
     * @param ontology The ontology to put the term in.
     * @param name the name of the term.
     * @param description the description for the term.
     * @param synonyms a set of synonyms for the term.
     */
    public SimpleComparableTerm(ComparableOntology ontology, String name, String description, Object[] synonyms) {
        super((Ontology)ontology,name,description,synonyms);
    }
    
    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * @return a negative integer, zero, or a positive integer as this object
     * 		is less than, equal to, or greater than the specified object.
     * @param o the Object to be compared.
     */
    public int compareTo(Object o) {
        ComparableTerm them = (ComparableTerm)o;
        if (this.getOntology().equals(them.getOntology())) return ((ComparableOntology)this.getOntology()).compareTo(them.getOntology());
        return this.getName().compareTo(them.getName());
    }
}