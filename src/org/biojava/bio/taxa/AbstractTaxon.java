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
package org.biojava.bio.taxa;

import org.biojava.utils.*;
import org.biojava.bio.*;

/**
 * @author Matthew Pocock
 * @author Keith James`
 */
public abstract class AbstractTaxon
  extends
    AbstractChangeable
  implements
    Taxon
{
  private transient ChangeListener annotationForwarder;
  private Annotation ann;
  private String commonName;
  private String scientificName;
  
  protected AbstractTaxon() {}

  protected AbstractTaxon(String scientificName, String commonName) {
    this.scientificName = scientificName;
    this.commonName = commonName;
  }
  
  // ensure that change support gubbins gets wired in for the annotation object.
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
    
    return cs;
  }
  
  public String getCommonName() {
    return commonName;
  }
  
  public void setCommonName(String commonName)
    throws
      ChangeVetoException
  {
    if(this.commonName != null) {
      throw new ChangeVetoException(
        "Common name already set to: " +
        this.commonName +
        " so you can't set it to: " +
        commonName
      );
    }
    
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(Taxon.CHANGE_COMMON_NAME);
      ChangeEvent cevt = new ChangeEvent(this, Taxon.CHANGE_COMMON_NAME, commonName);
      synchronized(cs) {
        cs.firePreChangeEvent(cevt);
        this.commonName = commonName;
        cs.firePostChangeEvent(cevt);
      }
    } else {
      this.commonName = commonName;
    }
  }
  
  public String getScientificName() {
    return scientificName;
  }
  
  public void setScientificName(String scientificName)
    throws
      ChangeVetoException
  {
    if(this.scientificName != null) {
      throw new ChangeVetoException(
        "Common name already set to: " +
        this.scientificName +
        " so you can't set it to: " +
        scientificName
      );
    }
    
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(Taxon.CHANGE_SCIENTIFIC_NAME);
      ChangeEvent cevt = new ChangeEvent(this, Taxon.CHANGE_SCIENTIFIC_NAME, scientificName);
      synchronized(cs) {
        cs.firePreChangeEvent(cevt);
        this.scientificName = scientificName;
        cs.firePostChangeEvent(cevt);
      }
    } else {
      this.scientificName = scientificName;
    }
  }
  
  public Annotation getAnnotation() {
    if(ann == null) {
      ann = new SmallAnnotation();
    }
    
    return ann;
  }
  
  public boolean equals(Object o) {
    if(o instanceof Taxon) {
      Taxon t = (Taxon) o;
      
      return
        this == t || (
        safeEq(this.getScientificName(), t.getScientificName()) &&
        safeEq(this.getCommonName(), t.getCommonName()) &&
        safeEq(this.getChildren(), t.getChildren())
        );
    }
    
    return false;
  }
  
  public String toString() {
    Taxon parent = getParent();
    String scientificName = getScientificName();
    
    if(parent != null) {
      return parent.toString() + " -> " + scientificName;
    } else {
      return scientificName;
    }
  }
  
  public int hashCode() {
    return getScientificName().hashCode();
  }
  
  private boolean safeEq(Object a, Object b) {
    if(a == null && b == null) {
      return true;
    } else if(a == null || b == null) {
      return false;
    } else {
      return a.equals(b);
    }
  }
}

