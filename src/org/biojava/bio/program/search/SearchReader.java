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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.utils.ParserException;

/**
 * A <code>SearchReader</code> parses a stream into result objects. It
 * implements the standard Java Iterator interface. It uses a
 * SearchParser to extract data from the stream encapsulated by the
 * reader and a SearchBuilder to coordinate assembly of the data into
 * result objects.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.1
 * @see Iterator
 */
public class SearchReader implements Iterator
{
    private BufferedReader reader;
    private SearchBuilder  handler;
    private SearchParser   parser;
    private boolean moreSearchesAvailable = true;

    /**
     * Creates a new <code>SearchReader</code> instance.
     *
     * @param reader a <code>BufferedReader</code> object
     * encapsulating the stream to be parsed.
     * @param handler a <code>SearchBuilder</code> object to
     * coordinate data and construct the result.
     * @param parser a <code>SearchParser</code> object to perform the
     * parsing.
     */
    public SearchReader(final BufferedReader reader,
			final SearchBuilder  handler,
			final SearchParser   parser)
    {
	this.reader  = reader;
	this.handler = handler;
	this.parser  = parser;
    }

    /**
     * <code>hasNext</code> returns whether there are more search
     * results in the stream.
     *
     * @return a <code>boolean</code> value.
     */
    public boolean hasNext()
    {
	if (! moreSearchesAvailable)
	    try
	    {
		reader.close();
	    }
	    catch (IOException ioe)
	    {
		ioe.printStackTrace();
	    }
	return moreSearchesAvailable;
    }

    /**
     * <code>next</code> returns the next Object from the stream. It
     * is up to the client to cast this to a suitable search result.
     *
     * @return an Object.
     *
     * @exception NoSuchElementException if there is no <i>valid</i>
     * result in the stream.
     */
    public Object next()
	throws NoSuchElementException
    {
	if (! moreSearchesAvailable)
	    throw new NoSuchElementException("Attempt to read search result from empty stream");

	try
	{
	    moreSearchesAvailable = parser.parseSearch(reader, handler);
	    return handler.makeSearchResult();
	}
	catch (ParserException pe)
	{
	    throw new NoSuchElementException("No valid search result could be parsed from this stream: "
					     + "parse failed at line "
					     + pe.getLineNumber()
					     + " of input");
	}
	catch (IOException ioe)
	{
	    throw new NoSuchElementException("No valid search result could be parsed from this stream");
	}
	catch (BioException be)
	{
	    throw new NoSuchElementException("No valid search result could be parsed from this stream: "
					     + be.getMessage());
	}
    }

    /**
     * The <code>remove</code> method is a stub which simply throws
     * UnsupportedOperationException as required by the Iterator
     * interface.
     *
     * @exception UnsupportedOperationException when called.
     */
    public void remove()
	throws UnsupportedOperationException
    {
	throw new UnsupportedOperationException();
    }
}
