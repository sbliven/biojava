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

public class StreamWriter {
  private SequenceFormat format;
  private PrintStream os;

  public void writeStream(SequenceIterator ss)
              throws IOException {
    while(ss.hasNext()) {
      try {
        format.writeSequence(ss.nextSequence(), os);
      } catch (SeqException se) {
        se.printStackTrace();
      }
    }
  }

  public StreamWriter(OutputStream os, SequenceFormat format) {
    this.os = new PrintStream(os);
    this.format = format;
  }
}
