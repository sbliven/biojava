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
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.LocationTools;

/**
 * <p>A type to constrain annotation bundles.</p>
 *
 * <p><code>AnnotationType</code> instances can be used to validate an
 * <code>Annotation</code> to check that it has the appropriate
 * properties and that they are of the right type.</p>
 *
 * <h2>Common Usage</h2>
 *
 * <pre>
 * // annType is going to define ID as being a single String and
 * // AC as being a list of Strings and accept any other properties at all
 * AnnotationType annType = new AnnotationType.Impl();
 * annType.setDefaultConstraints(PropertyConstraint.ANY, Cardinality.ANY)
 * annType.setConstraints("ID",
 *                        new PropertyConstraint.ByClass(String.class),
 *                        CardinalityConstraint.ONE );
 * annType.setConstraint("AC",
 *                       new PropertyConstraint.ByClass(String.class),
 *                       CardinalityConstraint.ANY );
 *
 * Annotation ann = ...;
 *
 * if(annType.accepts(ann)) {
 *   // we have proved that ann contains one ID property that is a String
 *   System.out.println("ID: " + (String) ann.getProperty("ID"));
 *   // we know that the AC property is potentialy more than one String -
 *   // let's use getProperty on AnnotationType to make sure that the
 *   // values get unpacked cleanly
 *   System.out.println("AC:");
 *   for(Iterator i = annType.getProperty(ann, "AC"); i.hasNext(); ) {
 *     System.out.println("\t" + (String) i.next());
 *   }
 * } else {
 *   throw new IllegalArgumentException(
 *     "Expecting an annotation conforming to: "
 *     annType + " but got: " + ann
 *   );
 * }
 * </pre>
 *
 * <h2>Description</h2>
 *
 * <p>AnnotationType is a powerful constraint-based language for describing
 * sets of Annotation bundles. It works by assuming that any given Annotation
 * may have any set of properties defined. If it matches a particular
 * AnnotationType instance, then each defined property must be of a value
 * that is acceptable to the type, and each undefined property must
 * be allowed to be absend in the type.</p>
 *
 * <p>The constraint on any given property is two-fold. Firstly, there is the
 * PropertyConstraint associated with it. This is an interface that will
 * accept or reject any given Java object. Effectively, this can be thought of
 * as a set membership operator over all objects. Seccondly,
 * CardinalityConstrait defines a legal number of values that the property can
 * hold. For example, if you had a car Annotation, you may wish to associate it
 * with exactly 4 values under the Wheel property. CardinalityConstraint is a
 * simple wrapper arround org.biojava.bio.symbol.Location, and it is Location
 * that is used to represent the legal range of cardinalities.
 * CardinalityConstraint provides some usefull standard Location instances such
 * as ZERO, ONE and ANY.</p>
 *
 * <p>It is usually left up to the AnnotationType instance to work out how
 * multiple values should be packed into a single property slot in an Annotation
 * instance. Commonly, things that are allowed a cardinality of 1 will store one
 * value directly in the slot. Things that allow multiple values (and optionaly
 * things with one value) will usualy store them within a Collection in the
 * slot. This complexity is hidden from you if you use the accessor methods
 * built into AnnotationType, setProperty() and getProperty().
 *
 * @since 1.3
 * @author Matthew Pocock
 * @author Keith James (docs)
 * @user Using AnnotationType instances that you have been provided with e.g.
 *       from UnigeneTools.LIBRARY_ANNOTATION
 * @powerUser Make AnnotationType instances that describe what should and
 *            should not appear in an Annotation bundle
 * @powerUser Constrain FeatureFilter schemas by Annotation associated with
 *            the features
 * @powerUser Provide meta-data to the tag-value parser for automatically
 *            generating object representations of flat-files
 * @developer Implementing your own AnnotationType implementations to reflect
 *            frame, schema or ontology definitions. For example, dynamically
 *            reflect an RDMBS schema or DAML/Oil deffinition as an
 *            AnnotationType.
 */
public interface AnnotationType {
    /**
     * The type that accepts all annotations and is the supertype of all
     * other annotations. Only an empty annotation is an exact instance of
     * this type.
     *
     * @user Use this whenever an AnnotationType is needed by an API and you
     *       don't want to constrain anything
     */
    public static final AnnotationType ANY = new Impl(
      PropertyConstraint.ANY,
      CardinalityConstraint.ANY
    );
    
