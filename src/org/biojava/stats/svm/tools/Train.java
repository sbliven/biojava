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


/*
 * @(#)Train.java      0.1 00/01/15
 *
 * By Thomas Down <td2@sanger.ac.uk>
 */

package org.biojava.stats.svm.tools;

import java.io.*;
import java.util.*;

import org.biojava.stats.svm.*;

public class Train {
    public static void main(String[] args) throws Throwable {
	if (args.length != 2) {
	    throw new Exception("usage: stats.svm.tools.Classify <train_examples> <model_file>");
	}
	String trainFile = args[0];
	String modelFile = args[1];

	List examples = new ArrayList();
	BufferedReader r = new BufferedReader(new FileReader(trainFile));
	String line;

	while ((line = r.readLine()) != null) {
	    if (line.length() == 0 || line.startsWith("#"))
		continue;
	    examples.add(SVM_Light.parseExample(line));
	}
	r.close();

	SVMModel model = new SVMModel(examples.size());
	double[] target = new double[examples.size()];
	for (int i = 0; i < examples.size(); ++i) {
	    SVM_Light.LabelledVector ex = (SVM_Light.LabelledVector) examples.get(i);
	    model.addVector(ex.getVector());
	    target[i] = ex.getLabel();
	}


	PolynomialKernel k = new PolynomialKernel();
	k.setOrder(4);
	k.setNestedKernel(SparseVector.kernel);
	model.setKernel(SparseVector.kernel);
	System.out.println("Calculating kernel...");
	model.calcKernel();
	SMOTrainer trainer = new SMOTrainer();
	trainer.setEpsilon(0.01);
	TrainingListener tl = new TrainingListener() {
	    public void trainingCycleComplete(TrainingEvent e) {
		System.out.print('.');
	    }
	    public void trainingComplete(TrainingEvent e) {
		System.out.println("");
	    }
	} ;
	System.out.println("Training");
	trainer.trainModel(model, target, tl);
	System.out.println("Done");

	for (int i=0; i < model.size(); ++i) {
	    System.out.println(target[i] + "\t" + model.classify(model.getVector(i)) + "    (" + model.getAlpha(i) + ")");
	}

	SVM_Light.writeModelFile(model, modelFile);
    }
}
