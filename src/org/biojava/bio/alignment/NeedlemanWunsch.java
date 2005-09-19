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
package org.biojava.bio.alignment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.BioRuntimeException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.seq.impl.SimpleGappedSequence;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alignment;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.SimpleAlignment;
import org.biojava.bio.symbol.SimpleSymbolList;

/*
 * Created on 23.06.2005
 */

/**
  * @author Andreas Dr&auml;ger
  */

public class NeedlemanWunsch implements SequenceAlignment
{

  protected double[][] CostMatrix;
  protected FiniteAlphabet alpha;
  protected SubstitutionMatrix subMatrix;
  protected Alignment pairalign;
  protected String alignment;
  private double insert, delete, gapExt, match, replace;

  
  /** Just for some tests.
    * @param args
    */
  public static void main (String args[])
  {
    if (args.length < 3)
      throw new Error("Usage: NeedlemanWunsch sourceSeqFile targetSeqFile substitutionMatrixFile");
    try {
      FiniteAlphabet alphabet = (FiniteAlphabet) AlphabetManager.alphabetForName("DNA");
      SubstitutionMatrix matrix = new SubstitutionMatrix(alphabet, new File(args[2]));
      SequenceAlignment aligner = new NeedlemanWunsch( 
			alphabet, 
			2,  	// insert
			2,	// delete
			1, 	// gapExtend
			0, 	// match
			3,	// replace
			matrix 	// SubstitutionMatrix
		);
      aligner.alignAll(
        // sources
	SeqIOTools.readFastaDNA(new BufferedReader(new FileReader(args[0]))),
	// sequenceDB
	SeqIOTools.readFasta(new FileInputStream(new File(args[1])), DNATools.getDNA()));
    } catch (NoSuchElementException exc) {
      exc.printStackTrace();
    } catch (FileNotFoundException exc) {
      exc.printStackTrace();
    } catch (BioException exc) {
      exc.printStackTrace();
    } catch (IOException exc) {
      exc.printStackTrace();
    } catch (Exception exc) {
      exc.printStackTrace();
    }
  }

