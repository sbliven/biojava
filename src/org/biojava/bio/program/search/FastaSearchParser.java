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

package org.biojava.bio.program.search;

import java.io.IOException;
import java.io.BufferedReader;
import java.lang.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.search.*;
import org.biojava.utils.ParserException;

/**
 * <code>FastaSearchParser</code> objects provide Fasta search parsing
 * functionality for the '-m 10' output format (see the Fasta
 * documentation). Data are passed to a SearchContentHandler which
 * coordinates its interpretation and creation of objects representing
 * the result.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.1
 */
public class FastaSearchParser implements SearchParser
{
    private static final int    NODATA = 0;
    private static final int  INHEADER = 1;
    private static final int     INHIT = 2;
    private static final int   INQUERY = 3;
    private static final int INSUBJECT = 4;
    private static final int   INALIGN = 5;

    // Valid line identifiers for result annotation
    private static HashSet resultAnnoTokens =
	(HashSet) fillSet(new String [] { "mp_name",   "mp_ver",
					  "mp_argv",   "mp_extrap",
					  "mp_stats",  "mp_KS",
					  "pg_name",   "pg_ver",
					  "pg_optcut", "pg_cgap" },
			  new HashSet());

    // Valid line identifiers for search parameters
    private static HashSet resultSearchParmTokens =
	(HashSet) fillSet(new String [] { "pg_matrix", "pg_ktup",
					  "pg_gap-pen" },
			  new HashSet());

    // Valid line identifiers for hit annotation
    private static HashSet hitAnnoTokens =
	(HashSet) fillSet(new String [] { "fa_frame",   "fa_initn",
					  "fa_init1",   "fa_opt",
					  "fa_bits",    "sw_score",
					  "sw_ident",   "sw_gident",
					  "sw_overlap", "fa_ident",
					  "fa_gident",  "fa_overlap",
					  "fa_score" },
			  new HashSet());

    // Valid line identifiers for hit data
    private static HashSet hitDataTokens =
	(HashSet) fillSet(new String [] { "fa_expect", "fa_z-score" },
			  new HashSet());

    // Set which values should be parsed from String to a number
    private static HashSet toDouble =
	(HashSet) fillSet(new String [] { "fa_expect", "fa_z-score",
					  "fa_bits",   "fa_initn", 
					  "fa_init1",  "fa_opt",
					  "fa_score",  "sw_score" },
			  new HashSet());

    private static HashSet toFloat =
	(HashSet) fillSet(new String [] { "sw_ident",  "fa_ident",
					  "fa_gident"  },
			  new HashSet());

    private static HashSet toInteger =
	(HashSet) fillSet(new String [] { "sw_overlap", "fa_overlap" },
			  new HashSet());

    private int              searchStatus = NODATA;

    private boolean          searchParsed = false;
    private boolean moreSearchesAvailable = false;

    private SearchContentHandler handler;
    private BufferedReader       reader;
    private String               line;
    private int                  lineNumber;

    StringBuffer   querySeqTokens = new StringBuffer(1024);
    StringBuffer subjectSeqTokens = new StringBuffer(1024);
    StringBuffer      matchTokens = new StringBuffer(1024);

