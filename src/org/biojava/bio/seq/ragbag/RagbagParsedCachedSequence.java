
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
import java.util.Iterator;

import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.utils.cache.Cache;


/**
 * A version of RagbagSequence that exhibits lazy instantiation
 * and caching behaviour.
 * It should reduce memory requirements when creating large
 * assemblies.
 * <p>
 * It functionally proxies for RagbagSequence.
 *
 * @author David Huen
 * @author Thomas Down
 */
class RagbagParsedCachedSequence extends RagbagCachedSequence
{
  private RagbagFilterFactory filterFactory;

/**
 * @param cache object that controls cache behaviour.
 */
  public RagbagParsedCachedSequence(String name, String urn, Cache cache, RagbagFilterFactory filterFactory)
  {
    super(name, urn, cache);
    this.filterFactory = filterFactory;
    System.out.println("RagbagParsedCachedSequence constructor: " + name + " " + urn);
  }


  public void makeSequence()
    throws BioException
  {
    // with lazy instantiation, there's little to do but validate
    super.makeSequence();

    // screen files without instantiating sequence object
    try {
      RagbagSequenceItf seq = new RagbagSequence("", "", filterFactory.wrap(new RagbagIdleSequenceBuilder()));
 
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
  }
}

