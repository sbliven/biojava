/*
 *					  BioJava development code
 *
 * This	code may be	freely distributed and modified	under the
 * terms of	the	GNU	Lesser General Public Licence.	This should
 * be distributed with the code.  If you do	not	have a copy,
 * see:
 *
 *		http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.	 These should be listed	in @author doc comments.
 *
 * For more	information	on the BioJava project and its aims,
 * or to join the biojava-l	mailing	list, visit	the	home page
 * at:
 *
 *		http://www.biojava.org/
 *
 */


package	org.biojava.bio.seq.io;

import java.io.*;
import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Format reader for GenBank files.	 Converted from the old style io to the
 * new by working from <code>EmblLikeFormat</code>
 *
 * @author Thomas Down
 * @author Thad	Welch
 * Added GenBank header	info to	the	sequence annotation. The ACCESSION header
 * tag is not included.	Stored in sequence.getName().
 * @author Greg	Cox
 */

public class GenbankFormat implements SequenceFormat, Serializable
{
	protected static final String END_SEQUENCE_TAG = "//";
	protected static final String FEATURE_TAG = "FEATURES";
	protected static final String START_SEQUENCE_TAG = "ORIGIN";
	protected static final String FEATURE_LINE_PREFIX = "     ";
	protected static final String FEATURE_FLAG = "FT";
	protected static final String ACCESSION_TAG = "ACCESSION";
	protected static final String LOCUS_TAG = "LOCUS";
	protected static final String SIZE_TAG = "SIZE";
	protected static final String TYPE_TAG = "TYPE";
	protected static final String DIVISION_TAG = "DIVISION";
	protected static final String DATE_TAG = "MDAT";
	protected static final String VERSION_TAG = "VERSION";
	protected static final String GI_TAG = "GI";

	/**
	 * Reads a sequence from the specified reader using the Symbol parser and
	 * Sequence Factory provided.  The sequence read in must be in Genbank
	 * format.
	 *
	 * @return boolean True if there is another sequence in the file; false
	 * otherwise
	 */
	public boolean readSequence(
			BufferedReader reader,
			SymbolParser symParser,
			SeqIOListener listener)
		throws IllegalSymbolException, IOException,	ParseException
	{
		GenbankContext ctx = new GenbankContext(symParser, listener);
		String line;
		StreamParser sParser = null;
		boolean hasAnotherSequence = true;
		listener.startSequence();
		while ((line = reader.readLine()) != null)
		{
			if(line.length() == 0)
			{
				continue;
			}

			if (line.startsWith(END_SEQUENCE_TAG))
			{
				if(sParser != null)
				{   // End of symbol data
					sParser.close();
					sParser = null;
				}

				reader.mark(2);
				if (reader.read() == -1)
				{
					hasAnotherSequence = false;
				}
				else
				{
					reader.reset();
				}

				listener.endSequence();
				return hasAnotherSequence;
			}
				ctx.processLine(line);
		}

		throw new IOException("Premature end of	stream for GENBANK");
	}

	/**
	 * This	is not implemented.	It does	not	write anything to the stream.
	 */
	public void	writeSequence(Sequence seq,	PrintStream	os)
		throws IOException
	{
		throw new RuntimeException("Can't write	in GENBANK format...");
	}
}

/**
 * Encapsulate state used while	reading	data from a	specific
 * Genbank file.
 *
 * @author Thomas Down
 * @author Greg Cox
 */
class GenbankContext
{
	private	final static int HEADER	= 1;
	private	final static int FEATURES =	2;
	private	final static int SEQUENCE =	3;
	private final static int VERSION_LENGTH = 11;
	private final static int TAG_LENGTH = 12;

	private	int	status;
	private	SymbolParser symParser;
	private StreamParser streamParser;
	private	List symbols;
	private	String accession;
	private	String headerTag = "";
	private	StringBuffer headerTagText = new StringBuffer();
	private SeqIOListener listener;

	/**
	 * Constructor that takes the listener and the Symbol parser from the
	 * GenbankFormat.
	 *
	 * @param theSymbolParser Symbol parser to use in processing the file
	 * @param theListener Listener to notify when field has been processed
	 */
	protected GenbankContext(
			SymbolParser theSymbolParser,
			SeqIOListener theListener)
	{
		this.symbols = new ArrayList();
		this.status = HEADER;
		this.listener = theListener;

		this.symParser = theSymbolParser;
		this.streamParser = symParser.parseStream(listener);
	}

	/**
	 * Processes the line passed in in the context of previous lines processed.
	 * This is the method that does the real work of the class.
	 *
	 * @throws ParseException Thrown when an error occurs parsing the file
	 * @throws IllegalSymbolException Thrown when there is an illegal symbol
	 * in the file; i.e. there is a symbol in the file that is not in the
	 * alphabet being used
	 * @param line The line to process
	 */
	protected void processLine(String line)
		throws ParseException, IllegalSymbolException
	{
		if (line.startsWith(GenbankFormat.FEATURE_TAG))
		{
			status = FEATURES;
			this.saveSeqAnno();
		}
		else if (line.startsWith(GenbankFormat.START_SEQUENCE_TAG))
		{
			status = SEQUENCE;
			this.saveSeqAnno();
		}
		else if (status == FEATURES)
		{
			processFeatureLine(line);
		}
		else if (status == SEQUENCE)
		{
			processSeqLine(line, streamParser);
		}
		else if (status == HEADER)
		{
			processHeaderLine(line);
		}
	}

