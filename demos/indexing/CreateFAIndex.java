package indexing;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.program.indexdb.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.utils.*;
import org.biojava.utils.io.*;

public class CreateFAIndex {
  public static void main(String[] args)
  throws Throwable {
    File storeFile = new File(args[0]);
    
    BioStoreFactory bsf = new BioStoreFactory();
    bsf.setPrimaryKey("ID");
    bsf.setStoreLocation(storeFile);
    bsf.addKey("ID", 10);

    BioStore store = bsf.createBioStore();
    
    FastaFormat format = new FastaFormat();
    SymbolTokenization tok = ProteinTools.getAlphabet().getTokenization("token");

    for(int i = 1; i < args.length; i++) {
      File faFile = new File(args[i]);
      RAF raf = new RAF(faFile, "r");
      Indexer indexer = new Indexer(raf, store);
    
      StreamReader sreader = new StreamReader(indexer.getReader(), format, tok, indexer);
      while(sreader.hasNext()) {
        sreader.nextSequence();
      }
    }
    
    store.commit();
  }
  
  private static class Indexer implements SequenceBuilderFactory {
    private final Map map = new HashMap();
    private final RAF raf;
    private final IndexStore store;
    private final CountedBufferedReader reader;
    
    public Indexer(RAF raf, IndexStore store)
    throws IOException {
      this.raf = raf;
      this.store = store;
      reader = new CountedBufferedReader(
        new FileReader(
          raf.getFile()
        )
      );
    }
    
    public CountedBufferedReader getReader() {
      return reader;
    }
    
    public SequenceBuilder makeSequenceBuilder() {
      return new SeqIOIndexer();
    }
    
    class SeqIOIndexer extends SeqIOAdapter implements SequenceBuilder {
      long offset = 0L;
      String id;
      
      public void startSequence() {
        id = null;
        offset = reader.getFilePointer();
      }
      
      public void addSequenceProperty(Object key, Object value) {
        if(key.equals(FastaFormat.PROPERTY_DESCRIPTIONLINE)) {
          String line = (String) value;
          
          /* 2nd word
          int a = line.indexOf(" ") + 1;
          int b = line.indexOf(" ", a);
          id = line.substring(a, b);
          */
          
          /* 1st word */
          int a = line.indexOf(" ");
          id = line.substring(0, a);
        }
      }
      
      public void endSequence() {
        long nof = reader.getFilePointer();
        store.writeRecord(raf, offset, (int) (nof - offset), id, map);
        offset = nof;
      }
      
      public Sequence makeSequence() {
        return null;
      }
    }
  }
}
