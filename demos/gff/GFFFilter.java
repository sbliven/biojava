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

    if(args.length == 0 || options == null || options.containsKey("help")) {
      throw new Exception(
        "Use: GFFToFeatures [options]\n" +
        "    --infile in    read gff from file named in (stdin if absent or -)\n" +
        "    --outfile out  write gff to file named out (stdout if absent or -)\n" +
        "    --source [!]s  source==s or source!=s\n" +
        "    --feature [!]f feature==f or feature!=f\n" +
        "    --seq [!]s     sequence==s or sequence!=s\n" +
        "    --frame [!]f   frame==f or frame!=f where f is one of 0, 1, 2\n" +
        "    --strand [!]s  strand==s or strand!=s where s is one of ., +, -\n" +
        "    --start [<>]x  start==x or start<x or start>x\n" +
        "    --end [<>]y    end==y or end<y or end>y\n" +
        "    --help         this help message\n"
      );
    }

    PrintWriter out = null;
    String outFileName = (String) options.get("outfile");
    if(outFileName == null || outFileName.equals("-")) {
      out = new PrintWriter(new OutputStreamWriter(System.out));
    } else {
      out = new PrintWriter(new FileWriter(new File(outFileName)));
    }

    GFFDocumentHandler handler = new GFFWriter(out);

    for(Iterator ki = options.keySet().iterator(); ki.hasNext(); ) {
      Object key = ki.next();
      handler = processOpt((String) key, (String) options.get(key), handler);
    }

    BufferedReader in = null;
    String inFileName = (String) options.get("infile");
    if(inFileName == null || inFileName.equals("-")) {
      in = new BufferedReader(new InputStreamReader(System.in));
    } else {
      in = new BufferedReader(new FileReader(new File(inFileName)));
    }

    GFFParser parser = new GFFParser();
    parser.parse(in, handler);
  }
  
  private static Map parseArgs(String[] args) {
      Map options = new HashMap();

      for(int i = 0; i < args.length; i+=2) {
        String key = args[i];
        String val = args[i+ 1];
        while(key.startsWith("-")) {
          key = key.substring(1);
        }
        options.put(key, val);
      }

      return options;
  }

  private static GFFDocumentHandler processOpt(
    String key,
    String val,
    GFFDocumentHandler handler
  ) {
    boolean negate = false;

    GFFRecordFilter filter = null;

    if(val.startsWith("!")) {
      negate = true;
      val = val.substring(1);
    }

    if(false) { // syntactic sugar to make it easy to re-order statements
    } else if(key.equals("end")) {
      boolean before = false;
      boolean after = false;
      if(val.startsWith("<")) {
        before = true;
        val = val.substring(1);
      }
      if(val.startsWith(">")) {
        after = true;
        val = val.substring(1);
      }
      int pos = Integer.parseInt(val);
      filter = new EndFilter(pos, before, after);
    } else if(key.equals("start")) {
      boolean before = false;
      boolean after = false;
      if(val.startsWith("<")) {
        before = true;
        val = val.substring(1);
      }
      if(val.startsWith(">")) {
        after = true;
        val = val.substring(1);
      }
      int pos = Integer.parseInt(val);
      filter = new StartFilter(pos, before, after);
    } else if(key.equals("strand")) {
      filter = new GFFRecordFilter.StrandFilter(StrandParser.parseStrand(val));
    } else if(key.equals("frame")) {
      filter = new GFFRecordFilter.FrameFilter(Integer.parseInt(val));
    } else if(key.equals("source")) {
      filter = new GFFRecordFilter.SourceFilter(val);
    } else if(key.equals("feature")) {
      filter = new GFFRecordFilter.FeatureFilter(val); 
    } else if(key.equals("seq")) {
      filter = new GFFRecordFilter.SequenceFilter(val);
    } else {
      return handler; // unrecognized option
    }

    if(negate == true) {
      filter = new GFFRecordFilter.NotFilter(filter);
    }

    return new GFFFilterer(handler, filter);
  }

  private abstract static class SE
  implements GFFRecordFilter {
    private int pos;
    private boolean before;
    private boolean after;

    protected SE(int pos, boolean before, boolean after) {
      this.pos = pos;
      this.before = before;
      this.after = after;
    }

    public int getPos() {
      return pos;
    }

    public boolean isBefore() {
      return before;
    }

    public boolean isAfter() {
      return after;
    }

    public boolean accept(GFFRecord record) {
      int co = getCoordinate(record);
      return
        (before && co < pos)
        ||
        (after && co > pos)
        ||
        (co == pos);
    }

    protected abstract int getCoordinate(GFFRecord record);
  }

  private static class StartFilter extends SE {
    StartFilter(int pos, boolean before, boolean after) { super(pos, before, after); }
    protected int getCoordinate(GFFRecord record) {
      return record.getStart();
    }
  }

  private static class EndFilter extends SE {
    EndFilter(int pos, boolean before, boolean after) { super(pos, before, after); }
    protected int getCoordinate(GFFRecord record) {
      return record.getEnd();
    }
  }
}
