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
 
import java.io.*;
import java.util.*;
 
import org.xml.sax.*;
import org.biojava.utils.stax.*;
import org.apache.xerces.parsers.*;
 
import org.biojava.bio.Annotation;
import org.biojava.bio.SimpleAnnotation;
import org.biojava.bio.BioException;
import org.biojava.bio.BioError;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.io.game.*;
import org.biojava.utils.*;

/**
 * Builds a SimpleAssembly from sequence files
 */
class RagbagVirtualSequenceBuilder
{
//  Sequence sequence =null;
  private SimpleAssemblyBuilder sab = null;
  private RagbagMap map = null;
  private String seqName;
  private RagbagSequenceFactory factory;
  private RagbagComponentDirectory compDir;
/**
 * create a virtual sequence with mappings specified
 * @param seqName name for virtual sequence
 * @param uri     URI for virtual sequence
 * @param mapFile file containing mappings to be used
 */
  protected RagbagVirtualSequenceBuilder(
              String seqName, 
              String uri, 
              File mapFile, 
              RagbagSequenceFactory factory, 
              RagbagComponentDirectory compDir)
    throws BioException, SAXException
  {
    this.seqName = seqName;
    this.factory = factory;
    this.compDir = compDir;

    // create mapping object
    map = new RagbagMap(mapFile);

    try {
      map.parse();
    }
    catch (FileNotFoundException fne) {
      throw new BioException("map file " + mapFile.getName() + " does not exist");
    }
    catch (IOException fne) {
      throw new BioException("IO Exception on file " + mapFile.getName());
    }
    
    // create SimpleAssembly
    System.out.println("creating SimpleAssembly");
//    sequence = new SimpleAssembly(map.getDstLength(), seqName, uri);
    sab = new SimpleAssemblyBuilder();
    sab.setName(seqName);
    sab.setURI(uri);
  }

  public void addSequence(File seqfile)
    throws BioException, ChangeVetoException, IOException, SAXException
  {
    // look up this file in the mappings
    Enumeration mapEntries = map.getEnumeration(seqfile.getName());

    // no entries? Report incident and go on.
    if (!mapEntries.hasMoreElements()) {
      System.err.println("RagbagVirtualSequenceBuilder.addSequence: no mapping for file "
                     + seqfile.getName());
      return;
    }

    Sequence currSequence = null;

    // the sequence may either be in a sequence file or a virtual sequence directory
    if (seqfile.isFile()) {
      // sequence is in a file
      currSequence = factory.getSequenceObject();
      ((RagbagSequence) currSequence).addSequenceFile(seqfile);
      ((RagbagSequence) currSequence).makeSequence();
    }
    else if (seqfile.isDirectory()) {
      // it's a virtual sequence in a represented by a directory
      try {
        currSequence = new RagbagAssembly(seqfile, factory, compDir);
      }
      catch (FileNotFoundException fne) {
        throw new BioException("Directory " + seqfile.getName() + "could not be found.");
      }
    }
    else 
      // hmmm... what could this be then?????
      throw new BioError(seqfile.getName() + " is not a file or directory!");

    // there may be multiple mappings of different parts of this sequence
    // to the SimpleAssembly, apply all of them.
    while(mapEntries.hasMoreElements()) {

      // create componentFeature for each mapping
      RagbagMap.MapElement mapping = (RagbagMap.MapElement) mapEntries.nextElement();
 
      // create ComponentFeature.Template and add to assembly
      ComponentFeature.Template cft = new ComponentFeature.Template();
        cft.annotation = new SimpleAnnotation();
        cft.location = mapping.getDstLocation();
        cft.source = "";
        cft.type = "Component";
 
        cft.strand = mapping.getStrand();
 
        cft.componentLocation = mapping.getSrcLocation();
        cft.componentSequence = currSequence;
 
      // and add it to SimpleAssembly
      System.out.println("Adding features on " + seqfile.getName() + cft.componentLocation);
      try {
        ComponentFeature cf = sab.addComponentSequence(cft);

        // should the feature be logged?
        if (compDir != RagbagComponentDirectory.UNLOGGED) {
          compDir.addComponentFeature(mapping.getRef(), cf);
        }
      }
      catch (ChangeVetoException cve) {
        System.err.println("Can't add feature: change vetoed.");
      }
    }            
  }

  public void addFeatures(File featureFile)
    throws BioException
  {
    // ensure it does exist and is a file
    if ((!featureFile.exists())
           || !featureFile.isFile()) throw new BioException("RagbagSequence: can't use the specified file");
 
    // set up GAME handler
    final GAMEHandler handler = new GAMEHandler();
 
    // create SAX parser for job
    SAXParser parser = new SAXParser();
 
    // link it all together
    handler.setFeatureListener(sab);
    parser.setContentHandler(new SAX2StAXAdaptor(handler));
 
    // parse sequence file, sending events to the listener.
    try {
      InputSource is = new InputSource(new FileReader(featureFile));
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

  public Sequence makeSequence()
  {
    return sab.makeSequence();
  }
}

