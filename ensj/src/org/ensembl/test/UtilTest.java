/*
    Copyright (C) 2001 EBI, GRL

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.ensembl.test;

import junit.framework.TestCase;

import org.ensembl.util.Util;

/**
 * The point of this class is ...
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class UtilTest extends TestCase {

  public UtilTest(String arg0) {
    super(arg0);
  }

  public void testBatch() {
    long[] arr = {};
    long[][] out = Util.batch(arr,1); 
    assertTrue(out.length==1);
    assertTrue(out[0].length==0);
    
    arr = new long[]{};
    out = Util.batch(arr,1000); 
    assertEquals(1, out.length);
    assertEquals(0, out[0].length);
    
    arr = new long[]{1};
    out = Util.batch(arr,1);
    assertEquals(1, out.length);
    assertEquals(1, out[0].length);
    assertEquals(1, out[0][0]);
    
    arr = new long[]{1,2};
    out = Util.batch(arr,1);
    assertEquals(2, out.length);
    assertEquals(1, out[0].length);
    assertEquals(1, out[1].length);
    assertEquals(1, out[0][0]);
    assertEquals(2, out[1][0]);
    
    arr = new long[]{1,2,3,4,5,6,7};
    out = Util.batch(arr,3);
    assertEquals(3, out.length);
    assertEquals(3, out[0].length);
    assertEquals(1, out[0][0]);
    assertEquals(2, out[0][1]);
    assertEquals(3, out[0][2]);
    assertEquals(3, out[1].length);
    assertEquals(6, out[1][2]);
    assertEquals(1, out[2].length);
    assertEquals(7, out[2][0]);
    
    arr = new long[]{1,2,3,4,5,6,7};
    out = Util.batch(arr,1000012);
    assertEquals(7, arr.length);
    assertEquals(arr[4],out[0][4]);
    
  }
}
