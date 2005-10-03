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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.ParseException;
import org.biojava.bio.seq.io.SeqIOListener;
import org.biojava.bio.seq.io.StreamParser;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.Namespace;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.db.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;


/**
 * Format object representing FASTA files. These files are almost pure
 * sequence data.
 * @author Thomas Down
 * @author Matthew Pocock
 * @author Greg Cox
 * @author Lukas Kall
 * @author Richard Holland
 */

public class FastaFormat implements RichSequenceFormat {
    
    /**
     * The name of this format
     */
    public static final String FASTA_FORMAT = "FASTA";
    
    /**
     * The line width for output.
     */
    private int lineWidth = 60;
    
    /**
     * {@inheritDoc}
     */
    public int getLineWidth() {
        return lineWidth;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setLineWidth(int width) {
        this.lineWidth = width;
    }    
    
    
    /**
     * {@inheritDoc}
     */
    public boolean getElideSymbols() {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setElideSymbols(boolean elideSymbols) {
        if (elideSymbols) throw new IllegalArgumentException("Are you kidding me? Fasta is nothing but symbols!");
    }
    
    /**
     * {@inheritDoc}
     */
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
    
    /**
     * {@inheritDoc}
     * If namespace is null, then the namespace of the sequence in the fasta is used.
     * If the namespace is null and so is the namespace of the sequence in the fasta,
     * then the default namespace is used.
     */
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
    
    // reads sequence data from the file by concatenating the whole lot
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
    
    /**
     * {@inheritDoc}
     */
    public void writeSequence(Sequence seq, PrintStream os) throws IOException {
        this.writeSequence(seq, getDefaultFormat(), os, null);
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeSequence(Sequence seq, String format, PrintStream os) throws IOException {
        this.writeSequence(seq, format, os, null);
    }
    
    /**
     * {@inheritDoc}
     * If namespace is null, then the sequence's own namespace is used.
     */
    public void writeSequence(Sequence seq, PrintStream os, Namespace ns) throws IOException {
        this.writeSequence(seq, getDefaultFormat(), os, ns);
    }
    
    /**
     * {@inheritDoc}
     * If namespace is null, then the sequence's own namespace is used.
     */
    public void writeSequence(Sequence seq, String format, PrintStream os, Namespace ns) throws IOException {
        if (! format.equalsIgnoreCase(getDefaultFormat()))
            throw new IllegalArgumentException("Unknown format '"
                    + format
                    + "'");
        
        RichSequence rs;
        try {
            if (seq instanceof RichSequence) rs = (RichSequence)seq;
            else rs = RichSequence.Tools.enrich(seq);
        } catch (ChangeVetoException e) {
            IOException e2 = new IOException("Unable to enrich sequence");
            e2.initCause(e);
            throw e2;
        }
        
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
    
    /**
     * {@inheritDoc}
     */
    public String getDefaultFormat() {
        return FASTA_FORMAT;
    }        
}
