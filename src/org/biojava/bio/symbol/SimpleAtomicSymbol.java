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


package org.biojava.bio.symbol;

import org.biojava.utils.*;
import org.biojava.bio.*;

/**
 * A no-frills implementation of AtomicSymbol.
 *
 * @author Matthew Pocock
 */
public class SimpleAtomicSymbol implements AtomicSymbol {
  private final char token;
  private final String name;
  private final Annotation annotation;
  private final SingletonAlphabet alphabet;

  protected ChangeSupport changeSupport = null;
  protected Annotatable.AnnotationForwarder annotationForwarder = null;
  
  public SimpleAtomicSymbol(char token, String name, Annotation annotation) {
    if(annotation == null) {
      throw new IllegalArgumentException("Can't use null Annotation");
    }
    this.token = token;
    this.name = name;
    this.annotation = annotation;
    this.alphabet = new SingletonAlphabet(this);
  }
  
  public char getToken() {
    return token;
  }
  
  public String getName() {
    return name;
  }
  
  public Annotation getAnnotation() {
    return annotation;
  }
  
  public Alphabet getMatches() {
    return alphabet;
  }
  
  protected void generateChangeSupport(ChangeType changeType) {
    if(changeSupport == null) {
      changeSupport = new ChangeSupport();
    }
    
    if(
      ((changeType == null) || (changeType == Annotation.PROPERTY)) &&
      (annotationForwarder == null)
    ) {
      annotationForwarder = new Annotatable.AnnotationForwarder(this, changeSupport);
      annotation.addChangeListener(annotationForwarder, Annotation.PROPERTY);
    }
  }
  
  public void addChangeListener(ChangeListener cl) {
    generateChangeSupport(null);

    synchronized(changeSupport) {
      changeSupport.addChangeListener(cl);
    }
  }
  
  public void addChangeListener(ChangeListener cl, ChangeType ct) {
    generateChangeSupport(ct);

    synchronized(changeSupport) {
      changeSupport.addChangeListener(cl, ct);
    }
  }
  
  public void removeChangeListener(ChangeListener cl) {
    if(changeSupport != null) {
      synchronized(changeSupport) {
        changeSupport.removeChangeListener(cl);
      }
    }
  }
  
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {
    if(changeSupport != null) {
      synchronized(changeSupport) {
        changeSupport.removeChangeListener(cl, ct);
      }
    }
  }
}
