/*
 * Created on 05.09.2005
 *
 */
package org.biojava.bio.alignment;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.BioRuntimeException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.impl.SimpleGappedSequence;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.SimpleAlignment;
import org.biojava.bio.symbol.SimpleSymbolList;


/** Smith and Waterman developed an efficient dynamic programing algorithm
  * to perform local sequence alignments, which returns the most conserved
  * region of two sequences (longes common substring with modifications).
  * This algorithm is performed by the method <code>pairwiseAlignment</code>
  * of this class. It uses affine gap penalties if and only if the expenses
  * of a delete or insert operation are unequal to the expenses of gap extension.
  * This uses significantly more memory (four times as much) and increases
  * the runtime if swaping is performed.
  * 
  * @author Andreas Dr&auml;ger
  */
public class SmithWaterman extends NeedlemanWunsch
{

  private double match, replace, insert, delete, gapExt;
  private double[][] scoreMatrix;  
  

  /** Constructs the new SmithWaterman alignment object. Alignments are only performed,
    * if the alphabet of the given <code>SubstitutionMatrix</code> equals the alpabet of
    * both the query and the target <code>Sequence</code>. The alignment parameters here
    * are expenses and not scores as they are in the <code>NeedlemanWunsch</code> object.
    * scores are just given by multipliing the expenses with <code>(-1)</code>. For example
    * you could use parameters like "-2, 5, 3, 3, 0". If the expenses for gap extension
    * are equal to the cost of starting a gap (delete or insert), no affine gap penalties
    * are used, which saves memory.
    *
    * @param match expenses for a match
    * @param replace expenses for a replace operation
    * @param insert expenses for a gap opening in the query sequence
    * @param delete expenses for a gap opening in the target sequence
    * @param gapExtend expenses for the extension of a gap which was started earlier.
    * @param matrix the <code>SubstitutionMatrix</code> object to use.
    */
  public SmithWaterman(double match, double replace, double insert, double delete, double gapExtend, SubstitutionMatrix matrix)
  {
    super(matrix.getAlphabet(), insert, delete, gapExtend, match, replace, matrix);
    this.match      = -match;
    this.replace    = -replace;
    this.insert     = -insert;
    this.delete     = -delete;
    this.gapExt     = -gapExtend;
    this.subMatrix  = matrix;
    this.alignment  = "";
  }
  

