
import java.io.*;
import org.xml.sax.*;
import org.biojava.utils.stax.*;
import org.apache.xerces.parsers.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.ragbag.*;

public class TestVirtualRagbag {
    public static void main(String[] args) 
        throws Exception
    {
      // create RagbagSequence doodad
      File thisDir = new File("seqdir");
      RagbagDirectoryHandler rs = new RagbagDirectoryHandler(thisDir);

      System.out.println("completed RagbagDirectoryHandler");

      // dump it
      SequenceDumper sd = new SequenceDumper( (Sequence) rs);
      sd.dump();
    }
}
