/*
 * BioJava development code
 * 
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 * 
 * http://www.gnu.org/copyleft/lesser.html
 * 
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 * 
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 * 
 * http://www.biojava.org
 */

package org.biojava.utils;

import java.io.*;

import java.lang.reflect.*;

/**
 *  
 * Class for all constants which are used to indicate change
 * types.  Note that all ChangeType objects must be accessible
 * via a public static field of some class or interface.  These should
 * be specified at construction time, so that the ChangeType can
 * be properly serialized.  Typically, they should be constructed
 * using code like:
 * <pre>
 * class MyClassWhichCanThrowChangeEvents {
 * public final static ChangeEvent CHANGE_COLOR = new ChangeEvent(
 * "Color change",
 * MyClassWhichCanThrowChangeEvents.class,
 * "CHANGE_COLOR");
 * Rest of the class here...
 * }
 *
 * @author     Thomas Down
 * @author     Matthew Pocock
 * @created    September 29, 2000 
 * @since      1.1 
 */

public final class ChangeType implements Serializable {

  private final String name;
  private final Field ourField;
  
  /**
   * Constant ChangeType field which indicates that a change has
   * occured which can't otherwise be represented.  Please do not
   * use this when there is another, more sensible, option.
 This
   * is the fallback for when you realy don't know what else to
   * do.
   */

  public static final ChangeType UNKNOWN;


  /**
   *  Construct a new ChangeType. 
   *
   * @param  name      The name of this change. 
   * @param  ourField  The public static field which contains this 
   *      ChangeType. 
   */

  public ChangeType(String name, Field ourField) {
    this.name = name;
    this.ourField = ourField;
  }


  /**
   *  Construct a new ChangeType. 
   *
   * @param  name   The name of this change. 
   * @param  clazz  The class which is going to contain this change. 
   * @param  fname  
   * The name of the field in <code>clazz</code> which 
   * is to contain a reference to this change.
   * @throws        BioError If the field cannot be found. 
   */

  public ChangeType(String name, Class clazz, String fname) {
    this.name = name;
    try {
      this.ourField = clazz.getField(fname);
    }
    catch (Exception ex) {
      throw new NestedError("Couldn't find field " + fname + " in class " + clazz.getName());
    }
  }


  /**
   *  Return the name of this change. 
   *
   * @return    The Name value 
   */

  public String getName() {
    return name;
  }


  /**
   *  Return a string representation of this change. 
   *
   * @return    Description of the Returned Value 
   */

  public String toString() {
    return "ChangeType: " + name;
  }


  /**
   *  Make a placeholder for this object in a serialized stream. 
   *
   * @return    Description of the Returned Value 
   */

  private Object writeReplace() {
    return new StaticMemberPlaceHolder(ourField);
  }

  static {
    UNKNOWN = new ChangeType(
      "Unknown change", 
      ChangeType.class, 
      "UNKNOWN"
    );
  }
}
