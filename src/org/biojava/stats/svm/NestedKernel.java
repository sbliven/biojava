package org.biojava.stats.svm;

import java.util.*;

/**
 * Encapsulates a kernel that wraps another kernel up.
 *
 * @author Matthew Pocock
 */
public abstract class NestedKernel implements SVMKernel {
  /**
   * The kernel being cached.
   */
  private SVMKernel nested;

  /**
   * Create a new nested kernel.
   */
  public NestedKernel() {}

  /**
   * Create a new nested kernel that wraps a kernel.
   */
  public NestedKernel(SVMKernel k) {
    setNestedKernel(k);
  }

  /**
   * Set the kernel to nest.
   * <P>
   * This will flush the cache.
   *
   * @param k  the kernel to nest.
   */
  public void setNestedKernel(SVMKernel k) {
    nested = k;
  }

  /**
   * Retrieve the currently nested kernel.
   *
   * @param the nested kernel
   */
  public SVMKernel getNestedKernel() {
    return nested;
  }
}
