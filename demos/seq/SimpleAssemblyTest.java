package seq;

import java.util.*;
import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;

public class SimpleAssemblyTest {
    public static void main(String[] args) throws Exception {
	Feature.Template temp = new Feature.Template();
	temp.type = "test";
	temp.source = "SATest";
	temp.annotation = Annotation.EMPTY_ANNOTATION;
	temp.location = new RangeLocation(2, 3);

	SymbolList dna1 = DNATools.createDNA("GATTACA");
	Sequence seq1 = new SimpleSequence(dna1, "seq1", "seq1",
					   Annotation.EMPTY_ANNOTATION);
	seq1.createFeature(temp);
	SymbolList dna2 = DNATools.createDNA("ACGA");
	Sequence seq2 = new SimpleSequence(dna2, "seq2", "seq2",
					   Annotation.EMPTY_ANNOTATION);
	Feature f = seq2.createFeature(temp);
	temp.location = new PointLocation(3);
	temp.type = "sub-test";
	f.createFeature(temp);
	
	Sequence contig = new SimpleAssembly(20, "contig", "contig");
	
	ComponentFeature.Template cft = new ComponentFeature.Template();
	cft.type = "fragment";
	cft.source = "SATest";
	cft.annotation = Annotation.EMPTY_ANNOTATION;
	
	cft.location = new RangeLocation(1, 1 + seq1.length() - 1);
	cft.componentSequence = seq1;
	cft.componentLocation = new RangeLocation(1, seq1.length());
	contig.createFeature(cft);

	cft.location = new RangeLocation(10, 10 + seq2.length() - 1);
	// cft.strand = StrandedFeature.NEGATIVE;
	cft.componentSequence = seq2;
	cft.componentLocation = new RangeLocation(1, seq2.length());
	contig.createFeature(cft);

	System.out.println(contig.seqString());
	printFeatures(contig);
    }

    public static void printFeatures(FeatureHolder fh) {
	printFeatures(fh, new PrintWriter(new OutputStreamWriter(System.out)), "");
    }

    public static void printFeatures(FeatureHolder fh, 
				     PrintWriter pw,
				     String prefix)
    {
	for (Iterator i = fh.features(); i.hasNext(); ) {
	    Feature f = (Feature) i.next();
	    System.out.print(prefix);
	    System.out.print(f.getType());
	    System.out.print(" at ");
	    System.out.print("" + f.getLocation().getMin() + "-" + 
			     f.getLocation().getMax());
	    System.out.println();
	    printFeatures(f, pw, prefix + "    ");
	}
    }
}
