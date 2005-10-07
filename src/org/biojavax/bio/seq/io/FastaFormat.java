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
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.Namespace;
import org.biojavax.SimpleNamespace;
import org.biojavax.RichObjectFactory;
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

public class FastaFormat extends RichSequenceFormat.HeaderlessFormat {
    
    /**
     * The name of this format
     */
    public static final String FASTA_FORMAT = "FASTA";
    
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
        if (!this.getElideComments()) rsiol.setDescription(desc);
        
        StringBuffer seq = new StringBuffer();
        boolean hasMoreSeq = true;
        while (hasMoreSeq) {
            reader.mark(500);
            line = reader.readLine();
            if (line!=null) {
                line = line.trim();
                if (line.charAt(0)=='>') {
                    reader.reset();
                    hasMoreSeq = false;
                } else {
                    seq.append(line);
                }
            } else {
                hasMoreSeq = false;
            }
        }
        if (!this.getElideSymbols()) {
            try {
                SymbolList sl = new SimpleSymbolList(symParser,
                        seq.toString().replaceAll("\\s+","").replaceAll("[\\.|~]","-"));
                rsiol.addSymbols(symParser.getAlphabet(),
                        (Symbol[])(sl.toList().toArray(new Symbol[0])),
                        0, sl.length());
            } catch (Exception e) {
                throw new ParseException(e);
            }
        }
        
        rsiol.endSequence();
        
        return line!=null;
    }
    
    /**
     * {@inheritDoc}
     */
    public void	writeSequence(Sequence seq, PrintStream os) throws IOException {
        if (this.getPrintStream()==null) this.setPrintStream(os);
        this.writeSequence(seq, RichObjectFactory.getDefaultNamespace());
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeSequence(Sequence seq, String format, PrintStream os) throws IOException {
        if (this.getPrintStream()==null) this.setPrintStream(os);
        if (!format.equals(this.getDefaultFormat())) throw new IllegalArgumentException("Unknown format: "+format);
        this.writeSequence(seq, RichObjectFactory.getDefaultNamespace());
    }
    
    /**
     * {@inheritDoc}
     * If namespace is null, then the sequence's own namespace is used.
     */
    public void writeSequence(Sequence seq, Namespace ns) throws IOException {
        RichSequence rs;
        try {
            if (seq instanceof RichSequence) rs = (RichSequence)seq;
            else rs = RichSequence.Tools.enrich(seq);
        } catch (ChangeVetoException e) {
            IOException e2 = new IOException("Unable to enrich sequence");
            e2.initCause(e);
            throw e2;
        }
        
        this.getPrintStream().print(">");
        
        String identifier = rs.getIdentifier();
        if (identifier!=null && !"".equals(identifier)) {
            this.getPrintStream().print("gi|");
            this.getPrintStream().print(identifier);
            this.getPrintStream().print("|");
        }
        this.getPrintStream().print((ns==null?rs.getNamespace().getName():ns.getName()));
        this.getPrintStream().print("|");
        this.getPrintStream().print(rs.getAccession());
        this.getPrintStream().print(".");
        this.getPrintStream().print(rs.getVersion());
        this.getPrintStream().print("|");
        this.getPrintStream().print(rs.getName());
        this.getPrintStream().print(" ");
        this.getPrintStream().println(rs.getDescription().replaceAll("\\n"," "));
        
        int length = rs.length();
        
        for (int pos = 1; pos <= length; pos += this.getLineWidth()) {
            int end = Math.min(pos + this.getLineWidth() - 1, length);
            this.getPrintStream().println(rs.subStr(pos, end));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDefaultFormat() {
        return FASTA_FORMAT;
    }
}
