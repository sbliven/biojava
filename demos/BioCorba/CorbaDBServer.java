/**
 * A simple corba server that serves up a simple database with randomly
 * positioned features.
 */
package BioCorba;

import java.io.*;
import org.omg.CORBA.*;
 
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bridge.Biocorba.Seqcore.*;
import org.Biocorba.Seqcore.*;

public class CorbaDBServer {
  public static final void main(String [] args) throws Exception {
    if(args.length != 2) {
      throw new Exception("Use: CorbaDBServer sequences.fa type");
    }
    
    // work out what to load
    File seqDBFile = new File(args[0]);
    Alphabet alpha = AlphabetManager.instance().alphabetForName(args[1]);
    if(alpha == null) {
      throw new NullPointerException("Alphabet " + args[1] + " not known");
    }    
    SymbolParser rParser = alpha.getParser("token");
    SequenceFactory sFact = new SimpleSequenceFactory();
    
    // load it
    System.out.println("Loading fasta database: " + seqDBFile);
    HashSequenceDB seqDB = new HashSequenceDB(
      HashSequenceDB.byURN, seqDBFile.toString()
    );
    SequenceFormat sFormat = new FastaFormat();
    InputStream seqDBI = new FileInputStream(seqDBFile);
    SequenceIterator seqI = new StreamReader(seqDBI,
                                             sFormat, rParser, sFact);
    while(seqI.hasNext()) {
      try {
        seqDB.addSequence(seqI.nextSequence());
      } catch (BioException se) {
        se.printStackTrace();
      }
    }
    
    // serve it as a corba server
    SeqDBImpl seqDBImpl = new SeqDBImpl(seqDB);
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
  }
}
