package ssaha;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.program.ssaha.*;

public class SSAHALarge {
  public static void main(String[] args)
  throws Throwable {
    File dataStoreFile = new File(args[0]);
    File seqFile = new File(args[1]);
    
    final DataStore ds = new NIODataStoreFactory().getDataStore(
      dataStoreFile
    );
    
    SearchListener listener = new HitMerger(
      new ResultPrinter(ds),
      20
    );
    
    //System.out.println("Searching:");
    
    for(
      //SequenceIterator si = SeqIOTools.readFastaDNA(
      SequenceIterator si = SeqIOTools.readEmbl(
        new BufferedReader(
          new FileReader(
            seqFile
          )
        )
      );
      si.hasNext();
    ) {
      Sequence seq = si.nextSequence();
      ds.search(seq.getName(), seq, listener);
    }
  }
}