    /**
     * The type that accepts no annotations at all and is the subtype of all
     * other annotations.
     *
     * @user Use this whenever an AnnotationType is needed by an API and you
     *       want to make sure that all Annotation objects get rejected
     */
    public static final AnnotationType NONE = new Impl(
      PropertyConstraint.NONE,
      CardinalityConstraint.NONE
    );

    /**
     * Validate an Annotation against this AnnotationType.
     *
     * @user Any time you wish to see if an Annotation bundle conforms to a
     *       type
     * @param ann the Annotation to validate.
     * @return true if ann conforms to this type and false if it doesn't.
     */
    public boolean instanceOf(Annotation ann);

    /**
     * <p>See if an AnnotationType is a specialisation of this type.</p>
     *
     * <p>An AnnotationType is a sub-type if it restricts each of the
     * properties of the super-type to a type that can be cast to the
     * type in the super-type. Note that this is not always a cast in
     * the pure Java sense; it may include checks on the number and
     * type of members in collections or other criteria.</p>
     *
     * @powerUser If you wish to check that one type is a more constrained
     *            version of another
     * @param subType an AnnotationType to check.
     * @return true if subType is a sub-type of this type.
     */
    public boolean subTypeOf(AnnotationType subType);

    /**
     * <p>Retrieve the constraint that will be applied to all
     * properties with a given key.</p>
     *
     * <p>For an <code>Annotation</code> to be accepted, each key in
     * getProperties() must be present in the annotation and each of the
     * values associated with those properties must match the
     * constraint.</p>
     *
     * @powerUser If you want to find out exactly what constraints will be
     *            applied to a particular propery key
     * @param key the property to be validated.
     * @return PropertyConstraint the constraint by which the values
     * must be accepted.
     */
    public PropertyConstraint getPropertyConstraint(Object key);

    /**
     * <p>Retrieve the cardinality constraint associated with properties.</p>
     *
     * <p>For an annotation to be acceptable, the property must have a number
     * of values that matches the cardinality constraint. Common values are
     * represented by static fields of Location.</p>
     *
     * @powerUser If you want to find out exactly what constraints will be
     *            applied to a particular propery key
     * @param key the property to be validated
     * @return a Location giving the number of values assocated
     *         with the property
     */
    public Location getCardinalityConstraint(Object key);

    /**
     * Set the constraints associated with a property.
     *
     * @powerUser When you are building your own AnnotationType
     * @param key  the name of the property to constrain
     * @param con  the PropertyConstraint to enforce
     * @param card the CardinalityConstraint to enforce
     */
    public void setConstraints(
      Object key,
      PropertyConstraint con,
      Location card
    );

    /**
     * Set the constraints that will apply to all properties without an
     * explicitly defined set of constraints.
     *
     * @powerUser When you are building your own AnnotationType
     * @param pc  the default PropertyConstraint
     * @param cc the default CardinalityConstraint
     */
    public void setDefaultConstraints(PropertyConstraint pc, Location cc);
    
    /**
     * Get the PropertyConstraint that will be applied to all properties without
     * an explicit binding. This defaults to PropertyConstraint.ALL.
     *
     * @powerUser If you want to find out exactly what constraint will be
     *            applied to properties with no explicitly defined constraints
     * @return the default PropertyConstraint
     */
    public PropertyConstraint getDefaultPropertyConstraint();

    /**
     * Get the CardinalityConstraint that will be applied to all properties without
     * an explicit binding. This defaults to CardinalityConstraint.ALL.
     *
     * @powerUser If you want to find out exactly what constraint will be
     *            applied to properties with no explicitly defined constraints
     * @return the default CardinalityConstraint
     */
    public Location getDefaultCardinalityConstraint();

    /**
     * <p>Retrieve the complete set of properties that must be present for
     * an <code>Annotation</code> to be accepted by this
     * <code>AnnotationType</code>.</p>
     *
     * @return the Set of properties to validate.
     * @powerUser Discover which properties have explicit constraints
     */
    public Set getProperties();

    /**
     * Set the property in an annotation bundle according to the type we believe
     * it should be. This will take care of any neccisary packing or unpacking
     * to Collections.
     *
     * @param ann  the Annotation to modify
     * @param property  the property key Object
     * @param value  the property value Object
     * @throws ChangeVetoException  if the value could not be accepted by this
     *         annotation type for that property key, or if the Annotation could
     *         not be modified
     * @user Edit an Annotation bundle in a way compattible with this
     *       AnnotationType
     */
    public void setProperty(Annotation ann, Object property, Object value)
        throws ChangeVetoException;

