package org.biojava.bio.ontology;

import java.util.*;

import org.biojava.utils.ChangeVetoException;
import org.biojava.bio.BioError;

abstract class DefaultOps
implements OntologyOps {
  public abstract Ontology getOntology();
  
    public Ontology transitiveClosure(
      Term subject,
      Term object,
      Term relation
    ) throws OntologyException {
      try {
        Ontology onto = OntoTools.getDefaultFactory().createOntology(
          "transitive closure on " +
          relation +
          " in " +
          subject.getOntology().getName(),
          ""
        );
        Term ourRel = onto.importTerm(relation);
        System.err.println("Transitive closure: " + onto);
        
        for(Iterator i = getOntology().getTerms().iterator(); i.hasNext(); ) {
          Term t = (Term) i.next();
          if(OntoTools.isa(t, subject) && OntoTools.isa(t, object)) {
            Term it = onto.importTerm(t);
            onto.createTriple(it, it, relation);
          }
        }
        
        Set tups = OntoTools.getTriples(
          subject.getOntology(),
          OntoTools.ANY,
          OntoTools.ANY,
          relation
        );
        System.err.println("tuples: " + tups.size());
        
        for(Iterator i = tups.iterator(); i.hasNext(); ) {
          Triple trip = (Triple) i.next();
          System.err.println("Evaluating: " + trip);
          
          if(OntoTools.isa(trip.getSubject(), subject)) {
            Set seen = new HashSet();
            seen.add(trip.getSubject());
            recurseTC(trip, trip, object, ourRel, tups, onto, seen);
          }
        }
        
        return onto;
      } catch (ChangeVetoException cve) {
        throw new BioError(cve, "Assertion Failure");
      }
    }
    
    private void recurseTC(
      Triple first,
      Triple current,
      Term object,
      Term relation,
      Set tups,
      Ontology onto,
      Set seen
    ) throws OntologyException, ChangeVetoException {
      if(
        OntoTools.isa(current.getObject(), object)
      ) {
        onto.createTriple(
          onto.importTerm(first.getSubject()),
          onto.importTerm(current.getObject()),
          relation
        );
      }
      
      seen.add(current.getObject());
      
      for(Iterator i = tups.iterator(); i.hasNext(); ) {
        Triple t = (Triple) i.next();
        if(
          !seen.contains(t.getObject()) &&
          OntoTools.isa(t.getSubject(), current.getObject())
        ) {
          recurseTC(
            first,
            t,
            object,
            relation,
            tups,
            onto,
            seen
          );
        }
      }
    }
    
    public boolean isa(Term subject, Term object)
    throws OntologyException {
      if(subject.getOntology() != object.getOntology()) {
        throw new IllegalArgumentException(
          "isa must be called with two terms from the same ontology: " +
          subject.toString() + " , " + object.toString()
        );
      }
      
      Set visited = new HashSet();
      
      return recurseIsa(subject, object, visited);
    }
    
    private boolean recurseIsa(Term subject, Term object, Set visited) {
      System.out.println("Checking " + subject + " and " + object);
      if(subject == object) {
        System.out.println("equal");
        return true;
      }
      
      if(visited.contains(subject)) {
        System.out.println("seen before");
        return false;
      }
      
      visited.add(subject);
      
      Set trips = subject.getOntology().getTriples(subject, null, OntoTools.IS_A);
      
      for(Iterator i = trips.iterator(); i.hasNext(); ) {
        Triple trip = (Triple) i.next();
        Term tobj = trip.getObject();
        
        if(recurseIsa(tobj, object, visited)) {
          return true;
        }
      }
      
      return false;
    }
  }
