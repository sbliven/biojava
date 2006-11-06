/*
 * Created on 13-Nov-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.ensembl.test;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ensembl.util.LruCache;

/**
 * @author arne
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LruTest extends TestCase {

  public static void main(String[] args) {
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(LruTest.class);
    return suite;
  }

  public LruTest(String name) {
    super(name);
  }

  public void testSingleKey() throws Exception {
    LruCache cache = new LruCache(3);
    for (int i = 0; i < 10; i++) {
      cache.put(new Long(i), new Integer(i));
    }

    // should contain 7,8,9
    for (int i = 0; i < 7; i++) {
      assertNull(cache.get(new Integer(i)));
    }
    for (int i = 7; i < 10; i++) {
      assertNotNull(cache.get(new Integer(i)));
    }

  }

  public void testMultiKey() {
    LruCache cache = new LruCache(3);
    cache.put(new Long(1), "1");
    cache.put(new Long(1), new Integer(1));
    int size = cache.listSize();
    assertTrue(size == 1);
    cache.put(new Long(2), "2");
    cache.put(new Long(2), new Integer(2));
    cache.put(new Long(3), "3");
    cache.get("1");
    cache.put(new Long(4), "4");
    // now 2 should be gone as oldest member
    assertNull(cache.get("2"));
    assertNull(cache.get(new Integer(2)));
    cache.clear();
    cache.put(new Long(5), "a5", "b5", "c5");
    assertNotNull(cache.get("a5"));
    assertEquals(cache.get("b5"), cache.get("c5"));
  }
  
  public void testFunnyBreak() {
  	LruCache cache = new LruCache( 10 );
  	cache.put(new Long(1), 1l);
  	cache.get(1l);
  	cache.put(new Long(2), 2l);
  	cache.get(1l);
  	cache.get(2l);
  	assertEquals( 2, cache.getSize() );
  	assertEquals( 2, cache.listSize() );
  	cache.put(new Long(3), 3l);
  	assertEquals( 3, cache.getSize() );
  	assertEquals( 3, cache.listSize() );
  	assertEquals(cache.get(1l), new Long(1l));
  	assertEquals(cache.get(3l), new Long(3l));
  	assertEquals(cache.get(2l), new Long(2l));
  	assertEquals(cache.get(2l), new Long(2l));
  	assertEquals(cache.get(3l), new Long(3l));
  	assertEquals(cache.get(1l), new Long(1l));
  }

  public void testRemoveMethods() {

    LruCache cache = new LruCache(3);
    Long one, two, three;
    cache.put(one = new Long(1), "1");
    cache.put(two = new Long(2), "2");
    cache.put(three = new Long(3), "3");
    assertEquals(3, cache.getSize());

    assertSame("Removed wrong object", two, cache.removeValue(two));
    assertEquals(2, cache.getSize());
    assertNotNull(cache.get("1"));
    try {
      assertNotNull(cache.get("2"));
      fail();
    } catch (AssertionFailedError e) {
    }
    assertNotNull(cache.get("3"));

    assertEquals("Removed wrong object (not equal)", new Long(3),cache.removeValueByKey("3"));
    assertEquals(1, cache.getSize());
    assertNotNull(cache.get("1"));
    try {
      assertNotNull(cache.get("3"));
      fail();
    } catch (AssertionFailedError e) {
    }

  }
}
