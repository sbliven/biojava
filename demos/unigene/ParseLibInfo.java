package unigene;

import java.io.*;
import org.biojava.bio.*;
import org.biojava.bio.program.tagvalue.*;
import org.biojava.bio.program.unigene.*;

public class ParseLibInfo {
  public static void main(String[] args)
  throws Exception {
    File dataFile = new File(args[0]);
    BufferedReader br = new BufferedReader(
      new FileReader(dataFile)
    );
    
    if(args.length > 1 && args[1].startsWith("val")) {
      Parser parser = new Parser();
      AnnotationBuilder ab = new AnnotationBuilder(UnigeneTools.UNIGENE_ANNOTATION);
      ParserListener pl = UnigeneTools.buildLibInfoParser(ab);
      
      while(parser.read(br, pl.getParser(), pl.getListener())) {
        System.out.println(ab.getLast());
      }
    } else {
      Parser parser = new Parser();
      Echo echo = new Echo();
      ParserListener pl = UnigeneTools.buildLibInfoParser(echo);
      
      while(parser.read(br, pl.getParser(), pl.getListener())) {
      }
    }
  }
}
