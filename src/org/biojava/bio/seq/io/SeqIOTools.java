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

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.db.*;
import org.biojava.utils.*;
import org.apache.regexp.*;


/**
 * A set of convenience methods for handling common file formats.
 *
 * @author Thomas Down
 * @author Mark Schreiber
 * @author Nimesh Singh
 * @author Matthew Pocock
 * @since 1.1
 */
public final class SeqIOTools  {
    //constants representing different filetypes
    public static final int UNKNOWN = 0;
    public static final int FASTADNA = 1;
    public static final int FASTAPROTEIN = 2;
    public static final int EMBL = 3;
    public static final int GENBANK = 4;
    public static final int SWISSPROT = 5;
    public static final int GENPEPT = 6;
    public static final int MSFDNA = 7;
    public static final int FASTA = 8;              //only appropriate for writing
    public static final int FASTAALIGNDNA = 9;
    public static final int MSFPROTEIN = 10;
    public static final int FASTAALIGNPROTEIN = 11;
    public static final int MSF = 12;               //only appropriate for reading

    private static SequenceBuilderFactory _emblBuilderFactory;
    private static SequenceBuilderFactory _genbankBuilderFactory;
    private static SequenceBuilderFactory _genpeptBuilderFactory;
    private static SequenceBuilderFactory _swissprotBuilderFactory;
    private static SequenceBuilderFactory _fastaBuilderFactory;

    /**
     * This can't be instantiated.
     */
    private SeqIOTools() {
    }

    private static SymbolTokenization getDNAParser() {
        try {
            return DNATools.getDNA().getTokenization("token");
        } catch (BioException ex) {
            throw new BioError(ex, "Assertion failing: Couldn't get DNA token parser");
        }
    }

    private static SymbolTokenization getRNAParser() {
        try {
            return RNATools.getRNA().getTokenization("token");
        } catch (BioException ex) {
            throw new BioError(ex, "Assertion failing: Couldn't get RNA token parser");
        }
    }

    private static SymbolTokenization getProteinParser() {
        try {
            return ProteinTools.getTAlphabet().getTokenization("token");
        } catch (BioException ex) {
            throw new BioError(ex, "Assertion failing: Couldn't get PROTEIN token parser");
        }
    }

    /**
     * Get a default SequenceBuilderFactory for handling EMBL
     * files.
     */
    public static SequenceBuilderFactory getEmblBuilderFactory() {
        if (_emblBuilderFactory == null) {
            _emblBuilderFactory = new EmblProcessor.Factory(SimpleSequenceBuilder.FACTORY);
        }
        return _emblBuilderFactory;
    }

    /**
     * Iterate over the sequences in an EMBL-format stream.
     */
    public static SequenceIterator readEmbl(BufferedReader br) {
        return new StreamReader(br,
                                new EmblLikeFormat(),
                                getDNAParser(),
                                getEmblBuilderFactory());
    }

    /**
     * Iterate over the sequences in an EMBL-format stream, but for RNA.
     */
    public static SequenceIterator readEmblRNA(BufferedReader br) {
        return new StreamReader(br,
                                new EmblLikeFormat(),
                                getRNAParser(),
                                getEmblBuilderFactory());
    }

    /**
     * Get a default SequenceBuilderFactory for handling GenBank
     * files.
     */
    public static SequenceBuilderFactory getGenbankBuilderFactory() {
        if (_genbankBuilderFactory == null) {
            _genbankBuilderFactory = new GenbankProcessor.Factory(SimpleSequenceBuilder.FACTORY);
        }
        return _genbankBuilderFactory;
    }

    /**
     * Iterate over the sequences in an GenBank-format stream.
     */
    public static SequenceIterator readGenbank(BufferedReader br) {
        return new StreamReader(br,
                                new GenbankFormat(),
                                getDNAParser(),
                                getGenbankBuilderFactory());
    }

    /**
    * Get a default SequenceBuilderFactory for handling Genpept
    * files.
    */
    public static SequenceBuilderFactory getGenpeptBuilderFactory() {
        if (_genpeptBuilderFactory == null) {
            _genpeptBuilderFactory = new GenbankProcessor.Factory(SimpleSequenceBuilder.FACTORY);
        }
        return _genpeptBuilderFactory;
    }

    /**
    * Iterate over the sequences in an Genpept-format stream.
    */
    public static SequenceIterator readGenpept(BufferedReader br) {
        return new StreamReader(br,
                                new GenbankFormat(),
                                getProteinParser(),
                                getGenpeptBuilderFactory());
    }

