package	biosql;

import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.db.biosql.*;
import org.biojava.bio.taxa.*;

public class UploadFlat {
    public static void main(String [] args)
    {
	try
	{
	    if(args.length < 6)
		{
		    throw new Exception("Use: UploadFlat dbURL dbUser dbPass biodatabase format file1 [file2...]");
		}
		
	    String dbURL = args[0];
	    String dbUser = args[1];
	    String dbPass = args[2];
            String bioDB = args[3];
	    String format = args[4];
            
            System.out.println("Opening database: " + bioDB);
            
            SequenceDB seqDB = new BioSQLSequenceDB(
              dbURL,
              dbUser,
              dbPass,
              bioDB,
              true
            );
            
	    SequenceFormat sFormat;
	    SequenceBuilderFactory sbFact;
	    Alphabet alpha;

	    if ("embl".equalsIgnoreCase(format)) {
		sFormat = new EmblLikeFormat();
		sbFact = new EmblProcessor.Factory(SimpleSequenceBuilder.FACTORY);
		alpha = DNATools.getDNA();
	    } else if ("swissprot".equalsIgnoreCase(format)) {
		sFormat = new EmblLikeFormat();
		sbFact = new SwissprotProcessor.Factory(
				 SimpleSequenceBuilder.FACTORY
				 );
		alpha = ProteinTools.getAlphabet();
	    } else if ("fasta".equalsIgnoreCase(format)) {
		sFormat = new FastaFormat();
		sbFact = new FastaDescriptionLineParser.Factory(SimpleSequenceBuilder.FACTORY);
		alpha = DNATools.getDNA();
	    } else if ("fasta-protein".equalsIgnoreCase(format)) {
		sFormat = new FastaFormat();
		sbFact = new FastaDescriptionLineParser.Factory(SimpleSequenceBuilder.FACTORY);
		alpha = ProteinTools.getAlphabet();
	    } else {
		System.err.println("Unknown format: " + format);
		return;
	    }

	    SymbolTokenization rParser = alpha.getTokenization("token");
            
            for(int i = 5; i < args.length; i++) {
		File swissProtFile = new File(args[i]);
		BufferedReader sReader = new BufferedReader(new	InputStreamReader(new FileInputStream(swissProtFile)));
		SequenceIterator seqI =
		    new	StreamReader(sReader, sFormat, rParser,	sbFact);
		
		while(seqI.hasNext())
		    {
          try {
            System.out.print(".");
            Sequence seq = seqI.nextSequence();
            seqDB.addSequence(seq);
          } catch (Throwable t) {
            t.printStackTrace(System.out);
          }
		    }
            }
            System.out.println();
	}
	catch (Throwable t)
	    {
		t.printStackTrace();
		System.exit(1);
	    }
    }
}
