package org.biojava.bio;

import java.util.*;
import org.biojava.utils.*;

public final class AnnotationTools {
  public static Annotation allIn(Annotation annotation, AnnotationType annType) {
    Annotation res;
    if(annotation instanceof SmallAnnotation) {
      res = new SmallAnnotation();
    } else {
      res = new SimpleAnnotation();
    }
    
    for(Iterator i = annType.getProperties().iterator(); i.hasNext(); ) {
      Object tag = i.next();
      try {
        res.setProperty(tag, annotation.getProperty(tag));
      } catch (ChangeVetoException cve) {
        throw new BioError(cve, "Assertion Failure: Can't alter an annotatoin");
      }
    }
    
    return res;
  }
  
  public static Annotation allOut(Annotation annotation, AnnotationType annType) {
    Annotation res;
    if(annotation instanceof SmallAnnotation) {
      res = new SmallAnnotation();
    } else {
      res = new SimpleAnnotation();
    }
    
    Set props = annType.getProperties();
    for(Iterator i = annotation.keys().iterator(); i.hasNext(); ) {
      Object tag = i.next();
      if(!props.contains(tag)) {
        try {
          res.setProperty(tag, annotation.getProperty(tag));
        } catch (ChangeVetoException cve) {
          throw new BioError(cve, "Assertion Failure: Can't alter an annotatoin");
        }
      }
    }
    
    return res;
  }
}