    /**
     * Get a default SequenceBuilderFactory for handling Swissprot
     * files.
     */
    public static SequenceBuilderFactory getSwissprotBuilderFactory() {
        if (_swissprotBuilderFactory == null) {
            _swissprotBuilderFactory = new SwissprotProcessor.Factory(SimpleSequenceBuilder.FACTORY);
        }
        return _swissprotBuilderFactory;
    }

    /**
     * Iterate over the sequences in an Swissprot-format stream.
     */
    public static SequenceIterator readSwissprot(BufferedReader br) {
        return new StreamReader(br,
                                new EmblLikeFormat(),
                                getProteinParser(),
                                getSwissprotBuilderFactory());
    }

    /**
     * Get a default SequenceBuilderFactory for handling FASTA
     * files.
     */
    public static SequenceBuilderFactory getFastaBuilderFactory() {
        if (_fastaBuilderFactory == null) {
            _fastaBuilderFactory = new FastaDescriptionLineParser.Factory(SimpleSequenceBuilder.FACTORY);
        }
        return _fastaBuilderFactory;
    }

    /**
     * Iterate over the sequences in an FASTA-format stream of DNA sequences.
     */
    public static SequenceIterator readFastaDNA(BufferedReader br) {
        return new StreamReader(br,
                                new FastaFormat(),
                                getDNAParser(),
                                getFastaBuilderFactory());
    }

    /**
     * Iterate over the sequences in an FASTA-format stream of Protein sequences.
     */
    public static SequenceIterator readFastaProtein(BufferedReader br) {
        return new StreamReader(br,
                                new FastaFormat(),
                                getProteinParser(),
                                getFastaBuilderFactory());
    }

  /**
   * Create a sequence database from a fasta file provided as an input stream.
   * Note this somewhat duplicates functionality in the readFastaDNA and readFastaProtein methods but
   * uses a stream rather than a reader and returns a SequenceDB rather than a SequenceIterator. If
   * the returned DB is likely to be large then the above mentioned methods should be used.
   *
   * @throws BioException if problems occur during reading of the stream.
   * @since 1.2
   */
  public static SequenceDB readFasta(InputStream seqFile, Alphabet alpha) throws BioException{
    HashSequenceDB db = new HashSequenceDB(IDMaker.byName);
    SequenceBuilderFactory sbFact = new FastaDescriptionLineParser.Factory(SimpleSequenceBuilder.FACTORY);
    FastaFormat fFormat = new FastaFormat();
    for(SequenceIterator seqI = new StreamReader(seqFile,fFormat,alpha.getTokenization("token"),sbFact);seqI.hasNext();){
      Sequence seq = seqI.nextSequence();
      try{
        db.addSequence(seq);
      }catch(ChangeVetoException cve){
        throw new NestedError(cve,"Could not successfully add sequence "+seq.getName()+" to sequence database");
      }
    }
    return db;
  }

  /**
   * Write a sequenceDB to an output stream in fasta format.
   *
   * @throws IOException if problems occur during writing.
   * @since 1.2
   */
  public static void writeFasta(OutputStream os, SequenceDB db) throws IOException{
    StreamWriter sw = new StreamWriter(os,new FastaFormat());
    sw.writeStream(db.sequenceIterator());
  }
  /**
   * Writes sequences from a SequenceIterator to an OutputStream in Fasta Format.
   * This makes for a useful format filter where a StreamReader can be sent to the
   * StreamWriter after formatting.
   *
   * @throws IOException if problems occur during writing.
   * @since 1.2
   */
   public static void writeFasta(OutputStream os, SequenceIterator in) throws IOException{
      StreamWriter sw = new StreamWriter(os,new FastaFormat());
      sw.writeStream(in);
   }

   public static void writeFasta(OutputStream os, Sequence seq)
   throws IOException {
     writeFasta(os, new SingleSeqIterator(seq));
   }

   /**
    * The following methods write sequences from a SequenceIterator to an OutputStream.
    */
    public static void writeEmbl(OutputStream os, SequenceIterator in) throws IOException{
        StreamWriter sw = new StreamWriter(os, new EmblLikeFormat());
        sw.writeStream(in);
    }

    public static void writeEmbl(OutputStream os, Sequence seq) throws IOException {
      writeEmbl(os, new SingleSeqIterator(seq));
    }

