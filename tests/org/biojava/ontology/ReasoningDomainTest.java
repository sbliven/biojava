/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.ontology;

import java.util.*;
import org.biojava.utils.*;

import junit.framework.TestCase;

/**
 * Tests for Ontology.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.4
 */
public class ReasoningDomainTest
extends TestCase {
  public static void main(String[] args) throws Throwable {
    new ReasoningDomainTest().testReasoningCoreNamespace();
  }

  public void testAddRemove()
  throws OntologyException, AlreadyExistsException, ChangeVetoException
  {
    Ontology core = OntoTools.getCoreOntology();
    Ontology derived = OntoTools.getDefaultFactory().createOntology("derived", "");
    derived.importTerm(OntoTools.ISA, null);

    // just core explicitly added & removed - this drags core.integer allong
    ReasoningDomain rDom1 = new ReasoningDomain.Impl();
    rDom1.addOntology(core);
    assertEquals(rDom1.getOntologies().size(), 2);
    assertTrue(rDom1.getOntologies().contains(core));
    rDom1.removeOntology(core);
    assertEquals(rDom1.getOntologies().size(), 0);
    assertFalse(rDom1.getOntologies().contains(core));

    // add derived only - should suck in core
    ReasoningDomain rDom2 = new ReasoningDomain.Impl();
    rDom2.addOntology(derived);
    assertEquals(rDom2.getOntologies().size(), 3);
    assertTrue(rDom2.getOntologies().contains(derived));
    assertTrue(rDom2.getOntologies().contains(core));
    rDom2.removeOntology(derived);
    assertEquals(rDom2.getOntologies().size(), 0);

    // add derived ony, remove core - should still have 3 ontologies
    ReasoningDomain rDom3 = new ReasoningDomain.Impl();
    rDom3.addOntology(derived);
    rDom3.removeOntology(core);
    assertEquals(rDom3.getOntologies().size(), 3);

    // add derived, then core, remove derived, should contain core
    ReasoningDomain rDom4 = new ReasoningDomain.Impl();
    rDom4.addOntology(derived);
    rDom4.addOntology(core);
    rDom4.removeOntology(derived);
    assertEquals(rDom4.getOntologies().size(), 2);
    assertTrue(rDom4.getOntologies().contains(core));

    // add core, add derived, remove derived, should contain core
    ReasoningDomain rDom5 = new ReasoningDomain.Impl();
    rDom5.addOntology(core);
    rDom5.addOntology(derived);
    rDom5.removeOntology(derived);
    assertEquals(rDom5.getOntologies().size(), 2);
    assertTrue(rDom5.getOntologies().contains(core));
  }

  public void testReasoningCoreNamespace()
  throws OntologyException, AlreadyExistsException, ChangeVetoException
  {
    Ontology core = OntoTools.getCoreOntology();

    ReasoningDomain coreD = new ReasoningDomain.Impl();
    coreD.addOntology(core);

    // isa relationships
    Iterator matcher;
    matcher = coreD.getMatching(OntoTools.ISA, OntoTools.PARTIAL_ORDER, OntoTools.ISA);
    while(matcher.hasNext()) {
      System.err.println("\t-> " + matcher.next());
    }

    matcher = coreD.getMatching(OntoTools.PARTIAL_ORDER, OntoTools.REFLEXIVE, OntoTools.ISA);
    while(matcher.hasNext()) {
      System.err.println("\t-> " + matcher.next());
    }

    matcher = coreD.getMatching(OntoTools.REFLEXIVE, OntoTools.RELATION, OntoTools.ISA);
    while(matcher.hasNext()) {
      System.err.println("\t-> " + matcher.next());
    }

    matcher = coreD.getMatching(OntoTools.PARTIAL_ORDER, OntoTools.RELATION, OntoTools.ISA);
    while(matcher.hasNext()) {
      System.err.println("\t-> " + matcher.next());
    }

    matcher = coreD.getMatching(OntoTools.ISA, OntoTools.RELATION, OntoTools.ISA);
    while(matcher.hasNext()) {
      System.err.println("\t-> " + matcher.next());
    }

    matcher = coreD.getMatching(OntoTools.ISA, coreD.createVariable("_parent"), OntoTools.ISA);
    while(matcher.hasNext()) {
      System.err.println("\t-> " + matcher.next());
    }


    //assertTrue(coreD.getMatching(OntoTools.RELATION, OntoTools.ANY, OntoTools.ISA));
    //assertTrue(coreD.getMatching(OntoTools.ISA, OntoTools.ISA, OntoTools.ISA));
    //assertTrue(coreD.getMatching(OntoTools.ISA, OntoTools.RELATION, OntoTools.ISA));
    //assertTrue(coreD.getMatching(OntoTools.ISA, OntoTools.ANY, OntoTools.ISA));
    //assertTrue(coreD.getMatching(OntoTools.PARTIAL_ORDER, OntoTools.RELATION, OntoTools.ISA));
    //assertFalse(coreD.getMatching(OntoTools.REFLEXIVE, OntoTools.SYMMETRIC, OntoTools.ISA));
    //assertFalse(coreD.getMatching(OntoTools.ANY, OntoTools.RELATION, OntoTools.ISA));
  }

/*  public void testReasoningUserNamespace()
      throws Exception
  {
      Ontology onto = new Ontology.Impl("biology", "Some random bits of biological knowledge");

      Term animal = onto.createTerm("animal", "Not vegetable or mineral");
      Term mammal = onto.createTerm("mammal", "Homeothermic animals with mammary glands");
      Term bird = onto.createTerm("bird", "Homeothermic animals which lay hard-shelled eggs");
      Term cow = onto.createTerm("cow", "moo");
      Term pig = onto.createTerm("pig", "oink");
      Term isa = onto.importTerm(OntoTools.ISA, null);
      onto.createTriple(mammal, animal, isa, null, null);
      onto.createTriple(cow, mammal, isa, null, null);
      onto.createTriple(pig, mammal, isa, null, null);
      onto.createTriple(bird, animal, isa, null, null);


      Term organ = onto.createTerm("organ", "It's a bit of an animal");
      Term has_organ = onto.createTerm("has_organ", "Animals have organs.  Yeah.");
      onto.createTriple(animal, organ, has_organ, null, null);
      onto.createTriple(has_organ, onto.importTerm(OntoTools.HAS_A, null), onto.importTerm(OntoTools.ISA, null), null, null);
      Term mammary_glands = onto.createTerm("mammary_glands", "mammals have these");
      onto.createTriple(mammary_glands, organ, isa, null, null);
      onto.createTriple(mammal, mammary_glands, has_organ, null, null);

      ReasoningDomain rd = new ReasoningDomain.Impl();
      rd.addOntology(onto);

      assertTrue(rd.getMatching(cow, animal, OntoTools.ISA));
      assertTrue(rd.getMatching(bird, animal, OntoTools.ISA));
      assertFalse(rd.getMatching(pig, bird, OntoTools.ISA));
      assertFalse(rd.getMatching(bird, mammal, OntoTools.ISA));

      assertTrue(rd.getMatching(cow, mammary_glands, OntoTools.HAS_A));
      assertFalse(rd.getMatching(bird, mammary_glands, OntoTools.HAS_A));
  }*/
}
