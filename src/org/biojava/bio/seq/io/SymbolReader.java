package org.biojava.bio.seq.io;

import org.biojava.bio.symbol.*;
import java.io.*;

/**
 * Encapsulate a stream of Symbols being parsed from some input
 * stream.  This allows SymbolList creation to be fully decoupled from
 * stream parsing.
 *
 * @author Thomas Down
 * @since 1.1 [newio proposal]
 */

public interface SymbolReader {
    /**
     * Find the alphabet of all symbols which may be returned by
     * this SymbolReader.  <strong>NOTE:</strong> SymbolList
     * implementations are expected to perform any necessary
     * validation of returned Symbols.  Client code should
     * not need to perform any extra validation.
     */

    public Alphabet getAlphabet();

    /**
     * Return a single symbol from the stream.
     *
     * @throws IOException if an error occured on the stream, or the
     *                     end of the stream has already been reached.
     * @throws IllegalSymbolException if a parse error occured.
     */

    public Symbol readSymbol() throws IOException, IllegalSymbolException;

    /**
     * Read one or more symbols from the stream.
     *
     * @param buffer the destination for read symbols.
     * @param start a start offset within the buffer.
     * @param length the maximum number of Symbols to read.
     * 
     * @return the number of Symbols which were actually read.
     *
     * @throws IOException if an error occured on the stream, or the
     *                     end of the stream has already been reached.
     * @throws IllegalSymbolException if a parse error occured.
     */

    public int readSymbols(Symbol[] buffer,
			   int start,
			   int length) throws IOException, 
			                       IllegalSymbolException;

    /**
     * Determine if there are more symbols left to read in this stream.
     */

    public boolean hasMoreSymbols();
}
