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
			     SymbolParser symParser,
			     SeqIOListener listener)
	throws IllegalSymbolException, IOException
    {
	final BufferedReader in = context.getReader();
	String line;
	StreamParser sparser = null;

	listener.startSequence();
	
	while ((line = in.readLine()) != null) {
	    if (line.startsWith("//")) {
		if (sparser != null) {
		    // End of symbol data
		    sparser.close();
		    sparser = null;
		}

		in.mark(2);
		if (in.read() == -1)
		    context.streamEmpty();
		else
		    in.reset();
		
		listener.endSequence();
		return;
	    } else if (line.startsWith("SQ")) {
		sparser = symParser.parseStream(listener);
	    } else {
		if (sparser == null) {
		    // Normal attribute line
		    String tag = line.substring(0, 2);
		    String rest = null;
		    if (line.length() > 5) {
			rest = line.substring(5);
		    }
		    listener.addSequenceProperty(tag, rest);
		} else {
		    // Sequence line
		    processSequenceLine(line, sparser);
		}
	    }
	}

	throw new IOException("Premature end of stream for EMBL");
    }

    /**
     * Dispatch symbol data from SQ-block line of an EMBL-like file.
     */

    protected void processSequenceLine(String line, StreamParser parser) 
        throws IllegalSymbolException
    {
	char[] cline = line.toCharArray();
	int parseStart = 0;
	int parseEnd = 0;

	while (parseStart < cline.length) {
	    while( parseStart < cline.length && cline[parseStart] == ' ')
		++parseStart;
	    if (parseStart >= cline.length)
		break;

	    if (Character.isDigit(cline[parseStart]))
		return;

	    parseEnd = parseStart + 1;
	    while (parseEnd < cline.length && cline[parseEnd] != ' ')
		++parseEnd;

	    // Got a segment of read sequence data

	    parser.characters(cline, parseStart, parseEnd - parseStart);

	    parseStart = parseEnd;
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
