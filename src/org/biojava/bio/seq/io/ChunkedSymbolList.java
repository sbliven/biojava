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

import org.biojava.utils.*;
import org.biojava.bio.symbol.*;

/**
 * General purpose SymbolList implementation which can efficiently capture
 * data from a SymbolReader.
 *
 * @author Thomas Down
 * @since 1.1 [newio proposal]
 */

class ChunkedSymbolList extends AbstractSymbolList implements Serializable {
    private Symbol[][] chunks;
    private final int chunkSize;

    private Alphabet alpha;
    private int length;

    protected void finalize() throws Throwable {
        super.finalize();
        alpha.removeChangeListener(ChangeListener.ALWAYS_VETO, Alphabet.SYMBOLS);
    }

    public ChunkedSymbolList(Symbol[][] chunks,
                             int chunkSize,
                             int length,
                             Alphabet alpha)
    {
        this.chunks = chunks;
        this.chunkSize = chunkSize;
        this.length = length;
        this.alpha = alpha;
        alpha.addChangeListener(ChangeListener.ALWAYS_VETO, Alphabet.SYMBOLS);
    }

    public Alphabet getAlphabet() {
        return alpha;
    }

    public int length() {
        return length;
    }

    public Symbol symbolAt(int pos) {
      try {
        --pos; // the inevitable...
        int chnk = pos / chunkSize;
        int spos = pos % chunkSize;
        return chunks[chnk][spos];
      } catch (IndexOutOfBoundsException ioobe) {
        ++pos;
        throw new IndexOutOfBoundsException("Attempted to access symbol at "
        + pos + " of ChunkedSymbolList length " + length);
      }
    }

    public SymbolList subList(int start, int end) {
        if (start < 1 || end > length()) {
          throw new IndexOutOfBoundsException(
                  "Sublist index out of bounds " + length() + ":" + start + "," + end
          );
      }

      if (end < start) {
        throw new IllegalArgumentException(
            "end must not be lower than start: start=" + start + ", end=" + end
        );
      }

      //
      // Mildly optimized for case where from and to are within
      // the same chunk.
      //

      int afrom = start - 1;
      int ato = end - 1;
      int cfrom = afrom / chunkSize;
      if (ato / chunkSize == cfrom) {
          return new SubArraySymbolList(chunks[cfrom],
                                                    end - start + 1,
                                        afrom % chunkSize,
                                        getAlphabet());
      } else {
          return super.subList(start, end);
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

