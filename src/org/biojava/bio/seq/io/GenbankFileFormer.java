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

    // Main sequence formatting buffer
    private StringBuffer sq = new StringBuffer();
    // Main qualifier formatting buffer
    private StringBuffer qb = new StringBuffer();
    // Utility formatting buffer
    private StringBuffer ub = new StringBuffer();

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
    protected GenbankFileFormer() { }

    /**
     * Creates a new <code>GenbankFileFormer</code> object. Instances
     * are made by the <code>Factory</code>.
     *
     * @param stream a <code>PrintStream</code>.
     */
    protected GenbankFileFormer(PrintStream stream)
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

	int end = start + length - 1;

	for (int i = start; i <= end; i++)
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

	sq.setLength(0);
	sq.append("BASE COUNT    ");
	sq.append(aCount + " a ");
	sq.append(aCount + " c ");
	sq.append(aCount + " g ");
	sq.append(aCount + " t");
	sq.append(nl);
	sq.append("ORIGIN");
	sq.append(nl);

	// Print sequence summary header
	stream.println(sq);

	int fullLine = length / 60;
	int partLine = length % 60;

	int lineCount = fullLine;
	if (partLine > 0)
	    lineCount++;

	int lineLens [] = new int [lineCount];

	// All lines are 60, except last (if present)
	Arrays.fill(lineLens, 60);
	lineLens[lineCount - 1] = partLine;

	// Prepare line 80 characters wide, sequence is subset of this
	char [] emptyLine = new char [80];

	for (int i = 0; i < lineLens.length; i++)
	{
	    // Empty the sequence buffer
	    sq.setLength(0);
	    // Empty the utility buffer
	    ub.setLength(0);

	    // How long is this chunk?
	    int len = lineLens[i];

	    // Prep the whitespace
	    Arrays.fill(emptyLine, ' ');
	    sq.append(emptyLine);

	    // Prepare a Symbol array same length as chunk
	    Symbol [] sa = new Symbol [len];

	    // Get symbols and format into blocks of tokens
	    System.arraycopy(syms, start + (i * 60), sa, 0, len);

	    String blocks = (formatTokenBlock(ub, sa, 10)).toString();

	    sq.replace(10, blocks.length() + 10, blocks);

	    // Calculate the running residue count and add to the line
	    String count = Integer.toString((i * 60) + 1);
	    sq.replace((9 - count.length()), 9, count);

	    // Print formatted sequence line
	    stream.println(sq);
	}

	// Print end of entry
	stream.println("//");
    }


    public void addSequenceProperty(Object key, Object value)
	throws ParseException
    {
	if (key.equals(GenbankProcessor.PROPERTY_GENBANK_ACCESSIONS))
	{
	    ub.setLength(0);
	    ub.append("ACCESSION   ");
	    for (Iterator ai = ((List) value).iterator(); ai.hasNext();)
	    {
		ub.append((String) ai.next());
	    }
	    stream.println(ub);
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

	ub.setLength(0);
	ub.append(leader);

	StringBuffer lb = formatLocationBlock(ub,
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
		qb.setLength(0);
		ub.setLength(0);
		StringBuffer fb = formatQualifierBlock(qb,
						       formatQualifier(ub, key, vi.next()).toString(),
						       leader,
						       80);
		stream.println(fb);
	    }
	}
	else
	{
            qb.setLength(0);
            ub.setLength(0);
	    StringBuffer fb = formatQualifierBlock(qb,
						   formatQualifier(ub, key, value).toString(),
						   leader,
						   80);
	    stream.println(fb);
	}
    }
}