    public static void writeSwissprot(OutputStream os, SequenceIterator in) throws IOException, BioException {
        SequenceFormat former = new EmblLikeFormat();
        PrintStream ps = new PrintStream(os);
        while (in.hasNext()) {
            former.writeSequence(in.nextSequence(), "Swissprot", ps);
        }
    }

    public static void writeSwissprot(OutputStream os, Sequence seq)
    throws IOException, BioException {
      writeSwissprot(os, new SingleSeqIterator(seq));
    }

    public static void writeGenpept(OutputStream os, SequenceIterator in) throws IOException, BioException {
        SequenceFormat former = new GenbankFormat();
        PrintStream ps = new PrintStream(os);
        while (in.hasNext()) {
            former.writeSequence(in.nextSequence(), "Genpept", ps);
        }
    }

    public static void writeGenpept(OutputStream os, Sequence seq)
    throws IOException, BioException {
      writeGenpept(os, new SingleSeqIterator(seq));
    }

    public static void writeGenbank(OutputStream os, SequenceIterator in) throws IOException{
        StreamWriter sw = new StreamWriter(os, new GenbankFormat());
        sw.writeStream(in);
    }

    public static void writeGenbank(OutputStream os, Sequence seq)
    throws IOException {
      writeGenbank(os, new SingleSeqIterator(seq));
    }


    /**
     * The following methods provide an alternate interface for reading and writing
     * sequences and alignments. (Nimesh Singh).
     *
     */

    /**
     * Attempts to guess the filetype of a file given the name.  For use with
     * the functions below that take an int fileType as a parameter.  The
     * constants used are above.
     */
    public static int guessFileType(File seqFile)
    throws IOException, FileNotFoundException {
        //First tries by matching an extension
        String fileName = seqFile.getName();
        try {
            if ((new RE(".*\\u002eem.*")).match(fileName)) {
                return EMBL;
            }
            else if ((new RE(".*\\u002edat.*")).match(fileName)) {
                return EMBL;
            }
            else if ((new RE(".*\\u002egb.*")).match(fileName)) {
                return GENBANK;
            }
            else if ((new RE(".*\\u002esp.*")).match(fileName)) {
                return SWISSPROT;
            }
            else if ((new RE(".*\\u002egp.*")).match(fileName)) {
                return GENPEPT;
            }
            else if ((new RE(".*\\u002efa.*")).match(fileName)) {
                return guessFastaType(seqFile);
            }
            else if ((new RE(".*\\u002emsf.*")).match(fileName)) {
                return guessMsfType(seqFile);
            }
        } catch (RESyntaxException e) {
            System.out.println("guessFileType -- Problem with regular expression matching for:" + seqFile);
        }

        //Reads the file to guess based on content
        BufferedReader br = new BufferedReader(new FileReader(seqFile));
        String line1 = br.readLine();
        String line2 = br.readLine();
        br.close();

        if (line1.startsWith(">")) {
            return guessFastaType(seqFile);
        }
        else if (line1.startsWith("PileUp")) {
            return guessMsfType(seqFile);
        }
        else if (line1.startsWith("!!AA_MULTIPLE_ALIGNMENT")) {
            return MSFPROTEIN;
        }
        else if (line1.startsWith("!!NA_MULTIPLE_ALIGNMENT")) {
            return MSFDNA;
        }
        else if (line1.startsWith("ID")) {
            for (int i = 0; i < line1.length(); i++) {
                if (Character.toUpperCase(line1.charAt(i)) == 'P' &&
                    Character.toUpperCase(line1.charAt(i+1)) == 'R' &&
                    Character.toUpperCase(line1.charAt(i+2)) == 'T') {
                        return SWISSPROT;
                }
            }
            return EMBL;
        }
        else if (line1.toUpperCase().startsWith("LOCUS")) {
            for (int i = 0; i < line1.length(); i++) {
                if (Character.toUpperCase(line1.charAt(i)) == 'A' &&
                    Character.toUpperCase(line1.charAt(i+1)) == 'A') {
                        return GENPEPT;
                }
            }
            return GENBANK;
        }
        else if (line1.length() >= 45 &&
                 line1.substring(19, 45).equalsIgnoreCase("GENETIC SEQUENCE DATA BANK")) {
            return guessGenType(fileName);
        }
        else {
            // fixme: mrp: We shouldn't have print statements inlibrary code
            //System.out.println("guessFileType -- Could not guess file type.");
            return UNKNOWN;
        }
    }

