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
import org.biojava.utils.lsid.*;

public class CreateFAIndex {
  private static final int ID_LENGTH = 40;
  public static void main(String[] args)
  throws Throwable {
    if(args.length < 3) {
      System.err.println("Use: indexdb.CreateFAIndex storeFile storeName [filesToINdex]");
      System.exit(1);
    }

    File storeFile = new File(args[0]);
    
    BioStoreFactory bsf = new BioStoreFactory();
    bsf.setPrimaryKey("ID");
    bsf.setStoreLocation(storeFile);
    bsf.addKey("ID", ID_LENGTH);
    bsf.setStoreName(args[1]);
    bsf.setSequenceFormat(LifeScienceIdentifier.valueOf("open-bio.org", "format", "fasta"));

    BioStore store = bsf.createBioStore();
    
    FastaFormat format = new FastaFormat();
    SymbolTokenization tok = ProteinTools.getAlphabet().getTokenization("token");

    for(int i = 2; i < args.length; i++) {
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
          if(a != -1) {
            id = line.substring(0, a);
          } else {
            id = line;
          }

          if(id.length() > ID_LENGTH) {
            System.err.println("ID too long: " + id.length() + " vs " + ID_LENGTH);
            System.err.println("Got description: " + line);
            System.err.println("ID: " + id);
            System.err.println();
          }
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
