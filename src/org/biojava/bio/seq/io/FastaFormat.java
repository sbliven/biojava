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
import java.net.*;

import org.biojava.utils.StaticMemberPlaceHolder;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;


/**
 * Format for Fasta files.
 * <P>
 * The description lines often include complicated annotation for sequences.
 * The parsing of these is handled by a FastaDescriptionReader object.</p>
 * 
 * <p>This version included the experimental new input system by thomasd</p>
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */

public class FastaFormat implements SequenceFormat, Serializable {
    /**
     * Constant string which is the property key used to notify
     * listeners of the description lines of FASTA sequences.
     */

    public final static String PROPERTY_DESCRIPTIONLINE = "description_line";
  
    /**
     * The line width for output.
     */
    private int lineWidth = 60;

    /**
     * Retrive the current line width.
     *
     * @return the line width
     */

    public int getLineWidth() {
	return lineWidth;
    }

    /**
     * Set the line width.
     * <P>
     * When writing, the lines of sequence will never be longer than the line
     * width.
     *
     * @param width the new line width
     */

    public void setLineWidth(int width) {
	this.lineWidth = lineWidth;
    }

    public void readSequence(StreamReader.Context context,
			     SymbolParser resParser,
			     SeqIOListener siol)
	throws IllegalSymbolException, IOException 
    {
	final BufferedReader in = context.getReader();
    
	String line = in.readLine();
	if(line == null) {
	    throw new IOException("File ended prematurely");
	}

	siol.startSequence();
    
	String description = line.substring(1).trim();
	siol.addSequenceProperty(PROPERTY_DESCRIPTIONLINE, description);

	FASymbolReader fasr = new FASymbolReader(resParser, in);
	Symbol[] buffer = new Symbol[256];
	while (fasr.hasMoreSymbols()) {
	    int num = fasr.readSymbols(buffer, 0, buffer.length);
	    try {
		siol.addSymbols(fasr.getAlphabet(), buffer, 0, num);
	    } catch (IllegalAlphabetException ex) {
		throw new IOException("Fussy SeqIOListener");
	    }
	}

	siol.endSequence();
    
	if (fasr.hasSeenEOF()) {
	    context.streamEmpty();
	} 
    }

    private class FASymbolReader implements SymbolReader {
	private char[] cache;
	private int cacheMax;
	private int cacheMark;

	private boolean theEnd;
	private boolean seenEOF;

	private SymbolParser parser;
	private BufferedReader br;

	public FASymbolReader(SymbolParser p, BufferedReader br) {
	    parser = p;
	    this.br = br;

	    cache = new char[256];
	    cacheMax = cacheMark = 0;
	}

	public boolean hasSeenEOF() {
	    return seenEOF;
	}

	public Alphabet getAlphabet() {
	    return parser.getAlphabet();
	}

	public Symbol readSymbol() throws IOException, IllegalSymbolException {
	    if (cacheMark >= cacheMax)
		readMoreSymbols();
	    if (cacheMark >= cacheMax)
		throw new IOException("Attempting to read beyond end of FASTA sequence.");
	    Symbol s = parser.parseToken(new String(cache, cacheMark, 1));
	    cacheMark += 1;
	    return s;
	}

	public int readSymbols(Symbol[] buffer,
			       int pos,
			       int length)
	    throws IOException, IllegalSymbolException
	{
	    if (cacheMark >= cacheMax)
		readMoreSymbols();
	    if (cacheMark >= cacheMax)
		throw new IOException("Attempting to read beyond end of FASTA sequence.");
	    int i = 0;
	    int scl = cacheMax - cacheMark;
	    while (i < length && i < scl) {
		buffer[pos + i] = parser.parseToken(new String(cache, cacheMark + i, 1));
		++i;
	    }
	    cacheMark += i;

	    return i;
	}

	public boolean hasMoreSymbols() {
	    try {
		if (cacheMark >= cacheMax)
		    readMoreSymbols();
		return !(cacheMark >= cacheMax);
	    } catch (IOException ex) {
		return false;
	    }
	}

	private void readMoreSymbols()
	    throws IOException
	{
	    cacheMark = cacheMax = 0;
	    if (theEnd)
		return;

	    char[] tempCache = new char[256];
	    br.mark(cache.length);
	    int bytesRead = br.read(tempCache, 0, tempCache.length);
	    if (bytesRead < 0) {
		theEnd = seenEOF = true;
		cacheMax = 0;
		return;
	    } 

	    for (int i = 0; i < bytesRead; ++i) {
		char c = tempCache[i];

		if (c == '>') {
		    theEnd = true;
		    br.reset();
		    if (br.skip(i) != i)
			throw new IOException("Couldn't reset to start of next sequence");
		    return;
		} else if (c != '\n') {
		    cache[cacheMax++] = c;
		}
	    }

	    if (cacheMax == 0 && !theEnd)
		readMoreSymbols();
	}
    }

    protected String writeDescription(Sequence seq) {
	String description = null;
	try {
	    description = seq.getAnnotation().getProperty(PROPERTY_DESCRIPTIONLINE).toString();
	} catch (NoSuchElementException ex) {
	    description = seq.getName();
	}
	return description;
    }

    public void writeSequence(Sequence seq, PrintStream os) {
	os.print(">");
	os.println(writeDescription(seq));

	int length = seq.length();
	for(int i = 1; i <= length; i++) {
	    os.print(seq.symbolAt(i).getToken());
	    if( (i % lineWidth) == 0)
		os.println();
	}
	if( (length % lineWidth) != 0)
	    os.println();
    }
}
