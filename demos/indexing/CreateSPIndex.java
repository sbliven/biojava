package indexing;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.program.tagvalue.*;
import org.biojava.bio.program.indexdb.*;

public class CreateSPIndex {
  public static void main(String[] args)
  throws Throwable {
    File storeFile = new File(args[0]);
    
    BioStoreFactory bsf = new BioStoreFactory();
    bsf.setPrimaryKey("ID");
    bsf.setStoreLocation(storeFile);
    bsf.addKey("AC", 10);
    bsf.addKey("ID", 10);
    
    BioStore store = bsf.createBioStore();
    
    for(int i = 1; i < args.length; i++) {
      File spFile = new File(args[i]);
      Indexer indexer = new Indexer(spFile, store);
      indexer.setPrimaryKeyName("ID");
      indexer.addSecondaryKey("AC");
      
      ChangeTable changeTable = new ChangeTable();
      
      changeTable.setChanger("ID", new ChangeTable.Changer() {
        public Object change(Object value) {
          String s = (String) value;
          int i = s.indexOf(" ");
          return s.substring(0, i);
        }
      });

      changeTable.setChanger("AC", new ChangeTable.Changer() {
        public Object change(Object value) {
          String s = (String) value;
          int i = s.indexOf(";");
          return s.substring(0, i);
        }
      });
      
      ValueChanger changer = new ValueChanger(indexer, changeTable);
    
      Parser parser = new Parser();
      
      while(
        parser.read(indexer.getReader(), LineSplitParser.EMBL, changer)
      ) {
        ;
      }
    }

    store.commit();
  }
}
