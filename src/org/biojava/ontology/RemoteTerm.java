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
 * A term in an ontology
 *
 * @author Thomas Down
 * @since 1.4
 */

public interface RemoteTerm extends Term {
    /**
     * Return the imported term
     */
     
    public Term getRemoteTerm();
    
    /**
     * Simple in-memory implementation of a remote ontology term.
     *
     * @for.developer This can be used to implement Ontology.importTerm
     */
    
    public final static class Impl extends AbstractTerm implements RemoteTerm {
        private final Ontology ontology;
        private final Term remoteTerm;
        private transient ChangeForwarder forwarder;
        
        public Impl(Ontology ontology, Term remoteTerm) {
            if (ontology == null) {
                throw new IllegalArgumentException("Ontology must not be null");
            }
            if (remoteTerm == null) {
                throw new IllegalArgumentException("RemoteTerm must not be null");
            }
            
            this.ontology = ontology;
            this.remoteTerm = remoteTerm;
        }
        
        public String getName() {
            return remoteTerm.getName();
        }
        
        public String getDescription() {
            return remoteTerm.getDescription();
        }
        
        public Ontology getOntology() {
            return ontology;
        }
        
        public Term getRemoteTerm() {
            return remoteTerm;
        }
        
        public String toString() {
            return "[" + remoteTerm.getOntology().getName() + ":" + getName() + "]";
        }
        
        public Annotation getAnnotation() {
            return remoteTerm.getAnnotation();
        }
    }
}
