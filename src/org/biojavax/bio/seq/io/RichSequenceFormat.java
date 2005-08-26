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
}
