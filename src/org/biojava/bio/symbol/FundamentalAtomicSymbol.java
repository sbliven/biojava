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


package org.biojava.bio.symbol;

import java.util.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.*;

/**
 * An atomic symbol consisting only of itself.  This is the
 * fundamental type of symbol, and needs a special implementation.
 *
 * @author Thomas Down
 * @since 1.1
 */

public class FundamentalAtomicSymbol extends AbstractSymbol implements AtomicSymbol, Serializable {
    private final String name;
    private final char token;
    private final Annotation annotation;

    public FundamentalAtomicSymbol(String name, 
				   char token,
				   Annotation annotation)
    {
	this.name = name;
	this.token = token;
	this.annotation = annotation;
    }

    public String getName() {
	return name;
    }

    public char getToken() {
	return token;
    }

    public Alphabet getMatches() {
	return new SingletonAlphabet(this);
    }

    public List getSymbols() {
	return Collections.nCopies(1, this);
    }

    public Annotation getAnnotation() {
	return annotation;
    }
}
