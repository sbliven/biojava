package symbol;

import org.biojava.bio.*;
import org.biojava.utils.*;
import org.biojava.bio.dist.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.symbol.*;

import java.util.*;
import java.io.*;

/**
 * Title:        WindowCount
 * Description:  A program to find the distribution of nmers in a fasta
library
 * Copyright:    Copyright (c) 2001
 * Company:      AgResearch
 * @author Mark Schreiber
 * @version 1.0
 */
public class WindowCount {

  public static void main(String[] args) {
    try{
      File infile = new File(args[0]);
      Integer order = new Integer(args[1]);
      Double threshold = new Double(1.0 /
      Math.pow(4.0,(double)order.intValue()));
      FiniteAlphabet dna = DNATools.getDNA();
      SequenceDB seqs = readSequenceDB(infile,dna);

      //create a cross product of N dna alphabets
      FiniteAlphabet nOrderAlpha =
      (FiniteAlphabet)AlphabetManager.getCrossProductAlphabet(
 
Collections.nCopies(order.intValue(),DNATools.getDNA())
                                );

      //create a distribution for the alphabet and a trainer.
      Distribution d =
DistributionFactory.DEFAULT.createDistribution(nOrderAlpha);
      DistributionTrainer dt = new SimpleDistributionTrainer(d);
      DistributionTrainerContext context =
                               new SimpleDistributionTrainerContext();

      //for each sequence
      SequenceIterator iter = seqs.sequenceIterator();
      while (iter.hasNext()) {
        SymbolList s = iter.nextSequence();
        SymbolList nseq =
SymbolListViews.orderNSymbolList(s,order.intValue());

        //add nmer counts to the distribution
        Iterator nmers = nseq.iterator();
        while(nmers.hasNext()){
          Object nmer = nmers.next();
          try{
            dt.addCount(context,(AtomicSymbol)nmer,1.0);
            //System.out.println("+");
          }catch(ClassCastException cce){
            //System.err.println(".");
            continue;// ignore the redundant basis symbols
          }
        }
      }

      //train the distribution.
      context.train();

      //return the list of nmer symbols in the alphabet
      SymbolList nOrderSymbols = nOrderAlpha.symbols();

      //Add each symbol and its counts to a collection so they can be sorted
      Iterator symbols = nOrderSymbols.iterator();
      SortedMap tree = new TreeMap();
      while(symbols.hasNext()){
        AtomicSymbol s = (AtomicSymbol)symbols.next();
        Double weight = new Double(d.getWeight(s)); // the key
        tree.put(weight,s);
      }

      //Print out the nmers above the threshold
      SortedMap sig = tree.tailMap(threshold);
      Set keys = sig.keySet();
      System.out.println("threshold = " + threshold.doubleValue());
      System.out.println("\nNMER\tWEIGHT");
      Iterator keysI = keys.iterator();
      while(keysI.hasNext()){
        Double key = (Double)keysI.next();
        AtomicSymbol value = (AtomicSymbol)sig.get(key);
        output(key, value);
      }

    }catch(IOException ioe){
      ioe.printStackTrace(System.err);
    }catch(Exception e){
     e.printStackTrace(System.err);
    }
  }

    /**
     * Create a sequence database from a fasta file.
     */
    public static SequenceDB readSequenceDB(File seqFile, Alphabet alpha)
            throws Exception {
    HashSequenceDB seqDB = new HashSequenceDB(IDMaker.byName);

    SequenceBuilderFactory sbFact = new FastaDescriptionLineParser.Factory(
                                            SimpleSequenceBuilder.FACTORY);
    FastaFormat fFormat = new FastaFormat();
    for(
      SequenceIterator seqI = new StreamReader(
        new FileInputStream(seqFile),
        fFormat,
        alpha.getTokenization("token"),
        sbFact
      );
      seqI.hasNext();
    ) {
      Sequence seq = seqI.nextSequence();
      seqDB.addSequence(seq);
    }

    return seqDB;
  }

  public static void output(Double d, AtomicSymbol s){
     //get the symbols that make up the atomic symbol
     //  List syms = ((BasisSymbol)s).getSymbols();
//       //print the symbol
//       Iterator iter = syms.iterator();
//       while (iter.hasNext()) {
//         Symbol subSymbol = (Symbol)iter.next();
//         System.out.print(subSymbol.getToken());
//       }

      System.out.print(s.getName());

     //print the double value
     System.out.println("\t" + d.doubleValue());
  }

  public static void usage(){
    System.out.println("\n\n\t***USAGE***\n\n");
    System.out.println("java WindowCount <file> [size]");
    System.out.println("\n\tfile\tFile in Fasta Format");
    System.out.println("\tsize\tSize of nmers to count");
    //bail out!
    System.exit(0);
  }
}

