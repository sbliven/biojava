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
import java.util.*;
import java.net.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Parses a stream into sequences.
 * <P>
 * This object implements SequenceIterator, so you can loop over each sequence
 * produced. It consumes a stream, and uses a SequenceFormat to extract each
 * sequence from the stream.
 * <P>
 * It is assumed that the stream contains sequences that can be handled by the
 * one format, and that they are not seperated other than by delimiters that the
 * format can handle.
 * <P>
 * Sequences are instantiated when they are requested by nextSequence, not
 * before, so it is safe to use this object to parse a giggabite fasta file, and
 * do sequence-by-sequence processing, while being guaranteed that StreamReader
 * will not require you to keep any of the sequences in memory.
 *
 * @author Matthew Pocock
 */
public class StreamReader implements SequenceIterator {
  /**
   * The context object for parsing from.
   */
  private Context context;
  
  /**
   * The symbol parser.
   */
  private SymbolParser resParser;
  
  /**
   * The sequenc format.
   */
  private SequenceFormat format;
  
  /**
   * The sequence factory.
   */
  private SequenceFactory sf;

  /**
   * Pull the next sequence out of the stream.
   * <P>
   * This method will delegate parsing from the stream to a SequenceFormat
   * object, and then return the resulting sequence.
   *
   * @return the next Sequence
   * @throws NoSuchElementException if the end of the stream has been hit
   * @throws BioException if for any reason the next sequence could not be read
   */
  public Sequence nextSequence()
         throws NoSuchElementException, BioException  {
    if(context.isStreamEmpty())
      throw new NoSuchElementException("Stream is empty");

    try {
      return format.readSequence(context, resParser, sf);
    } catch (Exception e) {
      throw new BioException(e, "Could not read sequence");
    }
  }

  public boolean hasNext() {
    return !context.isStreamEmpty();
  }

  public StreamReader(InputStream is, SequenceFormat format,
                      SymbolParser resParser,
                      SequenceFactory sf)  {
    context = new Context(is);
    this.format = format;
    this.resParser = resParser;
    this.sf = sf;
  }

  public StreamReader(BufferedReader reader, SequenceFormat format,
                      SymbolParser resParser,
                      SequenceFactory sf)  {
    context = new Context(reader);
    this.format = format;
    this.resParser = resParser;
    this.sf = sf;
  }

    /**
     * Encapsulate a stream for reading sequence data.  This
     * is the object which gets passed to SequenceFormats.
     */

  static public class Context {
    private BufferedReader reader;

      /**
       * Get a Reader object for accessing data from the
       * stream.
       *
       * @return A <code>Reader</code> object, or <code>null</code>
       *         if the stream contains no more sequences.
       */

    public BufferedReader getReader() {
      return reader;
    }

      /**
       * Signal that the stream contains no more sequence data.
       * This may or may not correspond to reaching the end of
       * the stream.
       */

    public void streamEmpty() {
      reader = null;
    }
    
      /**
       * Check if the stream is empty.
       */

    public boolean isStreamEmpty() {
      return reader == null;
    }

    public Context(InputStream is) {
      reader = new BufferedReader(new InputStreamReader(is));
    }
    
    public Context(BufferedReader reader) {
      this.reader = reader;
    }
  }
}
