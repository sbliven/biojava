
import java.io.*;
import org.xml.sax.*;
import org.biojava.utils.stax.*;
import org.apache.xerces.parsers.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.ragbag.*;

public class TestRagbagSequence {
    public static void main(String[] args) 
        throws Exception
    {
      // create RagbagSequence doodad
      RagbagSequence rs = new RagbagSequence();

      // add a sequence file
      rs.addSequenceFile(args[0]);

      // add a feature file
      rs.addFeatureFile(args[1]);

      // make the sequence
      rs.makeSequence();

      // dump it
      SequenceDumper sd = new SequenceDumper( (Sequence) rs);
      sd.dump();
    }
}
