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
 * @author Keith James (docs).
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
     * themselves. For example, PropertyConstraint.ByClass will infer
     * subConstraintOf by looking at the possible class of all items
     * matching subConstraint.</p>
     *
     * @param subConstraint a <code>PropertyConstraint</code> to check.
     * @return a <code>boolean</code>.
     */
    public boolean subConstraintOf(PropertyConstraint subConstraint);

    /**
     * <p><code>setProperty</code> sets a property in the Annotation
     * such that it conforms to the constraint. For example, you
     * create an Annotation having a key which is the String
     * "gene_synonyms" and corresponding value which has a
     * PropertyConstraint indicating that it must be an HashSet of
     * between 1 and 10 Strings. To add a new String "xylR" to the Set
     * you would call the method thus:
     * <code>setProperty(annotationObj, "gene_synonyms",
     * "xylR")</code>.</p>
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
     * <code>ANY</code> is a constraint which accepts a property for
     * addition under all conditions.
     */
    public static final PropertyConstraint ANY = new AnyPropertyConstraint();
  
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
            }

            return false;
        }

        public void setProperty(Annotation ann, Object property, Object value)
            throws ChangeVetoException {
            if (accept(value)) {
                ann.setProperty(property, value);
            } else {
                throw new ChangeVetoException("Incorrect class: " + cl + " not " + value.getClass());
            }
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
                throw new ChangeVetoException("Incorrect annotation type");
            }
        }
    }

    /**
     * <code>IsCollectionOf</code> accepts a property value if it is a
     * collection of objects which themselves conform to a specified
     * constraint.
     *
     * @since 1.3
     * @author Matthew Pocock
     */
    public class IsCollectionOf implements PropertyConstraint {
        private PropertyConstraint elementType;
        private Class clazz;
        private int minTimes;
        private int maxTimes;

        /**
         * Creates a new <code>IsCollectionOf</code> which accepts a
         * collection of 0 - Integer.MAX_VALUE elements which
         * themselves conform to the specified constraint.
         *
         * @param clazz a <code>Class</code> of collection.
         * @param elementType a <code>PropertyConstraint</code> to
         * constrain members of the collection.
         */
        public IsCollectionOf(Class clazz, PropertyConstraint elementType) {
            this(clazz, elementType, 0, Integer.MAX_VALUE);
        }

        /**
         * Creates a new <code>IsCollectionOf</code> which accepts a
         * collection of minTimes - maxTimes elements which themselves
         * conform to the specified constraint.
         *
         * @param clazz a <code>Class</code> of collection.
         * @param elementType a <code>PropertyConstraint</code> to
         * constrain members of the collection.
         * @param minTimes an <code>int</code> which is the minimum
         * number of conforming elements for the collection to be
         * accepted.
         * @param maxTimes an <code>int</code> which is the maximum
         * number of conforming elements for the collection to be
         * accepted.
         *
         * @exception IllegalArgumentException if an error occurs.
         */
        public IsCollectionOf(Class clazz,
                              PropertyConstraint elementType,
                              int minTimes,
                              int maxTimes) throws IllegalArgumentException {
            if (! Collection.class.isAssignableFrom(clazz) ||
                java.lang.reflect.Modifier.isAbstract(clazz.getModifiers()) ||
                java.lang.reflect.Modifier.isInterface(clazz.getModifiers())) {
                throw new IllegalArgumentException("Class must be a non-virtual collection");
            }
            this.clazz = clazz;
            this.elementType = elementType;
            this.minTimes = minTimes;
            this.maxTimes = maxTimes;
        }
 
        /**
         * <code>getElementType</code> returns the constraint on
         * element type in the collection.
         *
         * @return a <code>PropertyConstraint</code>.
         */
        public PropertyConstraint getElementType() {
            return elementType;
        }

        /**
         * <code>getMinTimes</code> returns the minumum number of
         * conforming elements for the collection to be accepted.
         *
         * @return an <code>int</code>.
         */
        public int getMinTimes() {
            return minTimes;
        }

        /**
         * <code>getMaxTimes</code> returns the maximum number of
         * conforming elements for the collection to be accepted.
         *
         * @return an <code>int</code> value.
         */
        public int getMaxTimes() {
            return maxTimes;
        }

        /**
         * <code>getCollectionClass</code> returns the Java class of
         * the conforming collection.
         *
         * @return a <code>Class</code>.
         */
        protected Class getCollectionClass() {
            return clazz;
        }

        public boolean accept(Object item) {
            if (item instanceof Collection) {
                Collection c = (Collection) item;
                int size = c.size();
                return
                    (size >= minTimes) &&
                    (size <= maxTimes) &&
                    getCollectionClass().isInstance(item);
            }

            return false;
        }

        public boolean subConstraintOf(PropertyConstraint subC) {
            if (subC instanceof IsCollectionOf) {
                IsCollectionOf ico = (IsCollectionOf) subC;
                return
                    (minTimes <= ico.getMinTimes()) &&
                    (maxTimes >= ico.getMaxTimes()) &&
                    (elementType.subConstraintOf(ico.getElementType())) &&
                    (getCollectionClass().isAssignableFrom(ico.getCollectionClass()));
            }

            return false;
        }

        /**
         * <p><code>setProperty</code> sets a property in the Annotation
         * such that it conforms to the constraint. For example, you
         * create an Annotation having a key which is the String
         * "gene_synonyms" and corresponding value which has a
         * PropertyConstraint indicating that it must be an HashSet of
         * between 1 and 10 Strings. To add a new String "xylR" to the Set
         * you would call the method thus:
         * <code>setProperty(annotationObj, "gene_synonyms",
         * "xylR")</code>.</p>
         *
         * <p>If the specified property does not exist, a new, empty
         * Collection instance is created automatically to hold the
         * value you are adding.</p>
         *
         * <p>If size constraints have been set on the collection the
         * addition may be vetoed.</p>
         *
         * @param ann an <code>Annotation</code> to populate.
         * @param property an <code>Object</code> under which to add the
         * value.
         * @param value an <code>Object</code> to add.
         * @exception ChangeVetoException if an error occurs.
         */
        public void setProperty(Annotation ann, Object property, Object value)
            throws ChangeVetoException {
            if (getElementType().accept(value)) {
                Collection c;
                if (ann.containsProperty(property)) {
                    c = (Collection) ann.getProperty(property);
                } else {
                    try {
                        c = (Collection) getCollectionClass().newInstance();
                        ann.setProperty(property, c);
                    } catch (Exception e) {
                        throw new ChangeVetoException(e, "Can't create collection resource");
                    }
                }

                if (c.size() == maxTimes)
                    throw new ChangeVetoException("Maximum elements ("
                                                  + maxTimes + ") reached");

                c.add(value);
            } else {
                throw new ChangeVetoException("Incorrect element type");
            }
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
         * @param values a <code>Set</code>.
         */
        public Enumeration(Set values) {
            this.values = values;
        }

        /**
         * <code>getValues</code> returns the set of values which
         * constrain the property.
         *
         * @return a <code>Set</code> value.
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
            }

            return false;
        }

        public void setProperty(Annotation ann, Object property, Object value)
            throws ChangeVetoException {
            if (accept(property)) {
                ann.setProperty(property, value);
            } else {
                throw new ChangeVetoException("Value not accepted");
            }
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
}
