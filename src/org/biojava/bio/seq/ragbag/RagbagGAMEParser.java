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
import java.io.IOException;

import org.biojava.bio.seq.io.game.GAMEHandler;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.io.SequenceBuilder;

import org.xml.sax.*;
import org.biojava.utils.stax.*;
import javax.xml.parsers.*;

/**
 * Ragbag FileParser class for handling GAME formatted files.
 *
 * @author David Huen
 */
class RagbagGAMEParser implements RagbagFileParser
{
/**
 * Factory for creating new instances of RagbagXFFParser.
 */
  // set up factory method
  public static final RagbagFileParserFactory FACTORY
    = new RagbagFileParserFactory() {
    public RagbagFileParser getParser(File inputFile) {
      return new RagbagGAMEParser(inputFile);
    }
  };

/**
 * File extension characteristic of the format.
 */
  public static final String EXTENSION = "game";

  private SequenceBuilder builder;
  private File inputFile;

  public RagbagGAMEParser(File inputFile)
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
    // set up GAME handler
    final GAMEHandler handler = new GAMEHandler();
 
    try {
	// create SAX parser for job
	SAXParserFactory spf = SAXParserFactory.newInstance();
	spf.setNamespaceAware(true);
	XMLReader parser = spf.newSAXParser().getXMLReader();
	
	// link it all together
	handler.setFeatureListener(builder);
	parser.setContentHandler(new SAX2StAXAdaptor(handler));
	
	// parse sequence file, sending events to the listener.
	
	InputSource is = new InputSource(new FileReader(inputFile));
	parser.parse(is);
    } catch (SAXException se) {
	throw new BioException(se);
    } catch (IOException io) {
	throw new BioException(io);
    } catch (ParserConfigurationException ex) {
	throw new BioException(ex);
    }
  }
}