    /**
     * The <code>parseSearch</code> method performs the core parsing
     * operations.
     *
     * @param reader a <code>BufferedReader</code> to read from.
     * @param handler a <code>SearchContentHandler</code> to notify
     * of events.
     *
     * @return a <code>boolean</code> value, true if a further search
     * result remains to be parsed, false if no further results were
     * detected.
     *
     * @exception IOException if the BufferedReader fails.
     * @exception BioException if the parser (via the registered
     * SearchContentHandler) fails to resolve a query sequence and
     * target database.
     * @exception ParserException if the parser fails to parse a
     * line.
     */
    public boolean parseSearch(final BufferedReader       reader,
			       final SearchContentHandler handler)
	throws IOException, BioException, ParserException
    {
	boolean foundQuerySeqID = false;
	lineNumber = 0;

	this.handler = handler;

    LINE:
	while ((line = reader.readLine()) != null)
	{
	    lineNumber++;
	    // System.out.println("Parser working on:" + line);

	    // This token indicates the end of the formatted search
	    // data. Some outputs don't have any alignment consensus
	    // tokens, so we need to check here as well as INALIGN
	    if (line.startsWith(">>><<<"))
	    {
		searchStatus = NODATA;

		// Pass final data to handler
		handler.addSubHitProperty("querySeqTokens",   querySeqTokens.toString());
		handler.addSubHitProperty("subjectSeqTokens", subjectSeqTokens.toString());
		handler.addSubHitProperty("matchTokens",      matchTokens.toString());

		handler.endSubHit();
		handler.endSearch();

		searchParsed          = true;
		moreSearchesAvailable = false;

		continue LINE;
	    }

	STATUS:
	    switch (searchStatus)
	    {
		case NODATA:
		    // This token marks the line describing the query
		    // sequence and database searched. It is followed
		    // by header lines containing data about the
		    // search
		    if (line.startsWith(">>>"))
		    {
			searchStatus = INHEADER;

			handler.setSubjectDB(parseDB(line));

			handler.startSearch();
			handler.startHeader();

			// If we already saw an end of search token
			// then this is the start of another
			// dataset. We break from the loop and return
			// that the stream is not empty
			if (searchParsed)
			{
			    searchParsed          = false;
			    moreSearchesAvailable = true;
			    break LINE;
			}
			break STATUS;
		    }
		    else
			continue LINE;

		case INHEADER:
		    // This token marks the line describing a hit. It
		    // is followed by header lines containing data
		    // about the hit
		    if (line.startsWith(">>"))
		    {
			searchStatus = INHIT;

			handler.endHeader();
			handler.startHit();

			querySeqTokens.setLength(0);
			subjectSeqTokens.setLength(0);
			matchTokens.setLength(0);

			handler.addHitProperty("id",   parseId(line));
			handler.addHitProperty("desc", parseDesc(line));
		    }
		    else
		    {
			if (! parseHeaderLine(line, resultAnnoTokens))
			    if (! parseHeaderLine(line, resultSearchParmTokens))
				throw new ParserException("Fasta parser failed to recognise line type",
							  null,
							  lineNumber,
							  line);
		    }
		    break STATUS;

		case INHIT:
		    // This token marks the line describing the query
		    // sequence.
		    if (line.startsWith(">"))
		    {
			searchStatus = INQUERY;

			if (! foundQuerySeqID)
			{
			    handler.setQuerySeq(parseId(line));
			    foundQuerySeqID = true;
			}

			handler.endHit();
			handler.startSubHit();
		    }
		    else
		    {
			if (! parseHitLine(line, hitAnnoTokens))
			    if (! parseHitLine(line, hitDataTokens))
				throw new ParserException("Fasta parser failed to recognise line type",
							  null,
							  lineNumber,
							  line);
		    }
		    break STATUS;

		case INQUERY:
		    // This token marks the line describing the
		    // subject sequence.
		    if (line.startsWith(">"))
		    {
			searchStatus = INSUBJECT;
		    }
		    else
		    {
			parseQuerySequence(line);
		    }
		    break STATUS;

		case INSUBJECT:
		    // This token marks the start of lines containing
		    // the consensus symbols from the Fasta alignment,
		    // which we ignore
		    if (line.startsWith("; al_cons:"))
		    {
			searchStatus = INALIGN;
		    }
		    else if (line.startsWith(">>"))
		    {
			searchStatus = INHIT;

			// Pass data to handler
			handler.addSubHitProperty("querySeqTokens",   querySeqTokens.toString());
			handler.addSubHitProperty("subjectSeqTokens", subjectSeqTokens.toString());
			handler.addSubHitProperty("matchTokens",      matchTokens.toString());

			handler.endSubHit();
			handler.startHit();

			querySeqTokens.setLength(0);
			subjectSeqTokens.setLength(0);
			matchTokens.setLength(0);

			handler.addHitProperty("id",   parseId(line));
			handler.addHitProperty("desc", parseDesc(line));
		    }
		    else
		    {
			parseSubjectSequence(line);
		    }
		    break STATUS;

		case INALIGN:
		    if (line.startsWith(">>"))
		    {
			searchStatus = INHIT;

			// Pass data to handler
			handler.addSubHitProperty("querySeqTokens",   querySeqTokens.toString());
			handler.addSubHitProperty("subjectSeqTokens", subjectSeqTokens.toString());
			handler.addSubHitProperty("matchTokens",      matchTokens.toString());

			handler.endSubHit();
			handler.startHit();

			querySeqTokens.setLength(0);
			subjectSeqTokens.setLength(0);
			matchTokens.setLength(0);

			handler.addHitProperty("id",   parseId(line));
			handler.addHitProperty("desc", parseDesc(line));
		    }
		    else if (line.startsWith(">>><<<"))
		    {
			searchStatus = NODATA;

			// Pass final data to handler
			handler.addSubHitProperty("querySeqTokens",   querySeqTokens.toString());
			handler.addSubHitProperty("subjectSeqTokens", subjectSeqTokens.toString());
			handler.addSubHitProperty("matchTokens",      matchTokens.toString());

			handler.endSubHit();
			handler.endSearch();

			searchParsed          = true;
			moreSearchesAvailable = false;

			continue LINE;
		    }
		    else
		    {
			matchTokens.append(line);
		    }
		    break STATUS;

		default:
		    break STATUS;
	    } // end switch
	} // end while

	// This is false if we reach here
	return moreSearchesAvailable;
    }

