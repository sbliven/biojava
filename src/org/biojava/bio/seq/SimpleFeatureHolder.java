/*
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

package org.biojava.bio.seq;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;

/**
 * A no-frills implementation of FeatureHolder.
 *
 * @author Matthew Pocock
 */
public class SimpleFeatureHolder extends AbstractFeatureHolder {
  /**
   * The child features.
   */
  private List features;

  /**
   * Initialize features.
   */
  {
    features = new ArrayList();
  }

  /**
  *Returns the list of features in this featureholder.
  */
  protected List getFeatures() {
    return features;
  }
  
  public int countFeatures() {
    return features.size();
  }

  public Iterator features() {
    return features.iterator();
  }

    /**
    *Add a feature to the featureholder
    */

  public void addFeature(Feature f)
  throws ChangeVetoException {
    if(!hasListeners()) {
      features.add(f);
    } else {
      ChangeSupport changeSupport = getChangeSupport(FeatureHolder.FEATURES);
      synchronized(changeSupport) {
        ChangeEvent ce = new ChangeEvent(
          this, FeatureHolder.FEATURES,
          f, null
        );
        changeSupport.firePreChangeEvent(ce);
        features.add(f);
        changeSupport.firePostChangeEvent(ce);
      }
    }
  }

  public void removeFeature(Feature f)
  throws ChangeVetoException {
    if(!hasListeners()) {
      features.remove(f);
    } else {
      ChangeSupport changeSupport = getChangeSupport(FeatureHolder.FEATURES);
      synchronized(changeSupport) {
        ChangeEvent ce = new ChangeEvent(
          this, FeatureHolder.FEATURES,
          null, f
        );
        changeSupport.firePreChangeEvent(ce);
        features.remove(f);
        changeSupport.firePostChangeEvent(ce);
      }
    }
  }
  
  public boolean containsFeature(Feature f) {
    return features.contains(f);
  }
}
