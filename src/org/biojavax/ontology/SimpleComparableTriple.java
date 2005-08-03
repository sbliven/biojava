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
 * SimpleComparableTriple.java
 *
 * Created on July 11, 2005, 10:54 AM
 */

package org.biojavax.ontology;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.biojava.bio.Annotation;
import org.biojava.ontology.Ontology;
import org.biojava.ontology.Term;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;


/**
 * Basic comparable triple, BioSQL style.
 *
 * Equality is a unique combination of ontology, predicate, object and subject.
 *
 * @author Richard Holland
 */
public class SimpleComparableTriple extends AbstractChangeable implements ComparableTriple {
    
    private ComparableOntology ontology;
    private ComparableTerm object;
    private ComparableTerm subject;
    private ComparableTerm predicate;
    private Set descriptors = new HashSet();
    
    /**
     * Creates a new instance of SimpleComparableTriple.
     * @param ontology the ontology of the triple.
     * @param subject the subject of the triple.
     * @param object the object of the triple.
     * @param predicate the predicate of the triple.
     */
    public SimpleComparableTriple(ComparableOntology ontology, ComparableTerm subject, ComparableTerm object, ComparableTerm predicate) {
        if (ontology == null) throw new IllegalArgumentException("Ontology must not be null");
        if (subject == null) throw new IllegalArgumentException("Subject must not be null");
        if (object == null) throw new IllegalArgumentException("Object must not be null");
        if (predicate == null) throw new IllegalArgumentException("Predicate must not be null");
        this.ontology = ontology;
        this.subject = subject;
        this.object = object;
        this.predicate = predicate;
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleComparableTriple() {}
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        ComparableTriple them = (ComparableTriple)o;
        // Hibernate comparison - we haven't been populated yet
        if (this.ontology==null) return -1;
        // Normal comparison
        if (!this.ontology.equals(them.getOntology())) return this.ontology.compareTo((ComparableOntology)them.getOntology());
        if (!this.subject.equals(them.getSubject())) return this.subject.compareTo((ComparableTerm)them.getSubject());
        if (!this.object.equals(them.getObject())) return this.object.compareTo((ComparableTerm)them.getObject());
        return this.predicate.compareTo((ComparableTerm)them.getPredicate());
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if(this == o) return true;
        if (o==null || !(o instanceof ComparableTriple)) return false;
        // Hibernate comparison - we haven't been populated yet
        if (this.ontology==null) return false;
        // Normal comparison
        ComparableTriple them = (ComparableTriple)o;
        return (this.ontology.equals(them.getOntology()) &&
                this.subject.equals(them.getSubject()) &&
                this.object.equals(them.getObject()) &&
                this.predicate.equals(them.getPredicate()));
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int code = 17;
        // Hibernate comparison - we haven't been populated yet
        if (this.ontology==null) return code;
        // Normal comparison
        code = 37*code + this.ontology.hashCode();
        code = 37*code + this.subject.hashCode();
        code = 37*code + this.object.hashCode();
        code = 37*code + this.predicate.hashCode();
        return code;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return this.predicate.getName() + "(" + this.subject.getName() + ", " + this.object.getName() + ")";
    }
    
    /**
     * {@inheritDoc}
     */
    public Term getSubject() {
        return this.subject;
    }
    
    // Hibernate requirement - not for public use.
    private void setSubject(ComparableTerm subject) { this.subject = subject; }
    
    /**
     * {@inheritDoc}
     */
    public Term getObject() {
        return this.object;
    }
    
    // Hibernate requirement - not for public use.
    private void setObject(ComparableTerm object) { this.object = object; }
    
    /**
     * {@inheritDoc}
     */
    public Term getPredicate() {
        return this.predicate;
    }
    
    // Hibernate requirement - not for public use.
    private void setPredicate(ComparableTerm predicate) { this.predicate = predicate; }
    
    /**
     * {@inheritDoc}
     */
    public void addDescriptor(ComparableTerm desc) throws IllegalArgumentException,ChangeVetoException {
        if (desc==null) throw new IllegalArgumentException("Cannot have null descriptor");
        if(!this.hasListeners(ComparableTriple.DESCRIPTOR)) {
            this.descriptors.add(desc);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    ComparableTriple.DESCRIPTOR,
                    desc,
                    null
                    );
            ChangeSupport cs = this.getChangeSupport(ComparableTriple.DESCRIPTOR);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.descriptors.add(desc);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean removeDescriptor(ComparableTerm desc) throws IllegalArgumentException,ChangeVetoException {
        if (desc==null) throw new IllegalArgumentException("Cannot have null descriptor");
        boolean result;
        if(!this.hasListeners(ComparableTriple.DESCRIPTOR)) {
            result = this.descriptors.remove(desc);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    ComparableTriple.DESCRIPTOR,
                    null,
                    desc
                    );
            ChangeSupport cs = this.getChangeSupport(ComparableTriple.DESCRIPTOR);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                result = this.descriptors.remove(desc);
                cs.firePostChangeEvent(ce);
            }
        }
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public Set getDescriptors() { return Collections.unmodifiableSet(this.descriptors); }
    
    /**
     * {@inheritDoc}
     */
    public void setDescriptors(Set descriptors) throws ChangeVetoException {
        this.descriptors.clear();
        if (descriptors==null) return;
        for (Iterator i = descriptors.iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (!(o instanceof ComparableTerm)) throw new ChangeVetoException("Descriptors must be comparable terms");
            this.addDescriptor((ComparableTerm)o);
        }
    }
    
    /**
     * {@inheritDoc}
     * NOT IMPLEMENTED
     */
    public void removeSynonym(Object synonym) {
        throw new UnsupportedOperationException("BioJavaX does not know about triple synonyms.");
    }
    
    /**
     * {@inheritDoc}
     * NOT IMPLEMENTED
     */
    public void addSynonym(Object synonym) {
        throw new UnsupportedOperationException("BioJavaX does not know about triple synonyms.");
    }
    
    /**
     * {@inheritDoc}
     * ALWAYS RETURNS AN EMPTY LIST
     */
    public Object[] getSynonyms() {
        return Collections.EMPTY_LIST.toArray();
    }
    
    /**
     * {@inheritDoc}
     */
    public Ontology getOntology() {
        return this.ontology;
    }
    
    // Hibernate requirement - not for public use.
    private void setOntology(ComparableOntology descriptors) { this.ontology = ontology; }
    
    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return "";
    }
    
    /**
     * {@inheritDoc}
     */
    public Annotation getAnnotation() {
        return Annotation.EMPTY_ANNOTATION;
    }
    
    /**
     * {@inheritDoc}
     * The string takes the form <code>predicate(subject,object)</code>
     */
    public String toString() {
        return this.predicate+"("+this.subject+","+this.object+")";
    }
    
    
    // Hibernate requirement - not for public use.
    private Long id;
    
    
    // Hibernate requirement - not for public use.
    private Long getId() { return this.id; }
    
    
    // Hibernate requirement - not for public use.
    private void setId(Long id) { this.id = id; }
}
