package tagvalue;

import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.program.tagvalue.*;

public class ParseEmbl {
  public static void main(String[] args)
  throws Exception {
    BufferedReader reader = new BufferedReader(
      new FileReader(
        new File(args[0])
      )
    );
    
    LineSplitParser lsp = LineSplitParser.EMBL;
    
    TagValueListener listener = null;
    if(args.length > 1 && args[1].startsWith("val")) {
      listener = new AnnotationBuilder(AnnotationType.ANY);
    } else {
      listener = new Echo();
    }


    TagDelegator td = new TagDelegator(listener);
    
    LineSplitParser ftParser = new LineSplitParser();
    ftParser.setSplitOffset(15);
    ftParser.setTrimTag(true);
    ftParser.setTrimValue(true);
    ftParser.setContinueOnEmptyTag(true);
    ftParser.setMergeSameTag(false);
    
    TagValueListener ftListener = new FeatureTableListener(listener);

    td.setParserListener("FT", ftParser, ftListener);
    
    Parser parser = new Parser();
    
    while(parser.read(reader, lsp, td)) {
      if(listener instanceof AnnotationBuilder) {
        System.out.println(((AnnotationBuilder) listener).getLast());
      }
    }
  }

  private static class FeatureTableListener
  extends TagValueWrapper {
    private TagValueParser featurePropertyParser = new FeaturePropertyParser();

    private TagValueListener childListener;
    private boolean inLocation;

    public FeatureTableListener() {
      super();
    }

    public FeatureTableListener(TagValueListener delegate) {
      super(delegate);
    }

    public void startRecord()
    throws ParserException  {
      inLocation = false;

      super.startRecord();
    }

    public void endRecord()
    throws ParserException {
      if(inLocation) {
        super.endTag();
      }

      super.endRecord();
    }

    public void value(TagValueContext tvc, Object value)
    throws ParserException {
      String line = (String) value;
      if(line.startsWith("/")) {
        if(inLocation) {
          super.endTag();
          inLocation = false;
        }
        tvc.pushParser(featurePropertyParser, getDelegate());
      } else {
        if(!inLocation) {
          super.startTag("LOCATION");
          inLocation = true;
        }
        super.value(tvc, value);
      }
    }
  }

  private static class FeaturePropertyParser
  implements TagValueParser {
    public TagValue parse(Object value)
    throws ParserException  {
      String line = (String) value;
      if(line.startsWith("/")) {
        int eq = line.indexOf("=");
        if(eq < 0) {
          return new TagValue(line.substring(1), "", true);
        } else {
          String ourTag = line.substring(1, eq);
          String ourValue = line.substring(eq + 1);
          return new TagValue(ourTag, ourValue, true);
        }
      } else {
        return new TagValue("", value, false);
      }
    }
  }
}
