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

import org.biojava.bio.BioException;
import org.biojava.utils.ParserException;

/**
 * Objects implementing the <code>SearchParser</code> interface are
 * responsible for parsing data from a stream and notifying a
 * SearchContentHandler of significant events.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @version 1.0
 * @since 1.1
 */
public interface SearchParser
{
    /**
     * The <code>parseSearch</code> method parses the next complete
     * search result from the stream encapsulated by the
     * BufferedReader. The SearchContentHandler is informed of
     * events as they occur.
     *
     * @param reader a <code>BufferedReader</code> to read from.
     * @param handler a <code>SearchContentHandler</code> to notify of
     * events.
     *
     * @return a <code>boolean</code> value.
     *
     * @exception IOException if an error occurs in the
     * BufferedReader.
     * @exception BioException if an internal error occurs.
     * @exception ParserException if the parser fails to recognise a
     * line or is unable to extract the required information from a
     * known line type.
     */
    public boolean parseSearch(BufferedReader       reader,
			       SearchContentHandler handler)
	throws IOException, BioException, ParserException;

}
