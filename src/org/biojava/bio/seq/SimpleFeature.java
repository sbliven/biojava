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

public class SimpleFeature implements Feature {
  private Location loc;
  private String type;
  private String source;
  private Sequence sourceSeq;
  private FeatureHolder featureHolder;
  private Annotation annotation;
 
  protected FeatureHolder getFeatureHolder() {
    if(featureHolder == null)
      featureHolder = new SimpleFeatureHolder();
    return featureHolder;
  }

  protected boolean featureHolderAllocated() {
    return featureHolder != null;
  }

  public Location getLocation() {
    return loc;
  }

  public String getType() {
    return type;
  }

  public String getSource() {
    return source;
  }

  protected Sequence getSourceSeq() {
    return sourceSeq;
  }

  public Annotation getAnnotation() {
    if(annotation == null)
      annotation = new SimpleAnnotation();
    return annotation;
  }
  
  public ResidueList getResidues() {
    return getLocation().residues(getSourceSeq());
  }

  public int countFeatures() {
    if(featureHolderAllocated())
      return getFeatureHolder().countFeatures();
    return 0;
  }

  public Iterator features() {
    if(featureHolderAllocated())
      return getFeatureHolder().features();
    return Collections.EMPTY_LIST.iterator();
  }

  public void addFeature(Feature f) {
    getFeatureHolder().addFeature(f);
  }

  public void removeFeature(Feature f) {
    getFeatureHolder().removeFeature(f);
  }

  public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
    if(featureHolderAllocated())
      return getFeatureHolder().filter(ff, recurse);
    return new SimpleFeatureHolder();
  }

  public SimpleFeature(Location loc, String type, String source,
                       Sequence sourceSeq, Annotation annotation) {
    this.loc = loc;
    this.type = type;
    this.source = source;
    this.sourceSeq = sourceSeq;
    this.annotation = annotation;
  }

  public String toString() {
    return "Feature " + getType() + " " +
              getSource() + " (" + getLocation() + ")";
  }
}
