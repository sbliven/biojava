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
 
package org.biojava.bio.program.ragbag;
 
import java.io.*;
import java.util.*;
 
import org.xml.sax.*;
import org.biojava.utils.stax.*;
import org.apache.xerces.parsers.*;
 
import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.game.*;
import org.biojava.utils.*;
 
/**
 * object that instantiates a sequence in Ragbag.
 * It accepts a single sequence file and any number
 * of feature files that are to be applied to that
 * sequence.
 *
 * @author David Huen
 * @since 1.8
 */
public class RagbagSequence extends AbstractSequence
{
  // class variables
  private SequenceBuilder builder;  // this is the SeqIOListener that will build the sequence
  private boolean haveSequence = false; // prevents attempting to input more than one sequence.

  public RagbagSequence()
  {
    // create SequenceBuilder
    builder = new SimpleSequenceBuilder();
  }

/**
 * Processes a file that contains features only.
 * It may be called repeatedly.
 * <p>
 * it should be noted that the file must NOT create any features that
 * don't have a location or the makeSequence() will barf.  This happens
 * with GAME on files that don't have a &lt;seq&gt; element with sequence
 * info.
   </p>
 * @param filename name of file to be processed (currently GAME format only).
 */
  public void addFeatureFile(File thisFile)
    throws BioException
  {
    // ensure it does exist and is a file
    if ((!thisFile.exists())
           || !thisFile.isFile()) throw new BioException("RagbagSequence: can't use the specified file");

    // set up GAME handler
    final GAMEHandler handler = new GAMEHandler();
 
    // create SAX parser for job
    SAXParser parser = new SAXParser();
 
    // link it all together
    handler.setFeatureListener(builder);
    parser.setContentHandler(new SAX2StAXAdaptor(handler));

    // parse sequence file, sending events to the listener.
    try {
      InputSource is = new InputSource(new FileReader(thisFile));
      parser.parse(is);
    }
    catch (SAXException se) {
      throw new BioException(se);
    }
    catch (IOException io) {
      throw new BioException(io);
    }

    // at this stage, the GAMEHandler and SAX parser will go out of scope 
    // and be destroyed in eternal digital oblivion.
  }

  public void addFeatureFile(String filename)
    throws BioException
  {
    // create File object
    File thisFile = new File(filename);
 
    // call actual grunt method
    addFeatureFile(thisFile);
  }

/**
 * Processes a file that contains sequence and (optionally) features.
 * Must be called ONCE only.
 * <p>
 * @param filename name of file to be processed (currently GAME format only).
 */
  public void addSequenceFile(File thisFile)
    throws BioException
  {
    // ragbag opens a file for the purpose of instantiating a Sequence
    // object.
    if (haveSequence) throw new BioException("RagbagSequence: addSequenceFile called twice!");
 
    // use common parsing code.
    addFeatureFile(thisFile);

    haveSequence = true;
  }

  public void addSequenceFile(String filename)
    throws BioException
  {
    // ragbag opens a file for the purpose of instantiating a Sequence
    // object.
 
    // create File object
    File thisFile = new File(filename);
 
    // use common parsing code.
    addSequenceFile(thisFile);
  }

  public void makeSequence()
    throws BioException
  {
    if (!haveSequence) throw new BioException("RagbagSequence: no sequence to instantiate!");

    // create sequence object
    sequence = builder.makeSequence();
  }
}

