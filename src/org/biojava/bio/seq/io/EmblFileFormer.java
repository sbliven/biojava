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

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;

/**
 * <code>EmblFileFormer</code> performs the detailed formatting of
 * EMBL entries for writing to a PrintStream. There is some code
 * dupication with <code>GenbankFileFormer</code> which could be
 * factored out.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class EmblFileFormer implements SeqFileFormer
{
    private ArrayList   fStack = new ArrayList();
    private PrintStream stream;

    static
    {
        SeqFileFormerFactory.addFactory("Embl", new EmblFileFormer.Factory());
    }

    private static class Factory extends SeqFileFormerFactory
    {
        protected SeqFileFormer make()
        {
            return new EmblFileFormer(System.out);
        }
    }

    /**
     * Private <code>EmblFileFormer</code> constructor. Instances are
     * made by the <code>Factory</code>.
     */
    private EmblFileFormer() { }

    /**
     * Creates a new <code>EmblFileFormer</code> object. Instances are
     * made by the <code>Factory</code>.
     *
     * @param stream a <code>PrintStream</code> object.
     */
    private EmblFileFormer(final PrintStream stream)
    {
        this.stream = stream;
    }

    public PrintStream getPrintStream()
    {
        return stream;
    }

    public void setPrintStream(final PrintStream stream)
    {
        this.stream = stream;
    }

    public void setName(final String id) throws ParseException { }

    public void startSequence() throws ParseException { }

    public void endSequence() throws ParseException { }

    public void setURI(final String uri) throws ParseException { }

    public void addSymbols(final Alphabet alpha,
                           final Symbol[] syms,
                           final int      start,
                           final int      length)
        throws IllegalAlphabetException
    {
        int aCount = 0;
        int cCount = 0;
        int gCount = 0;
        int tCount = 0;
        int oCount = 0;

        for (int i = 0; i < syms.length; i++)
        {
            char c = syms[i].getToken();

            switch (c)
            {
                case 'a': case 'A':
                    aCount++;
                    break;
                case 'c': case 'C':
                    cCount++;
                    break;
                case 'g': case 'G':
                    gCount++;
                    break;
                case 't': case 'T':
                    tCount++;
                    break;
                    
                default:
                    oCount++;
            }
        }

        StringBuffer sq = new StringBuffer("XX\nSQ   Sequence ");
        sq.append(length + " BP; ");
        sq.append(aCount + " A; ");
        sq.append(cCount + " C; ");
        sq.append(gCount + " G; ");
        sq.append(tCount + " T; ");
        sq.append(oCount + " other;");

        // Print sequence summary header
        stream.println(sq.toString());

        int fullLine = syms.length / 60;
        int partLine = syms.length % 60;

        int lineCount = fullLine;
        if (partLine > 0)
            lineCount++;

        int lineLens [] = new int [lineCount];

        // All lines are 60, except last (if present)
        Arrays.fill(lineLens, 60);
        lineLens[lineCount - 1] = partLine;

        for (int i = 0; i < lineLens.length; i++)
        {
            // How long is this chunk?
            int len = lineLens[i];

            // Prepare line 80 characters wide
            StringBuffer sb   = new StringBuffer(80);
            char [] emptyLine = new char [80];
            Arrays.fill(emptyLine, ' ');
            sb.append(emptyLine);

            // Prepare a Symbol array same length as chunk
            Symbol [] sa = new Symbol [len];

            // Get symbols and format into blocks of tokens
            System.arraycopy(syms, (i * 60), sa, 0, len);

            String blocks = (SeqFormatTools.formatTokenBlock(sa, 10)).toString();
            sb.replace(5, blocks.length() + 5, blocks);

            // Calculate the running residue count and add to the line
            String count = Integer.toString((i * 60) + len);
            sb.replace((80 - count.length()), 80, count);

            // Print formatted sequence line
            stream.println(sb);
        }

        // Print end of entry
        stream.println("//");
    }

    public void addSequenceProperty(final Object key, final Object value)
        throws ParseException
    {
        if (key.equals(EmblProcessor.PROPERTY_EMBL_ACCESSIONS))
        {
            StringBuffer sb = new StringBuffer("AC   ");
            for (Iterator ai = ((List) value).iterator(); ai.hasNext();)
            {
                sb.append((String) ai.next());
                sb.append(";");
            }
            stream.println(sb);
        }
    }

    public void startFeature(final Feature.Template templ)
        throws ParseException
    {
        // There are 19 spaces in the leader
        String leader = "FT                   ";
        int    strand = 0;

        if (templ instanceof StrandedFeature.Template)
            strand = ((StrandedFeature.Template) templ).strand.getValue();

        StringBuffer lb = SeqFormatTools.formatLocationBlock(templ.location,
                                                             strand,
                                                             leader,
                                                             80);
        lb.replace(5, 5 + templ.type.length(), templ.type);

        stream.println(lb);
    }

    public void endFeature() throws ParseException { }

    public void addFeatureProperty(final Object key, final Object value)
        throws ParseException
    {
        // There are 19 spaces in the leader
        String   leader = "FT                   ";

	// Don't print internal data structures
	if (key.equals(Feature.PROPERTY_DATA_KEY))
	    return;

        // The value may be a collection if several qualifiers of the
        // same type are present in a feature
        if (Collection.class.isInstance(value))
        {
            for (Iterator vi = ((Collection) value).iterator(); vi.hasNext();)
            {
                stream.println(SeqFormatTools.formatQualifierBlock(SeqFormatTools.formatQualifier(key, vi.next()),
                                                                   leader,
                                                                   80));
            }
        }
        else
        {
            stream.println(SeqFormatTools.formatQualifierBlock(SeqFormatTools.formatQualifier(key, value),
                                                               leader,
                                                               80));
        }
    }
}
