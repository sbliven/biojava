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

import java.util.*;

/**
 * An implementation of a sparse vector.
 * <P>
 * Memory is only allocated for dimensions that have non-zero values.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */
public class SparseVector {
    int size;
    int[] keys;
    double[] values;

    public SparseVector() {
      this(100);
    }

    public SparseVector(int capacity) {
      keys = new int[capacity];
      values = new double[capacity];
      Arrays.fill(keys, 0, capacity, Integer.MAX_VALUE);
      size = 0;
    }

    /**
     * The number of used dimensions.
     * <P>
     * This is the total number of non-zero dimensions. It is not equal to the
     * number of the highest indexed dimension.
     *
     * @return the number of non-zero dimensions
     */
    public int size() {
      return size;
    }
    
    /**
     * Set the value at a particular dimension.
     *
     * @param dim  the dimension to alter
     * @param value the new value
     */
    public void put(int dim, double value) {
      // find index of key nearest dim
      int indx = Arrays.binarySearch(keys, dim);
      
      if(indx >= 0) { // found entry for dim
        values[indx] = value;
      } else { // need to create entry for dim
        indx = -(indx + 1);

        System.out.println("indx  = " + indx);
        System.out.println("dim   = " + dim);
        System.out.println("value = " + value);
        if(indx < size) {
          System.out.println("current key = " + keys[indx]);
          System.out.println("current val = " + values[indx]);
          System.out.println("last key = " + keys[size-1]);
          System.out.println("last val = " + values[size-1]);
        }
        if ((size + 1) >= keys.length) { // growing arrays
          int[] nKeys = new int[keys.length * 2];
          System.arraycopy(keys, 0, nKeys, 0, indx);
          System.arraycopy(keys, indx, nKeys, indx+1, size-indx);
          Arrays.fill(nKeys, size+1, nKeys.length, Integer.MAX_VALUE);
          keys = nKeys;
          
          double[] nValues = new double[values.length * 2];
          System.arraycopy(values, 0, nValues, 0, indx);
          System.arraycopy(values, indx, nValues, indx+1, size-indx);
          values = nValues;
        } else {
          System.arraycopy(keys, indx, keys, indx+1, size-indx);
          System.arraycopy(values, indx, values, indx+1, size-indx);
        }

        keys[indx] = dim;
        values[indx] = value;
        ++size;
        
        if(indx < size) {
          System.out.println("new key = " + keys[indx+1]);
          System.out.println("new val = " + values[indx+1]);
          System.out.println("last key = " + keys[size-1]);
          System.out.println("last val = " + values[size-1]);
        }
      }
    }

    public double get(int dim) {
      int pos = Arrays.binarySearch(keys, dim);
      if (pos >= 0) {
        return values[pos];
      }
      return 0.0;
    }

    public int getDimAtIndex(int indx) {
      return keys[indx];
    }
    
    public double getValueAtIndex(int indx) {
      return values[indx];
    }
    
    public int maxIndex() {
      return keys[size - 1];
    }

    public static SparseVector normalLengthVector(SparseVector v, double length) {
	SparseVector n = new SparseVector(v.size);

	double oldLength = 0;
	for (int i = 0; i < v.size; ++i)
	    oldLength += v.values[i] * v.values[i];
	oldLength = Math.sqrt(oldLength);
	
	for (int i = 0; i < v.size; ++i)
	    n.put(v.keys[i], v.values[i] * length / oldLength);
	return n;
    }

    public static final SVMKernel kernel = new SVMKernel() {
      public double evaluate(Object o1, Object o2) {
        SparseVector a = (SparseVector) o1;
        SparseVector b = (SparseVector) o2;
        
        int ai=0, bi=0;
        double total = 0.0;
	    
  	    while (ai < a.size && bi < b.size) {
          if (a.keys[ai] > b.keys[bi]) {
            ++bi;
          } else if (a.keys[ai] < b.keys[bi]) {
            ++ai;
          } else {
            total += a.values[ai++] * b.values[bi++];
          }
        }
        return total;
      }
      
      public String toString() {
        return "SparseVector kernel K(x, y) = sum_i ( x_i * y_i ).";
      }
    };
    
    /**
     * A version of the standard dot-product kernel that scales each column
     * independantly.
     *
     * @author Matthew Pocock
     */
    public static class NormalizingKernel implements SVMKernel {
      /**
       * The sparse vector that performes the normalization.
       */
      private SparseVector s;
      
      /**
       * Retrive the current normalizing vector.
       *
       * @return the normalizing vector
       */
      public SparseVector getNormalizingVector() {
        return s;
      }
      
      /**
       * Set the normalizing vector.
       *
       * @param the new normalizing vector
       */
      public void setNormalizingVector(SparseVector nv) {
        s = nv;
      }
      
      /**
       * Evaluate the kernel function between two SparseVectors.
       * <P>
       * This function is equivalent to:
       * <br>
       * <code>k(a, b) = sum_i ( a_i * b_i * nv_i )</code>
       * <br>
       * where nv_i is the value of the normalizing vector at index i. This can
       * be thought of as scaling each vector at index i by
       * <code>sqrt(nv_i)</code>.
       */
      public double evaluate(Object o1, Object o2) {
        SparseVector a = (SparseVector) o1;
        SparseVector b = (SparseVector) o2;
        
        int ai=0, bi=0, si=0;
        double total = 0.0;
	    
  	    while (ai < a.size && bi < b.size && si < s.size) {
          if (a.keys[ai] < b.keys[bi] && a.keys[ai] < s.keys[si]) {
            ++ai;
          } else if (b.keys[bi] < a.keys[ai] && b.keys[ai] < s.keys[si]) {
            ++bi;
          } else if (s.keys[si] < a.keys[ai] && s.keys[si] < b.keys[bi]) {
            ++si;
          } else {
            total += a.values[ai++] * b.values[bi++] * s.values[si++];
          }
        }
        return total;
      }
      
      /**
       * Generate a normalizing kernel with the normalizing vector s.
       *
       * @param s the SparseVector to normalize by
       */
      public NormalizingKernel(SparseVector s) {
        this.s = s;
      }
      
      /**
       * Generate a normalizing kernel defined by the SparseVectors in vectors.
       * <P>
       * It will set up a normalizing vector that has weight that will scale
       * each element so that the average score is 1.
       */
      public NormalizingKernel(List vectors) {
        this.s = new SparseVector();
        
        for(Iterator i = vectors.iterator(); i.hasNext(); ) {
          SparseVector v = (SparseVector) i.next();
          for(int j = 0; j < v.size(); j++) {
            s.put(v.keys[j], s.get(v.keys[j]) + v.values[j]);
          }
        }
        
        for(int j = 0; j < s.size(); j++) {
          s.values[j] = (double) vectors.size() / s.values[j];
          s.values[j] *= s.values[j];
        }
      }

      public String toString() {
        return "SparseVector.NormalizingKernel K(x, y | s) = " +
               "sum_i ( x_i * y_i * s_i ).";
      }

    }
}
