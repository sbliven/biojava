package org.biojava.bio.program.tagvalue;

import java.util.*;

import org.biojava.bio.*;
import org.biojava.utils.*;

/**
 * <p>
 * Builds an Annotation tree from TagValue events using an AnnotationType to
 * work out which fields are of what type.
 * </p>
 *
 * @since 1.2
 * @author Matthew pocock
 */
public class AnnotationBuilder
  implements TagValueListener
{
  private List annotationStack;
  private AnnotationType type;
  private Annotation last;
  
  /**
   * <p>
   * Make a new AnnotationBuilder that will build Annotation instances of a
   * given type.
   * </p>
   *
   * <p>
   * The type is used to provide appropreate accessors for properties. As tag
   * -value events stream through this TagValueListener, they will be matched
   * against the properties of the annotation type. As sub-trees of events are
   * pushed, child annotation bundles will be pushed into the apropreate
   * properties. If any of the tag-value events are of a type that are not
   * accepted by the annoataion type, a ClassCastException will be thrown.
   * </p>
   *
   * @param type  the AnnotationType stating what will be built and how
   * @throws ClassCastException if any of the tag-value events are of
   *         inapropreate type
   */
  public AnnotationBuilder(AnnotationType type) {
    this.type = type;
    this.annotationStack = new ArrayList();
  }
  
  /**
   * <p>
   * Get the last complete annotation built.
   * </p>
   *
   * <p>
   * The value of this is undefined before the first annotation has been built
   * and during the parsing of an event stream.
   * </p>
   *
   * @return the Annotation that was last built
   */
  public Annotation getLast() {
    return last;
  }
  
  public void startRecord() {
    Frame top = new Frame();
    top.annotation = new SmallAnnotation();
    if(annotationStack.isEmpty()) {
      top.type = this.type;
    } else {
      Frame old = peek(annotationStack);
      PropertyConstraint pc = old.type.getPropertyConstraint(old.tag);
      if(pc instanceof PropertyConstraint.IsCollectionOf) {
        PropertyConstraint.IsCollectionOf pcc = (PropertyConstraint.IsCollectionOf) pc;
        pc = pcc.getElementType();
      }
      if(pc instanceof PropertyConstraint.ByAnnotationType) {
        PropertyConstraint.ByAnnotationType pcat = (PropertyConstraint.ByAnnotationType) pc;
        top.type = pcat.getAnnotationType();
      } else {
        top.type = AnnotationType.ANY;
      }
    }
    push(annotationStack, top);
  }
  
  public void endRecord() {
    Frame top = pop(annotationStack);
    last = top.annotation;
    if(!annotationStack.isEmpty()) {
      try {
        Frame old = peek(annotationStack);
        old.type.setProperty(old.annotation, old.tag, last);
      } catch (ChangeVetoException cve) {
        throw new NestedError(cve);
      }
    }
  }
  
  public void startTag(Object tag) {
    peek(annotationStack).tag = tag;
  }
  
  public void value(TagValueContext ctxt, Object value) {
    try {
      Frame top = peek(annotationStack);
      top.type.setProperty(top.annotation, top.tag, value);
    } catch (ChangeVetoException cve) {
      throw new NestedError(cve);
    }
  }
  
  public void endTag() {}
  
  private void push(List list, Frame frame) {
    list.add(frame);
  }
  
  private Frame peek(List list) {
    return (Frame) list.get(list.size() - 1);
  }
  
  private Frame pop(List list) {
    return (Frame) list.remove(list.size() - 1);
  }
  
  private static class Frame {
    public AnnotationType type;
    public Annotation annotation;
    public Object tag;
  }
}