  /** Constructs a new Object with the given parameters based on the Needleman-Wunsch algorithm
    * @param alpha
    * @param insert
    * @param delete
    * @param gapExtend
    * @param subMat
    */
  public NeedlemanWunsch(FiniteAlphabet alpha, double insert, double delete, double gapExtend, double match, double replace, SubstitutionMatrix subMat)
  {
    this.alpha = alpha;
    this.subMatrix = subMat;
    this.alpha = subMatrix.getAlphabet();
    this.insert = insert;
    this.delete = delete;
    this.gapExt = gapExtend;  
    this.match  = match;
    this.replace = replace;
    this.alignment = "";
  }

  
  /** Prints a String representation of the CostMatrix for the given Alignment on the screen.
    * @param queryChar
    * @param targetChar
    * @return
    */
  public static String printCostMatrix (double[][] CostMatrix, char[] queryChar, char[] targetChar)
  {
    int line, col;
    String output = "\t";
   
    for (col=0; col<=targetChar.length; col++)
      if (col==0) output += "["+col+"]\t";
      else output += "["+targetChar[col-1]+"]\t";
    for (line=0; line<=queryChar.length; line++)
    {
      if (line==0) output += "\n["+line+"]\t";
      else output += "\n["+queryChar[line-1]+"]\t";
      for (col=0; col<=targetChar.length; col++)
        output += CostMatrix[line][col]+"\t";
    }
    output += "\ndelta[Edit] = "+CostMatrix[line-1][col-1]+"\n";
    return output;
  }

 
  /** Computes the optimal alignment of the two sequences and constructs a String
    * representing the alignment as well as a new Alignment object, which can be
    * used in different ways by BioJava.
    *  
    * @param query
    * @param target
    * @return alignment string representation
    */
  private String optimalAlignment(Sequence query, Sequence target) throws BioException
  {
    /*StringBuffer[] align = new StringBuffer[] {new StringBuffer(CostMatrix.length), new StringBuffer(CostMatrix[0].length)};
    StringBuffer path = new StringBuffer("");//*/
    
    String[] align = new String[] {"", ""};
    String path = "";//*/ 
    
    String output = "";
    int j = this.CostMatrix[CostMatrix.length - 1].length -1;
    SymbolTokenization st = alpha.getTokenization("default");

    for (int i = this.CostMatrix.length - 1; i>0; )
    {
      do { 
        // From now only Insert possible.
        if (i == 0) {
          align[0] = '~'     + align[0]; // st.tokenizeSymbol(alpha.getGapSymbol()) + align[0]; //
          align[1] = st.tokenizeSymbol(target.symbolAt(j--)) + align[1];
          path = ' ' + path;//*/
          
        // From now only Delete possible.
        } else if (j == 0) { 
          align[0] = st.tokenizeSymbol(query.symbolAt(i--))  + align[0];
          align[1] = '~'     + align[1]; // st.tokenizeSymbol(alpha.getGapSymbol()) + align[1]; //
          path = ' ' + path;//*/
          
        // Match/Replace
        } else if (CostMatrix[i-1][j-1] == min(CostMatrix[i][j-1], CostMatrix[i-1][j-1], CostMatrix[i-1][j]))  { 
          align[0] = st.tokenizeSymbol(query.symbolAt(i--)) + align[0];
          align[1] = st.tokenizeSymbol(target.symbolAt(j--)) + align[1];//*/
          if (query.symbolAt(i+1) == target.symbolAt(j+1))
            path = '|' + path;//*/ 
          else path = ' ' + path;//*/
          
        // Insert
        } else if ((CostMatrix[i][j-1] < CostMatrix[i-1][j-1]) && (CostMatrix[i][j-1] < CostMatrix[i-1][j])) {
          align[0] = '-'     + align[0]; // st.tokenizeSymbol(alpha.getGapSymbol()) + align[0]; //
          align[1] = st.tokenizeSymbol(target.symbolAt(j--)) + align[1];
          path = ' ' + path;//*/
  
        // Delete
        } else { 
          align[0] = st.tokenizeSymbol(query.symbolAt(i--))  + align[0];
          align[1] = '-'     + align[1]; // st.tokenizeSymbol(alpha.getGapSymbol()) + align[1]; //
          path = ' ' + path;//*/
        }
      } while (j>0);
    }

    // construct the biojava Alignment object:    
    query = new SimpleGappedSequence(
      new SimpleSequence(
        new SimpleSymbolList(query.getAlphabet().getTokenization("token"), align[0]), 
        query.getURN(), 
        query.getName(), 
        query.getAnnotation()));
    target = new SimpleGappedSequence(
      new SimpleSequence(
        new SimpleSymbolList(target.getAlphabet().getTokenization("token"), align[1]),
        target.getURN(),
        target.getName(),
        target.getAnnotation()));
    Map m = new HashMap();
    m.put(query.getName(), query);
    m.put(target.getName(), target);
    pairalign = new SimpleAlignment(m);
        
    output += "Length:\t"+align[0].length()+"\n";
    output += "Score:\t\t"+(-1)*getEditDistance()+"\n\n";
        
    int currline = Math.min(60, align[0].length()); 
    // counts the absoulute position in the String.

    output += "\nQuery:\t"  + align[0].substring(0, currline); 
    output += "\n\t"        + path.substring(0, currline);
    output += "\nTarget:\t" + align[1].substring(0, currline)+"\n";
    
    for (; currline+60 < align[0].length(); currline+=60) {
      output += "\nQuery:\t"  + align[0].substring(currline, currline+60);
      output += "\n\t"        + path.substring(currline, currline+60);
      output += "\nTarget:\t" + align[1].substring(currline, currline+60)+"\n";
    }
    if (currline+1 < align[0].length()) {
      output += "\nQuery:\t"  + align[0].substring(currline, align[0].length());
      output += "\n\t"        + path.substring(currline, path.length());
      output += "\nTarget:\t" + align[1].substring(currline, align[1].length())+"\n";
    }

    output += "\n\n";
 
    return output;    
  }
  
  /** prints the alignment String on the screen.
    * @param align
    */
  public static void printAlignment(String align)
  { 
    System.out.print(align);
  }
  
  
  /**
    * @return Alignment object containing the two gapped sequences constructed from query and target.
    * @throws Exception
    */
  public Alignment getAlignment(Sequence query, Sequence target) throws Exception
  {
    pairwiseAlignment(query, target);
    return pairalign;  
  }
  
  /** 
    * @return returns the edit_distance computed with the given parameters.
    */
  public double getEditDistance()
  {
    return CostMatrix[CostMatrix.length-1][CostMatrix[CostMatrix.length-1].length - 1];
  }

  
  /** 
    * @param x
    * @param y
    * @param z
    * @return Gives the minimum of three doubles
    */
  protected static double min (double x, double y, double z)
  {
    if ((x < y) && (x < z)) return x;
    if (y < z) return y;
    return z;
  }


  /* (non-Javadoc)
   * @see toolbox.align.SequenceAlignment#getAlignment()
   */
  public String getAlignmentString() throws BioException
  {
    return alignment;
  }
  
  
  /* (non-Javadoc)
   * @see toolbox.align.SequenceAlignment#alignAll(org.biojava.bio.seq.SequenceIterator, org.biojava.bio.seq.db.SequenceDB)
   */
  public List alignAll(SequenceIterator source, SequenceDB subjectDB) throws NoSuchElementException, BioException
  {
    List l = new LinkedList();
    while (source.hasNext()) {
  	  Sequence query = source.nextSequence();
  	  // compare all the sequences of both sets.
  	  SequenceIterator target = subjectDB.sequenceIterator();
      while (target.hasNext()) 
        try {
          l.add(getAlignment(query, target.nextSequence()));
          //pairwiseAlignment(query, target.nextSequence());
        } catch (Exception exc) {
          exc.printStackTrace();
        }
    }
    return l;
  }