    /**
     * Helper function for guessFileName.
     */
    private static int guessFastaType(File seqFile) throws IOException, FileNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(seqFile));
        String line = br.readLine();
        line = br.readLine();
        br.close();
        for (int i = 0; i < line.length(); i++) {
            if (Character.toUpperCase(line.charAt(i)) == 'F' ||
                Character.toUpperCase(line.charAt(i)) == 'L' ||
                Character.toUpperCase(line.charAt(i)) == 'I' ||
                Character.toUpperCase(line.charAt(i)) == 'P' ||
                Character.toUpperCase(line.charAt(i)) == 'Q' ||
                Character.toUpperCase(line.charAt(i)) == 'E') {
                    return FASTAPROTEIN;
            }
        }
        return FASTADNA;
    }

    /**
     * Helper function for guessFileName.
     */
    private static int guessMsfType(File seqFile) throws IOException, FileNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(seqFile));
        String line = br.readLine();
        if (line.startsWith("!!NA_MULTIPLE_ALIGNMENT")) {
            return MSFDNA;
        }
        else if (line.startsWith("!!AA_MULTIPLE_ALIGNMENT")) {
            return MSFPROTEIN;
        }
        else {
            while (line.indexOf("Type: ") == -1) {
                line = br.readLine();
            }
            br.close();
            int typeIndex = line.indexOf("Type: ") + 6;
            if (line.substring(typeIndex).startsWith("N")) {
                return MSFDNA;
            }
            else if (line.substring(typeIndex).startsWith("P")) {
                return MSFPROTEIN;
            }
            else {
                System.out.println("guessFileType -- Could not guess file type.");
                return UNKNOWN;
            }
        }
    }

    /**
     * Helper function for guessFileName.
     */
    private static int guessGenType(String fileName) throws IOException, FileNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = br.readLine();
        while (line.indexOf("LOCUS") == -1) {
            line = br.readLine();
        }
        br.close();
        for (int i = 0; i < line.length(); i++) {
            if (Character.toUpperCase(line.charAt(i)) == 'A' &&
                Character.toUpperCase(line.charAt(i+1)) == 'A') {
                    return GENPEPT;
            }
        }
        return GENBANK;
    }

    public static SequenceBuilderFactory fileToFactory(int fileType)
    throws BioException {
      switch(fileType) {
            case FASTADNA:
              return getFastaBuilderFactory();
            case FASTAPROTEIN:
              return getFastaBuilderFactory();
            case EMBL:
              return getEmblBuilderFactory();
            case GENBANK:
              return getGenbankBuilderFactory();
            case SWISSPROT:
              return getSwissprotBuilderFactory();
            case GENPEPT:
              return getGenpeptBuilderFactory();
            default:
              throw new BioException("Unknown format: " + fileType);
      }
    }

    /**
     * Attempts to retreive the most appropriate SequenceBuilder object
     * for some combination of <code>Alphabet</code> and
     * <code>SequenceFormat</code>
     * @param format currently supports <code>FastaFormat</code>, <code>GenbankFormat</code>, <code>EmblLikeFormat</code>
     * @param alpha currently only supports the DNA and Protein alphabets
     * @return the <code>SequenceBuilderFactory</code>
     * @throws BioException if the combination of alpha and format is unrecognized.
     */
    public static SequenceBuilderFactory formatToFactory(
        SequenceFormat format, Alphabet alpha)
        throws BioException{

      if((format instanceof FastaFormat) &&
         (alpha == DNATools.getDNA() || alpha == ProteinTools.getAlphabet())){

        return getFastaBuilderFactory();
      }

      else if(format instanceof GenbankFormat &&
              alpha == DNATools.getDNA()){

        return getGenbankBuilderFactory();
      }

      else if(format instanceof GenbankFormat &&
              alpha == ProteinTools.getAlphabet()){

        return getGenpeptBuilderFactory();
      }

      else if(format instanceof EmblLikeFormat &&
              alpha == DNATools.getDNA()){
        return getEmblBuilderFactory();
      }

      else if(format instanceof EmblLikeFormat &&
              alpha == ProteinTools.getAlphabet()){
        return getSwissprotBuilderFactory();
      }

      else{
        throw new BioException("Unknown combination of Alphabet and Format");
      }
    }

    public static SequenceFormat fileToFormat(int fileType)
    throws BioException {
      switch(fileType) {
            case FASTADNA:
              return new FastaFormat();
            case FASTAPROTEIN:
              return new FastaFormat();
            case EMBL:
              return new EmblLikeFormat();
            case GENBANK:
              return new GenbankFormat();
            case SWISSPROT:
              return new EmblLikeFormat();
            default:
              throw new BioException("Unknown format: " + fileType);
      }
    }



    /**
     * Reads a file and returns the corresponding Biojava object.  You need to cast it as
     * an Alignment or a SequenceIterator as appropriate.
     */
    public static Object fileToBiojava(int fileType, BufferedReader br) {
        switch (fileType) {
            case MSF:
            case MSFDNA:
            case MSFPROTEIN:
            case FASTAALIGNDNA:
            case FASTAALIGNPROTEIN:
                return fileToAlign(fileType, br);
            case FASTADNA:
            case FASTAPROTEIN:
            case EMBL:
            case GENBANK:
            case SWISSPROT:
            case GENPEPT:
                return fileToSeq(fileType, br);
            default:
                // fixme: mrp: don't print a message & return null,
                // throw an exception!
                System.out.println("fileToBiojava -- File type not recognized.");
                return null;
        }
    }

    /**
     * Converts a file to an Biojava alignment.
     */
    private static Alignment fileToAlign(int fileType, BufferedReader br) {
        switch(fileType) {
            case MSF:
            case MSFDNA:
            case MSFPROTEIN:
                return (new MSFAlignmentFormat()).read(br);
            case FASTAALIGNDNA:
            case FASTAALIGNPROTEIN:
                return (new FastaAlignmentFormat()).read(br);
            default:
                System.out.println("fileToAlign -- File type not recognized.");
                return null;
        }
    }

    /**
     * Converts a file to a Biojava sequence.
     */
    private static SequenceIterator fileToSeq(int fileType, BufferedReader br) {
        switch (fileType) {
            case FASTADNA:
                return SeqIOTools.readFastaDNA(br);
            case FASTAPROTEIN:
                return SeqIOTools.readFastaProtein(br);
            case EMBL:
                return SeqIOTools.readEmbl(br);
            case GENBANK:
                return SeqIOTools.readGenbank(br);
            case SWISSPROT:
                return SeqIOTools.readSwissprot(br);
            case GENPEPT:
                return SeqIOTools.readGenpept(br);
            default:
                System.out.println("fileToSeq -- File type not recognized.");
                return null;
        }
    }

    /**
     * Converts a Biojava object to the given filetype.
     */
    public static void biojavaToFile(int fileType, OutputStream os, Object biojava)
                            throws BioException, IOException, IllegalSymbolException {
        switch (fileType) {
            case MSFDNA:
            case MSFPROTEIN:
            case FASTAALIGNDNA:
            case FASTAALIGNPROTEIN:
                alignToFile(fileType, os, (Alignment) biojava);
                break;
            case FASTA:
            case FASTADNA:
            case FASTAPROTEIN:
            case EMBL:
            case GENBANK:
            case SWISSPROT:
            case GENPEPT:
                seqToFile(fileType, os, (SequenceIterator) biojava);
                break;
            default:
                System.out.println("biojavaToFile -- File type not recognized.");
        }
    }

    /**
     * Converts a Biojava alignment to the given filetype.
     */
    private static void alignToFile(int fileType, OutputStream os, Alignment align) throws BioException, IllegalSymbolException {
        switch(fileType) {
            case MSFDNA:
                (new MSFAlignmentFormat()).writeDna(os, align);
                break;
            case MSFPROTEIN:
                (new MSFAlignmentFormat()).writeProtein(os, align);
                break;
            case FASTAALIGNDNA:
                (new FastaAlignmentFormat()).writeDna(os, align);
                break;
            case FASTAALIGNPROTEIN:
                (new FastaAlignmentFormat()).writeProtein(os, align);
                break;
            default:
                System.out.println("alignToFile -- File type not recognized.");
        }
    }

    /**
     * Converts a Biojava sequence to the given filetype.
     */
    private static void seqToFile(int fileType, OutputStream os, SequenceIterator seq) throws IOException, BioException {
        switch (fileType) {
            case FASTADNA:
            case FASTAPROTEIN:
            case FASTA:
                SeqIOTools.writeFasta(os, seq);
                break;
            case EMBL:
                SeqIOTools.writeEmbl(os, seq);
                break;
            case SWISSPROT:
                SeqIOTools.writeSwissprot(os, seq);
                break;
            case GENBANK:
                SeqIOTools.writeGenbank(os, seq);
                break;
            case GENPEPT:
                SeqIOTools.writeGenpept(os, seq);
                break;
            default:
                System.out.println("seqToFile -- File type not recognized.");
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