    /**
     * Get the Collection of values associated with an Annotation bundle
     * according to the type we believe it to be. This will take care of any
     * neccisary packing or unpacking to Collections. Properties with no values
     * will return empty Collections.
     *
     * @param ann  the Annotatoin to modify
     * @param property  the property key Object
     * @return a Collection of values
     * @throws ChangeVetoException  if the value could not be removed
     * @user Edit an Annotation bundle in a way compattible with this
     *       AnnotationType
     */
    public Collection getProperty(Annotation ann, Object property)
        throws ChangeVetoException;
        
    /**
     * Remove a property key, value pair from an Annotaiton instance. This will
     * take care of any neccisary packing or unpacking to Collections.
     *
     * @param ann  the Annotation to modify
     * @param property  the property key Object
     * @param value  the property value Object
     * @throws ChangeVetoException  if the Annotation could
     *         not be modified
     * @user Edit an Annotation bundle in a way compattible with this
     *       AnnotationType
     */
    public void removeProperty(Annotation ann, Object property, Object value)
        throws ChangeVetoException;
    
    /**
     * <p>An abstract base class useful for retrieving AnnotationType
     * instances.</p>
     *
     * <p>This provides deffinitions for the logical operators (validate(),
     * subTypeOf()), the mutators (setProperty(), getProperty() and
     * deleteProperty()) and toString() that you may not want to
     * write yourself. It leaves the data-related methods up to you.</p>
     *
     * @developer  When implementing AnnotationType
     * @since 1.3
     * @author Matthew Pocock
     */
    public abstract class Abstract
    implements AnnotationType {
        public boolean instanceOf(Annotation ann) {
          Set props = new HashSet();
          props.addAll(getProperties());
          props.addAll(ann.keys());

          for (Iterator i = getProperties().iterator(); i.hasNext();) {
            Object key = i.next();
            PropertyConstraint con = getPropertyConstraint(key);
            Location card = getCardinalityConstraint(key);

            if(!validate(ann, key, con, card)) {
              return false;
            }
          }

          return true;
        }

        private boolean validate(
          Annotation ann,
          Object key,
          PropertyConstraint con,
          Location card
        ) {
          if(
            CardinalityConstraint.ZERO.equals(card) &&
            !ann.containsProperty(key)
          ) {
            return true;
          } else if(
            CardinalityConstraint.ONE.equals(card) &&
            ann.containsProperty(key) &&
            con.accept(ann.getProperty(key))
          ) {
            return true;
          } else if(
            CardinalityConstraint.ZERO_OR_ONE.equals(card) && (
              !ann.containsProperty(key) ||
              con.accept(ann.getProperty(key))
            )
          ) {
            return true;
          } else {
            if(!ann.containsProperty(key)) {
              return card.contains(0);
            } else {
              Object val = ann.getProperty(key);
              if(val instanceof Collection) {
                Collection vals = (Collection) val;
                if(!card.contains(vals.size())) {
                  return false;
                }
                for(Iterator i = vals.iterator(); i.hasNext(); ) {
                  if(!con.accept(i.next())) {
                    return false;
                  }
                }
                return true;
              } else {
                  if (card.contains(1)) {
                      return con.accept(val);
                  } else {
                      return false;
                  }
              }
            }
          }
        }

        public final void setProperty(
          Annotation ann,
          Object property,
          Object value
        ) throws ChangeVetoException {
          try {
            PropertyConstraint prop = getPropertyConstraint(property);
            Location card = getCardinalityConstraint(property);
            if(card.getMax() > 1) {
              Collection vals = null;
              if(ann.containsProperty(property)) {
                vals = (Collection) ann.getProperty(property);
              } else {
                vals = new ArrayList();
                ann.setProperty(property, vals);
              }
              prop.addValue(vals, value);
            } else {
              prop.setProperty(ann, property, value);
            }
          } catch (ChangeVetoException cve) {
            throw new ChangeVetoException(cve, "Failed to change property " + property);
          }
        }
        
        public final Collection getProperty(Annotation ann, Object property)
        throws ChangeVetoException {
          Collection vals = null;
          
          if(!ann.containsProperty(property)) {
            vals = Collections.EMPTY_SET;
          } else {
            Location card = getCardinalityConstraint(property);
            Object val = ann.getProperty(property);
            if(card.getMax() > 1 && val instanceof Collection) {
              vals = (Collection) val;
            } else {
              vals = Collections.singleton(ann.getProperty(property));
            }
          }
          
          return vals;
        }
        
        public final void removeProperty(
          Annotation ann,
          Object key,
          Object value
        ) throws ChangeVetoException {
          if(!ann.containsProperty(key)) {
            throw new ChangeVetoException("No values associated with " + key +
              " in " + ann);
          }
          
          Object val = ann.getProperty(key);
          if(val == value) {
            ann.removeProperty(key);
          } else if(val instanceof Collection) {
            ((Collection) val).remove(value);
          } else {
            throw new ChangeVetoException("Don't know how to remove " +
              value + " from " + val);
          }
        }
        
        public String toString() {
          StringBuffer sb = new StringBuffer("AnnotationType: {");
          
          for(Iterator i = getProperties().iterator(); i.hasNext(); ) {
            Object key = i.next();
            PropertyConstraint pc = getPropertyConstraint(key);
            Location cc = getCardinalityConstraint(key);
            
            sb.append(" [" + key + ", " + pc + ", " + cc + "]");
          }
          sb.append(" [*, " + getDefaultPropertyConstraint() + ", " + getDefaultCardinalityConstraint() + "]");
          
          sb.append(" }");
          
          return sb.toString();
        }

        public boolean subTypeOf(AnnotationType subType) {
            Set props = new HashSet();
            props.addAll(getProperties());
            props.addAll(subType.getProperties());

            for (Iterator i = props.iterator(); i.hasNext();) {
                Object key = i.next();
         
                PropertyConstraint thisPC = getPropertyConstraint(key);
                PropertyConstraint subPC = subType.getPropertyConstraint(key);
                if (! thisPC.subConstraintOf(subPC)) {
                    return false;
                }

                Location thisCC = getCardinalityConstraint(key);
                Location subCC = subType.getCardinalityConstraint(key);
                if (!LocationTools.contains(thisCC, subCC)) {
                  return false;
                }
            }

            return true;
        }
    }

