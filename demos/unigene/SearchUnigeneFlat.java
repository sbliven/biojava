package unigene;

import java.io.*;
import java.net.*;
import org.biojava.bio.*;
import org.biojava.bio.program.unigene.*;

public class SearchUnigeneFlat {
  public static void main(String[] args)
  throws Exception {
    URL url = new URL(new URL("file:"), args[0]);
    UnigeneDB unigene = UnigeneTools.loadUnigene(url);
    
    for(int i = 1; i < args.length; i++) {
      UnigeneCluster cluster = unigene.getCluster(args[i]);
      System.out.println(cluster.getAnnotation());
    }
  }
}
