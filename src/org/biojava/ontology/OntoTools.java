package org.biojava.ontology;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.biojava.bio.BioError;
import org.biojava.ontology.io.TabDelimParser;

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
  public static final Term TRIPLE;
  public static final Term SOURCE;
  public static final Term OBJECT;
  public static final Term RELATION;
  public static final Term REFLEXIVE;
  public static final Term SYMMETRIC;
  public static final Term TRANSITIVE;
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
      TRIPLE = CORE_ONTOLOGY.getTerm("triple");
      SOURCE = CORE_ONTOLOGY.getTerm("source");
      OBJECT = CORE_ONTOLOGY.getTerm("object");
      RELATION = CORE_ONTOLOGY.getTerm("relation");
      REFLEXIVE = CORE_ONTOLOGY.getTerm("reflexive");
      EQUIVALENCE = CORE_ONTOLOGY.getTerm("equivalence");
      SYMMETRIC = CORE_ONTOLOGY.getTerm("symmetric");
      TRANSITIVE = CORE_ONTOLOGY.getTerm("transitive");
      PARTIAL_ORDER = CORE_ONTOLOGY.getTerm("partial-order");
      PART_OF = CORE_ONTOLOGY.getTerm("part-of");
      INVERSE = CORE_ONTOLOGY.getTerm("inverse");
    } catch (Exception e) {
      throw new BioError("Could not initialize OntoTools", e);
    }
  }


  private OntoTools() {}

  /**
   * Get the Ontology that defines our core "central dogma".
   *
   * <p>This contains definitions that we have to have, such as <code>any</code>,
   * <code>relation</code>, <code>is-a</code> and <code>transient</code>. These
   * are our axioms, upon which the default interpreters build.</p>
   *
   * @return the "core" Ontology
   */
  public static Ontology getCoreOntology() {
    return CORE_ONTOLOGY;
  }

  public static OntologyFactory getDefaultFactory() {
    return DEFAULT_FACTORY;
  }
}
