package das;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.das.*;

import java.net.*;
import java.io.*;
import java.util.*;

public class TestDAS {
    public static void main(String[] args) throws Exception {
	if (args.length != 1)
	    throw new Exception("java das.TestDAS <url>");
	String dbURLString = args[0];

	URL dbURL = new URL(dbURLString);
	
	DASSequenceDB dasDB = new DASSequenceDB(dbURL);
	System.out.println("Top-level entry points:");
	Set ids = dasDB.ids();
	for (Iterator i = ids.iterator(); i.hasNext(); ) {
	    System.out.println(i.next().toString());
	}

	System.out.println("All entry points:");
	Set allIds = dasDB.allEntryPointsDB().ids();
	for (Iterator i = allIds.iterator(); i.hasNext(); ) {
	    System.out.println(i.next().toString());
	}

	DASSequence dasSeq = (DASSequence) dasDB.getSequence((String) ids.iterator().next());
	// dasSeq.addAnnotationSource(annoURL);
	// dasSeq.addAnnotationSource(miscURL);
	System.out.println("Length: " + dasSeq.length());
	System.out.println("1st 10 bases: " + dasSeq.subStr(1, 10));
	System.out.println("Feature count: " + dasSeq.countFeatures());
    }
}
