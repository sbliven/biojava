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

package org.biojava.bio.seq.tools;

import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;

/**
 * ResidueList which gives a view of the reverse complement of
 * another ResidueList.  The parent ResidueList must use
 * either the DNA or DNA-AMBIGUITY alphabet, as provided
 * by the DNATools utility class.
 *
 * @author Thomas Down
 */

public class ComplementResidueList implements ResidueList {
    private ResidueList parent;

    public ComplementResidueList(ResidueList p) throws IllegalAlphabetException {
	Alphabet a = p.alphabet();
	if (a != DNATools.getAlphabet() && a != DNATools.getAmbiguity())
	    throw new IllegalAlphabetException("Only DNA can be complemented.");
	parent = p;
    }

    public Alphabet alphabet() {
	return parent.alphabet();
    }

    public Iterator iterator() {
	return new ResidueIterator(1, length());
    }

    public int length() {
	return parent.length();
    }

    public Residue residueAt(int pos) {
	try {
	    return DNATools.complement(parent.residueAt(parent.length() - pos + 1));
	} catch (IllegalResidueException ex) {
	    throw new BioError(ex);
	}
    }

    public ResidueList subList(int start, int end) {
	return new SubList(start, end);
    }

    public List toList() {
	return new ListView(this);
    }

    class ResidueIterator implements Iterator {
	private int min, max;
        private int pos;
	    
	public ResidueIterator(int min, int max) {
	    this.min = min;
	    this.max = max;
	    pos = min;
	}

	public boolean hasNext() {
	    return (pos <= max);
	}

	public Object next() {
	    if (pos > max)
		throw new NoSuchElementException();
	    return residueAt(pos++);
	}

	public void remove() {
	    throw new UnsupportedOperationException();
	}
    }

    class SubList implements ResidueList {
	private int start, end;

	public SubList(int start, int end) {
	    this.start = start;
	    this.end = end;
	}

	public Alphabet alphabet() {
	    return ComplementResidueList.this.alphabet();
	}

	public Iterator iterator() {
	    return new ResidueIterator(start, end);
	}

	public int length() {
	    return end - start + 1;
	}

	public Residue residueAt(int pos) {
	    return ComplementResidueList.this.residueAt(pos + start - 1);
	}

	public ResidueList subList(int sstart, int send) {
	    return new SubList(sstart + start - 1, sstart + end - 1);
	}

	public List toList() {
	    return new ListView(this);
	}
    }

    private static class ListView extends AbstractList {
	private ResidueList rl;

	ListView(ResidueList rl) {
	    this.rl = rl;
	}

	public int size() {
	    return rl.length();
	}
	
	public Object get(int pos) {
	    return rl.residueAt(pos + 1);
	}
    }
}
