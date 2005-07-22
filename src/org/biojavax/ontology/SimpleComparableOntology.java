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
 * SimpleComparableOntology.java
 *
 * Created on June 16, 2005, 2:30 PM
 */

package org.biojavax.ontology;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.biojava.ontology.AlreadyExistsException;
import org.biojava.ontology.DefaultOps;
import org.biojava.ontology.OntologyOps;
import org.biojava.ontology.Term;
import org.biojava.ontology.Triple;
import org.biojava.ontology.Variable;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.SmallMap;
import org.biojava.utils.SmallSet;



/**
 * Represents an ontology that can be compared to other ontologies.
 *
 * Equality is based on name alone.
 *
 * @author Richard Holland
 * @author Mark Schreiber
 */
public class SimpleComparableOntology extends AbstractChangeable implements ComparableOntology {
    
    /**
     * The name of this ontology.
     */
    private String name;
    /**
     * The description of this ontology.
     */
    private String description;
    /**
     * The terms of this ontology.
     */
    private Map terms;
    /**
     * The triples of this ontology.
     */
    private Set triples;
    /**
     * The ops of this ontology.
     */
    private OntologyOps ops;
    
    /**
     * Creates a new instance of SimpleComparableOntology
     * @param name the name of the ontology.
     */
    public SimpleComparableOntology(String name) {
        if (name==null) throw new IllegalArgumentException("Name cannot be null");
        this.name = name;
        this.description = null;
        this.terms = new SmallMap();
        this.triples = new SmallSet();
        this.ops = new DefaultOps() {
            public Set getRemoteTerms() {
                return Collections.EMPTY_SET;
            }
        };
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
        ComparableOntology them = (ComparableOntology)o;
        return this.getName().compareTo(them.getName());
    }
    
