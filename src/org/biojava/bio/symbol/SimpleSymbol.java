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

import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.*;

/**
 * A no-frills implementation of a symbol.
 *
 * @author Matthew Pocock
 */
public class SimpleSymbol implements Symbol, Serializable {
  private Annotation annotation;
  private char token;
  private String name;
  private Alphabet matches;
  
  protected transient ChangeSupport changeSupport = null;
  protected Annotatable.AnnotationForwarder annotationForwarder = null;
  
  protected void generateChangeSupport(ChangeType ct) {
    changeSupport = new ChangeSupport();
    
    if(
      ((ct == null) || (ct == Annotatable.ANNOTATION) ) &&
      annotationForwarder == null
    ) {
      annotationForwarder = new Annotatable.AnnotationForwarder(this, changeSupport);
      if(annotation != null) {
        annotation.addChangeListener(annotationForwarder);
      }
    }
  }
  
  public Annotation getAnnotation() {
    if(annotation == null) {
      annotation = new SimpleAnnotation();
      if(annotationForwarder != null) {
        annotation.addChangeListener(annotationForwarder);
      }
    }
    return this.annotation;
  }

  public char getToken() {
    return this.token;
  }

  public String getName() {
    return this.name;
  }
    /**
*Assign a name to the symbol
*@param name the name you wish to give this symbol
*/

  public void setName(String name) {
    this.name = name;
  }
  
  public Alphabet getMatches() {
    return this.matches;
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
  
  /**
   * Create a new SimpleSymbol.
   *
   * @param token  the char to represent this symbol when sequences are
   *                stringified
   * @param name  the long name
   * @param matches the Alphabet of symbols that this symbol can match
   * @param annotation the annotation
   */
  public SimpleSymbol(
    char token,
    String name,
    Alphabet matches,
    Annotation annotation
  ) {
    this.token = token;
    this.name = name;
    this.annotation = annotation;
    this.matches = matches;
  }

  public String toString() {
    return super.toString() + " " + token;
  }
}
