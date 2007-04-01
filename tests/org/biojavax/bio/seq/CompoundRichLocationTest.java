/*
 * CoumpoundRichLocationTest.java
 *
 * Created on April 1, 2007, 8:34 AM
 */

package org.biojavax.bio.seq;

import java.util.Collection;
import java.util.Vector;
import junit.framework.TestCase;
import org.biojavax.CrossRef;
import org.biojavax.SimpleCrossRef;
import org.biojavax.bio.seq.RichLocation.Strand;

/**
 *
 * @author George Waldon - equality
 */
public class CompoundRichLocationTest extends TestCase {

    protected void setUp() throws Exception {
        
    }

    protected void tearDown() throws Exception {
    }
    
   /**Test of equals method.
     * Compound locations are only equal to other Locations if all their
     * components are equal.
     */
    public void testEquals() {
        System.out.println("testEquals");
        Position p1 = new SimplePosition(23);
        Position p2 = new SimplePosition(36);
        Position p3 = new SimplePosition(44);
        Position p4 = new SimplePosition(55);
        CrossRef cr1 = new SimpleCrossRef("GenBank","A12345",3);
        
        SimpleRichLocation loc1 = new SimpleRichLocation(p1, p2, 0,Strand.POSITIVE_STRAND,cr1); 
        SimpleRichLocation loc2 = new SimpleRichLocation(p3, p4, 0,Strand.POSITIVE_STRAND,cr1); 
        Collection c1 = new Vector();
        c1.add(loc1);
        c1.add(loc2);
        
        RichLocation r1 = RichLocation.Tools.construct(c1);
        assertTrue(r1 instanceof CompoundRichLocation);
        
        RichLocation r2 = RichLocation.Tools.construct(c1);
        assertTrue(r2 instanceof CompoundRichLocation);
        assertTrue(r1.equals(r2));
        
        CrossRef cr2 = new SimpleCrossRef("another","A12345",3);
        SimpleRichLocation loc3 = new SimpleRichLocation(p3, p4, 0,Strand.POSITIVE_STRAND,cr2); 
        c1.remove(loc2);
        c1.add(loc3);
        RichLocation r3 = RichLocation.Tools.construct(c1);
        assertTrue(r3 instanceof CompoundRichLocation);
        assertFalse(r1.equals(r3));
    }
}
