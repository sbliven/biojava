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
 * Simple state for representing matches in multiheaded HMMs.
 *
 * @author Thomas Down
 */

public class UniformMatchState implements EmissionState {
    private CrossProductAlphabet alpha;
    private FiniteAlphabet subAlpha;
    private int ungappedSize;

    private double matchWeight = 0.0;
    private double mismatchWeight = 0.0;
    private int[] advance;

    private String name;

    public UniformMatchState(Alphabet a) throws IllegalAlphabetException {
	this.alpha = (CrossProductAlphabet) a;
	Iterator i = alpha.getAlphabets().iterator();
	Alphabet sa = (Alphabet) i.next();
  if(! (sa instanceof FiniteAlphabet) ) {
    throw new IllegalAlphabetException(
      "Can't generate a UniformMatchState over infinite alphabet " +
      subAlpha.getName() + " of type " + subAlpha.getClass()
    );
  }
  subAlpha = (FiniteAlphabet) sa;
	while (i.hasNext()) {
	    if (i.next() != subAlpha)
		throw new IllegalAlphabetException("Sub-alphabets of " + alpha.getName() + " don't match");
	}
	
	ungappedSize = subAlpha.size();
	if (subAlpha.contains(AlphabetManager.instance().getGapSymbol()))
	    ungappedSize--;

	advance = new int[alpha.getAlphabets().size()];
	for (int l = 0; l < advance.length; ++l)
	    advance[l] = 1;
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
	if (srl.contains(AlphabetManager.instance().getGapSymbol()))
	    return Double.NEGATIVE_INFINITY;
	Iterator i = srl.iterator();
        Object first = i.next();
	while (i.hasNext())
	    if (i.next() != first)
		return mismatchWeight;
	return matchWeight;
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

    public void setMatchProbability(double p) {
	int nEmissions = (int) Math.pow(ungappedSize, alpha.getAlphabets().size());
	int nMatches = ungappedSize;
	int nMismatches = nEmissions - nMatches;
	
	matchWeight = Math.log(p / nMatches);
	mismatchWeight = Math.log((1-p) / nMismatches);
	System.out.println("Setting match probability "+p);
	System.out.println("matchWeight = " + matchWeight);
	System.out.println("mismatchWeight = " + mismatchWeight);
    }
}
