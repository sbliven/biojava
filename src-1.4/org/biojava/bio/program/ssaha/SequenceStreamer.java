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

package org.biojava.bio.program.ssaha;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.symbol.*;

public interface SequenceStreamer {
    public boolean hasNext();
    public void streamNext(SeqIOListener listener) throws BioException;
    public void reset() throws BioException;

    public static class SequenceDBStreamer implements SequenceStreamer {
	private SequenceDB seqDB;
	private SequenceIterator si;

	public SequenceDBStreamer(SequenceDB seqDB) {
	    this.seqDB = seqDB;
	    this.si = seqDB.sequenceIterator();
	}

	public boolean hasNext() {
	    return si.hasNext();
	}

	public void reset() {
	    si = seqDB.sequenceIterator();
	}

	public void streamNext(SeqIOListener listener)
	    throws BioException
	{
	    Sequence seq = si.nextSequence();
	    listener.startSequence();
	    listener.setName(seq.getName());
	    listener.setURI(seq.getURN());
	    Symbol[] syms = new Symbol[4096];
	    int pos = 1;
	    int spos = 0;
	    while (pos <= seq.length()) {
		syms[spos++] = seq.symbolAt(pos++);
		if (spos == syms.length || pos > seq.length()) {
		    listener.addSymbols(seq.getAlphabet(), syms, 0, spos);
		    spos = 0;
		}
	    }
	    listener.endSequence();
	}
    }
}
