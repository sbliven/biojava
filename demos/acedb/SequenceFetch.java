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
package acedb;

import java.io.*;
import java.net.*;
import java.util.*;

import org.acedb.*;
import org.acedb.socket.*;
import org.acedb.seq.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;

public class SequenceFetch {
  public static void main(String [] args) throws Exception {
    if(args.length < 5) {
      throw new Exception(
        "Use: AceClient host port user password fastaOut [seqname]"
      );
    }
    
    try {
      String host = args[0];
      String port = args[1];
      String user = args[2];
      String passwd = args[3];
      File fastaOut = new File(args[4]);
      String seqname = (args.length < 6) ? "*" : args[5];
      
      Ace.registerDriver(new SocketDriver());
      AceURL dbURL = new AceURL("acedb://" + user + ':' + passwd + '@' + host + ':' + port);
      System.out.println("Connecting to " + dbURL);
      
      SequenceFormat sFormat = new FastaFormat();
      OutputStream out = new FileOutputStream(fastaOut);
      SequenceDB seqs = new AceSequenceDB(dbURL);
      StreamWriter fastaWriter = new StreamWriter(out, sFormat);
      
      fastaWriter.writeStream(seqs.sequenceIterator());
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
