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
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.SequenceFormat;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojavax.Namespace;
import org.biojavax.bio.db.RichObjectFactory;
import org.biojavax.ontology.ComparableTerm;

/**
 * Allows a file format to be read/written as RichSequences.
 * @author Richard Holland
 */
public interface RichSequenceFormat extends SequenceFormat {
    
    /**
     * Reads a sequence from the given buffered reader using the given tokenizer to parse
     * sequence symbols. Events are passed to the listener, and the namespace used
     * for sequences read is the one given. If the namespace is null, then the default
     * namespace for the parser is used, which may depend on individual implementations
     * of this interface.
     * @param reader the input source
     * @param symParser the tokenizer which understands the sequence being read
     * @param listener the listener to send sequence events to
     * @param ns the namespace to read sequences into.
     * @return true if there is more to read after this, false otherwise.
     * @throws BioException in case of parsing errors.
     * @throws IllegalSymbolException if the tokenizer couldn't understand one of the
     * sequence symbols in the file.
     * @throws IOException if there was a read error.
     */
    public boolean readRichSequence(BufferedReader reader, SymbolTokenization symParser,
            RichSeqIOListener listener,Namespace ns) throws BioException, IllegalSymbolException, IOException;
    
    /**
     * Writes a sequence out to the given outputstream using the default format of the
     * implementing class. If namespace is given, sequences will be written with that
     * namespace, otherwise they will be written with the default namespace of the
     * implementing class (which is usually the namespace of the sequence itself).
     * If you pass this method a sequence which is not a RichSequence, it will attempt to
     * convert it using RichSequence.Tools.enrich(). Obviously this is not going to guarantee
     * a perfect conversion, so it's better if you just use RichSequences to start with!
     * @param seq the sequence to write
     * @param os the place to write it to
     * @param ns the namespace to write it with
     * @throws IOException in case it couldn't write something
     */
    public void writeSequence(Sequence seq, PrintStream os, Namespace ns) throws IOException;
    
    /**
     * @deprecated - use writeSequence(seq,os,ns)
     * Writes a sequence out to the given outputstream using the given format of the
     * implementing class. If namespace is given, sequences will be written with that
     * namespace, otherwise they will be written with the default namespace of the
     * implementing class (which is usually the namespace of the sequence itself).
     * If you pass this method a sequence which is not a RichSequence, it will attempt to
     * convert it using RichSequence.Tools.enrich(). Obviously this is not going to guarantee
     * a perfect conversion, so it's better if you just use RichSequences to start with!
     * @param seq the sequence to write
     * @param format the format to use - depends on the implementing class
     * @param os the place to write it to
     * @param ns the namespace to write it with
     * @throws IOException in case it couldn't write something
     */
    public void writeSequence(Sequence seq, String format, PrintStream os, Namespace ns) throws IOException;
        
    /**
     * Retrive the current line width. Defaults to 80.
     * @return the line width
     */
    public int getLineWidth();
    
    /**
     * Set the line width. When writing, the lines of sequence will never be longer than the line
     * width. Defaults to 80.
     * @param width the new line width
     */
    public void setLineWidth(int width);
    
    /**
     * Is the format going to emit events when sequence data is read?
     * @return true if it is (true is default) otherwise false.
     */
    public boolean getElideSymbols();
    
    /**
     * Use this method to toggle reading of sequence data. If you're only
     * interested in header data set to true.
     * @param elideSymbols set to true if you don't want the sequence data.
     */
    public void setElideSymbols(boolean elideSymbols);
    
    /**
     * Stores a number of useful terms used across many sequence formats for consistency's sake.
     */
    public static class Terms {
        private static ComparableTerm ACCESSION_TERM = null;
        private static ComparableTerm KERYWORDS_TERM = null;
        private static ComparableTerm MODIFICATION_TERM = null;
        private static ComparableTerm MOLTYPE_TERM = null;
        private static ComparableTerm STRANDED_TERM = null;
        private static ComparableTerm IDENTIFIER_TERM = null;
        
        /**
         * Holds a reference to the key that must be used to store PubMed references.
         */
        public static final String PUBMED_KEY = "PUBMED";
        
        /**
         * Holds a reference to the key that must be used to store Medline references.
         */
        public static final String MEDLINE_KEY = "MEDLINE";
        
        /**
         * Holds a reference to the key that must be used to store DOI references.
         */
        public static final String DOI_KEY = "DOI";
        
        /**
         * Getter for the accession term
         * @return A Term that represents the accession tag
         */
        public static ComparableTerm getAccessionTerm() {
            if (ACCESSION_TERM==null) ACCESSION_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("ACCESSION");
            return ACCESSION_TERM;
        }
        
        /**
         * Getter for the keyword term
         * @return a Term that represents the Keyword tag
         */
        public static ComparableTerm getKeywordsTerm() {
            if (KERYWORDS_TERM==null) KERYWORDS_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("KEYWORDS");
            return KERYWORDS_TERM;
        }
        
        /**
         * Getter for the modification term
         * @return a Term
         */
        public static ComparableTerm getModificationTerm() {
            if (MODIFICATION_TERM==null) MODIFICATION_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("MDAT");
            return MODIFICATION_TERM;
        }
        
        /**
         * getter for the MolType term
         * @return a Term that represents the molecule type
         */
        public static ComparableTerm getMolTypeTerm() {
            if (MOLTYPE_TERM==null) MOLTYPE_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("MOLTYPE");
            return MOLTYPE_TERM;
        }
        
        /**
         * Getter for the Strand term
         * @return a Term that represents the Strand tag
         */
        public static ComparableTerm getStrandedTerm() {
            if (STRANDED_TERM==null) STRANDED_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("STRANDED");
            return STRANDED_TERM;
        }
        
        /**
         * getter for the Identifier term
         * @return a Term that represents the secondary/tertiary identifiers of a dbxref (use rank to determine n-ary-ness).
         */
        public static ComparableTerm getIdentifierTerm() {
            if (IDENTIFIER_TERM==null) IDENTIFIER_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("IDENTIFIER");
            return IDENTIFIER_TERM;
        }
    }
}