    /**
     * The <code>fillSet</code> method populates a Set with the
     * elements of an Array.
     *
     * @param tokenArray a <code>String []</code> array.
     * @param set a <code>Set</code> object.
     *
     * @return a Set object.
     */
    private static Set fillSet(final String [] tokenArray, final Set set)
    {
	for (int i = 0; i < tokenArray.length; i++)
	    set.add(tokenArray[i]);

	return set;
    }

    /**
     * The <code>parseId</code> method parses sequence Ids from lines
     * starting with '>' and '>>'.
     *
     * @param line a <code>String</code> to be parsed.
     *
     * @return a <code>String</code> containing the Id.
     *
     * @exception ParserException if an error occurs. 
     */
    private String parseId(final String line)
	throws ParserException
    {
	String trimmed = line.trim();
	int firstSpace = trimmed.indexOf(' ');

	// For Hit header lines (always start with >>)
	if (trimmed.startsWith(">>"))
	{
	    if (trimmed.length() == 2)
		throw new ParserException("Fasta parser encountered a sequence with no Id",
					  null,
					  lineNumber,
					  line);

	    if (firstSpace == -1)
		return trimmed.substring(2);
	    else
		return trimmed.substring(2, firstSpace);
	}
	// For SubHit header lines (always start with >)
	else
	{
	    if (trimmed.length() == 1)
		throw new ParserException("Fasta parser encountered a sequence with no Id",
					  null,
					  lineNumber,
					  line);

	    if (firstSpace == -1)
		return trimmed.substring(1);
	    else
		return trimmed.substring(1, firstSpace);
	}
    }

    /**
     * The <code>parseDesc</code> method parses the sequence
     * description from subject header lines.
     *
     * @param line a <code>String</code> to be parsed.
     *
     * @return a <code>String</code> containing the description.
     */
    private String parseDesc(final String line)
    {
	String trimmed = line.trim();
	int firstSpace = trimmed.indexOf(' ');

	if (firstSpace == -1)
	    return "No description";

	return trimmed.substring(firstSpace + 1);
    }

    /**
     * The <code>parseDB</code> method parses a database filename from
     * the relevant output line.
     *
     * @param line a <code>String</code> to be parsed.
     *
     * @return a <code>String</code> containing the filename.
     *
     * @exception ParserException if an error occurs.
     */
    private String parseDB(final String line)
	throws ParserException
    {
	StringTokenizer st = new StringTokenizer(line);
	String db;
	String previous = null;
	String  current = null;

	// The database filename is the second-to-last token on the line
	while (st.hasMoreTokens())
	{
	    if (current == null)
		current = st.nextToken();
	    else
	    {
		previous = current;
		current = st.nextToken();
	    }
	}

	if (previous == null)
	    throw new ParserException("Fasta parser failed to parse a database filename",
				      null,
				      lineNumber,
				      line);
	return previous;
    }

