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

public class SimpleFeatureHolder implements FeatureHolder {
  private List features;

  {
    features = new ArrayList();
  }

  public int countFeatures() {
    return features.size();
  }

  public Iterator features() {
    return features.iterator();
  }

  public void addFeature(Feature f) {
    features.add(f);
  }

  public void removeFeature(Feature f) {
    features.remove(f);
  }

  public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
    FeatureHolder res = new SimpleFeatureHolder();
    for(Iterator f = features(); f.hasNext();) {
      Feature feat = (Feature) f.next();
      if(ff.accept(feat))
        res.addFeature(feat);
      if(recurse) {
        FeatureHolder r = feat.filter(ff, recurse);
        for(Iterator rf = r.features(); rf.hasNext();) {
          res.addFeature((Feature) rf.next());
        }
      }
    }
    return res;
  }
}
