/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.bio.program.phred;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.dist.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.utils.*;

/**
 * Title:        PhredTools
 * Description:  Static methods for working with phred quality data
 * Copyright:    Copyright (c) 2001
 * Company:      AgResearch
 * @author Mark Schreiber
 * @since 1.1
 *
 * Note that Phred is a copyright of CodonCode Corporation
 */

public class PhredTools {

  /**
   * Writes Phred quality data in a Fasta type format.
   * @param db a bunch of PhredSequence objects
   * @param qual the OutputStream to write the quality data to.
   * @param seq the OutputStream to write the sequence data to.
   * @author Mark Schreiber
   * @since 1.2
   */
   public static void writePhredQuality(OutputStream qual, OutputStream seq, SequenceDB db)
    throws IOException, BioException{
      StreamWriter qualw = new StreamWriter(qual,new PhredFormat());
      StreamWriter seqw = new StreamWriter(seq, new FastaFormat());
      SequenceDB qualDB = new HashSequenceDB(IDMaker.byName);
      //Get the quality SymbolLists and add them to a SeqDB
      for(SequenceIterator i = db.sequenceIterator(); i.hasNext();){
        Sequence p = i.nextSequence();
        if(p instanceof PhredSequence){
          PhredSequence ps = (PhredSequence)p;
          SymbolList ql = ps.getQuality();
          try{
            qualDB.addSequence( new SimpleSequence(ql,p.getURN(),p.getName(),p.getAnnotation()));
          }catch(ChangeVetoException cve){
            throw new NestedError(cve,"Cannot Add Quality Sequences to Database");
          }
        }
        else{
          throw new BioException("Expecting PhredSequence, got " + p.getClass().getName());
        }
      }
      qualw.writeStream(qualDB.sequenceIterator());
      seqw.writeStream(db.sequenceIterator());//this works as sequence methods act on the underlying SimpleSequence
   }

  /**
   * Constructs a StreamReader to read in Phred quality data in FASTA format.
   * The data is converted into sequences consisting of Symbols from the IntegerAlphabet.
   */
  public static StreamReader readPhredQuality(BufferedReader br){
    return new StreamReader(br,
      new PhredFormat(),
      getQualityParser(),
      getFastaBuilderFactory());
  }

  public static PhredSequence makePhredSequence(Sequence seq, Sequence quality, String name, String urn, Annotation anno){
    return new PhredSequence(seq,quality,name,urn,anno);
  }

  /**
   * Calls SeqIOTools.readFastaDNA(br), added here for convinience.
   */
  public static StreamReader readPhredSequence(BufferedReader br){
    return (StreamReader)SeqIOTools.readFastaDNA(br);
  }


  private static SequenceBuilderFactory _fastaBuilderFactory;

    /**
     * Get a default SequenceBuilderFactory for handling FASTA
     * files.
     */
  private static SequenceBuilderFactory getFastaBuilderFactory() {
      if (_fastaBuilderFactory == null) {
          _fastaBuilderFactory = new FastaDescriptionLineParser.Factory(SimpleSequenceBuilder.FACTORY);
      }
      return _fastaBuilderFactory;
  }

  /**
   * returns the IntegerAlphabet parser
   */
  private static SymbolParser getQualityParser() {
    return IntegerAlphabet.getInstance().getParser("token");
  }

  /**
   * The quality value is related to the base call error probability
   * by the formula  QV = - 10 * log_10( P_e )
   * where P_e is the probability that the base call is an error.
   * @return a <code>double</code> value, note that for most Phred scores this will be rounded
   * to the nearest <code>int</code>
   */
   public static double qualityFromP(double probOfError){
     return (-10 * (Math.log(probOfError)/Math.log(10.0)));
   }

   /**
    * Calculates the probability of an error from the quality score via the formula
    *  P_e = 10**(QV/-10)
    */
    public static double pFromQuality(double quality){
      return Math.pow(10.0,(quality/-10.0));
    }

    /**
     * Calculates the probability of an error from the quality score via the formula
     *  P_e = 10**(QV/-10)
     */
    public static double pFromQuality(int quality){
      return pFromQuality((double)quality);
    }

    /**
     * Calculates the probability of an error from the quality score via the formula
     *  P_e = 10**(QV/-10)
     */
    public static double pFromQuality(IntegerAlphabet.IntegerSymbol quality){
      return pFromQuality(quality.intValue());
    }

    /**
     * Converts a Phred sequence to an array of distributions. Essentially a fuzzy sequence
     * Assumes that all of the non called bases are equiprobable
     */
    public static Distribution[] phredToDistArray(PhredSequence s){
      Distribution[] pos = new Distribution[s.length()];
      DistributionTrainerContext dtc = new SimpleDistributionTrainerContext();

      for (int i = 0; i < s.length(); i++) {// for each symbol in the phred sequence
        Symbol qual = s.getQualityAt(i);
        Symbol base = s.symbolAt(i);
        double pBase = pFromQuality((IntegerAlphabet.IntegerSymbol)qual);
        double pOthers = (1.0 - pBase)/3;

        try{
          pos[i] = DistributionFactory.DEFAULT.createDistribution(DNATools.getDNA());
          dtc.registerDistribution(pos[i]);

          for(Iterator iter = (DNATools.getDNA().iterator()); iter.hasNext();){
            Symbol sym = (Symbol)iter.next();
            if(sym.equals(base)) pos[i].setWeight(sym,pBase);
            else pos[i].setWeight(sym,pOthers);
          }

          dtc.train();
        }catch(IllegalAlphabetException iae){
          throw new NestedError(iae,"Sequence "+s.getName()+" contains an illegal alphabet");
        }catch(ChangeVetoException cve){
          throw new NestedError(cve, "The Distribution has become locked");
        }catch(IllegalSymbolException ise){
          throw new NestedError(ise, "Sequence "+s.getName()+" contains an illegal symbol");
        }
      }
      return pos;
    }

    /**
     * converts an Alignment of PhredSequences to a Distribution[] where each position is the average
     * distribution of the underlying column of the alignment.
     * @throws ClassCastException if the sequences in the alignment are not instances of PhredSequence
     */
    public static Distribution[] phredAlignmentToDistArray(Alignment a){
      List labels = a.getLabels();
      int depth = labels.size();
      Distribution [] average = new Distribution[a.length()];

      Distribution[][] matrix = new Distribution[labels.size()][];
      for(int y = 0; y < a.length(); y++){// for eaxh position
        for(Iterator i = labels.iterator(); i.hasNext();){
          SymbolList sl = a.symbolListForLabel(i.next());
          matrix[y] = phredToDistArray((PhredSequence)sl);
        }
        average[y] = DistributionTools.average(matrix[y]);
      }

      return average;
    }
}