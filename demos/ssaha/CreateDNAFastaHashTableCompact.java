package ssaha;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.program.ssaha.*;

/**
 * Build a SSAHAj hash table
 *
 * @author Thomas Down
 */

public class CreateDNAFastaHashTableCompact {
    public static void printUsage() {
	System.err.println("Usage: java ssaha.CreateDNAFastaHashTable [<options>] hashfile.store seq1.fa [seq2.fa ...]");
	System.err.println("Options are: ");
	System.err.println("   -wordSize      [default 10]");
	System.err.println("   -stepSize      [default 1]");
	System.err.println("   -threshold     [default 20000]");
    }

    public static void main(String[] args)
	throws Exception
    {
	File dataStoreFile = null;
	int wordSize = 10;
	int stepSize = 1;
	int threshold = 20000;

	List seqFiles = new ArrayList();
	for(int i = 0; i < args.length; i++) {
	    if (args[i].charAt(0) == '-') {
		if ("-threshold".equals(args[i])) {
		    threshold = Integer.parseInt(args[++i]);
		} else if ("-wordSize".equals(args[i])) {
		    wordSize = Integer.parseInt(args[++i]);
		} else if ("-stepSize".equals(args[i])) {
		    stepSize = Integer.parseInt(args[++i]);
		} else {
		    System.err.println("Unknown option " + args[i]);
		    printUsage();
		    return;
		}
	    } else {
		if (dataStoreFile == null) {
		    dataStoreFile = new File(args[i]);
		} else {
		    seqFiles.add(new File(args[i]));
		}
	    }
	}

	if (dataStoreFile == null || seqFiles.size() == 0) {
	    printUsage();
	    return;
	}

	SequenceStreamer streamer = new SequenceStreamer.FileStreamer(
		SeqIOTools.getSequenceFormat(
                  SeqIOTools.guessFileType((File) seqFiles.get(0))
                ),
		DNATools.getDNA().getTokenization("token"),
		seqFiles
        );
	
	DataStore ds = new CompactedDataStoreFactory().buildDataStore(
    		dataStoreFile,
		streamer,
		new DNANoAmbPack((byte) -1),
		wordSize,
		stepSize,
		threshold
	);
    }
}
