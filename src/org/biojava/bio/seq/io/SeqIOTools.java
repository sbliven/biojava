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

/**
 * A set of convenience methods for handling common file formats.
 *
 * @author Thomas Down
 * @author Nimesh Singh
 * @since 1.1
 */
public class SeqIOTools  {

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

    private static SymbolTokenization getProteinParser() {
        try {
            return ProteinTools.getTAlphabet().getTokenization("token");
        } catch (BioException ex) {
            throw new BioError(ex, "Assertion failing: Couldn't get PROTEIN token parser");
        }
    }

    private static SequenceBuilderFactory _emblBuilderFactory;

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

    private static SequenceBuilderFactory _genbankBuilderFactory;

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

      private static SequenceBuilderFactory _genpeptBuilderFactory;

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


    private static SequenceBuilderFactory _swissprotBuilderFactory;

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

    private static SequenceBuilderFactory _fastaBuilderFactory;

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
   * @author Mark Schreiber
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
   * Write a sequenceDB to an output stream in fasta format
   * @author Mark Schreiber
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
   * @author Mark Schreiber
   * @since 1.2
   */
   public static void writeFasta(OutputStream os, SequenceIterator in) throws IOException{
      StreamWriter sw = new StreamWriter(os,new FastaFormat());
      sw.writeStream(in);
   }

   /**
    * The following methods write sequences from a SequenceIterator to an OutputStream.
    */
    public static void writeEmbl(OutputStream os, SequenceIterator in) throws IOException{
        StreamWriter sw = new StreamWriter(os, new EmblLikeFormat());
        sw.writeStream(in);
    }

    public static void writeSwissprot(OutputStream os, SequenceIterator in) throws IOException, BioException {
        SequenceFormat former = new EmblLikeFormat();
        PrintStream ps = new PrintStream(os);
        while (in.hasNext()) {
            former.writeSequence(in.nextSequence(), "Swissprot", ps);
        }
    }

    public static void writeGenpept(OutputStream os, SequenceIterator in) throws IOException, BioException {
        SequenceFormat former = new GenbankFormat();
        PrintStream ps = new PrintStream(os);
        while (in.hasNext()) {
            former.writeSequence(in.nextSequence(), "Genpept", ps);
        }
    }

    public static void writeGenbank(OutputStream os, SequenceIterator in) throws IOException{
        StreamWriter sw = new StreamWriter(os, new GenbankFormat());
        sw.writeStream(in);
    }
}
