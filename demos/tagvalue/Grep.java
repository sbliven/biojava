package tagvalue;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.biojava.utils.*;
import org.biojava.utils.lsid.*;
import org.biojava.bio.*;
import org.biojava.bio.program.tagvalue.*;
import org.biojava.bio.program.formats.*;

public class Grep {
  public static void main(String[] args)
  throws Exception {
    if(args.length < 3) {
      useageAndExit();
    }

    String formatName = args[0];
    Format format = FormatTools.getFormat(formatName);
    System.err.println("Using format: " + format.getLSID());

    String searchList = args[1];
    String displayList = args[2];

    TagValueListener listener = new Reporter(searchList, displayList);
    ParserListener pl = format.getParserListener(listener);

    Parser parser = new Parser();

    for(int i = 3; i < args.length; i++) {
      BufferedReader reader = new BufferedReader(
        new FileReader(
          new File(args[i]) ));
      while(parser.read(reader, pl.getParser(), pl.getListener())) {
        ;
      }
    }
  }
   
  private static void useageAndExit() {
    System.err.println("Use: tagvalue.Grep toMatch toPrintList fileList");

    System.err.println("\tformat\t: format name. One of");
    System.err.println("\t\tthe name of a class in org.biojava.bio.program.format");
    System.err.println("\t\tfully qualified class name");
    System.err.println("\t\tEither way, the class must be castable to");
    System.err.println("\t\torg.biojava.bio.program.formats.Format.");

    System.exit(1);
  }

  private static class Reporter
  implements TagValueListener {
    private final String searchKey;
    private final Pattern searchVal;
    private final Set dumpVals;

    private int depth;
    private Object tag;
    private boolean shouldDump;
    private boolean goodTag;
    private boolean dumpTag;
    private Annotation values;

    public Reporter(String searchList, String displayList)
    throws Exception {
      dumpVals = new HashSet();

      Pattern slp = Pattern.compile("([^=]+)=([^=]+)");
      Matcher slm = slp.matcher(searchList);
      if(!slm.matches()) {
        throw new Exception(
          "Can't match " + searchList +
           " to " + slp.pattern());
      }
      searchKey = slm.group(1);
      searchVal = Pattern.compile(slm.group(2));

      Pattern dlp = Pattern.compile("[^,]+");
      Matcher dlm = dlp.matcher(displayList);
      while(dlm.find()) {
        dumpVals.add(dlm.group(0));
      }

      depth = 0;
    }

    public void startRecord() {
      if(depth == 0) {
        shouldDump = false;
        values = new SimpleAnnotation();
      }

      depth++;
    }

    public void endRecord() {
      depth--;

      if(depth == 0) {
        if(shouldDump) {
          System.out.println(values);
        }
      }
    }

    public void startTag(Object tag) {
      this.tag = tag;

      goodTag = tag.equals(searchKey);
      dumpTag = dumpVals.contains(tag);
    }

    public void endTag() {
      goodTag = false; // just to make sure
      dumpTag = false; // just to make sure
    }

    public void value(TagValueContext ctxt, Object value) {
      if(goodTag) {
        if(searchVal.matcher(value.toString()).find()) {
          shouldDump = true;
        }
      }

      if(dumpTag) {
        try {
          values.setProperty(tag, value);
        } catch (ChangeVetoException e) {
          throw new Error(e);
        }
      }
    }
  }
}
