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
import org.biojava.bio.*;

/**
 * A term in an ontology which identifies another ontology.
 *
 * @author Thomas Down
 * @since 1.4
 */

public interface OntologyTerm extends Term {
    /**
     * Get the remote ontology referenced by this term
     */
    
    public Ontology getOntology();
    
    /**
     * Simple in-memory implementation of a remote ontology term.
     *
     * @for.developer This can be used to implement Ontology.importTerm
     */
    
    public final static class Impl extends AbstractChangeable implements OntologyTerm {
        private final Ontology onto;
        private transient ChangeForwarder forwarder;
        
        public Impl(Ontology onto) {
            if (onto == null) {
                throw new NullPointerException("The targetted ontology may not be null");
            }
            this.onto = onto;
        }
        
        public String getName() {
            return onto.getName();
        }
        
        public String getDescription() {
            return onto.getDescription();
        }
        
        public Ontology getOntology() {
            return onto;
        }
        
        public String toString() {
            return "Remote ontology: " + getName();
        }
        
        public Annotation getAnnotation() {
            return Annotation.EMPTY_ANNOTATION;
        }
        
        public ChangeSupport getChangeSupport(ChangeType ct) {
            ChangeSupport cs = super.getChangeSupport(ct);
            forwarder = new ChangeForwarder(this, cs) {
                protected ChangeEvent generateEvent(ChangeEvent cev) {
                    return new ChangeEvent(
                        getSource(),
                        ChangeType.UNKNOWN,
                        onto,
                        null,
                        cev
                    );
                }
            } ;
            onto.addChangeListener(forwarder, ChangeType.UNKNOWN);
            return cs;
        }
    }
}
