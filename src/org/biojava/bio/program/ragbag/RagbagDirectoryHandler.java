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
import org.biojava.bio.SimpleAnnotation;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.game.*;
import org.biojava.utils.*;
 
/**
 * object that instantiates a sequence when given
 * a Ragbag directory.
 *
 * @author David Huen
 * @since 1.8
 */
public class RagbagDirectoryHandler extends AbstractSequence
{
  // there are two possibilities here.
  // We could be given a directory with a Map file in
  // it, in which case we should create a virtual
  // sequence, ie. SimpleAssembly and map everything we
  // are given onto it accordint to the mapping in Map.
  // Alternatively, there may be no map file. In this
  // case, we expect to have a SINGLE sequence file
  // in the directory and a folder marked Annotation
  // which contains files with features ONLY.  These
  // are mapped onto the sequence file.
/*
  public RagbagDirectoryHandler(String dirname) 
  {
    // create File reference for this directory
    File thisDir = new File(dirname);

    // call other constructor
    RagbagDirectoryHandler(thisDir);
  }
*/
  public RagbagDirectoryHandler(File thisDir)
    throws BioException, FileNotFoundException, ChangeVetoException, IOException, SAXException
  {
    // check it is a directory
    if (!thisDir.exists() ||
        !thisDir.isDirectory()) {
      throw new BioException("RagbagDirectoryHandler: the reference is not to a directory.");
    }

    // check if there is a file called Map within it
    File mapFile = new File(thisDir, "Map");
    if (mapFile.exists()) {
      // ************ Construct virtual sequence ****************
      // virtual contig required
      RagbagVirtualSequenceBuilder seqBuilder = 
        new RagbagVirtualSequenceBuilder(thisDir.getName(), "", mapFile);


      // search thru' files and instantiate them
      // if it's a file, it must be a sequence file
      // if it's a directory and NOT Annotation/ it should be a virtual sequence
      File dirList[] = thisDir.listFiles();

      for (int i=0; i < dirList.length; i++) {
        File currFile = dirList[i];
        //is it a file?
        if (!(currFile.getName().equals("Map"))
             && !(currFile.getName().equals("Annotation")) ) {
          // this is not the Map file or the Annotation directory.
          // add sequence to contig
          seqBuilder.addSequence(currFile);

        }
      }

      // ************ Handle Annotations on Virtual Sequence ***************  
      // if there is an Annotation/, map its features onto the SimpleAssembly
      File annotDir = new File(thisDir, "Annotation");
      if (annotDir.exists() && annotDir.isDirectory()
          && annotDir.getName().equals("Annotation"))
      {
        // load annotations
        File annotDirList[] = annotDir.listFiles();

        for (int i=0; i < annotDirList.length; i++) {
          // add all files.
          if (annotDirList[i].isFile()) {
//            mySequence.addFeatures(annotDirList[i]);
            System.out.println("adding annotations onto a virtual sequence has not been implemented yet.");
          }
        }
      }

      // retrieve the new sequence
      System.out.println("return built virtual sequence");
      sequence = seqBuilder.makeSequence();

    } // end of block for case where Map exists.
    else {
      // ************ build concrete sequence *****************
      // no Map file.
      // looks like a concrete sequence is to be instantiated
      // check that there is only ONE sequence file in the directory.
      // get directory contents
      File dirList[] = thisDir.listFiles();

      // check directory is not empty
      if ((dirList == null) || (dirList.length == 0))
        throw new BioException("RagbagDirectoryHandler: directory is empty");

      // look for sequence file
      File seqFile = null;
      int seqFileCount = 0;
      boolean hasAnnotation = false;
      File annotDir = null;

      for (int i=0; i < dirList.length; i++) {
        // all files are assumed to be sequence files
//        System.out.println(dirList[i].getName());
        if (dirList[i].isFile()) {
          seqFileCount++;
          seqFile = dirList[i];
        }
        // is this the Annotation directory
        else if (dirList[i].isDirectory() && (dirList[i].getName().equals("Annotation"))) {
//          System.out.println("found Annotation/");
          annotDir = dirList[i];
          hasAnnotation = true;
        }
      }

      if (seqFileCount == 0 || seqFileCount > 1)
        throw new BioException(
        "RagbagDirectory: an unmapped directory MUST have ONE sequence file.");

      // load sequence file
      RagbagSequence currSequence = new RagbagSequence();
      currSequence.addSequenceFile(seqFile);

      // load annotations
      File annotDirList[] = annotDir.listFiles();

      for (int i=0; i < annotDirList.length; i++) {
        // add all files.
        if (annotDirList[i].isFile())
          currSequence.addFeatureFile(annotDirList[i]);
      }

      // instantiate sequence
      currSequence.makeSequence();
      sequence = currSequence;
    } // end else for no Map case
  }

}
