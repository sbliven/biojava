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

package org.biojava.bio;

import java.util.*;
import org.biojava.utils.*;
import org.biojava.bio.symbol.*;

/**
 * <p><code>AnnotationTools</code> is a set of static utility methods for
 * manipulating <code>Annotation</code>s and <code>AnnotationType</code>s.</p>
 *
 * <p>The methods allIn() and allOut() let you compare an Annotation to an
 * AnnotationType and produce a new Annotation with only those properties
 * explicitly constrained by or not constrained by the type. This could be
 * of use when using an Annotation as a template for some object. You could use
 * allOut to make an Annotation that has all the properties that do not fit into
 * normal constructor properties, and pass that in as the Annotation bundle.</p>
 *
 * <p>intersection(AnnotationType) and union(AnnotationType) return new
 * AnnotationType instances that will accept every Annotation instance that is
 * accepted by both or either respectively. It is particularly informative to
 * compare the result of this to the AnnotationType.NONE to see if the two types
 * are mutualy disjoint.</p>
 *
 * <p>intersection(PropertyConstraint) and union(PropertyConstraint) return new
 * PropertyConstraint instances that will accept every Object that is accepted
 * by both or either one respectively.</p>
 *
 * @since 1.3
 * @author Matthew Pocock
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a> (docs).
 *
 * @for.powerUser
 * Comparing types and annotations. For example, FilterTools uses these methods
 * when comparing filters on features by their Annotation bundles.
 */
public final class AnnotationTools {
    /**
     * <p>
     * Destructive down-cast an annotation to a type.
     * </p>
     *
     * <p>
     * <code>allIn</code> returns a new <code>Annotation</code>
     * containing only those values in the <code>Annotation</code>
     * argument which are of a type specified by the
     * <code>AnnotationType</code>.
     * </p>
     *
     * @param annotation an <code>Annotation</code> to scan.
     * @param annType an <code>AnnotationType</code>.
     *
     * @return an <code>Annotation</code>.
     *
     * @for.powerUser When disecting an Annotation
     */
    public static Annotation allIn(Annotation annotation, AnnotationType annType) {
        Annotation res;
        if (annotation instanceof SmallAnnotation) {
            res = new SmallAnnotation();
        } else {
            res = new SimpleAnnotation();
        }

        for (Iterator i = annType.getProperties().iterator(); i.hasNext();) {
            Object tag = i.next();
            try {
                res.setProperty(tag, annotation.getProperty(tag));
            } catch (ChangeVetoException cve) {
                throw new BioError(cve, "Assertion Failure: Can't alter an annotation");
            }
        }

        return res;
    }

    /**
     * <code>allOut</code> returns a new <code>Annotation</code>
     * containing only those values in the <code>Annotation</code>
     * argument which are <strong>not</strong> of a type specified by
     * the <code>AnnotationType</code>.
     *
     * @param annotation an <code>Annotation</code>.
     * @param annType an <code>AnnotationType</code>.
     *
     * @return an <code>Annotation</code> value.
     *
     * @for.powerUser When disecting an Annotation
     */
    public static Annotation allOut(Annotation annotation, AnnotationType annType) {
        Annotation res;
        if (annotation instanceof SmallAnnotation) {
            res = new SmallAnnotation();
        } else {
            res = new SimpleAnnotation();
        }

        Set props = annType.getProperties();
        for (Iterator i = annotation.keys().iterator(); i.hasNext();) {
            Object tag = i.next();
            if (! props.contains(tag)) {
                try {
                    res.setProperty(tag, annotation.getProperty(tag));
                } catch (ChangeVetoException cve) {
                    throw new BioError(cve, "Assertion Failure: Can't alter an annotation");
                }
            }
        }

        return res;
    }
    
    /**
     * Calculate an AnnotationType that matches all Annotation instances matched
     * by both types.
     *
     * @param ann1  the first AnnotationType
     * @param ann1  the seccond AnnotationType
     * @return the intersection AnnotationType
     *
     * @for.powerUser
     * Usually you will either use this value blind or compare it to
     * AnnotationType.NONE.
     */
    public static AnnotationType intersection(
      AnnotationType ann1,
      AnnotationType ann2
    ) {
      if(ann1.subTypeOf(ann2)) {
        return ann2;
      } else if(ann2.subTypeOf(ann1)) {
        return ann1;
      } else {
        Set props = new HashSet();
        props.addAll(ann1.getProperties());
        props.addAll(ann2.getProperties());
        
        AnnotationType.Impl intersect = new AnnotationType.Impl();
        for(Iterator i = props.iterator(); i.hasNext(); ) {
          Object key = i.next();

          Location cc1 = ann1.getCardinalityConstraint(key);
          Location cc2 = ann2.getCardinalityConstraint(key);
          Location cc = LocationTools.intersection(cc1, cc2);
          if(cc == Location.empty) {
            return AnnotationType.NONE;
          }
          
          PropertyConstraint pc1 = ann1.getPropertyConstraint(key);
          PropertyConstraint pc2 = ann2.getPropertyConstraint(key);
          PropertyConstraint pc = intersection(pc1, pc2);
          if (pc == PropertyConstraint.NONE && !cc.contains(0)) {
            return AnnotationType.NONE;
          }
          
          intersect.setConstraints(key, pc, cc);
        }

        intersect.setDefaultConstraints(
          intersection(ann1.getDefaultPropertyConstraint(), ann2.getDefaultPropertyConstraint()),
          LocationTools.intersection(ann1.getDefaultCardinalityConstraint(), ann2.getDefaultCardinalityConstraint())
        );
        
        return intersect;
      }
    }
    
