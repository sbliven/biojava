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

public class SequenceTest {
    public static void main(String[] args)
        throws Exception
    {
	if (args.length < 3) {
	    System.err.println("usage: SequenceTest <database_url> <username> <password>");
	    System.err.println("example: SequenceTest jdbc:postgresql://localhost/thomasd_biosql2 thomas \"\"");
	    return;
	}

	String dbURL = args[0];
	String dbUser = args[1];
	String dbPass = args[2];
	String bioName = "testdb2";
	
	SequenceDB seqDB = new BioSQLSequenceDB(dbURL, dbUser, dbPass, bioName, true);
	
	String seqName = "testseq";
        
	Sequence seq = new SimpleSequence(DNATools.createDNA("gattaca"), seqName, seqName, Annotation.EMPTY_ANNOTATION);
	Feature.Template temp = new Feature.Template();
	temp.type = "feature_on_inserted_sequence";
	temp.source = "magic";
	temp.location = new RangeLocation(2, 6);
	temp.annotation = Annotation.EMPTY_ANNOTATION;
	Feature f = seq.createFeature(temp);
        
        System.out.println("Inserting sequence");
        
	seqDB.addSequence(seq);

	System.out.println("Getting ids");

	Set ids = seqDB.ids();
	for (Iterator i = ids.iterator(); i.hasNext(); ) {
	    System.out.println(i.next().toString());
	}

        System.out.println("Retrieving sequence");
        
	seq = seqDB.getSequence(seqName);
        
	System.out.println("Testing that a feature was persisted correctly");

	Feature f2 = (Feature) seq.features().next();
	Feature.Template temp2 = f2.makeTemplate();
	if (! temp.type.equals(temp2.type)) {
	    System.out.println("!!! types don't match");
	}
	if (! temp.source.equals(temp2.source)) {
	    System.out.println("!!! sources don't match");
	}
	if (! temp.location.equals(temp2.location)) {
	    System.out.println("!!! locations don't match");
	}
	f2 = null;
	
        System.out.println("Creating a feature");
        
	temp = new Feature.Template();
	temp.type = "testing";
	temp.source = "magic";
	temp.location = new RangeLocation(1, 6);
	temp.annotation = Annotation.EMPTY_ANNOTATION;
	f = seq.createFeature(temp);

        System.out.println("Creating a child feature");
        
	temp.type = "child";
	temp.location = new RangeLocation(2, 4);
	Feature cf = f.createFeature(temp);

	System.out.println("Changing feature type");
	f.setType("this_is_a_changed_type");

	System.out.println("Changing feature source");
	f.setSource("this_is_a_changed_source");

	System.out.println("Changing feature location");
	f.setLocation(new RangeLocation(1, 5));

	System.out.println("Checking event system (feature level)");

	f.addChangeListener(ChangeListener.ALWAYS_VETO, ChangeType.UNKNOWN);
	{
	    boolean vetoed = false;
	    try {
		f.createFeature(temp);
	    } catch (ChangeVetoException ex) {
		vetoed = true;
	    }
	    if (!vetoed) {
		throw new Exception("Create feature event didn't get vetoed!");
	    }
	}
	{
	    boolean vetoed = false;
	    try {
		f.removeFeature(cf);
	    } catch (ChangeVetoException ex) {
		vetoed = true;
	    }
	    if (!vetoed) {
		throw new Exception("Remove feature event didn't get vetoed!");
	    }
	}
	f.removeChangeListener(ChangeListener.ALWAYS_VETO, ChangeType.UNKNOWN);
        
	System.out.println("Checking event system (sequence level)");
	seq.addChangeListener(ChangeListener.ALWAYS_VETO, ChangeType.UNKNOWN);
	{
	    boolean vetoed = false;
	    try {
		f.createFeature(temp);
	    } catch (ChangeVetoException ex) {
		vetoed = true;
	    }
	    if (!vetoed) {
		throw new Exception("Create feature event didn't get vetoed!");
	    }
	}
	seq.removeChangeListener(ChangeListener.ALWAYS_VETO, ChangeType.UNKNOWN);

	System.out.println("Checking event system (database level)");
	seqDB.addChangeListener(ChangeListener.ALWAYS_VETO, ChangeType.UNKNOWN);
	{
	    boolean vetoed = false;
	    try {
		f.createFeature(temp);
	    } catch (ChangeVetoException ex) {
		vetoed = true;
	    }
	    if (!vetoed) {
		throw new Exception("Create feature event didn't get vetoed!");
	    }
	}
	
	System.out.println("Checking event system (forwarding from feature annotations)");
	{
	    boolean vetoed = false;
	    try {
		f.getAnnotation().setProperty("foo", "bar");
	    } catch (ChangeVetoException ex) {
		vetoed = true;
	    }
	    if (!vetoed) {
		throw new Exception("Set property event didn't get vetoed!");
	    }
	}

	System.out.println("Checking event system (forwarding from sequence annotations)");
	{
	    boolean vetoed = false;
	    try {
		f.getSequence().getAnnotation().setProperty("foo", "bar");
	    } catch (ChangeVetoException ex) {
		vetoed = true;
	    }
	    if (!vetoed) {
		throw new Exception("Set property event didn't get vetoed!");
	    }
	}
	
	seqDB.removeChangeListener(ChangeListener.ALWAYS_VETO, ChangeType.UNKNOWN);

	System.out.println("Removing a child feature");

	f.removeFeature(cf);
	cf = null;

        temp.type = "removal_test";
        Feature rtest = seq.createFeature(temp);
        
        System.out.println("Removing a feature");
        
        seq.removeFeature(rtest);
        
        rtest = null;
        f = null;
        seq = null; // Can't delete features while a reference still exists
        
        seqDB.removeSequence(seqName);
    }
}
