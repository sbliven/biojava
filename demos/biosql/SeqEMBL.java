package biosql;

import java.io.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.gff.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.db.biosql.*;

public class SeqEMBL {
    public static void main(String[] args)
        throws Exception
    {
//  	String dbURL = "jdbc:mysql://fred.biohack.egenetics.com/test_biosql";
//  	String dbUser = "";
//  	String dbPass = "";
//  	String bioName = "embl_rod";

  	String dbURL = "jdbc:postgresql://localhost/test42";
  	String dbUser = "thomas";
  	String dbPass = "";
  	String bioName = "embl";
	
	SequenceDB seqDB = new BioSQLSequenceDB(dbURL, dbUser, dbPass, bioName, false);
	Sequence seq = seqDB.getSequence(args[0]);

	SequenceFormat ff = new EmblFormat();
	ff.writeSequence(seq, "embl", System.out);
    }
}
