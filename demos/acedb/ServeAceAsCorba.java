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

import org.omg.CORBA.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;

import org.acedb.*;
import org.acedb.socket.*;
import org.acedb.seq.*;

import org.biojava.bridge.Biocorba.Seqcore.*;
import org.Biocorba.Seqcore.*;

public class ServeAceAsCorba {
  public static void main(String [] args) throws Exception {
    if(args.length < 4) {
      throw new Exception(
        "Use: AceClient host port user password [seqname]"
      );
    }
    
    try {
      String host = args[0];
      String port = args[1];
      String user = args[2];
      String passwd = args[3];
      String seqname = (args.length < 5) ? "*" : args[4];
      
      Ace.registerDriver(new SocketDriver());
      AceURL dbURL = new AceURL("acedb://" + user + ':' + passwd + '@' + host + ':' + port);
      System.out.println("Connecting to " + dbURL);
      SequenceDB seqDB = new AceSequenceDB(dbURL);
      System.out.println("Contains");
      for(Iterator i = seqDB.ids().iterator(); i.hasNext(); ) {
        System.out.println("\t" + i.next());
      }
      
      SeqDBImpl seqDBImpl = new SeqDBImpl(seqDB, dbURL.toString());
      _SeqDB_Tie seqDBTie = new _SeqDB_Tie(seqDBImpl);
      ORB orb = ORB.init(new String[0], null);
      orb.connect(seqDBTie);

      // print out the ior
      String ior = orb.object_to_string(seqDBTie);
      System.out.println(ior);
    
      // hang this thread so that the process doesn't exit
      java.lang.Object sync = new java.lang.Object();
      synchronized(sync) {
        sync.wait();
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
