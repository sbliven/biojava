/*
 * @(#)NormalizingKernel.java      0.1 00/01/20
 *
 * By Thomas Down <td2@sanger.ac.uk>
 */

package org.biojava.stats.svm;

public class NormalizingKernel implements SVMKernel {
    private SVMKernel k;

    public void setNestedKernel(SVMKernel k) {
	this.k = k;
    }

    public SVMKernel getNestedKernel() {
	return k;
    }

    public double evaluate(Object a, Object b) {
      double kAA = k.evaluate(a, a);
      double kBB = k.evaluate(b, b);
      double kAB = k.evaluate(a, b);
      return kAB / Math.sqrt(kAA * kBB);
    }
    
    public String toString() {
      return "Normalizing Kernel K(x, y | k) = " +
             " k(x, y) / sqrt(k(x, x) * k(y, y)). k(x,y) = " +
             getNestedKernel().toString();
    }
}
