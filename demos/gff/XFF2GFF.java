package gff;

import java.util.*;
import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.program.gff.*;
import org.biojava.bio.program.xff.*;


public class XFF2GFF {
  public static void main(String[] args)
  throws Exception {
    if(args.length != 2) {
      System.out.println("Use: XFF2GFF xffFile gffFile");
      System.exit(1);
    }

    File xffFile = new File(args[0]);
    File gffFile = new File(args[1]);

    FeatureHolder features = XFFTools.readXFF(xffFile, xffFile.toString(), DNATools.getDNA());

    GFFWriter gffWriter = new GFFWriter(new PrintWriter(new FileWriter(gffFile)));

    new SequencesAsGFF().processSequence(
      (Sequence) features,
      gffWriter
    );
  }
}