
import java.io.*;
import org.xml.sax.*;
import org.biojava.utils.stax.*;
import org.apache.xerces.parsers.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.ragbag.*;

public class TestRagbagAssembly {
    public static void main(String[] args) 
        throws Exception
    {
      // create RagbagSequence doodad
      File f = new File(args[0]);

      RagbagSequenceFactory sf = new RagbagSoftRefSeqFactory();
      RagbagAssembly rs = new RagbagAssembly(f, sf);

      System.out.println("completed RagbagDirectoryHandler");

      // dump it
      SequenceDumper sd = new SequenceDumper( (Sequence) rs);
      sd.dump();
    }
}
