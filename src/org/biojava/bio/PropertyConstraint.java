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

/**
 * <p><code>PropertyConstraint</code>s describes a constraint applied
 * to the members of an annotation bundle.</p>
 *
 * <p><code>PropertyConstraint</code>s are usually used in conjunction
 * with the <code>AnnotationType</code> interface to describe a class
 * of annotations by the values of their properties. In this way, you
 * can generate controlled vocabularies over Java objects.</p>
 *
 * <p>The constraints accept or reject individual objects and provide
 * an accessor method to appropriately manipulate properties of their
 * type. In general, it is not possible to get back a set of all items
 * that would be accepted by a particular constraint.</p>
 *
 * @since 1.3
 * @author Matthew Pocock
 * @author Keith James.
 */
public interface PropertyConstraint {
    /**
     * <code>accept</code> returns true if the value fulfills the
     * constraint.
     *
     * @param value an <code>Object</code> to check.
     * @return a <code>boolean</code>.
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
     */
    public boolean subConstraintOf(PropertyConstraint subConstraint);

    /**
     * <p><code>setProperty</code> sets a property in the Annotation
     * such that it conforms to the constraint.</p>
     *
     * @param ann an <code>Annotation</code> to populate.
     * @param property an <code>Object</code> under which to add the
     * value.
     * @param value an <code>Object</code> to add.
     * @exception ChangeVetoException if an error occurs.
     */
    public void setProperty(Annotation ann, Object property, Object value)
        throws ChangeVetoException;

    /**
     * <p>Adds a value to the collection.</p>
     *
     * @param coll the Collection to modufy to include value
     * @param value  the value to add
     * @throws ChangeVetoException if the value can not be added
     */
    public void addValue(Collection coll, Object value)
        throws ChangeVetoException;
        
    /**
     * <code>ANY</code> is a constraint which accepts a property for
     * addition under all conditions.
     */
    public static final PropertyConstraint ANY = new AnyPropertyConstraint();
    
    public static final PropertyConstraint NONE = new NonePropertyConstraint();
  
    /**
     * <code>ByClass</code> accepts a property value if it is an
     * instance of a specific Java class.
     *
     * @since 1.3
     * @author Matthew Pocock
     */
    public class ByClass implements PropertyConstraint {
        private Class cl;

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

        public void setProperty(Annotation ann, Object property, Object value)
            throws ChangeVetoException {
            if (accept(value)) {
                ann.setProperty(property, value);
            } else {
                throw new ChangeVetoException(
                  "Incorrect class: expecting " + cl +
                  " but got " + value.getClass()
                );
            }
        }
        
        public void addValue(Collection coll, Object value)
        throws ChangeVetoException {
          if(accept(value)) {
            coll.add(value);
          } else {
            throw new ChangeVetoException(
              "Incorrect class: expecting " + cl +
              " but got " + value.getClass()
            );
          }
        }
        
        public String toString() {
          return "Class:" + cl.toString();
        }
    }

    /**
     * <code>ByAnnotationType</code> accepts a property value if it
     * belongs to type defined by AnnotationType.
     *
     * @since 1.3
     * @author Matthew Pocock
     */
    public class ByAnnotationType implements PropertyConstraint {
        private AnnotationType annType;

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
 
        public void setProperty(Annotation ann, Object property, Object value)
            throws ChangeVetoException {
            if (accept(value)) {
                ann.setProperty(property, value);
            } else {
              throw new ChangeVetoException(
                "value: " + value +
                " is not an annotation implementing " + annType.getProperties()
              );
            }
        }
        
        public void addValue(Collection coll, Object value)
        throws ChangeVetoException {
          if(accept(value)) {
            coll.add(value);
          } else {
            throw new ChangeVetoException(
              "value: " + value +
              " is not an annotation implementing " + annType.getProperties()
            );
          }
        }
        
        public String toString() {
          return "AnnotationType:" + annType.getProperties();
        }
    }

    public class ExactValue implements PropertyConstraint {
      private Object value;
      
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
      
      public void setProperty(Annotation ann, Object prop, Object val)
      throws ChangeVetoException {
        if(accept(val)) {
          ann.setProperty(prop, val);
        } else {
          throw new ChangeVetoException(
            "Can't set property " + prop +
            " to " + val + " as it is not " + value
          );
        }
      }
      
