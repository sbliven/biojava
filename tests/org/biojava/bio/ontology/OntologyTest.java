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
public class OntologyTest
extends TestCase {
  public OntologyTest(String name) {
    super(name);
  }
  
  // fixme: this needs splitting into multiple tests
  // we need a basic ontology to do tests over
  public void testProperties()
  throws OntologyException, AlreadyExistsException, ChangeVetoException
  {
    String name = "Tester";
    String description = "Our Description";
    
    Ontology onto = OntoTools.getDefaultFactory().createOntology(name, description);
    Term isa = onto.importTerm(OntoTools.IS_A);
    Term animal = onto.createTerm("Animal", "An animal");
    Term fish = onto.createTerm("Fish", "A swimming, cold-blooded thingy");
    Term mamal = onto.createTerm("Mamal", "A milk-producing quadraped");
    Term human = onto.createTerm("Human", "Us");
    
    Triple fish_isa_animal = onto.createTriple(fish, animal, isa);
    Triple mamal_isa_animal = onto.createTriple(mamal, animal, isa);
    Triple human_isa_mamal = onto.createTriple(human, mamal, isa);
    
    // basic properties
    assertEquals(onto.getName(), name);
    assertEquals(onto.getDescription(), description);
    
    // terms
    assertEquals(onto.getTerms().size(), 5);
    assertTrue(onto.getTerms().contains(animal));
    assertTrue(onto.getTerms().contains(fish));
    assertTrue(onto.getTerms().contains(mamal));
    assertTrue(onto.getTerms().contains(human));
    
    // terms by name
    assertEquals(onto.getTerm("Animal"), animal);
    assertEquals(onto.getTerm("Human"), human);
    
    // triples
    assertEquals(onto.getTriples(null, null, null).size(), 3);
    assertTrue(onto.getTriples(null, null, null).contains(fish_isa_animal));
    assertTrue(onto.getTriples(null, null, null).contains(mamal_isa_animal));
    assertTrue(onto.getTriples(null, null, null).contains(human_isa_mamal));
    
    // triple searching
    assertEquals(onto.getTriples(null, animal, null).size(), 2);
    assertEquals(onto.getTriples(null, mamal, null).size(), 1);
    assertEquals(onto.getTriples(null, human, null).size(), 0);
    assertEquals(onto.getTriples(null, mamal, isa), onto.getTriples(human, null, isa));
    assertEquals(onto.getTriples(human, mamal, isa).size(), 1);
    assertEquals(onto.getTriples(human, animal, isa).size(), 0);
  }
  
}

