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
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

import java.util.*;

/**
 * Title:        DistributionTools.java
 * Description:  A class to hold static methods for calculations and
 * manipulations using Distributions.
 * @author Mark Schreiber
 */

public class DistributionTools {

  /**
   * Overide the constructer to prevent subclassing
   */
  private DistributionTools(){}

  /**
   * Compares the emission spectra of two distributions
   * @return true if alphabets and symbol weights are equal for the two distributions.
   * @throws IllegalAlphabetException if one or both of the Distributions are over infinite alphabets.
   * @since 1.2
   */
  public static final boolean areEmissionSpectraEqual(Distribution a, Distribution b)
    throws BioException{
      //are either of the Dists infinite
      if(a.getAlphabet() instanceof FiniteAlphabet == false
          || b.getAlphabet() instanceof FiniteAlphabet == false){
        throw new IllegalAlphabetException("Cannot compare emission spectra over infinite alphabet");
      }
      //are alphabets equal?
      if(!(a.getAlphabet().equals(b.getAlphabet()))){
        return false;
      }
      //are emissions equal?
      for(Iterator i = ((FiniteAlphabet)a.getAlphabet()).iterator();i.hasNext();){
        Symbol s = (Symbol)i.next();
        if(a.getWeight(s) != b.getWeight(s)) return false;
      }
      return true;
  }

  /**
   * Compares the emission spectra of two distribution arrays
   * @return true if alphabets and symbol weights are equal for each pair
   * of distributions. Will return false if the arrays are of unequal length.
   * @throws BioException if one of the Distributions is over an infinite
   * alphabet.
   * @since 1.3
   */
  public static final boolean areEmissionSpectraEqual(Distribution[] a,
                                                      Distribution[] b)
    throws BioException{
      if(a.length != b.length) return false;
      for (int i = 0; i < a.length; i++) {
        if(areEmissionSpectraEqual(a[i], b[i]) == false){
          return false;
        }
      }
      return true;
    }

  /**
   * A method to calculate the Kullback-Liebler Distance (relative entropy)
   *
   * @param logBase  - the log base for the entropy calculation. 2 is standard.
   * @return  - A HashMap mapping Symbol to <code>(Double)</code> relative entropy.
   * @since 1.2
   */
  public static final HashMap KLDistance(Distribution observed,
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
   * @since 1.2
   */
  public static final HashMap shannonEntropy(Distribution observed, double logBase){
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
   * @since 1.2
   */
  public static final double bitsOfInformation(Distribution observed){
    HashMap ent = shannonEntropy(observed, 2.0);
    double totalEntropy = 0.0;

    for(Iterator i = ent.values().iterator(); i.hasNext();){
      totalEntropy -= ((Double)i.next()).doubleValue();
    }
    int size = ((FiniteAlphabet)observed.getAlphabet()).size();
    return Math.log((double)size)/Math.log(2.0) - totalEntropy;
  }

  /**
   * Creates an array of distributions, one for each column of the alignment
   * @throws IllegalAlphabetException if all sequences don't use the same alphabet
   * @param countGaps if true gaps will be included in the distributions
   * @param nullWeight the number of pseudo counts to add to each distribution
   * @since 1.2
   */
  public static final Distribution[] distOverAlignment(Alignment a,
                                                 boolean countGaps,
                                                 double nullWeight)
  throws IllegalAlphabetException {

    List seqs = a.getLabels();
    FiniteAlphabet alpha = (FiniteAlphabet)((SymbolList)a.symbolListForLabel(seqs.get(0))).getAlphabet();
    for(int i = 1; i < seqs.size();i++){
        FiniteAlphabet test = (FiniteAlphabet)((SymbolList)a.symbolListForLabel(seqs.get(i))).getAlphabet();
        if(test != alpha){
          throw new IllegalAlphabetException("Cannot Calculate distOverAlignment() for alignments with"+
          "mixed alphabets");
        }
    }

    Distribution[] pos = new Distribution[a.length()];

    try{
      for(int i = 0; i < a.length(); i++){// For each position
        pos[i] = DistributionFactory.DEFAULT.createDistribution(alpha);
        DistributionTrainerContext dtc = new SimpleDistributionTrainerContext();
        dtc.setNullModelWeight(nullWeight);
        dtc.registerDistribution(pos[i]);

        for(Iterator j = seqs.iterator(); j.hasNext();){// of each sequence
          Object seqLabel = j.next();
          Symbol s = a.symbolAt(seqLabel,i + 1);
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
   * Creates an array of distributions, one for each column of the alignment.
   * No pseudo counts are used.
   * @param countGaps if true gaps will be included in the distributions
   * @throws IllegalAlphabetException if the alignment is not composed from sequences all
   *         with the same alphabet
   *
   * @since 1.2
   */
  public static final Distribution[] distOverAlignment(Alignment a,
                                                 boolean countGaps)
  throws IllegalAlphabetException {
    return distOverAlignment(a,countGaps,0.0);
  }

  /**
   * Averages two or more distributions. NOTE the current implementation ignore the null model.
   * @since 1.2
   */
  public static final Distribution average (Distribution [] dists){

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
