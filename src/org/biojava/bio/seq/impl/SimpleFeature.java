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

package org.biojava.bio.seq.impl;

import java.util.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * A no-frills implementation of a feature.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */

public class SimpleFeature
extends AbstractChangeable
implements Feature, RealizingFeatureHolder, java.io.Serializable {
  private transient ChangeListener annotationForwarder;
  private transient ChangeListener featureForwarder;
  
  /**
   * The FeatureHolder that we will delegate the FeatureHolder interface too.
   * This is lazily instantiated.
   */
  private SimpleFeatureHolder featureHolder;

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
   * Our parent FeatureHolder.
   */
  private FeatureHolder parent;
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
  protected SimpleFeatureHolder getFeatureHolder() {
    if(featureHolder == null) {
      featureHolder = new SimpleFeatureHolder();
    }
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

  protected ChangeSupport getChangeSupport(ChangeType ct) {
    ChangeSupport cs = super.getChangeSupport(ct);
    
    if(
      (annotationForwarder == null) &&
      (ct == null || ct == Annotatable.ANNOTATION)
    ) {
      annotationForwarder = new Annotatable.AnnotationForwarder(
        this,
        cs
      );
      getAnnotation().addChangeListener(
        annotationForwarder,
        Annotatable.ANNOTATION
      );
    }
    
    if(
      (featureForwarder == null) &&
      (ct == null || ct == FeatureHolder.FEATURES)
    ) {
      featureForwarder = new ChangeForwarder(
        this,
        cs
      );
      getFeatureHolder().addChangeListener(
        featureForwarder,
        FeatureHolder.FEATURES
      );
    }
    
    return cs;
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

    public FeatureHolder getParent() {
	return parent;
    }

    public Sequence getSequence() {
	FeatureHolder fh = this;
	while (fh instanceof Feature) {
	    fh = ((Feature) fh).getParent();
	}
	try {
	    return (Sequence) fh;
	} catch (ClassCastException ex) {
	    throw new BioError("Feature doesn't seem to have a Sequence ancestor.");
	}
    }

    public Annotation getAnnotation() {
	if(annotation == null)
	    annotation = new SimpleAnnotation();
	return annotation;
    }
  
    public SymbolList getSymbols() {
	return getLocation().symbols(getSequence());
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

    public void removeFeature(Feature f)
    throws ChangeVetoException {
	getFeatureHolder().removeFeature(f);
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	if(featureHolderAllocated())
	    return getFeatureHolder().filter(ff, recurse);
	return new SimpleFeatureHolder();
    }

    public Feature.Template makeTemplate() {
      Feature.Template ft = new Feature.Template();
      fillTemplate(ft);
      return ft;
    }
    
    protected void fillTemplate(Feature.Template ft) {
      ft.location = getLocation();
      ft.type = getType();
      ft.source = getSource();
      ft.annotation = getAnnotation();
    }
    
    public SimpleFeature(Sequence sourceSeq, 
			 FeatureHolder parent,
			 Feature.Template template)
	throws IllegalArgumentException
    {
	if(template.location == null) {
	    throw new IllegalArgumentException(
		     "Location can not be null. Did you mean Location.EMPTY_LOCATION?"
					      );
	}
	this.parent = parent;
	this.loc = template.location;
	this.type = template.type;
	this.source = template.source;
	this.annotation = new SimpleAnnotation(template.annotation);
    }

    public String toString() {
	return "Feature " + getType() + " " +
	    getSource() + " " + getLocation();
    }

    public Feature realizeFeature(FeatureHolder fh, Feature.Template templ)
        throws BioException
    {
	try {
	    RealizingFeatureHolder rfh = (RealizingFeatureHolder) getParent();
	    return rfh.realizeFeature(fh, templ);
	} catch (ClassCastException ex) {
	    throw new BioException("Couldn't propagate feature creation request.");
	}
    }

    public Feature createFeature(Feature.Template temp) 
    throws BioException, ChangeVetoException {
	Feature f = realizeFeature(this, temp);
	getFeatureHolder().addFeature(f);
	return f;
    }
}
