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
   *               or true(x, y, S) implies true(a, b, R) and true(x, y, S)
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
   * of it's implications. Similarly, </p>
   *
   * <pre>
   * (a => b) <=> (b or !a)
   * </pre>
   *
   * <p>This lets you make inferences about a based upon the truth of b.</p>
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
  public Iterator getMatching(Term subject, Term object, Term relation)
  throws InvalidTermException;

  /**
   * Get all ontologies by name within the domain.
   *
   * @param name  the name of the ontology
   * @return      an Ontology with the matching name
   */
  public Ontology getOntology(String name);

  /**
   * Get all terms by name in the domain.
   *
   * @param name  the name of the term
   * @return      a Set containing all terms with local names that match
   */
  public Set getTerms(String name);

  public Variable createVariable(String name);

  /**
   * An implementation of ReasoningDomain.
   *
   * @author Matthew Pocock
   */
  public class Impl
  extends AbstractChangeable
  implements ReasoningDomain
  {
    private final Set explicitOntologies;
    private final Map allOntologies;

    /**
     * Working values for relations.
     *
     * Relation->true means we can prove that the relation holds
     * Relation->false means that we can not prove that the relation holds
     * Relation->null means that we are in the process of proving things
     */
    private final Map resultCache;
    //private final Map proofs;
    private final Ontology scratchOnto;

    public Impl() {
      try {
        explicitOntologies = new HashSet();
        allOntologies = new HashMap();
        resultCache = new HashMap();
        //proofs = new HashMap();
        scratchOnto = OntoTools.getDefaultFactory().createOntology(
                "Scratch",
                "Ontology for temporary terms in " + this.toString());
      } catch (OntologyException oe) {
        throw new AssertionFailure(
                "Problem making our scratch space ontology",
                oe);
      }
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

      for(Iterator i = onto.getOps().getRemoteTerms().iterator(); i.hasNext(); ) {
        RemoteTerm rt = (RemoteTerm) i.next();
        Ontology ro = rt.getRemoteTerm().getOntology();
        recSearchImpl(res, ro, toRemove);
      }
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

    public Ontology getOntology(String name)
    {
      for(Iterator i = getOntologies().iterator(); i.hasNext();) {
        Ontology o = (Ontology) i.next();
        if(o.getName().equals(name)) {
          return o;
        }
      }

      throw new NoSuchElementException(
              "Could not find ontology with name: " + name +
              " in reasoning domain: " + this);
    }

    public Variable createVariable(String name) {
      return new Variable.Impl(scratchOnto, name, "");
    }

    public Iterator getMatching(Term subject, Term object, Term relation)
            throws InvalidTermException
    {
      Triple virtualTerm = createVirtualTerm(subject, object, relation);
      System.err.println("Getting all matches for: " + virtualTerm);
      Set propTerms = new HashSet();
      extractTerms(virtualTerm, propTerms);
      return new MatchIterator(virtualTerm,
                               new ExpressionIterator(propTerms));
    }

    void extractTerms(Term term, Set terms) {
      terms.add(term);
      if(term instanceof Triple) {
        Triple trip = (Triple) term;
        extractTerms(trip.getSubject(),  terms);
        extractTerms(trip.getObject(),   terms);
        extractTerms(trip.getRelation(), terms);
      }
    }

    Set getTerms() {
      MergingSet res = new MergingSet();

      for(Iterator i = getOntologies().iterator(); i.hasNext(); ) {
        Ontology o = (Ontology) i.next();
        res.addSet(o.getTerms());
      }

      return res;
    }

    Set getTriples() {
      MergingSet res = new MergingSet();

      for (Iterator i = getOntologies().iterator(); i.hasNext();) {
        Ontology o = (Ontology) i.next();
        res.addSet(o.getTriples(null, null, null));
      }

      return res;
    }

    Set getAxioms() {
      Set axioms = new HashSet(getTriples());

      for(Iterator i = new HashSet(axioms).iterator(); i.hasNext(); ) {
        Term t = (Term) i.next();
        if(t instanceof Triple) {
          Triple trip = (Triple) t;
          axioms.remove(trip.getSubject());
          axioms.remove(trip.getObject());
          axioms.remove(trip.getRelation());
        }
      }

      return axioms;
    }

    Triple createVirtualTerm(Term subject, Term object, Term relation) {
      try {
        if(subject.getOntology() != scratchOnto)
          subject = scratchOnto.importTerm(subject, null);
        if(object.getOntology() != scratchOnto)
          object = scratchOnto.importTerm(object, null);
        if(relation.getOntology() != scratchOnto)
          relation = scratchOnto.importTerm(relation, null);
      } catch (ChangeVetoException e) {
        throw new AssertionFailure(e);
      }

      return new Triple.Impl(subject, object, relation);
    }

    boolean areTriplesEqual(Triple a, Triple b)
    {
      if(a == b)
        return true;

      if(!areTermsEqual(a.getRelation(), b.getRelation()))
        return false;

      if(!areTermsEqual(a.getSubject(), b.getSubject()))
        return false;

      if(!areTermsEqual(a.getObject(), a.getObject()))
        return false;

      return true;
    }

    boolean areTermsEqual(Term a, Term b)
    {
      a = resolveRemote(a);
      b = resolveRemote(b);

      if(a == b)
        return true;

      if(a instanceof Triple & b instanceof Triple)
        return areTriplesEqual((Triple) a, (Triple) b);

      return false;
    }

    boolean evaluateExpression(Term expr)
    {
      if(expr instanceof Triple) expr = evaluateTriple((Triple) expr);

      if(areTermsEqual(expr, OntoTools.TRUE)) {
        return true;
      } else {
        return false;
      }
    }

    Term evaluateTriple(Triple expr)
    {
      //System.err.println("evaluateTriple: Evaluating: " + expr);

      // see if we already know the answer
      if(resultCache.keySet().contains(expr)) {
        Term res = (Term) resultCache.get(expr);
        if(res == null) // needed but unproven facts are considered false
          res = OntoTools.FALSE;
        //System.err.println("evaluateTriple: Known result: " + res);
        return res;
      }

      for(Iterator i = getAxioms().iterator();
          i.hasNext(); ) {
        Term t = (Term) i.next();
        if(areTermsEqual(t, expr)) {
          //System.err.println("evaluateTriple: this is an axiom, so is true");
          return OntoTools.TRUE;
        }
      }

      // we need to do some real evaluation - first resolve our terms
      Term sub = resolveRemote(expr.getSubject());
      Term obj = resolveRemote(expr.getObject());
      Term rel = resolveRemote(expr.getRelation());


      // let's evaluate the 3 components
      if(sub instanceof Triple) sub = evaluateTriple((Triple) sub);
      if(obj instanceof Triple) obj = evaluateTriple((Triple) obj);

      // bit hairy here - doing some funkey recursive shit
      /*try {
        Iterator mi = getMatching(sub, obj, rel);
        Term value = (mi.hasNext()) ? OntoTools.TRUE : OntoTools.FALSE;
        resultCache.put(expr, value);
        //System.err.println("evaluateTriple: Evaluated: " + expr + " to " + value);
        return value;
      } catch (InvalidTermException ie) {
        throw new AssertionFailure(ie);
      }*/
      return OntoTools.FALSE;
    }

    Term resolveRemote(Term t)
    {
      while(t instanceof RemoteTerm) {
        t = ((RemoteTerm) t).getRemoteTerm();
      }

      return t;
    }

    Term substitute(Term expr, Term origVal, Term newVal)
    {
      //System.err.println("substitute: expression: " + expr + " origVal: " + origVal + " newVal: " + newVal);
      if(areTermsEqual(expr, origVal)) {
        //System.err.println("substitute: expression and origVal are equal");
        return newVal;
      }

      if(expr instanceof Triple) {
        Triple trip = (Triple) expr;
        Term res = createVirtualTerm(substitute(trip.getSubject(), origVal, newVal),
                                     substitute(trip.getObject(), origVal, newVal),
                                     substitute(trip.getRelation(), origVal, newVal));
        //System.err.println("substitute: transformed " + expr + " into " + res);
        return res;
      }
      //System.err.println("substitute: not changed " + expr);
      return expr;
    }

    Variable findFirstVariable(Term term) {
      term = resolveRemote(term);

      if(term instanceof Variable)
        return (Variable) term;

      if(term instanceof Triple) {
        Triple trip = (Triple) term;

        Variable r;

        r = findFirstVariable(trip.getRelation());
        if(r != null) return r;

        r = findFirstVariable(trip.getSubject());
        if(r != null) return r;

        r = findFirstVariable(trip.getObject());
        if(r != null) return r;
      }

      return null;
    }

    Set findValues(Term term, Variable var) {
      Set values = new HashSet(getTerms());
      //System.err.println("findValues: number of values: " + values.size());

      for(Iterator i = values.iterator(); i.hasNext(); ) {
        Term t = (Term) i.next();
        Term rt = resolveRemote(t);
        if(rt instanceof Triple) {
          i.remove();
        }

        if(rt instanceof Variable) {
          i.remove();
        }

        if(rt.getName().startsWith("list:")) {
          i.remove();
        }
      }
      //System.err.println("findValues: reduced to: " + values.size());

      return values;
    }

    private void populateMembership(Term term, Variable var, Set membership) {
      if(term instanceof Triple) {
        Triple trip = (Triple) term;
        Term sub = trip.getSubject();
        Term obj = trip.getObject();
        Term rel = trip.getRelation();

        if(resolveRemote(sub) == var) {
          findDomain(rel, membership);
        } else {
          populateMembership(sub, var, membership);
        }

        if(resolveRemote(obj) == var) {
          findCodomain(rel, membership);
        } else {
          populateMembership(obj, var, membership);
        }

        if(resolveRemote(rel) == OntoTools.INSTANCEOF) {
          membership.add(obj);
        }
      }
    }

    private void findDomain(Term term, Set membrs) {
      // dumb search - probably wrong
      for(Iterator i = getOntologies().iterator(); i.hasNext(); ) {
        Ontology onto = (Ontology) i.next();
        Set domains = onto.getTriples(term, null, OntoTools.DOMAIN);
        for(Iterator di = domains.iterator(); di.hasNext(); ) {
          Triple trip = (Triple) di.next();
          membrs.add(trip.getObject());
        }
      }
    }

    private void findCodomain(Term term, Set membrs)
    {
      // dumb search - probably wrong
      for(Iterator i = getOntologies().iterator(); i.hasNext();) {
        Ontology onto = (Ontology) i.next();
        Set domains = onto.getTriples(term, null, OntoTools.CO_COMAIN);
        for(Iterator di = domains.iterator(); di.hasNext();) {
          Triple trip = (Triple) di.next();
          membrs.add(trip.getObject());
        }
      }
    }

    private static int depth = 0;
    private static int tries = 0;

    /**
     * An Iterator that implements the state-machine that matches a given
     * relation against all the things proveable by the ontology.
     *
     * @author Matthew Pocock
     */
    final class MatchIterator
            implements Iterator {
      private final Iterator expI;
      private final Triple proposition;
      private Triple nextResult;

      public MatchIterator(Triple proposition, Iterator expI)
      {
        depth++;
        this.proposition = proposition;
        this.expI = expI;
        nextResult = findNextMatch();
        System.err.println("Created: " + this + ": " + depth);
        if(depth > 5) throw new Error("Depth exceeded: " + depth);
      }

      public Object next() {
        Triple res = nextResult;
        nextResult = findNextMatch();
        return res;
      }

      public boolean hasNext() {
        return nextResult != null;
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }

      public String toString() {
        return "MatchIterator for: " + proposition;
      }

      Triple findNextMatch() {
        //System.err.println("findNextMatch in " + this.toString());
        while(expI.hasNext()) {
          tries++;
          if(tries > 600000) throw new Error("Tried to many times");

          Triple next = (Triple) expI.next();

          System.err.println("findNextMatch: Evaluating: " + next);

          Term ifTrue = substitute(next, proposition, OntoTools.TRUE);
          if(evaluateExpression(ifTrue) == false) continue;

          Term ifFalse = substitute(next, proposition, OntoTools.FALSE);
          if(evaluateExpression(ifFalse) == true) continue;

          //System.err.println("findNextMatch: Accepted: " + next);

          return next;
        }

        //System.err.println("findNextMatch: Found no matches");
        depth--;
        return null;
      }
    }

    final class ExpressionIterator
    implements Iterator {
      private final Iterator axI;
      private final Stack stack;
      private final Set propTerms;

      private Triple nextExpr;

      ExpressionIterator(Set propTerms) {
        this.axI = getAxioms().iterator();
        this.stack = new Stack();
        this.propTerms = propTerms;

        nextExpr = findNext();
      }

      public boolean hasNext()
      {
        return nextExpr != null;
      }

      public Object next()
      {
        Triple val = nextExpr;
        nextExpr = findNext();
        return val;
      }

      public void remove()
      {
        throw new UnsupportedOperationException();
      }

      Triple findNext() {
        //System.err.println("findNext: Finding next term to evaluate");

        if(stack.size() == 0) {
          //System.err.println("findNext: Nothing left in subs");
          while(true) {
            if(!axI.hasNext()) {
              //System.err.println("findNext: No more axioms");
              return null;
            }

            Triple expr = (Triple) axI.next();
            //System.err.println("findNext: Testing next axiom: " + expr);
            Variable var = findFirstVariable(expr);
            if(var == null) {
              //System.err.println("findNext: Returning variable-less axiom");
              return expr;         // this axiom had no variables
            }

            //System.err.println("findNext: First variable is: " + var);

            Set vals = findValues(expr, var);
            vals.addAll(propTerms);

            // got one
            OptionSearcher subs = new OptionSearcher(expr, var, vals.iterator());
            //System.err.println("findNext: Created new option searcher: " + subs);
            stack.push(subs);
            //System.err.println("findNext: Pushing a new frame: " + stack);
            break;
          }
        }

        OptionSearcher subs = (OptionSearcher) stack.peek();

        while(true) {
          if(!subs.hasNext()) {
            stack.pop();
            //System.err.println("findNext: Run out of options: " + stack);
            if(stack.isEmpty()) {
              //System.err.println("findNext: Empty stack - out of options");
              return findNext();
            }

            subs = (OptionSearcher) stack.peek();
            continue;
          }

          Triple expr = (Triple) subs.next();

          //System.err.println("findNext: expanding expression: " + expr);
          Variable var = findFirstVariable(expr);
          //System.err.println("findNext: First variable: " + var);
          if(var == null) return expr;        // this contains no variables

          Set vals = findValues(expr, var);
          vals.addAll(propTerms);

          subs = new OptionSearcher(expr, var, vals.iterator());
          //System.err.println("findNext: Creating new option searcher: " + subs + " " + vals);
          stack.push(subs);
          //System.err.println("findNext: Pushing a new frame: " + stack);
          if(stack.size() > 10) throw new Error("Stack too deep");
        }
      }
    }

    final class OptionSearcher
            implements Iterator
    {
      private final Term expr;
      private final Term var;
      private final Iterator vals;

      public OptionSearcher() {
        expr = null;
        var = null;
        vals = Collections.EMPTY_SET.iterator();
      }

      public OptionSearcher(Term expr, Term var, Iterator vals) {
        this.expr = expr;
        this.var = var;
        this.vals = vals;
      }

      public Term getExpr() {
        return expr;
      }

      public Term getVar() {
        return var;
      }

      public boolean hasNext()
      {
        return vals.hasNext();
      }

      public Object next()
      {
        Term newVal = (Term) vals.next();
        //System.err.println("OptionSearcher.newVal(): Binding " + getVar() + " to " + newVal);
        return substitute(expr, var, newVal);
      }

      public void remove()
      {
        throw new UnsupportedOperationException();
      }

      public String toString()
      {
        return "@" + hashCode() + getVar();
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

    public Relation(Triple trip) {
      this.subject = trip.getSubject();
      this.object = trip.getObject();
      this.relation = trip.getRelation();
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
