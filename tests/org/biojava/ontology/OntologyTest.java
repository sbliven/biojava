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

import junit.framework.TestCase;

/**
 * Tests for Ontology.
 *
 * @author Thomas Down
 * @since 1.4
 */
public class OntologyTest extends TestCase {
    protected Ontology onto;
    protected Term widget;
    protected Term sprocket;
    protected Term machine;
    protected Term blueWidget;
    protected Term isa;
    protected Term partof;
    protected Term dummy;
    
    public OntologyTest(String name) {
        super(name);
    }
  
    protected void setUp() throws Exception {
        Ontology relations = new Ontology.Impl("relations", "Some standard relations");
        Term master_isa = relations.createTerm("is-a", "");
        Term master_partof = relations.createTerm("part-of", "");
        
        onto = new Ontology.Impl("test", "Test ontology");
        machine = onto.createTerm("machine", "A fancy machine");
        sprocket = onto.createTerm("sprocket", "");
        widget = onto.createTerm("widget", "");
        blueWidget = onto.createTerm("blueWidget", "");
        isa = onto.importTerm(master_isa);
        partof = onto.importTerm(master_partof);
        
        dummy = new Ontology.Impl("", "").createTerm("dummy", "Silly dummy term");
        
        onto.createTriple(sprocket, machine, partof);
        onto.createTriple(widget, machine, partof);
        onto.createTriple(blueWidget, widget, isa);
    }
  
    public void testGetBySubject() {
        assertEquals(onto.getTriples(widget, null, null).size(), 1);
        assertEquals(onto.getTriples(machine, null, null).size(), 0);
        assertEquals(onto.getTriples(dummy, null, null).size(), 0);
    }
    
    public void testGetByObject() {
        assertEquals(onto.getTriples(null, machine, null).size(), 2);
        assertEquals(onto.getTriples(null, widget, null).size(), 1);
        assertEquals(onto.getTriples(null, dummy, null).size(), 0);
    }
    
    public void testGetByRelation() {
        assertEquals(onto.getTriples(null, null, partof).size(), 2);
        assertEquals(onto.getTriples(null, null, isa).size(), 1);
        assertEquals(onto.getTriples(null, null, dummy).size(), 0);
    }
    
    public void testGetByMulti() {
        assertEquals(onto.getTriples(sprocket, machine, null).size(), 1);
        assertEquals(onto.getTriples(sprocket, machine, partof).size(), 1);
        assertEquals(onto.getTriples(sprocket, machine, isa).size(), 0);
    }
    
    public void testOntoToos_Core() {
      Ontology onto = OntoTools.getCoreOntology();
      
      onto.getTriples(null, null, null);
    }
    
    public void testCoreISA()
    throws OntologyException {
      Ontology onto = OntoTools.getCoreOntology();
      
      doIsaTest(OntoTools.IS_A, OntoTools.IS_A, true);
      doIsaTest(OntoTools.HAS_A, OntoTools.IS_A, false);
      doIsaTest(OntoTools.RELATION, OntoTools.ANY, true);
      doIsaTest(OntoTools.REFLEXIVE, OntoTools.RELATION, true);
      doIsaTest(OntoTools.EQUIVALENCE, OntoTools.REFLEXIVE, true);
      doIsaTest(OntoTools.EQUIVALENCE, OntoTools.RELATION, true);
      doIsaTest(OntoTools.EQUIVALENCE, OntoTools.ANY, true);
      doIsaTest(OntoTools.EQUIVALENCE, OntoTools.PARTIAL_ORDER, false);
    }
    
    private void doIsaTest(Term t1, Term t2, boolean yesno)
    throws OntologyException {
      boolean res = OntoTools.isa(t1, t2);
      assertEquals(
        "Terms: " + t1 + " , " + t2,
        yesno, res
      );
    }
    
    public void testSimpleISA()
    throws OntologyException{
      Ontology onto = OntoTools.getDefaultFactory().createOntology(
        "is-a tester",
        "Lets just get this is-a relationship stuff tested across ontologies"
      );
      
      
    }
}

