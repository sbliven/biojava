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
 * A term in an ontology.  This has an {@link org.biojava.bio.Annotation Annotation}
 * which can be used for storing additional human-displayable information.  It
 * is strongly recommended that the Annotation is not used for any machine-readable
 * data -- this should be represented by relations in the ontology instead.
 *
 * @author Thomas Down
 * @since 1.4
 */

public interface Term extends Annotatable {
    /**
     * Return the name of this term.
     */
    
    public String getName();
    
    /**
     * Return a human-readable description of this term, or the empty string if
     * none is available.
     */
    
    public String getDescription();
    
    /**
     * Simple in-memory implementation of an ontology term.
     *
     * @for.developer This can be used to implement Ontology.createTerm
     */
    
    public final static class Impl extends AbstractChangeable implements Term {
        private final String name;
        private final String description;
        private transient ChangeForwarder forwarder;
        private Annotation annotation;
        
        public Impl(String name, String description) {
            if (name == null) {
                throw new IllegalArgumentException("Name must not be null");
            }
            if (description == null) {
                throw new IllegalArgumentException("Description must not be null");
            }
            
            this.name = name;
            this.description = description;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String toString() {
            return name;
        }
        
        public Annotation getAnnotation() {
            if (annotation == null) {
                annotation = new SmallAnnotation();
            }
            return annotation;
        }
        
        public ChangeSupport getChangeSupport(ChangeType ct) {
            ChangeSupport cs = super.getChangeSupport(ct);
            forwarder = new ChangeForwarder(this, cs) {
                protected ChangeEvent generateEvent(ChangeEvent cev) {
                    return new ChangeEvent(
                        getSource(),
                        Annotatable.ANNOTATION,
                        annotation,
                        null,
                        cev
                    );
                }
            } ;
            getAnnotation().addChangeListener(forwarder, ChangeType.UNKNOWN);
            return cs;
        }
    }
}
