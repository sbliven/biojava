/*
    Copyright (C) 2003 EBI, GRL

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */
 
package org.ensembl.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Extended PrintStream to allow users to print fasta
 * formatted sequence output with an unformatted header, and sequence formatted to a
 * user-specified column width maximum, using a user supplied line ending.  
 * Users should use the normal PrintStream methods to write header elements.  
 * Users should use the various *Sequence methods to print out sequences. 
 * Note, the default line ending is a newline, but this can be over ridden.
 * 
 * @author <a href="mailto:dlondon@ebi.ac.uk">Darin London</a>
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class FormattedSequencePrintStream extends PrintStream {

  private final int maxColumnLen; // default, eg. no limit
  private int currentColumn = 0;
  private static final String DEFAULTLINEEND = "\n"; // default, but can be overridden
  private final String LINEEND;
  
  /**
   * Constructor for a basic FormattedSequencePrintStream, with maxColumn and an underlying OutputStream.
   * The default line ending, a newline, is used to add line endings to sequences.
   * Autoflush is set to false.
   * 
   * @param maxColumnLen - integer, column length max. A maxColumnLen of 0 signals that no formatting should occur
   * @param out - underlying OutputStream for the PrintStream
   */
  public FormattedSequencePrintStream(int maxColumnLen, OutputStream out) {
     this(maxColumnLen, DEFAULTLINEEND, out, false);
  }
  
  public FormattedSequencePrintStream(int maxColumnLen, String lineEnd, OutputStream out) {
    this(maxColumnLen, lineEnd, out, false);
  }
  
	/**
   * Constructor for a FormattedSequencePrintStream, stating whether the underlying PrintStream should be in autoFlush
   * mode, or not. The default line ending, a newline, is used to add line endings to sequences.
   * 
   * @param maxColumnLen - integer, column length max. A maxColumnLen of 0 signals that no formatting should occur
   * @param out - underlying OutputStream for the PrintStream
	 * @param autoFlush - boolean, if true, underlying PrintStream is set to autoFlush true
   * @see java.io.PrintStream
	 */
	public FormattedSequencePrintStream(int maxColumnLen, OutputStream out, boolean autoFlush) {
      this(maxColumnLen, DEFAULTLINEEND, out, autoFlush);
	}

/**
 * Constructor for a FormattedSequencePrintStream, setting the autoFlush and the lineEnd string to
 * print when adding a line end to formatted sequence output.  Useful for output such as HTML, where
 * the newline should be </p> instead of \n.
 * 
 * @param maxColumnLen - integer, column length max. A maxColumnLen of 0 signals that no formatting should occur
 * @param lineEnd - String to end sequence lines with when the formatter inserts a new line to the sequences (default is newline).
 * @param out - underlying OutputStream for the PrintStream
 * @param autoFlush - boolean, if true, underlying PrintStream is set to autoFlush true
 * @see java.io.PrintStream
 */
  public FormattedSequencePrintStream(int maxColumnLen, String lineEnd, OutputStream out, boolean autoFlush) {
    super(out, autoFlush);
    this.maxColumnLen = maxColumnLen;
    this.LINEEND = lineEnd;
  }
  
  /**
   * Constructor for a FormattedSequencePrintStream, with a specified encoding.
   * 
   * @param maxColumnLen - integer, column length max. A maxColumnLen of 0 signals that no formatting should occur
   * @param out - underlying OutputStream for the PrintStream
   * @param encoding - encoding to use for character/string conversion
   * @throws java.io.UnsupportedEncodingException
   * @see java.io.PrintStream
   */  
  public FormattedSequencePrintStream(int maxColumnLen, OutputStream out, String encoding) throws UnsupportedEncodingException {
    this(maxColumnLen, DEFAULTLINEEND, out, false, encoding);
  }
  
	/**
   * Constructor for a FormattedSequencePrintStream, with a specified lineEnd, autoFlush, and encoding.
   * 
   * @param maxColumnLen - integer, column length max. A maxColumnLen of 0 signals that no formatting should occur
   * @param out - underlying OutputStream for the PrintStream
   * @param autoFlush - boolean, if true, underlying PrintStream is set to autoFlush true
	 * @param encoding - encoding to use for character/string conversion
	 * @throws java.io.UnsupportedEncodingException
   * @see java.io.PrintStream
	 */
	public FormattedSequencePrintStream(int maxColumnLen, String lineEnd, OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
    super(out, autoFlush, encoding);
    this.maxColumnLen = maxColumnLen;
    this.LINEEND = lineEnd;      
	}
  
  /**
   * Write out a char of sequence, with formatting.
   * 
   * @param x - char of sequence to be written
   */
  public void printSequence(char x) {
    if (currentColumn == maxColumnLen) {
      currentColumn = 0;
      print(LINEEND);
    }
    print(x);
    currentColumn++;
  }
  
  /**
   * Write out a char[] object of sequence, with formatting.
   *
   * @param x - char[] with sequence to be printed
   */
  public void printSequence(char[] x) {
    for (int i = 0, n = x.length; i < n; i++)
      printSequence(x[i]);
  }
  
  /**
   * Write out a String of sequence.  Resolves to writeSequence(sequence.getBytes()).
   * @param x
   */
  public void printSequence(String x) {
    writeSequence(x.getBytes());
  }
  
  /**
   * Write out a byte of sequence, with formatting.
   * 
   * @param b - byte of sequence to be printed
   */
  public void writeSequence(byte b) {
    if (currentColumn == maxColumnLen) {
      currentColumn = 0;
      print(LINEEND);
    }
    write(b);
    currentColumn++;
  }
  
  /**
   * write an entire byte[] object containing sequence, with formatting.
   * 
   * @param buf - byte[] containing sequence to be printed
   */
  public void writeSequence(byte[] buf) {
    for (int i = 0, n = buf.length; i < n; i++)
      writeSequence(buf[i]);
  }
  
  /**
   * write a portion of a byte[] object containing sequence, with formatting.
   * 
   * @param buf - byte[] containing sequence bytes to be printed
   * @param off - beginning offset, eg. byte[off] will be the first byte printed
   * @param len - number of bytes to print, eg. byte[off] - byte[off + len - 1] will be printed 
   */
  public void writeSequence(byte[] buf, int off, int len) {
    for (int i = off; i < off + len; i++)
      writeSequence(buf[i]);
  }
  
  /**
   * sets the columnCount to 0, to signal that a new output of sequence has begun
   */
  public void resetColumnCount() {
    currentColumn = 0;
  }
}
