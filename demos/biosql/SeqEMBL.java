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

        Class.forName("org.postgresql.Driver");

  	String dbURL = "jdbc:postgresql://localhost:5432/biosql";
  	String dbUser = "keith";
  	String dbPass = "";
  	String bioName = "n_meningitidis";
	
	SequenceDB seqDB = new BioSQLSequenceDB(dbURL, dbUser, dbPass, bioName, false);
	Sequence seq = seqDB.getSequence(args[0]);

	SequenceFormat ff = new EmblLikeFormat();
	ff.writeSequence(seq, "embl", System.out);
    }
}
