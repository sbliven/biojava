package das;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.das.*;

import java.net.*;
import java.io.*;
import java.util.*;

public class TestDSN {
    public static void main(String[] args) throws Exception {
      if (args.length < 1) {
        throw new Exception("java das.TestDSN <url>");
      }
      
      String dbURLString = args[0];
      DAS das = new DAS();
      das.addDasURL(new URL(dbURLString));
      
      for(Iterator i = das.getReferenceServers().iterator(); i.hasNext(); ) {
        ReferenceServer rs = (ReferenceServer) i.next();
        System.out.println(rs.getName() + "\t" + rs.getURL());
        for(Iterator j = rs.getAnnotaters().iterator(); j.hasNext(); ) {
          DataSource ds = (DataSource) j.next();
          System.out.println("\t" + ds.getName() + "\t" + ds.getURL());
        }
      }
    }
}
