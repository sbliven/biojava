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

package org.biojava.bio.ontology;

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
  public void testAddRemove()
  throws OntologyException, AlreadyExistsException, ChangeVetoException
  {
    Ontology core = OntoTools.getCoreOntology();
    Ontology derived = OntoTools.getDefaultFactory().createOntology("derived", "");
    derived.importTerm(OntoTools.IS_A);
    
    // just core explicitly added & removed
    ReasoningDomain rDom1 = new ReasoningDomain.Impl();
    rDom1.addOntology(core);
    assertEquals(rDom1.getOntologies().size(), 1);
    assertTrue(rDom1.getOntologies().contains(core));
    rDom1.removeOntology(core);
    assertEquals(rDom1.getOntologies().size(), 0);
    assertFalse(rDom1.getOntologies().contains(core));
    
    // add derived only - should suck in core
    ReasoningDomain rDom2 = new ReasoningDomain.Impl();
    rDom2.addOntology(derived);
    assertEquals(rDom2.getOntologies().size(), 2);
    assertTrue(rDom2.getOntologies().contains(derived));
    assertTrue(rDom2.getOntologies().contains(core));
    rDom2.removeOntology(derived);
    assertEquals(rDom2.getOntologies().size(), 0);
    
    // add derived ony, remove core - should still have 2 ontologies
    ReasoningDomain rDom3 = new ReasoningDomain.Impl();
    rDom3.addOntology(derived);
    rDom3.removeOntology(core);
    assertEquals(rDom3.getOntologies().size(), 2);
    
    // add derived, then core, remove derived, should contain core
    ReasoningDomain rDom4 = new ReasoningDomain.Impl();
    rDom4.addOntology(derived);
    rDom4.addOntology(core);
    rDom4.removeOntology(derived);
    assertEquals(rDom4.getOntologies().size(), 1);
    assertTrue(rDom4.getOntologies().contains(core));
    
    // add core, add derived, remove derived, should contain core
    ReasoningDomain rDom5 = new ReasoningDomain.Impl();
    rDom5.addOntology(core);
    rDom5.addOntology(derived);
    rDom5.removeOntology(derived);
    assertEquals(rDom5.getOntologies().size(), 1);
    assertTrue(rDom5.getOntologies().contains(core));
  }
  
  public void testReasoningOneNamespace()
  throws OntologyException, AlreadyExistsException, ChangeVetoException
  {
    Ontology core = OntoTools.getCoreOntology();
    
    ReasoningDomain coreD = new ReasoningDomain.Impl();
    coreD.addOntology(core);
    
    // isa relationships
    assertTrue(coreD.isTrue(OntoTools.ANY, OntoTools.ANY, OntoTools.IS_A));
    assertTrue(coreD.isTrue(OntoTools.RELATION, OntoTools.ANY, OntoTools.IS_A));
    assertTrue(coreD.isTrue(OntoTools.IS_A, OntoTools.IS_A, OntoTools.IS_A));
    assertTrue(coreD.isTrue(OntoTools.IS_A, OntoTools.RELATION, OntoTools.IS_A));
    assertTrue(coreD.isTrue(OntoTools.IS_A, OntoTools.ANY, OntoTools.IS_A));
    assertTrue(coreD.isTrue(OntoTools.PARTIAL_ORDER, OntoTools.RELATION, OntoTools.IS_A));
    assertFalse(coreD.isTrue(OntoTools.REFLEXIVE, OntoTools.SYMMETRIC, OntoTools.IS_A));
    assertFalse(coreD.isTrue(OntoTools.ANY, OntoTools.RELATION, OntoTools.IS_A));
    
    // hasa relationships
    assertTrue(coreD.isTrue(OntoTools.TRIPLE, OntoTools.SOURCE, OntoTools.HAS_A));
    assertFalse(coreD.isTrue(OntoTools.TRIPLE, OntoTools.REMOTE_TERM, OntoTools.HAS_A));
    assertTrue(coreD.isTrue(OntoTools.TRIPLE, OntoTools.ANY, OntoTools.HAS_A));
  }
}
