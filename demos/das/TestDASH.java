package das;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.das.*;

import java.net.*;
import java.io.*;
import java.util.*;

public class TestDASH {
    public static void main(String[] args) throws Exception {
	if (args.length != 4)
	    throw new Exception("java das.TestDAS <url> <seq> <min> <max>");
	String dbURLString = args[0];
	String seqName = args[1];
        int min = Integer.parseInt(args[2]);
        int max = Integer.parseInt(args[3]);

	URL dbURL = new URL(dbURLString);
	
	DASSequenceDB dasDB = new DASSequenceDB(dbURL);
	//  System.out.println("Top-level entry points:");
//  	Set ids = dasDB.ids();
//  	for (Iterator i = ids.iterator(); i.hasNext(); ) {
//  	    System.out.println(i.next().toString());
//  	}

	DASSequence dasSeq = (DASSequence) dasDB.getSequence(seqName);
	// dasSeq.addAnnotationSource(annoURL);
	// dasSeq.addAnnotationSource(miscURL);
	System.out.println("Length: " + dasSeq.length());
	System.out.println("1st 10 bases: " + dasSeq.subStr(1, 10));

	printFeatures(dasSeq, new FeatureFilter.OverlapsLocation(new RangeLocation(min, max)), System.out, "");
	// printFeatures(dasSeq, System.out, "");
    }

    public static void printFeatures(FeatureHolder fh, 
				     FeatureFilter ff,
				     PrintStream pw,
				     String prefix)
	throws Exception
    {
	for (Iterator i = fh.filter(ff, true).features(); i.hasNext(); ) {
	    Feature f = (Feature) i.next();
	    pw.print(prefix);
	    pw.print(f.getType());
	    pw.print(" at ");
	    pw.print(f.getLocation().toString());
	    try {
		String id = (String) f.getAnnotation().getProperty(DASSequence.PROPERTY_FEATUREID);
		pw.print(" (" + id + ')');
	    } catch (NoSuchElementException ex) {
	    }
	    pw.println();
	    //printFeatures(f, ff, pw, prefix + "    ");
	}
    }
}