  /** Overrides the method inherited from the NeedlemanWunsch and performs only a local alignment.
    * It finds only the longest common subsequence. This is good for the beginning, but it might
    * be better to have a system to find more than only one hit within the score matrix. Therfore
    * one should only define the k-th best hit, where k is somehow related to the number of hits.
    *
    * @see SequenceAlignment#pairwiseAlignment(org.biojava.bio.seq.Sequence, org.biojava.bio.seq.Sequence)
    */
  public double pairwiseAlignment(Sequence query, Sequence subject) throws BioRuntimeException
  {    
    if (query.getAlphabet().equals(subject.getAlphabet()) && query.getAlphabet().equals(alpha)) {
      
      long time = System.currentTimeMillis();
      int i, j, maxI = 0, maxJ = 0;
      double matchReplace;
      this.scoreMatrix = new double[query.length()+1][subject.length()+1];    
    
      /*
      * Use affine gap panalties.
      */
      if ((gapExt != delete) || (gapExt != insert)) {
        
        double[][] E = new double[query.length()+1][subject.length()+1];	// Inserts
        double[][] F = new double[query.length()+1][subject.length()+1];	// Deletes
        double[][] G = new double[query.length()+1][subject.length()+1];	// Match/Replace
        
        G[0][0] = scoreMatrix[0][0] = 0;
        E[0][0] = F[0][0]   = Double.NEGATIVE_INFINITY;
        for (i=1; i<=query.length();   i++) {
          scoreMatrix[i][0]  = scoreMatrix[i-1][0] + delete;
          G[i][0] = E[i][0] = Double.NEGATIVE_INFINITY;
          F[i][0] = delete;
        }
        for (j=1; j<=subject.length(); j++) {
          scoreMatrix[0][j]  = scoreMatrix[0][j-1] + insert;
          G[0][j] = F[0][j] = Double.NEGATIVE_INFINITY;
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
                matchReplace = match; 
              else matchReplace = replace;
            }
            
            G[i][j] = scoreMatrix[i-1][j-1] + matchReplace;
            E[i][j] = Math.max(E[i][j-1], scoreMatrix[i][j-1] + insert) + gapExt;
            F[i][j] = Math.max(F[i-1][j], scoreMatrix[i-1][j] + delete) + gapExt;
            scoreMatrix[i][j] = max(0, E[i][j], F[i][j], G[i][j]);
            
            if (scoreMatrix[i-1][j] < Math.min(scoreMatrix[i][j-1], scoreMatrix[i-1][j-1]))
              scoreMatrix[i-1][j] = Double.NEGATIVE_INFINITY;
            if (scoreMatrix[i][j-1] < Math.min(scoreMatrix[i-1][j], scoreMatrix[i-1][j-1]))
              scoreMatrix[i][j-1] = Double.NEGATIVE_INFINITY;
            if (scoreMatrix[i-1][j-1] < Math.max(scoreMatrix[i-1][j], scoreMatrix[i][j-1]))
              scoreMatrix[i-1][j-1] = Double.NEGATIVE_INFINITY;
            
            if (scoreMatrix[i][j] > scoreMatrix[maxI][maxJ]) {
              maxI = i;
              maxJ = j;
            }              
          }
        //System.out.println(printCostMatrix(G, query.seqString().toCharArray(), subject.seqString().toCharArray()));
      
      /*
      * No affine gap penalties to save memory.
      */
      } else {
              
        for (i=0; i<=query.length();   i++) scoreMatrix[i][0] = 0;
        for (j=0; j<=subject.length(); j++) scoreMatrix[0][j] = 0;
        for (i=1; i<=query.length();   i++) 
          for (j=1; j<=subject.length(); j++) {
        
            try {
              matchReplace = subMatrix.getValueAt(query.symbolAt(i), subject.symbolAt(j));
            } catch (Exception exc) {
              if (query.symbolAt(i).getMatches().contains(subject.symbolAt(j)))
                matchReplace = match;
              else matchReplace = replace;
            }
            
            scoreMatrix[i][j] = max(
              0.0,
              scoreMatrix[i-1][j]   + delete,
              scoreMatrix[i][j-1]   + insert,
              scoreMatrix[i-1][j-1] + matchReplace
            );
                        
            if (scoreMatrix[i-1][j] < Math.min(scoreMatrix[i][j-1], scoreMatrix[i-1][j-1]))
              scoreMatrix[i-1][j] = Double.NEGATIVE_INFINITY;
            if (scoreMatrix[i][j-1] < Math.min(scoreMatrix[i-1][j], scoreMatrix[i-1][j-1]))
              scoreMatrix[i][j-1] = Double.NEGATIVE_INFINITY;
            if (scoreMatrix[i-1][j-1] < Math.max(scoreMatrix[i-1][j], scoreMatrix[i][j-1]))
              scoreMatrix[i-1][j-1] = Double.NEGATIVE_INFINITY;
        
            if (scoreMatrix[i][j] > scoreMatrix[maxI][maxJ]) {
              maxI = i;
              maxJ = j;
            }
          }
      }
      
      /*
       * From here both cases are equal again.
       */
      
      //System.out.println(printCostMatrix(scoreMatrix, query.seqString().toCharArray(), subject.seqString().toCharArray()));
     
      try {
      
        String[] align = new String[] {"", ""};
        String path = "";
        SymbolTokenization st = query.getAlphabet().getTokenization("default");
    
        j = maxJ;
        for (i=maxI; (i>0) && (scoreMatrix[i][j] != Double.NEGATIVE_INFINITY); ) {
          do {
            // only Deletes or Inserts or Replaces possible. That's not what we want to have.
            if ((i == 0) || (j == 0) || (scoreMatrix[i][j] == 0)) {
              i = j = 0;
            
            // Nothing is possible anymore.
            } else if ((scoreMatrix[i][j-1] == scoreMatrix[i-1][j]) && 
                (scoreMatrix[i-1][j-1] == scoreMatrix[i-1][j]) && 
                (scoreMatrix[i-1][j]   == Double.NEGATIVE_INFINITY)) {
              
              if (query.symbolAt(i) == subject.symbolAt(j)) path = '|' + path;
              else path = ' ' + path;
              
              align[0] = st.tokenizeSymbol(query.symbolAt(i))   + align[0];
              align[1] = st.tokenizeSymbol(subject.symbolAt(j)) + align[1];
              
              i = j = 0;
          
            // Insert
            } else if (scoreMatrix[i][j-1] > Math.max(scoreMatrix[i-1][j-1], scoreMatrix[i-1][j])) {
              align[0] = '-' + align[0];
              align[1] = st.tokenizeSymbol(subject.symbolAt(j--)) + align[1];
              path     = ' ' + path;
             
            // Delete
            } else if (scoreMatrix[i-1][j] > Math.max(scoreMatrix[i-1][j-1], scoreMatrix[i][j-1])) {
              align[0] = st.tokenizeSymbol(query.symbolAt(i--)) + align[0];
              align[1] = '-' + align[1];
              path     = ' ' + path;
           
            // Match/Replace
            } else {
              if (query.symbolAt(i) == subject.symbolAt(j)) path = '|' + path;
              else path = ' ' + path;
              
              align[0] = st.tokenizeSymbol(query.symbolAt(i--))   + align[0];
              align[1] = st.tokenizeSymbol(subject.symbolAt(j--)) + align[1];
            }

          } while ((j>0) && (scoreMatrix[i][j] != Double.NEGATIVE_INFINITY));
        }

        this.CostMatrix = new double[1][1];
        CostMatrix[0][0] = -scoreMatrix[maxI][maxJ];
                  
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
        m.put(query.getName(), query);
        m.put(subject.getName(), subject);
        pairalign = new SimpleAlignment(m);

        /*
        * Construct the output with only 60 symbols in each line.
        */
        
        time = System.currentTimeMillis() - time;  
                    
        String output = "Time in ms:\t"+ time + "\n";
        output += "Length:  \t"+align[0].length()+"\n";
        //output += "Distance:\t"+getEditDistance()+"\n";
        output += "Score:\t\t"+(-1)*getEditDistance()+"\n\n";
              
        int currline = Math.min(60, align[0].length()); 

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
        this.alignment += output + "\n\n";
        
        return scoreMatrix[maxI][maxJ];
      
      } catch (BioException exc) {
        exc.printStackTrace();
      }
      
      return Double.NEGATIVE_INFINITY;
      
    } else throw new BioRuntimeException("The alphabets of the sequences and the substitution matrix have to be equal.");
  }

  
  /** 
    * @param w
    * @param x
    * @param y
    * @param z
    * @return the maximum of four <code>double</code>s.
    */
  private double max(double w, double x, double y, double z)
  {    
    if ((w > x) && (w > y) && (w > z)) return w;
    if ((x > y) && (x > z)) return x;
    if ((y > z)) return y;
    return z;
  }

  
  
  /** This is to perform some tests. The folowing parameters are used for the 
    * alignment: match = -2, replace = 5, insert = 3, delete = 3, gap extension = 0.
    * Only DNA alignments are performed by this method. If you wish to change anything,
    * just write your own class to use this object.
    * 
    * @param args you need to specify the substitution matrix file, a query and a target sequence.
    */
  public static void main(String args[])
  {
    if (args.length < 3) 
      throw new Error("Usage: SmithWaterman <SubstitutionMatrix> <Query-String> <Target-String>");
    try {
      SubstitutionMatrix m = new SubstitutionMatrix(
        (FiniteAlphabet) AlphabetManager.alphabetForName("DNA"), 
        new File(args[0]));
      SmithWaterman sw = new SmithWaterman(-2, 5, 3, 3, 0, m);
      sw.pairwiseAlignment(
        new SimpleSequence(
          new SimpleSymbolList(
              m.getAlphabet().getTokenization("token"), 
              args[1]), "", "", Annotation.EMPTY_ANNOTATION), 
        new SimpleSequence(
          new SimpleSymbolList(m.getAlphabet().getTokenization("token"), 
              args[2]), "", "", Annotation.EMPTY_ANNOTATION));
      System.out.println(sw.alignment);
    } catch (NoSuchElementException exc) {
      exc.printStackTrace();
    } catch (IOException exc) {
      exc.printStackTrace();
    } catch (BioException exc) {
      exc.printStackTrace();
    } catch (Exception exc) {
      exc.printStackTrace();
    }
  }
}
