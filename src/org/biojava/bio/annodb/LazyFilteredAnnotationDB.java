package org.biojava.bio.annodb;

import java.util.*;
import org.biojava.bio.*;

public class LazyFilteredAnnotationDB
implements AnnotationDB {
  private final String name;
  private final AnnotationDB source;
  private final AnnotationType schema;
  private AnnotationDB result;
  
  public LazyFilteredAnnotationDB(String name, AnnotationDB source, AnnotationType schema) {
    this.name = name;
    this.source = source;
    this.schema = schema;
  }
  
  public String getName() {
    return "";
  }
  
  public AnnotationType getSchema() {
    if(result == null) {
      return this.schema;
    } else {
      return result.getSchema();
    }
  }
  
  public Iterator iterator() {
    if(result == null) {
      populate();
    }
    
    return result.iterator();
  }
  
  public int size() {
    if(result == null) {
      populate();
    }
    
    return result.size();
  }
  
  public AnnotationDB filter(AnnotationType at) {
    if(result == null) {
      return new LazyFilteredAnnotationDB(
        "",
        source,
        AnnotationTools.intersection(schema, at)
      );
    } else {
      return new LazyFilteredAnnotationDB(
        "",
        result,
        at
      );
    }
  }
  
  public AnnotationDB search(AnnotationType at) {
    if(result == null) {
      return new LazySearchedAnnotationDB(
        "",
        this,
        at
      );
    } else {
      return new LazySearchedAnnotationDB(
        "",
        result,
        at
      );
    }
  }
  
  private void populate() {
    if(result != null) {
      return;
    }
      
    Set hits = new  HashSet();
    
    for(Iterator i = iterator(); i.hasNext(); ) {
      Annotation ann = (Annotation) i.next();
      if(schema.instanceOf(ann)) {
        hits.add(ann);
      }
    }
    
    if(hits.isEmpty()) {
      result = AnnotationDB.EMPTY;
    } else {
      result = new SimpleAnnotationDB("", hits, schema);
    }
  }
}
