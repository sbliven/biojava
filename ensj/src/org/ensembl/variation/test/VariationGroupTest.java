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
package org.ensembl.variation.test;

import java.util.List;

import org.ensembl.driver.AdaptorException;
import org.ensembl.variation.datamodel.Variation;
import org.ensembl.variation.datamodel.VariationGroup;

/**
 * Tests the Variation group adaptor.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class VariationGroupTest extends VariationBase {

  public VariationGroupTest(String name) throws Exception {
    super(name);
  }

  public void testFetchByID() throws AdaptorException {
    final long ID = 1;
    VariationGroup vg = vdriver.getVariationGroupAdaptor().fetch(ID);
    check(vg);
  }

  public void testFetchByName() throws AdaptorException {
    final String NAME = "DBMHC:ABDR";
    VariationGroup vg = vdriver.getVariationGroupAdaptor().fetch(NAME);
    check(vg);
  }

  public void testFetchByVariation() throws AdaptorException {
    final long ID = 794245;
    Variation v = vdriver.getVariationAdaptor().fetch(ID);
    assertNotNull("Test requires a valid Variation.internalID", v);
    List vgs = vdriver.getVariationGroupAdaptor().fetch(v);
    assertTrue(vgs.size()>0);
    for (int i = 0, n = vgs.size(); i < n; i++) 
      check((VariationGroup) vgs.get(i));
  }


  private void check(VariationGroup vg) {
    assertNotNull(vg);
    assertTrue(vg.getInternalID()>0);
    assertTrue(vg.getName().length()>0);
    assertTrue(vg.getSource().length()>0);
    // variation group not always associated with variations?
    //assertTrue(vg.getVariations().size()>0);
    
  }

}
