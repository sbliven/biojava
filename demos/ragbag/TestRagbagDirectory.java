
import java.io.*;
import org.xml.sax.*;
import org.biojava.utils.stax.*;
import org.apache.xerces.parsers.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.ragbag.*;

public class TestRagbagDirectory {
    public static void main(String[] args) 
        throws Exception
    {
      // create RagbagSequence doodad
      File f = new File(args[0]);

      RagbagDirectoryHandler rs = new RagbagDirectoryHandler(f);

      System.out.println("completed RagbagDirectoryHandler");

      // dump it
      SequenceDumper sd = new SequenceDumper( (Sequence) rs);
      sd.dump();
    }
}
