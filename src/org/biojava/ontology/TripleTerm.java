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
 * @author Thomas Down
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
    
    public final static class Impl extends AbstractChangeable implements TripleTerm {
        private final Term subject;
        private final Term object;
        private final Term relation;
        private transient ChangeForwarder forwarder;
        
        public Impl(Term subject, Term object, Term relation) {
            this.subject = subject;
            this.object = object;
            this.relation = relation;
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
                    return new ChangeEvent(
                        getSource(),
                        cev.getType(),
                        cev.getChange(),
                        cev.getPrevious(),
                        cev
                    );
                }
            } ;
            subject.addChangeListener(forwarder, ChangeType.UNKNOWN);
            object.addChangeListener(forwarder, ChangeType.UNKNOWN);
            relation.addChangeListener(forwarder, ChangeType.UNKNOWN);
            return cs;
        }
    }
}
