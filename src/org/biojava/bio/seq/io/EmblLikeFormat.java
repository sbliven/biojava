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

import java.io.*;
import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Format processor for handling EMBL records and similar
 * files.  This takes a very simple approach: all
 * `normal' attribute lines are passed to the listener
 * as a tag (first two characters) and a value (the
 * rest of the line from the 6th character onwards).
 * Any data between the special `SQ' line and the
 * "//" entry terminator is passed as a SymbolReader.
 *
 * <p>
 * This low-level format processor should normally be
 * used in conjunction with one or more `filter' objects,
 * such as EmblProcessor.
 * </p>
 *
 * <p>
 * Many ideas borrowed from the old EmblFormat processor
 * by Thomas Down and Thad Welch.
 * </p>
 *
 * @author Thomas Down
 * @since 1.1
 */

public class EmblLikeFormat implements SequenceFormat, Serializable {
    public void readSequence(StreamReader.Context context,
			     SymbolParser resParser,
			     SeqIOListener listener)
	throws IllegalSymbolException, IOException
    {
	final BufferedReader in = context.getReader();
	String line;

	listener.startSequence();

	while ((line = in.readLine()) != null) {
	    if (line.startsWith("//")) {
		in.mark(2);
		if (in.read() == -1)
		    context.streamEmpty();
		else
		    in.reset();
		
		listener.endSequence();
		return;
	    } else if (line.startsWith("SQ")) {
		EmblSymbolReader esr = new EmblSymbolReader(resParser, in);
		Symbol[] buffer = new Symbol[256];
		while (esr.hasMoreSymbols()) {
		    int num = esr.readSymbols(buffer, 0, buffer.length);
		    try {
			listener.addSymbols(esr.getAlphabet(), buffer, 0, num);
		    } catch (IllegalAlphabetException ex) {
			throw new IOException("Fussy SeqIOListener");
		    }
		}
	    } else {
		// Normal attribute line
		String tag = line.substring(0, 2);
		String rest = null;
		if (line.length() > 5) {
		    rest = line.substring(5);
		}
		listener.addSequenceProperty(tag, rest);
	    }
	}

	throw new IOException("Premature end of stream for EMBL");
    }

    /**
     * Simple SymbolReader implementation for EMBL files and the like.  This is
     * currently based on the /old/ NFastaFormat SymbolReader, and could
     * probably be made more efficient in lots of ways.
     *
     * @author Thomas Down
     * @since 1.1
     */

    private class EmblSymbolReader implements SymbolReader {
	private String symbolCache;
	private boolean theEnd;

	private SymbolParser parser;
	private BufferedReader br;

	public EmblSymbolReader(SymbolParser p, BufferedReader br) {
	    parser = p;
	    this.br = br;
	}

	public Alphabet getAlphabet() {
	    return parser.getAlphabet();
	}

	public Symbol readSymbol() throws IOException, IllegalSymbolException {
	    if (symbolCache == null)
		symbolCache = readSymbolLine();
	    if (symbolCache == null)
		throw new IOException("Attempting to read beyond end of EMBL sequence.");
	    Symbol s = parser.parseToken(symbolCache.substring(0, 1));
	    symbolCache = (symbolCache.length() == 1 ? null : symbolCache.substring(1));
	    return s;
	}

	public int readSymbols(Symbol[] buffer,
			       int pos,
			       int length)
	    throws IOException, IllegalSymbolException
	{
	    if (symbolCache == null)
		symbolCache = readSymbolLine();
	    if (symbolCache == null)
		throw new IOException("Attempting to read beyond end of EMBL sequence.");
	    int i = 0;
	    int scl = symbolCache.length();
	    while (i < length && i < scl) {
		buffer[pos + i] = parser.parseToken(symbolCache.substring(i, i+1));
		++i;
	    }
	    if (i == scl)
		symbolCache = null;
	    else
		symbolCache = symbolCache.substring(i);

	    return i;
	}

	public boolean hasMoreSymbols() {
	    try {
		if (symbolCache == null)
		    symbolCache = readSymbolLine();
		return (symbolCache != null);
	    } catch (IOException ex) {
		return false;
	    }
	}

	private String readSymbolLine() 
	    throws IOException
	{
	    if (theEnd)
		return null;

	    br.mark(20);
	    String line = br.readLine();
	    if (line == null)
		throw new IOException("Premature end of EMBL-like file");

	    if (line.startsWith("//")) {
		br.reset();
		theEnd = true;
		return null;
	    } else {
		StringBuffer sb = new StringBuffer();
		StringTokenizer toke = new StringTokenizer(line);
		while (toke.hasMoreTokens()) {
		    String token = toke.nextToken();
		    if (!Character.isDigit(token.charAt(0)))
			sb.append(token);
		}
		return sb.toString();
	    }
	}
    }

    /**
     * This is not implemented. It does not write anything to the stream.
     */

    public void writeSequence(Sequence seq, PrintStream os)
	throws IOException 
    {
	throw new RuntimeException("Can't write in EMBL format...");
    }
}
