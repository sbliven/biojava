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

import java.util.Arrays;
import java.util.List;

import org.ensembl.util.IDSet;
import org.ensembl.variation.datamodel.Allele;
import org.ensembl.variation.datamodel.ValidationState;
import org.ensembl.variation.datamodel.Variation;



/**
 * Tests the variation adaptor.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class VariationTest extends VariationBase {

  public VariationTest(String name) throws Exception {
    super(name);
  }

  public void testFetchByID() throws Exception {
        
    Variation v = vdriver.getVariationAdaptor().fetch(2);
    checkVariation(v);
  }

  private void checkVariation(Variation v) {
    
    assertNotNull(v);
    assertTrue(v.getInternalID()>0);
    
    assertTrue(v.getAlleles().size()>0);
    assertTrue(v.getAlleles().get(0) instanceof Allele);
    
    Allele a = (Allele) v.getAlleles().get(0);
    assertTrue(a.getAlleleString().length()>0);
    // frequency can be 0
    //assertTrue(a.getFrequency()>0);
    assertTrue(a.getInternalID()>0);

    
    // some alleles don't have a population
    //assertNotNull("Allele " +a.getInternalID()+ "missing a population",a.getPopulation());
    //Population p = a.getPopulation();
    //assertTrue(p.getInternalID()>0);
    
    assertTrue(v.getSynonyms().size()>0);
    assertTrue(v.getSynonyms().get(0) instanceof String);
    
    assertTrue(v.getSynonymSources().size()>0);
    assertTrue(v.getSynonymSources().get(0) instanceof String);
        
    if (v.getValidationStates().size()>0);
      assertTrue(v.getValidationStates().get(0) instanceof ValidationState);
      
    assertNotNull(v.getFivePrimeFlankingSeq());
    assertTrue(v.getFivePrimeFlankingSeq().length()>0);
    assertNotNull(v.getThreePrimeFlankingSeq());
    assertTrue(v.getThreePrimeFlankingSeq().length()>0);
    
    
  }

  public void testFetchByName() throws Exception {
    String name = "rs3";
    Variation v = vdriver.getVariationAdaptor().fetch(name);
    checkVariation(v);
    assertEquals(name,v.getName());
  }

  
  public void testFetchByInternalIDs() throws Exception {
    
    long[] ids = {1,2,3};
    List vs = vdriver.getVariationAdaptor().fetch(ids);
    
    assertNotNull(vs);
    assertEquals(ids.length, vs.size());
    
    IDSet set = new IDSet();
    for (int i = 0, n = vs.size(); i < n; i++) 
      set.add((Variation) vs.get(i)); 
    long[] ids2 = set.to_longArray();
    
    Arrays.sort(ids);
    Arrays.sort(ids2);
    for (int i = 0, n = ids.length; i < n; i++) 
      assertEquals(ids[i],ids2[i]);
  }

}
