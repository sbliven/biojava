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
import org.biojava.bio.seq.io.AlignIOConstants;
import org.biojava.bio.seq.io.FastaFormat;
import org.biojava.bio.seq.io.SeqIOConstants;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.bio.seq.io.StreamWriter;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
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
    private static RichSequenceBuilderFactory _factory;
    
    /**
     * This can't be instantiated.
     */
    private RichSeqIOTools() {
    }
    
    /**
     * Get a default SequenceBuilderFactory for handling EMBL
     * files.
     * @return a <CODE>SmartSequenceBuilder.FACTORY</CODE>
     */
    private static RichSequenceBuilderFactory getFactory() {
        if (_factory == null) {
            _factory = new SimpleRichSequenceBuilderFactory();
        }
        return _factory;
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
                getFactory());
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
                getFactory());
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
                getFactory());
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
                getFactory());
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
        RichSequenceBuilderFactory sbFact = getFactory();
        FastaFormat fFormat = new FastaFormat();
        for (RichSequenceIterator seqI = new RichStreamReader(seqFile,
                fFormat,
                alpha.getTokenization("token"),
                sbFact);seqI.hasNext();) {
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
    public static void writeFasta(OutputStream os, Sequence seq)
    throws IOException {
        writeFasta(os, new SingleSeqIterator(seq));
    }
    
    /**
     * <code>identifyFormat</code> performs a case-insensitive mapping
     * of a pair of common sequence format name (such as 'embl',
     * 'genbank' or 'fasta') and alphabet name (such as 'dna', 'rna',
     * 'protein', 'aa') to an integer. The value returned will be one
     * of the public static final fields in
     * <code>SeqIOConstants</code>, or a bitwise-or combination of
     * them. The method will reject known illegal combinations of
     * format and alphabet (such as swissprot + dna) by throwing an
     * <code>IllegalArgumentException</code>. It will return the
     * <code>SeqIOConstants.UNKNOWN</code> value when either format or
     * alphabet are unknown.
     *
     * @param formatName a <code>String</code>.
     * @param alphabetName a <code>String</code>.
     *
     * @return an <code>int</code>.
     */
    public static int identifyFormat(String formatName, String alphabetName) {
        int format, alpha;
        if (formatName.equalsIgnoreCase("raw")) {
            format = SeqIOConstants.RAW;
        } else if (formatName.equalsIgnoreCase("fasta")) {
            format = SeqIOConstants.FASTA;
        } else if (formatName.equalsIgnoreCase("nbrf")) {
            format = SeqIOConstants.NBRF;
        } else if (formatName.equalsIgnoreCase("ig")) {
            format = SeqIOConstants.IG;
        } else if (formatName.equalsIgnoreCase("embl")) {
            format = SeqIOConstants.EMBL;
        } else if (formatName.equalsIgnoreCase("swissprot") ||
                formatName.equalsIgnoreCase("swiss")) {
            if (alphabetName.equalsIgnoreCase("aa") ||
                    alphabetName.equalsIgnoreCase("protein")) {
                return SeqIOConstants.SWISSPROT;
            } else {
                throw new IllegalArgumentException("Illegal format and alphabet "
                        + "combination "
                        + formatName
                        + " + "
                        + alphabetName);
            }
        } else if (formatName.equalsIgnoreCase("genbank")) {
            format = SeqIOConstants.GENBANK;
        } else if (formatName.equalsIgnoreCase("genpept")) {
            if (alphabetName.equalsIgnoreCase("aa") ||
                    alphabetName.equalsIgnoreCase("protein")) {
                return SeqIOConstants.GENPEPT;
            } else {
                throw new IllegalArgumentException("Illegal format and alphabet "
                        + "combination "
                        + formatName
                        + " + "
                        + alphabetName);
            }
        } else if (formatName.equalsIgnoreCase("refseq")) {
            format = SeqIOConstants.REFSEQ;
        } else if (formatName.equalsIgnoreCase("gcg")) {
            format = SeqIOConstants.GCG;
        } else if (formatName.equalsIgnoreCase("gff")) {
            format = SeqIOConstants.GFF;
        } else if (formatName.equalsIgnoreCase("pdb")) {
            if (alphabetName.equalsIgnoreCase("aa") ||
                    alphabetName.equalsIgnoreCase("protein")) {
                return SeqIOConstants.PDB;
            } else {
                throw new IllegalArgumentException("Illegal format and alphabet "
                        + "combination "
                        + formatName
                        + " + "
                        + alphabetName);
            }
        } else if (formatName.equalsIgnoreCase("phred")) {
            if (alphabetName.equalsIgnoreCase("dna")) {
                return SeqIOConstants.PHRED;
            } else {
                throw new IllegalArgumentException("Illegal format and alphabet "
                        + "combination "
                        + formatName
                        + " + "
                        + alphabetName);
            }
        } else if (formatName.equalsIgnoreCase("clustal")) {
            format = AlignIOConstants.CLUSTAL;
        } else if (formatName.equalsIgnoreCase("msf")) {
            format = AlignIOConstants.MSF;
        } else {
            return SeqIOConstants.UNKNOWN;
        }
        
        if (alphabetName.equalsIgnoreCase("dna")) {
            alpha = SeqIOConstants.DNA;
        } else if (alphabetName.equalsIgnoreCase("rna")) {
            alpha = SeqIOConstants.RNA;
        } else if (alphabetName.equalsIgnoreCase("aa") ||
                alphabetName.equalsIgnoreCase("protein")) {
            alpha = SeqIOConstants.AA;
        } else {
            return SeqIOConstants.UNKNOWN;
        }
        
        return (format | alpha);
    }
    
    /**
     * <code>getAlphabet</code> accepts a value which represents a
     * sequence format and returns the relevant
     * <code>FiniteAlphabet</code> object.
     *
     * @param identifier an <code>int</code> which represents a binary
     * value with bits set according to the scheme described in
     * <code>SeqIOConstants</code>.
     *
     * @return a <code>FiniteAlphabet</code>.
     *
     * @exception BioException if an error occurs.
     */
    public static FiniteAlphabet getAlphabet(int identifier)
    throws BioException {
        
        // Mask the sequence format bytes
        int alphaType = identifier & (~ 0xffff);
        if (alphaType == 0)
            throw new IllegalArgumentException("No alphabet was set in the identifier");
        
        switch (alphaType) {
            case SeqIOConstants.DNA:
                return DNATools.getDNA();
            case SeqIOConstants.RNA:
                return RNATools.getRNA();
            case SeqIOConstants.AA:
                return ProteinTools.getTAlphabet();
            default:
                throw new BioException("No FiniteAlphabet available for "
                        + "alphabet identifier '"
                        + identifier
                        + "'");
        }
    }
    
    /**
     * Reads a file with the specified format and alphabet
     * @param formatName the name of the format eg genbank or
     * swissprot (case insensitive)
     * @param alphabetName the name of the alphabet eg dna or rna or
     * protein (case insensitive)
     * @param br a BufferedReader for the input
     * @return either an Alignment object or a SequenceIterator
     * (depending on the format read)
     * @throws BioException if an error occurs while reading or a
     * unrecognized format, alphabet combination is used (eg swissprot
     * and DNA).
     *
     * @since 1.3
     */
    public static Object fileToBiojava(String formatName,
            String alphabetName,
            BufferedReader br)
            throws BioException {
        
        int fileType = identifyFormat(formatName, alphabetName);
        
        return fileToBiojava(fileType, br);
    }
    
    /**
     * Reads a file and returns the corresponding Biojava object. You
     * need to cast it as an Alignment or a SequenceIterator as
     * appropriate.
     * @param fileType a value that describes the file type
     * @param br the reader for the input
     * @throws org.biojava.bio.BioException if the file cannot be parsed
     * @return either a <code>SequenceIterator</code> if the file type is a
     * sequence file, or a <code>Alignment</code> if the file is a sequence
     * alignment.
     */
    public static Object fileToBiojava(int fileType, BufferedReader br)
    throws BioException {
        
        // Mask the sequence format bytes
        int alphaType = fileType & (~ 0xffff);
        if (alphaType == 0)
            throw new IllegalArgumentException("No alphabet was set in the identifier");
        
        // Mask alphabet bytes
        int formatType = fileType & (~ 0xffff0000);
        if (formatType == 0)
            throw new IllegalArgumentException("No format was set in the identifier");
        
        switch (fileType) {
            case AlignIOConstants.MSF_DNA:
            case AlignIOConstants.MSF_AA:
            case AlignIOConstants.FASTA_DNA:
            case AlignIOConstants.FASTA_AA:
                return SeqIOTools.fileToBiojava(fileType, br);
            case SeqIOConstants.FASTA_DNA:
            case SeqIOConstants.FASTA_AA:
                return fileToSeq(fileType, br);
            default:
                throw new BioException("Unknown file type '"
                        + fileType
                        + "'");
        }
    }
    
    /**
     * Writes a Biojava <code>SequenceIterator</code>,
     * <code>SequenceDB</code>, <code>Sequence</code> or <code>Aligment</code>
     * to an <code>OutputStream</code>
     *
     * @param formatName eg fasta, GenBank (case insensitive)
     * @param alphabetName eg DNA, RNA (case insensititve)
     * @param os where to write to
     * @param biojava the object to write
     * @throws BioException problems getting data from the biojava object.
     * @throws IOException if there are IO problems
     * @throws IllegalSymbolException a Symbol cannot be parsed
     */
    public static void biojavaToFile(String formatName, String alphabetName,
            OutputStream os, Object biojava)
            throws BioException, IOException, IllegalSymbolException{
        int fileType = identifyFormat(formatName,alphabetName);
        biojavaToFile(fileType, os, biojava);
    }
    
    /**
     * Converts a Biojava object to the given filetype.
     * @param fileType a value that describes the type of sequence file
     * @param os the stream to write the formatted results to
     * @param biojava a <code>SequenceIterator</code>, <code>SequenceDB</code>,
     * <code>Sequence</code>, or <code>Alignment</code>
     * @throws org.biojava.bio.BioException if <code>biojava</code> cannot be
     * converted to that format.
     * @throws java.io.IOException if the output cannot be written to
     * <code>os</code>
     * @throws org.biojava.bio.symbol.IllegalSymbolException if <code>biojava
     * </code> contains a <code>Symbol</code> that cannot be understood by the
     * parser.
     */
    public static void biojavaToFile(int fileType, OutputStream os,
            Object biojava)
            throws BioException, IOException, IllegalSymbolException {
        switch (fileType) {
            case AlignIOConstants.MSF_DNA:
            case AlignIOConstants.MSF_AA:
            case AlignIOConstants.FASTA_DNA:
            case AlignIOConstants.FASTA_AA:
                SeqIOTools.biojavaToFile(fileType, os, biojava);
                break;
            case SeqIOConstants.FASTA_DNA:
            case SeqIOConstants.FASTA_AA:
                if(biojava instanceof SequenceDB){
                    seqToFile(fileType, os, ((SequenceDB)biojava).sequenceIterator());
                }else if(biojava instanceof Sequence){
                    seqToFile(fileType, os, new SingleSeqIterator((Sequence)biojava));
                }else{
                    seqToFile(fileType, os, (SequenceIterator) biojava);
                }
                break;
            default:
                throw new BioException("Unknown file type '"
                        + fileType
                        + "'");
        }
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
            
    /**
     * Converts a file to a Biojava sequence.
     */
    private static SequenceIterator fileToSeq(int fileType,
            BufferedReader br)
            throws BioException {
        switch (fileType) {
            case SeqIOConstants.FASTA_DNA:
                return RichSeqIOTools.readFastaDNA(br);
            case SeqIOConstants.FASTA_AA:
                return RichSeqIOTools.readFastaProtein(br);
            default:
                throw new BioException("Unknown file type '"
                        + fileType
                        + "'");
        }
    }
        
    /**
     * Converts a Biojava sequence to the given filetype.
     */
    private static void seqToFile(int fileType, OutputStream os,
            SequenceIterator seq)
            throws IOException, BioException {
        switch (fileType) {
            case SeqIOConstants.FASTA_DNA:
            case SeqIOConstants.FASTA_AA:
                RichSeqIOTools.writeFasta(os, seq);
                break;
            default:
                throw new BioException("Unknown file type '"
                        + fileType
                        + "'");
        }
    }
    
    private static final class SingleSeqIterator
            implements SequenceIterator {
        private Sequence seq;
        SingleSeqIterator(Sequence seq) {
            this.seq = seq;
        }
        
        public boolean hasNext() {
            return seq != null;
        }
        
        public Sequence nextSequence() {
            Sequence seq = this.seq;
            this.seq = null;
            return seq;
        }
    }
}