    /**
     * <p>An implementation of <code>AnnotationType</code>.</p>
     *
     * <p>To build an instance of <code>AnnotationType.Impl</code>,
     * first invoke the no-args constructor, and then use the
     * setPropertyConstraint method to build the property->constraint
     * mapping.</p>
     *
     * @powerUser A convenient class for when you need an AnnotationType
     *            instance and don't want to write your own
     * @since 1.3
     * @author Matthew Pocock
     */
    public class Impl extends AnnotationType.Abstract {
        private Map cons;
        private Map cards;
        
        private PropertyConstraint unknownPC;
        private Location unknownCC;

        /**
         * Create a new Impl with no constraints.
         */
        public Impl() {
            cons = new SmallMap();
            cards = new SmallMap();
            unknownPC = PropertyConstraint.ANY;
            unknownCC = CardinalityConstraint.ANY;
        }

        /**
         * Create a new Impl with a default property and cardinality constraint.
         *
         * @param defaultPC  the default PropertyConstraint
         * @param defaultCC  the default CardinalityConstraint
         */
        public Impl(PropertyConstraint defaultPC, Location defaultCC) {
            this();
            setDefaultConstraints(defaultPC, defaultCC);
        }

        public void setDefaultConstraints(PropertyConstraint pc, Location cc) {
          unknownPC = pc;
          unknownCC = cc;
        }
        
        public PropertyConstraint getDefaultPropertyConstraint() {
          return unknownPC;
        }
        
        public Location getDefaultCardinalityConstraint() {
          return unknownCC;
        }

        public PropertyConstraint getPropertyConstraint(Object key) {
            PropertyConstraint pc = (PropertyConstraint) cons.get(key);
            if (pc == null) {
                pc = unknownPC;
            }
            return pc;
        }

        public Location getCardinalityConstraint(Object key) {
            Location card = (Location) cards.get(key);
            if (card == null) {
                card = unknownCC;
            }
            return card;
        }

        public void setConstraints(
          Object key,
          PropertyConstraint con,
          Location card
        ) {
            cons.put(key, con);
            cards.put(key, card);
        }

        public Set getProperties() {
            return cons.keySet();
        }
    }
}
