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
 * ComparableTriple.java
 *
 * Created on July 11, 2005, 10:54 AM
 */

package org.biojavax.ontology;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.biojava.bio.Annotation;
import org.biojava.ontology.AlreadyExistsException;
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
    
    /**
     * The name of the ontology the triple lives in.
     */
    private ComparableOntology ontology;
    /**
     * The object of the triple.
     */
    private ComparableTerm object;
    /**
     * The subject of the triple.
     */
    private ComparableTerm subject;
    /**
     * The predicate of the triple.
     */
    private ComparableTerm predicate;
    /**
     * A set of terms describing the triple.
     */
    private Set descriptors;
    
    /**
     * Creates a new instance of SimpleComparableTriple
     * @param ontology the ontology of the triple.
     * @param subject the subject of the triple.
     * @param object the object of the triple.
     * @param predicate the predicate of the triple.
     */
    public SimpleComparableTriple(ComparableOntology ontology, ComparableTerm subject, ComparableTerm object, ComparableTerm predicate) {
        if (ontology == null) throw new NullPointerException("Ontology must not be null");
        if (subject == null) throw new NullPointerException("Subject must not be null");
        if (object == null) throw new NullPointerException("Object must not be null");
        if (predicate == null) throw new NullPointerException("Predicate must not be null");
        this.ontology = ontology;
        this.subject = subject;
        this.object = object;
        this.predicate = predicate;
        this.descriptors = new HashSet();
    }
    
    /**
     * Returns the name of the triple. This is:  predicate(subject,object)
     * @return the name of the triple.
     */
    public String getName() {
        return this.predicate.getName() + "(" + this.subject.getName() + ", " + this.object.getName() + ")";
    }
    
    /**
     * Returns the triple's subject.
     * @return the subject of the triple.
     */
    public Term getSubject() {
        return this.subject;
    }
    
    /**
     * Returns the triple's object.
     * @return the object of the triple.
     */
    public Term getObject() {
        return this.object;
    }
    
    /**
     * Return's the triple's predicate.
     * @return the predicate of the triple.
     */
    public Term getPredicate() {
        return this.predicate;
    }
    
    /**
     * Adds a descriptor.
     * @param desc the descriptor to add.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     * @throws AlreadyExistsException if the descriptor already exists.
     * @throws IllegalArgumentException if the descriptor is missing.
     */
    public void addDescriptor(ComparableTerm desc) throws AlreadyExistsException, IllegalArgumentException,ChangeVetoException {
        if (desc==null) throw new IllegalArgumentException("Cannot have null descriptor");
        if(!this.hasListeners(ComparableTriple.DESCRIPTOR)) {
            if (this.descriptors.contains(desc)) throw new AlreadyExistsException("Descriptor has already been used");
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
                if (this.descriptors.contains(desc)) throw new AlreadyExistsException("Descriptor has already been used");
                this.descriptors.add(desc);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * Removes a descriptor.
     * @return True if it did it, false if the descriptor did not exist.
     * @param desc the descriptor to remove.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     * @throws IllegalArgumentException if the descriptor is missing.
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
     * Tests for existence of a descriptor.
     * @return True if it exists, false if the descriptor did not exist.
     * @param desc the descriptor to look for.
     * @throws IllegalArgumentException if the descriptor is missing.
     */
    public boolean containsDescriptor(ComparableTerm desc) throws IllegalArgumentException {
        return this.descriptors.contains(desc);
    }
    
    /**
     * Returns all descriptors.
     * @return a set of all descriptors, possible empty.
     */
    public Set getDescriptors() {
        return Collections.unmodifiableSet(this.descriptors);
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
        ComparableTriple them = (ComparableTriple)o;
        if (!this.getOntology().equals(them.getOntology())) return ((ComparableOntology)this.getOntology()).compareTo((ComparableOntology)them.getOntology());
        if (!this.getSubject().equals(them.getSubject())) return ((ComparableTerm)this.getSubject()).compareTo((ComparableTerm)them.getSubject());
        if (!this.getObject().equals(them.getObject())) return ((ComparableTerm)this.getObject()).compareTo((ComparableTerm)them.getObject());
        return ((ComparableTerm)this.getPredicate()).compareTo((ComparableTerm)them.getPredicate());
    }
    
    /**
     * Two triples are equal if all their fields are identical.
     * @param o the object to compare to.
     * @return true if the object is a triple and its fields are all the same, false otherwise.
     */
    public boolean equals(Object o) {
        if(this == o) return true;
        if (o==null || !(o instanceof ComparableTriple)) return false;
        ComparableTriple them = (ComparableTriple)o;
        return (this.getOntology().equals(them.getOntology()) &&
                this.getSubject().equals(them.getSubject()) &&
                this.getObject().equals(them.getObject()) &&
                this.getPredicate().equals(them.getPredicate()));
    }
    
    /**
     * Returns a hash code for this object.
     * @return the hashcode.
     */
    public int hashCode() {
        int code = 17;
        code = 37*code + this.getOntology().hashCode();
        code = 37*code + this.getSubject().hashCode();
        code = 37*code + this.getObject().hashCode();
        code = 37*code + this.getPredicate().hashCode();
        return code;
    }
    
    /**
     * Remove a synonym for this term. NOT IMPLEMENTED.
     * @param synonym the synonym to remove.
     */
    public void removeSynonym(Object synonym) {
        throw new UnsupportedOperationException("BioSQL does not know about triple synonyms.");
    }
    
    /**
     * Add a synonym for this term. NOT IMPLEMENTED.
     * @param synonym the synonym to add.
     */
    public void addSynonym(Object synonym) {
        throw new UnsupportedOperationException("BioSQL does not know about triple synonyms.");
    }
    
    /**
     * Return the synonyms for this term.
     * @return the set of synonyms for this term.
     */
    public Object[] getSynonyms() {
        return Collections.EMPTY_LIST.toArray();
    }
    
    /**
     * Return the ontology in which this term exists.
     * @return the ontology for this term.
     */
    public Ontology getOntology() {
        return this.ontology;
    }
    
    /**
     * Return a human-readable description of this term, or the empty string if
     * none is available.
     * @return the description of this term.
     */
    public String getDescription() {
        return "";
    }
    
    /**
     * Should return the associated annotation object.
     *
     * @return an Annotation object, never null
     */
    public Annotation getAnnotation() {
        return Annotation.EMPTY_ANNOTATION;
    }
    
    /**
     * Returns a string representation of the object of the form
     * <code>predicate(subject,object)</code>
     * @return  a string representation of the object.
     */
    public String toString() {
        return this.predicate+"("+this.subject+","+this.object+")";
    }
}
