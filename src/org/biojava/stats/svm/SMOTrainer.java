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
 * Train a support vector machine using the Sequential Minimal
 * Optimization algorithm.  See Kernel Methods book.
 *
 * @author Thomas Down
 */

public class SMOTrainer implements TrainingContext {
    private double C = 1000;
    private double epsilon = 0.000001;

    // Working variables for the trainer: protected by the
    // synchronization on trainModel.

    private SVMModel model;
    private SVMKernel kernel;
    private double[] target;
    private double[] E;

    private TrainingListener listener;
    private int cycle = 0;

    private TrainingEvent ourEvent;

    public void setC(double C) {
	this.C = C;
    }

    public void setEpsilon(double epsilon) {
	this.epsilon = epsilon;
    }

    private boolean takeStep(int i1, int i2) {
	// System.out.print("+");

	if (i1 == i2)
	    return false;
	double alpha1 = model.getAlpha(i1) / target[i1];
	double alpha2 = model.getAlpha(i2) / target[i2];
	double y1 = target[i1];
	double y2 = target[i2];
	double E1 = getError(i1);
	double E2 = getError(i2);
	double s = y1 * y2;
	
	double L, H;
	if (y2 != y1) /* preferred (s<0) */ {
	    // targets in opposite directions
	    L = Math.max(0, alpha2 - alpha1);
	    H = Math.min(C, C + alpha2 - alpha1);
	} else {
	    // Equal targets.
	    L = Math.max(0, alpha1 + alpha2 - C);
	    H = Math.min(C, alpha1 + alpha2);
	}
	if (L == H) {
    //System.out.print("h");
    return false;
  }

	// double k11 = kernel.evaluate(model.getVector(i1), model.getVector(i1));
	// double k12 = kernel.evaluate(model.getVector(i1), model.getVector(i2));
	// double k22 = kernel.evaluate(model.getVector(i2), model.getVector(i2));
	double k11 = model.getKernelValue(i1, i1);
	double k12 = model.getKernelValue(i1, i2);
	double k22 = model.getKernelValue(i2, i2);
	double eta = 2 * k12 - k11 - k22;
	
	double a1 = 0, a2 = 0;
  if (eta > 0 && eta < epsilon) {
    eta = 0.0;
  }
  
	if (eta < 0) {
	    a2 = alpha2 - y2 * (E1 - E2) / eta;
	    if (a2 < L)
		a2 = L;
	    else if (a2 > H)
		a2 = H;
	} else {
	    //System.out.println("Positive eta!");

	    /*

	    double gamma = alpha1 + s*alpha2;
	    double v1 = model.classify(model.getVector(i1)) + model.getThreshold() - y1*alpha1*k11 - y2*alpha2*k12;
	    double v2 = model.classify(model.getVector(i2)) + model.getThreshold() - y1*alpha1*k12 - y2*alpha2*k22;

	    double Lobj = gamma - s * L + L - 0.5*k11*Math.pow(gamma - s*L,2) - 0.5*k22*Math.pow(L,2) - s*k12*(gamma-s*L)*L-y1*(gamma-s*L) - y1*(gamma - s*L)*v1 - y2*L*v2; 
	    double Hobj = gamma - s * H + H - 0.5*k11*Math.pow(gamma - s*H,2) - 0.5*k22*Math.pow(H,2) - s*k12*(gamma-s*H)*H-y1*(gamma-s*H) - y1*(gamma - s*H)*v1 - y2*H*v2;
	    if (Lobj > Hobj+epsilon)
		a2 = L;
	    else if (Lobj < Hobj-epsilon)
		a2 = H;
	    else
		a2 = alpha2;

	    */
      //System.out.print("+");
	    return false;
	}
	
	a1 = alpha1 + s*(alpha2 - a2);
	if (Math.abs(a1 - alpha1) < epsilon * (a1 + alpha1+1 +epsilon)) {
//    System.out.print("s");
    return false;
  }

	// Calculate new threshold
	
	double b;
	double bOLD = model.getThreshold();

  	if (0 < a1 && a1 < C) {
  	    // use "b1 formula"
	    // System.out.println("b1");
  	    b = E1 + y1*(a1 - alpha1)*k11 + y2*(a2 - alpha2)*k12 + bOLD;
  	} else if (0 < a2 && a2 < C) {
  	    // use "b2 formula"
  	    b = E2 + y1*(a1 - alpha1)*k12 + y2*(a2 - alpha2)*k22 + bOLD;
	    // System.out.println("b2");
  	} else {
	    // Both are at bounds -- use `half way' method.
	    double b1, b2;
	    b1 = E1 + y1*(a1 - alpha1)*k11 + y2*(a2 - alpha2)*k12 + bOLD;
	    b2 = E2 + y1*(a1 - alpha1)*k12 + y2*(a2 - alpha2)*k22 + bOLD;
	    // System.out.println("hybrid");
	    b = (b1 + b2) / 2.0;
	}
	model.setThreshold(b);
	model.setAlpha(i1, a1*y1);
	model.setAlpha(i2, a2*y2);

	// Update error cache

  E[i1] = model.internalClassify(i1) - target[i1];
  E[i2] = model.internalClassify(i2) - target[i2];

  for (int l = 0; l < E.length; ++l) {
    if (l==i1 || l==i2) {
  		continue;
    }
    if (!(isBound(model.getAlpha(l)))) {
  		E[l] += y1*(a1-alpha1)*model.getKernelValue(i1, l)
           +  y2*(a2-alpha2)*model.getKernelValue(i2, l)
           +  bOLD - b;
    }
 	}

	return true;
    }

