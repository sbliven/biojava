package org.biojava.ontology;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PushbackReader;

import org.biojava.bio.BioError;
import org.biojava.ontology.io.TriplesParser;

/**
 * Tools for manipulating ontologies.
 *
 * @author Matthew Pocock
 */
public final class OntoTools {
  private static final Ontology CORE_ONTOLOGY;
  private static final OntologyFactory DEFAULT_FACTORY;
  private static final IntegerOntology CORE_INTEGER;
  //private static final Ontology CORE_STRING;

  public static final Term BOOLEAN;
  public static final Term TRUE;
  public static final Term FALSE;
  public static final Term AND;
  public static final Term OR;
  public static final Term XOR;
  public static final Term EQUAL;
  public static final Term NOT_EQUAL;
  public static final Term IMPLIES;

  public static final Term TYPE;
  public static final Term INSTANCEOF;
  public static final Term ISA;
  public static final Term ANY;
  public static final Term NONE;
  public static final Term RELATION;
  public static final Term DOMAIN;
  public static final Term CO_COMAIN;

  public static final Term SET;
  public static final Term UNIVERSAL;
  public static final Term EMPTY;
  public static final Term CONTAINS;
  public static final Term NOT_CONTAINS;
  public static final Term SUB_SET;

  public static final Term PREDICATE;
  public static final Term REFLEXIVE;
  public static final Term SYMMETRIC;
  public static final Term TRANSITIVE;
  public static final Term EQUIVALENCE;
  public static final Term PARTIAL_ORDER;

  public static final Term PART_OF;

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
            "org/biojava/ontology/core.pred"
          )
        )
      );

      CORE_INTEGER = new IntegerOntology();

      ReasoningDomain rdom = new ReasoningDomain.Impl();
      rdom.addOntology(CORE_INTEGER);
      CORE_ONTOLOGY = new TriplesParser().parse(
              new PushbackReader(reader),
              DEFAULT_FACTORY,
              rdom);

      BOOLEAN = CORE_ONTOLOGY.getTerm("boolean");
      TRUE = CORE_ONTOLOGY.getTerm("true");
      FALSE = CORE_ONTOLOGY.getTerm("false");
      AND = CORE_ONTOLOGY.getTerm("and");
      OR = CORE_ONTOLOGY.getTerm("or");
      XOR = CORE_ONTOLOGY.getTerm("xor");
      EQUAL = CORE_ONTOLOGY.getTerm("equal");
      NOT_EQUAL = CORE_ONTOLOGY.getTerm("not_equal");
      IMPLIES = CORE_ONTOLOGY.getTerm("implies");

      TYPE = CORE_ONTOLOGY.getTerm("type");
      INSTANCEOF = CORE_ONTOLOGY.getTerm("instanceof");
      ISA = CORE_ONTOLOGY.getTerm("isa");
      ANY = CORE_ONTOLOGY.getTerm("any");
      NONE = CORE_ONTOLOGY.getTerm("none");

      RELATION = CORE_ONTOLOGY.getTerm("relation");
      DOMAIN = CORE_ONTOLOGY.getTerm("domain");
      CO_COMAIN = CORE_ONTOLOGY.getTerm("co_domain");

      SET = CORE_ONTOLOGY.getTerm("set");
      UNIVERSAL = CORE_ONTOLOGY.getTerm("universal");
      EMPTY = CORE_ONTOLOGY.getTerm("empty");
      CONTAINS = CORE_ONTOLOGY.getTerm("contains");
      NOT_CONTAINS = CORE_ONTOLOGY.getTerm("not_contains");
      SUB_SET = CORE_ONTOLOGY.getTerm("sub_set");

      PREDICATE = CORE_ONTOLOGY.getTerm("predicate");
      REFLEXIVE = CORE_ONTOLOGY.getTerm("reflexive");
      EQUIVALENCE = CORE_ONTOLOGY.getTerm("equivalence");
      SYMMETRIC = CORE_ONTOLOGY.getTerm("symmetric");
      TRANSITIVE = CORE_ONTOLOGY.getTerm("transitive");
      PARTIAL_ORDER = CORE_ONTOLOGY.getTerm("partial_order");

      PART_OF = null;
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

  /**
   * Get the Ontology that defines integers.
   *
   * <p>This contains a term for each and every integer. I haven't decided yet
   * if it contains terms for arithmatic.</p>
   *
   * @return the integer Ontology
   */
  public static IntegerOntology getIntegerOntology() {
    return CORE_INTEGER;
  }

  public static OntologyFactory getDefaultFactory() {
    return DEFAULT_FACTORY;
  }
}
