package org.biojava.stats.svm;

import java.util.*;

/**
 * Encapsulates a kernel that wraps another kernel up.
 *
 * @author Matthew Pocock
 */
public abstract class NestedKernel implements SVMKernel {
  /**
   * The <span class="type">SVMKernel</span> being wrapped.
   */
  private SVMKernel nested;

  /**
   * Create a new <span class="type">NestedKernel</span>.
   */
  public NestedKernel() {}

  /**
   * Create a new <span class="type">NestedKernel</span> that wraps
   * <span class="arg">k</span>.
   *
   * @param k  the <span class="type">SVMKernel</span> to wrap
   */
  public NestedKernel(SVMKernel k) {
    setNestedKernel(k);
  }

  /**
   * Set the <span class="type">SVMKernel</span> to nest to
   * <span class="arg">k</span>.
   *
   * @param k  the <span class="type">SVMKernel</span> to nest.
   */
  public void setNestedKernel(SVMKernel k) {
    nested = k;
  }

  /**
   * Retrieve the currently nested <span class="type">SVMKernel</span>.
   *
   * @param the nested <span class="type">SVMKernel</span>
   */
  public SVMKernel getNestedKernel() {
    return nested;
  }
}
