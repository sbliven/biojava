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

package org.biojavax.bio.seq.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.ParseException;
import org.biojava.bio.seq.io.SeqIOListener;
import org.biojava.bio.seq.io.StreamParser;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.utils.ParseErrorListener;
import org.biojavax.Namespace;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.db.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;


/**
 * Format object representing FASTA files. These files are almost pure
 * sequence data. The only `sequence property' reported by this parser
 * is PROPERTY_DESCRIPTIONLINE, which is the contents of the
 * sequence's description line (the line starting with a '>'
 * character). Normally, the first word of this is a sequence ID. If
 * you wish it to be interpreted as such, you should use
 * FastaDescriptionLineParser as a SeqIO filter.
 *
 * If you pass it a RichSeqIOListener, you'll get RichSequence objects
 * in return. Likewise, if you write RichSequence objects, you'll get
 * absolutely correct FASTA formatted output.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @author Greg Cox
 * @author Lukas Kall
 * @author Richard Holland
 */

public class FastaFormat implements RichSequenceFormat {
    public static final String DEFAULT_FORMAT = "FASTA";
    
    /**
     * The line width for output.
     */
    protected int lineWidth = 60;
    
    /**
     * Retrive the current line width.
     *
     * @return the line width
     */
    public int getLineWidth() {
        return lineWidth;
    }
    
    /**
     * Set the line width.
     * <p>
     * When writing, the lines of sequence will never be longer than the line
     * width.
     *
     * @param width the new line width
     */
    public void setLineWidth(int width) {
        this.lineWidth = width;
    }
    
    public boolean readSequence(
            BufferedReader reader,
            SymbolTokenization symParser,
            SeqIOListener listener
            )	throws
            IllegalSymbolException,
            IOException,
            ParseException {
        if (!(listener instanceof RichSeqIOListener)) throw new IllegalArgumentException("Only accepting RichSeqIOListeners today");
        return this.readRichSequence(reader,symParser,(RichSeqIOListener)listener,null);
    }
        
    // if ns==null then namespace of sequence in fasta is used
    // if ns==null and namespace of sequence==null then default namespace is used
    public boolean readRichSequence(
            BufferedReader reader,
            SymbolTokenization symParser,
            RichSeqIOListener rsiol,
            Namespace ns
            )	throws
            IllegalSymbolException,
            IOException,
            ParseException {
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("Premature stream end");
        }
        while(line.length() == 0) {
            line = reader.readLine();
            if (line == null) {
                throw new IOException("Premature stream end");
            }
        }
        if (!line.startsWith(">")) {
            throw new IOException("Stream does not appear to contain FASTA formatted data: " + line);
        }
        
        rsiol.startSequence();
        
        String regex = ">(\\S+)(\\s+(.*))*";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(line);
        if (!m.matches()) {
            throw new IOException("Stream does not appear to contain FASTA formatted data: " + line);
        }
        
        String name = m.group(1);
        String desc = m.group(3);
        
        regex = "^(gi\\|(\\d+)\\|)*(\\S+)\\|(\\S+?)(\\.(\\d+))*\\|(\\S+)$";
        p = Pattern.compile(regex);
        m = p.matcher(name);
        if (m.matches()) {
            String gi = m.group(2);
            String namespace = m.group(3);
            String accession = m.group(4);
            String verString = m.group(6);
            int version = verString==null?0:Integer.parseInt(verString);
            name = m.group(7);
            
            rsiol.setAccession(accession);
            rsiol.setVersion(version);            
            if (gi!=null) rsiol.setIdentifier(gi);
            if (ns==null) rsiol.setNamespace((Namespace)RichObjectFactory.getObject(SimpleNamespace.class,new Object[]{namespace}));
            else rsiol.setNamespace(ns);
        } else {
            rsiol.setAccession(name);
            rsiol.setNamespace((ns==null?RichObjectFactory.getDefaultNamespace():ns));
        }
        rsiol.setName(name);
        rsiol.setDescription(desc);
        
        boolean seenEOF = this.readSequenceData(reader, symParser, rsiol);
        
        rsiol.endSequence();
        
