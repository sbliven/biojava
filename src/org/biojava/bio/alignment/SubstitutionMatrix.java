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

/*
 * Created on 01.08.2005
 *
 */
package org.biojava.bio.alignment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.Symbol;

/** <p>This object is able to read a substitution matrix file and constructs an int matrix to
  * store the matrix. Every single element of the matrix can be accessed by the method
  * <code>getValueAt</code> with the parameters being tow biojava symbols. This is why it is
  * not necessary to access the matrix directly. If there is no value for the two specified
  * <code>Symbol</code>s an <code>Exception</code> is thrown.</p><p>
  * Substitution matrix files, are available at <a href="ftp://ftp.ncbi.nlm.nih.gov/blast/matrices/">
  * The NCBI FTP directory</a>.</p>
  *
  * @author Andreas Dr&auml;ger
  */
public class SubstitutionMatrix
{
  protected Map rowSymbols, colSymbols;
  protected int[][] matrix;
  protected int min, max;
  protected FiniteAlphabet alphabet;
  protected String description, name;
  
  /** This constructs a <code>SubstitutionMatrix</code>-object that contains two 
    * <code>Map</code> data structures having biojava symbols as key and the value
    * being the index of the matrix containing the substitution score.
    * 
    * @param alpha the alphabet of the matrix (DNA, RNA or PROTEIN, or PROTEIN-TERM)
    * @param matrixFile the file containing the substitution matrix. Lines starting with
    *   '<code>#</code>' are comments. The line starting with a white space, is the table head. 
    *   Every line has to start with the one letter representation of the Symbol and then 
    *   the values for the exchange.
    * 
    * @throws IOException
    * @throws BioException
    */
  public SubstitutionMatrix(FiniteAlphabet alpha, File matrixFile) throws IOException, BioException
  {
    this.alphabet     = alpha;
    this.description  = "";
    this.name         = matrixFile.getName();
    this.rowSymbols   = new HashMap();
    this.colSymbols   = new HashMap();
    String matString  = "";
    BufferedReader br = new BufferedReader(new FileReader(matrixFile));
    while(br.ready()) 
      matString      += br.readLine() + "\n";
    this.matrix       = this.parseMatrix(matString);
    //this.printMatrix();
  }
  
  
  /** With this constructor it is possible to construct a SubstitutionMatrix object from a
    * substitution matrix file. The given String contains a number of lines separated by 
    * <code>\n</code>. Everything else is the same than for the constructor above.
    * 
    * @param alpha The <code>FiniteAlphabet</code> to use
    * @param matrixString
    * @param name of the matrix.
    * @throws BioException
    */
  public SubstitutionMatrix(FiniteAlphabet alpha, String matrixString, String name) throws BioException
  {
    this.alphabet    = alpha;
    this.description = "";
    this.name        = name;
    this.rowSymbols  = new HashMap();
    this.colSymbols  = new HashMap();
    this.matrix = this.parseMatrix(matrixString);   
    //this.printMatrix();
  }
  
  
  /** Constructs a SubstitutionMatrix with every Match and every Replace having the same
    * expenses given by the parameters. Ambigious symbols are not considered because there
    * might be to many of them (for proteins).
    *  
    * @param alpha
    * @param match
    * @param replace
    */
  public SubstitutionMatrix(FiniteAlphabet alpha, int match, int replace)
  {
    int i=0, j=0;
    
    this.alphabet = alpha;
    this.description = "Identity matrix. All replaces and all matches are treated equally.";
    this.name = "IDENTITY_"+match+"_"+replace;
    this.rowSymbols = new HashMap();
    this.colSymbols = new HashMap();
    this.matrix = new int[alpha.size()][alpha.size()];
    
    Symbol[] sym = new Symbol[alpha.size()];
    Iterator iter = alpha.iterator();

    for (i=0; iter.hasNext(); i++) {
      sym[i] = (Symbol) iter.next();
      rowSymbols.put(sym[i], new Integer(i));
      colSymbols.put(sym[i], new Integer(i));
    }   
    
    for(i=0; i<alphabet.size(); i++)
      for(j=0; j<alphabet.size(); j++) 
        if (sym[i].getMatches().contains(sym[j])) matrix[i][j] = match;
        else matrix[i][j] = replace;
    
    //this.printMatrix();
  }
  
  
  /** Reads a String representing the contents of a substitution matrix file.
    *  
    * @param matString
    * @return matrix
    * @throws BioException
    */
  protected int[][] parseMatrix(String matString) throws BioException
  {
    int j = 0, rows = 0, cols = 0;
    StringTokenizer br, st;
    SymbolTokenization symtok = alphabet.getTokenization("token");

    this.min = Integer.MAX_VALUE;
    this.max = Integer.MIN_VALUE;
    /* First: count how many elements are in the matrix
     * fill lines and rows
     */
    br = new StringTokenizer(matString, "\n");
    while (br.hasMoreElements()) {
      String line = br.nextElement().toString();
      if (line.startsWith("#")) {
        description += line.substring(1);
        continue;
      } else if (line.startsWith(" ")) {
        st = new StringTokenizer(line, " ");
        for (j=0; st.hasMoreElements(); j++) { 
          colSymbols.put(symtok.parseToken(st.nextElement().toString()), new Integer(j));
        }
        cols = j;
      } else if (!line.startsWith("\n")) { // the matrix.
        st = new StringTokenizer(line, " ");
        if (st.hasMoreElements()) 
          rowSymbols.put(symtok.parseToken(st.nextElement().toString()), new Integer(rows++));
      }
    }
    
    int[][] matrix = new int[rows][cols];
    
    rows = 0;
    br = new StringTokenizer(matString, "\n");
    /* Second reading. Fill the matrix.
     */
    while (br.hasMoreElements())
    {
      String line = br.nextElement().toString();
      if      (line.startsWith("#")) continue;
      else if (line.startsWith(" ")) continue;
      else if (!line.startsWith("\n")) { // lines:
        st = new StringTokenizer(line, " ");
        if (st.hasMoreElements()) st.nextElement(); // throw away Symbol at beginning.
        for (j=0; st.hasMoreElements(); j++) {// cols:
          matrix[rows][j] = Integer.parseInt(st.nextElement().toString());
          if (matrix[rows][j] > max) max = matrix[rows][j]; // Maximum.
          if (matrix[rows][j] < min) min = matrix[rows][j]; // Minimum.
        }
        rows++;
      }
    }
    
    return matrix;
  }
  
  
  /** There are some substitution matrices containing more columns than lines. This
    * has to do with the ambigious symbols. Lines are always good, columns might not
    * contain the whole information. The matrix is supposed to be symmetric anyway, so
    * you can always set the ambigious symbol to be the first argument.
    * @param row Symbol of the line
    * @param col Symbol of the column
    * @return expenses for the exchange of symbol row and symbol col.
    * @throws BioException
    */
  public int getValueAt(Symbol row, Symbol col) throws BioException
  {
    if ((!rowSymbols.containsKey(row)) || (!colSymbols.containsKey(col)))
      throw new BioException("No entry for the sybols "+row.getName()+" and "+col.getName());
    return matrix[((Integer) rowSymbols.get(row)).intValue()][((Integer) colSymbols.get(col)).intValue()];
  }
  
  
  /** This gives you the description of this matrix if there is one. Normally
    * substitution matrix files like BLOSUM contain some lines of description.
    * @return the comment of the matrix
    */
  public String getDescription()
  {
    return description;
  }
  
  
  /** Every substitution matrix has a name like "BLOSUM30" or "PAM160". This will be returned 
    * by this method.
    * @return the name of the matrix.
    */
  public String getName()
  {
    return name;
  }
  
  
  /** The minimum score of this matrix.
    * @return minimum of the matrix.
    */
  public int getMin()
  {
    return min;
  }
  
  
  /** The maximum score in this matrix.
    * 
    * @return maximum of the matrix.
    */
  public int getMax()
  {
    return max;
  }
  
  
  /** Sets the description to the given value.
    * 
    * @param desc a description. This doesn't have to start with '#'.
    */
  public void setDescription(String desc)
  {
    this.description = desc;
  }
  
  
  /** Gives the alphabet used by this matrix.
    * @return the alphabet of this matrix.
    */
  public FiniteAlphabet getAlphabet()
  {
    return alphabet;
  }
  
  
  /** Creates a <code>String</code> representation of this matrix.
    * @return a string representation of this matrix without the description.
    */
  public String stringnifyMatrix()
  {
    int i = 0;
    String matrixString = "";
    Symbol[] colSyms    = new Symbol[this.colSymbols.keySet().size()];
    
    try {
      SymbolTokenization symtok = alphabet.getTokenization("default");  
      matrixString += "  ";
      Iterator colKeys = colSymbols.keySet().iterator();
      while (colKeys.hasNext()) {
        colSyms[i]    = (Symbol) colKeys.next(); 
        matrixString += symtok.tokenizeSymbol(colSyms[i++]).toUpperCase() + " ";
      }
      matrixString += "\n";
    
      Iterator rowKeys = rowSymbols.keySet().iterator();
      while (rowKeys.hasNext()) {
        Symbol rowSym = (Symbol) rowKeys.next();
        matrixString += symtok.tokenizeSymbol(rowSym).toUpperCase() + " ";
        for (i=0; i<colSyms.length; i++) {
          matrixString += getValueAt(rowSym, colSyms[i]) + " ";
        }
        matrixString += "\n";
      }  
    } catch (BioException exc) {
      exc.printStackTrace();
    }
    
    return matrixString;  
  }
  
  
  /** Stringnifies the description of the matrix.
    * @return Gives a description with approximately 60 letters on every line 
    *   separated by <code>\n</code>. Every line starts with <code>#</code>.
    */
  public String stringnifyDescription()
  {
    String desc = "", line = "# ";
    StringTokenizer st  = new StringTokenizer(description, " ");
    while(st.hasMoreElements()) {
      line += st.nextElement().toString()+" ";
      if (line.length() >= 60) {
        desc += line + "\n";
        if (st.hasMoreElements()) line = "# ";
      } else if (!st.hasMoreElements())
        desc += line + "\n";
    }    
    return desc;
  }
  
  
  /** Overides the inherited method.
    * @return Gives a string representation of the SubstitutionMatrix. 
    *   This is a valid input for the constructor which needs a matrix string. This String
    *   also contains the description of the matrix if there is one.
    */
  public String toString()
  {
    String desc = "", line = "# ";
    StringTokenizer st  = new StringTokenizer(description, " ");
    while(st.hasMoreElements()) {
      line += st.nextElement().toString()+" ";
      if (line.length() >= 60) {
        desc += line + "\n";
        if (st.hasMoreElements()) line = "# ";
      } else if (!st.hasMoreElements())
        desc += line + "\n";
    }    
    
    return desc + stringnifyMatrix();
  }

  
  /** Just to perform some test. It prints the matrix on the screen.
    */
  public void printMatrix()
  {
    // Testausgabe: 
    Iterator rowKeys = rowSymbols.keySet().iterator();
    while(rowKeys.hasNext()) {
      Iterator colKeys = colSymbols.keySet().iterator();
      Symbol rowSym = (Symbol) rowKeys.next();
      System.out.print(rowSym.getName()+"\t");
      while(colKeys.hasNext()) {
        Symbol colSym = (Symbol) colKeys.next();
        int x = ((Integer) rowSymbols.get(rowSym)).intValue();
        int y = ((Integer) colSymbols.get(colSym)).intValue();
        System.out.print(colSym.getName()+" "+" "+x+" "+y+" "+matrix[x][y]+"\t");
      }
      System.out.println("\n");
    }
    System.out.println(toString());
  }

