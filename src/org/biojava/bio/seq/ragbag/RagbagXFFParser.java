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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.biojava.bio.BioException;
import org.biojava.bio.program.xff.XFFFeatureSetHandler;
import org.biojava.bio.seq.io.SequenceBuilder;
import org.biojava.utils.stax.DelegationManager;
import org.biojava.utils.stax.SAX2StAXAdaptor;
import org.biojava.utils.stax.StAXContentHandlerBase;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Ragbag FileParser class for handling XFF formatted files.
 * <p>
 * Extensively derived from the xff demo (Thomas Down?)
 *
 * @author David Huen
 */
class RagbagXFFParser implements RagbagFileParser
{

/**
 * Factory for creating new instances of RagbagXFFParser.
 */
  public static final RagbagFileParserFactory FACTORY
    = new RagbagFileParserFactory() {
    public RagbagFileParser getParser(File inputFile) {
      return new RagbagGAMEParser(inputFile);
    }
  };

/**
 * File extension characteristic of the format.
 */
  public static final String EXTENSION = "xff";


  private SequenceBuilder builder;
  private File inputFile;

/**
 * @param inputFile file from which sequence/features are to be read.
 */
  public RagbagXFFParser(File inputFile)
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
    // set up XFF handler
    final XFFFeatureSetHandler xffhandler = new XFFFeatureSetHandler();
 
    try {
	// create SAX parser for job
	SAXParserFactory spf = SAXParserFactory.newInstance();
	spf.setNamespaceAware(true);
	XMLReader parser = spf.newSAXParser().getXMLReader();
 
	// link it all together
	xffhandler.setFeatureListener(builder);
	//    parser.setContentHandler(new SAX2StAXAdaptor(handler));
	
	parser.setContentHandler(new SAX2StAXAdaptor(new StAXContentHandlerBase() {
                public void startElement(String nsURI,
                                         String localName,
                                         String qName,
                                         Attributes attrs,
                                         DelegationManager dm)
                    throws SAXException
                {
                    if (localName.equals("featureSet")) {
                        dm.delegate(xffhandler);
                    }
                }
            }));

    // parse sequence file, sending events to the listener.
      InputSource is = new InputSource(new FileReader(inputFile));
      parser.parse(is);
    }
    catch (SAXException se) {
      throw new BioException(se);
    }
    catch (IOException io) {
      throw new BioException(io);
    }catch (ParserConfigurationException ex) {
	throw new BioException(ex);
    }
  }
}
