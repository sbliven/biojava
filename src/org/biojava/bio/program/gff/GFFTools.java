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

package org.biojava.bio.program.gff;

import java.util.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;

/**
 * @author Mark Schreiber
 * @author Matthew Pocock
 * @since 1.2
 */

public class GFFTools {

 /**
  * Reads a <code>GFFEntrySet</code> from a file with no filtering
  * @param fileName the file containing the GFF
  * @throws FileNotFoundException if file is not found
  * @throws ParserException if format is wrong
  * @throws BioException if format is wrong
  * @throws IOException if file reading error occurs
  * @return a <code>GFFEntrySet</code> encapsulating the records read from the file
  */
  public static GFFEntrySet readGFF(String fileName)
    throws FileNotFoundException, ParserException, BioException, IOException
  {
    return readGFF(fileName, GFFRecordFilter.ACCEPT_ALL);
  }

  /**
   * Reads a GFFEntrySet from a file with the specified filter
   * @param fileName the file containing the GFF
   * @param recFilt the filter to use
   * @throws FileNotFoundException if file is not found
   * @throws ParserException if format is wrong
   * @throws BioException if format is wrong
   * @throws IOException if file reading error occurs
   * @return a <code>GFFEntrySet</code> encapsulating the records read from the file
   */
  public static GFFEntrySet readGFF(String fileName, GFFRecordFilter recFilt)
    throws FileNotFoundException, ParserException, BioException, IOException
  {
    GFFEntrySet gffEntries = new GFFEntrySet();
    GFFFilterer filterer = new GFFFilterer(gffEntries.getAddHandler(),recFilt);
    GFFParser parser = new GFFParser();
    parser.parse(new BufferedReader(new FileReader(fileName)),filterer);
    return gffEntries;
  }

  public static GFFEntrySet readGFF(BufferedReader gffIn)
    throws ParserException, BioException, IOException
  {
    return readGFF(gffIn, GFFRecordFilter.ACCEPT_ALL);
  }

  public static GFFEntrySet readGFF(BufferedReader gffIn, GFFRecordFilter recFilt)
    throws ParserException, BioException, IOException
  {
    GFFEntrySet gffEntries = new GFFEntrySet();
    GFFFilterer filterer = new GFFFilterer(gffEntries.getAddHandler(),recFilt);
    GFFParser parser = new GFFParser();
    parser.parse(gffIn, filterer);
    return gffEntries;
  }

  /**
   * Writes a GFFEntrySet to a file
   * @param fileName the file to write to
   * @param ents the entries to write
   * @throws IOException if file writing fails
   */
  public static void writeGFF(String fileName, GFFEntrySet ents)
    throws IOException
  {
    GFFWriter writer = new GFFWriter(new PrintWriter(new FileWriter(fileName)));
    ents.streamRecords(writer);
  }

  /**
   * Annotates a sequence with the features from a GFF entry set
   * @param seq the <code>Sequence</code> to annotate.
   * @param ents the the GFF features to annotate it with.
   * @return a reference to a newly annotated sequence.
   */
  public static Sequence annotateSequence(Sequence seq, GFFEntrySet ents){
    Sequence annotated;
    try {
      annotated = ents.getAnnotator().annotate(seq);
    }
    catch (ChangeVetoException ex) {
      throw new BioError(ex,"Assertion Error: Unable to annotate sequence");
    }catch (BioException ex) {
      throw new BioError(ex,"Assertion Error: Unable to annotate sequence");
    }
    return annotated;
  }

  public static SequenceDB annotateSequences(SequenceDB seqs, GFFEntrySet ents)
    throws IllegalIDException, BioException{
    Set names = new HashSet();

    //get the list of names for each sequence
    for (Iterator i = ents.lineIterator(); i.hasNext(); ) {
      GFFRecord record = (GFFRecord)i.next();
      if(! names.contains(record.getSeqName())){
        names.add(record.getSeqName());
      }
    }

    //filter entry set into subsets with same names, use that subset to annotate
    //the correct sequence.
    for (Iterator i = names.iterator(); i.hasNext(); ) {
      final String name = (String)i.next();
      GFFRecordFilter filt = new GFFRecordFilter(){
        public boolean accept(GFFRecord rec){
          return rec.getSeqName().equals(name);
        }
      };

      GFFEntrySet filtered = ents.filter(filt);
      Sequence seq = seqs.getSequence(name);
      seq = GFFTools.annotateSequence(seq, filtered);
    }

    return seqs;
  }
}
