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
    private static final int NODATA    = 0;
    private static final int INHEADER  = 1;
    private static final int INHIT     = 2;
    private static final int INQUERY   = 3;
    private static final int INSUBJECT = 4;
    private static final int INALIGN   = 5;

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
					  "fa_gident",  "fa_overlap" },
			  new HashSet());

    // Valid line identifiers for hit data
    private static HashSet hitDataTokens =
	(HashSet) fillSet(new String [] { "fa_expect", "fa_z-score" },
			  new HashSet());

    // Set which values should be parsed from String to a number
    private static HashSet toDouble =
	(HashSet) fillSet(new String [] { "fa_expect", "fa_z-score",
					  "fa_bits" },
			  new HashSet());

    private static HashSet toFloat =
	(HashSet) fillSet(new String [] { "sw_ident", "fa_ident",
					  "fa_gident" },
			  new HashSet());

    private static HashSet toInteger =
	(HashSet) fillSet(new String [] { "fa_initn",   "fa_init1",
					  "fa_opt",     "sw_score",
					  "sw_overlap", "fa_overlap" },
			  new HashSet());

    private int              searchStatus = NODATA;

    private boolean          searchParsed = false;
    private boolean moreSearchesAvailable = false;

    private BufferedReader reader;
    private String         line;
    private int            lineNumber;

    Map resultPreAnnotation = new HashMap();
    Map resultSearchParm    = new HashMap();
    Map hitPreAnnotation    = new HashMap();
    Map hitData             = new HashMap();

    StringBuffer querySeqTokens   = new StringBuffer(1024);
    StringBuffer subjectSeqTokens = new StringBuffer(1024);

    /**
     * The <code>parseSearch</code> method performs the core parsing
     * operations.
     *
     * @param reader a <code>BufferedReader</code> to read from.
     * @param scHandler a <code>SearchContentHandler</code> to notify
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
			       final SearchContentHandler scHandler)
	throws IOException, BioException, ParserException
    {
	boolean foundQuerySeqID = false;
	lineNumber = 0;

	FastaSearchBuilder handler = (FastaSearchBuilder) scHandler;

    LINE:
	while ((line = reader.readLine()) != null)
	{
	    lineNumber++;
	    // System.out.println("Parser:" + line);

	    // This token indicates the end of the formatted search
	    // data. Some outputs don't have any alignment consensus
	    // tokens, so we need to check here as well as INALIGN
	    if (line.startsWith(">>><<<"))
	    {
		searchStatus = NODATA;

		// Pass final data to handler
		hitData.put("querySeqTokens",   querySeqTokens.toString());
		hitData.put("subjectSeqTokens", subjectSeqTokens.toString());

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
			// Clear the data store
			resultSearchParm.clear();
			resultPreAnnotation.clear();

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

			handler.setSearchAnnotationData(resultPreAnnotation);
			handler.endHeader();

			handler.startHit();
			// Clear the data store
			hitData.clear();
			hitPreAnnotation.clear();
			querySeqTokens.setLength(0);
			subjectSeqTokens.setLength(0);

			hitData.put("id", parseID(line));
		    }
		    else
		    {
			if (! parseLine(line, resultAnnoTokens, resultPreAnnotation))
			    if (! parseLine(line, resultSearchParmTokens, resultSearchParm))
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
			    handler.setQuerySeq(parseID(line));
			    foundQuerySeqID = true;
			}

			handler.endHit();
			handler.startSubHit();
		    }
		    else
		    {
			if (! parseLine(line, hitAnnoTokens, hitPreAnnotation))
			    if (! parseLine(line, hitDataTokens, hitData))
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
			if (! parseSequence(line, "query", hitData))
			    throw new ParserException("Fasta parser failed to recognise line type",
						      null,
						      lineNumber,
						      line);
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
			hitData.put("querySeqTokens",   querySeqTokens.toString());
			hitData.put("subjectSeqTokens", subjectSeqTokens.toString());

			handler.setHitData(hitData);
			handler.setHitAnnotationData(hitPreAnnotation);
			handler.endSubHit();

			handler.startHit();
			// Clear the data store
			hitData.clear();
			hitPreAnnotation.clear();
			querySeqTokens.setLength(0);
			subjectSeqTokens.setLength(0);

			hitData.put("id", parseID(line));
		    }
		    else
		    {
			if (! parseSequence(line, "subject", hitData))
			    throw new ParserException("Fasta parser failed to recognise line type",
						      null,
						      lineNumber,
						      line);
		    }
		    break STATUS;

		case INALIGN:
		    if (line.startsWith(">>"))
		    {
			searchStatus = INHIT;

			// Pass data to handler
			hitData.put("querySeqTokens",   querySeqTokens.toString());
			hitData.put("subjectSeqTokens", subjectSeqTokens.toString());

			handler.setHitData(hitData);
			handler.setHitAnnotationData(hitPreAnnotation);
			handler.endSubHit();

			handler.startHit();
			// Clear the data store
			hitData.clear();
			hitPreAnnotation.clear();
			querySeqTokens.setLength(0);
			subjectSeqTokens.setLength(0);

			hitData.put("id", parseID(line));
		    }
		    else if (line.startsWith(">>><<<"))
		    {
			searchStatus = NODATA;

			// Pass final data to handler
			hitData.put("querySeqTokens",   querySeqTokens.toString());
			hitData.put("subjectSeqTokens", subjectSeqTokens.toString());

			handler.endSubHit();
			handler.endSearch();

			searchParsed          = true;
			moreSearchesAvailable = false;

			continue LINE;
		    }
		    break STATUS;

		default:
		    break STATUS;
	    } // end switch
	} // end while

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
     * The <code>parseID</code> method parses sequence IDs from lines
     * starting with '>' and '>>'.
     *
     * @param line a <code>String</code> to be parsed.
     *
     * @return a <code>String</code> containing the ID.
     *
     * @exception ParserException if an error occurs. 
     */
    private String parseID(final String line)
	throws ParserException
    {
	StringTokenizer st = new StringTokenizer(line);
	String id;
	if (st.hasMoreTokens())
	    id = st.nextToken();
	else
	    throw new ParserException("Fasta parser failed to parse a sequence ID",
				      null,
				      lineNumber,
				      line);
	// For Hit header lines
	if (id.startsWith(">>"))
	    return id.substring(2);

	// For SubHit header lines where the sequence in the Fasta
	// file had no ID
	if (id == ">")
	    throw new ParserException("Fasta parser encountered a sequence with no ID",
				      null,
				      lineNumber,
				      line);
	// For SubHit header lines where the sequence in the Fasta did
	// have an ID
	return id.substring(1);
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
	String current  = null;

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

    /**
     * The <code>parseLine</code> method parses values from Fasta data
     * lines, accepting only lines with recognised leader tokens
     * specified in the Set. The result is entered in the Map with the
     * token as key (as a side-effect) and the method returns true if
     * successful.
     *
     * @param line a <code>String</code> to be parsed.
     * @param tokenSet a <code>Set</code> containing valid leader tokens.
     * @param dataMap a <code>Map</code> to contain the result.
     *
     * @return a <code>boolean</code> value, true if a token and value
     * were recognised.
     *
     * @exception ParserException if an error occurs.
     */
    private boolean parseLine(final String line,
			      final Set    tokenSet,
			      final Map    dataMap)
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
		    dataMap.put(idToken, val);
		    return true;
		}

		if (toFloat.contains(idToken))
		{
		    Float val = Float.valueOf(idValue);
		    dataMap.put(idToken, val);
		    return true;
		}

		if (toInteger.contains(idToken))
		{
		    Integer val = Integer.valueOf(idValue);
		    dataMap.put(idToken, val);
		    return true;
		}
	    }
	    catch (NumberFormatException nfe)
	    {
		throw new ParserException("Fasta parser failed to parse a double value",
					  null,
					  lineNumber,
					  line);
	    }

	    // Otherwise leave as a string
	    dataMap.put(idToken, idValue);
	    return true;
	}

	return false;
    }

    /**
     * The <code>parseSequence</code> method parses the sequence
     * alignment lines of the Fasta subhits.
     *
     * @param line a <code>String</code> object to parse.
     * @param name a <code>String</code> object with value "query" or
     * "subject" to indicate where to store the data.
     * @param dataMap a <code>Map</code> object to contain the parsed data.
     *
     * @return a <code>boolean</code> value, true if the line was
     * parsed successfully.
     */
    private boolean parseSequence(final String line,
				  final String name,
				  final Map    dataMap)
    {
	if (line.startsWith(";"))
	{
	    // Check the sequence type given by the report
	    if (line.equals("; sq_type: p"))
	    {
		dataMap.put(name, "protein");
		return true;
	    }
	    else if (line.equals("; sq_type: D"))
	    {
		dataMap.put(name, "dna");
		return true;
	    }

	    // Record the coordinates and offset of the alignment
	    if (line.startsWith("; al_start:"))
	    {
		dataMap.put(name + "AlStart", parseCoord(line));
		return true;
	    }
	    else if (line.startsWith("; al_stop:"))
	    {
		dataMap.put(name + "AlStop", parseCoord(line));
		return true;
	    }
	    else if (line.startsWith("; al_display_start:"))
	    {
		dataMap.put(name + "AlDispStart", parseCoord(line));
		return true;
	    }

	    // Query and subject sequence type should never be
	    // different, but check anyway
	    if (dataMap.containsKey("query") && dataMap.containsKey("subject"))
	    {
		if (! dataMap.get("query").equals(dataMap.get("subject")))
		    return false;
	    }
	    return true;
	}

	// Otherwise record the sequence tokens
	if (name.equals("query"))
	{
	    querySeqTokens.append(line);
	}
	else if (name.equals("subject"))
	{	 
	    subjectSeqTokens.append(line);
	}
	return true;
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
