package ssaha;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.program.ssaha.*;

public class CreateDNAFastaHashTableCompact {
    public static void main(String[] args)
	throws Exception
    {
	File dataStoreFile = new File(args[0]);
	List seqFiles = new ArrayList();
	for(int i = 1; i < args.length; i++) {
	    seqFiles.add(new File(args[i]));
	}
	SequenceStreamer streamer = new SequenceStreamer.FileStreamer(
		new FastaFormat(),
		DNATools.getDNA().getTokenization("token"),
		seqFiles
        );
	
	DataStore ds = new CompactedDataStoreFactory().buildDataStore(
    		dataStoreFile,
		streamer,
		new DNANoAmbPack(DNATools.t()),
		10,
		10000
	);
    }
}
