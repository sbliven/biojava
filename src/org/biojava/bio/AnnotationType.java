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
 * @since 1.3
 * @author Matthew Pocock
 * @author Keith James (docs).
 */
public interface AnnotationType {
    /**
     * The type that accepts all annotations and is the supertype of all
     * other annotations. Only an empty annotation is an exact instance of
     * this type.
     */
    public static final AnnotationType ANY = new Impl(
      PropertyConstraint.ANY,
      CardinalityConstraint.ANY
    );
    
    /**
     * The type that accepts no annotations at all and is the subtype of all
     * other annotations.
     */
    public static final AnnotationType NONE = new Impl(
      PropertyConstraint.NONE,
      CardinalityConstraint.NONE
    );

    /**
     * Validate an Annotation against this AnnotationType.
     *
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
     * @param key the property to be validated
     * @return a Location giving the number of values assocated
     *         with the property
     */
    public Location getCardinalityConstraint(Object key);

    /**
     * Set the constraints associated with a property.
     *
     * @param key  the name of the property to constrain
     * @param con  the PropertyConstraint to enforce
     * @param card the CardinalityCnstraint to enforce
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
     * @param pc  the default PropertyConstraint
     * @param cc the default CardinalityConstraint
     */
    public void setDefaultConstraints(PropertyConstraint pc, Location cc);
    
    /**
     * Get the PropertyConstraint that will be applied to all properties without
     * an explicit binding. This defaults to PropertyConnstraint.ALL.
     *
     * @return the default PropertyConstraint
     */
    public PropertyConstraint getDefaultPropertyConstraint();

    /**
     * Get the CardinalityConstraint that will be applied to all properties without
     * an explicit binding. This defaults to CardinalityConstraint.ALL.
     *
     * @return the default CardinalityConstraint
     */
    public Location getDefaultCardinalityConstraint();

    /**
     * <p>Retrieve the complete set of properties that must be present for
     * an <code>Annotation</code> to be accepted by this
     * <code>AnnotationType</code>.</p>
     *
     * @return the Set of properties to validate.
     */
    public Set getProperties();

    /**
     * Set the property in an annotation bundle according to the type we believe
     * it should be.
     */
    public void setProperty(Annotation ann, Object property, Object value)
        throws ChangeVetoException;

    /**
     * <p>An abstract base class useful for retrieving AnnotationType
     * instances</p>
     *
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

        public final void setProperty(Annotation ann, Object property, Object value)
        throws ChangeVetoException {
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
