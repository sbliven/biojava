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

import org.biojava.bio.search.*;
import org.biojava.bio.BioException;

/**
 * The <code>SearchBuilder</code> interface is to be used by objects
 * which accumulate state via a SearchContentHandler and then
 * construct a SeqSimilaritySearchResult object.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @version 1.0
 * @since 1.1
 * @see SearchContentHandler
 */
public interface SearchBuilder extends SearchContentHandler
{
    /**
     * The <code>makeSearchResult</code> method returns a
     * SeqSimilaritySearchResult instance created from data
     * accumulated from an associated SearchParser.
     *
     * @return a <code>SeqSimilaritySearchResult</code> object.
     *
     * @exception BioException if an error occurs.
     */
    public SeqSimilaritySearchResult makeSearchResult()
	throws BioException;
}
