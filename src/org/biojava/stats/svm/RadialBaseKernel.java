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
 * This kernel computes the radial base kernel that corresponds to a gausian
 * distribution. 
 *
 * @author Matthew Pocock
 */
public class RadialBaseKernel implements SVMKernel {
    private double width;
    private SVMKernel kernel;

    public RadialBaseKernel() {
      width = 1.0;
      kernel = null;
    }

    public double evaluate(Object a, Object b) {
      SVMKernel k = getKernel();
      double w = getWidth();
      return Math.exp(-Math.abs(2.0 * k.evaluate(a, b) - k.evaluate(a, a) -
                                k.evaluate(b, b)
                               ) / ( w * w ));
    }

    public double getWidth() {
      return width;
    }

    public void setWidth(double w) {
      this.width = width;
    }

    public SVMKernel getKernel() {
      return kernel;
    }
    
    public void setKernel(SVMKernel kernel) {
      this.kernel = kernel;
    }
    
    public String toString() {
      return "Radial base kernel K(x, k) = exp(-abs(k(x,x) - k(y,y)) / ("
        + getWidth() + "^2)"
        + ". k = " + getKernel().toString();
    }
}
