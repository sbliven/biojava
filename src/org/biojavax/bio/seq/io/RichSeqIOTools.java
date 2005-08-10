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
import java.io.InputStream;
import java.io.OutputStream;

import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.NucleotideTools;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.db.HashSequenceDB;
import org.biojava.bio.seq.db.IDMaker;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.seq.io.StreamWriter;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.utils.AssertionFailure;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;


/**
 * A set of convenience methods for handling common file formats.
 *
 * @author Thomas Down
 * @author Mark Schreiber
 * @author Nimesh Singh
 * @author Matthew Pocock
 * @author Keith James
 * @author Richard Holland
 * @since 1.1
 */
public final class RichSeqIOTools {
    private static RichSequenceBuilderFactory factory = new SimpleRichSequenceBuilderFactory();
    
    /**
     * This can't be instantiated.
     */
    private RichSeqIOTools() {
    }
        
    /**
     * Read a fasta file.
     *
     * @param br    the BufferedReader to read data from
     * @param sTok  a SymbolTokenization that understands the sequences
     * @return      a SequenceIterator over each sequence in the fasta file
     */
    public static RichSequenceIterator readFasta(
            BufferedReader br, SymbolTokenization sTok) {
        return new RichStreamReader(br,
                new FastaFormat(),
                sTok,
                factory);
    }
    
    /**
     * Read a fasta file using a custom type of SymbolList. For example,
     * use SmartSequenceBuilder.FACTORY to emulate readFasta(BufferedReader,
     * SymbolTokenization) and SmartSequenceBuilder.BIT_PACKED to force all
     * symbols to be encoded using bit-packing.
     * @param br the BufferedReader to read data from
     * @param sTok a SymbolTokenization that understands the sequences
     * @param seqFactory a factory used to build a SymbolList
     * @return a <CODE>SequenceIterator</CODE> that iterates over each
     * <CODE>Sequence</CODE> in the file
     */
    public static RichSequenceIterator readFasta(
            BufferedReader br,
            SymbolTokenization sTok,
            RichSequenceBuilderFactory seqFactory) {
        return new RichStreamReader(
                br,
                new FastaFormat(),
                sTok,
                seqFactory);
    }
    
    /**
     * Iterate over the sequences in an FASTA-format stream of DNA sequences.
     * @param br the BufferedReader to read data from
     * @return a <CODE>SequenceIterator</CODE> that iterates over each
     * <CODE>Sequence</CODE> in the file
     */
    public static RichSequenceIterator readFastaDNA(BufferedReader br) {
        return new RichStreamReader(br,
                new FastaFormat(),
                getDNAParser(),
                factory);
    }
    
    /**
     * Iterate over the sequences in an FASTA-format stream of RNA sequences.
     * @param br the BufferedReader to read data from
     * @return a <CODE>SequenceIterator</CODE> that iterates over each
     * <CODE>Sequence</CODE> in the file
     */
    public static RichSequenceIterator readFastaRNA(BufferedReader br) {
        return new RichStreamReader(br,
                new FastaFormat(),
                getRNAParser(),
                factory);
    }
    
    /**
     * Iterate over the sequences in an FASTA-format stream of Protein sequences.
     * @param br the BufferedReader to read data from
     * @return a <CODE>SequenceIterator</CODE> that iterates over each
     * <CODE>Sequence</CODE> in the file
     */
    public static RichSequenceIterator readFastaProtein(BufferedReader br) {
        return new RichStreamReader(br,
                new FastaFormat(),
                getProteinParser(),
                factory);
    }
    
    /**
     * Create a sequence database from a fasta file provided as an
     * input stream.  Note this somewhat duplicates functionality in
     * the readFastaDNA and readFastaProtein methods but uses a stream
     * rather than a reader and returns a SequenceDB rather than a
     * SequenceIterator. If the returned DB is likely to be large then
     * the above mentioned methods should be used.
     * @return a <code>SequenceDB</code> containing all the <code>Sequences</code>
     * in the file.
     * @since 1.2
     * @param seqFile The file containg the fasta formatted sequences
     * @param alpha The <code>Alphabet</code> of the sequence, ie DNA, RNA etc
     * @throws BioException if problems occur during reading of the
     * stream.
     */
    public static SequenceDB readFasta(InputStream seqFile, Alphabet alpha)
    throws BioException {
        HashSequenceDB db = new HashSequenceDB(IDMaker.byName);
        FastaFormat fFormat = new FastaFormat();
        for (RichSequenceIterator seqI = new RichStreamReader(seqFile,
                fFormat,
                alpha.getTokenization("token"),
                factory);seqI.hasNext();) {
            RichSequence seq = seqI.nextRichSequence();
            try {
                db.addSequence(seq);
            } catch (ChangeVetoException cve) {
                throw new AssertionFailure(
                        "Could not successfully add sequence "
                        + seq.getName()
                        + " to sequence database",
                        cve);
            }
        }
        return db;
    }
    
    public static RichSequenceIterator readGenbank(
            BufferedReader br,
            SymbolTokenization sTok,
            RichSequenceBuilderFactory seqFactory) {
        return new RichStreamReader(
                br,
                new GenbankFormat(),
                sTok,
                seqFactory);
    }
    
