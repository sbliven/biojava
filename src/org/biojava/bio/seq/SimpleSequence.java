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

/**
 * A no-frills implementation of Sequence.
 * <P>
 * It implements the ResidueList portion of Sequence by extending
 * SimpleResideList. This should probably be changed to delegation to allow
 * custom ResidueList implementations to be wrapped.
 *
 * @author Matthew Pocock
 */
public class SimpleSequence extends SimpleResidueList implements Sequence {
  /**
   * The default feature factory for all SimpleSequence objects.
   */
  protected static FeatureFactory SIMPLE_FEATURE_FACTORY;
  
  /**
   * Initialize SIMPLE_FEATURE_FACTORY.
   */
  static {
    SIMPLE_FEATURE_FACTORY = new SimpleFeatureFactory();
  }
  
  private FeatureFactory fFact;
  private String urn;
  private String name;
  private Annotation annotation;
  private MutableFeatureHolder featureHolder;

  /**
   * Initialize fFact.
   */
  {
    fFact = SIMPLE_FEATURE_FACTORY;
  }
  
  protected MutableFeatureHolder getFeatureHolder() {
    if(featureHolder == null)
      featureHolder = new SimpleMutableFeatureHolder();
    return featureHolder;
  }

  protected boolean featureHolderAllocated() {
    return featureHolder != null;
  }

  public FeatureFactory getFeatureFactory() {
    return fFact;
  }
  
  public void setFeatureFactory(FeatureFactory fFact) {
    this.fFact = fFact;
  }

  public String getURN() {
    return urn;
  }

  public void setURN(String urn) {
    this.urn = urn;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Annotation getAnnotation() {
    if(annotation == null)
      annotation = new SimpleAnnotation();
    return annotation;
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

  public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
    if(featureHolderAllocated())
      return getFeatureHolder().filter(ff, recurse);
    return new SimpleFeatureHolder();
  }

  public Feature createFeature(MutableFeatureHolder fh, Location loc,
                               String type, String source, Annotation annotation) {
    Feature f = fFact.createFeature(this, loc, type, source, annotation);
    if(fh == this) {
      fh = this.getFeatureHolder();
    }
    fh.addFeature(f);
    return f;
  }

  /**
   * Create a SimpleSequence with the residues and alphabet of res, and the
   * sequence properties listed.
   *
   * @param res the ResidueList to wrap as a sequence
   * @param urn the URN
   * @param name the name - should be unique if practical
   * @param annotation the annotation object to use or null
   */
  public SimpleSequence(ResidueList res, String urn, String name, Annotation annotation) {
    super(res);
    setURN(urn);
    setName(name);
    this.annotation = annotation;
  }
}
