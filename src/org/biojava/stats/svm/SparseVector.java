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
 * @author Thomas Down
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
	size = 0;
    }

    public void put(int dim, double value) {
	if (size > 0 && dim <= keys[size - 1]) {
	    throw new UnsupportedOperationException();
	}
	
	if ((size + 1) >= keys.length) {
	    int[] nKeys = new int[keys.length * 2];
	    // System.arraycopy(keys, 0, nKeys, 0, size);
	    for (int c = 0; c < size; ++c)
		nKeys[c] = keys[c];
	    keys = nKeys;
	    double[] nValues = new double[values.length * 2];
	    for (int c = 0; c < size; ++c)
		nValues[c] = values[c];
	    // System.arraycopy(values, 0, nValues, 0, size);
	    values = nValues;
	} 

	keys[size] = dim;
	values[size] = value;
	++size;
    }

    public double get(int dim) {
	int pos = Arrays.binarySearch(keys, dim);
	if (pos >= 0)
	    return values[pos];
	return 0.0;
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
    };
}
