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