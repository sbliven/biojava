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
   * Decide if two terms are linked by a given predicate.
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
   * this predicate holds if there is any other predicate that would imply it, and
   * that predicate holds.</p>
   *
   * <p>Imported terms should be handled using the seccond clause. If x, y or S
   * are imported versions of a, b or R respectively, then the proposition
   * holds.</p>
   *
   * @for.developer
   * <p>There are four types of predicate that allow you to explore solutions
   * involving a range of related terms for a given predicate.</p>
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
   * @param predicate the predicate term
   * @return true if this relationship can be inferred within this domain
   * @throws InvalidTermException  if the predicate term is not a predicate
   */
  public Iterator getMatching(Term subject, Term object, Term predicate)
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

    //private final Map proofs;
    private final Ontology scratchOnto;

    public Impl() {
      try {
        explicitOntologies = new HashSet();
        allOntologies = new HashMap();
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

    public Iterator getMatching(Term subject, Term object, Term predicate)
            throws InvalidTermException
    {
      Triple virtualTerm = createVirtualTerm(subject, object, predicate, null, null);
      //System.out.println("Getting all matches for: " + virtualTerm);
      Set propTerms = new HashSet();
      //extractTerms(virtualTerm, propTerms);

      return new MatchIterator(virtualTerm,
                               getConstantAxioms(),
                               getVariableAxioms(),
                               propTerms);
    }

    void extractTerms(Term term, Set terms) {
      term = resolveRemote(term);

      if(term instanceof Triple) {
        Triple trip = (Triple) term;
        extractTerms(trip.getSubject(),  terms);
        extractTerms(trip.getObject(),   terms);
        extractTerms(trip.getPredicate(), terms);

        return;
      } else {
        if(term instanceof Variable) {
          return;
        }
      }

      terms.add(term);
    }

    void extractConstTerms(Set allTerms, Set constTerms) {
      for(Iterator i = allTerms.iterator(); i.hasNext(); ) {
        Triple trip = (Triple) i.next();
        if(findFirstVariable(trip) == null) {
          constTerms.add(trip);
        }
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

    private Set _axioms;

    Set getAxioms() {
      if(_axioms == null) {
        Set axioms = new HashSet(getTriples());

        for(Iterator i = new HashSet(axioms).iterator(); i.hasNext(); ) {
          Term t = (Term) i.next();
          if(t instanceof Triple) {
            Triple trip = (Triple) t;
            axioms.remove(trip.getSubject());
            axioms.remove(trip.getObject());
            axioms.remove(trip.getPredicate());
          }
        }

        _axioms = Collections.unmodifiableSet(axioms);
      }

      return _axioms;
    }

    private Set _constantAxioms;

    Set getConstantAxioms() {
      if(_constantAxioms == null) {
        Set constantAxioms = new HashSet(getAxioms());

        for(Iterator i = constantAxioms.iterator(); i.hasNext(); ) {
          Term t = (Term) i.next();

          if(findFirstVariable(t) != null) {
            i.remove();
          }
        }

        _constantAxioms = Collections.unmodifiableSet(constantAxioms);
      }

      return _constantAxioms;
    }

    private Set _variableAxioms;

    Set getVariableAxioms() {
      if(_variableAxioms == null) {
        Set variableAxioms = new HashSet(getAxioms());

        for(Iterator i = variableAxioms.iterator(); i.hasNext();) {
          Term t = (Term) i.next();

          if(findFirstVariable(t) == null) {
            i.remove();
          }
        }

        _variableAxioms = Collections.unmodifiableSet(variableAxioms);
      }

      return _variableAxioms;
    }

    Triple createVirtualTerm(Term subject, Term object, Term predicate,
                             String name, String description) {
      try {
        if(subject.getOntology() != scratchOnto)
          subject = scratchOnto.importTerm(subject, null);
        if(object.getOntology() != scratchOnto)
          object = scratchOnto.importTerm(object, null);
        if(predicate.getOntology() != scratchOnto)
          predicate = scratchOnto.importTerm(predicate, null);
      } catch (ChangeVetoException e) {
        throw new AssertionFailure(e);
      }

      return new Triple.Impl(subject, object, predicate, name, description);
    }

    boolean areTriplesEqual(Triple a, Triple b)
    {
      //System.out.println("areTriplesEqual:\n\t: " + a + "\n\t: " + b);

      if(a == b) {
        //System.out.println("areTriplesEqual: Identical refferences");
        return true;
      }

      if(!areTermsEqual(a.getPredicate(), b.getPredicate())) {
        //System.out.println("areTriplesEqual: different relations");
        return false;
      }

      if(!areTermsEqual(a.getSubject(), b.getSubject())) {
        //System.out.println("areTriplesEqual: different subjects");
        return false;
      }

      if(!areTermsEqual(a.getObject(), b.getObject())) {
        //System.out.println("areTriplesEqual: different objects");
        return false;
      }

      //System.out.println("Same subject, object and predicate");
      return true;
    }

    boolean areTermsEqual(Term a, Term b)
    {
      //System.out.println("areTermsEqual:\n\t: " + a + "\n\t: " + b);
      a = resolveRemote(a);
      b = resolveRemote(b);

      //System.out.println("areTermsEqual resolved:\n\t: " + a + "\n\t: " + b);

      if(a == b) {
        //System.out.println("areTermsEqual: Identical terms");
        return true;
      }

      if(a instanceof Triple & b instanceof Triple) {
        //System.out.println("areTermsEqual: Triples - calling areTriplesEqual");
        return areTriplesEqual((Triple) a, (Triple) b);
      }

      //System.out.println("Terms are not equal");
      return false;
    }

    boolean evaluateExpression(Term expr)
    {
      //System.out.println("evaluateExpression: " + expr);
      if(expr instanceof Triple) expr = evaluateTriple((Triple) expr);

      if(areTermsEqual(expr, OntoTools.TRUE)) {
        //System.out.println("evaluateExpression: true <- " + expr);
        return true;
      } else {
        //System.out.println("evaluateExpression: false <- " + expr);
        return false;
      }
    }

    /**
     * Working values for relations.
     *
     * predicate->true means we can prove that the predicate holds
     * predicate->false means that we can not prove that the predicate holds
     * predicate->null means that we are in the process of proving things
     */
    private Map resultCache = null;

    Map getResultCache() {
      if(resultCache == null) {
        resultCache = new HashMap();
      }

      return resultCache;
    }

    Term evaluateTriple(Triple expr)
    {
      //System.out.println("evaluateTriple: Evaluating: " + expr);

      // see if we already know the answer
      Map resultCache = getResultCache();
      //System.out.println("evaluateTriple: results so far: " + resultCache.keySet().size());
      if(resultCache.keySet().contains(expr)) {
        Term res = (Term) resultCache.get(expr);

        if(res == null) { // needed but unproven facts are considered false
          res = OntoTools.FALSE;
        }

        //System.out.println("evaluateTriple: Known result: " + res);
        return res;
      } else {
        //System.out.println("evaluateTriple: Never seen this before: " + expr.hashCode() + " " + expr);
      }

      resultCache.put(expr, null);

      for(Iterator i = getAxioms().iterator();
          i.hasNext(); ) {
        Term t = (Term) i.next();
        if(areTermsEqual(t, expr)) {
          //System.out.println("evaluateTriple: this is an axiom, so is true");
          resultCache.put(expr, OntoTools.TRUE);
          return OntoTools.TRUE;
        }
      }

      // we need to do some real evaluation - first resolve our terms
      Term sub = resolveRemote(expr.getSubject());
      Term obj = resolveRemote(expr.getObject());
      Term rel = resolveRemote(expr.getPredicate());


      // let's evaluate the 3 components
      if(sub instanceof Triple) sub = evaluateTriple((Triple) sub);
      if(obj instanceof Triple) obj = evaluateTriple((Triple) obj);

      // bit hairy here - doing some funkey recursive shit
      /*try {
        //System.out.println("evaluateTriple: Recursive call for: " + depth + " " + expr);
        Iterator mi = getMatching(sub, obj, rel);
        Term value = (mi.hasNext()) ? OntoTools.TRUE : OntoTools.FALSE;
        //System.out.println("evaluateTriple: Evaluated: " + expr + " to " + value);
        resultCache.put(expr, value);
        return value;
      } catch (InvalidTermException ie) {
        throw new AssertionFailure(ie);
      }*/

      //System.out.println("evaluateTriple: Unable to prove proposition");
      resultCache.put(expr, OntoTools.FALSE);
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
      //System.out.println("substitute: expression: " + expr + " origVal: " + origVal + " newVal: " + newVal);
      if(areTermsEqual(expr, origVal)) {
        //System.out.println("substitute: expression and origVal are equal");
        return newVal;
      }

      if(expr instanceof Triple) {
        Triple trip = (Triple) expr;
        Term res = createVirtualTerm(substitute(trip.getSubject(), origVal, newVal),
                                     substitute(trip.getObject(), origVal, newVal),
                                     substitute(trip.getPredicate(), origVal, newVal),
                                     null,
                                     null);
        //System.out.println("substitute: transformed " + expr + " into " + res);
        return res;
      }
      //System.out.println("substitute: not changed " + expr);
      return expr;
    }

    Variable findFirstVariable(Term term) {
      term = resolveRemote(term);

      if(term instanceof Variable)
        return (Variable) term;

      if(term instanceof Triple) {
        Triple trip = (Triple) term;

        Variable r;

        r = findFirstVariable(trip.getPredicate());
        if(r != null) return r;

        r = findFirstVariable(trip.getSubject());
        if(r != null) return r;

        r = findFirstVariable(trip.getObject());
        if(r != null) return r;
      }

      return null;
    }

    private Set _values;
    //private Map _knownVals;

    Set findValues(Term term, Variable var) {
      if(_values == null) {
        _values = new HashSet(getTerms());
        //_knownVals = new HashMap();

        for(Iterator i = _values.iterator(); i.hasNext(); ) {
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
      }

      //Set vals = (Set) _knownVals.get(var);
      Set vals = null;
      if(vals == null) {
        try {
          Set types = new HashSet();
          populateMembership(term, var, types);
          //System.out.println("Variable: " + var + " should be of types: " + types + "\n\t" + term);

          if(types.isEmpty()) {
            //System.out.println("Unable to work out the type of " + var + " in " + term);
            types.add(OntoTools.ANY);
          }

          Ontology ioC = getInstanceOfClosure();
          Term io = ioC.importTerm(OntoTools.INSTANCE_OF, null);

          HashSet values = new HashSet();
          Iterator typeI = types.iterator();
          Term type = ioC.importTerm((Term) typeI.next(), null);

          values.addAll(ioC.getTriples(null, type, io));
          //System.out.println("\tvalues: " + type + " " + values);
          while(typeI.hasNext()) {
            type = ioC.importTerm((Term) typeI.next(), null);
            values.retainAll(ioC.getTriples(null, type, io));
            //System.out.println("\tvalues: " + type + " " + values);
          }

          vals = new HashSet();
          for(Iterator i = values.iterator(); i.hasNext(); )  {
            Triple trip = (Triple) i.next();
            vals.add(trip.getSubject());
          }

          //_knownVals.put(var, vals);
          //System.out.println("Final values: " + vals.size() + " " + vals);
        } catch (ChangeVetoException e) {
          throw new AssertionFailure(e);
        }
      }

      return vals;
    }

    Map _closures = new HashMap();

    Ontology _subTypeOfClosure;

    Ontology getSubTypeOfClosure() {
      if(_subTypeOfClosure == null) {
        //System.out.println("Creating closure over SUB_TYPE_OF");
        try {
          _subTypeOfClosure = OntoTools.getDefaultFactory().createOntology(
                  "sub_type_of_closure",
                  "Closure over sub_type_of");
          Term sto = _subTypeOfClosure.importTerm(OntoTools.SUB_TYPE_OF, null);

          Set typeDecls = getTriples(null, OntoTools.TYPE, OntoTools.SUB_TYPE_OF);
          //System.out.println("Top Types: " + typeDecls);

          Set types = new HashSet();
          for(Iterator i = typeDecls.iterator(); i.hasNext(); ) {
            Triple trip = (Triple) i.next();
            types.add(trip.getSubject());
          }
          types.add(OntoTools.TYPE); // incase it gets missed
          //System.out.println("Main types: " + types);

          Set allTypes = new HashSet();
          // if we say x instance_of type, then x is a type
          for(Iterator ti = types.iterator(); ti.hasNext();) {
            Term type = (Term) ti.next();
            allTypes.addAll(getTriples(null, type, OntoTools.INSTANCE_OF));
          }
          // if we say x sub_type_of y, then x and y must be types
          allTypes.addAll(getTriples(null, null, OntoTools.SUB_TYPE_OF));

          //System.out.println("ALL types: " + allTypes);

          for(Iterator ti = allTypes.iterator(); ti.hasNext(); ) {
            Triple type = (Triple) ti.next();
            Term rt = _subTypeOfClosure.importTerm(type.getSubject(), null);

            if(!_subTypeOfClosure.containsTriple(rt, rt, sto)) {
              _subTypeOfClosure.createTriple(rt, rt, sto, null, null);
            }
          }

          Set existingRelations = getTriples(null, null, OntoTools.SUB_TYPE_OF);
          //System.out.println("Exploring existing relations: " + existingRelations);
          for(Iterator eri = existingRelations.iterator(); eri.hasNext(); ) {
            Triple triple = (Triple) eri.next();

            objectClosure(_subTypeOfClosure.importTerm(triple.getSubject(), null),
                          _subTypeOfClosure.importTerm(triple.getObject(), null),
                          sto,
                          _subTypeOfClosure);
          }
          //System.out.println("Full closure: " + _subTypeOfClosure.getTriples(null, null, sto));
        } catch (OntologyException e) {
          throw new AssertionFailure(e);
        } catch (ChangeVetoException e) {
          throw new AssertionFailure(e);
        }
      }

      return _subTypeOfClosure;
    }

    Ontology _instanceOfClosure;

    Ontology getInstanceOfClosure() {
      if(_instanceOfClosure == null) {
        //System.out.println("Creating instance_of closure");
        try {
          _instanceOfClosure = OntoTools.getDefaultFactory().createOntology(
                  "instance_of_closure",
                  "Closure over instance_of");
          Term io = _instanceOfClosure.importTerm(OntoTools.INSTANCE_OF, null);

          Ontology sub_types = getSubTypeOfClosure();
          Term sto = sub_types.importTerm(OntoTools.SUB_TYPE_OF, null);

          Set instancesOf = getTriples(null, null, OntoTools.INSTANCE_OF);
          for(Iterator i = instancesOf.iterator(); i.hasNext(); ) {
            Triple trip = (Triple) i.next();
            //System.out.println("\tProcessing:" + trip);

            Term instance = _instanceOfClosure.importTerm(trip.getSubject(), null);
            Term type = trip.getObject();

            typeIoIsaClosure(sub_types, type, sto, instance, io);

            Term sub = sub_types.importTerm(trip.getSubject(), null);
            Set subTypes = sub_types.getTriples(null, sub, sto);
            for(Iterator sti = subTypes.iterator(); sti.hasNext(); ) {
              Triple stTrip = (Triple) sti.next();
              typeIoIsaClosure(sub_types, type, sto, _instanceOfClosure.importTerm(stTrip.getSubject(), null), io);
            }
          }

          Term any = _instanceOfClosure.importTerm(OntoTools.ANY, null);
          for(Iterator i = getTerms().iterator(); i.hasNext(); ) {
            Term t = (Term) i.next();
            Term rt = _instanceOfClosure.importTerm(t, null);
            if(!_instanceOfClosure.containsTriple(rt, any, io) ) {
              _instanceOfClosure.createTriple(rt, any, io, null, null);
            }
          }

          //System.out.println("All instance_of relations: " + _instanceOfClosure.getTriples(null, null, null));
        } catch (ChangeVetoException e) {
          throw new AssertionFailure(e);
        } catch (OntologyException e){
          throw new AssertionFailure(e);
        }
      }

      return _instanceOfClosure;
    }

    private void typeIoIsaClosure(Ontology sub_types, Term type, Term sto, Term instance, Term io) throws ChangeVetoException, AlreadyExistsException
    {
      Set typeClosure = sub_types.getTriples(sub_types.importTerm(type, null),
                                             null,
                                             sto);
      for(Iterator tci = typeClosure.iterator(); tci.hasNext(); ) {
        Triple typeTrip = (Triple) tci.next();
        Term tp = _instanceOfClosure.importTerm(typeTrip.getObject(), null);
        if(!_instanceOfClosure.containsTriple(instance,  tp, io)) {
          _instanceOfClosure.createTriple(instance,
                                          tp,
                                          io,
                                          null,
                                          null);
        }
      }
    }

    private void objectClosure(Term subject,
                               Term object,
                               Term predicate,
                               Ontology closure)
    {
      try {
        Term sub = closure.importTerm(subject, null);
        Term obj = closure.importTerm(object, null);
        Term prd = closure.importTerm(predicate, null);
        if(!closure.containsTriple(sub, obj, prd)) {
          closure.createTriple(sub, obj, prd, null, null);
        }
      } catch (AlreadyExistsException e) {
        throw new AssertionFailure(e);
      } catch (ChangeVetoException e) {
        throw new AssertionFailure(e);
      }

      Set targets = getTriples(object, null, predicate);
      for(Iterator i = targets.iterator(); i.hasNext();) {
        Triple t = (Triple) i.next();
        Term tObj = t.getObject();
        if(tObj != subject && tObj != object && !(tObj instanceof Variable)) {
          objectClosure(subject, tObj, predicate, closure);
        }
      }
    }

    boolean tripleExists(Term subject, Term object, Term predicate) {
      return !getTriples(subject, object, predicate).isEmpty();
    }

    Set getTriples(Term subject, Term object, Term predicate) {
      Set triples = new HashSet();

      for(Iterator i = getOntologies().iterator(); i.hasNext(); ) {
        Ontology o = (Ontology) i.next();
        try {
          if(subject  != null) subject  = o.importTerm(subject,  null);
          if(object   != null) object   = o.importTerm(object,   null);
          if(predicate != null) predicate = o.importTerm(predicate, null);
          triples.addAll(o.getTriples(subject,
                                     object,
                                     predicate));
        } catch (ChangeVetoException e) {
          throw new AssertionFailure(e);
        }
      }

      return triples;
    }

    private void populateMembership(Term term, Variable var, Set membership)
    {
      if(term instanceof Triple) {
        Triple trip = (Triple) term;
        Term sub = resolveRemote(trip.getSubject());
        Term obj = resolveRemote(trip.getObject());
        Term rel = resolveRemote(trip.getPredicate());

        if(sub == var) {
          //System.out.println("Variable used as subject: " + var + " " + trip);
          findDomain(rel, membership);
        } else {
          populateMembership(sub, var, membership);
        }

        if(obj == var) {
          //System.out.println("Variable used as object: " + var + " " + trip);
          findCodomain(rel, membership);
        } else {
          populateMembership(obj, var, membership);
        }

        if(rel == var) {
          //System.out.println("Variable used as relation: " + var + " " + trip);
          membership.add(OntoTools.RELATION);
        }

        if(sub == var && rel == OntoTools.INSTANCE_OF) {
          //System.out.println("Variable with INSTANCE_OF constraint: " + var + " " + trip);
          membership.add(obj);
        }

      }
    }

    private void findDomain(Term term, Set membrs) {
      // dumb search - probably wrong
      //System.out.println("findDomain: " + term);
      for(Iterator i = getOntologies().iterator(); i.hasNext(); ) {
        Ontology onto = (Ontology) i.next();
        Set domains = onto.getTriples(null, term, OntoTools.DOMAIN);
        for(Iterator di = domains.iterator(); di.hasNext(); ) {
          Triple trip = (Triple) di.next();
          //System.out.println("\t->" + trip);
          membrs.add(trip.getSubject());
        }
      }
    }

    private void findCodomain(Term term, Set membrs)
    {
      // dumb search - probably wrong
      for(Iterator i = getOntologies().iterator(); i.hasNext();) {
        Ontology onto = (Ontology) i.next();
        Set co_domains = onto.getTriples(null, term, OntoTools.CO_DOMAIN);
        for(Iterator di = co_domains.iterator(); di.hasNext();) {
          Triple trip = (Triple) di.next();
          membrs.add(trip.getSubject());
        }
      }
    }

    private static int depth = 0;

    /**
     * An Iterator that implements the state-machine that matches a given
     * predicate against all the things proveable by the ontology.
     *
     * @author Matthew Pocock
     */
    final class MatchIterator
            implements Iterator {
      private final Set constExps;
      private final Set varExps;
      private final Set extraVals;
      private Iterator constI;
      private Iterator varI;
      private final Iterator propI;
      private final Triple proposition;

      private Triple nextProposition;
      private Triple nextResult;

      public MatchIterator(Triple proposition, Set constExps, Set varExps, Set extraVals)
      {
        depth++;
        this.proposition = proposition;
        this.constExps = constExps;
        this.varExps = varExps;
        this.extraVals = extraVals;

        this.constI = constExps.iterator();
        this.varI = new ExpressionIterator(extraVals, varExps);

        if(findFirstVariable(proposition) != null) {
          propI = new ExpressionIterator(extraVals, Collections.singleton(proposition));
        } else {
          propI = Collections.singleton(proposition).iterator();
        }
        nextProposition = (Triple) propI.next();

        //System.out.println("matchIterator: Matcher for: " + proposition);
        //System.out.println("matchIterator: Propositioning: " + nextProposition);

        nextResult = findNextMatch();
        //System.out.println("Created: " + this + ": " + depth);
        if(depth > 400) throw new Error("Depth exceeded: " + depth);
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
        //System.out.println("findNextMatch in " + this.toString());
        while(true) {
          Triple next;
          if(constI.hasNext()) {
            next = (Triple) constI.next();
          } else if(varI.hasNext()) {
            next = (Triple) varI.next();
          } else if(propI.hasNext()) {
            nextProposition = (Triple) propI.next();
            constI = constExps.iterator();
            varI = varI = new ExpressionIterator(extraVals, varExps);
            //System.out.println("findNextMatch: Propositioning: " + nextProposition);
            continue;
          } else {
            //System.out.println("findNextMatch: Found no matches");
            depth--;
            return null;
          }

          //if(tries % 10000 == 0) //System.out.println("Iteration: " + tries + "\n\t" + next);

          //System.out.println("findNextMatch: Proposition: " + nextProposition);
          //System.out.println("findNextMatch: Evaluating: " + next);

          Term ifTrue = substitute(next, nextProposition, OntoTools.TRUE);
          if(evaluateExpression(ifTrue) == false) continue;

          Term ifFalse = substitute(next, nextProposition, OntoTools.FALSE);
          if(evaluateExpression(ifFalse) == true) continue;

          //System.out.println("findNextMatch: Accepted: " + next);

          return next;
        }
      }
    }

    final class ExpressionIterator
    implements Iterator {
      private final Iterator axI;
      private final Stack stack;
      private final Set propTerms;

      private Triple nextExpr;

      ExpressionIterator(Set propTerms, Set toExpand) {
        this.axI = toExpand.iterator();
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
        //System.out.println("findNext: Finding next term to evaluate");

        if(stack.size() == 0) {
          //System.out.println("findNext: Nothing left in subs");
          while(true) {
            if(!axI.hasNext()) {
              //System.out.println("findNext: No more axioms");
              return null;
            }

            Triple expr = (Triple) axI.next();
            //System.out.println("findNext: Testing next axiom: " + expr);
            Variable var = findFirstVariable(expr);

            //System.out.println("findNext: First variable is: " + var);

            Set vals = findValues(expr, var);
            //if(var.getName().equals("_x:76")) System.out.println("Values for " + var + " = " + vals);
            vals.addAll(propTerms);
            for(Iterator i = vals.iterator(); i.hasNext(); ) {
              Term t = resolveRemote((Term) i.next());
              if(t instanceof Variable) i.remove();
              if(t instanceof Triple) i.remove();
            }
            //System.out.println("Possible values: " + var + " " + vals);

            // got one
            OptionSearcher subs = new OptionSearcher(expr, var, vals.iterator());
            //System.out.println("findNext: Created new option searcher: " + subs);
            stack.push(subs);
            //System.out.println("findNext: Pushing a new frame: " + stack);
            break;
          }
        }

        OptionSearcher subs = (OptionSearcher) stack.peek();

        while(true) {
          if(!subs.hasNext()) {
            stack.pop();
            //System.out.println("findNext: Run out of options: " + stack);
            if(stack.isEmpty()) {
              //System.out.println("findNext: Empty stack - out of options");
              return findNext();
            }

            subs = (OptionSearcher) stack.peek();
            continue;
          }

          Triple expr = (Triple) subs.next();

          //System.out.println("findNext: expanding expression: " + expr);
          Variable var = findFirstVariable(expr);
          //System.out.println("findNext: First variable: " + var);
          if(var == null) return wrap(expr, stack);        // this contains no variables

          Set vals = findValues(expr, var);
          //if(var.getName().equals("_x:76")) System.out.println("Values for " + var + " = " + vals);
          vals.addAll(propTerms);
          for(Iterator i = vals.iterator(); i.hasNext();) {
            Term t = resolveRemote((Term) i.next());
            if(t instanceof Variable) i.remove();
            if(t instanceof Triple) i.remove();
          }
          //System.out.println("Possible values: " + var + " " + vals);

          subs = new OptionSearcher(expr, var, vals.iterator());
          //System.out.println("findNext: Creating new option searcher: " + subs + " " + vals);
          stack.push(subs);
          //System.out.println("findNext: Pushing a new frame: " + stack);
          if(stack.size() > 10) throw new Error("Stack too deep: " + stack);
        }
      }

      private Triple wrap(Triple expr, Stack stack) {
        StringBuffer name = new StringBuffer(((OptionSearcher) stack.get(0)).getExpr().getName());

        name.append("[");
        for(Iterator i = stack.iterator(); i.hasNext(); ) {
          OptionSearcher os = (OptionSearcher) i.next();
          name.append(os.getVar());
          name.append(" <- ");
          name.append(os.getNewVal());
          if(i.hasNext()) {
            name.append(", ");
          }
        }
        name.append("]");

        return createVirtualTerm(expr.getSubject(),
                                 expr.getObject(),
                                 expr.getPredicate(),
                                 name.toString(),
                                 null);
      }
    }

    final class OptionSearcher
            implements Iterator
    {
      private final Term expr;
      private final Term var;
      private final Iterator vals;
      private Term newVal;

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

      public Term getNewVal() {
        return newVal;
      }

      public boolean hasNext()
      {
        return vals.hasNext();
      }

      public Object next()
      {
        newVal = (Term) vals.next();
        //System.out.println("OptionSearcher.newVal(): Binding " + getVar() + " to " + newVal);
        return substitute(expr, var, newVal);
      }

      public void remove()
      {
        throw new UnsupportedOperationException();
      }

      public String toString()
      {
        return "@" + hashCode() + " " + getVar();
      }
    }
  }

  static class Expression {
    private final Term term;
    private final Term value;
    private final Term reason;

    public Expression(Term term, Term value, Term reason) {
      this.term = term;
      this.value = value;
      this.reason = reason;
    }

    public Term getTerm() {
      return term;
    }

    public Term getValue() {
      return value;
    }

    public Term getReason() {
      return reason;
    }
  }
}
