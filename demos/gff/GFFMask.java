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
 * @author Matthew Pocock
 */
public class GFFMask {
  public static void main(String [] args) throws Exception {
    Map options = parseArgs(args);

    if(args.length == 0 || options == null || options.containsKey("help")) {
      throw new Exception(
        "Use: GFFToFeatures [options]\n" +
        "    --infile in            read gff from file named in (stdin if absent or -)\n" +
        "    --outfile out          write gff to file named out (stdout if absent or -)\n" +
        "    --maskfile mask        read the masking gff from file named mask (stdin if absent or -)\n" +
        "    --mode contain|overlap features should either be totaly contained within or overlap the mask\n" +
        "    --hits accept|reject   features hitting the mask will be accepted or rejected\n" +
        "    --use_seq_names y|n    mask hits should count only if seq names match if y\n"
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

    BufferedReader in = null;
    String inFileName = (String) options.get("infile");
    if(inFileName == null || inFileName.equals("-")) {
      in = new BufferedReader(new InputStreamReader(System.in));
    } else {
      in = new BufferedReader(new FileReader(new File(inFileName)));
    }

    BufferedReader maskIn = null;
    String maskInFileName = (String) options.get("maskfile");
    if(maskInFileName == null || maskInFileName.equals("-")) {
      maskIn = new BufferedReader(new InputStreamReader(System.in));
    } else {
      maskIn = new BufferedReader(new FileReader(new File(maskInFileName)));
    }

    GFFParser parser = new GFFParser();

    Mask mask = makeMask(options, parser, maskIn);

    handler = makeHandler(options, mask, handler);
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

  private static Mask makeMask(Map options, GFFParser parser, BufferedReader maskIn)
  throws Exception {
    MaskBuilder mb = null;

    String use_seq_names = (String) options.get("use_seq_names"); 
    if(use_seq_names == null || use_seq_names.equals("y")) {
      mb = new MaskBuilderY();
    } else {
      mb = new MaskBuilderN();
    }

    parser.parse(maskIn, mb);

    return mb.getMask();
  }

  private static GFFDocumentHandler makeHandler(Map options, final Mask mask, GFFDocumentHandler handler) {
    String hits = (String) options.get("hits");
    boolean negate;
    if(hits != null && hits.equals("reject")) {
      negate = false;
    } else if(hits == null || hits.equals("accept")) {
      negate = true;
    } else {
      throw new IllegalArgumentException("Unknown hits option: " + hits);
    }

    String mode = (String) options.get("mode");
    GFFRecordFilter filter = null;
    if(mode == null || mode.equals("contain")) {
      filter = new GFFRecordFilter() {
        public boolean accept(GFFRecord rec) {
          return mask.getLocation(rec).contains(new RangeLocation(rec.getStart(), rec.getEnd()));
        }
      };
    } else if(mode != null && mode.equals("overlap")) {
      filter = new GFFRecordFilter() {
        public boolean accept(GFFRecord rec) {
          return mask.getLocation(rec).overlaps(new RangeLocation(rec.getStart(), rec.getEnd()));
        }
      };
    } else {
      throw new IllegalArgumentException("Unknown mode option: " + mode);
    }

    if(negate == true) {
      filter = new GFFRecordFilter.NotFilter(filter);
    }

    return new GFFFilterer(handler, filter);
  }

  private static abstract class MaskBuilder
  implements GFFDocumentHandler {
    public void commentLine(String comment) {}
    public void endDocument() {}
    public void startDocument(String locator) {}

    protected abstract Mask getMask();
  }

  private static class MaskBuilderY
  extends MaskBuilder {
    private Map negSeq2Loc = new HashMap();
    private Map posSeq2Loc = new HashMap();

    public void recordLine(GFFRecord rec) {
      StrandedFeature.Strand strand = rec.getStrand();
      if(strand == StrandedFeature.POSITIVE || strand == StrandedFeature.UNKNOWN) {
        processRec(posSeq2Loc, rec);
      }
      if(strand == StrandedFeature.NEGATIVE || strand == StrandedFeature.UNKNOWN) {
        processRec(negSeq2Loc, rec);
      }
    }

    private void processRec(Map hits, GFFRecord rec) {
      Location loc = (Location) hits.get(rec.getSeqName());
      Location range = new RangeLocation(rec.getStart(), rec.getEnd());

      if(loc == null) {
        loc = range;
      } else {
        loc = LocationTools.union(loc, range);
      }

      hits.put(rec.getSeqName(), loc);
    }

    protected Mask getMask() {
      return new Mask() {
        public Location getLocation(GFFRecord rec) {
          Map m;
          StrandedFeature.Strand s = rec.getStrand();
          if(s == StrandedFeature.POSITIVE || s == StrandedFeature.UNKNOWN) {
            m = posSeq2Loc;
          } else {
            m = negSeq2Loc;
          }
          return (Location) m.get(rec.getSeqName());
        }
      };
    }
  }

  private static class MaskBuilderN
  extends MaskBuilder {
    Location loc = null;

    public void recordLine(GFFRecord rec) {
      Location range = new RangeLocation(rec.getStart(), rec.getEnd());
      if(loc == null) {
        loc = range;
      } else {
        loc = LocationTools.union(loc, range);
      }
    }

    protected Mask getMask() {
      return new Mask() {
        public Location getLocation(GFFRecord rec) {
          return loc;
        }
      };
    }
  }

  private static interface Mask {
    public Location getLocation(GFFRecord rec);
  }

}
