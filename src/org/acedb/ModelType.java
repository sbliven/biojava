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


package org.acedb;

/**
 * @author Matthew Pocock
 */

public class ModelType extends AceType {
  public final static ModelType MODEL = new ModelType("model");
  public final static ModelType TAG   = new ModelType("tag");
  public final static ModelType VALUE = new ModelType("value");
  public final static ModelType REF   = new ModelType("ref");
  
  private String name;
  
  private ModelType(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
}

