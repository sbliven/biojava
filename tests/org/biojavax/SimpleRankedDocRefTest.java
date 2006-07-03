/*
 * SimpleRankedDocRefTest.java
 * JUnit based test
 *
 * Created on 12 November 2005, 15:43
 */

package org.biojavax;

import java.util.Collections;
import junit.framework.*;

/**
 *
 * @author Mark Schreiber
 */
public class SimpleRankedDocRefTest extends TestCase {
    DocRef dr;
    SimpleRankedDocRef ref;
    SimpleRankedDocRef ref2;
    int rank = 1;
    Integer start;
    Integer end;
    
    public SimpleRankedDocRefTest(String testName) {
        super(testName);
        start = new Integer(1);
        end = new Integer(25);
        dr = new SimpleDocRef(Collections.singletonList(
                new SimpleDocRefAuthor("Hubert Hubertson", false, false)), "Journal of Voodoo Virology", "Viruses, what are they good for?");
    }

    protected void setUp() throws Exception {
        ref = new SimpleRankedDocRef(dr, start, end, rank);
    }

    protected void tearDown() throws Exception {
        ref = null;
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SimpleRankedDocRefTest.class);
        
        return suite;
    }

    /**
     * Test of getRank method, of class org.biojavax.SimpleRankedDocRef.
     */
    public void testGetRank() {
        System.out.println("testGetRank");
        
        assertEquals(rank, ref.getRank());
    }

    /**
     * Test of getDocumentReference method, of class org.biojavax.SimpleRankedDocRef.
     */
    public void testGetDocumentReference() {
        System.out.println("testGetDocumentReference");
        
        assertEquals(dr, ref.getDocumentReference());
    }

    /**
     * Test of getStart method, of class org.biojavax.SimpleRankedDocRef.
     */
    public void testGetStart() {
        System.out.println("testGetStart");
        
        assertEquals(start, ref.getStart());
    }

    /**
     * Test of getEnd method, of class org.biojavax.SimpleRankedDocRef.
     */
    public void testGetEnd() {
        System.out.println("testGetEnd");
        
        assertEquals(end, ref.getEnd());
    }

    /**
     * Test of equals method, of class org.biojavax.SimpleRankedDocRef.
     */
    public void testEquals() {
        System.out.println("testEquals");
        
        assertTrue(ref.equals(ref));
        assertFalse(ref.equals(new Object()));
        assertFalse(ref.equals(null));
        //Two ranked document references are equal if they have the same rank 
        //and refer to the same document reference.
        ref2 = new SimpleRankedDocRef(dr, start, end, 1); //equal
        assertTrue(ref.equals(ref2));
        assertTrue(ref2.equals(ref));
        
        ref2 = new SimpleRankedDocRef(dr, new Integer(30), new Integer(60), 1); //equal
        assertTrue(ref.equals(ref2));
        assertTrue(ref2.equals(ref));
        
        ref2 = new SimpleRankedDocRef(dr, start, end, 100); //not equal
        assertFalse(ref.equals(ref2));
        assertFalse(ref2.equals(ref));
        
        ref2 = new SimpleRankedDocRef(new SimpleDocRef(
                Collections.singletonList(new SimpleDocRefAuthor("Rev. Falliwell", false, false)), 
                "Kansas Journal of Creationism", "Un-intelligent design"), start, end, 1); //not equal
        assertFalse(ref.equals(ref2));
        assertFalse(ref2.equals(ref));
    }

    /**
     * Test of compareTo method, of class org.biojavax.SimpleRankedDocRef.
     */
    public void testCompareTo() {
        System.out.println("testCompareTo");
        
        assertTrue(ref.compareTo(ref) == 0);

        //Two ranked document references are equal if they have the same rank 
        //and refer to the same document reference.
        ref2 = new SimpleRankedDocRef(dr, start, end, 1); //equal
        assertTrue(ref.compareTo(ref2) == 0);
        assertTrue(ref2.compareTo(ref) == 0);
        
        ref2 = new SimpleRankedDocRef(dr, new Integer(30), new Integer(60), 1); //equal
        assertTrue(ref.compareTo(ref2) == 0);
        assertTrue(ref2.compareTo(ref) == 0);
        
        ref2 = new SimpleRankedDocRef(dr, start, end, 100); //not equal
        assertTrue(ref.compareTo(ref2) < 0);
        assertTrue(ref2.compareTo(ref) > 0);
        
        ref2 = new SimpleRankedDocRef(new SimpleDocRef(
                Collections.singletonList(new SimpleDocRefAuthor("Rev. Falliwell", false, false)), 
                "Kansas Journal of Creationism", "Evidence for the giant spaghetti monster"), start, end, 1); //not equal
        assertTrue(ref.compareTo(ref2) < 0);
        assertTrue(ref2.compareTo(ref) > 0);
    }

    /**
     * Test of hashCode method, of class org.biojavax.SimpleRankedDocRef.
     */
    public void testHashCode() {
        System.out.println("testHashCode");
        
        ref2 = new SimpleRankedDocRef(dr, start, end, 1); //equal
        assertTrue(ref.hashCode() == ref2.hashCode());
        
        ref2 = new SimpleRankedDocRef(dr, new Integer(30), new Integer(60), 1); //equal
        assertTrue(ref.hashCode() == ref2.hashCode());
    }

    /**
     * Test of toString method, of class org.biojavax.SimpleRankedDocRef.
     */
    public void testToString() {
        System.out.println("testToString");
        
        String expected = "(#"+rank+") "+dr;
        assertEquals(expected, ref.toString());
    }
}
