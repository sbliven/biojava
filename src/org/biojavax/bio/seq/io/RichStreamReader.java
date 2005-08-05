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
import java.io.InputStream;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.SequenceFormat;
import org.biojava.bio.seq.io.StreamReader;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;


/**
 * Parses a stream into sequences.
 * <p>
 * This object implements SequenceIterator, so you can loop over each sequence
 * produced. It consumes a stream, and uses a SequenceFormat to extract each
 * sequence from the stream.
 * <p>
 * It is assumed that the stream contains sequences that can be handled by the
 * one format, and that they are not seperated other than by delimiters that the
 * format can handle.
 * <p>
 * Sequences are instantiated when they are requested by nextSequence, not
 * before, so it is safe to use this object to parse a gigabyte fasta file, and
 * do sequence-by-sequence processing, while being guaranteed that RichStreamReader
 * will not require you to keep any of the sequences in memory.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 * @author Richard Holland
 */

public class RichStreamReader extends StreamReader implements RichSequenceIterator {
    
    /**
     * @{inheritDoc}
     */
    public Sequence nextSequence()
    throws NoSuchElementException, BioException {
        return super.nextSequence();
    }
    public RichSequence nextRichSequence()
    throws NoSuchElementException, BioException {
        return (RichSequence)this.nextSequence();
    }
    
    public boolean hasNext() {
        return super.hasNext();
    }
    
    public RichStreamReader(InputStream is,
            SequenceFormat format,
            SymbolTokenization symParser,
            RichSequenceBuilderFactory sf)  {
        super(is,format,symParser,sf);
    }
    
    public RichStreamReader(BufferedReader reader,
            SequenceFormat format,
            SymbolTokenization symParser,
            RichSequenceBuilderFactory sf)  {
        super(reader,format,symParser,sf);
    }
    
    /**
     * @{inheritDoc}
     */
    public void BadLineParsed(org.biojava.utils.ParseErrorEvent theEvent) {
        super.BadLineParsed(theEvent);
    }
}
