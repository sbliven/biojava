package seq;

import java.io.*;

import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;

public class GCContent {
    public static void main(String[] args)
        throws Exception
    {
        if (args.length != 1)
	    throw new Exception("usage: java GCContent filename.fa");
	String fileName = args[0];
       
	// Set up stream reader

	Alphabet dna = DNATools.getDNA();
	SymbolTokenization dnaParser = dna.getTokenization("token");
	BufferedReader br = new BufferedReader(
			        new FileReader(fileName));
	SequenceBuilderFactory fact = new FastaDescriptionLineParser.Factory(
					      SimpleSequenceBuilder.FACTORY);
	StreamReader stream = new StreamReader(br,
					       new FastaFormat(),
					       dnaParser,
					       fact);

	// Iterate over all sequences in the stream

	while (stream.hasNext()) {
	    Sequence seq = stream.nextSequence();
	    System.out.println("Length: " + seq.length());
	    int gc = 0;
	    for (int pos = 1; pos <= seq.length(); ++pos) {
		Symbol sym = seq.symbolAt(pos);
		if (sym == DNATools.g() || sym == DNATools.c())
		    ++gc;
	    }
	    System.out.println(seq.getName() + ": " + 
			       ((gc * 100.0) / seq.length()) + 
			       "%");
	}
    }			       
}
