package	seq;

import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;

public class TestRefSeqPrt {
	public static void main(String [] args)
	{
		try
		{
			if(args.length !=	1)
			{
				throw new Exception("Use: TestRefSeqPrt refseqFile");
			}

			File genbankFile = new File(args[0]);
			SequenceFormat gFormat = new GenbankFormat();
			BufferedReader gReader = new BufferedReader(
					new	InputStreamReader(new FileInputStream(genbankFile)));
			SequenceBuilderFactory sbFact =
					new ProteinRefSeqProcessor.Factory(SimpleSequenceBuilder.FACTORY);
			Alphabet alpha = ProteinTools.getTAlphabet();
			SymbolTokenization rParser = alpha.getTokenization("token");
			SequenceIterator seqI =
					new	StreamReader(gReader, gFormat, rParser,	sbFact);

			while(seqI.hasNext())
			{
				Sequence seq = seqI.nextSequence();
				System.out.println(seq.getName() + " has " + seq.countFeatures() + " features");
				System.out.println("\tand is " + seq.length());
				// Feature (location) testing
//				java.util.Iterator theIterator = seq.features();
//				SeqFileFormer theFormatter = SeqFileFormerFactory.makeFormer("Genbank");
//				while(theIterator.hasNext())
//				{
//					System.out.println();
//					Feature theFeature = (Feature)theIterator.next();
//					try
//					{
//						StrandedFeature castedFeature = (StrandedFeature)theFeature;
//						StringBuffer theBuffer = new StringBuffer("Location: ");
//						System.out.println(theFormatter.formatLocation(theBuffer, castedFeature.getLocation(), castedFeature.getStrand()));
//					}
//					catch (ClassCastException cce)
//					{
//						System.out.println("Feature is not Stranded");
//						StringBuffer theBuffer = new StringBuffer("Location: ");
//						System.out.println(theFormatter.formatLocation(theBuffer, theFeature.getLocation(), StrandedFeature.POSITIVE));
//					}
//					Annotation featureAnnotation = theFeature.getAnnotation();
//					java.util.Set theKeys = featureAnnotation.keys();
//					System.out.println("Keys:");
//					java.util.Iterator keyIterator = theKeys.iterator();
//					while(keyIterator.hasNext())
//					{
//						System.out.println("\t" + keyIterator.next());
//					}
//
//					System.out.println();
//					keyIterator = theKeys.iterator();
//					while(keyIterator.hasNext())
//					{
//						Object tempKey = keyIterator.next();
//						System.out.println(tempKey + ": " + featureAnnotation.getProperty(tempKey));
//					}
//				}

				// Annotation testing
//				Annotation theAnnotation = seq.getAnnotation();
//				java.util.Set theKeys = theAnnotation.keys();
//				java.util.Iterator theOtherIterator = theKeys.iterator();
//				while(theOtherIterator.hasNext())
//				{
//					System.out.println();
//					String theKey = theOtherIterator.next().toString();
//					System.out.println(theKey + ":");
//					System.out.println("\t" + theAnnotation.getProperty(theKey).toString());
//				}
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			System.exit(1);
		}
	}
}
