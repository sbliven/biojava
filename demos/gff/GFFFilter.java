import java.io.*;
import java.util.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.tools.*;
import org.biojava.bio.program.gff.*;

/**
 * This tests the gff code to check that we can read in features, add them to
 * a sequence, and then print something out.
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
        } else {
          out = new PrintWriter(new OutputStreamWriter(System.out));
        }
      } else {
        in = new BufferedReader(new InputStreamReader(System.in));
      }

      GFFParser parser = new GFFParser();
      GFFWriter writer = new GFFWriter(out);
      
      parser.parse(in, writer);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
