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

public class FastaFormat implements SequenceFormat {
  private FastaDescriptionReader fdr;
  private int lineWidth = 60;

  public int getLineWidth() {
    return lineWidth;
  }

  public void setLineWidth(int width) {
    this.lineWidth = lineWidth;
  }

  public Sequence readSequence(StreamReader.Context context,
                               ResidueParser resParser,
                               SequenceFactory sf)
         throws IllegalResidueException, IOException {
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
    List resList = new ArrayList();
    in.mark(120);
    line = in.readLine();
    while(line != null && !line.startsWith(">")) {
      StringTokenizer st = new StringTokenizer(line, " ", false);
      while(st.hasMoreTokens()) {
        String token = st.nextToken();
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
    Sequence seq = sf.createSequence(new SimpleResidueList(resParser.alphabet(),
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
      os.print(seq.residueAt(i).getSymbol());
      if( (i % lineWidth) == 0)
        os.print("\n");
    }
    if( (length % lineWidth) != 0)
      os.print("\n");
  }

  public FastaFormat(FastaDescriptionReader fdr) {
    this.fdr = fdr;
  }
  
  public FastaFormat() {
    this.fdr = new DefaultDescriptionReader();
  }
}
