package org.biojava.directory;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.gff.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.db.biosql.*;

import java.io.*;
import java.util.*;

public class RegistryTest{

    public static void main(String[] args){
	
	try{
	    SequenceDBFactory dbFactory = SequenceDBFactory.getInstance();

	    dbFactory.setRegistryConfiguration(new RegistryConfiguration(args[0]));
	    
	    SequenceDB seqDB = dbFactory.getDatabase("EMBL");
	    
	    Sequence seq = seqDB.getSequence(args[1]);
	    
	    SequencesAsGFF seqgff = new SequencesAsGFF();
	    
	    seqgff.setFeatureFilter(FeatureFilter.all);
	    
	    seqgff.setRecurse(false);

	    PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
	    
	    GFFWriter gffw = new GFFWriter(pw);
	    
	    seqgff.processSequence(seq, gffw);
	    
	    pw.flush();
	}catch(Exception e){
	    e.printStackTrace();
	}
    }
}
