package tagvalue;

import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.program.tagvalue.*;

public class ParseSwissprot {
  public static void main(String[] args)
  throws Exception {
    BufferedReader reader = new BufferedReader(
      new FileReader(
        new File(args[0])
      )
    );
    
    TagValueListener listener = null;
    if(args.length > 1 && args[1].startsWith("val")) {
      listener = new AnnotationBuilder(Formats.SWISSPROT_TYPE);
    } else {
      listener = new Echo();
    }
    ParserListener pl = Formats.createSwissprotParserListener(listener);

    Parser parser = new Parser();
    
    while(parser.read(reader, pl.getParser(), pl.getListener())) {
      if(listener instanceof AnnotationBuilder) {
        System.out.println(((AnnotationBuilder) listener).getLast());
      }
    }
  }
}