    private int examineExample(int i2) {
	double y2 = target[i2];
	double alpha2 = model.getAlpha(i2) / y2;
	double E2 = getError(i2);
	double r2 = E2 * y2;

	if ((r2 < -epsilon && alpha2 < C) || (r2 > epsilon && alpha2 > 0)) {
	    int secondChoice = -1;
	    double step = 0.0;
	    for (int l = 0; l < model.size(); ++l) {
		if (!isBound(model.getAlpha(l) / target[l])) {
		    double thisStep = Math.abs(getError(l) - E2);
		    if (thisStep > step) {
			step = thisStep;
			secondChoice = l;
		    }
		}
	    }

	    if (secondChoice >= 0) {
		if (takeStep(secondChoice, i2)) {
		    return 1;
		}
	    }

  	    int randomStart = (int) Math.floor(Math.random() * model.size());
  	    for (int l = 0; l < model.size(); ++l) {
  		int i1 = (l + randomStart) % model.size();
  		if (!isBound(model.getAlpha(i1) / target[i1])) {
  		    if (takeStep(i1, i2))
  			return 1;
  		}
  	    }
	    // The second pass should look at ALL alphas, but
	    // we've already checked the non-bound ones.
	    for (int l = 0; l < model.size(); ++l) {
		int i1 = (l + randomStart) % model.size();
		if (isBound(model.getAlpha(i1) / target[i1])) {
		    if (takeStep(i1, i2))
			return 1;
		}
	    }
	} else {
    //System.out.print("/");
  }
	return 0;
    }

    private boolean isBound(double alpha) {
	return (alpha <= 0 || alpha >= C);
    }

    private double getError(int i) {
      double alpha = model.getAlpha(i) / target[i];
      if (isBound(alpha)) {
        return E[i] = model.internalClassify(i) - target[i];
      }
      return E[i];
    }

    public synchronized void trainModel(SVMModel m, double[] t, 
					TrainingListener l) 
    {
	model = m;
	target = t;
	kernel = m.getKernel();

	listener = l;
	ourEvent = new TrainingEvent(this);
	cycle = 0;

	E = new double[model.size()];
	for (int i = 0; i < model.size(); ++i) {
	    E[i] = model.internalClassify(i) - t[i];
  }
	int numChanged = 0;
	boolean examineAll = true;

	while (numChanged > 0 || examineAll) {
	    numChanged = 0;
	    if (examineAll) {
		// System.out.println("Running full iteration");
		for (int i = 0; i < model.size(); ++i) {
		    numChanged += examineExample(i);
		} 
	    } else {
		// System.out.println("Running non-bounds iteration");
		for (int i = 0; i < model.size(); ++i) {
		    double alpha = model.getAlpha(i) / target[i];
		    if (alpha != 0 && alpha != C) {
			numChanged += examineExample(i);
		    }
		}
	    }

  	    if (examineAll)
  		examineAll = false;
  	    else
  		examineAll = (numChanged == 0);
	    
	    ++cycle;
	    if (listener != null)
		listener.trainingCycleComplete(ourEvent);
	}

	if (listener != null)
	    listener.trainingComplete(ourEvent);

	E = null;
    }

    public int getCurrentCycle() {
	return cycle;
    }
}
