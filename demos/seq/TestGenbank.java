package	seq;

import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;

public class TestGenbank {
	public static void main(String [] args)
	{
		try
		{
			if(args.length !=	1)
			{
				throw new Exception("Use: TestGenbank genbankFile");
			}

			File genbankFile = new File(args[0]);
			SequenceFormat gFormat = new GenbankFormat();
			BufferedReader gReader = new BufferedReader(
					new	InputStreamReader(new FileInputStream(genbankFile)));
			SequenceBuilderFactory sbFact =
					new GenbankProcessor.Factory(SimpleSequenceBuilder.FACTORY);
			Alphabet alpha = DNATools.getDNA();
			SymbolParser rParser = alpha.getParser("token");
			SequenceIterator seqI =
					new	StreamReader(gReader, gFormat, rParser,	sbFact);

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
