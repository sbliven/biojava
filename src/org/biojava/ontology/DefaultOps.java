package org.biojava.bio.ontology;

import java.util.*;

import org.biojava.utils.ChangeVetoException;
import org.biojava.bio.BioError;

abstract class DefaultOps
implements OntologyOps, java.io.Serializable {
  Set trueThings;
  Set falseThings;

  {
    trueThings = new HashSet();
    falseThings = new HashSet();
  }
  
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
        
        for(Iterator i = getOntology().getTerms().iterator(); i.hasNext(); ) {
          Term t = (Term) i.next();
          if(OntoTools.isa(t, subject) && OntoTools.isa(t, object)) {
            Term it = onto.importTerm(t);
            onto.createTriple(it, it, ourRel);
          }
        }
        
        Set tups = OntoTools.getTriples(
          subject.getOntology(),
          OntoTools.ANY,
          OntoTools.ANY,
          relation
        );
        
        for(Iterator i = tups.iterator(); i.hasNext(); ) {
          Triple trip = (Triple) i.next();
          
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
      
      return recurseIsaC(subject, object, visited);
    }
    
    private boolean recurseIsaC(Term subject, Term object, Set visited) {
      TripleStruct ts = new TripleStruct(subject, object, OntoTools.IS_A);
      
      if(trueThings.contains(ts)) {
        return true;
      } else if(falseThings.contains(ts)) {
        return false;
      }
      
      boolean isa = recurseIsa(subject, object, visited);
      
      if(isa) {
        trueThings.add(ts);
      } else {
        falseThings.add(ts);
      }
      
      return isa;
    }
    
    private boolean recurseIsa(Term subject, Term object, Set visited) {
      if(subject == object) {
        return true;
      }
      
      if(visited.contains(subject)) {
        return false;
      }
      
      visited.add(subject);
      
      Set trips = subject.getOntology().getTriples(subject, null, OntoTools.IS_A);
      
      for(Iterator i = trips.iterator(); i.hasNext(); ) {
        Triple trip = (Triple) i.next();
        Term tobj = trip.getObject();
        
        if(recurseIsaC(tobj, object, visited)) {
          return true;
        }
      }
      
      return false;
    }
    
  private static class TripleStruct
  implements java.io.Serializable {
    public final Term subject;
    public final Term object;
    public final Term relation;
    
    public TripleStruct(Term subject, Term object, Term relation) {
      this.subject = subject;
      this.object = object;
      this.relation = relation;
    }
    
    public boolean equals(Object o) {
      if(o instanceof TripleStruct) {
        TripleStruct ts = (TripleStruct) o;
        return ts.subject == subject &&
               ts.object == object &&
               ts.relation == relation;
      }
      
      return false;
    }
    
    public int hashCode() {
      return subject.hashCode() +
             object.hashCode() * 37 +
             relation.hashCode() * 37 * 37;
    }
  }
}