    /**
     * Calculate the intersection of two PropertyConstraint instances.
     *
     * @param pc1 the first PropertyConstraint
     * @param pc2 the seccond PropertyConstraint
     * @return the intersection PropertyConstraint
     *
     * @for.powerUser
     * This method is realy only interesting when comparing each property in an
     * AnnotationType in turn. Usually the return value is either compared to
     * PropertyConstraint.NONE or is used blindly.
     */
    public static PropertyConstraint intersection(
      PropertyConstraint pc1,
      PropertyConstraint pc2
    ) {
      if(pc1.subConstraintOf(pc2)) {
        return pc2;
      } else if(pc2.subConstraintOf(pc1)) {
        return pc1;
      } else if(
        pc1 instanceof PropertyConstraint.ByClass &&
        pc2 instanceof PropertyConstraint.ByClass
      ) {
        PropertyConstraint.ByClass pc1c = (PropertyConstraint.ByClass) pc1;
        PropertyConstraint.ByClass pc2c = (PropertyConstraint.ByClass) pc2;
        Class c1 = pc1c.getPropertyClass();
        Class c2 = pc2c.getPropertyClass();
        
        if(!c1.isInterface() && !c2.isInterface()) {
          return new PropertyConstraint.And(pc1c, pc2c);
        } else {
          return PropertyConstraint.NONE;
        }
      } else if(pc2 instanceof PropertyConstraint.ByClass) {
        return intersection(pc2, pc1);
      } else if(pc1 instanceof PropertyConstraint.ByClass) {
        PropertyConstraint.ByClass pc1c = (PropertyConstraint.ByClass) pc1;
        
        if(pc2 instanceof PropertyConstraint.Enumeration) {
          PropertyConstraint.Enumeration pc2e = (PropertyConstraint.Enumeration) pc2;
          Set values = new HashSet();
          for(Iterator i = pc2e.getValues().iterator(); i.hasNext(); ) {
            Object val = i.next();
            if(pc1c.accept(val)) {
              values.add(val);
            }
          }
          if(values.isEmpty()) {
            return PropertyConstraint.NONE;
          } else if(values.size() == 1) {
            return new PropertyConstraint.ExactValue(values.iterator().next());
          } else {
            return new PropertyConstraint.Enumeration(values);
          }
        }
        
        if(pc2 instanceof PropertyConstraint.ExactValue) {
          // we've already checked for containment - we know this value is of
          // the wrong class
          return PropertyConstraint.NONE;
        }
      } else if(
        (pc1 instanceof PropertyConstraint.Enumeration ||
         pc1 instanceof PropertyConstraint.ExactValue) &&
        (pc2 instanceof PropertyConstraint.Enumeration ||
         pc2 instanceof PropertyConstraint.ExactValue)
      ) {
        // they are iterated value types, but we know they are disjoint
        return PropertyConstraint.NONE;
      } else if(
        (pc1 instanceof PropertyConstraint.ByAnnotationType &&
         !(pc2 instanceof PropertyConstraint.ByAnnotationType)) ||
        (pc2 instanceof PropertyConstraint.ByAnnotationType &&
         !(pc1 instanceof PropertyConstraint.ByAnnotationType))
      ) {
        return PropertyConstraint.NONE;
      } else if(
        pc1 instanceof PropertyConstraint.ByAnnotationType &&
        pc2 instanceof PropertyConstraint.ByAnnotationType
      ) {
        PropertyConstraint.ByAnnotationType pc1a = (PropertyConstraint.ByAnnotationType) pc1;
        PropertyConstraint.ByAnnotationType pc2a = (PropertyConstraint.ByAnnotationType) pc2;
        
        AnnotationType intersect = intersection(
          pc1a.getAnnotationType(),
          pc2a.getAnnotationType()
        );
        if(intersect == AnnotationType.NONE) {
          return PropertyConstraint.NONE;
        } else {
          return new PropertyConstraint.ByAnnotationType(intersect);
        }
      }
      
      return new PropertyConstraint.And(pc1, pc2);
    }
    
