package biosql;

import java.io.*;
import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.gff.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.db.biosql.*;

public class LargeSequenceTest {
    public static void main(String[] args)
        throws Exception
    {
	if (args.length < 3) {
	    System.err.println("usage: LargeSequenceTest <database_url> <username> <password>");
	    System.err.println("example: LargeSequenceTest jdbc:postgresql://localhost/thomasd_biosql2 thomas \"\"");
	    return;
	}

	String dbURL = args[0];
	String dbUser = args[1];
	String dbPass = args[2];
	String bioName = "testdb2";
	
	SequenceDB seqDB = new BioSQLSequenceDB(dbURL, dbUser, dbPass, bioName, true);
	Sequence seq;
	{
	    List symbols = new ArrayList();
	    for (int i = 0; i < 50000; ++i) {
		symbols.add(DNATools.a());
		symbols.add(DNATools.c());
		symbols.add(DNATools.g());
		symbols.add(DNATools.t());
	    }
	    SymbolList sl = new SimpleSymbolList(DNATools.getDNA(), symbols);
	    seq = new SimpleSequence(sl, null, "testSeq", Annotation.EMPTY_ANNOTATION);
	}
	seqDB.addSequence(seq);
	
	int count = 0;

	seq = seqDB.getSequence("testSeq");
	for (int i = 1; i < 190000; i += 1000) {
	    Feature.Template templ = new Feature.Template();
	    templ.type = "test";
	    templ.source = "test";
	    templ.location = new RangeLocation(i, i + 1100);
	    templ.annotation = Annotation.EMPTY_ANNOTATION;
	    seq.createFeature(templ);
	    ++count;
	}
	seq = null;
	seqDB = null;

	seqDB = new BioSQLSequenceDB(dbURL, dbUser, dbPass, bioName, true);
	seq = seqDB.getSequence("testSeq");

	List startList = new ArrayList();

	for (Iterator i = seq.features(); i.hasNext(); ) {
	    Feature f = (Feature) i.next();
	    startList.add(new Integer(f.getLocation().getMin()));
	    --count;
	}	
	    
	Collections.sort(startList);
	for (Iterator i = startList.iterator(); i.hasNext(); ) {
	    System.out.println(i.next());
	}

	System.out.println("Balance: " + count);
	
	for (Iterator i = seq.features(); i.hasNext(); ) {
	    Feature f = (Feature) i.next();
	}	

	seq = null;

	seqDB = new BioSQLSequenceDB(dbURL, dbUser, dbPass, bioName, true);
	seqDB.removeSequence("testSeq");
    }
}