      public void addValue(Collection coll, Object val)
      throws ChangeVetoException {
        if(accept(val)) {
          coll.add(val);
        } else {
          throw new ChangeVetoException(
            "Can't set property to " + val +
            " as it is not " + value
          );
        }
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
     */
    public class Enumeration implements PropertyConstraint {
        private Set values;

        /**
         * Creates a new <code>Enumeration</code> using the members of
         * the specified set as a constraint.
         *
         * @param values a <code>Set</code> of all possible values
         */
        public Enumeration(Set values) {
            this.values = values;
        }
        
        /**
         * Creates a new <code>Enumeration</code> using the elements of the
         * specified array as a constraint.
         *
         * @param values an <code>Array</code> of all possible values
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

        public void setProperty(Annotation ann, Object property, Object value)
            throws ChangeVetoException {
            if (accept(value)) {
                ann.setProperty(property, value);
            } else {
                throw new ChangeVetoException(
                  "Value not accepted: '" + value + "'" +
                  " not in " + values
                );
            }
        }
        
        public void addValue(Collection coll, Object value)
        throws ChangeVetoException {
          if(accept(value)) {
            coll.add(value);
          } else {
            throw new ChangeVetoException(
              "Value not accepted: '" + value + "'" +
              " not in " + values
            );
          }
        }
        
        public String toString() {
          return "Enumeration:" + values;
        }
    }
    
    public class And implements PropertyConstraint {
      private PropertyConstraint c1;
      private PropertyConstraint c2;
      
      public And(PropertyConstraint c1, PropertyConstraint c2) {
        this.c1 = c1;
        this.c2 = c2;
      }
      
      public PropertyConstraint getChild1() {
        return c1;
      }
      
      public PropertyConstraint getChild2() {
        return c2;
      }
      
      public boolean accept(Object object) {
        return c1.accept(object) && c2.accept(object);
      }
      
      public boolean subConstraintOf(PropertyConstraint pc) {
        return c1.subConstraintOf(pc) && c2.subConstraintOf(pc);
      }
      
      public void setProperty(Annotation ann, Object key, Object val)
      throws ChangeVetoException {
        if(!accept(val)) {
          throw new ChangeVetoException("Can't accept value: " + this + ": " + val);
        }
        c1.setProperty(ann, key, val);
      }
      
      public void addValue(Collection coll, Object val)
      throws ChangeVetoException {
        if(!accept(val)) {
          throw new ChangeVetoException("Can't accept value: " + this + ": " + val);
        }
        c1.addValue(coll, val);
      }
      
      public String toString() {
        return "And(" + c1 + ", " + c2 + ")";
      }
    }
    
    public class Or implements PropertyConstraint {
      private PropertyConstraint c1;
      private PropertyConstraint c2;
      
      public Or(PropertyConstraint c1, PropertyConstraint c2) {
        this.c1 = c1;
        this.c2 = c2;
      }
      
      public PropertyConstraint getChild1() {
        return c1;
      }
      
      public PropertyConstraint getChild2() {
        return c2;
      }
      
      public boolean accept(Object object) {
        return c1.accept(object) || c2.accept(object);
      }
      
      public boolean subConstraintOf(PropertyConstraint pc) {
        return c1.subConstraintOf(pc) || c2.subConstraintOf(pc);
      }
      
      public void setProperty(Annotation ann, Object key, Object val)
      throws ChangeVetoException {
        if(!accept(val)) {
          throw new ChangeVetoException("Can't accept value: " + this + ": " + val);
        }
        c1.setProperty(ann, key, val);
      }
      
      public void addValue(Collection coll, Object val)
      throws ChangeVetoException {
        if(!accept(val)) {
          throw new ChangeVetoException("Can't accept value: " + this + ": " + val);
        }
        c1.addValue(coll, val);
      }
      
      public String toString() {
        return "Or(" + c1 + ", " + c2 + ")";
      }
    }
}

class AnyPropertyConstraint implements PropertyConstraint  {
    public boolean accept(Object value) {
        return true;
    }
    
    public boolean subConstraintOf(PropertyConstraint subConstraint) {
        return true;
    }
    
    public void setProperty(Annotation ann, Object property, Object value)
        throws ChangeVetoException {
        ann.setProperty(property, value);
    }
    
    public void addValue(Collection coll, Object value)
    throws ChangeVetoException {
      coll.add(value);
    }
    
    public String toString() {
      return "ANY";
    }
}

class NonePropertyConstraint implements PropertyConstraint {
  public boolean accept(Object value) {
    return false;
  }
  
  public boolean subConstraintOf(PropertyConstraint subConstraint) {
    return subConstraint instanceof NonePropertyConstraint;
  }
  
  public void setProperty(Annotation ann, Object property, Object value)
        throws ChangeVetoException {
    throw new ChangeVetoException(
      "There are no values that could match this property constraint: " +
      ann + ", " + property + " -> " + value
    );
  }
  
  public void addValue(Collection coll, Object value)
        throws ChangeVetoException {
    throw new ChangeVetoException(
      "There are no values that could match this property constraint: " +
      value
    );
  }
  
  public String toString() {
    return "NONE";
  }
}
