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
package gff;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;

import org.biojava.bio.program.gff.*;

/**
 * This tests the parsing and writing of GFF features.
 * <p>
 * Use: GFFToFeatures [in.gff [out.gff]]
 * <p>
 * If you do not supply out.gff, then output will go to stdout. If you do not
 * supply in.gff, then input will come from stdin.
 * <P>
 * in.gff will be parsed into a stream of comments and
 * <span class="type">GFFRecord</span>s. These will pass directly to a 
 * <span class="type">GFFWriter</span>, which will write out the GFF.
 * <P>
 * You can extend this simple application to include a filter between the
 * parser and the writer. This would allow you to write a GFFGrep application
 * that only allows through GFF files that have some value in a given column.
 * <P>
 * You may notice that the input is not exactly identical to the output. In
 * particular, white-space will be changed and some missing values will be
 * written according to the GFF2 specification, regardless of what was read
 * in. Also, the attribute value-list text may be re-ordered. None of this
 * affects the validity of the file.
 * <P>
 * Have fun!
 *
 * @author Matthew Pocock
 */
public class GFFFilter {
  public static void main(String [] args) throws Exception {
    Map options = parseArgs(args);

    if(args.length == 0 || args == null || args.containsKey("help")) {
      throw new Exception(
        "Use: GFFToFeatures [options]\n" +
        "    --infile in    read gff from file named in (stdin if absent or -)\n" +
        "    --outfile out  write gff to file named out (stdout if absent or -)\n" +
        "    --source [!]s  source==s or source!=s\n" +
        "    --type [!]t    type==t or type!=t\n" +
        "    --seq [!]s     sequence==s or sequence!=s\n" +
        "    --frame [!]f   frame==f or frame!=f where f is one of 0, 1, 2\n" +
        "    --strand [!]s  strand==s or strand!=s where s is one of ., +, -\n" +
        "    --start [<>]x  start==x or start<x or start>x\n" +
        "    --end [<>]y    end==y or end<y or end>y\n" +
        "    --help         this help message\n"
      );
    }
    
    try {
      BufferedReader in = null;
      PrintWriter out = null;

      if(args.length > 0) {
        in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[0]))));
        if(args.length > 1) {
          out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(args[1]))));
        }
      }
      
      if(in == null) {
        in = new BufferedReader(new InputStreamReader(System.in));
      }
      if(out == null) {
        out = new PrintWriter(new OutputStreamWriter(System.out));
      }

      GFFParser parser = new GFFParser();
      GFFWriter writer = new GFFWriter(out);
      
      parser.parse(in, writer);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private static Map parseArgs(String[] args) {
      
  }
}
