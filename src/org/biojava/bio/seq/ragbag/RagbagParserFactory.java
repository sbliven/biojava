/**
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
 
package org.biojava.bio.seq.ragbag;

import java.io.File;
import java.lang.String;
import java.util.Map;
import java.util.HashMap;

import org.biojava.bio.BioException;

/**
 * Returns an instance of the RagbagFileParser specific for an input file.
 *
 * @author David Huen
 */
class RagbagParserFactory
{
  static Map parserRegistry;
  static RagbagParserFactory FACTORY = new RagbagParserFactory();

  {
    parserRegistry = new HashMap();

    // go register a few types
    // there must be a better way!
    registerParser(RagbagGAMEParser.EXTENSION, RagbagGAMEParser.FACTORY);
    registerParser(RagbagEmblParser.EXTENSION, RagbagEmblParser.FACTORY);
    registerParser(RagbagGenbankParser.EXTENSION, RagbagGenbankParser.FACTORY);
    registerParser(RagbagXFFParser.EXTENSION, RagbagXFFParser.FACTORY);
  }

  public static void registerParser(String extension, RagbagFileParserFactory parserFactory)
  {
    parserRegistry.put(extension, parserFactory);
  }

  private String getExtension(File inputFile)
  {
    // find last "."
    String name = inputFile.getName();
    return name.substring(name.lastIndexOf(".") + 1);
  }

  public RagbagFileParser getParser(File inputFile)
    throws BioException
  {
    // invoke correct parser factory
    String currExt = getExtension(inputFile);

    if (parserRegistry.containsKey(currExt)) {
      return ((RagbagFileParserFactory) parserRegistry.get(currExt)).getParser(inputFile);
    }
    else 
      throw new BioException("no parser found for file " + inputFile.getName());
  }
}
