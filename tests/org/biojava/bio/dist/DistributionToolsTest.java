package org.biojava.bio.dist;

import java.util.*;
import java.io.*;

import junit.framework.*;

import org.biojava.utils.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Tests that methods from DistributionTools work as advertised.
 *
 * @author Mark Schreiber
 * @since 1.3
 */
public class DistributionToolsTest extends TestCase {

  private Alignment a;

  public DistributionToolsTest(String name) {
    super(name);
  }
  
  protected void setUp() {
    try{	  
      String[] sa = {"CATTGGG","AATTGGC","AATTGGG","AATTGGC","AATTGGG","AATTGGC",
        "AATTGGG","AATTGGC","AATTGGG","AATTGGC"};

      Map map = new HashMap(sa.length);
      for (int i = 0; i < sa.length; i++) {
         map.put(new Integer(i), DNATools.createDNA(sa[i]));
      }

      a = new SimpleAlignment(map);
    }catch(Exception e){
      e.printStackTrace();    
    }
  }
  
  
  public void testDistOverAlignment() {
    try{
      Distribution[] d = DistributionTools.distOverAlignment(a,false);
      Distribution[] d2 = DistributionTools.distOverAlignment(a,false,10.0);
      Distribution[] d3 = DistributionTools.distOverAlignment(a,true,10.0);
    
      assertTrue(d[0].getWeight(DNATools.a()) == 0.9);
      assertTrue(d[0].getWeight(DNATools.c()) == 0.1);
      assertTrue(d[0].getWeight(DNATools.g()) == 0.0);
      assertTrue(d[0].getWeight(DNATools.t()) == 0.0);
     
      assertTrue(d2[1].getWeight(DNATools.a()) == 0.625);
      assertTrue(d2[1].getWeight(DNATools.c()) == 0.125);
      assertTrue(d2[1].getWeight(DNATools.g()) == 0.125);
      assertTrue(d2[1].getWeight(DNATools.t()) == 0.125);
    
      for (int i = 0; i < d2.length; i++) {
        assertTrue(DistributionTools.areEmissionSpectraEqual(d2[i],d3[i]));
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }

}
