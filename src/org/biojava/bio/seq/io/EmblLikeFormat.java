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
 * Format processor for handling EMBL records and similar files.  This
 * takes a very simple approach: all `normal' attribute lines are
 * passed to the listener as a tag (first two characters) and a value
 * (the rest of the line from the 6th character onwards).  Any data
 * between the special `SQ' line and the "//" entry terminator is
 * passed as a SymbolReader.
 *
 * <p>This low-level format processor should normally be used in
 * conjunction with one or more `filter' objects, such as
 * EmblProcessor.</p>
 *
 * <p>Many ideas borrowed from the old EmblFormat processor by Thomas
 * Down and Thad Welch.</p>
 *
 * @author Thomas Down
 * @author Greg Cox
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.1
 */

public class EmblLikeFormat implements SequenceFormat, Serializable
{
    private boolean elideSymbols = false;

    /**
     * Should we ignore the symbols (SQ) part of the entry? If this
     * property is set to <code>true</code>, the parser will never
     * call addSymbols on the <code>SeqIOListener</code>, but parsing
     * will be faster if you're only interested in header information.
     *
     * <p>This property also allows the header to be parsed for files
     * which have invalid sequence data.</p>
     */
    public void setElideSymbols(boolean b)
    {
	elideSymbols = b;
    }

    /**
     * Return a flag indicating if symbol data will be skipped
     * when parsing streams.
     */
    public boolean getElideSymbols()
    {
	return elideSymbols;
    }
    
    public boolean readSequence(BufferedReader reader,
				SymbolParser   symParser,
				SeqIOListener  listener)
	throws IllegalSymbolException, IOException, ParseException
    {
	String            line;
	StreamParser    sparser = null;
	boolean hasMoreSequence = true;

	listener.startSequence();

	while ((line = reader.readLine()) != null)
	{
	    if (line.startsWith("//"))
	    {
		if (sparser != null)
		{
		    // End of symbol data
		    sparser.close();
		    sparser = null;
		}

		reader.mark(2);
		if (reader.read() == -1)
		    hasMoreSequence = false;
		else
		    reader.reset();

		listener.endSequence();
		return hasMoreSequence;
	    }
	    else if (line.startsWith("SQ"))
	    {
		// Adding a null property to flush the last feature;
		// Needed for Swissprot files because there is no gap
		// between the feature table and the sequence data
		listener.addSequenceProperty("XX", "");
		sparser = symParser.parseStream(listener);
	    }
	    else
	    {
		if (sparser == null)
		{
		    // Normal attribute line
		    String tag  = line.substring(0, 2);
		    String rest = null;
		    if (line.length() > 5)
		    {
			rest = line.substring(5);
		    }
		    listener.addSequenceProperty(tag, rest);
		}
		else
		{
		    // Sequence line
		    if (! elideSymbols)
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
        throws IllegalSymbolException, ParseException
    {
	char[] cline = line.toCharArray();
	int parseStart = 0;
	int parseEnd   = 0;

	while (parseStart < cline.length)
	{
	    while (parseStart < cline.length && cline[parseStart] == ' ')
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

    public void writeSequence(Sequence seq, PrintStream os)
	throws IOException
    {
	
    }

    public void writeSequence(Sequence seq, String format, PrintStream os)
	throws IOException
    {
	try
	{
	    SeqFileFormer former = SeqFileFormerFactory.makeFormer(format);

	    SeqIOEventEmitter.getSeqIOEvents(seq, former);
	}
	catch (BioException bex)
	{
	    bex.printStackTrace();
	}
    }
}
