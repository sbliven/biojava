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

package org.biojava.bio.program.gff;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;

/**
 * Listens to a stream of GFF events and writes the lines to a
 * <span class="type">PrintWriter</span>.
 * <P>
 * This will ignore all exceptions. Mabey the error-handeling needs to move into
 * an error handeling interface?
 *
 * @author Matthew Pocock
 */
public class GFFWriter implements GFFDocumentHandler {
  /**
   * The destination of the lines.
   */
  private PrintWriter out;
  
  /**
   * Create a new <span class="type">GFFWriter</span> that will write to 
   * <span class="arg">out</span>.
   *
   * @param out  the <span class="type">PrintWriter</span> to write to
   */
  public GFFWriter(PrintWriter out) {
    this.out = out;
  }
  
  public void startDocument() {}
  
  /**
   * Flushes the <span class="type">PrintWriter</span> to make sure that everything is written.
   */
  public void endDocument()   {
    out.flush();
  }
  
  /**
   * Prints the comment directly to the <span class="type">PrintWriter</span>
   * after adding a leading '<code>#</code>'.
   */
  public void commentLine(String comment) {
    out.println("#" + comment);
  }
  
  /**
   * Prints <span class="arg">record</span> to the <span class="type">PrintWriter</span>.
   */
  public void recordLine(GFFRecord record) {
    out.print(
      record.getSeqName() + "\t" +
      record.getSource()  + "\t" +
      record.getFeature() + "\t" +
      record.getStart()   + "\t" +
      record.getEnd()     + "\t"
    );
    double score = record.getScore();
    if(score == GFFRecord.NO_SCORE) {
      out.print(".\t");
    } else {
      out.print(score + "\t");
    }
    
    int strand = record.getStrand();
    if(strand == GFFRecord.POSITIVE_STRAND) {
      out.print("+\t");
    } else if(strand == GFFRecord.NEGATIVE_STRAND) {
      out.print("-\t");
    } else {
      out.print(".\t");
    }
    
    int frame = record.getFrame();
    if(frame == GFFRecord.NO_FRAME) {
      out.print(".");
    } else {
      out.print(frame + "");
    }
    
    Map gaMap = record.getGroupAttributes();
    String ga = SimpleGFFRecord.stringifyAttributes(gaMap);
    if(ga != null && ga.length() > 0) {
      out.print("\t" + ga);
    }
    
    String comment = record.getComment();
    if(comment != null && comment.length() > 0) {
      if(ga != null && ga.length() > 0) {
        out.print(" ");
      }
      out.print(comment);
    }
    
    out.println("");
  }
  
  public void invalidStart(String token, NumberFormatException nfe)
  throws BioException {}
  public void invalidEnd(String token, NumberFormatException nfe)
  throws BioException {}
  public void invalidScore(String token, NumberFormatException nfe)
  throws BioException {}
  public void invalidStrand(String token)
  throws BioException {}
  public void invalidFrame(String token, NumberFormatException nfe)
  throws BioException {}
}
