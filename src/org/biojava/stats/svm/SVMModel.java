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

public class SVMModel {
    private SVMKernel kernel;
    private double threshold;

    private double[][] kvals;

    private Object[] vectors;
    private double[] alphas;
    private int size;

    public SVMModel() {
	this(100);
    }

    public SVMModel(int capacity) {
	vectors = new Object[capacity];
	alphas = new double[capacity];
	size = 0;
    }

    public SVMKernel getKernel() {
	return kernel;
    }

    public void setKernel(SVMKernel k) {
	kernel = k;
    }

    public double getThreshold() {
	return threshold;
    }

    public void setThreshold(double t) {
	threshold = t;
    }

    public void addVector(Object v, double alpha) {
	if ((size + 1) >= vectors.length) {
	    Object[] nVectors = new Object[vectors.length * 2];
	    System.arraycopy(vectors, 0, nVectors, 0, size);
	    vectors = nVectors;
	    double[] nAlphas = new double[alphas.length * 2];
	    System.arraycopy(alphas, 0, nAlphas, 0, size);
	    alphas = nAlphas;
	}

	vectors[size] = v;
	alphas[size] = alpha;
	size++;
    }

    public void addVector(Object v) {
	addVector(v, 0.0);
    }

    public int size() {
	return size;
    }

    public Object getVector(int pos) {
      if (pos >= size) {
        throw new NoSuchElementException();
      }
      return vectors[pos];
    }

    public double getAlpha(int pos) {
      if (pos >= size) {
        throw new NoSuchElementException();
      }
      return alphas[pos];
    }

    public void setAlpha(int pos, double a) {
	if (pos >= size) {
	    throw new NoSuchElementException();
	}
	alphas[pos] = a;
    }

    public double classify(Object v) {
	double delta=0;
	for (int i = 0; i < size; ++i) {
	    if (alphas[i] != 0)
		delta += alphas[i] * kernel.evaluate(vectors[i], v);
	}
	return delta - threshold;
    }

    public double internalClassify(int obj) {
	double delta=0;
	for (int i = 0; i < size; ++i) {
	    delta += alphas[i] * kvals[i][obj];
	}
	return delta - threshold;
    }

    public void calcKernel() {
      kvals = new double[size][size];

      for(int i = 0; i < size; i++) {
        for(int j = 0; j < i; j++) {
          kvals[i][j] = kvals[j][i] = kernel.evaluate(vectors[i], vectors[j]);
        }
        kvals[i][i] = kernel.evaluate(vectors[i], vectors[i]);
        System.out.print(".");
      }
    }

    public double getKernelValue(int i, int j) {
	return kvals[i][j];
    }
}
