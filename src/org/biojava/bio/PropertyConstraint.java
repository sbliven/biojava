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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * <p><code>PropertyConstraint</code>s describes a constraint applied
 * to the members of an annotation bundle.</p>
 *
 * <p><code>PropertyConstraint</code>s are usually used in conjunction
 * with the <code>AnnotationType</code> interface to describe a class
 * of annotations by the values of their properties. In this way, you
 * can generate controlled vocabularies over Java objects.</p>
 *
 * <p>The constraints accept or reject individual objects.
 * In general, it is not possible to get back a set of all items
 * that would be accepted by a particular constraint.</p>
 *
 * @since 1.3
 * @author Matthew Pocock
 * @author Keith James
 * @author Thomas Down
 *
 * @for.powerUser Instantiate PropertyConstraint classes when populating an
 *            AnnotationType instance
 * @for.developer Implement PropertyContraint to provide meta-data about a new
 *            type of object relationship. For example, if there was a
 *            data-structure representing an inheritance hierachy, then an
 *            implementation of PropertyConstraint could be written that allowed
 *            a propertie's value to be constrained to be a child of a
 *            particular node in the hierachy
 */
public interface PropertyConstraint {
    /**
     * <code>accept</code> returns true if the value fulfills the
     * constraint.
     *
     * @param value an <code>Object</code> to check.
     * @return a <code>boolean</code>.
     *
     * @for.powerUser Manually compare items with the PropertyConstraint. Node:
     * this will ususaly be done for you in an AnnotationType instance
     *
     * @for.developer Use for implementing accept() on AnnotatoinType
     */
    public boolean accept(Object value);

    /**
     * <p><code>subConstraintOf</code> returns true if the constraint
     * is a sub-constraint.<p>
     *
     * <p>A pair of constraints super and sub are in a
     * superConstraint/subConstraint relationship if every object
     * accepted by sub is also accepted by super. To put it another
     * way, if instanceOf was used as a set-membership indicator
     * function over some set of objects, then the set produced by
     * super would be a superset of that produced by sub.</p>
     *
     * <p>It is not expected that constraints will neccesarily
     * maintain references to super/sub types. It will be more usual
     * to infer this relationship by introspecting the constraints
     * themselves. For example,
     * <code>PropertyConstraint.ByClass</code> will infer
     * subConstraintOf by looking at the possible class of all items
     * matching subConstraint.</p>
     *
     * @param subConstraint a <code>PropertyConstraint</code> to check.
     * @return a <code>boolean</code>.
     *
     * @for.developer
     * Usefull when attempting to compare two constraints to see
     * if it is necisary to retain both. You may want to check the more
     * general or the more specific constraint only.
     */
    public boolean subConstraintOf(PropertyConstraint subConstraint);
        
    /**
     * <code>ANY</code> is a constraint which accepts a property for
     * addition under all conditions.
     *
     * @for.user Whenever a PropertyConstraint is needed and you want to allow
     * any value there
     */
    public static final PropertyConstraint ANY = new AnyPropertyConstraint();
    
    /**
     * <code>NONE</code> is a constraint which accepts no value for a property
     * under any condition.
     *
     * @for.user Whenever a PropertyConstraint is needed and you want to
     * dissalow all values there e.g. when marking a property as having to be unset
     */
    public static final PropertyConstraint NONE = new NonePropertyConstraint();
  
    /**
     * <code>ByClass</code> accepts a property value if it is an
     * instance of a specific Java class.
     *
     * @since 1.3
     * @author Matthew Pocock
     * @for.user Constrain a property to containing values of a particular class
     *       e.g. <code>new ByClass(String.class)</code> or 
     *       <code>new ByClass(Double)</code> will ensure
     *       that the property is a String or a Double respecitvely.
     */
    public class ByClass implements PropertyConstraint {
        private Class cl;

        /**
         * Create a new ByClass instance.
         *
         * @param cl the Class that all properties must be assignable to
         * @for.user
         */
        public ByClass(Class cl) {
            this.cl = cl;
        }

        public Class getPropertyClass() {
            return cl;
        }

        public boolean accept(Object value) {
            return cl.isInstance(value);
        }

