import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.program.tagvalue.*;
import org.biojava.bio.program.indexdb.*;

public class ListIDs {
  public static void main(String[] args)
  throws Throwable {
    File storeFile = new File(args[0]);
    
    List pidl = new BioStore(storeFile, false).getRecordList();
    
    for(Iterator i = pidl.iterator(); i.hasNext(); ) {
      Record rec = (Record) i.next();
      System.out.println(rec.getID());
    }
  }
}
