package org.biojava.ontology;

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
    "org.biojava.ontology.ReasoningDomain",
    "ONTOLOGIES" );

  public static final ChangeType TERMS = new ChangeType(
    "Terms in ontologies in this domain are changing",
    "org.biojava.ontology.ReasoningDomain",
    "TERMS" );

  public static final ChangeType ADD_ONTOLOGY = new ChangeType(
    "Adding an ontology to this domain",
    "org.biojava.ontology.ReasoningDomain",
    "ADD_ONTOLOGY",
    ONTOLOGIES );

  public static final ChangeType REMOVE_ONTOLOGY = new ChangeType(
    "Removing an ontology to this domain",
    "org.biojava.ontology.ReasoningDomain",
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
   * <p>Imported terms should be handled using the seccond clause. If x, y or S
   * are imported versions of a, b or R respectively, then the proposition
   * holds.</p>
   *
   * @for.developer
   * <p>There are four types of relation that allow you to explore solutions
   * involving a range of related terms for a given relation.</p>
   *
   * <table>
   * <tr><td>symmetric</td><td>i R j implies j R i</td></tr>
   * <tr><td>antisymmetric</td><td>i R j and j R i implies i = j</td></tr>
   * <tr><td>transitive</td><td>i R j and j R k implies i R k</td></tr>
   * <tr><td>reflexive</td><td>i R i</td></tr>
   * </table>
   *
   * @for.developer
   * <p>It is important to treat the relations themselves as things to be
   * reasoned over. Here is a simple example.</p>
   *
   * <pre>
   * relations X, Y;
   * ( X is-a Y and a X b ) implies ( a Y b )
   * </pre>
   *
   * <p>To be fully functional, though, it is necissary to work with inferred
   * relations.</p>
   *
   * @for.developer
   * <p>One flip that may be usefull is the following inference:</p>
   *
   * <pre>
   * (a => b) <=> (!b => !a)
   * </pre>
   *
   * <p>This lets you make inferences about a statement based upon the falseness
   * of it's implications.</p>
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
   * Get all terms by name in the domain.
   *
   * @param name  the name of the term
   * @return      a Set containing all terms with local names that match
   */
  public Set getTerms(String name);

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
    private boolean debug = false;

    /**
     * Working values for relations.
     *
     * Relation->true means we can prove that the relation holds
     * Relation->false means that we can not prove that the relation holds
     * Relation->null means that we are in the process of proving things
     */
    private Map knownTrue;

    private int level = 0;

    private void upLevel() {
        ++level;
    }

    private void downLevel() {
        --level;
    }

    private void println(String s) {
        if (debug) {
            for (int i = 0; i < level; ++i) {
                System.err.print('\t');
            }
            System.err.println(s);
        }
    }

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
        doAdd(onto, Collections.singleton(onto));
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
      if(!allOntologies.containsKey(onto)) {
        return;
      }

      Set all = recSearch(onto, true);

      if(all.size() == 0) {
        doRemove(onto, Collections.singleton(onto));
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
      throws InvalidTermException
    {
      Relation rel = new Relation(subject, object, relation);

      println("isTrue(" + rel + ")");
      upLevel();
      boolean result = _isTrue(subject, object, relation, rel);
      downLevel();
      return result;
    }


    private boolean _isTrue(Term subject, Term object, Term relation, Relation rel)
      throws InvalidTermException
    {
      // see if we know anything about this one
      println("Check proven facts: " + rel);
      if(knownTrue.containsKey(rel)) {
        Boolean val = (Boolean) knownTrue.get(rel);
        if(val == Boolean.TRUE) {
          println("We already know this is true: " + rel);
          return true;
        } else {
          println("We already know this is false, or unproven: " + rel);
          return false;
        }
      }

      // we're working on it...
      knownTrue.put(rel, null);

      // for every Term x, x IS_A x
      println("Check for self-isa: " + rel);
      if(subject == object && relation == OntoTools.IS_A) {
        knownTrue.put(rel, Boolean.TRUE);
        println("For every term x, x IS_A x holds");
        return true;
      }

      // true(a, b, R)  = (a, b, R) member_of triples
      //               or (x, y, S) implies (a, b, R) and true(x, y, S)

      // do (a, b, R) member_of triples
      println("Check for direct support: " + rel);
      if(subject.getOntology() == object.getOntology() &&
         subject.getOntology() == relation.getOntology() &&
         subject.getOntology().containsTriple(subject, object, relation)
      ) {
        knownTrue.put(rel, Boolean.TRUE);
        println("Directly supported by the ontology: " + rel);
        return true;
      }

      // do (x, y, S) implies (a, b, R) and true(x, y, S)
      //

      // 1st case - implicit identity due to imports

      // subject import
      println("Check for subject imports");
      for(Iterator ti = findIdentities(subject).iterator(); ti.hasNext(); ) {
        Term t = (Term) ti.next();
        println("Trying identity of the subject: " + t + ", " + rel);
        if(isTrue(t, object, relation) )
        {
          knownTrue.put(rel, Boolean.TRUE);
          println("True of an identity of the subject: " + rel);
          return true;
        }
      }
      // object import
      println("Check for object imports");
      for(Iterator ti = findIdentities(object).iterator(); ti.hasNext(); ) {
        Term t = (Term) ti.next();
        println("Trying identity of the object: " + t + ", " + rel);
        if(isTrue(subject, t, relation) )
        {
          knownTrue.put(rel, Boolean.TRUE);
          println("True of an identity of the object: " + rel);
          return true;
        }
      }
      // relation import
      println("Check for relation imports");
      for(Iterator ti = findIdentities(relation).iterator(); ti.hasNext(); ) {
        Term t = (Term) ti.next();
        println("Trying identity of the relation: " + t + ", " + rel);
        if(isTrue(subject, object, t) )
        {
          knownTrue.put(rel, Boolean.TRUE);
          println("True of an identity of the relation: " + rel);
          return true;
        }
      }

      // checking for reflexive relation: i R i
      println("Checking for reflexive relation: " + rel);
      if(object.equals(subject) && isReflexive(relation)) {
        knownTrue.put(rel, Boolean.TRUE);
        println("Reflexive proposition true: " + rel);
        return true;
      }

      // checking for symmetric relation: a R b => b R a
      println("Checking for symmetric relation: " + rel);
      if(isSymmetric(relation)) {
        if(isTrue(object, subject, relation)) {
          knownTrue.put(rel, Boolean.TRUE);
          println("Symmetric proposition true: " + rel);
          return true;
        }
      }

      // checking for transitive relation: x R y && y R z => x R z
      // we have a potential x & z - search for a suitable y.
      println("Checking for transitive relation: " + rel);
      if(isTransitive(relation)) {
        // this brute-force search should be replaced by something more
        // optimised
        for(Iterator ti = getAllTerms().iterator(); ti.hasNext(); ) {
          Term t = (Term) ti.next();
          println("Checking transitive possibility for: " + t + ", " + rel);
          if(isTrue(subject, t, relation) &&
             isTrue(t, object, relation) )
          {
            knownTrue.put(rel, Boolean.TRUE);
            println("Transitive proposition true: " + rel);
            return true;
          }
        }
      }

      // Special case handling for HAS_A.  This should actually cover
      // some other types of relation, but I can't remember the generic
      // term for things that follow this pattern.  Just playing
      // around for now --thomasd.

      if (isTrue(relation, OntoTools.HAS_A, OntoTools.IS_A)) {
          println("Special case for HAS_A.  Checking through transitive closure (slowly)");
          for (Iterator ti = getAllTerms().iterator(); ti.hasNext(); ) {
              Term t = (Term) ti.next();
              if (isTrue(subject, t, OntoTools.IS_A) && isTrue(t, object, relation)) {
                  return true;
              }
              if (isTrue(t, object, OntoTools.IS_A) && isTrue(subject, t, relation)) {
                  return true;
              }
              if (isTrue(t, relation, OntoTools.IS_A) && isTrue(subject, object, t)) {
                  return true;
              }
          }
      }

      // not able to prove this proposition.
      println("Unable to prove: " + rel);
      knownTrue.put(rel, Boolean.FALSE);
      return false;
    }

    private Set recSearch(Ontology onto, boolean toRemove) {
      Set res = new HashSet();
      recSearchImpl(res, onto, toRemove);
      return res;
    }

    private void recSearchImpl(Set res, Ontology onto, boolean toRemove) {
      Set dependants = (Set) allOntologies.get(onto);
      if(toRemove && dependants != null && dependants.size() == 1) {
        res.add(onto);
      } else if(!toRemove && dependants == null) {
        res.add(onto);
      }

      for(Iterator i = onto.getTerms().iterator(); i.hasNext(); ) {
        Term t = (Term) i.next();
        if(t instanceof RemoteTerm) {
          RemoteTerm rt = (RemoteTerm) t;
          Ontology ro = rt.getRemoteTerm().getOntology();
          recSearchImpl(res, ro, toRemove);
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
    private Set findParents(Term term) {
      Set parents = null;

      for(Iterator tripI = term.getOntology().getTriples(term, null, OntoTools.IS_A).iterator();
          tripI.hasNext(); )
      {
        Triple trip = (Triple) tripI.next();
        if(parents == null) {
          parents = new HashSet();
        }
        parents.add(trip.getObject());
      }

      if(parents == null) {
        return Collections.EMPTY_SET;
      } else {
        return parents;
      }
    }

    // fixme: shouldn't be throwing this - should be an assertion failure
    private Set findChildren(Term term) {
      Set children = null;

      for(Iterator tripI = term.getOntology().getTriples(null, term, OntoTools.IS_A).iterator();
          tripI.hasNext(); )
      {
        Triple trip = (Triple) tripI.next();
        if(children == null) {
          children = new HashSet();
        }
        children.add(trip.getObject());
      }

      if(children == null) {
        return Collections.EMPTY_SET;
      } else {
        return children;
      }
    }

    private boolean isReflexive(Term relation)
    throws InvalidTermException {
      if(relation == OntoTools.IS_A) {
        return true;
      } else {
        return isa(relation, OntoTools.REFLEXIVE);
      }
    }

    private boolean isSymmetric(Term relation)
    throws InvalidTermException {
      return isa(relation, OntoTools.SYMMETRIC);
    }

    private boolean isTransitive(Term relation)
    throws InvalidTermException {
      if(relation == OntoTools.IS_A) {
        return true;
      } else {
        return isa(relation, OntoTools.TRANSITIVE);
      }
    }

    private Set getAllTerms() {
      Set all = new HashSet();
      for(Iterator i = allOntologies.keySet().iterator(); i.hasNext(); ) {
        Ontology onto = (Ontology) i.next();
        all.addAll(onto.getTerms());
      }
      return all;
    }

    private boolean isa(Term subject, Term object)
    throws InvalidTermException {
      return isTrue(subject, object, OntoTools.IS_A);
    }

    public Set getTerms(String name) {
      Set hits = new SmallSet();

      for(Iterator i = getOntologies().iterator(); i.hasNext(); ) {
        Ontology o = (Ontology) i.next();
        if(o.containsTerm(name)) {
          hits.add(o.getTerm(name));
        }
      }

      return hits;
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

    public String toString() {
      return
        "Relation [subject: " + getSubject()
        + ", object: " + getObject()
        + ", relation: " + getRelation()
        + "]";
    }
  }
}
