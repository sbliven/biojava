import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.program.tagvalue.*;
import org.biojava.bio.program.indexdb.*;

public class ReadRawSecondary {
  public static void main(String[] args)
  throws Throwable {
    File storeFile = new File(args[0]);
    String nameSpace = args[1];
    File listFile = new File(args[2]);
    
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
      List recList = store.get(id, nameSpace);
      for(Iterator i = recList.iterator(); i.hasNext(); ) {
        Record rec = (Record) i.next();
        rec.getFile().seek(rec.getOffset());
        if(buff.length < rec.getLength()) {
          buff = new byte[rec.getLength()];
        }
        rec.getFile().readFully(buff, 0, rec.getLength());
        System.out.write(buff, 0, rec.getLength());
      }
    }
  }
}
