package indexing;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.program.tagvalue.*;
import org.biojava.bio.program.indexdb.*;

public class ReadRaw {
  public static void main(String[] args)
  throws Throwable {
    File storeFile = new File(args[0]);
    File listFile = new File(args[1]);
    
    BioStore store = new BioStore(storeFile, false);
    
    BufferedReader reader = new BufferedReader(
      new FileReader(
        listFile
      )
    );
    
    byte[] buff = new byte[256];
    
    for(
      String line = reader.readLine();
      line != null;
      line = reader.readLine()
    ) {
      String id = line.trim();
      Record rec = store.get(id);
      rec.getFile().seek(rec.getOffset());
      if(buff.length < rec.getLength()) {
        buff = new byte[rec.getLength()];
      }
      rec.getFile().readFully(buff, 0, rec.getLength());
      System.out.write(buff, 0, rec.getLength());
    }
  }
}
