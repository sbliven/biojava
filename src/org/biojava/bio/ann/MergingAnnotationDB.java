package org.biojava.bio.ann;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;

/**
 * <p>An AnnotationDB that provides a merged view of a list of underlying DBs.</p>
 *
 * @author Matthew Pocock
 */
public class MergingAnnotationDB implements AnnotationDB {
  private final String name;
  private final List merged;
  
  public MergingAnnotationDB(String name) {
    this.name = name;
    this.merged = new ArrayList();
  }
  
  public MergingAnnotationDB(String name, List merged) {
    this.name = name;
    this.merged = new ArrayList(merged);
  }
  
  public void addAnnotationDB(AnnotationDB toAdd) {
    if(!merged.contains(toAdd)) {
      merged.add(toAdd);
    }
  }
  
  public void removeAnnotationDB(AnnotationDB toRemove) {
    merged.remove(toRemove);
  }
  
  public List getMerged() {
    return new ArrayList(merged);
  }
  
  public String getName() {
    return name;
  }
  
  public AnnotationType getSchema() {
    AnnotationType schema = AnnotationType.NONE;
    
    for(Iterator i = merged.iterator(); i.hasNext(); ) {
      AnnotationDB db = (AnnotationDB) i.next();
      schema = AnnotationTools.union(schema, db.getSchema());
    }
    
    return schema;
  }

  public Iterator iterator() {
    return new Iterator() {
      Iterator ii;
      Iterator ci;
      Object item;
      
      {
        ii = merged.iterator();
       EVERYTHING:
        while(item == null) {
          if(ii.hasNext()) {
            AnnotationDB adb = (AnnotationDB) ii.next();
            ci = adb.iterator();
            if(ci.hasNext()) {
              item = ci.next();
            }
          } else {
            break EVERYTHING;
          }
        }
      }
      
      public boolean hasNext() {
        return item != null;
      }
      
      public Object next() {
        Object it = item;
        item = _next();
        return it;
      }
      
      private Object _next() {
        while(!ci.hasNext()) {
          if(ii.hasNext()) {
            AnnotationDB adb = (AnnotationDB) ii.next();
            ci = adb.iterator();
          } else {
            return null;
          }
        }
        
        return ci.next();
      }
      
      public void remove() {
        throw new NoSuchElementException();
      }
    };
  }
  
  public int size() {
    int size = 0;
    
    for(Iterator dbi = merged.iterator(); dbi.hasNext(); ) {
      size += ((AnnotationDB) dbi.next()).size();
    }
    
    return size;
  }
  
  public AnnotationDB filter(AnnotationType at) {
    List anns = new ArrayList();
    
    for(Iterator i = merged.iterator(); i.hasNext(); ) {
      AnnotationDB adb = (AnnotationDB) i.next();
      AnnotationDB res = adb.filter(at);
      if(res.size() > 0) {
        anns.add(res);
      }
    }
    
    if(anns.isEmpty()) {
      return AnnotationDB.EMPTY;
    } else if(anns.size() == 1) {
      return (AnnotationDB) anns.get(0);
    } else {
      return new MergingAnnotationDB("", anns);
    }
  }
  
  public AnnotationDB search(AnnotationType at) {
    List anns = new ArrayList();
    
    for(Iterator i = merged.iterator(); i.hasNext(); ) {
      AnnotationDB adb = (AnnotationDB) i.next();
      AnnotationDB res = adb.search(at);
      if(res.size() > 0) {
        anns.add(res);
      }
    }
    
    if(anns.isEmpty()) {
      return AnnotationDB.EMPTY;
    } else if(anns.size() == 1) {
      return (AnnotationDB) anns.get(0);
    } else {
      return new MergingAnnotationDB("", anns);
    }
  }
}

