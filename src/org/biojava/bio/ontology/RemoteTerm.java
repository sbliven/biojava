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
 * A term in another ontology.
 *
 * <p>
 * This is how you allow one ontology to refer to terms in another one. Since
 * these ontologies are designed to be modular and self-contained, it is
 * expected that you would not copy terms from one ontology into another. The
 * best-practice way to represent terms from another ontology in your one is to
 * use RemoteTerm instances. Ontology has a method importTerm that does this
 * for you.
 * </p>
 *
 * <p>
 * The imported term will have the same name as the original term. They are
 * implicitly identical to each other. The most common use of imports will be
 * to slurp in the "core" ontology so that opperations such as <code>is-a</code>
 * and <code>has-a</code> are available.
 * </p>
 *
 * @author Thomas Down
 * @author Matthew Pocock
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
    
    public final static class Impl
    extends AbstractTerm
    implements RemoteTerm, java.io.Serializable {
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
            return getOntology().getName() + ":" + remoteTerm.getName();
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
