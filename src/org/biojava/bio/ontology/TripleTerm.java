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
 * A term in an ontology which identifies a set of triples.
 *
 * <p>
 * In order to describe a relation, it is important to both describe it at the
 * level of general properties (such as symmetry) and also at the level of
 * constraints on the complete set of Terms that are part of that relation.
 * TripleTerm allows you to reason over Triples, or over sets of Triples.
 * For example, you could state that Triples involving is-a and has-a in your
 * ontology are exclusive. That is, you chose to either allow is-a or has-a
 * relations between any pair of terms, but not both. By using the size
 * operator, it is possible to constrain the cardinality of relations, for
 * example, to ensure that each car has four wheels or that only 1 Term can
 * ever be in the object slot of a singleton property.
 * </p>
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.4
 */

public interface TripleTerm extends Term {
    /**
     * Return the subject of the identified triples.  <code>null</code> may
     * be used as a wildcard
     */
    
    public Term getSubject();
    
    /**
     * Return the object of the identified triples.  <code>null</code> may
     * be used as a wildcard
     */
    
    public Term getObject();
    
    /**
     * Return the relation of the identified triples.  <code>null</code> may
     * be used as a wildcard
     */
    
    public Term getRelation();
    
    /**
     * Simple in-memory implementation of a remote ontology term.
     *
     * @for.developer This can be used to implement Ontology.importTerm
     */
    
    public final static class Impl
    extends AbstractChangeable
    implements TripleTerm, java.io.Serializable {
        private final Ontology ontology;
        private final Term subject;
        private final Term object;
        private final Term relation;
        private transient ChangeForwarder forwarder;
        
        public Impl(Ontology ontology, Term subject, Term object, Term relation) {
            if (ontology == null) {
                throw new NullPointerException("Ontology must not be null");
            }
            this.ontology = ontology;
            this.subject = subject;
            this.object = object;
            this.relation = relation;
        }
        
        public Ontology getOntology() {
            return ontology;
        }
        
        public String getName() {
            return "(" + subject.getName() + "," + object.getName() + "," + relation.getName() + ")";
        }
        
        public String getDescription() {
            return getName();
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
        
        public String toString() {
            return getName();
        }
        
        public Annotation getAnnotation() {
            return Annotation.EMPTY_ANNOTATION;
        }
        
        public ChangeSupport getChangeSupport(ChangeType ct) {
            ChangeSupport cs = super.getChangeSupport(ct);
            forwarder = new ChangeForwarder(this, cs) {
                protected ChangeEvent generateEvent(ChangeEvent cev) {
                    if (cev.getSource() instanceof Ontology) {
                        return new ChangeEvent(
                            getSource(),
                            Term.ONTOLOGY,
                            getOntology(),
                            null,
                            cev
                        );
                    } else if (cev.getSource() instanceof Term) {
                        return new ChangeEvent(
                            getSource(),
                            ChangeType.UNKNOWN,
                            cev.getSource(),
                            null,
                            cev
                       );
                    } else {
                        throw new BioRuntimeException("Unknown event");
                    }
                }
            } ;
            subject.addChangeListener(forwarder, ChangeType.UNKNOWN);
            object.addChangeListener(forwarder, ChangeType.UNKNOWN);
            relation.addChangeListener(forwarder, ChangeType.UNKNOWN);
            return cs;
        }
    }
}
