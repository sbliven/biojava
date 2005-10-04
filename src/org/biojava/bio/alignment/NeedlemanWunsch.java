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

/** Needleman and Wunsch definied the problem of global sequence alignments, 
  * from the first till the last symbol of a sequence. 
  * This class is able to perform such global sequence comparisons efficiently
  * by dynamic programing. If inserts and deletes are equally expensive and
  * as expensive as the extension of a gap, the alignment method of this class
  * does not use affine gap panelties. Otherwise it does. Those costs need
  * four times as much memory, which has significant effects on the run time,
  * if the computer needs to swap.
  *
  * @author Andreas Dr&auml;ger
  */

public class NeedlemanWunsch extends SequenceAlignment
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
    * @param alpha The alphabet of the sequences to be aligned by this class.
    * @param insert The costs of a single insert operation.
    * @param delete The expenses of a single delete operation.
    * @param gapExtend The expenses of an extension of a existing gap (that is a previous insert or
    *       delete. If the costs for insert and delete are equal and also equal to gapExtend, no
    *       affine gap penalties will be used, which saves a significant amount of memory.
    * @param match This gives the costs for a match operation. It is only used, if there is no entry
    *       for a certain match of two symbols in the substitution matrix (default value).
    * @param replace This is like the match parameter just the default, if there is no entry in the
    *       substitution matrix object.
    * @param subMat The substitution matrix object which gives the costs for matches and replaces.
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
    * This can be used to get a better understanding of the algorithm. There is no other purpose.
    * This method also works for all extensions of this class with all kinds of matrices.
    * @param queryChar a character representation of the query sequence 
    *   (<code>mySequence.seqString().toCharArray()</code>).
    * @param targetChar a character representation of the target sequence.
    * @return a String representation of the matrix.
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

  
  /** prints the alignment String on the screen (standard output).
    * @param align The parameter is typically given by the {@link #getAlignmentString() getAlignmentString()} method.
    */
  public static void printAlignment(String align)
  { 
    System.out.print(align);
  }
  
  
  /** This method is good if one wants to reuse the alignment calculated by this class in another
    * BioJava class. It just performs {@link #pairwiseAlignment(Sequence, Sequence) pairwiseAlignment} and returns an <code>Alignment</code>
    * instance containing the two aligned sequences.
    * @return Alignment object containing the two gapped sequences constructed from query and target.
    * @throws Exception
    */
  public Alignment getAlignment(Sequence query, Sequence target) throws Exception
  {
    pairwiseAlignment(query, target);
    return pairalign;  
  }
  
  /** This gives the edit distance acording to the given parameters of this certain 
    * object. It returns just the last element of the internal cost matrix (left side
    * down). So if you extend this class, you can just do the following:
    * <code>double myDistanceValue = foo; this.CostMatrix = new double[1][1]; this.CostMatrix[0][0] = myDistanceValue;</code>
    * @return returns the edit_distance computed with the given parameters.
    */
  public double getEditDistance()
  {
    return CostMatrix[CostMatrix.length-1][CostMatrix[CostMatrix.length-1].length - 1];
  }

  
  /** This just computes the minimum of three double values.
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
    * @see org.biojava.bio.alignment.SequenceAlignment#pairwiseAlignment(org.biojava.bio.seq.Sequence, org.biojava.bio.seq.Sequence)
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
    try {
        
      /*StringBuffer[] align = new StringBuffer[] {new StringBuffer(CostMatrix.length), new StringBuffer(CostMatrix[0].length)};
       StringBuffer path = new StringBuffer("");//*/
        
      String[] align = new String[] {"", ""};
      String path = "";//*/
        
      j = this.CostMatrix[CostMatrix.length - 1].length -1;
      SymbolTokenization st = alpha.getTokenization("default");

       for (i = this.CostMatrix.length - 1; i>0; )
       {
          do { 
            // only Insert.
            if (i == 0) {
              /*align[0].insert(0, '~');
              align[1].insert(0, st.tokenizeSymbol(target.symbolAt(j--)));
              path.insert(0, ' ');//*/
              align[0] = '~' + align[0]; // st.tokenizeSymbol(alpha.getGapSymbol()) + align[0]; //
              align[1] = st.tokenizeSymbol(subject.symbolAt(j--)) + align[1];
              path     = ' ' + path;//*/
              
            // only Delete.
            } else if (j == 0) { 
              /*align[0].insert(0, st.tokenizeSymbol(query.symbolAt(i--)));
              align[1].insert(0, '~');
              path.insert(0, ' ');//*/
              align[0] = st.tokenizeSymbol(query.symbolAt(i--))  + align[0];
              align[1] = '~' + align[1]; // st.tokenizeSymbol(alpha.getGapSymbol()) + align[1]; //
              path     = ' ' + path;//*/
              
            // Match/Replace
            } else if (CostMatrix[i-1][j-1] == min(CostMatrix[i][j-1], CostMatrix[i-1][j-1], CostMatrix[i-1][j]))  { 
              if (query.symbolAt(i) == subject.symbolAt(j)) {
                /*path.insert(0, '|');//*/
                path = '|' + path;//*/ 
              } else {
                path = ' ' + path;//*/
                /*path.insert(0, ' ');//*/
              } /*            
              align[0].insert(0, st.tokenizeSymbol(query.symbolAt(i--)));
              align[1].insert(0, st.tokenizeSymbol(target.symbolAt(j--)));//*/
              align[0] = st.tokenizeSymbol(query.symbolAt(i--)) + align[0];
              align[1] = st.tokenizeSymbol(subject.symbolAt(j--)) + align[1];//*/
              /* ist nicht so gut, weil sonst auch Striche zwischen ambigious Symbols kommen.
               *if (query.symbolAt(i+1).getMatches().contains(target.symbolAt(j+1))) 
               */
              
            // Insert
            } else if ((CostMatrix[i][j-1] < CostMatrix[i-1][j-1]) && (CostMatrix[i][j-1] < CostMatrix[i-1][j])) {
             /*align[0].insert(0, '-');
             align[1].insert(0, st.tokenizeSymbol(target.symbolAt(j--)));
             path.insert(0, ' ');//*/
              align[0] = '-' + align[0]; // st.tokenizeSymbol(alpha.getGapSymbol()) + align[0]; //
              align[1] = st.tokenizeSymbol(subject.symbolAt(j--)) + align[1];
              path     = ' ' + path;//*/
            
            // Delete
            } else { 
              /*align[0].insert(0, st.tokenizeSymbol(query.symbolAt(i--)));
              align[1].insert(0, '-');
              path.insert(0, ' ');//*/
              align[0] = st.tokenizeSymbol(query.symbolAt(i--))  + align[0];
              align[1] = '-'  + align[1]; // st.tokenizeSymbol(alpha.getGapSymbol()) + align[1]; //
              path     = ' '  + path;//*/
            }
          } while (j>0);
        }
                
        query = new SimpleGappedSequence(
          new SimpleSequence(
            new SimpleSymbolList(query.getAlphabet().getTokenization("token"), align[0]), 
            query.getURN(), 
            query.getName(), 
            query.getAnnotation()));
        subject = new SimpleGappedSequence(
          new SimpleSequence(
            new SimpleSymbolList(subject.getAlphabet().getTokenization("token"), align[1]),
            subject.getURN(),
            subject.getName(),
            subject.getAnnotation()));
        Map m = new HashMap();
        m.put(query.getName(),    query);
        m.put(subject.getName(), subject);
        pairalign = new SimpleAlignment(m);
        
        // this.printCostMatrix(queryChar, targetChar);	// only for tests important
        this.alignment = formatOutput(
            query.getName(), 
            subject.getName(), 
            align, 
            path, 
            0, 
            CostMatrix.length-1, 
            CostMatrix.length-1,
            0, 
            CostMatrix[0].length-1,
            CostMatrix[0].length-1,
            getEditDistance(),
            System.currentTimeMillis() - time) + "\n";
        
        //System.out.println(printCostMatrix(CostMatrix, query.seqString().toCharArray(), subject.seqString().toCharArray()));   
        return getEditDistance();
        
      } catch (BioException exc) {
        throw new BioRuntimeException(exc);
      }
    } else throw new BioRuntimeException("Alphabet missmatch occured: sequences with different alphabet cannot be aligned.");
  }


}
