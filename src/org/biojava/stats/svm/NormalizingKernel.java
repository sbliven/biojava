/*
 * @(#)NormalizingKernel.java      0.1 00/01/20
 *
 * By Thomas Down <td2@sanger.ac.uk>
 */

package org.biojava.stats.svm;

/**
 * Performs a normalization on the results of a nested kernel.
 * <P>
 * This is equivalent to making the locations in feature space of the nested
 * kernel unit vectors lying on a unit sphere. The dot product in feature space
 * then becomes just <code>cos theta</code> rather than
 * <code>||a|| * ||b|| * cos theta</code> as both lengths are 1. The length of
 * a in the feature space of kernel k is <code>sqrt( k(a, a) )</code>, so that
 * the normalizing kernel ends up calculating
 * <code>k(a, b) / sqrt( k(a, a) * k(b, b) )</code>.
 * <P>
 * As the values of k(x, x) are required repeatedly, it may be worth making the
 * nested kernel a DiagonalCachingKernel.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */
public class NormalizingKernel extends NestedKernel {
  public NormalizingKernel() {}
  
  public NormalizingKernel(SVMKernel k) {
    setNestedKernel(k);
  }
  
    public double evaluate(Object a, Object b) {
      SVMKernel k = getNestedKernel();
      double kAA = k.evaluate(a, a);
      double kBB = k.evaluate(b, b);
      double kAB = k.evaluate(a, b);
      return kAB / Math.sqrt(kAA * kBB);
    }
    
    public String toString() {
      return "Normalizing Kernel K(x, y | k) = " +
             " k(x, y) / sqrt(k(x, x) * k(y, y)); k(x,y) = " +
             getNestedKernel().toString();
    }
}