  /** Global pairwise sequence alginment of two BioJava-Sequence objects according to the
    * Needleman-Wunsch-algorithm. 
    *
    * @see toolbox.align.SequenceAlignment#pairAlign(org.biojava.bio.seq.Sequence, org.biojava.bio.seq.Sequence)
    */
  public double pairwiseAlignment(Sequence query, Sequence subject) throws BioRuntimeException
  {
    if (query.getAlphabet().equals(subject.getAlphabet()) && query.getAlphabet().equals(alpha)) {
    
      long time = System.currentTimeMillis();
      int   i, j;
      double matchReplace;
      this.CostMatrix   = new double[query.length()+1][subject.length()+1]; // Matrix CostMatrix

      // construct the matrix:
      CostMatrix[0][0] = 0;
    
      /* If we want to have affine gap penalties, we have to initialise additional matrices:
       * If this is not necessary, we won't do that (because it's expensive).
       */
      if ((gapExt != delete) || (gapExt != insert)) {
      
        double[][] E = new double[query.length()+1][subject.length()+1];	// Inserts
        double[][] F = new double[query.length()+1][subject.length()+1];	// Deletes
        double[][] G = new double[query.length()+1][subject.length()+1];	// Match/Replace
      
        G[0][0] = 0;
        E[0][0] = F[0][0]   = Double.MAX_VALUE;
        for (i=1; i<=query.length();   i++) {
          CostMatrix[i][0]  = CostMatrix[i-1][0] + delete;
          G[i][0] = E[i][0] = Double.MAX_VALUE;
          F[i][0] = delete;
        }
        for (j=1; j<=subject.length(); j++) {
          CostMatrix[0][j]  = CostMatrix[0][j-1] + insert;
          G[0][j] = F[0][j] = Double.MAX_VALUE;
          E[0][j] = insert;
        }
        for (i=1; i<=query.length();   i++)
          for (j=1; j<=subject.length(); j++)
          {
            try {
              matchReplace = subMatrix.getValueAt(query.symbolAt(i), subject.symbolAt(j));
            } catch (Exception exc) {
              if (query.symbolAt(i).getMatches().contains(subject.symbolAt(j)) ||
                  subject.symbolAt(j).getMatches().contains(query.symbolAt(i)))
                matchReplace = match; //0;
              else matchReplace = replace; //delete/2 + insert/2;
            }
            G[i][j] = CostMatrix[i-1][j-1] - matchReplace;
            E[i][j] = Math.min(E[i][j-1], CostMatrix[i][j-1] + insert) + gapExt;
            F[i][j] = Math.min(F[i-1][j], CostMatrix[i-1][j] + delete) + gapExt;
            CostMatrix[i][j] = min (E[i][j], F[i][j], G[i][j]);
          }
      
      } else {
        /*
        * No affine gap penalties, constant gap penalties, which is much faster and needs less memory.
        */
        for (i=1; i<=query.length();   i++) CostMatrix[i][0] = CostMatrix[i-1][0] + delete;
        for (j=1; j<=subject.length(); j++) CostMatrix[0][j] = CostMatrix[0][j-1] + insert;
        for (i=1; i<=query.length();   i++)
          for (j=1; j<=subject.length(); j++)
          {        
            try {
              matchReplace = subMatrix.getValueAt(query.symbolAt(i), subject.symbolAt(j));
            } catch (Exception exc) {
              if (query.symbolAt(i).getMatches().contains(subject.symbolAt(j)) ||
                subject.symbolAt(j).getMatches().contains(query.symbolAt(i)))
                matchReplace = match; //0;
              else matchReplace = replace; //delete/2 + insert/2;
           }
           CostMatrix[i][j] = min (
             CostMatrix[i-1][j]   + delete, 
             CostMatrix[i][j-1]   + insert, 
             CostMatrix[i-1][j-1] - matchReplace);
         }
    } 
      // Everything is the same from here:
      try {
        //this.printCostMatrix(query.seqString().toCharArray(), target.seqString().toCharArray());
        // generate "String" with optimal Alignment.
        String tmp = this.optimalAlignment(query, subject) + "\n";
        time = System.currentTimeMillis() - time;
        this.alignment += "Time in ms:\t"+time + tmp;
        // print the Alignment on the screen
        //this.printAlignment(alignment);		
        
      } catch (BioException exc) {
        exc.printStackTrace();
      }    
          
      return getEditDistance();
      
    } else throw new BioRuntimeException("Alphabet missmatch occured: sequences with different alphabet cannot be aligned.");
  }


}