    private boolean parseHeaderLine(final String line,
				    final Set    tokenSet)
	throws ParserException	    
    {
	Object [] data = parseLine(line, tokenSet);

	if (data.length > 0)
	{
	    handler.addSearchProperty(data[0], data[1]);
	    return true;
	}
	else
	{
	    return false;
	}
    }

    private boolean parseHitLine(final String line,
				 final Set    tokenSet)
	throws ParserException	    
    {
	Object [] data = parseLine(line, tokenSet);

	if (data.length > 0)
	{
	    handler.addHitProperty(data[0], data[1]);
	    return true;
	}

	return false;
    }

    private Object [] parseLine(final String line,
				final Set    tokenSet)
	throws ParserException
    {
	int idTokenStart = line.indexOf(";");
	int   idTokenEnd = line.indexOf(":");

	String   idToken = line.substring(idTokenStart + 1, idTokenEnd);
	idToken          = idToken.trim();

	String   idValue = line.substring(idTokenEnd + 1);
	idValue          = idValue.trim();

	if (tokenSet.contains(idToken))
	{
	    try
	    {
		if (toDouble.contains(idToken))
		{
		    Double val = Double.valueOf(idValue);
		    return new Object [] { idToken, val };
		}

		if (toFloat.contains(idToken))
		{
		    Float val = Float.valueOf(idValue);
		    return new Object [] { idToken, val };
		}

		if (toInteger.contains(idToken))
		{
		    Integer val = Integer.valueOf(idValue);
		    return new Object [] { idToken, val };
		}
	    }
	    catch (NumberFormatException nfe)
	    {
		throw new ParserException("Fasta parser failed to parse a numeric value",
					  null,
					  lineNumber,
					  line);
	    }

	    // Otherwise leave as a string
	    return new Object [] { idToken, idValue };
	}

	return new Object [0];
    }

    private void parseQuerySequence(final String line)
    {
	Object [] data = parseSequence(line);

	if (data.length > 0)
	{
	    // We have a key/value pair
	    handler.addSubHitProperty("query" + data[0].toString(),
				      data[1]);
	}
	else
	{
	    // We have a line of sequence tokens
	    querySeqTokens.append(line);
	}
    }

    private void parseSubjectSequence(final String line)
    {
	Object [] data = parseSequence(line);

	if (data.length > 0)
	{
	    // We have a key/value pair
	    handler.addSubHitProperty("subject" + data[0].toString(),
				      data[1]);
	}
	else
	{
	    // We have a line of sequence tokens
	    subjectSeqTokens.append(line);
	}
    }

    private Object [] parseSequence(final String line)
    {
	if (line.startsWith(";"))
	{
	    // Check the sequence type given by the report
	    if (line.equals("; sq_type: p"))
	    {
		return new Object [] { "_sq_type", "protein"};
	    }
	    else if (line.equals("; sq_type: D"))
	    {
		return new Object [] { "_sq_type", "DNA"};
	    }

	    // Record the coordinates and offset of the alignment
	    if (line.startsWith("; al_start:"))
	    {
		return new Object [] { "_al_start", parseCoord(line) };
	    }
	    else if (line.startsWith("; al_stop:"))
	    {
		 return new Object [] { "_al_stop", parseCoord(line) };
	    }
	    else if (line.startsWith("; al_display_start:"))
	    {
		return new Object [] {"_al_display_start", parseCoord(line) };
	    }
	    else if (line.startsWith("; sq_len:"))
	    {
		return new Object [] { "_sq_len", parseCoord(line) };
	    }
	    else if (line.startsWith("; sq_offset:"))
	    {
		return new Object [] { "_sq_offset", parseCoord(line) };
	    }
	}

	return new Object [0];
    }

    /**
     * The <code>parseCoord</code> method extracts integer coordinates
     * from Fasta output lines.
     *
     * @param line a <code>String</code> object to parse.
     *
     * @return an <code>Integer</code> coordinate.
     */
    private Integer parseCoord(final String line)
    {
	int sepIndex = line.lastIndexOf(":");
	String coord = line.substring(sepIndex + 1);

	return Integer.valueOf(coord.trim());
    }
}
