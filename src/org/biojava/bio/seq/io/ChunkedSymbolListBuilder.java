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

package org.biojava.bio.seq.io;

import java.util.*;
import java.io.*;

import org.biojava.bio.symbol.*;

/**
 * @author Thomas Down
 */
public class ChunkedSymbolListBuilder {
    private final static int CHUNK_SIZE = 1<<12;

    private List chunkL = new ArrayList();
    private Symbol[] headChunk = null;
    private int headChunkPos = 0;

    private Alphabet alpha = null;
    
    public void addSymbols(Alphabet alpha,
			   Symbol[] syms,
			   int pos,
			   int len)
	throws IllegalAlphabetException
    {
	if (this.alpha == null) {
	    this.alpha = alpha;
	} else {
	    if (this.alpha != alpha) {
		throw new IllegalAlphabetException("Alphabet changed!");
	    }
	}

	if (headChunk == null) {
	    headChunk = new Symbol[CHUNK_SIZE];
	    headChunkPos = 0;
	}

	int ipos = 0;
	while (ipos < len) {
	    if (headChunkPos == CHUNK_SIZE) {
		chunkL.add(headChunk);
		headChunk = new Symbol[CHUNK_SIZE];
		headChunkPos = 0;
	    }
	    int read = Math.min(len - ipos, CHUNK_SIZE - headChunkPos);
	    System.arraycopy(syms, pos + ipos, headChunk, headChunkPos, read);
	    ipos += read;
	    headChunkPos += read;
	}
    }

    public SymbolList makeSymbolList() {
	if (headChunkPos > 0) {
	    if (headChunkPos < CHUNK_SIZE) {
		Symbol[] oldChunk = headChunk;
		headChunk = new Symbol[headChunkPos];
		System.arraycopy(oldChunk, 0, headChunk, 0, headChunkPos);
	    }
	    chunkL.add(headChunk);
	}

	if (chunkL.size() == 0) {
	    // Really boring case.

	    return SymbolList.EMPTY_LIST;
	} else if (chunkL.size() == 1) {
	    // Small-sequence optimization
	    
	    return new SubArraySymbolList((Symbol[]) chunkL.get(0),
					  headChunkPos,
					  0,
					  alpha);
	} else {
	    Symbol[][] chunks = new Symbol[chunkL.size()][];
	    for (int cnum = 0; cnum < chunkL.size(); ++cnum) {
		chunks[cnum] = (Symbol[]) chunkL.get(cnum);
	    }
	    int length = (chunkL.size() - 1) * CHUNK_SIZE + headChunkPos;
	    
	    return new ChunkedSymbolList(chunks,
					 CHUNK_SIZE,
					 length,
					 alpha);
	}
    }
}
