/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */
package seq.db;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.db.*;

/**
 * This demo file is a simple implementation of pairwise-alignment.
 *
 * @author Matthew Pocock
 */

public class AddFilesToIndex {
  public static void main(String[] args) {
    try {
      if(args.length == 0) {
        throw new Exception("Use: indexName file1 file2 file...");
      }
      String indexName = args[0];
      File indexFile = new File(indexName);
      
      TabIndexStore tis = TabIndexStore.open(indexFile);
      
      IndexedSequenceDB seqDB = new IndexedSequenceDB(tis);
      
      for(int i = 1; i < args.length; i++) {
        System.out.print("Adding: " + args[i] + "...");
        seqDB.addFile(new File(args[i]));
        System.out.println(" Done");
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
