
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
 
import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.BioError;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.io.game.*;
import org.biojava.utils.*;
import org.biojava.utils.cache.*;


/**
 * A version of RagbagSequence that exhibits lazy instantiation
 * and caching behaviour.
 * It should reduce memory requirements when creating large
 * assemblies.
 * <p>
 * It functionally proxies for RagbagSequence.
 *
 * @author David Huen
 * @author Matthew Pocock
 */
class RagbagCachedSequence implements RagbagSequenceItf
{
  private Cache cache;

  // these are the names of files associated with the RagbagSequence
  protected String seqFilename = null;
  protected List annotFilenames = null;

  private String name;
  private String urn;

  private CacheReference cachedSequence;
  private boolean gotSequenceFile = false;

/**
 * @param cache object that controls cache behaviour.
 */
  public RagbagCachedSequence(String name, String urn, Cache cache)
  {
//    System.out.println("RagbagCachedSequence: constructor entered");
    this.cache = cache;

    this.name = name;
    this.urn = urn;
    System.out.println("RagbagCachedSequence constructor: " + name + " " + urn);
  }

  public void addFeatureFile(File thisFile)
    throws BioException
  {
//    System.out.println("RagbagCachedSequence: addfeatureFile entered");

    // verify that the file exists
    if (!thisFile.exists() || !thisFile.isFile())
      throw new BioException("RagbagCachedSequence: not valid file!");

    // create annotation file list if necessary
    if (annotFilenames == null) {
      annotFilenames = new Vector();
    }

    // get retrieve full pathname for later
    try {
     annotFilenames.add(thisFile.getCanonicalPath());
    }
    catch (IOException ioe) {
      throw new BioException(ioe);
    }
  }


  public void addFeatureFile(String filename)
    throws BioException
  {
    // create file object to retrieve full pathname
    File thisFile = new File(filename);

    // add to list
    addFeatureFile(thisFile);
  }

  public void addSequenceFile(File thisFile)
    throws BioException
  {
//    System.out.println("RagbagCachedSequence: addSequenceFile entered");

    // verify that the file exists
    if (!thisFile.exists() || !thisFile.isFile())
      throw new BioException("RagbagCachedSequence: not valid file!");

    // cache file name for instantiation later
    try {
      seqFilename = thisFile.getCanonicalPath();
    }
    catch (IOException ioe) {
      throw new BioException(ioe);
    }

    // prevalidation passed
    gotSequenceFile = true;
  }

  public void addSequenceFile(String filename)
    throws BioException
  {
    // create file object to retrieve full pathname
    File thisFile = new File(filename);

    addSequenceFile(thisFile);
  }

  public void makeSequence()
    throws BioException
  {
    // with lazy instantiation, there's little to do but validate
    if (!gotSequenceFile)
      throw new BioException("RagbagCachedSequence: no sequence file defined");
  }

/**
 * ensures that a concrete Sequence object is available for use
 */
  private Sequence instantiateSequence()
//    throws BioException
  {
//    System.out.println("RagbagCachedSequence: instantiateSequence entered");

    RagbagSequenceItf seq;

    // check if we have a CacheReference yet
    if (cachedSequence != null) {
      // got cache but is it still there?
      seq = (RagbagSequenceItf) cachedSequence.get();

      // if it is, return it.
      if (seq != null) return seq;
    }

    // check prevalidation
    if (!gotSequenceFile) 
      throw new BioError("RagbagCachedSequence: no sequence file defined");

    // looks like we'll have to recreate the sequence object
    try {
      seq = new RagbagSequence(name, urn);

      seq.addSequenceFile(new File(seqFilename));

      // now add any features if necessary
      if (annotFilenames != null) {
        // get iterator
        Iterator ai = annotFilenames.iterator();

        // add features
        while (ai.hasNext()) {
          // create file
          File currAnnotFile = new File((String) ai.next());

          // add its features
          seq.addFeatureFile(currAnnotFile);
        }
      }

      // create sequence
      seq.makeSequence(); 
    }
    catch (BioException be) {
      throw new BioError(be);
    }

    // update cache reference
    cachedSequence = cache.makeReference(seq);

    return seq;
  }

  public String getName()
  {
    return name;
  }
  public String getURN() 
  {
    return urn;
  }
 
  public void edit(Edit edit)
     throws IllegalAlphabetException, ChangeVetoException
  {
    Sequence tempSeq = instantiateSequence();
    tempSeq.edit(edit);
  }
  public Alphabet getAlphabet() 
  {
    Sequence tempSeq = instantiateSequence();
    return tempSeq.getAlphabet();
  }
  public Iterator iterator() 
  {
    Sequence tempSeq = instantiateSequence();
    return tempSeq.iterator();
  }
  public int length() 
  {
    Sequence tempSeq = instantiateSequence();
    return tempSeq.length();
  }
  public String seqString() 
  {
    Sequence tempSeq = instantiateSequence();
    return tempSeq.seqString();
  }
  public SymbolList subList(int start, int end) 
  {
    Sequence tempSeq = instantiateSequence();
    return tempSeq.subList(start, end);
  }
  public String subStr(int start, int end) 
  {
    Sequence tempSeq = instantiateSequence();
    return tempSeq.subStr(start, end);
  }
  public Symbol symbolAt(int index) 
  {
    Sequence tempSeq = instantiateSequence();
    return tempSeq.symbolAt(index);
  }
  public List toList() 
  {
    Sequence tempSeq = instantiateSequence();
    return tempSeq.toList();
  }
 
  public void addChangeListener(ChangeListener cl) 
  {
    Sequence tempSeq = instantiateSequence();
    tempSeq.addChangeListener(cl);
  }
  public void addChangeListener(ChangeListener cl, ChangeType ct) 
  {
    Sequence tempSeq = instantiateSequence();
    tempSeq.addChangeListener(cl, ct);
  }
  public void removeChangeListener(ChangeListener cl) 
  {
    Sequence tempSeq = instantiateSequence();
    tempSeq.addChangeListener(cl);
  }
  public void removeChangeListener(ChangeListener cl, ChangeType ct) 
  {
    Sequence tempSeq = instantiateSequence();
    tempSeq.addChangeListener(cl, ct);
  }
  public boolean isUnchanging(ChangeType ct) {
    Sequence tempSeq = instantiateSequence();
    return tempSeq.isUnchanging(ct);
}
 
  public boolean containsFeature(Feature f) 
  {
    Sequence tempSeq = instantiateSequence();
    return tempSeq.containsFeature(f);
  }
  public int countFeatures() 
  {
    Sequence tempSeq = instantiateSequence();
    return tempSeq.countFeatures();
  }
  public Feature createFeature(Feature.Template ft) 
    throws BioException, ChangeVetoException
  {
    Sequence tempSeq = instantiateSequence();
    return tempSeq.createFeature(ft);
  }
  public Iterator features() 
  {
    Sequence tempSeq = instantiateSequence();
    return tempSeq.features();
  }
  public FeatureHolder filter(FeatureFilter ff, boolean recurse) 
  {
    Sequence tempSeq = instantiateSequence();
    return tempSeq.filter(ff, recurse);
  }
  public void removeFeature(Feature f) 
    throws ChangeVetoException 
  {
    Sequence tempSeq = instantiateSequence();
    tempSeq.removeFeature(f);
  }
 
  public Annotation getAnnotation() 
  {
    Sequence tempSeq = instantiateSequence();
    return tempSeq.getAnnotation();
  }
}