  /** With this method you can get a "normalized" <code>SubstitutionMatrix</code> object;
    * however, since this implementation uses an int matrix, the normalized matrix will
    * be scaled by ten. If you need values between zero and one, you have to devide every
    * value returnd by <code>getValueAt</code> by ten.
    * @return a new and normalized <code>SubstitutionMatrix</code> object given by this
    *   substitution matrix. Because this uses an <code>int</code> matrix, all values are
    *   skaled by 10.
    */
  public SubstitutionMatrix normalizeMatrix()
  {
    try {
      int i, j, min = getMin(), newMax = Integer.MIN_VALUE;
      int[][] mat = new int[matrix.length][matrix[matrix.length-1].length];
      String name = getName()+"_normalized";
      String matString = stringnifyDescription()+"  ";
      FiniteAlphabet alphabet = getAlphabet();
      Map rowMap = this.rowSymbols;
      Map colMap = this.colSymbols;
      SymbolTokenization symtok = alphabet.getTokenization("default");
    
      for (i=0; i<matrix.length; i++) 
        for (j=0; j<matrix[matrix.length-1].length; j++) {
          mat[i][j] = matrix[i][j] - min;
          if (mat[i][j] > newMax) newMax = mat[i][j];
        }
      
      for (i=0; i<mat.length; i++)
        for (j=0; j<mat[mat.length-1].length; j++)
          mat[i][j] = mat[i][j]*10/newMax;
    
      Object[] rows = rowSymbols.keySet().toArray();
      Object[] cols = colSymbols.keySet().toArray();
      for (i=0; i<cols.length; i++) 
        matString += symtok.tokenizeSymbol((Symbol) cols[i])+" ";
      for (i=0; i<rows.length; i++) {
        matString += "\n" + symtok.tokenizeSymbol((Symbol) rows[i]) + " ";
        for (j=0; j<cols.length; j++) {
          matString += mat[((Integer) rowMap.get((Symbol) rows[i])).intValue()][((Integer) colMap.get((Symbol) cols[j])).intValue()] + " ";
        }
      }
      matString += "\n";
      return new SubstitutionMatrix(alphabet, matString, name);
    } catch (BioException exc) {
      exc.printStackTrace();
    }
    
    return null;
  }
  
}