        public boolean subConstraintOf(PropertyConstraint subConstraint) {
            if (subConstraint instanceof ByClass) {
                ByClass sc = (ByClass) subConstraint;
                return cl.isAssignableFrom(sc.getPropertyClass());
            } else if(subConstraint instanceof Enumeration) {
              Set values = ((Enumeration) subConstraint).getValues();
              for(Iterator i = values.iterator(); i.hasNext(); ) {
                if(!accept(i.next())) {
                  return false;
                }
              }
              
              return true;
            } else if(subConstraint instanceof ExactValue) {
              return accept(((ExactValue) subConstraint).getValue());
            }

            return false;
        }
        
        public String toString() {
          return "Class:" + cl.toString();
        }
    }

    /**
     * <p><code>ByAnnotationType</code> accepts a property value if it
     * belongs to type defined by AnnotationType.</p>
     *
     * <p>If you had an Embl AnnotationType then you could say that the REF
     * property must contain annotations that fits your reference AnnotationType
     * (with author list, title, optinal medline ID etc.).</p>
     *
     * @since 1.3
     * @author Matthew Pocock
     *
     * @for.powerUser If you wish to build a tree of Annotations so that a
     * property in one is guaranteed to be itself an Annotation of a
     * particular type. Effectively this lets you build your own
     * type system using AnnotationType and PropertyConstraint.
     */
    public class ByAnnotationType implements PropertyConstraint {
        private AnnotationType annType;

        /**
         * Create a new constraint by type.
         *
         * @param annType the AnnotationType to constrain to
         * @for.user
         */
        public ByAnnotationType(AnnotationType annType) {
            this.annType = annType;
        }

        public AnnotationType getAnnotationType() {
            return annType;
        }

        public boolean accept(Object value) {
            if (value instanceof Annotation) {
                return annType.instanceOf((Annotation) value);
            }

            return false;
        }

        public boolean subConstraintOf(PropertyConstraint subConstraint) {
            if (subConstraint instanceof ByAnnotationType) {
                ByAnnotationType at = (ByAnnotationType) subConstraint;
                return annType.subTypeOf(at.getAnnotationType());
            }

            return false;
        }
        
        public String toString() {
          return "AnnotationType:" + annType.getProperties();
        }
    }

    /**
     * <p>Matches properties if they have exactly this one value.</p>
     *
     * <p>This is like the extreme case of an Enumeration which has just one
     * member. It is most usefull for selecting annotations with a particular
     * property set to a particular value e.g. ID="01234".</p>
     *
     * @author Matthew Pocock
     * @for.user If you want to declare that a property must have a single value
     *
     * @for.powerUser In conjunction with CardinalityConstraint.ZERO_OR_ONE you
     * could make a property that is potional but if present must have this
     * value
     *
     * @for.powerUser Use with FilterUtils.byAnnotation() to search for features
     * with properties set to specific values
     */
    public class ExactValue implements PropertyConstraint {
      private Object value;
      
      /**
       * Get a PropertyConstraint that matches this object and all those that
       * are equal to it (by the Object.equals() method).
       *
       * @param value  the Object to match against
       * @for.user
       */
      public ExactValue(Object value) {
        this.value = value;
      }
      
      public Object getValue() {
        return value;
      }
      
      public boolean accept(Object obj) {
        return value.equals(obj);
      }
      
      public boolean subConstraintOf(PropertyConstraint pc) {
        if(pc instanceof ExactValue) {
          return value.equals(((ExactValue) pc).getValue());
        } else if(pc instanceof Enumeration) {
          Enumeration e = (Enumeration) pc;
          return e.getValues().size() == 1 && e.accept(value);
        }
        
        return false;
      }
      
      public String toString() {
        return "ExactValue: " + value;
      }
    }
    
    /**
     * <code>Enumeration</code> accepts a property if it is present
     * in the specified set of values.
     *
     * @since 1.3
     * @author Matthew Pocock
     *
     * @for.user If you want to declare that a property must be within a range
     * of values, for example PRIMARY_COLOR is one of "RED, YELLOW, BLUE"
     *
     * @for.powerUser Use with FilterUtils.byAnnotation() to search for features
     * with properties set to a range of values
     */
    public class Enumeration implements PropertyConstraint {
        private Set values;

        /**
         * Creates a new <code>Enumeration</code> using the members of
         * the specified set as a constraint.
         *
         * @param values a <code>Set</code> of all possible values
         * @for.user
         */
        public Enumeration(Set values) {
            this.values = values;
        }
        
