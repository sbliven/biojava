package ssaha;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.program.ssaha.*;

public class SSAHA {
  public static void main(String[] args)
  throws Throwable {
    File dataStoreFile = new File(args[0]);
    File seqFile = new File(args[1]);
    String tableFormat = "classic";
    if (args.length > 2) {
	tableFormat = args[2];
    }

    DataStoreFactory dsf;
    if ("compact".equals(tableFormat)) {
	dsf = new CompactedDataStoreFactory();
    } else {
	dsf = new MappedDataStoreFactory();
    }
    
    final DataStore ds = dsf.getDataStore(
      dataStoreFile
    );
    
    SearchListener listener = new HitMerger(
      new ResultPrinter(ds),
      20
    );
    
    //System.out.println("Searching:");
    
    for(
      SequenceIterator si = SeqIOTools.readFastaDNA(
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
