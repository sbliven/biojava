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

import java.net.*;
import java.util.Map;
import java.util.HashMap;

/**
 * Encapsulates core acedb node types.
 *
 * @author Matthew Pocock
 */
public abstract class AceType {
    /**
     * Type representing a tag within an ACeDB object.
     */

  public final static TagType TAG          = new TagType();

    /**
     * Type representing an integer value in an ACeDB object.
     */

  public final static ValueType INT        = new ValueType("int");

    /**
     * Type representing a floating-point (real) number in an
     * ACeDB object.
     */

  public final static ValueType FLOAT      = new ValueType("float");

    /**
     * Type representing a date in an ACeDB object.
     */

  public final static ValueType DATE       = new ValueType("date_type");

    /**
     * Type representing a free-form string in an ACeDB object.
     */

  public final static ValueType STRING     = new ValueType("string");

  /**
   * Flyweight for class type objects - ensures that equality checks work without magic.
   */
  private static Map classes;

  static {
    classes = new HashMap();
  }

  /**
   * Retrieve a class type for the class name.
   */
  public static ClassType getClassType(Database db, String className) {
    URL cURL = null;

    try {  
      cURL = new URL(db.toURL(), className);
    } catch (MalformedURLException ex) {
      throw new AceError(ex, "Unable to generate url for class " + className);
    }

    ClassType ct = (ClassType) classes.get(cURL);
    if(ct == null) {
      ct = new ClassType(db, className);
      classes.put(cURL, ct);
    }
    return ct;
  }
  
  /**
   * Return the name of this type.
   * <P>
   * Names include the class name, 'element', 'float', 'date' or 'string'.
   */
  public abstract String getName();
  
  /**
   * Type for representing ACeDB top-level Objects.
   */
  public static class ClassType extends AceType {
    private Database db;
    private String name;
    private RefType rt;
    
    ClassType(Database db, String name) {
      this.db = db;
      this.name = name;
      rt = new RefType(this);
    }
    
    public String getName() {
      return name;
    }
    
    public RefType getRefType() {
      return rt;
    }

      /*
    
    public ModelNode getModel() {
      return db.getModel(this);
    }

      */
  }
  
  /**
   * Element type.
   */
  private static class TagType extends AceType {
    public String getName() {
      return "tag";
    }
  }

  /**
   * A value type - instances held in AceType.FLOAT, INT, DATE, STRING.
   */
  private static class ValueType extends AceType {
    private String name;
    ValueType(String name) {
      this.name = name;
    }
    public String getName() {
      return name;
    }
  }
  
    /**
     * Type for a reference within an ACeDB object.  One of
     * these is created implicitly to go with each class type.
     */

  public static class RefType extends AceType {
    private ClassType type;
    RefType(ClassType type) {
      this.type = type;
    }
    public String getName() {
      return "REF " + type.getName();
    }
    public ClassType getClassType() {
      return type;
    }
  }
}
