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


package org.biojava.bio.dp;

import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * Simple state for representing gaps in multiheaded HMMs.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */

public class UniformGapState implements EmissionState {
    private final static Symbol GAP;

    static {
	GAP = AlphabetManager.instance().getGapSymbol();
    }

    private final CrossProductAlphabet alpha;
    private final FiniteAlphabet subAlpha;
    private int ungappedSize;
    private double gapScore;

    private int[] advance;

    private String name;

    public UniformGapState(Alphabet a, int[] advance) throws IllegalAlphabetException {
	this.alpha = (CrossProductAlphabet) a;
	Iterator i = alpha.getAlphabets().iterator();
	Alphabet sa = (Alphabet) i.next();

  if(! (sa instanceof FiniteAlphabet)) {
    throw new IllegalAlphabetException("Alphabet " + sa.getName() + " is not finite");
  }
  subAlpha = (FiniteAlphabet) sa;
	while (i.hasNext()) {
	    if (i.next() != subAlpha)
		throw new IllegalAlphabetException("Sub-alphabets of " + alpha.getName() + " don't match.");
	}
	
	if (! subAlpha.contains(GAP)) {
	    throw new IllegalAlphabetException("Alphabet " + subAlpha.getName() + " does not contain the Gap special Symbol.");
	}

	if (advance.length != alpha.getAlphabets().size())
	    throw new IllegalAlphabetException("Advance array doesn't match number of dimensions in Alphabet " + alpha); 

	ungappedSize = subAlpha.size() - 1;
	this.advance = advance;
	int numGaps = 0;
	for (int l = 0; l < advance.length; ++l)
	    if (advance[l] == 0)
		numGaps++;
	if (numGaps == 0)
	    throw new IllegalAlphabetException("No gaps!");
	gapScore = Math.log(1.0 / Math.pow(ungappedSize, advance.length - numGaps));
    }

    public int[] getAdvance() {
	return advance;
    }

    public Alphabet alphabet() {
	return alpha;
    }

    public double getWeight(Symbol r) throws IllegalSymbolException {
	alpha.validate(r);
	List srl = ((CrossProductSymbol) r).getSymbols();
	for (int i = 0; i < advance.length; ++i) {
	    Symbol sr = (Symbol) srl.get(i);
	    if (sr==GAP && advance[i] > 0)
		return Double.NEGATIVE_INFINITY;
	    if (sr != GAP && advance[i] == 0)
		return Double.NEGATIVE_INFINITY;
	}
	return gapScore;
    }

    public void setWeight(Symbol r, double w) throws IllegalSymbolException, UnsupportedOperationException
    {
	throw new UnsupportedOperationException();
    }

    public Symbol sampleSymbol() {
	throw new UnsupportedOperationException();
    }

    public void registerWithTrainer(ModelTrainer mt) {
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public char getToken() {
	return name.charAt(0);
    }

    public Annotation getAnnotation() {
	return Annotation.EMPTY_ANNOTATION;
    }
}

