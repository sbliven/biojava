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

import org.biojava.bio.seq.*;

public class StreamReader implements SequenceIterator {
  private Context context;
  private ResidueParser resParser;
  private SequenceFormat format;
  private SequenceFactory sf;

  public Sequence nextSequence()
         throws NoSuchElementException, SeqException  {
    if(context.isStreamEmpty())
      throw new NoSuchElementException("Stream is empty");

    try {
      return format.readSequence(context, resParser, sf);
    } catch (Exception e) {
      throw new SeqException(e, "Could not read sequence");
    }
  }

  public boolean hasNext() {
    return !context.isStreamEmpty();
  }

  public StreamReader(InputStream is, SequenceFormat format,
                      ResidueParser resParser,
                      SequenceFactory sf)  {
    context = new Context(is);
    this.format = format;
    this.resParser = resParser;
    this.sf = sf;
  }

  public StreamReader(BufferedReader reader, SequenceFormat format,
                      ResidueParser resParser,
                      SequenceFactory sf)  {
    context = new Context(reader);
    this.format = format;
    this.resParser = resParser;
    this.sf = sf;
  }

  static public class Context {
    private BufferedReader reader;

    BufferedReader getReader() {
      return reader;
    }

    void streamEmpty() {
      reader = null;
    }
    boolean isStreamEmpty() {
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
