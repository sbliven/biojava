package org.biojava.bio.ontology;

import java.util.*;

import org.biojava.utils.*;

/**
 * A domain over which we are reasoning.
 *
 * <p>
 * BioJava ontologies are namespaces within which facts are deposited. When we
 * reason over these facts, we typically need to reason over multiple
 * namespaces. If one ontology imports terms from another, then the domain
 * over which we must reason implicitly must include both that ontology, and
 * the one it imports from.
 * </p>
 *
 * <p>Two domains are equivalent if they are over the same ontologies at the
 * same instant in time.</p>
 *
 * @author Matthew Pocock
 * @since 1.4
 */
public interface ReasoningDomain
extends Changeable {
  public static final ChangeType ONTOLOGIES = new ChangeType(
    "Set of ontologies in this domain are changing",
    "org.biojava.bio.ontology.ReasoningDomain",
    "ONTOLOGIES" );
  
  public static final ChangeType TERMS = new ChangeType(
    "Terms in ontologies in this domain are changing",
    "org.biojava.bio.ontology.ReasoningDomain",
    "TERMS" );
  
  public static final ChangeType ADD_ONTOLOGY = new ChangeType(
    "Adding an ontology to this domain",
    "org.biojava.bio.ontology.ReasoningDomain",
    "ADD_ONTOLOGY",
    ONTOLOGIES );

  public static final ChangeType REMOVE_ONTOLOGY = new ChangeType(
    "Removing an ontology to this domain",
    "org.biojava.bio.ontology.ReasoningDomain",
    "REMOVE_ONTOLOGY",
    ONTOLOGIES );

  /**
   * Get back the complete set of ontologies contributing to this domain.
   *
   * @for.powerUser
   * Don't make any assumptions about the mutability of the set returned. If
   * you need a persistent copy of the ontologies at an instant in time, lock
   * the ReasoningDomain with an AlwaysVeto, and make a new set from the return
   * result of this method. e.g.
   * <code>Set ontsNow = new HashSet(rDomain.getOntologies());</code>
   *
   * @for.developer
   * To avoid people mucking about with your state, either return a new
   * <code>Set</code> instance each time, or return an unmodifiable view.
   */
  public Set getOntologies();
  
  /**
   * Add an ontology to this domain.
   *
   * <p>Adding an ontology will result in all of the ontologies it imports terms
   * from also being added. Change events will be raised for each ontology
   * added. If any of the dependant ontologies can not be added, then none are
   * added.</p>
   *
   * <p>Adding an already present ontology results in no error, and no
   * events.</p>
   *
   * @for.developer
   * Be sure to fire off all pre-change events for all ontologies (the
   * explicitly added one, and all dependants, and their dependants) before
   * fireing any postChange notifications.
   * It is unlikely that adding dependancies will cause vetoes to be raised.
   * However, since you can not control the listeners registered with your
   * domain, you should make sure you uphold the contract for addOntology.
   *
   * @param onto  the Ontology to add
   * @throws ChangeVetoException  if onto could not be added
   */
  public void addOntology(Ontology onto)
  throws ChangeVetoException;
  
  /**
   * Remove an ontology from this domain.
   *
   * <p>Removing an ontology should remove that ontology from getOntologies(),
   * but should only remove the dependant ontologies that are not needed for
   * any other ontologies previously added.</p>
   *
   * <p>Removing an absent ontology results in no error, and no events.</p>
   *
   * @for.developer
   * Be sure to fire off all pre-change events for all ontologies being removed.
   *
   * @for.developer
   * It may be worth internaly maintaining a list of those ontologies explicitly
   * added, and a map from all ontologies to these if they were imported in
   * response to adding the explicit ontology. This will make it much easier to
   * remove the correct sub-set of ontologies.
   *
   * @param onto  the Ontology to remove
   * @throws ChangeVetoException  if onto could not be removed
   */
  public void removeOntology(Ontology onto)
  throws ChangeVetoException;
  
  /**
   * Decide if two terms are linked by a given relation.
   *
   * <p>This method must use all information within this domain, and no
   * information not present in this domain to see if a relationship of the
   * given type holds between these two terms.</p>
   *
   * <p>This is equivalent to asserting that:</p>
   *
   * <pre>
   * true(a, b, R)  = (a, b, R) member_of triples
   *               or (x, y, S) implies (a, b, R) and true(x, y, S)
   * </pre>
   *
   * <p>The first clause is equivalent to scanning the ontologies for a triple
   * with exactly the right terms in this domain. The seccond clause states that
   * this relation holds if there is any other relation that would imply it, and
   * that relation holds.</p>
   *
   * <p>The first special case of implication is identity. If two terms are
   * identical, then anything that is true for one is also true of the other.
   * the most common case of this is when terms are imported. The imported term
   * and the original term have an implicit identity relationship.</p>
   *
   * <p>The seccond special case of implication is via inheritance. In particular, if
   * the relationship holds for any parent terms of the subject or object, and
   * and child terms of the relation, then it holds for that triple. For
   * example, if you have stated that humans are mamals, and that mamals have
   * eyes, then we know that humans have eyes. Likewise, if we know that Jane
   * has a father Mark, then we know that Jane has a parent Mark, as father is
   * a sub-type of parent.</p>
   *
   * @for.developer
   * It is probably worth caching inheritance and identity information
   * internally. This will let you optimize for the special cases of implication
   * without too much bother.
   *
   * @for.developer
   * You will almost certainly want to maintain some cache of things you have
   * proved so far. This will both help in performance, and also make any
   * reasoning recursions global to the ReasoningDomain instance it is acting
   * upon.
   *
   * @param subject  the subject Term
   * @param object   the object Term
   * @param relation the relation term
   * @return true if this relationship can be inferred within this domain
   * @throws InvalidTermException  if the relation term is not a relation
   */
  public boolean isTrue(Term subject, Term object, Term relation)
  throws InvalidTermException;
  
  /**
   * An implementation of ReasoningDomain.
   *
   * @author Matthew Pocock
   */
  public class Impl
  extends AbstractChangeable
  implements ReasoningDomain
  {
    private Set explicitOntologies;
    private Map allOntologies;
    
    /**
     * Working values for relations.
     *
     * Relation->true means we can prove that the relation holds
     * Relation->false means that we can not prove that the relation holds
     * Relation->null means taht we are in the process of proving things
     */
    private Map knownTrue;
    
    public Impl() {
      explicitOntologies = new HashSet();
      allOntologies = new HashMap();
      knownTrue = new HashMap();
    }
    
    public Set getOntologies() {
      return Collections.unmodifiableSet(allOntologies.keySet());
    }
    
    public void addOntology(Ontology onto)
    throws ChangeVetoException {
      Set all = recSearch(onto, false);
      
      if(all.size() == 0) {
        return;
      }
      
      if(hasListeners()) {
        ChangeSupport cs = getChangeSupport(ReasoningDomain.ADD_ONTOLOGY);
        synchronized(cs) {
          for(Iterator i = all.iterator(); i.hasNext(); ) {
            Ontology o = (Ontology) i.next();
            cs.firePreChangeEvent(new ChangeEvent(
              this,
              ReasoningDomain.ADD_ONTOLOGY,
              o ));
          }
          doAdd(onto, all);
          for(Iterator i = all.iterator(); i.hasNext(); ) {
            Ontology o = (Ontology) i.next();
            cs.firePostChangeEvent(new ChangeEvent(
              this,
              ReasoningDomain.ADD_ONTOLOGY,
              o ));
          }
        }
      } else {
        doAdd(onto, all);
      }
    }
    
    private void doAdd(Ontology onto, Set all) {
      explicitOntologies.add(onto);
      for(Iterator i = all.iterator(); i.hasNext(); ) {
        Ontology o = (Ontology) i.next();
        Set os = (Set) allOntologies.get(o);
        if(os == null) {
          allOntologies.put(o, os = new HashSet());
        }
        os.add(onto);
      }
    }
    
    public void removeOntology(Ontology onto)
    throws ChangeVetoException {
      Set all = recSearch(onto, true);
      
      if(all.size() == 0) {
        return;
      }
      
      if(hasListeners()) {
        ChangeSupport cs = getChangeSupport(ReasoningDomain.REMOVE_ONTOLOGY);
        synchronized(cs) {
          for(Iterator i = all.iterator(); i.hasNext(); ) {
            Ontology o = (Ontology) i.next();
            cs.firePreChangeEvent(new ChangeEvent(
              this,
              ReasoningDomain.REMOVE_ONTOLOGY,
              o ));
          }
          doRemove(onto, all);
          for(Iterator i = all.iterator(); i.hasNext(); ) {
            Ontology o = (Ontology) i.next();
            cs.firePostChangeEvent(new ChangeEvent(
              this,
              ReasoningDomain.REMOVE_ONTOLOGY,
              o ));
          }
        }
      } else {
        doRemove(onto, all);
      }
    }
    
    private void doRemove(Ontology onto, Set all) {
      explicitOntologies.remove(onto);
      for(Iterator i = all.iterator(); i.hasNext(); ) {
        Ontology o = (Ontology) i.next();
        Set os = (Set) allOntologies.get(o);
        os.remove(onto);
        if(os.size() == 0) {
          allOntologies.remove(o);
        }
      }
    }
    
    // fixme: we just assume that relation is a relation term for now
    public boolean isTrue(Term subject, Term object, Term relation)
    throws InvalidTermException {
      Relation rel = new Relation(subject, object, relation);
      
      // see if we know anything about this one
      if(knownTrue.containsKey(rel)) {
        Boolean val = (Boolean) knownTrue.get(rel);
        if(val == Boolean.TRUE) {
          return true;
        } else {
          return false;
        }
      }
      
      // we're working on it...
      knownTrue.put(rel, null);
      
      // true(a, b, R)  = (a, b, R) member_of triples
      //               or (x, y, S) implies (a, b, R) and true(x, y, S)
      
      // do (a, b, R) member_of triples
      if(subject.getOntology() == object.getOntology() &&
         subject.getOntology() == relation.getOntology() &&
         subject.getOntology().containsTriple(subject, object, relation)
      ) {
        knownTrue.put(rel, Boolean.TRUE);
        return true;
      }
      
      // do (x, y, S) implies (a, b, R) and true(x, y, S)
      //
      
      // 1st case - implicit identity due to imports
      
      // subject import
      for(Iterator ti = findIdentities(subject).iterator(); ti.hasNext(); ) {
        Term t = (Term) ti.next();
        if(isTrue(t, object, relation) )
        {
          knownTrue.put(rel, Boolean.TRUE);
          return true;
        }
      }
      // object import
      for(Iterator ti = findIdentities(object).iterator(); ti.hasNext(); ) {
        Term t = (Term) ti.next();
        if(isTrue(subject, t, relation) )
        {
          knownTrue.put(rel, Boolean.TRUE);
          return true;
        }
      }
      // relation import
      for(Iterator ti = findIdentities(relation).iterator(); ti.hasNext(); ) {
        Term t = (Term) ti.next();
        if(isTrue(subject, object, t) )
        {
          knownTrue.put(rel, Boolean.TRUE);
          return true;
        }
      }
      
      // 2nd case - walk inheritance tree
      
      // subject
      for(Iterator ti = findParents(subject).iterator(); ti.hasNext(); ) {
        Term t = (Term) ti.next();
        if(isTrue(t, object, relation))
        {
          knownTrue.put(rel, Boolean.TRUE);
          return true;
        }
      }
      // object
      for(Iterator ti = findParents(object).iterator(); ti.hasNext(); ) {
        Term t = (Term) ti.next();
        if(isTrue(subject, t, relation))
        {
          knownTrue.put(rel, Boolean.TRUE);
          return true;
        }
      }
      // relation
      for(Iterator ti = findChildren(relation).iterator(); ti.hasNext(); ) {
        Term t = (Term) ti.next();
        if(isTrue(subject, object, t))
        {
          knownTrue.put(rel, Boolean.TRUE);
          return true;
        }
      }
      
      // we should do the more esoteric implies here - this is what would make
      // it a full reasoning engine.
      
      knownTrue.put(rel, Boolean.FALSE);
      return false;
    }

    private Set recSearch(Ontology onto, boolean toRemove) {
      Set res = new HashSet();
      recSearchImpl(res, onto, toRemove);
      return res;
    }
    
    private void recSearchImpl(Set res, Ontology onto, boolean toRemove) {
      for(Iterator i = onto.getTerms().iterator(); i.hasNext(); ) {
        Term t = (Term) i.next();
        if(t instanceof RemoteTerm) {
          RemoteTerm rt = (RemoteTerm) t;
          Ontology ro = rt.getRemoteTerm().getOntology();
          if(res.contains(ro) == toRemove) {
            res.add(ro);
            recSearchImpl(res, ro, toRemove);
          }
        }
      }
    }
    
    private Set findIdentities(Term term) {
      Set identities = null;
      
      if(term instanceof RemoteTerm) {
        identities = new HashSet();
        identities.add(((RemoteTerm) term).getRemoteTerm());
      }
      
      for(Iterator oi = allOntologies.keySet().iterator(); oi.hasNext(); ) {
        Ontology o = (Ontology) oi.next();
        // we need an optimization for fetching all remote terms in an ontology
        for(Iterator ti = o.getTerms().iterator(); ti.hasNext(); ) {
          Term t = (Term) ti.next();
          if(t instanceof RemoteTerm) {
            RemoteTerm rt = (RemoteTerm) t;
            if(rt.getRemoteTerm() == term) {
              if(identities == null) {
                identities = new HashSet();
              }
              identities.add(rt);
            }
          }
        }
      }
      
      if(identities == null) {
        return Collections.EMPTY_SET;
      } else {
        return identities;
      }
    }
    
    // fixme: shouldn't be throwing this - should be an assertion failure
    private Set findParents(Term term)
    throws InvalidTermException {
      Set parents = null;
      
      for(Iterator tripI = term.getOntology().getTriples(term, null, null).iterator();
          tripI.hasNext(); )
      {
        Triple trip = (Triple) tripI.next();
        // nasty nasty way to prove this - optimization needed desperately
        if(isTrue(trip.getRelation(), OntoTools.IS_A, OntoTools.IS_A)) {
          if(parents == null) {
            parents = new HashSet();
          }
          parents.add(trip.getObject());
        }
      }
      
      if(parents == null) {
        return Collections.EMPTY_SET;
      } else {
        return parents;
      }
    }
    
    // fixme: shouldn't be throwing this - should be an assertion failure
    private Set findChildren(Term term)
    throws InvalidTermException {
      Set children = null;
      
      for(Iterator tripI = term.getOntology().getTriples(null, term, null).iterator();
          tripI.hasNext(); )
      {
        Triple trip = (Triple) tripI.next();
        // nasty nasty way to prove this - optimization needed desperately
        if(isTrue(trip.getRelation(), OntoTools.IS_A, OntoTools.IS_A)) {
          if(children == null) {
            children = new HashSet();
          }
          children.add(trip.getObject());
        }
      }
      
      if(children == null) {
        return Collections.EMPTY_SET;
      } else {
        return children;
      }
    }
  }
  
  /**
   * A relation together with a proven or not flag.
   * Hashcode & equals only take the terms into account. They ignore proven.
   *
   * @author Matthew Pocock
   * @since 1.4
   */
  final class Relation {
    private final Term subject;
    private final Term object;
    private final Term relation;
    
    public Relation(Term subject, Term object, Term relation) {
      this.subject = subject;
      this.object = object;
      this.relation = relation;
    }
    
    public Term getSubject()  { return subject; }
    public Term getObject()   { return object; }
    public Term getRelation() { return relation; }
    
    public boolean equals(Object o) {
      if(o instanceof Relation) {
        Relation that = (Relation) o;
        return
          this.getSubject().equals(that.getSubject()) &&
          this.getObject().equals(that.getObject()) &&
          this.getRelation().equals(that.getRelation());
      } else {
        return false;
      }
    }
    
    public int hashCode() {
      return getSubject().hashCode() +
        31 * getObject().hashCode() +
        31 * 31 * getRelation().hashCode();
    }
  }
}
