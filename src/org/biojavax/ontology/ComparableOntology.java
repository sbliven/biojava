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
 * ComparableOntology.java
 *
 * Created on June 16, 2005, 2:29 PM
 */

package org.biojavax.ontology;
import org.biojava.ontology.Ontology;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.Changeable;

/**
 * An Ontology that can be compared to another.
 * @author Richard Holland
 */
public interface ComparableOntology extends Ontology,Comparable,Changeable {
    /**
     * A change type.
     */
    public static final ChangeType TERM = new ChangeType(
            "This ontology's terms have changed",
            "org.biojavax.ontology.ComparableOntology",
            "terms"
            );
    /**
     * A change type.
     */
    public static final ChangeType TRIPLE = new ChangeType(
            "This ontology's triples have changed",
            "org.biojavax.ontology.ComparableOntology",
            "triples"
            );
    /**
     * A change type.
     */
    public static final ChangeType DESCRIPTION = new ChangeType(
            "This ontology's description has changed",
            "org.biojavax.ontology.ComparableOntology",
            "description"
            );    
        
    /**
     * Sets a human-readable description of this ontology.
     * @param description the description.
     */
    public void setDescription(String description) throws ChangeVetoException;
    
    /**
     * Return a human-readable description of this ontology.
     * @return the description.
     */
    public String getDescription();
}
