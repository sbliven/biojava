package org.biojava.bio.seq.io;

import java.util.*;
import java.io.*;

import org.biojava.bio.symbol.*;

/**
 * General purpose SymbolList implementation which can efficiently capture
 * data from a SymbolReader.
 *
 * @author Thomas Down
 * @since 1.1 [newio proposal]
 */

class ChunkedSymbolList extends AbstractSymbolList {
    private Symbol[][] chunks;
    private final int chunkSize;

    private Alphabet alpha;
    private int length;

    public ChunkedSymbolList(Symbol[][] chunks,
			     int chunkSize,
			     int length,
			     Alphabet alpha) 
    {
	this.chunks = chunks;
	this.chunkSize = chunkSize;
	this.length = length;
	this.alpha = alpha;
    }
    
    public Alphabet getAlphabet() {
	return alpha;
    }

    public int length() {
	return length;
    }

    public Symbol symbolAt(int pos) {
	pos -= 1; // the inevitable...
	int chnk = pos / chunkSize;
	int spos = pos % chunkSize;
	return chunks[chnk][spos];
    }

    public SymbolList subList(int from, int to) {
	//
	// Mildly optimized for case where from and to are within
	// the same chunk.
	//	

	int afrom = from - 1;
	int ato = to - 1;
	int cfrom = afrom / chunkSize;
	if (ato / chunkSize == cfrom) {
	    return new SubArraySymbolList(chunks[cfrom],
					  to - from + 1,
					  afrom % chunkSize,
					  getAlphabet());
	} else {
	    return super.subList(from, to);
	}
    }


    private final static int CHUNK_SIZE = 1<<14;

    /**
     * Factory method which builds a ChunkedSymbolList or
     * SubArraySymbolList.
     */

    public static SymbolList make(SymbolReader sr)
        throws IOException, IllegalSymbolException
    {
	List chunkL = new ArrayList();
	Symbol[] headChunk = new Symbol[CHUNK_SIZE];
	int headChunkPos = 0;
	while (sr.hasMoreSymbols()) {
	    if (headChunkPos == CHUNK_SIZE) {
		chunkL.add(headChunk);
		headChunk = new Symbol[CHUNK_SIZE];
		headChunkPos = 0;
	    }
	    int read = sr.readSymbols(headChunk, headChunkPos, CHUNK_SIZE - headChunkPos);
	    headChunkPos += read;
	}
	if (headChunkPos > 0) {
	    if (headChunkPos < CHUNK_SIZE) {
		Symbol[] oldChunk = headChunk;
		headChunk = new Symbol[headChunkPos];
		System.arraycopy(oldChunk, 0, headChunk, 0, headChunkPos);
	    }
	    chunkL.add(headChunk);
	}

	if (chunkL.size() == 1) {
	    // Small-sequence optimization
	    
	    return new SubArraySymbolList((Symbol[]) chunkL.get(0),
					  headChunkPos,
					  0,
					  sr.getAlphabet());
	} else {
	    Symbol[][] chunks = new Symbol[chunkL.size()][];
	    for (int cnum = 0; cnum < chunkL.size(); ++cnum) {
		chunks[cnum] = (Symbol[]) chunkL.get(cnum);
	    }
	    int length = (chunkL.size() - 1) * CHUNK_SIZE + headChunkPos;
	    
	    return new ChunkedSymbolList(chunks,
					 CHUNK_SIZE,
					 length,
					 sr.getAlphabet());
	}
    }
}