    /**
     * Create an AnnotationType that matches all Anntotations that are accepted
     * by two others.
     *
     * @param ann1  the first AnnotationType
     * @param ann2  the seccond AnnotationType
     * @return an AnnotationType that represents their unions
     *
     * @for.powerUser
     * This method is realy not very usefull in most cases. You may wish to
     * compare the result of this to AnnotationType.ANY, or use it blindly.
     */
    public static AnnotationType union(
      AnnotationType ann1,
      AnnotationType ann2
    ) {
      if(ann1.subTypeOf(ann2)) {
        return ann1;
      } else if(ann2.subTypeOf(ann1)) {
        return ann2;
      } else {
        Set props = new HashSet();
        props.addAll(ann1.getProperties());
        props.addAll(ann2.getProperties());
        
        AnnotationType.Impl union = new AnnotationType.Impl();
        for(Iterator i = props.iterator(); i.hasNext(); ) {
          Object key = i.next();
          
          Location cc1 = ann1.getCardinalityConstraint(key);
          Location cc2 = ann2.getCardinalityConstraint(key);
          Location cc = LocationTools.intersection(cc1, cc2);
          
          PropertyConstraint pc1 = ann1.getPropertyConstraint(key);
          PropertyConstraint pc2 = ann2.getPropertyConstraint(key);
          PropertyConstraint pc = intersection(pc1, pc2);
          
          union.setConstraints(key, pc, cc);
        }
        
        return union;
      }
    }
    
    /**
     * Create a PropertyConstraint that matches all Objects that are accepted
     * by two others.
     *
     * @param pc1 the first PropertyConstraint
     * @param pc2 the seccond PropertyConstraint
     * @return the union PropertyConstraint
     *
     * @for.powerUser
     * In the general case, there is no clean way to represent the union of two
     * PropertyConstraint instances. You may get back a PropertyConstraint.Or
     * instance, or perhaps PropertyConstraint.ANY. Alternatively, there may be
     * some comparrison possible. It is a thankless task introspecting this in
     * code. You have been warned.
     */
    public static PropertyConstraint union(
      PropertyConstraint pc1,
      PropertyConstraint pc2
    ) {
      if(pc1.subConstraintOf(pc2)) {
        return pc1;
      } else if(pc2.subConstraintOf(pc1)) {
        return pc2;
      } else if(
        pc1 instanceof PropertyConstraint.ByClass &&
        pc2 instanceof PropertyConstraint.ByClass
      ) {
        return new PropertyConstraint.Or(pc1, pc2);
      } else if(pc2 instanceof PropertyConstraint.ByClass) {
        return intersection(pc2, pc1);
      } else if(pc1 instanceof PropertyConstraint.ByClass) {
        PropertyConstraint.ByClass pc1c = (PropertyConstraint.ByClass) pc1;
        
        if(pc2 instanceof PropertyConstraint.Enumeration) {
          PropertyConstraint.Enumeration pc2e = (PropertyConstraint.Enumeration) pc2;
          Set values = new HashSet();
          for(Iterator i = pc2e.getValues().iterator(); i.hasNext(); ) {
            Object val = i.next();
            if(!pc1c.accept(val)) {
              values.add(val);
            }
          }
          if(values.isEmpty()) {
            return pc1;
          } else if(values.size() == 1) {
            return new PropertyConstraint.Or(
              pc1,
              new PropertyConstraint.ExactValue(values.iterator().next())
            );
          } else {
            return new PropertyConstraint.Or(
              pc1,
              new PropertyConstraint.Enumeration(values)
            );
          }
        }
        
        if(pc2 instanceof PropertyConstraint.ExactValue) {
          // we've already checked for containment - we know this value is of
          // the wrong class
          return new PropertyConstraint.Or(pc1, pc2);
        }
      } else if(
        pc1 instanceof PropertyConstraint.ByAnnotationType &&
        pc2 instanceof PropertyConstraint.ByAnnotationType
      ) {
        PropertyConstraint.ByAnnotationType pc1a = (PropertyConstraint.ByAnnotationType) pc1;
        PropertyConstraint.ByAnnotationType pc2a = (PropertyConstraint.ByAnnotationType) pc2;
        
        return new PropertyConstraint.ByAnnotationType(union(
          pc1a.getAnnotationType(),
          pc2a.getAnnotationType()
        ));
      }
      
      return new PropertyConstraint.Or(pc1, pc2);
    }
}
