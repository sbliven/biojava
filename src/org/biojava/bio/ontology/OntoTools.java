package org.biojava.bio.ontology;

import java.io.*;
import java.util.*;

import org.biojava.bio.BioError;
import org.biojava.bio.ontology.io.TabDelimParser;

/**
 * Tools for manipulating ontologies.
 *
 * @author Matthew Pocock
 */
public final class OntoTools {
  private static final Ontology CORE_ONTOLOGY;
  private static final OntologyFactory DEFAULT_FACTORY;
  
  public static final Term IS_A;
  public static final Term HAS_A;
  public static final Term ANY;
  public static final Term REMOTE_TERM;
  public static final Term TRIPLE_TERM;
  public static final Term RELATION;
  public static final Term REFLEXIVE;
  public static final Term EQUIVALENCE;
  public static final Term PARTIAL_ORDER;
  public static final Term PART_OF;
  public static final Term INVERSE;
  
  static {
    DEFAULT_FACTORY = new OntologyFactory() {
      public Ontology createOntology(String name, String desc)
      throws OntologyException {
        return new Ontology.Impl(name, desc);
      }
    };
    
    try {
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(
          OntoTools.class.getClassLoader().getResourceAsStream(
            "org/biojava/bio/ontology/core.onto"
          )
        )
      );
      
      CORE_ONTOLOGY = new TabDelimParser().parse(reader, DEFAULT_FACTORY);
      
      IS_A = CORE_ONTOLOGY.getTermByName("is-a");
      HAS_A = CORE_ONTOLOGY.getTermByName("has-a");
      ANY = CORE_ONTOLOGY.getTermByName("any");
      REMOTE_TERM = CORE_ONTOLOGY.getTermByName("remote-term");
      TRIPLE_TERM = CORE_ONTOLOGY.getTermByName("triple-term");
      RELATION = CORE_ONTOLOGY.getTermByName("relation");
      REFLEXIVE = CORE_ONTOLOGY.getTermByName("reflexive");
      EQUIVALENCE = CORE_ONTOLOGY.getTermByName("equivalence");
      PARTIAL_ORDER = CORE_ONTOLOGY.getTermByName("partial-order");
      PART_OF = CORE_ONTOLOGY.getTermByName("part-of");
      INVERSE = CORE_ONTOLOGY.getTermByName("inverse");
    } catch (Exception e) {
      throw new BioError(e, "Could not initialize OntoTools");
    }
  }
  
  
  private OntoTools() {}
  
  /**
   * Get the Ontology that defines our core "central dogma".
   *
   * This contains deffinitions that we have to have, such as <code>any</code>,
   * <code>relation</code>, <code>is-a</code> and <code>transient</code>.
   *
   * @return the "core" Ontology
   */
  public static Ontology getCoreOntology() {
    return CORE_ONTOLOGY;
  }
  
  public static OntologyFactory getDefaultFactory() {
    return DEFAULT_FACTORY;
  }
  
  /**
   * Decide if one Term is an instance of another.
   *
   * @param subject  the subject Term
   * @param object   the object Term
   * @return true if it can be proved that subject is-a object, false otherwise
   */
  public static boolean isa(
    Term subject,
    Term object
  ) throws OntologyException {
    String _prefix = "isa(" + subject + "," + object + ") ";
    System.err.println(_prefix + "Starting");
    
    if(subject == object) {
      System.err.println(_prefix + "Ending true: identical");
      return true;
    } else if(object == ANY) {
      System.err.println(_prefix + "Ending true: any");
      return true;
    }
    
    if(subject instanceof RemoteTerm) {
      RemoteTerm srt = (RemoteTerm) subject;
      
      if(OntoTools.isa(srt.getRemoteTerm(), object)) {
        return true;
      }
    }
    
    if(object instanceof RemoteTerm) {
      RemoteTerm ort = (RemoteTerm) object;
      
      if(OntoTools.isa(subject, ort.getRemoteTerm())) {
        return true;
      }
    }
    
    OntologyOps oo;
    if(subject.getOntology() instanceof OntologyOps) {
      oo = (OntologyOps) subject.getOntology();
    } else {
      final Ontology so = subject.getOntology();
      oo = new DefaultOps() {
        public Ontology getOntology() {
          return so;
        }
      };
    }
    
    if(subject.getOntology() == object.getOntology()) {
      System.err.println(_prefix + "Delegating to ontology tools");
      return oo.isa(subject, object);
    } else {
      System.err.println(_prefix + "Computing transitive closure for " + subject);
      Ontology remoteTriples = oo.transitiveClosure(
        subject, ANY, IS_A
      );
      System.err.println(_prefix + "Searching closure for " + subject + " " + remoteTriples);
      for(
        Iterator i = remoteTriples.getTriples(null, null, null).iterator();
        i.hasNext();
      ) {
        Triple triple = (Triple) i.next();
        System.err.println(_prefix + "Evaluating " + triple);
        RemoteTerm rt = (RemoteTerm) triple.getObject();
        System.err.println(_prefix + "Following to " + rt.getRemoteTerm() + "," + object);
        if(!isa(
          rt.getRemoteTerm(),
          object
        )) {
          System.err.println(_prefix + "Ending true");
          return true;
        }
      }
      
      System.err.println(_prefix + "Ending false");
      return false;
    }
  }
  /**
   * Get a Set of Triples satisfying some constraints.
   *
   * <p>
   * This will find all tripples where each component of the triple inherits
   * from those provided. You may use OntoTools.ANY to accept any Term for that
   * component. This differs from the similar method in Ontology, as
   * Ontology.getTriples() finds exact matches for each component not set to
   * NULL.
   * </p>
   *
   * @param ontology  the Ontology to search
   * @param subject   a Term that the subjects must inherit from
   * @param object    a Term that the objects must inherit from
   * @param relation  a Term that the relations must inherit from
   * @return  a Set of Triples satisfying the constraints
   * @throws NullPointerException if any of the arguments are null
   */
  public static Set getTriples(
    Ontology ontology,
    Term subject,
    Term object,
    Term relation
  ) throws OntologyException {
    Set res = new HashSet();
    
    System.err.println("Searching triples");
    for(
      Iterator tripI = ontology.getTriples(null, null, null).iterator();
      tripI.hasNext();
    ) {
      Triple trip = (Triple) tripI.next();
      System.err.println("evaluating: " + trip);
      if(
        (subject == ANY || isa(trip.getSubject(), subject)) &&
        (object == ANY || isa(trip.getObject(), object)) &&
        isa(trip.getRelation(), relation)
      ) {
        System.err.println("accepted");
        res.add(trip);
      }
    }
    
    return res;
  }
}
