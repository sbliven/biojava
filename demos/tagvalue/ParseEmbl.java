package tagvalue;

import java.io.*;
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
    
    AnnotationBuilder annBuilder = new AnnotationBuilder(AnnotationType.ANY);
    TagDelegator td = new TagDelegator(annBuilder);
    
    LineSplitParser ftParser = new LineSplitParser();
    ftParser.setSplitOffset(15);
    ftParser.setTrimTag(true);
    ftParser.setTrimValue(true);
    ftParser.setContinueOnEmptyTag(true);
    ftParser.setMergeSameTag(false);
    
    td.setParserListener("FT", ftParser, annBuilder);
    
    Parser parser = new Parser();
    parser.read(reader, lsp, td);
    
    System.out.println(annBuilder.getLast());    
  }
}
