package ssaha;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.program.ssaha.*;

public class CreateEmblHashTable {
  public static void main(String[] args)
  throws Throwable {
    File dataStoreFile = new File(args[0]);
    File[] seqFiles = new File[args.length - 1];
    for(int i = 0; i < seqFiles.length; i++) {
      seqFiles[i] = new File(args[i+1]);
    }
    SequenceDB seqDB = new FilesWrapper(seqFiles);
    
    DataStore ds = new MappedDataStoreFactory().buildDataStore(
      dataStoreFile,
      seqDB,
      new DNANoAmbPack(DNATools.t()),
      10,
      10000
    );
  }
  
  private static class FilesWrapper extends AbstractSequenceDB {
    private final File[] files;
    
    public FilesWrapper(File[] files) {
      this.files = files;
    }
    
    public Set ids() {
      throw new UnsupportedOperationException("Naughty, I know");
    }
    
    public Sequence getSequence(String id) {
      throw new UnsupportedOperationException("Naughty, I know");
    }
    
    public String getName() {
      throw new UnsupportedOperationException("Naughty, I know");
    }
    
    public SequenceIterator sequenceIterator() {
      return new SequenceIterator() {
        int indx;
        SequenceIterator si;
        
        {
          try {
            indx = 0;
            si = SeqIOTools.readEmbl(
              new BufferedReader(
                new FileReader(
                  files[indx]
                )
              )
            );
          } catch (Exception e) {
            throw new Error(e);
          }
        }
        
        public boolean hasNext() {
          return indx < files.length;
        }
        
        public Sequence nextSequence()
        throws BioException {
          Sequence seq = null;
          seq = si.nextSequence();
          if(!si.hasNext()) {
            indx++;
            if(indx < files.length) {
              try {
                si = SeqIOTools.readEmbl(
                  new BufferedReader(
                    new FileReader(
                      files[indx]
                    )
                  )
                );
              } catch (IOException ioe) {
                throw new BioException(ioe);
              }
            }
          }
          return seq;
        }
      };
    }
  }
}
