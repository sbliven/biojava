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

/**
 * A simple implementation of a residue.
 */
public class SimpleResidue implements Residue {
  /**
   * The annotation for this object.
   */
  private Annotation annotation;
  
  /**
   * The character symbol for this residue.
   */
  private char symbol;
  
  /**
   * The name for this residue.
   */
  private String name;

  public Annotation getAnnotation() {
    if(annotation == null)
      annotation = new SimpleAnnotation();
    return annotation;
  }

  public char getSymbol() {
    return symbol;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public SimpleResidue(char symbol, String name, Annotation annotation) {
    this.symbol = symbol;
    this.name = name;
    this.annotation = annotation;
  }

  public String toString() {
    return super.toString() + " " + symbol;
  }
}
