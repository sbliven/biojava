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

import java.io.Serializable;
import java.util.*;

import org.biojava.bio.symbol.*;

/**
 * A no-frills implementation of DistributionTrainerContext.
 *
 * @author Matthew Pocock
 */
public class SimpleDistributionTrainerContext
implements DistributionTrainerContext, Serializable {
  private final Map distToTrainer;
  private final Set trainers;
  
  private Distribution nullModel;
  private double nullModelWeight;

  public Distribution getNullModel() {
    return this.nullModel;
  }
  
  public void setNullModel(Distribution nullModel) {
    this.nullModel = nullModel;
  }

  public double getNullModelWeight() {
    return this.nullModelWeight;
  }
  
  public void setNullModelWeight(double nullModelWeight) {
    this.nullModelWeight = nullModelWeight;
  }
  
  
  public void registerDistribution(Distribution dist) {
    if(!distToTrainer.keySet().contains(dist)) {
      dist.registerWithTrainer(this);
    }
  }
  
  public void registerDistributionTrainer(
    Distribution dist, DistributionTrainer trainer
  ) {
    distToTrainer.put(dist, trainer);
    trainers.add(trainer);
  }
  
  public DistributionTrainer getDistributionTrainer(Distribution dist) {
    return (DistributionTrainer) distToTrainer.get(dist);
  }

  public void addCount(Distribution dist, Symbol sym, double times)
  throws IllegalSymbolException {
    DistributionTrainer dt = getDistributionTrainer(dist);
    if(dt == null) {
      throw new NullPointerException(
        "No trainer associated with distribution " + dist
      );
    }
    dt.addCount(this, sym, times);
  }
  
  public void trainDistributions()
  throws IllegalSymbolException {
    for(Iterator i = trainers.iterator(); i.hasNext(); ) {
      ((DistributionTrainer) i.next()).train(getNullModel(), getNullModelWeight());
    }
  }
  
  public void clearDistributionCounts() {
    for(Iterator i = trainers.iterator(); i.hasNext(); ) {
      ((DistributionTrainer) i.next()).clearCounts();
    }
  }
  
  public SimpleDistributionTrainerContext() {
    this.distToTrainer = new HashMap();
    this.trainers = new HashSet();
  }
}
