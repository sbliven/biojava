package org.biojava.bio.dist;

import java.util.*;
import java.io.*;

import junit.framework.*;

import org.biojava.utils.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Tests that serilization works as advertised.
 *
 * @author Mark Schreiber
 * @since 1.21
 */
public class DistSerTest extends TestCase {
    private double a = 0.1;
    private double g = 0.3;
    private double c = 0.25;
    private double t = 0.35;

    private Distribution dist;
    private Distribution dist2;
    private Distribution uniform;
    private Distribution gap;
    private Distribution pair;
    private OrderNDistribution orderN;
    private Alphabet comp;

    public DistSerTest(String name){
	super(name);
    }

  protected void setUp() {
    // create distributions
    try {
      dist = DistributionFactory.DEFAULT.createDistribution(DNATools.getDNA());
      uniform = new UniformDistribution(DNATools.getDNA());
      gap = new GapDistribution(DNATools.getDNA());
      pair =  new PairDistribution(dist,gap);
      List l = Collections.nCopies(3,DNATools.getDNA());
      comp = AlphabetManager.getCrossProductAlphabet(l);
      orderN = (OrderNDistribution)OrderNDistributionFactory.DEFAULT.createDistribution(comp);
    } catch (IllegalAlphabetException iae) {
      throw new AssertionFailedError("Can't initialize test distributions " + iae.getMessage());
    }

    // set the weights in dist
    try {
      dist.setWeight(DNATools.a(), a);
      dist.setWeight(DNATools.g(), g);
      dist.setWeight(DNATools.c(), c);
      dist.setWeight(DNATools.t(), t);

      DistributionTrainerContext dtc = new SimpleDistributionTrainerContext();
      dtc.registerDistribution(orderN);
      for(Iterator i = ((FiniteAlphabet)comp).iterator(); i.hasNext();){
	  dtc.addCount(orderN,(Symbol)i.next(),1.0);
      }
      dtc.train();
    } catch (IllegalSymbolException ise) {
      throw new AssertionFailedError("Unable to set weights: "
      + ise.getMessage());
    } catch (ChangeVetoException cve) {
      throw new AssertionFailedError("Unable to set weights: "
      + cve.getMessage());
    }
  }

    public void testSimpleSerialization()throws Exception{
      File f = File.createTempFile("DistSerTest",".tmp");
      ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
      oos.writeObject(dist);
      oos.flush();
      oos.close();

      ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
      dist2 = (Distribution)ois.readObject();
      ois.close();

	assertTrue(DistributionTools.areEmissionSpectraEqual(dist, dist2));
        assertEquals(dist.getAlphabet(), dist2.getAlphabet());
    }
    
    public void testGapSerialization()throws Exception{
      File f = File.createTempFile("DistSerTest",".tmp");
      ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
      oos.writeObject(gap);
      oos.flush();
      oos.close();

      ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
      dist2 = (Distribution)ois.readObject();
      ois.close();

	assertTrue(DistributionTools.areEmissionSpectraEqual(gap, dist2));
    }
    
    public void testPairSerialization()throws Exception{
      File f = File.createTempFile("DistSerTest",".tmp");
      ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
      oos.writeObject(pair);
      oos.flush();
      oos.close();

      ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
      dist2 = (Distribution)ois.readObject();
      ois.close();

	assertTrue(DistributionTools.areEmissionSpectraEqual(pair, dist2));
    }

    public void testOrderNSerialization()throws Exception{
      File f = File.createTempFile("DistSerTest",".tmp");
      ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
      oos.writeObject(orderN);
      oos.flush();
      oos.close();

      ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
      dist2 = (Distribution)ois.readObject();
      ois.close();

      assertTrue(orderN.getAlphabet()== dist2.getAlphabet());
      assertTrue(orderN.getConditionedAlphabet() == ((OrderNDistribution)dist2).getConditionedAlphabet());
      assertTrue(orderN.getConditioningAlphabet() == ((OrderNDistribution)dist2).getConditioningAlphabet());

      for(Iterator i = ((FiniteAlphabet)orderN.getConditioningAlphabet()).iterator(); i.hasNext();){
	Symbol s = (Symbol)i.next();
	assertTrue(DistributionTools.areEmissionSpectraEqual(orderN.getDistribution(s), orderN.getDistribution(s)));
      }
      assertTrue(orderN.conditionedDistributions().size() == ((OrderNDistribution)dist2).conditionedDistributions().size());
      for(Iterator i = ((FiniteAlphabet)orderN.getConditioningAlphabet()).iterator(); i.hasNext();){
	Symbol s = (Symbol)i.next();
	assertTrue(DistributionTools.areEmissionSpectraEqual(((OrderNDistribution)dist2).getDistribution(s),
                                                              ((OrderNDistribution)dist2).getDistribution(s)));
      }
      for(Iterator i = ((FiniteAlphabet)orderN.getConditioningAlphabet()).iterator(); i.hasNext();){
	Symbol s = (Symbol)i.next();
	assertTrue(DistributionTools.areEmissionSpectraEqual(orderN.getDistribution(s), ((OrderNDistribution)dist2).getDistribution(s)));
      }
    }

}
