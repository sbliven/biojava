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
 * An ontology.
 *
 * <p>This is just a set of Term objects, and a set of
 * Triple objects describing relationships between these terms.
 * This class does not itself contain any reasoning functionality. Ontology is
 * a collection of facts, or axioms.</p>
 *
 * @author Thomas Down
 * @author Matthew Pocock
 *
 * @since 1.4
 */

public interface Ontology extends Changeable {
    public static final ChangeType TERM = new ChangeType(
      "A term has been added or removed",
      "org.biojava.bio.ontology.Ontology",
      "TERM"
    );
    
    public static final ChangeType TRIPLE = new ChangeType(
      "A triple has been added or removed",
      "org.biojava.bio.ontology.Ontology",
      "TRIPLE"
    );
    
    /**
     * Return the name of this ontology
     */
    
    public String getName();
    
    /**
     * Return a human-readable description of this ontology, or the empty
     * string if none is available
     */
    
    public String getDescription();
    
    /**
     * Return all the terms in this ontology
     */
    
    public Set getTerms();
    
    /**
     * Fetch the term with the specified name.
     *
     * @return The term named <code>name</code>
     * @throws NoSuchElementException if no term exists with that name
     */
    
    public Term getTerm(String s) throws NoSuchElementException;
    
    /**
     * Return all triples from this ontology which match the supplied
     * pattern.  If any of the parameters of this method are <code>null</code>,
     * they are treated as wildcards.
     *
     * @param subject The subject to search for, or <code>null</code>
     * @param object The object to search for, or <code>null</code>
     * @param relation The relationship to search for, or <code>null</code>.
     */
    
    public Set getTriples(Term subject, Term object, Term relation);
    
    /**
     * Return the associated OntologyOps.
     *
     * @for.developer  This method should be implemented by ontology
     * implementors to allow OntoTools
     * to get optimized access to some usefull ontology operations. It is not
     * intended that users will ever invoke this. A sensible dumb implementation
     * of this would return a per-ontology instance of DefaultOps.
     *
     * @return the OntologyOps instance associated with this instance.
     */
     
    public OntologyOps getOps();
    
    /**
     * Create a new term in this ontology
     *
     * @param name The name of the term (must be unique)
     * @param description A human-readable description (may be empty)
     * @throws IllegalArgumentException if either <code>name</code> or
     *         <code>description</code> is <code>null</code>, or violates
     *         some other constraint of this implementation.
     * @throws AlreadyExistsException if a term of this name already exists
     * @return The newly created term.
     */
    
    public Term createTerm(String name, String description)
    throws
      AlreadyExistsException,
      ChangeVetoException,
      IllegalArgumentException;
    
    /**
     * Create a term which represents a set of triples
     */
     
    public TripleTerm createTripleTerm(Term subject, Term object, Term relation)
    throws
      AlreadyExistsException,
      ChangeVetoException,
      IllegalArgumentException;
    
    /**
     * Create a view of a term from another ontology.  If the requested term
     * has already been imported, this method returns the existing RemoteTerm
     * object.
     */
   
    public RemoteTerm importTerm(Term t)
    throws
      AlreadyExistsException,
      ChangeVetoException,
      IllegalArgumentException;
   
    /**
     * Create a new triple in this ontology
     */
    
    public Triple createTriple(Term subject, Term object, Term relation)
    throws
      AlreadyExistsException,
      ChangeVetoException;
    
    /**
     * See if a triple exists in this ontology
     */
    
    public boolean containsTriple(Term subject, Term object, Term relation);
    
    /**
     * Remove a triple from an ontology
     */
    
    public void deleteTriple(Triple t) throws ChangeVetoException;
    
    /**
     * Remove a term from an ontology, together with all triples which refer to it.
     */
    
    public void deleteTerm(Term t) throws ChangeVetoException;
    
    /**
     * Determines if this ontology currently contains a term named <code>name</code>
     */
    
    public boolean containsTerm(String name);
    
