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

/**
 * @author Mark Schreiber
 * @since 1.2
 */

public class GFFTools {

 /**
  * Reads a GFFEntrySet from a file with no filtering
  */
  public static GFFEntrySet readGFF(String fileName)
    throws FileNotFoundException, ParserException, BioException, IOException
  {
    return readGFF(fileName, GFFRecordFilter.ACCEPT_ALL);
  }

  /**
   * Reads a GFFEntrySet from a file with the specified filter
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

  /**
   * Writes a GFFEntrySet to a file
   */
  public static void writeGFF(String fileName, GFFEntrySet ents)
    throws IOException
  {
    GFFWriter writer = new GFFWriter(new PrintWriter(new FileWriter(fileName)));
    for(Iterator line = ents.lineIterator(); line.hasNext();){
      writer.recordLine((GFFRecord)line.next());
    }
    writer.endDocument();
    }
}