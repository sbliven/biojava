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

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.Symbol;

/**
 * <code>GenbankFileFormer</code> performs the detailed formatting of
 * Genbank entries for writing to a <code>PrintStream</code>. There is
 * some code dupication with <code>EmblFileFormer</code> which could
 * be factored out.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class GenbankFileFormer extends AbstractGenEmblFileFormer
    implements SeqFileFormer
{
    private PrintStream stream;

    static
    {
	SeqFileFormerFactory.addFactory("Genbank", new GenbankFileFormer.Factory());
    }

    private static class Factory extends SeqFileFormerFactory
    {
	protected SeqFileFormer make()
	{
	    return new GenbankFileFormer(System.out);
	}
    }

    /**
     * Private <code>GenbankFileFormer</code> constructor. Instances
     * are made by the <code>Factory</code>.
     */
    private GenbankFileFormer() { }

    /**
     * Creates a new <code>GenbankFileFormer</code> object. Instances
     * are made by the <code>Factory</code>.
     *
     * @param stream a <code>PrintStream</code> object.
     */
    private GenbankFileFormer(PrintStream stream)
    {
	this.stream = stream;
    }

    public PrintStream getPrintStream()
    {
	return stream;
    }

    public void setPrintStream(PrintStream stream)
    {
	this.stream = stream;
    }

    public void setName(String id) throws ParseException { }

    public void startSequence() throws ParseException { }

    public void endSequence() throws ParseException { }

    public void setURI(String uri) throws ParseException { }

    public void addSymbols(Alphabet  alpha,
			   Symbol [] syms,
			   int       start,
			   int       length)
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

	// Get separator for system
	String nl = System.getProperty("line.separator");

	StringBuffer sq = new StringBuffer("BASE COUNT    ");
	sq.append(aCount + " a ");
	sq.append(aCount + " c ");
	sq.append(aCount + " g ");
	sq.append(aCount + " t");
	sq.append(nl);
	sq.append("ORIGIN");
	sq.append(nl);

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

	    String blocks = (formatTokenBlock(new StringBuffer(sa.length),
					      sa,
					      10)).toString();

	    sb.replace(10, blocks.length() + 10, blocks);

	    // Calculate the running residue count and add to the line
	    String count = Integer.toString((i * 60) + 1);
	    sb.replace((9 - count.length()), 9, count);

	    // Print formatted sequence line
	    stream.println(sb);
	}

	// Print end of entry
	stream.println("//");
    }


    public void addSequenceProperty(Object key, Object value)
	throws ParseException
    {
	if (key.equals(GenbankProcessor.PROPERTY_GENBANK_ACCESSIONS))
	{
	    StringBuffer sb = new StringBuffer("ACCESSION   ");
	    for (Iterator ai = ((List) value).iterator(); ai.hasNext();)
	    {
		sb.append((String) ai.next());
	    }
	    stream.println(sb);
	}
    }

    public void startFeature(Feature.Template templ)
	throws ParseException
    {
	// There are 21 spaces in the leader
	String leader = "                     ";
	int    strand = 0;

	if (templ instanceof StrandedFeature.Template)
	    strand = ((StrandedFeature.Template) templ).strand.getValue();

	StringBuffer lb = formatLocationBlock(new StringBuffer(leader),
					      templ.location,
					      strand,
					      leader,
					      80);

	lb.replace(5, 5 + templ.type.length(), templ.type);

	stream.println(lb);
    }

    public void endFeature() throws ParseException { }

    public void addFeatureProperty(Object key, Object value)
	throws ParseException
    {
	// There are 21 spaces in the leader
	String   leader = "                     ";

	// Don't print internal data structures
	if (key.equals(Feature.PROPERTY_DATA_KEY))
	    return;

	// The value may be a collection if several qualifiers of the
	// same type are present in a feature
	if (Collection.class.isInstance(value))
	{
	    for (Iterator vi = ((Collection) value).iterator(); vi.hasNext();)
	    {
		StringBuffer sb = formatQualifierBlock(new StringBuffer(),
						       formatQualifier(key, vi.next()),
						       leader,
						       80);
		stream.println(sb);
	    }
	}
	else
	{
	    StringBuffer sb = formatQualifierBlock(new StringBuffer(),
						   formatQualifier(key, value),
						   leader,
						   80);
	    stream.println(sb);
	}
    }
}
