/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */
package org.biojava.bio.dist;

import org.biojava.utils.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

import java.util.*;

/**
 * Title:        DistributionTools.java
 * Description:  A static class to hold static methods for calculations and
 * maniputlations using Distributions.
 *
 */

public class DistributionTools {

  /**
   * A method to calculate the Kullback-Liebler Distance (relative entropy)
   *
   * @param logBase  - the log base for the entropy calculation. 2 is standard.
   * @return  - A HashMap mapping Symbol to <code>(Double)</code> relative entropy.
   * @author Mark Schreiber
   * @since 1.2
   */
  public static HashMap KLDistance(Distribution observed,
                                   Distribution expected,
                                   double logBase){
    Iterator alpha = ((FiniteAlphabet)observed.getAlphabet()).iterator();
    HashMap kldist = new HashMap(((FiniteAlphabet)observed.getAlphabet()).size());

    while(alpha.hasNext()){
      Symbol s = (Symbol)alpha.next();
      try{
        double obs = observed.getWeight(s);
        double exp = expected.getWeight(s);
        if(obs == 0.0){
          kldist.put(s,new Double(0.0));
        }else{
          double entropy = obs * (Math.log(obs/exp))/Math.log(logBase);
          kldist.put(s,new Double(entropy));
        }
      }catch(IllegalSymbolException ise){
        ise.printStackTrace(System.err);
      }
    }
    return kldist;
  }

  /**
   * A method to calculate the Shannon Entropy for a Distribution
   *
   * @param logBase  - the log base for the entropy calculation. 2 is standard.
   * @return  - A HashMap mapping Symbol to <code>(Double)</code> entropy.
   * @author Mark Schreiber
   * @since 1.2
   */
  public static HashMap shannonEntropy(Distribution observed, double logBase){
    Iterator alpha = ((FiniteAlphabet)observed.getAlphabet()).iterator();
    HashMap entropy = new HashMap(((FiniteAlphabet)observed.getAlphabet()).size());

    while(alpha.hasNext()){
      Symbol s = (Symbol)alpha.next();
      try{
        double obs = observed.getWeight(s);
        if(obs == 0.0){
          entropy.put(s,new Double(0.0));
        }else{
          double e = obs * (Math.log(obs))/Math.log(logBase);
          entropy.put(s,new Double(e));
        }
      }catch(IllegalSymbolException ise){
        ise.printStackTrace(System.err);
      }
    }
    return entropy;
  }

  /**
   * Calculates the total bits of information for a distribution.
   * @author Mark Schreiber
   * @since 1.2
   */
  public static double bitsOfInformation(Distribution observed){
    HashMap ent = shannonEntropy(observed, 2.0);
    double totalEntropy = 0.0;

    for(Iterator i = ent.entrySet().iterator(); i.hasNext();){
      totalEntropy =+ ((Double)i.next()).doubleValue();
    }
    return 2.0 - totalEntropy;
  }

  /**
   * Creates an array of distributions, one for each column of the alignment
   * @param countGaps if true gaps will be included in the distributions
   * @author Mark Schreiber
   * @since 1.2
   */
  public static Distribution[] distOverAlignment(Alignment a, boolean countGaps){
    List seqs = a.getLabels();
    Distribution[] pos = new Distribution[a.length()];
    FiniteAlphabet dna = DNATools.getDNA();

    try{
      for(int i = 0; i < a.length(); i++){// For each position
        pos[i] = DistributionFactory.DEFAULT.createDistribution(dna);
        DistributionTrainerContext dtc = new SimpleDistributionTrainerContext();
        dtc.registerDistribution(pos[i]);

        for(Iterator j = seqs.iterator(); j.hasNext();){// of each sequence
          Object seqLabel = j.next();
          Symbol s = a.symbolAt(seqLabel,i);
          if(countGaps == false && s.equals(a.getAlphabet().getGapSymbol())){
            //do nothing, not counting gaps
          }else{
            dtc.addCount(pos[i],s,1.0);// count the symbol
          }
        }

        dtc.train();
      }
    }catch(Exception e){
      e.printStackTrace(System.err);
    }
    return pos;
  }

  /**
   * Averages two or more distributions. NOTE the current implementation ignore the null model.
   * @author Mark Schreiber
   * @since 1.2
   */
  public static Distribution average (Distribution [] dists){

    Alphabet alpha = dists[0].getAlphabet();
    //check if all alphabets are the same
    for (int i = 1; i < dists.length; i++) {
      if(!(dists[i].getAlphabet().equals(alpha))){
        throw new IllegalArgumentException("All alphabets must be the same");
      }
    }

    try{
      Distribution average = DistributionFactory.DEFAULT.createDistribution(alpha);
      DistributionTrainerContext dtc = new SimpleDistributionTrainerContext();
      dtc.registerDistribution(average);

      for (int i = 0; i < dists.length; i++) {// for each distribution
        for(Iterator iter = ((FiniteAlphabet)dists[i].getAlphabet()).iterator(); iter.hasNext(); ){//for each symbol
          Symbol sym = (Symbol)iter.next();
          dtc.addCount(average,sym,dists[i].getWeight(sym));
        }
      }


      dtc.train();
      return average;
    } catch(IllegalAlphabetException iae){//The following throw unchecked exceptions as they shouldn't happen
       throw new NestedError(iae,"Distribution contains an illegal alphabet");
    } catch(IllegalSymbolException ise){
       throw new NestedError(ise, "Distribution contains an illegal symbol");
    } catch(ChangeVetoException cve){
       throw new NestedError(cve, "The Distribution has become locked");
    }
  }
}//End of class