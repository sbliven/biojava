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

import java.util.Map;

import org.biojava.bio.BioException;

/**
 * <code>SearchContentHandler</code> is a notification interface for
 * objects which listen to search stream parsers. This is applicable
 * to all types of search results which are represented by flat files
 * created by external programs e.g. Fasta, (T)BlastN/PX, EMBOSS
 * programs. This is not limited to sequence similarity searches, but
 * includes any format consisting of a header followed by hits, each
 * of which may, or may not, have subhits.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.1
 */
public interface SearchContentHandler
{
    /**
     * <code>getMoreSearches</code> returns the state of the
     * <code>SearchContentHandler</code> with respect to further
     * searches from its data source. Used for handling streams of
     * search results.
     *
     * @return a <code>boolean</code> value.
     */
    public boolean getMoreSearches();

    /**
     * <code>setMoreSearches</code> sets the state of the
     * <code>SearchContentHandler</code>'s expectation of receiving
     * more results. Used for handling streams of search results.
     *
     * @param value a <code>boolean</code> value.
     */
    public void setMoreSearches(boolean value);

    /**
     * The <code>startSearch</code> method indicates the start of
     * useful search information.
     */
    public void startSearch();

    /**
     * The <code>endSearch</code> method indicates the end of useful
     * search information.
     */
    public void endSearch();

    /**
     * The <code>startHeader</code> method indicates the start of a
     * formatted header. This usually contains information relevant to
     * the search as a whole.
     */
    public void startHeader();

    /**
     * The <code>endHeader</code> method indicates the end of a
     * formatted header.
     */
    public void endHeader();

    /**
     * The <code>startHit</code> method indicates the start of a
     * formatted hit. This could be a single line, or a block of
     * lines.
     */
    public void startHit();

    /**
     * The <code>endHit</code> method indicates the end of a formatted
     * hit.
     */
    public void endHit();

    /**
     * The <code>startSubHit</code> method indicates the start of a
     * formatted subhit. There may be zero or more of these per hit.
     */
    public void startSubHit();

    /**
     * The <code>endSubHit</code> method indicates the end of a
     * formatted subhit.
     */
    public void endSubHit();

    /**
     * The <code>addSearchProperty</code> method adds a key/value pair
     * containing some property of the overall search result.
     *
     * @param key an <code>Object</code>.
     * @param value an <code>Object</code>.
     */
    public void addSearchProperty(Object key, Object value);

    /**
     * The <code>addHitProperty</code> method adds a key/value pair
     * containing some property of a particular hit.
     *
     * @param key an <code>Object</code>.
     * @param value an <code>Object</code>.
     */
    public void addHitProperty(Object key, Object value);

    /**
     * The <code>addSubHitProperty</code> method adds a key/value pair
     * containing some property of a particular subhit.
     *
     * @param key an <code>Object</code>.
     * @param value an <code>Object</code>.
     */
    public void addSubHitProperty(Object key, Object value);

    /**
     * <code>setQuerySeq</code> identifies the query sequence as being
     * known by a particular name, ID or URN.
     *
     * @param identifier a <code>String</code> which should be an
     * unambiguous identifer for the sequence.
     *
     * @exception BioException if the sequence cannot be obtained from
     * the identifier.
     */
    public void setQuerySeq(String identifier)
	throws BioException;

    /**
     * <code>setSubjectDB</code> identifies the database searched
     * by means of a name, ID or URN.
     *
     * @param id a <code>String</code> which should be an unambiguous
     * identifier for the database searched.
     *
     * @exception BioException if the database cannot be obtained from
     * the identifier.
     */
    public void setSubjectDB(String identifier)
	throws BioException;
}
