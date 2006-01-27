package	seq;

import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;

/**
 * SwissProt format has been replaced by Uniprot format. You should refer to
 * the UniProt demo
 * @see UniProtDemo
 */
public class TestSwissprot
{
	public static void main(String [] args)
	{
		try
		{
			if(args.length !=	1)
			{
				throw new Exception("Use: TestSwissProt swissprotFile");
			}

			File swissProtFile = new File(args[0]);
			SequenceFormat sFormat = new EmblLikeFormat();
			BufferedReader sReader = new BufferedReader(
					new	InputStreamReader(new FileInputStream(swissProtFile)));
			SequenceBuilderFactory sbFact =
					new SwissprotProcessor.Factory(SimpleSequenceBuilder.FACTORY);
			Alphabet alpha = ProteinTools.getAlphabet();
			SymbolTokenization rParser = alpha.getTokenization("token");
			SequenceIterator seqI =
					new	StreamReader(sReader, sFormat, rParser,	sbFact);

			while(seqI.hasNext())
			{
				Sequence seq = seqI.nextSequence();
				System.out.println(seq.getName() + " has " + seq.countFeatures() + " features");
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			System.exit(1);
		}
	}
}
