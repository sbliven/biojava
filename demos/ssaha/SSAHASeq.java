package ssaha;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.program.ssaha.*;

public class SSAHASeq {
  public static void main(String[] args)
  throws Throwable {
    File dataStoreFile = new File(args[0]);
    String seqStr = args[1];
    
    final DataStore ds = new MappedDataStoreFactory().getDataStore(
      dataStoreFile
    );
    
    SearchListener listener = new HitMerger(
      new ResultPrinter(ds),
      20
    );
    
    //System.out.println("Searching:");
    
    SymbolList symList = new SimpleSymbolList(
      DNATools.getDNA().getTokenization("token"),
      seqStr
    );
    
    ds.search("Sequence", symList, listener);
  }
}