        /**
         * Creates a new <code>Enumeration</code> using the elements of the
         * specified array as a constraint.
         *
         * @param values an <code>Array</code> of all possible values
         * @for.user
         */
        public Enumeration(Object[] values) {
          this.values = new HashSet();
          for(int i = 0; i < values.length; i++) {
            this.values.add(values[i]);
          }
        }

        /**
         * <code>getValues</code> returns the set of values which
         * constrain the property.
         *
         * @return a <code>Set</code>.
         */
        public Set getValues() {
            return values;
        }

        public boolean accept(Object value) {
            return values.contains(value);
        }

        public boolean subConstraintOf(PropertyConstraint subConstraint) {
            if (subConstraint instanceof Enumeration) {
                Enumeration subE = (Enumeration) subConstraint;
                return values.containsAll(subE.getValues());
            } else if(subConstraint instanceof ExactValue) {
              return accept(((ExactValue) subConstraint).getValue());
            }

            return false;
        }
        
        public String toString() {
          return "Enumeration:" + values;
        }
    }
    
    /**
     * A property constraint that accpepts items iff they are accepted by both
     * child constraints. This effectively matches the intersection of the items
     * matched by the two constraints.
     *
     * @author Matthew Pocock
     * @for.powerUser Use this to combine multiple constraints. You can make one
     *            or both of the children And instances if you need a tighter
     *            intersection.
     */
    public class And implements PropertyConstraint {
      private PropertyConstraint c1;
      private PropertyConstraint c2;
      
      /**
       * Create a new <code>And</code> from two child constraints.
       *
       * @param c1 the first child
       * @param c2 the seccond child
       * @for.user
       */
      public And(PropertyConstraint c1, PropertyConstraint c2) {
        this.c1 = c1;
        this.c2 = c2;
      }
      
      /**
       * Get the first child PropertyConstraint.
       *
       * @return the first child PropertyConstraint
       *
       * @for.powerUser Introspect this constraint
       */
      public PropertyConstraint getChild1() {
        return c1;
      }
      
      /**
       * Get the seccond child PropertyConstraint.
       *
       * @return the seccond child PropertyConstraint
       *
       * @for.powerUser Introspect this constraint
       */
      public PropertyConstraint getChild2() {
        return c2;
      }
      
      public boolean accept(Object object) {
        return c1.accept(object) && c2.accept(object);
      }
      
      public boolean subConstraintOf(PropertyConstraint pc) {
        return c1.subConstraintOf(pc) && c2.subConstraintOf(pc);
      }
      
      public String toString() {
        return "And(" + c1 + ", " + c2 + ")";
      }
    }
    
    /**
     * A property constraint that accepts items iff they are accepted by either
     * child constraints. This effectively matches the union of the items
     * matched by the two constraints.
     *
     * @author Matthew Pocock
     * @for.powerUser Use this to combine multiple constraints. You can make one
     *            or both of the children Or instances if you need a wider
     *            union.
     */
    public class Or implements PropertyConstraint {
      private PropertyConstraint c1;
      private PropertyConstraint c2;
      
      /**
       * Create a new <code>Or</code> from two child constraints.
       *
       * @param c1 the first child
       * @param c2 the seccond child
       * @for.user
       */
      public Or(PropertyConstraint c1, PropertyConstraint c2) {
        this.c1 = c1;
        this.c2 = c2;
      }
      
      /**
       * Get the first child PropertyConstraint.
       *
       * @return the first child PropertyConstraint
       * @for.powerUser Introspect this constraint
       */
      public PropertyConstraint getChild1() {
        return c1;
      }
      
      /**
       * Get the seccond child PropertyConstraint.
       *
       * @return the seccond child PropertyConstraint
       * @for.powerUser Introspect this constraint
       */
      public PropertyConstraint getChild2() {
        return c2;
      }
      
      public boolean accept(Object object) {
        return c1.accept(object) || c2.accept(object);
      }
      
      public boolean subConstraintOf(PropertyConstraint pc) {
        return c1.subConstraintOf(pc) || c2.subConstraintOf(pc);
      }
      
      public String toString() {
        return "Or(" + c1 + ", " + c2 + ")";
      }
    }
}


