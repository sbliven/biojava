package indexing;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.program.tagvalue.*;
import org.biojava.bio.program.indexdb.*;

public class CreateIndex {
  public static void main(String[] args)
  throws Throwable {
    File storeFile = new File(args[0]);
    File spFile = new File(args[1]);
    
    BioStoreFactory bsf = new BioStoreFactory();
    bsf.setPrimaryKey("ID");
    bsf.setStoreLocation(storeFile);
    bsf.addKey("ID", 14);
    
    BioStore store = bsf.createBioStore();
    
    Indexer indexer = new Indexer(spFile, store);
    indexer.setPrimaryKeyName("ID");
    
    ValueChanger changer = new ValueChanger(indexer);
    changer.setChanger("ID", new ValueChanger.Changer() {
      public Object change(Object value) {
        String s = (String) value;
        int i = s.indexOf(" ");
        return s.substring(0, i);
      }
    });
    
    Parser parser = new Parser();
    
    while(
      parser.read(indexer.getReader(), LineSplitParser.EMBL, changer)
    ) {
      ;
    }

    store.commit();
  }
}
