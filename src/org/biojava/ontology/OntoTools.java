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
      
      IS_A = CORE_ONTOLOGY.getTerm("is-a");
      HAS_A = CORE_ONTOLOGY.getTerm("has-a");
      ANY = CORE_ONTOLOGY.getTerm("any");
      REMOTE_TERM = CORE_ONTOLOGY.getTerm("remote-term");
      TRIPLE_TERM = CORE_ONTOLOGY.getTerm("triple-term");
      RELATION = CORE_ONTOLOGY.getTerm("relation");
      REFLEXIVE = CORE_ONTOLOGY.getTerm("reflexive");
      EQUIVALENCE = CORE_ONTOLOGY.getTerm("equivalence");
      PARTIAL_ORDER = CORE_ONTOLOGY.getTerm("partial-order");
      PART_OF = CORE_ONTOLOGY.getTerm("part-of");
      INVERSE = CORE_ONTOLOGY.getTerm("inverse");
    } catch (Exception e) {
      throw new BioError(e, "Could not initialize OntoTools");
    }
  }
  
  
  private OntoTools() {}
  
  /**
   * Get the Ontology that defines our core "central dogma".
   *
   * This contains definitions that we have to have, such as <code>any</code>,
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
    if(subject == object) {
      return true;
    } else if(object == ANY) {
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
    
    OntologyOps subOps = subject.getOntology().getOps();
    OntologyOps objOps = object.getOntology().getOps();
    
    if(subOps == objOps) {
      return subOps.isa(subject, object);
    } else {
      Ontology remoteTriples = subOps.transitiveClosure(
        subject, ANY, IS_A
      );
      for(
        Iterator i = remoteTriples.getTriples(null, null, null).iterator();
        i.hasNext();
      ) {
        Triple triple = (Triple) i.next();
        RemoteTerm rt = (RemoteTerm) triple.getObject();
        if(
          rt.getRemoteTerm() != subject &&
          !isa(
            rt.getRemoteTerm(),
            object
          )
        ) {
          return true;
        }
      }
      
      return false;
    }
  }
  
  /**
   * Get a Set of Triples satisfying some constraints.
   *
   * <p>
   * This will find all triples where each component of the triple inherits
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