        return !seenEOF;
    }
    
    private boolean readSequenceData(
            BufferedReader r,
            SymbolTokenization parser,
            SeqIOListener listener
            ) throws
            IOException,
            IllegalSymbolException {
        char[] cache = new char[512];
        boolean reachedEnd = false, seenEOF = false;
        StreamParser sparser = parser.parseStream(listener);
        
        while (!reachedEnd) {
            r.mark(cache.length + 1);
            int bytesRead = r.read(cache, 0, cache.length);
            if (bytesRead < 0) {
                reachedEnd = seenEOF = true;
            } else {
                int parseStart = 0;
                int parseEnd = 0;
                while (!reachedEnd && parseStart < bytesRead && cache[parseStart] != '>') {
                    parseEnd = parseStart;
                    
                    while (parseEnd < bytesRead &&
                            cache[parseEnd] != '\n' &&
                            cache[parseEnd] != '\r'
                            ) {
                        ++parseEnd;
                    }
                    
                    sparser.characters(cache, parseStart, parseEnd - parseStart);
                    
                    parseStart = parseEnd + 1;
                    while (parseStart < bytesRead &&
                            (cache[parseStart] == '\n' ||
                            cache[parseStart] == '\r') ) {
                        ++parseStart;
                    }
                }
                if (parseStart < bytesRead && cache[parseStart] == '>') {
                    try {
                        r.reset();
                    } catch (IOException ioe) {
                        throw new IOException(
                                "Can't reset: " +
                                ioe.getMessage() +
                                " parseStart=" + parseStart +
                                " bytesRead=" + bytesRead
                                );
                    }
                    if (r.skip(parseStart) != parseStart) {
                        throw new IOException("Couldn't reset to start of next sequence");
                    }
                    reachedEnd = true;
                }
            }
        }
        
        sparser.close();
        return seenEOF;
    }
    
    public void writeSequence(Sequence seq, PrintStream os)
    throws IOException {        
        if (!(seq instanceof RichSequence)) throw new IllegalArgumentException("Sorry, only RichSequence objects accepted");
        this.writeRichSequence((RichSequence)seq, os,null);
    }
    public void writeSequence(Sequence seq, String format, PrintStream os)
    throws IOException {        
        if (!(seq instanceof RichSequence)) throw new IllegalArgumentException("Sorry, only RichSequence objects accepted");
        this.writeRichSequence((RichSequence)seq, format, os, null);
    }
    
    // if ns==null then sequence's namespace is used
    public void writeRichSequence(RichSequence rs, PrintStream os, Namespace ns)
    throws IOException {
        os.print(">");
        
        String identifier = rs.getIdentifier();
        if (identifier!=null && !"".equals(identifier)) {
            os.print("gi|");
            os.print(identifier);
            os.print("|");
        }
        os.print((ns==null?rs.getNamespace().getName():ns.getName()));
        os.print("|");
        os.print(rs.getAccession());
        os.print(".");
        os.print(rs.getVersion());
        os.print("|");
        os.print(rs.getName());
        os.print(" ");
        os.println(rs.getDescription());
        
        int length = rs.length();
        
        for (int pos = 1; pos <= length; pos += this.lineWidth) {
            int end = Math.min(pos + this.lineWidth - 1, length);
            os.println(rs.subStr(pos, end));
        }
    }   
       
    public void writeRichSequence(RichSequence seq, String format, PrintStream os, Namespace ns)
    throws IOException {
        if (! format.equalsIgnoreCase(getDefaultFormat()))
            throw new IllegalArgumentException("Unknown format '"
                    + format
                    + "'");
        this.writeRichSequence(seq, os, ns);
    }
    
    /**
     * <code>getDefaultFormat</code> returns the String identifier for
     * the default format.
     *
     * @return a <code>String</code>.
     * @deprecated
     */
    public String getDefaultFormat() {
        return DEFAULT_FORMAT;
    }
    
    private Vector mListeners = new Vector();
    
    /**
     * Adds a parse error listener to the list of listeners if it isn't already
     * included.
     *
     * @param theListener Listener to be added.
     */
    public synchronized void addParseErrorListener(ParseErrorListener theListener) {
        if (mListeners.contains(theListener) == false) {
            mListeners.addElement(theListener);
        }
    }
    
    /**
     * Removes a parse error listener from the list of listeners if it is
     * included.
     *
     * @param theListener Listener to be removed.
     */
    public synchronized void removeParseErrorListener(ParseErrorListener theListener) {
        if (mListeners.contains(theListener) == true) {
            mListeners.removeElement(theListener);
        }
    }
    
}
