package org.biojava.bio.seq;

import java.util.*;
import org.biojava.utils.*;

/**
 * @author Matthew Pocock
 */
public class FeatureTypes {
  private static final Map repositories;
  public static final String URI_PREFIX = "uri:biojava.org:types";
  
  static {
    repositories = new SmallMap();
  }
  
  public static Repository getRepository(String name) {
    Repository rep = (Repository) repositories;
    
    if(rep == null) {
      throw new NoSuchElementException("Could not find repository: " + name);
    }
    
    return rep;
  }
  
  public static Set getRepositoryNames() {
    return repositories.keySet();
  }
  
  public static void addRepository(Repository types) {
    repositories.put(types.getName(), types);
  }
  
  public static void removeRepository(Repository types) {
    repositories.remove(types.getName());
  }
  
  public Type getType(String uri) {
    if(!uri.startsWith(URI_PREFIX)) {
      throw new NoSuchElementException(
        "All types start with: " + URI_PREFIX +
        " while processing " + uri
      );
    }
    
    String names = uri.substring(URI_PREFIX.length() + 1);
    int slash = uri.indexOf("/");
    String repName = names.substring(0, slash);
    String typeName = names.substring(slash + 1);
    
    Repository rep = getRepository(repName);
    return rep.getType(typeName);
  }
  
  public static isSubTypeOf(Type subType, Type superType) {
    List parents = subType.getParents();
    
    for(Iterator i = parents.iterator(); i.hasNext(); ) {
      String puri = (String) i.next();
      if(puri.equals(superType.getURI())) {
        return true;
      } else {
        return isSubTypeOf(getType(puri), superType);
      }
    }
  }
  
  public static interface Repository {
    String getName();
    Set getTypes();
    Type getType(String name);
  }
  
  public static interface Type {
    FeatureFilter getSchema();
    String getName();
    List getParents();
    String getURI();
  }
  
  public static class RepositoryImpl
  implements Repository {
    private final String name;
    private final Map types;
    
    public RepositoryImpl(String name) {
      this.name = name;
      types = new HashMap();
    }
    
    public String getName() {
      return name;
    }
    
    public Set getTypes() {
      return new HashSet(types.values());
    }
    
    public Type getType(String name)
    throws NoSuchElementException {
      Type type = (Type) types.get(name);
      if(type == null) {
        throw new NoSuchElementException(
          "Could not find type " + name +
          " in repository " + getName()
        );
      }
      return type;
    }
    
    public Type createType(
      final String name,
      final FeatureFilter schema,
      final List parents
    ) {
      Type type = new Type() {
        public String getName() {
          return name;
        }
        
        public FeatureFilter getSchema() {
          return schema;
        }
        
        public List getParents() {
          return parents;
        }
        
        public String getURI() {
          return URI_PREFIX + "/" + RepositoryImpl.this.getName() + "/" + name;
        }
      };
      
      types.put(name, type);
      return type;
    }
  }
}