    public static RichSequenceIterator readGenbankDNA(BufferedReader br) {
        return new RichStreamReader(br,
                new GenbankFormat(),
                getDNAParser(),
                factory);
    }
    
    /**
     * Iterate over the sequences in an FASTA-format stream of RNA sequences.
     * @param br the BufferedReader to read data from
     * @return a <CODE>SequenceIterator</CODE> that iterates over each
     * <CODE>Sequence</CODE> in the file
     */
    public static RichSequenceIterator readGenbankRNA(BufferedReader br) {
        return new RichStreamReader(br,
                new GenbankFormat(),
                getRNAParser(),
                factory);
    }
    
    public static RichSequenceIterator readGenbankProtein(BufferedReader br) {
        return new RichStreamReader(br,
                new GenbankFormat(),
                getProteinParser(),
                factory);
    }
    
    public static SequenceDB readGenbank(InputStream seqFile, Alphabet alpha)
    throws BioException {
        HashSequenceDB db = new HashSequenceDB(IDMaker.byName);
        GenbankFormat fFormat = new GenbankFormat();
        for (RichSequenceIterator seqI = new RichStreamReader(seqFile,
                fFormat,
                alpha.getTokenization("token"),
                factory);seqI.hasNext();) {
            RichSequence seq = seqI.nextRichSequence();
            try {
                db.addSequence(seq);
            } catch (ChangeVetoException cve) {
                throw new AssertionFailure(
                        "Could not successfully add sequence "
                        + seq.getName()
                        + " to sequence database",
                        cve);
            }
        }
        return db;
    }
    
    /**
     * Write a sequenceDB to an output stream in fasta format.
     * @since 1.2
     * @param os the stream to write the fasta formatted data to.
     * @param db the database of <code>Sequence</code>s to write
     * @throws IOException if there was an error while writing.
     */
    public static void writeFasta(OutputStream os, SequenceDB db)
    throws IOException {
        StreamWriter sw = new StreamWriter(os,new FastaFormat());
        sw.writeStream(db.sequenceIterator());
    }
    
    /**
     * Writes sequences from a SequenceIterator to an OutputStream in
     * Fasta Format.  This makes for a useful format filter where a
     * StreamReader can be sent to the StreamWriter after formatting.
     *
     * @since 1.2
     * @param os The stream to write fasta formatted data to
     * @param in The source of input <code>Sequences</code>
     * @throws IOException if there was an error while writing.
     */
    public static void writeFasta(OutputStream os, SequenceIterator in)
    throws IOException {
        StreamWriter sw = new StreamWriter(os,new FastaFormat());
        sw.writeStream(in);
    }
    
    /**
     * Writes a single Sequence to an OutputStream in Fasta format.
     *
     * @param os  the OutputStream.
     * @param seq  the Sequence.
     * @throws IOException if there was an error while writing.
     */
    public static void writeFasta(OutputStream os, RichSequence seq)
    throws IOException {
        writeFasta(os, new SingleRichSeqIterator(seq));
    }
    
    public static void writeGenbank(OutputStream os, SequenceDB db)
    throws IOException {
        StreamWriter sw = new StreamWriter(os,new GenbankFormat());
        sw.writeStream(db.sequenceIterator());
    }
    
    public static void writeGenbank(OutputStream os, SequenceIterator in)
    throws IOException {
        StreamWriter sw = new StreamWriter(os,new GenbankFormat());
        sw.writeStream(in);
    }
    
    public static void writeGenbank(OutputStream os, RichSequence seq)
    throws IOException {
        writeGenbank(os, new SingleRichSeqIterator(seq));
    }
            
    private static SymbolTokenization getDNAParser() {
        try {
            return DNATools.getDNA().getTokenization("token");
        } catch (BioException ex) {
            throw new BioError("Assertion failing:"
                    + " Couldn't get DNA token parser",ex);
        }
    }
    
    private static SymbolTokenization getRNAParser() {
        try {
            return RNATools.getRNA().getTokenization("token");
        } catch (BioException ex) {
            throw new BioError("Assertion failing:"
                    + " Couldn't get RNA token parser",ex);
        }
    }
    
    private static SymbolTokenization getNucleotideParser() {
        try {
            return NucleotideTools.getNucleotide().getTokenization("token");
        } catch (BioException ex) {
            throw new BioError("Assertion failing:"
                    + " Couldn't get nucleotide token parser",ex);
        }
    }
    
    private static SymbolTokenization getProteinParser() {
        try {
            return ProteinTools.getTAlphabet().getTokenization("token");
        } catch (BioException ex) {
            throw new BioError("Assertion failing:"
                    + " Couldn't get PROTEIN token parser",ex);
        }
    }
            
    private static final class SingleRichSeqIterator
            implements RichSequenceIterator {
        private RichSequence seq;
        SingleRichSeqIterator(RichSequence seq) {
            this.seq = seq;
        }
        
        public boolean hasNext() {
            return seq != null;
        }
        
        public Sequence nextSequence() {
            RichSequence seq = this.seq;
            this.seq = null;
            return seq;
        }
        
        public RichSequence nextRichSequence() {
            return (RichSequence)this.nextSequence();
        }
    }
}
