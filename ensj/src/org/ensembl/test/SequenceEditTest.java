/*
	Copyright (C) 2003 EBI, GRL

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

import org.ensembl.datamodel.SequenceEdit;
import org.ensembl.datamodel.impl.SequenceEditImpl;

/**
 * Sequence Edit Test.
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class SequenceEditTest extends TestCase {

  /**
   * Constructor for SequenceEditTest.
   * @param arg0
   */
  public SequenceEditTest(String arg0) {
    super(arg0);
  }

  private void checkApplyEdit(String source, String editOperation, String expected, int expectedLengthDiff) {
    SequenceEdit se = new SequenceEditImpl("code", "name", "desc", editOperation);
    String actual = se.applyEdit(source);
    assertEquals(expected, actual);
    assertEquals(expectedLengthDiff, se.lengthDiff());
  }

  public void test() {    
    
    // test insert before first base
    checkApplyEdit("ACTG", "1 0 CC", "CCACTG",  2);

    // test insert after last base
    checkApplyEdit("ACTG", "5 4 CC", "ACTGCC", 2);

    // test deletion of entire sequence
    checkApplyEdit("ACTG", "1 4", "", -4);
    
    // test replacement of some sequence
    checkApplyEdit("ACTG", "2 3 TC", "ATCG", 0);

  }
}
