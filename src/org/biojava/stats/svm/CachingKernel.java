/*
 * @(#)CachingKernel.java      0.1 00/01/20
 *
 * By Thomas Down <td2@sanger.ac.uk>
 */

package org.biojava.stats.svm;

import java.util.*;

/**
 * Caches the results of a nested kernel so that k(a, b) need only be calculated
 * once.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */
public class CachingKernel extends NestedKernel {
    private Map cache;

    public CachingKernel() {
      cache = new HashMap();
    }

    public void setNestedKernel(SVMKernel k) {
      super.setNestedKernel(k);
      cache.clear();
    }

    public double evaluate(Object x, Object y) {
      ObjectPair op = new ObjectPair(x, y);
      Double d = (Double) cache.get(op);
      if (d == null) {
        d = new Double(getNestedKernel().evaluate(x, y));
        cache.put(op, d);
      }
      return d.doubleValue();
    }

    private static class ObjectPair {
      Object a;
      Object b;

      public ObjectPair(Object a, Object b) {
        this.a = a;
        this.b = b;
      }

      public boolean equals(Object x) {
        if (! (x instanceof ObjectPair))
          return false;
        ObjectPair op = (ObjectPair) x;
        return ((op.a == a && op.b == b) || 
		            (op.a == b && op.b == a));
      }

      public int hashCode() {
        return a.hashCode() + b.hashCode();
      }
    }
}
