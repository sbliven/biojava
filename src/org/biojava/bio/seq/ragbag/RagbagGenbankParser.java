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
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.io.GenbankProcessor;
import org.biojava.bio.seq.io.GenbankFormat;
import org.biojava.bio.seq.io.SequenceBuilder;
import org.biojava.bio.seq.io.SeqIOListener;
import org.biojava.bio.seq.io.SequenceFormat;
import org.biojava.bio.seq.io.SymbolParser;

import org.xml.sax.*;
import org.biojava.utils.stax.*;
import org.apache.xerces.parsers.*;

/**
 * Ragbag FileParser class for handling Genbank formatted files.
 * <p>
 * Extensively derived from the example from Biojava Bootcamp
 * by Thomas Down.
 * <p>
 * @author David Huen
 * @author Thomas Down
 */
class RagbagGenbankParser implements RagbagFileParser
{

  // set up factory method
/**
 * Factory for creating new instances of RagbagGenbankParser.
 */
  public static final RagbagFileParserFactory FACTORY
    = new RagbagFileParserFactory() {
    public RagbagFileParser getParser(File inputFile) {
      return new RagbagGenbankParser(inputFile);
    }
  };

/**
 * File extension characteristic of the format.
 */
  public static final String EXTENSION = "gb";

  private SequenceBuilder builder;
  private File inputFile;

/**
 * @param inputFile file from which sequence/features are to be read.
 */
  public RagbagGenbankParser(File inputFile)
  {
    // cache the file reference before it vanishes!
    this.inputFile = inputFile;
  }

  public void setListener(SequenceBuilder builder)
  {
    this.builder = builder;
  }

  public void parse()
    throws BioException
  {
    try {
    // open the file
    BufferedReader file = new BufferedReader(new FileReader(inputFile));
    SequenceFormat parser = new GenbankFormat();
    SymbolParser dnaParser = DNATools.getDNA().getParser("token");

    // set up listener chain
    SeqIOListener chain = new GenbankProcessor(builder);

    // parse it
    parser.readSequence(file, dnaParser, chain);
    }
    catch (FileNotFoundException fne) {
      throw new BioException(fne);
    }
    catch (IOException ie) {
      throw new BioException(ie);
    }
  }
}
