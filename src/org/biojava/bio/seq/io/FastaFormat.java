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
 * Format object representing FASTA files. These files are almost pure
 * sequence data. The only `sequence property' reported by this parser
 * is PROPERTY_DESCRIPTIONLINE, which is the contents of the
 * sequence's description line (the line starting with a '>'
 * character). Normally, the first word of this is a sequence ID. If
 * you wish it to be interpreted as such, you should use
 * FastaDescriptionLineParser as a SeqIO filter.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */

public class FastaFormat implements SequenceFormat, Serializable {

    static
    {
	Set validFormats = new HashSet();
	validFormats.add("Fasta");

	SequenceFormat.FORMATS.put(FastaFormat.class.getName(),
				   validFormats);
    }

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

    public boolean readSequence(BufferedReader reader,
				SymbolParser symParser,
				SeqIOListener siol)
	throws IllegalSymbolException, IOException, ParseException 
    {
	String line = reader.readLine();
	if (line == null) {
	    throw new IOException("Premature stream end");
	}
	if (!line.startsWith(">")) {
	    throw new IOException("Stream does not appear to contain FASTA formatted data: " + line);
	}

	siol.startSequence();
    
	String description = line.substring(1).trim();
	siol.addSequenceProperty(PROPERTY_DESCRIPTIONLINE, description);

	boolean seenEOF = readSequenceData(reader, symParser, siol);
	siol.endSequence();
    
	return !seenEOF;
    }

    private boolean readSequenceData(BufferedReader r, 
				     SymbolParser parser,
				     SeqIOListener listener)
        throws IOException, IllegalSymbolException
    {
	char[] cache = new char[256];
	boolean reachedEnd = false, seenEOF = false;
	StreamParser sparser = parser.parseStream(listener);

	while (!reachedEnd) {
	    r.mark(cache.length);
	    int bytesRead = r.read(cache, 0, cache.length);
	    if (bytesRead < 0) {
		reachedEnd = seenEOF = true;
	    } else {
		int parseStart = 0;
		int parseEnd = 0;
		while (!reachedEnd && parseStart < bytesRead && cache[parseStart] != '>') {
		    parseEnd = parseStart;

		    while (parseEnd < bytesRead && 
			   cache[parseEnd] != '\n' && 
			   cache[parseEnd] != '\r')
		    {
			++parseEnd;
		    }
		    sparser.characters(cache, parseStart, parseEnd - parseStart);

		    parseStart = parseEnd + 1;
		    while (parseStart < bytesRead &&
			   cache[parseStart] == '\n' &&
			   cache[parseStart] == '\r')
		    {
			++parseStart;
		    }
		}
		if (parseStart < bytesRead && cache[parseStart] == '>') {
		    r.reset();
		    if (r.skip(parseStart) != parseStart)
			throw new IOException("Couldn't reset to start of next sequence");
		    reachedEnd = true;
		}
	    }
	}

	sparser.close();
	return seenEOF;
    }

    /**
     * Return a suitable description line for a Sequence. If the
     * sequence's annotation bundle contains PROPERTY_DESCRIPTIONLINE,
     * this is used verbatim.  Otherwise, the sequence's name is used.
     */

    protected String describeSequence(Sequence seq) {
	String description = null;
	try {
	    description = seq.getAnnotation().getProperty(PROPERTY_DESCRIPTIONLINE).toString();
	} catch (NoSuchElementException ex) {
	    description = seq.getName();
	}
	return description;
    }

    public void writeSequence(Sequence seq, PrintStream os) 
	throws IOException
    {
	os.print(">");
	os.println(describeSequence(seq));

	//  int length = seq.length();
//    	for(int i = 1; i <= length; i++) {
//    	    os.write(seq.symbolAt(i).getToken());
//    	    if( (i % lineWidth) == 0)
//    		os.println();
//    	}
//    	if( (length % lineWidth) != 0)
//    	    os.println();

	for(int pos = 1; pos <= seq.length() + 1; pos += lineWidth) {
	    int end = Math.min(pos + lineWidth - 1, seq.length());
	    os.println(seq.subStr(pos, end));
	}
    }

    public void writeSequence(Sequence seq, String format, PrintStream os)
	throws IOException
    {
	String requestedFormat = new String(format);
	boolean          found = false;

	String [] formats = (String []) getFormats().toArray(new String[0]);

	if (! found)
	    throw new IOException("Unable to wrtie: an invalid file format '"
				  + format
				  + "' was requested");

	writeSequence(seq, os);
    }

    public Set getFormats()
    {
	return (Set) SequenceFormat.FORMATS.get(this.getClass().getName());
    }

    public String getDefaultFormat()
    {
	return "Fasta";
    }
}
