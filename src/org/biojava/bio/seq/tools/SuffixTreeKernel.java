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

package org.biojava.bio.seq.tools;

import org.biojava.bio.seq.*;
import org.biojava.stats.svm.SVMKernel;

/**
 * Computes the dot-product of two suffix-trees as the sum of the products
 * of the counts of all nodes they have in common.
 * <P>
 * This implementation allows you to scale the sub-space for each word length.
 *
 * @author Matthew Pocock
 */
public class SuffixTreeKernel implements SVMKernel {
  private DepthScaler depthScaler = new UniformScaler();
  
  public DepthScaler getDepthScaler() {
    return depthScaler;
  }
  
  public void setDepthScaler(DepthScaler depthScaler) {
    this.depthScaler = depthScaler;
  }
  
  public double evaluate(Object a, Object b) {
    SuffixTree st1 = (SuffixTree) a;
    SuffixTree st2 = (SuffixTree) b;
    SuffixTree.SuffixNode n1 = st1.getRoot();
    SuffixTree.SuffixNode n2 = st2.getRoot();
      
    return dot(n1, n2, st1.alphabet().size(), 0);
  }
    
  private double dot(SuffixTree.SuffixNode n1,
                     SuffixTree.SuffixNode n2, int size, int depth)
  {
    double scale = getDepthScaler().getScale(depth);
    double dot = n1.getNumber() * n2.getNumber() * scale * scale;
    for(int i = 0; i < size; i++) {
      if(n1.hasChild(i) && n2.hasChild(i)) {
        dot += dot(n1.getChild(i), n2.getChild(i), size, depth+1);
      }
    }
    return dot;
  }
    
  public String toString() {
    return new String("Suffix tree kernel");
  }
  
  /**
   * Encapsulates the scale factor to apply at a given depth.
   *
   * @author Matthew Pocock
   */
  public interface DepthScaler {
    /**
     * Retrieve the scaling factor at a given depth
     *
     * @param depth  word length
     * @return the scaling factor for the subspace at that length
     */
    double getScale(int depth);
  }
  
  /**
   * Scales by 4^depth - equivalent to dividing by a flat null model
   */
  public class NullModelScaler implements DepthScaler {
    public double getScale(int depth) {
      return Math.pow(4.0, (double) depth);
    }
  }
  
  /**
   * Scale all depths by 1.0
   */
  public class UniformScaler implements DepthScaler {
    public double getScale(int depth) {
      return 1.0;
    }
  }
}