	/**
	 * Private method that processes a line, assuming that it is a sequence
	 * line instead of a feature line or header line.  Removes whitespace and
	 * location numbers and hands the sequence data to theParser to convert to
	 * biojava symbols
	 *
	 * @throws IllegalSymbolException Thrown when there is an illegal symbol
	 * in the file; i.e. there is a symbol in the file that is not in the
	 * alphabet being used
	 * @param line The line to process
	 * @param theParser Parser to convert the string data to biojava symbols
	 */
	private	void processSeqLine(String line, StreamParser theParser)
		throws IllegalSymbolException
	{
		char[] cline = line.toCharArray();
		int parseStart = 0;
		int parseEnd = 0;

		while (parseStart < cline.length)
		{
			while((parseStart < cline.length) &&
					((cline[parseStart] == ' ') ||
							(Character.isDigit(cline[parseStart]))))
			{
				// Read past leading spaces and numbers
				++parseStart;
			}
			if (parseStart >= cline.length)
			{
				break;
			}

			parseEnd = parseStart + 1;
			while ((parseEnd < cline.length && cline[parseEnd] != ' '))
			{
				++parseEnd;
			}

			// Got a segment of read sequence data
			theParser.characters(cline, parseStart, parseEnd - parseStart);
			parseStart = parseEnd;
		}
	}

	/**
	 * Private method to process a line assuming it's a feature line.  A
	 * feature line is defined to be a line between the FEATURE tag and the
	 * ORIGIN tag.  The BASE COUNT line is processed here.
	 *
	 * @throws ParseException Thrown when an error occurs parsing the file
	 * @param line The line to be processed
	 */
	private void processFeatureLine(String line)
			throws ParseException
	{
		// Check the line is really a feature line
		if(line.startsWith(GenbankFormat.FEATURE_LINE_PREFIX))
		{
			this.saveSeqAnno();
			// Flag value as a feature line for GenbankProcessor.  By a
			// strange coincidence, this happens to be the same as the EMBL
			// value
			headerTag = GenbankFormat.FEATURE_FLAG;
			headerTagText = new StringBuffer(line.substring(5));
		}
		else
		{
			// Otherwise, process it as a header line
			processHeaderLine(line);
		}
	}

	/**
	 * Private method to process a line assuming it's a header line.  A header
	 * line is defined to be a line before the FEATURE tag appears in the file.
	 *
	 * @throws ParseException Thrown when an error occurs parsing the file
	 * @param line The line to be processed
	 */
	private	void processHeaderLine(String line)
			throws ParseException
	{
		if(line.startsWith(GenbankFormat.LOCUS_TAG))
		{
			// the LOCUS line is a special case because it contains the
			// locus, size, molecule type, GenBank division, and the date
			// of last modification.
			this.saveSeqAnno();
			StringTokenizer lineTokens = new StringTokenizer(line);
			headerTag = lineTokens.nextToken();
			headerTagText = new StringBuffer(lineTokens.nextToken());

			this.saveSeqAnno();
			headerTag = GenbankFormat.SIZE_TAG;
			headerTagText = new StringBuffer(lineTokens.nextToken());

			this.saveSeqAnno();
			headerTag = GenbankFormat.TYPE_TAG; // Check this; may be under PROP
			// Read past 'bp'
			headerTagText = new StringBuffer(lineTokens.nextToken());
			headerTagText = new StringBuffer(lineTokens.nextToken());

			this.saveSeqAnno();
			headerTag = GenbankFormat.DIVISION_TAG; // May be under PROP
			headerTagText = new StringBuffer(lineTokens.nextToken());

			this.saveSeqAnno();
			headerTag = GenbankFormat.DATE_TAG;
			headerTagText = new StringBuffer(lineTokens.nextToken());
		}
		else if(line.startsWith(GenbankFormat.VERSION_TAG))
		{
			// VERSION line is a special case because it contains both
			// the VERSION field and the GI number
			this.saveSeqAnno();
			StringTokenizer lineTokens = new StringTokenizer(line);
			headerTag = lineTokens.nextToken();
			headerTagText = new StringBuffer(lineTokens.nextToken());

			String nextToken = lineTokens.nextToken();
			if(nextToken.startsWith(GenbankFormat.GI_TAG))
			{
				this.saveSeqAnno();
				headerTag = GenbankFormat.GI_TAG; // Possibly should be UID?
				headerTagText =
						new StringBuffer(nextToken.substring(3));
			}
		}
		else if (hasHeaderTag(line))
		{	// line	has	a header tag
			this.saveSeqAnno();
			headerTag =	line.substring(0, TAG_LENGTH).trim();
			headerTagText =	new	StringBuffer(line.substring(TAG_LENGTH));
		}
		else
		{	// keep	appending tag text value
			headerTagText.append(" " + line.substring(TAG_LENGTH));
		}
	}

	/**
	 * Passes the tag and the text to the listener.
	 *
	 * @throws ParseException Thrown when an error occurs parsing the file
	 */
	private	void saveSeqAnno()
		throws ParseException
	{
		if (!headerTag.equals(""))
		{ // save tag and its text
			listener.addSequenceProperty(
					headerTag, headerTagText.toString());
			headerTag = "";
			headerTagText = new StringBuffer("");
		}
	}

	/**
	 * @return does	the	line contain a header tag.
	 * Yes,	if any of the leading TAG_LENGTH characters	aren't a space
	 */
	private	boolean	hasHeaderTag(String	line)
	{
		boolean isHeaderTag = false;
		char[] l = line.toCharArray();
		for (int i = 0; i < TAG_LENGTH; i++)
		{
			if(l[i] != ' ')
			{
				isHeaderTag = true;
				break;
			}
		}
		return isHeaderTag;
	}
}

