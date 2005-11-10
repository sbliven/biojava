/*
 * SimpleCommentTest.java
 * JUnit based test
 *
 * Created on November 10, 2005, 11:11 AM
 */

package org.biojavax;

import junit.framework.*;

/**
 *
 * @author Mark Schreiber
 */
public class SimpleCommentTest extends TestCase {
    SimpleComment comment;
    String com = "Test comment";
    int rank = 1;
    
    public SimpleCommentTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        comment = new SimpleComment(com, rank);
    }

    protected void tearDown() throws Exception {
        comment = null;
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SimpleCommentTest.class);
        
        return suite;
    }

    /**
     * Test of setComment method, of class org.biojavax.SimpleComment.
     */
    public void testSetComment() {
        System.out.println("testSetComment");
        String newCommentText = "new comment";
        comment.setComment(newCommentText);
        assertEquals(newCommentText, comment.getComment());
    }

    /**
     * Test of getComment method, of class org.biojavax.SimpleComment.
     */
    public void testGetComment() {
        System.out.println("testGetComment");
        
        assertEquals(com, comment.getComment());
    }

    /**
     * Test of getRank method, of class org.biojavax.SimpleComment.
     */
    public void testGetRank() {
        System.out.println("testGetRank");
        
        assertEquals(rank, comment.getRank());
    }

    /**
     * Test of equals method, of class org.biojavax.SimpleComment.
     */
    public void testEquals() {
        System.out.println("testEquals");
        
        Comment comment2 = new SimpleComment(com, rank);
        assertTrue(comment2.equals(comment));
        assertTrue(comment.equals(comment2));
        
        comment2 = new SimpleComment(com, 50);
        assertFalse(comment2.equals(comment));
        assertFalse(comment.equals(comment2));
    }

    /**
     * Test of compareTo method, of class org.biojavax.SimpleComment.
     */
    public void testCompareTo() {
        System.out.println("testCompareTo");
        
        Comment before = new SimpleComment(com, 0);
        Comment after = new SimpleComment(com, 10);
        Comment equal = new SimpleComment(com, rank);
        assertTrue(before.compareTo(comment) < 0);
        assertTrue(after.compareTo(comment) > 0);
        assertTrue(equal.compareTo(comment) == 0);
        assertTrue(comment.compareTo(before) > 0);
        assertTrue(comment.compareTo(equal) == 0);
        assertTrue(comment.compareTo(after) < 0);
    }

    /**
     * Test of hashCode method, of class org.biojavax.SimpleComment.
     */
    public void testHashCode() {
        System.out.println("testHashCode");
        
        Comment equal = new SimpleComment(com, rank);
        assertEquals(comment.hashCode(), equal.hashCode());
    }

    /**
     * Test of toString method, of class org.biojavax.SimpleComment.
     */
    public void testToString() {
        System.out.println("testToString");
        String expected = "(#"+rank+") "+com;
        assertEquals(expected, comment.toString());
    }
    
}
