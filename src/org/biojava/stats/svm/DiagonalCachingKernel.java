package org.biojava.stats.svm;

import java.util.*;

/**
 * Caches the leading diagonal of a kernel matrix.
 * <P>
 * Several kernels need to repeatedly access k(x,x) to do things like
 * normalization, or to calculate distances. This kernel wraps k so that these
 * leading diagonal elements do not need to be calculated each time.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */
public class DiagonalCachingKernel implements SVMKernel {
  /**
   * The kernel being cached.
   */
  private SVMKernel parent;
  
  /**
   * The cache of values.
   */
  private Map cache;

  /**
   * Create a new CachingKernel.
   */
  public DiagonalCachingKernel() {
    cache = new HashMap();
  }

  /**
   * Set the kernel to nest.
   * <P>
   * This will flush the cache.
   *
   * @param k  the kernel to nest.
   */
  public void setNestedKernel(SVMKernel k) {
    cache.clear();
    parent = k;
  }

  /**
   * Retrieve the currently nested kernel.
   *
   * @param the nested kernel
   */
  public SVMKernel getNestedKernel() {
    return parent;
  }

  /**
   * Returns the kernel product of two Objects.
   * <P>
   * This returns <code>getNestedKernel.evaluate(x, y)</code>. If
   * <code>x.equals(y)</code> then it will cache the result first time, and do
   * a hash table look up to retrieve the value in subsequent calls.
   */
  public double evaluate(Object x, Object y) {
    if(x.equals(y)) {
      Double d = (Double) cache.get(x);
      if (d == null) {
        d = new Double(parent.evaluate(x, x));
        cache.put(x, d);
      }
      return d.doubleValue();
    } else {
      return parent.evaluate(x, y);
    }
  }
}
