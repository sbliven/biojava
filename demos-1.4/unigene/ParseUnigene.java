package unigene;

import java.io.*;
import org.biojava.bio.*;
import org.biojava.bio.program.tagvalue.*;
import org.biojava.bio.program.unigene.*;

public class ParseUnigene {
  public static void main(String[] args)
  throws Exception {
    File dataFile = new File(args[0]);
    BufferedReader br = new BufferedReader(
      new FileReader(dataFile)
    );
    
    Parser parser = new Parser();
    AnnotationBuilder ab = new AnnotationBuilder(UnigeneTools.UNIGENE_ANNOTATION);
    ParserListener pl = UnigeneTools.buildParser(ab);
    
    while(parser.read(br, pl.getParser(), pl.getListener())) {
      System.out.println(ab.getLast());
    }
  }
}
