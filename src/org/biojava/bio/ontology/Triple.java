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

package org.biojava.bio.ontology;

import java.util.*;
import org.biojava.utils.*;

/**
 * A triple in an ontology.  This is two terms and a relationship between
 * them, similar to RDF and other similar logic systems.
 *
 * @author Thomas Down
 * @since 1.4
 */
 
public interface Triple {
    /**
     * Return the subject term of this triple
     */
    
    public Term getSubject();
    
    /**
     * Return the object term of this triple.
     */
    
    public Term getObject();
    
    /**
     * Return a Term which defines the type of relationship between the subject and object terms.
     */
    
    public Term getRelation();
    
    /**
     * Basic in-memory implementation of a Triple in an ontology
     *
     * @for.developer This can be used to implement Ontology.createTriple
     */
    
    public static final class Impl implements Triple {
        private final Term subject;
        private final Term object;
        private final Term relation;
        
        public Impl(Term subject, Term object, Term relation) {
            if (subject == null) {
                throw new IllegalArgumentException("Subject must not be null");
            }
            if (object == null) {
                throw new IllegalArgumentException("Object must not be null");
            }
            if (relation == null) {
                throw new IllegalArgumentException("Relation must not be null");
            }
            
            this.subject = subject;
            this.object = object;
            this.relation = relation;
        }
        
        public Term getSubject() {
            return subject;
        }
        
        public Term getObject() {
            return object;
        }
        
        public Term getRelation() {
            return relation;
        }
        
        /**
         * Two triples are equal if all their fields are identical.
         */
        
        public boolean equals(Object o) {
            if (! (o instanceof Triple)) {
                return false;
            }
            Triple to = (Triple) o;
            return to.getSubject() == getSubject() &&
                   to.getObject() == getObject() &&
                   to.getRelation() == getRelation();
        }
        
        public int hashCode() {
            return getSubject().hashCode() + getObject().hashCode() + getRelation().hashCode();
        }
        
        public String toString() {
            return subject.toString() + "," + object.toString() + ": " + relation.toString();
        }
    }
}
