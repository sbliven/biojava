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
}
