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
  private static final OntologyOps DEFAULT_OPS;
  
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
    
    DEFAULT_OPS = new DefaultOps();
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
  
  public OntologyOps getDefaultOps() {
    return DEFAULT_OPS;
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
      System.err.println(_prefix + "Ending true");
      return true;
    }
    
    OntologyOps oo;
    if(subject.getOntology() instanceof OntologyOps) {
      oo = (OntologyOps) subject.getOntology();
    } else {
      oo = DEFAULT_OPS;
    }
    
    if(subject.getOntology() == object.getOntology()) {
      System.err.println(_prefix + "Delegating to ontology tools");
      return oo.isa(subject, object);
    } else {
      System.err.println(_prefix + "Computing transitive closure");
      Ontology remoteTriples = oo.transitiveClosure(
        REMOTE_TERM, ANY, IS_A
      );
      System.err.println(_prefix + "Searching closure for " + subject);
      for(
        Iterator i = remoteTriples.getTriples(null, null, null).iterator();
        i.hasNext();
      ) {
        Triple triple = (Triple) i.next();
        RemoteTerm rt = (RemoteTerm) triple.getSubject();
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
    
  public static Set getTriples(
    Ontology ontology,
    Term subject,
    Term object,
    Term relation
  ) throws OntologyException {
    Set res = new HashSet();
    
    for(
      Iterator tripI = ontology.getTriples(null, null, null).iterator();
      tripI.hasNext();
    ) {
      Triple trip = (Triple) tripI.next();
      
      if(
        (subject == ANY || isa(trip.getSubject(), subject)) &&
        (object == ANY || isa(trip.getObject(), object)) &&
        isa(trip.getRelation(), relation)
      ) {
        res.add(trip);
      }
    }
    
    return res;
  }
}
