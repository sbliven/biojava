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

import org.biojava.utils.cache.Cache;
import org.biojava.utils.cache.SoftReferenceCache;

/**
 * class that is passed to a RagbagAssembly to use
 * a SoftReferenceCache-backed RagbagCachedSequences.
 */
public class RagbagFilteredCachedSeqFactory implements RagbagSequenceFactory
{
  Cache cache;

  {
    cache = new SoftReferenceCache();
  }

  public RagbagSequenceItf getSequenceObject(String name, String urn)
  {
    return (RagbagSequenceItf) new RagbagParsedCachedSequence(name, urn, cache, RagbagFeatureTypeCatcher.FACTORY);
  }
}
