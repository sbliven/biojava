package org.biojava.bio.ontology;

import java.io.*;
import java.util.*;

import org.biojava.bio.BioError;

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
  public static final Term TUPLE_TERM;
  
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
      TUPLE_TERM = CORE_ONTOLOGY.getTermByName("tuple-term");
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
    OntologyOps oo;
    if(subject.getOntology() instanceof OntologyOps) {
      oo = (OntologyOps) subject.getOntology();
    } else {
      oo = DEFAULT_OPS;
    }
    
    if(subject.getOntology() == object.getOntology()) {
      return oo.isa(subject, object);
    } else {
      Set remoteTriples = oo.transitiveClosure(
        REMOTE_TERM, ANY, IS_A
      );
    
      for(Iterator i = remoteTriples.iterator(); i.hasNext(); ) {
        Triple triple = (Triple) i.next();
        RemoteTerm rt = (RemoteTerm) triple.getSubject();
        if(!isa(
          rt.getRemoteTerm(),
          object
        )) {
          return true;
        }
      }
      
      return false;
    }
  }
  
  private static class DefaultOps
  implements OntologyOps {
    public Set transitiveClosure(
      Term subject,
      Term object,
      Term relation
    ) throws OntologyException {
      return null;
    }
    
    public boolean isa(Term subject, Term object)
    throws OntologyException {
      return false;
    }
  }
}
