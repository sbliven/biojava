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
 
import java.io.*;
import java.util.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.tools.*;
import org.biojava.bio.program.gff.*;

/**
 * This tests the gff code to check that we can read in features, add them to
 * a sequence, and then print something out.
 *
 * @author Matthew Pocock
 */
public class GFFFilter {
  public static void main(String [] args) throws Exception {
    if(args.length > 2) {
      throw new Exception("Use: GFFToFeatures [in.gff [out.gff]]");
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
}
