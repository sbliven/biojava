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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.biojava.bio.BioException;
import org.biojava.bio.BioError;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;

/**
 * <code>EmblFileFormer</code> performs the detailed formatting of
 * EMBL entries for writing to a PrintStream. There is some code
 * duplication with <code>GenbankFileFormer</code> which could be
 * factored out.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class EmblFileFormer extends AbstractGenEmblFileFormer
    implements SeqFileFormer
{
    private ArrayList    fStack = new ArrayList();
    private PrintStream  stream;

    // Main sequence formatting buffer
    private StringBuffer sq = new StringBuffer();
    // Main qualifier formatting buffer
    private StringBuffer qb = new StringBuffer();
    // Utility formatting buffer
    private StringBuffer ub = new StringBuffer();

    // Buffers for each possible sequence property line
    private StringBuffer idb = null;
    private StringBuffer acb = null;
    private StringBuffer dtb = null;
    private StringBuffer deb = null;
    private StringBuffer svb = null;
    private StringBuffer kwb = null;
    private StringBuffer osb = null;
    private StringBuffer ocb = null;
    private StringBuffer ccb = null;
    private StringBuffer ftb = new StringBuffer();


    private SymbolTokenization dnaTokenization;

    {
	try {
	    dnaTokenization = DNATools.getDNA().getTokenization("token");
	} catch (Exception ex) {
	    throw new BioError(ex, "Couldn't initialize tokenizer for the DNA alphabet");
	}
    }

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
     * @param stream a <code>PrintStream</code>.
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

    public void setName(final String id) throws ParseException {
        idb = new StringBuffer("ID   " + id);
    }

    public void startSequence() throws ParseException { }

    public void endSequence() throws ParseException { }

    public void setURI(final String uri) throws ParseException { }

    public void addSymbols(final Alphabet  alpha,
                           final Symbol [] syms,
                           final int       start,
                           final int       length)
        throws IllegalAlphabetException
    {
	try {
	    int aCount = 0;
	    int cCount = 0;
	    int gCount = 0;
	    int tCount = 0;
	    int oCount = 0;

	    int end = start + length - 1;


	    for (int i = start; i <= end; i++)
		{
		    char c = dnaTokenization.tokenizeSymbol(syms[i]).charAt(0);

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

            // My Changes are here
            if (idb != null) {stream.println(idb); stream.println("XX");}
            if (acb != null) {stream.println(acb); stream.println("XX");}
            if (svb != null) {stream.println(svb); stream.println("XX");}
            if (dtb != null) {stream.println(dtb); stream.println("XX");}
            if (deb != null) {stream.println(deb); stream.println("XX");}
            if (kwb != null) {stream.println(kwb); stream.println("XX");}
            if (osb != null) {stream.println(osb);}
            if (ocb != null) {stream.println(ocb); stream.println("XX");}
            if (ccb != null) {stream.println(ccb); stream.println("XX");}
            if (ftb.length() != 0) {
                ftb.insert(0, "FH   Key             Location/Qualifiers" + nl);
                stream.print(ftb);
            }

	    sq.setLength(0);
	    sq.append("XX");
	    sq.append(nl);
	    sq.append("SQ   Sequence ");
	    sq.append(length + " BP; ");
	    sq.append(aCount + " A; ");
	    sq.append(cCount + " C; ");
	    sq.append(gCount + " G; ");
	    sq.append(tCount + " T; ");
	    sq.append(oCount + " other;");

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

		    String blocks = (formatTokenBlock(ub, sa, 10, dnaTokenization)).toString();

		    sq.replace(5, blocks.length() + 5, blocks);

		    // Calculate the running residue count and add to the line
		    String count = Integer.toString((i * 60) + len);
		    sq.replace((80 - count.length()), 80, count);

		    // Print formatted sequence line
		    stream.println(sq);
		}

	    // Print end of entry
	    stream.println("//");
	} catch (IllegalSymbolException ex) {
	    throw new IllegalAlphabetException(ex, "DNA not tokenizing");
	}
    }

    private String sequenceBufferCreator(Object key, Object value) {
        StringBuffer temp = new StringBuffer();

        if (value == null) {
            temp.append((String) key);
        }
        else if (value instanceof ArrayList) {
            Iterator iter = ((ArrayList) value).iterator();
            while (iter.hasNext()) {
                temp.append((String) key + "   " + iter.next());
                if (iter.hasNext())
                    temp.append(nl);
            }
        }
        else {
            StringTokenizer valueToke = new StringTokenizer((String) value, " ");
            int fullline = 80;
            int length = 0;
            if (valueToke.hasMoreTokens()) {
                String token = valueToke.nextToken();

                while (true) {
                    temp.append((String) key + "  ");
                    length = (temp.length() % (fullline + 1)) + token.length() + 1;
                    if (temp.length() % (fullline + 1) == 0) length = 81 + token.length();
                    while (length <= fullline && valueToke.hasMoreTokens()) {
                        temp.append(" " + token);
                        token = valueToke.nextToken();
                        length = (temp.length() % (fullline + 1)) + token.length() + 1;
                        if (temp.length() % (fullline + 1) == 0) length = 81 + token.length();
                    }
                    if (valueToke.hasMoreTokens()) {
                        for(int i = length-token.length(); i < fullline; i++) {
                            temp.append(" ");
                        }
                        temp.append(nl);
                    }
                    else if (length <= fullline) {
                        temp.append(" " + token);
                        break;
                    }
                    else {
                        temp.append(nl);
                        temp.append((String) key + "   " + token);
                        break;
                    }
                }
            }
        }

        return temp.toString();
    }

    public void addSequenceProperty(final Object key, final Object value)
        throws ParseException
    {
        if (key.equals("ID")) {
            idb.setLength(0);
            idb.append("ID   " + (String) value);
        }
        else if (key.equals("DT") || key.equals("MDAT")) {
            dtb = new StringBuffer(sequenceBufferCreator("DT", value));
        }
        else if (key.equals("DE") || key.equals("DEFINITION")) {
            deb = new StringBuffer(sequenceBufferCreator("DE", value));
        }
        else if (key.equals("SV") || key.equals("VERSION")) {
            svb = new StringBuffer(sequenceBufferCreator("SV", value));
        }
        else if (key.equals("KW") || key.equals("KEYWORDS")) {
            kwb = new StringBuffer(sequenceBufferCreator("KW", value));
        }
        else if (key.equals("OS") || key.equals("SOURCE")) {
            osb = new StringBuffer(sequenceBufferCreator("OS", value));
        }
        else if (key.equals("OC") || key.equals("ORGANISM")) {
            ocb = new StringBuffer(sequenceBufferCreator("OC", value));
        }
        else if (key.equals("CC") || key.equals("COMMENT")) {
            ccb = new StringBuffer(sequenceBufferCreator("CC", value));
        }
        else if (key.equals(EmblProcessor.PROPERTY_EMBL_ACCESSIONS))
        {
	    acb = new StringBuffer();
            acb.append("AC   ");
            for (Iterator ai = ((List) value).iterator(); ai.hasNext();)
            {
                acb.append((String) ai.next());
                acb.append(";");
            }
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

	ub.setLength(0);
	ub.append(leader);

        StringBuffer lb = formatLocationBlock(ub,
					      templ.location,
					      strand,
					      leader,
					      80);

        lb.replace(5, 5 + templ.type.length(), templ.type);

        ftb.append(lb + nl);
    }

    public void endFeature() throws ParseException { }

    public void addFeatureProperty(final Object key, final Object value)
        throws ParseException
    {
        // There are 19 spaces in the leader
        String leader = "FT                   ";

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
                ftb.append(fb + nl);
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
            ftb.append(fb + nl);
        }
    }
}