    /**
     * Indicates whether some other object is "equal to" this one.
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     * @see     #hashCode()
     * @see     java.util.Hashtable
     */
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if (obj==null || !(obj instanceof ComparableOntology)) return false;
        ComparableOntology them = (ComparableOntology)obj;
        return this.getName().equals(them.getName());
    }
    
    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hashtables such as those provided by
     * <code>java.util.Hashtable</code>.
     * @return  a hash code value for this object.
     * @see     java.lang.Object#equals(java.lang.Object)
     * @see     java.util.Hashtable
     */
    public int hashCode() {
        int hash = 17;
        return 31*hash + this.getName().hashCode();
    }
    
    /**
     * Returns the name of the ontology as a <code>String.</code>
     * @return  a <code>String<code> representation of the object.
     * @see #getName()
     */
    public String toString() {
        return this.getName();
    }
    
    /**
     * Determines if this ontology currently contains a term named <code>name</code>
     * @param name the name of the term to look for.
     * @return true if it contains it, false if not.
     */
    public boolean containsTerm(String name) {
        return this.terms.containsKey(name);
    }
    
    /**
     * Fetch the term with the specified name.
     * @return The term named <code>s</code>
     * @param s The name to look for.
     * @throws NoSuchElementException if no term exists with that name
     */
    public Term getTerm(String s) throws NoSuchElementException {
        if (!this.terms.containsKey(s)) throw new NoSuchElementException("Ontology does not have term "+s);
        return (ComparableTerm)this.terms.get(s);
    }
    
    /**
     * Create a new term in this ontology.
     * @return The newly created term.
     * @param name The name of the term (must be unique)
     * @param description A human-readable description (may be empty)
     * @param synonyms Some synonyms for this term.
     * @throws org.biojava.utils.ChangeVetoException if it doesn't want to be created.
     * @throws IllegalArgumentException if either <code>name</code> or
     *         <code>description</code> is <code>null</code>, or violates
     *         some other constraint of this implementation.
     * @throws AlreadyExistsException if a term of this name already exists
     */
    public Term createTerm(String name, String description, Object[] synonyms) throws AlreadyExistsException, ChangeVetoException, IllegalArgumentException {
        if (name==null) throw new IllegalArgumentException("Name cannot be null");
        if (this.terms.containsKey(name)) throw new AlreadyExistsException("Ontology already has term");
        ComparableTerm ct = new SimpleComparableTerm(this,name,description,synonyms);
        if(!this.hasListeners(ComparableOntology.TERM)) {
            this.terms.put(name,ct);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    ComparableOntology.TERM,
                    ct,
                    this.terms.get(name)
                    );
            ChangeSupport cs = this.getChangeSupport(ComparableOntology.TERM);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.terms.put(name,ct);
                cs.firePostChangeEvent(ce);
            }
        }
        return ct;
    }
    
    /**
     * Create a view of a term from another ontology.  If the requested term
     * has already been imported under that name, this method returns the existing
     * RemoteTerm object. If the term that is being imported is itself a
     * RemoteTerm instance then first unwrap the term back to the orriginal
     * term it represents and then produce a RemoteTerm from that. If the term
     * being imported orriginated from this ontology, then return that term
     * unaltered.
     *
     * This particular instance however ignores all the above as BioSQL has no concept
     * of remote terms. Instead, it makes a copy of the imported term and returns a
     * pointer to it (or a pointer to the existing copy if one exists). Thus the term
     * becomes a part of this ontology instead of a pointer to another ontology.
     * @param t the Term to import
     * @param localName the local name to import it under, optionally null
     * @throws org.biojava.utils.ChangeVetoException if it doesn't want to be imported.
     * @throws java.lang.IllegalArgumentException if any of the arguments were null.
     * @return the newly imported term.
     */
    public Term importTerm(Term t, String localName) throws ChangeVetoException, IllegalArgumentException {
        if (localName==null) localName=t.getName();
        if (localName==null) throw new IllegalArgumentException("Name cannot be null");
        if (this.terms.containsKey(localName)) return (ComparableTerm)this.terms.get(localName);
        ComparableTerm ct = new SimpleComparableTerm(this,localName,t.getDescription(),t.getSynonyms());
        if(!this.hasListeners(ComparableOntology.TERM)) {
            this.terms.put(localName,ct);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    ComparableOntology.TERM,
                    ct,
                    this.terms.get(localName)
                    );
            ChangeSupport cs = this.getChangeSupport(ComparableOntology.TERM);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.terms.put(localName,ct);
                cs.firePostChangeEvent(ce);
            }
        }
        return ct;
    }
    
    /**
     * Creates a new Triple.
     * @return a new Triple over these three terms
     * @param subject the subject Term
     * @param object the object Term
     * @param predicate the predicate Term
     * @param name the name of the triple, or null  - IGNORED
     * @param description the description of the triple, or null  - IGNORED
     * @throws AlreadyExistsException if a triple already exists with the same
     *      subject, object and predicate, regardless of the name and description
     * @throws ChangeVetoException if it doesn't want to change.
     */
    public Triple createTriple(Term subject, Term object, Term predicate, String name, String description) throws AlreadyExistsException, ChangeVetoException {
        if (this.containsTriple(subject,object,predicate)) throw new AlreadyExistsException("Ontology already has triple");
        if (!(subject instanceof ComparableTerm)) throw new IllegalArgumentException("Subject must be a ComparableTerm");
        if (!(object instanceof ComparableTerm)) throw new IllegalArgumentException("Object must be a ComparableTerm");
        if (!(predicate instanceof ComparableTerm)) throw new IllegalArgumentException("Predicate must be a ComparableTerm");
        ComparableTriple ct = new SimpleComparableTriple(this,(ComparableTerm)subject,(ComparableTerm)object,(ComparableTerm)predicate);
        if (!this.triples.contains(ct)) {
            if(!this.hasListeners(ComparableOntology.TRIPLE)) {
                this.triples.add(ct);
            } else {
                ChangeEvent ce = new ChangeEvent(
                        this,
                        ComparableOntology.TRIPLE,
                        ct,
                        null
                        );
                ChangeSupport cs = this.getChangeSupport(ComparableOntology.TRIPLE);
                synchronized(cs) {
                    cs.firePreChangeEvent(ce);
                    this.triples.add(ct);
                    cs.firePostChangeEvent(ce);
                }
            }
        }
        return ct;
    }
    
    /**
     * Remove a term from an ontology, together with all triples which refer to it.
     * @param t the term to delete.
     * @throws org.biojava.utils.ChangeVetoException if it doesn't want to change.
     */
    public void deleteTerm(Term t) throws ChangeVetoException {
        for (Iterator i = this.triples.iterator(); i.hasNext();) {
            ComparableTriple ct = (ComparableTriple)i.next();
            if (ct.equals(t) || ct.getSubject().equals(t) || ct.getObject().equals(t) || ct.getPredicate().equals(t)) {
                if(!this.hasListeners(ComparableOntology.TRIPLE)) {
                    i.remove();
                } else {
                    ChangeEvent ce = new ChangeEvent(
                            this,
                            ComparableOntology.TRIPLE,
                            null,
                            ct
                            );
                    ChangeSupport cs = this.getChangeSupport(ComparableOntology.TRIPLE);
                    synchronized(cs) {
                        cs.firePreChangeEvent(ce);
                        i.remove();
                        cs.firePostChangeEvent(ce);
                    }
                }
            }
        }
        if(!this.hasListeners(ComparableOntology.TERM)) {
            if (t instanceof Triple) this.triples.remove(t);
            else this.terms.remove(t.getName());
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    ComparableOntology.TERM,
                    null,
                    t
                    );
            ChangeSupport cs = this.getChangeSupport(ComparableOntology.TERM);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                if (t instanceof Triple) this.triples.remove(t);
                else this.terms.remove(t.getName());
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * Return all triples from this ontology which match the supplied
     * pattern.  If any of the parameters of this method are <code>null</code>,
     * they are treated as wildcards.
     * @param subject The subject to search for, or <code>null</code>
     * @param object The object to search for, or <code>null</code>
     * @param predicate The relationship to search for, or <code>null</code>.
     * @return the set of triples.
     */
    public Set getTriples(Term subject, Term object, Term predicate) {
        Set results = new SmallSet();
        for (Iterator i = this.triples.iterator(); i.hasNext();) {
            ComparableTriple ct = (ComparableTriple)i.next();
            if ((subject==null || ct.getSubject().equals(subject)) &&
                    (object==null || ct.getObject().equals(object)) &&
                    (predicate==null || ct.getPredicate().equals(predicate))) results.add(ct);
        }
        return results;
    }
    
    /**
     * Return all the terms in this ontology
     * @return the set of terms.
     */
    public Set getTerms() {
        return Collections.unmodifiableSet(new HashSet(this.terms.values()));
    }
    
    /**
     * See if a triple exists in this ontology
     * @param subject the subject to test for.
     * @param object the object to test for.
     * @param predicate the predicate to test for.
     * @return the triple if it was found, or null if not.
     */
    public boolean containsTriple(Term subject, Term object, Term predicate) {
        for (Iterator i = this.triples.iterator(); i.hasNext();) {
            ComparableTriple ct = (ComparableTriple)i.next();
            if (ct.getSubject().equals(subject) &&
                    ct.getObject().equals(object) &&
                    ct.getPredicate().equals(predicate)) return true;
        }
        return false;
    }
    
    /**
     * Create a new term in this ontology.
     * @return The newly created term.
     * @param name The name of the term (must be unique)
     * @param description A human-readable description (may be empty)
     * @throws org.biojava.utils.ChangeVetoException if it doesn't want to change.
     * @throws IllegalArgumentException if either <code>name</code> or
     *         <code>description</code> is <code>null</code>, or violates
     *         some other constraint of this implementation.
     * @throws AlreadyExistsException if a term of this name already exists
     */
    public Term createTerm(String name, String description) throws AlreadyExistsException, ChangeVetoException, IllegalArgumentException {
        return this.createTerm(name,description,null);
    }
    
    /**
     * Create a new term in this ontology that is used as a variable.
     * @return The newly created term.
     * @param name The name of the term (must be unique)
     * @param description A human-readable description (may be empty)
     * @throws org.biojava.utils.ChangeVetoException if it doesn't want to change.
     * @throws IllegalArgumentException if either <code>name</code> or
     *         <code>description</code> is <code>null</code>, or violates
     *         some other constraint of this implementation.
     * @throws AlreadyExistsException if a term of this name already exists
     */
    public Variable createVariable(String name, String description) throws AlreadyExistsException, ChangeVetoException, IllegalArgumentException {
        throw new ChangeVetoException("BioSQL doesn't know what these are so we cowardly refuse to know too.");
    }
    
    /**
     * Sets a human-readable description of this ontology.
     * @param description the description.
     * @throws org.biojava.utils.ChangeVetoException in case of problems.
     */
    public void setDescription(String description) throws ChangeVetoException {
        if(!this.hasListeners(ComparableOntology.DESCRIPTION)) {
            this.description = description;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    ComparableOntology.DESCRIPTION,
                    description,
                    this.description
                    );
            ChangeSupport cs = this.getChangeSupport(ComparableOntology.DESCRIPTION);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.description = description;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * Return a human-readable description of this ontology.
     * @return the description.
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Return the name of this ontology
     * @see #getName()
     * @return the name of this ontology.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Return the associated OntologyOps.
     *
     * This method should be implemented by ontology
     * implementors to allow OntoTools
     * to get optimized access to some usefull ontology operations. It is not
     * intended that users will ever invoke this. A sensible dumb implementation
     * of this would return a per-ontology instance of DefaultOps.
     *
     * @return the OntologyOps instance associated with this instance.
     */
    public OntologyOps getOps() {
        return this.ops;
    }
    
}
