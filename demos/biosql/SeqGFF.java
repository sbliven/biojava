package biosql;

import java.io.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.gff.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.db.biosql.*;

public class SeqGFF {
    public static void main(String[] args)
        throws Exception
    {
	String dbURL = "jdbc:mysql://fred.biohack.egenetics.com/test_biosql";
	String dbUser = "";
	String dbPass = "";
	String bioName = "embl_rod";
	
	SequenceDB seqDB = new BioSQLSequenceDB(dbURL, dbUser, dbPass, bioName, false);
	Sequence seq = seqDB.getSequence(args[0]);

	SequencesAsGFF seqgff = new SequencesAsGFF();
	if (args.length == 1) {
	    seqgff.setFeatureFilter(FeatureFilter.all);
	} else {
	    seqgff.setFeatureFilter(new FeatureFilter.OverlapsLocation(new RangeLocation(Integer.parseInt(args[1]),
											 Integer.parseInt(args[2]))));
	}
	seqgff.setRecurse(false);

	PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
	GFFWriter gffw = new GFFWriter(pw);
	seqgff.processSequence(seq, gffw);
	pw.flush();
    }
}