    /**
     * A basic in-memory implementation of an ontology
     *
     * @author Thomas Down
     * @author Matthew Pocock
     * @since 1.3
     */
     
    
    public final class Impl
    extends AbstractChangeable
    implements Ontology, java.io.Serializable {
        private Map terms;
        private Set triples;
        private Map subjectTriples;
        private Map objectTriples;
        private Map relationTriples;
        private Map remoteTerms;
        
        private final String name;
        private final String description;
        private final OntologyOps ops;
        
        {
            terms = new HashMap();
            triples = new HashSet();
            subjectTriples = new HashMap();
            objectTriples = new HashMap();
            relationTriples = new HashMap();
            remoteTerms = new HashMap();
        }
        
        public Impl(String name, String description) {
            this.name = name;
            this.description = description;
            ops = new DefaultOps() {
              public Ontology getOntology() { return Ontology.Impl.this; }
            };
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public Set getTerms() {
            return new HashSet(terms.values());
        }
        
        public Term getTerm(String name) 
            throws NoSuchElementException
        {
            Term t = (Term) terms.get(name);
            if (t == null) {
                throw new NoSuchElementException("No term named " + name);
            } else {
                return (Term) terms.get(name);
            }
        }
        
        public Set getTriples(Term subject, Term object, Term relation) {
            if (subject != null) {
                return filterTriples((Set) subjectTriples.get(subject), null, object, relation);
            } else if (object != null) {
                return filterTriples((Set) objectTriples.get(object), subject, null, relation);
            } else if (relation != null) {
                return filterTriples((Set) relationTriples.get(relation), subject, object, null);
            } else {
                return filterTriples(triples, subject, object, relation);
            }
        }
        
        private Set filterTriples(Set base, Term subject, Term object, Term relation) {
            if (base == null) {
                return Collections.EMPTY_SET;
            } else if (subject == null && object == null && relation == null) {
                return Collections.unmodifiableSet(base);
            } 
            
            Set retval = new HashSet();
            for (Iterator i = base.iterator(); i.hasNext(); ) {
                Triple t = (Triple) i.next();
                if (subject != null && t.getSubject() != subject) {
                    continue;
                }
                if (object != null && t.getObject() != object) {
                    continue;
                }
                if (relation != null && t.getRelation() != relation) {
                    continue;
                }
                retval.add(t);
            }
            return retval;
        }
        
        private void addTerm(Term t)
            throws AlreadyExistsException, IllegalArgumentException, ChangeVetoException
        {
            if (terms.containsKey(t.getName())) {
                throw new AlreadyExistsException("Ontology " + getName() + " already contains " + t.getName());
            }
            
            if(!hasListeners()) {
                terms.put(t.getName(), t);
            } else {
                ChangeEvent ce = new ChangeEvent(
                this,
                Ontology.TERM,
                t,
                null
                );
                ChangeSupport cs = getChangeSupport(Ontology.TERM);
                synchronized(cs) {
                    cs.firePreChangeEvent(ce);
                    terms.put(t.getName(), t);
                    cs.firePostChangeEvent(ce);
                }
            }
        }
        
        public Term createTerm(String name, String description) 
            throws AlreadyExistsException, IllegalArgumentException, ChangeVetoException
        {
            Term t = new Term.Impl(this, name, description);
            addTerm(t);
            return t;
        }
        
        public TripleTerm createTripleTerm(Term subject, Term object, Term relation)
            throws AlreadyExistsException, ChangeVetoException
        {
            TripleTerm tt = new TripleTerm.Impl(this, subject, object, relation);
            if (!containsTerm(subject)) {
                throw new IllegalArgumentException("Term " + subject.getName() + " is not contained in this ontology");
            }
            if (!containsTerm(object)) {
                throw new IllegalArgumentException("Term " + object.getName() + " is not contained in this ontology");
            }
            if (!containsTerm(relation)) {
                throw new IllegalArgumentException("Term " + relation.getName() + " is not contained in this ontology");
            }
            addTerm(tt);
            return tt;
        }
        
        public OntologyTerm createOntologyTerm(Ontology o)
            throws AlreadyExistsException, ChangeVetoException
        { 
            OntologyTerm ot = new OntologyTerm.Impl(this, o);
            addTerm(ot);
            return ot;
        }
            
        
        public RemoteTerm importTerm(Term t)
            throws AlreadyExistsException, IllegalArgumentException, ChangeVetoException
        {
            RemoteTerm rt = (RemoteTerm) remoteTerms.get(t);
            if (rt == null) {
                rt = new RemoteTerm.Impl(this, t);
                addTerm(rt);
                remoteTerms.put(t, rt);
            }
            return rt;
        }
        
        public void deleteTerm(Term t) 
            throws ChangeVetoException
        {
            String name = t.getName();
            if (terms.get(name) != t) {
                return; // Should this be an exception?
            }
            if(!hasListeners()) {
                terms.remove(name);
            } else {
                ChangeEvent ce = new ChangeEvent(
                this,
                Ontology.TERM,
                null,
                t
                );
                ChangeSupport cs = getChangeSupport(Ontology.TERM);
                synchronized(cs) {
                    cs.firePreChangeEvent(ce);
                    terms.remove(name);
                    cs.firePostChangeEvent(ce);
                }
            }
        }
        
        public boolean containsTerm(String name) {
            return terms.containsKey(name);
        }
        
        private boolean containsTerm(Term t) {
            return (terms.get(t.getName()) == t);
        }
        
        public boolean containsTriple(Term subject, Term object, Term relation) {
          return triples.contains(new Triple.Impl(subject, object, relation));
        }
        
        public Triple createTriple(Term subject, Term object, Term relation)
            throws AlreadyExistsException, IllegalArgumentException, ChangeVetoException
        {
            Triple t = new Triple.Impl(subject, object, relation);
            if (!containsTerm(subject)) {
                throw new IllegalArgumentException("Ontology " + getName() + " doesn't contain " + subject);
            }
            if (!containsTerm(relation)) {
                throw new IllegalArgumentException("Ontology " + getName() + " doesn't contain " + relation);
            }
            if (!containsTerm(object)) {
                throw new IllegalArgumentException("Ontology " + getName() + " doesn't contain " + object);
            }
            if (triples.contains(t)) {
                throw new AlreadyExistsException("Ontology " + getName() + " already contains " + t.toString());
            }
            
            if(!hasListeners()) {
                addTriple(t);
            } else {
                ChangeEvent ce = new ChangeEvent(
                    this,
                    Ontology.TRIPLE,
                    t,
                    null
                );
                ChangeSupport cs = getChangeSupport(Ontology.TRIPLE);
                synchronized(cs) {
                    cs.firePreChangeEvent(ce);
                    addTriple(t);
                    cs.firePostChangeEvent(ce);
                }
            }
            return t;
        }
        
        private void addTriple(Triple t) {
            triples.add(t);
            pushTriple(subjectTriples, t.getSubject(), t);
            pushTriple(objectTriples, t.getObject(), t);
            pushTriple(relationTriples, t.getRelation(), t);
        }
        
        private void pushTriple(Map m, Term key, Triple t) {
            Set s = (Set) m.get(key);
            if (s == null) {
                s = new HashSet();
                m.put(key, s);
            }
            s.add(t);
        }
        
        public void deleteTriple(Triple t)
            throws ChangeVetoException
        {
            if (!triples.contains(t)) {
                return;
            }
            
            if(!hasListeners()) {
                removeTriple(t);
            } else {
                ChangeEvent ce = new ChangeEvent(
                this,
                Ontology.TRIPLE,
                null,
                t
                );
                ChangeSupport cs = getChangeSupport(Ontology.TERM);
                synchronized(cs) {
                    cs.firePreChangeEvent(ce);
                    removeTriple(t);
                    cs.firePostChangeEvent(ce);
                }
            }
        }
        
        private void removeTriple(Triple t) {
            triples.remove(t);
            pullTriple(subjectTriples, t.getSubject(), t);
            pullTriple(objectTriples, t.getObject(), t);
            pullTriple(relationTriples, t.getRelation(), t);
        }
        
        private void pullTriple(Map m, Term key, Triple t) {
            Set s = (Set) m.get(key);
            if (s != null) {
                s.remove(t);
            }
        }
        
        public OntologyOps getOps() {
          return ops;
        }
        
        public String toString() {
          return "ontology: " + getName();
        }
    }
}
    
