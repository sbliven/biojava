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

import org.biojava.bio.*;

/**
 * A no-frills implementation of a symbol.
 *
 * @author Matthew Pocock
 */
public class SimpleSymbol implements Symbol {
  /**
   * The annotation for this object.
   */
  private Annotation annotation;
  
  /**
   * The character token for this symbol.
   */
  private char token;
  
  /**
   * The name for this symbol.
   */
  private String name;

  public Annotation getAnnotation() {
    if(annotation == null)
      annotation = new SimpleAnnotation();
    return annotation;
  }

  public char getToken() {
    return token;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * Create a new SimpleSymbol.
   *
   * @param token  the char to represent this symbol when sequences are
   *                stringified
   * @param name  the long name
   * @param annotation the annotation
   */
  public SimpleSymbol(char token, String name, Annotation annotation) {
    this.token = token;
    this.name = name;
    this.annotation = annotation;
  }

  public String toString() {
    return super.toString() + " " + token;
  }
}
