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


package org.biojava.stats.svm;

/**
 * This kernel computes all possible products of order features in feature
 * space. This is done by computing (a.k(i,j) + c)^order for some other kernel k
 * that defines a dot product in some feature space.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */
public class PolynomialKernel implements SVMKernel {
    private int order;
    private double a;
    private double c;
    private SVMKernel kernel;

    public PolynomialKernel() {
      order = 3;
      a = 1.0;
      c = 1.0;
      kernel = null;
    }

    public double evaluate(Object a, Object b) {
      return Math.pow(getMultiplier()*getWrappedKernel().evaluate(a, b)
                      + getConstant(),
                      getOrder());
    }

    public int getOrder() {
      return order;
    }

    public void setOrder(int o) {
      this.order = o;
    }

    public double getConstant() {
      return c;
    }

    public void setConstant(double c) {
      this.c = c;
    }
    
    public double getMultiplier() {
      return a;
    }
    
    public void setMultiplier(double m) {
      this.a = m;
    }

    public SVMKernel getWrappedKernel() {
      return kernel;
    }
    
    public void setWrappedKernel(SVMKernel kernel) {
      this.kernel = kernel;
    }
    
    public String toString() {
      return "Polynomial kernel K(x, k) = ("
        + getMultiplier() + ".k(x) + " + c + ")^" + order
        + ". k = " + getWrappedKernel().toString();
    }
}
