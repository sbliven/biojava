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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.io.ParseException;
import org.biojava.bio.seq.io.SequenceBuilder;
import org.biojava.bio.seq.io.SequenceBuilderFilter;

/**
 * A class that assists in wrapping a feature filter
 * that records all types encountered
 * around a SequenceBuilder class.
 * It may be scrapped as it was implemented to
 * permit a getAllTypes() call which is IMPOSSIBLE
 * with lazy instantiation.
 */
public class RagbagFeatureTypeCatcher
  implements RagbagFilterFactory
{
  public static RagbagFeatureTypeCatcher FACTORY = new RagbagFeatureTypeCatcher();

  private static Set allTypes = new HashSet();

  private class RagbagFeatureFilter extends SequenceBuilderFilter
  {
    RagbagFeatureFilter(SequenceBuilder delegate)
    {
      super(delegate);
    }

    // override the startFeature method
    public void startFeature(Feature.Template templ)
      throws ParseException
    {
      allTypes.add(templ.type);
      getDelegate().startFeature(templ);
    }
  }

  public SequenceBuilder wrap(SequenceBuilder delegate)
  {
    return new RagbagFeatureFilter(delegate);
  }

  public Set getAllTypes() 
  {
    return Collections.unmodifiableSet(allTypes);
  }
}


