package biosql;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.db.biosql.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.gff.*;

public class DummyFromGFF {
    public static void main(String[] args) 
        throws Exception
    {
  	String dbURL = "jdbc:postgresql://localhost/thomasd_biosql2";
  	String dbUser = "thomas";
  	String dbPass = "";
  	String bioName = "testing";

	BioSQLSequenceDB bssdb = new BioSQLSequenceDB(dbURL,
						      dbUser,
						      dbPass,
						      bioName,
						      true);

	String seqID = args[0];
	int length = Integer.parseInt(args[1]);
	String gffFile = args[2];

	bssdb.createDummySequence(seqID, DNATools.getDNA(), length);
	Sequence dummy = bssdb.getSequence(seqID);
	
	GFFEntrySet gff = loadGFF(gffFile);
	gff.getAnnotator().annotate(dummy);
    }

    
    public static GFFEntrySet loadGFF(String name) 
        throws Exception
    {
	GFFParser gffp = new GFFParser();
	BufferedReader gff = new BufferedReader(new FileReader(name));
	GFFEntrySet set = new GFFEntrySet();
	gffp.parse(gff, set.getAddHandler(), name);

	return set;
    }
}
