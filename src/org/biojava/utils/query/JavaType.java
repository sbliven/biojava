package org.biojava.utils.query;

public class JavaType implements Type {
  private static java.util.Map typeCache = new java.util.HashMap();
  
  public static Type getType(Class clazz) {
    clazz = convertPrimatives(clazz);
    Type t = (Type) typeCache.get(clazz);
    if(t == null) {
      typeCache.put(clazz, t = new JavaType(clazz));
    }
    return t;
  }
  
  public static Type getType(Class[] clazzes) {
    if(clazzes.length == 1) {
      return getType(clazzes[0]);
    }
    
    Type[] types = new Type[clazzes.length];
    for(int i = 0; i < types.length; i++) {
      types[i] = getType(clazzes[i]);
    }
    
    return new SimpleTuple.TypeList(types);
  }
  
  private final Class clazz;
  
  private JavaType(Class clazz) {
    this.clazz = clazz;
  }
  
  public String getName() {
    return "javaclass:" + clazz.getName();
  }
  
  public Class getJavaClass() {
    return clazz;
  }
  
  public boolean isAssignableFrom(Type type) {
    if(type instanceof JavaType) {
      JavaType jType = (JavaType) type;
      
      return this.getJavaClass().isAssignableFrom(jType.getJavaClass());
    }
    
    return false;
  }
  
  public boolean isInstance(Object o) {
    return getJavaClass().isInstance(o);
  }
  
  public String toString() {
    return getName();
  }
  
  public static Class convertPrimatives(Class inputType) {
    if(inputType.isPrimitive()) {
      if(false) ; // syntactic sugar to make the rest line up
      else if(inputType == Boolean.TYPE)   inputType = Boolean.class;
      else if(inputType == Character.TYPE) inputType = Character.class;
      else if(inputType == Byte.TYPE)      inputType = Byte.class;
      else if(inputType == Short.TYPE)     inputType = Short.class;
      else if(inputType == Integer.TYPE)   inputType = Integer.class;
      else if(inputType == Long.TYPE)      inputType = Long.class;
      else if(inputType == Float.TYPE)     inputType = Float.class;
      else if(inputType == Double.TYPE)    inputType = Double.class;
      else throw new IllegalArgumentException("Can't convert VOID");
    }
    
    return inputType;
  }
}
