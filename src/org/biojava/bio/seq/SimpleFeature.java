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
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * A no-frills implementation of a feature.
 *
 * @author Matthew Pocock
 */
class SimpleFeature implements Feature, MutableFeatureHolder {
  /**
   * The FeatureHolder that we will delegate the FeatureHolder interface too.
   * This is lazily instantiated.
   */
  private MutableFeatureHolder featureHolder;


  /**
   * The location of this feature.
   */
  private Location loc;
  /**
   * The type of this feature - something like Exon.
   * This is included for cheap interoperability with GFF.
   */
  private String type;
  /**
   * The source of this feature - the program that generated it.,
   * This is included for cheap interoperability with GFF.
   */
  private String source;
  /**
   * The sequence that this feature ultimately resides in.
   */
  private Sequence sourceSeq;
  /**
   * The annotation object.
   * This is lazily instantiated.
   */
  private Annotation annotation;
 
  /**
   * A utility function to retrieve the feature holder delegate, creating it if
   * necisary.
   *
   * @return  the FeatureHolder delegate
   */
  protected MutableFeatureHolder getFeatureHolder() {
    if(featureHolder == null)
      featureHolder = new SimpleMutableFeatureHolder();
    return featureHolder;
  }

  /**
   * A utility function to find out if the feature holder delegate has been
   * instantiated yet. If it has not, we may avoid instantiating it by returning
   * some pre-canned result.
   *
   * @return true if the feature holder delegate has been created and false
   *         otherwise
   */
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
  
  public SymbolList getSymbols() {
    return getLocation().symbols(getSourceSeq());
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

  public SimpleFeature(Sequence sourceSeq, Feature.Template template)
  throws IllegalArgumentException{
    if(template.location == null) {
      throw new IllegalArgumentException(
        "Location can not be null. Did you mean Location.EMPTY_LOCATION?"
      );
    }
    this.sourceSeq = sourceSeq;
    this.loc = template.location;
    this.type = template.type;
    this.source = template.source;
    this.annotation = new SimpleAnnotation(template.annotation);
  }

  public String toString() {
    return "Feature " + getType() + " " +
              getSource() + " (" + getLocation() + ")";
  }
}
