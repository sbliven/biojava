package org.biojava.utils;

import java.io.*;
import java.lang.reflect.*;

public class StaticMemberPlaceHolder implements Serializable {
  private String className;
  private String fieldName;
  
  public StaticMemberPlaceHolder(Field field) {
    this.className = field.getDeclaringClass().getName();
    this.fieldName = field.getName();
  }
  
  protected StaticMemberPlaceHolder() {}
  
  public Object readResolve() throws ObjectStreamException {
    try {
      Class c = getClass().forName(className);
      Field f = c.getDeclaredField(fieldName);
      return f.get(null);
    } catch (Exception e) {
      throw new InvalidObjectException(
        "Unable to retrieve static field " + fieldName + 
        "for class " + className + " because:\n" +
        e.getMessage()
      );
    }
  }
}
