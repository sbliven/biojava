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

/**
 * Format for Fasta files.
 * <P>
 * The description lines often include complicated annotation for sequences.
 * The parsing of these is handled by a FastaDescriptionReader object.
 *
 * @author Matthew Pocock
 */
public class FastaFormat implements SequenceFormat {
  /**
   * The default description reader.
   */
  private static final FastaDescriptionReader DEFAULT_DESCRIPTION_READER;
  
  static {
    DEFAULT_DESCRIPTION_READER = new DefaultDescriptionReader();
  }
  
  /**
   * The description reader.
   */
  private FastaDescriptionReader fdr = DEFAULT_DESCRIPTION_READER;
  
  /**
   * The line width for output.
   */
  private int lineWidth = 60;

  /**
   * Set the descripiton reader.
   *
   * @param dfr the new description reader
   */
  public void setDescriptionReader(FastaDescriptionReader fdr) {
    this.fdr = fdr;
  }
  
  /**
   * Retrieve the current description reader.
   *
   * @return the current description reader
   */
  public FastaDescriptionReader getDescriptionReader() {
    return fdr;
  }
  
  /**
   * Retrive the current line width.
   *
   * @return the line width
   */
  public int getLineWidth() {
    return lineWidth;
  }

  /**
   * Set the line width.
   * <P>
   * When writing, the lines of sequence will never be longer than the line
   * width.
   *
   * @param width the new line width
   */
  public void setLineWidth(int width) {
    this.lineWidth = lineWidth;
  }

  public Sequence readSequence(StreamReader.Context context,
                               SymbolParser resParser,
                               SequenceFactory sf)
         throws IllegalSymbolException, IOException {
    final BufferedReader in = context.getReader();
    StringBuffer sb = new StringBuffer();

    String line;

    // find >
    line = in.readLine();
    while(!line.startsWith(">")) {
      line = in.readLine();
    }
    
    String description = line.substring(1).trim();

    // read in all the sequence up untill > or eof
    ArrayList resList = new ArrayList();
    in.mark(120);
    line = in.readLine();
    while(line != null && !line.startsWith(">")) {
      StringTokenizer st = new StringTokenizer(line, " ", false);
      while(st.hasMoreTokens()) {
        String token = st.nextToken();
        resList.ensureCapacity(resList.size() + getLineWidth());
        resList.addAll(resParser.parse(token).toList());
      }
      in.mark(120);
      line = in.readLine();
    }
    
    if(line == null) {
      context.streamEmpty();
    } else {
      in.reset();
    }

    String [] urnName = fdr.parseURNName(description);
    Sequence seq = sf.createSequence(new SimpleSymbolList(resParser.alphabet(),
                                                           resList), 
                                     urnName[0], urnName[1], null);
    fdr.parseAnnotation(description, seq.getAnnotation());
    return seq;
  }

  public void writeSequence(Sequence seq, PrintStream os) {
    os.print("> ");
    os.println(fdr.writeDescription(seq));

    int length = seq.length();
    for(int i = 1; i <= length; i++) {
      os.print(seq.symbolAt(i).getToken());
      if( (i % lineWidth) == 0)
        os.print("\n");
    }
    if( (length % lineWidth) != 0)
      os.print("\n");
  }
}
