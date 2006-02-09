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

package org.biojavax.bio.db.biosql;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.biojava.bio.BioRuntimeException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FilterUtils;
import org.biojava.utils.walker.WalkerFactory;
import org.biojavax.Note;
import org.biojavax.RichAnnotation;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichLocation.Tools;


/**
 * A filter for accepting or rejecting a feature.
 *
 * <p>
 * It is possible to write custom <code>FeatureFilter</code>s by implementing this
 * interface.  There are also a wide range of built-in features, and it is possible
 * to build complex queries using <code>FeatureFilter.And</code>, <code>FeatureFilter.Or</code>,
 * and <code>FeatureFilter.Not</code>.  Where possible, use of the built-in filters
 * is preferable to writing new filters, since the methods in the <code>FilterUtils</code>
 * class have access to special knowledge about the built-in filter types and how they
 * relate to one another.
 * </p>
 *
 * <p>
 * If the filter is to be used in a remote process, it is recognized that it may
 * be serialized and sent over to run remotely, rather than each feature being
 * retrieved locally.
 * </p>
 *
 * <p>
 * This class requires the Hibernate JAR files to be on your classpath at runtime. It is
 * designed ONLY for use with BioSQLRichSequenceDB and BioSQLBioEntryDB.
 * </p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 * @author Richard Holland
 * @since 1.5
 */

public interface BioSQLFeatureFilter extends FeatureFilter {
    
    /**
     * This method returns a Hibernate Criterion object that can be used to
     * query the database.
     */
    public Object asCriterion();
    
    /**
     * Returns true if the criterion returned by asCriterion refers to the parent
     * sequence of this feature.
     */
    public boolean criterionRefersToParent();
    
    /**
     * Returns true if the criterion returned by asCriterion refers to the
     * location of this feature.
     */
    public boolean criterionRefersToLocation();
    
    /**
     * Returns true if the criterion returned by asCriterion refers to the
     * annotations of this feature.
     */
    public boolean criterionRefersToAnnotation();
    
    /**
     * All features are selected by this filter.
     */
    static final public BioSQLFeatureFilter all = new BioSQLAcceptAllFilter();
    
    /**
     * No features are selected by this filter.
     */
    static final public BioSQLFeatureFilter none = new BioSQLAcceptNoneFilter();
    
    /**
     *  A filter that returns all features not accepted by a child filter.
     *
     * @author Thomas Down
     * @author Matthew Pocock
     * @author Richard Holland
     * @since 1.5
     */
    public final static class Not implements BioSQLFeatureFilter {
        static { WalkerFactory.getInstance().addTypeWithParent(Not.class); }
        
        BioSQLFeatureFilter child;
        private Method not;
        
        public BioSQLFeatureFilter getChild() {
            return child;
        }
        
        public Not(BioSQLFeatureFilter child) {
            if (!(child instanceof BioSQLFeatureFilter))
                throw new BioRuntimeException("Cannot use non-BioSQLFeatureFilter instances with this class");
            this.child = child;
            try {
                // Lazy load the Restrictions class from Hibernate.
                Class restrictions = Class.forName("org.hibernate.criterion.Restrictions");
                // Lazy load the Criterion class from Hibernate.
                Class criterion = Class.forName("org.hibernate.Criterion");
                // Lookup the methods
                this.not = restrictions.getMethod("not", new Class[]{criterion});
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        
        public boolean accept(Feature f) {
            return !(child.accept(f));
        }
        
        public Object asCriterion() {
            try {
                return this.not.invoke(null,new Object[]{child.asCriterion()});
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        public boolean criterionRefersToParent() { return child.criterionRefersToParent(); }
        
        public boolean criterionRefersToLocation() { return child.criterionRefersToLocation(); }
        
        public boolean criterionRefersToAnnotation() { return false; }
        
        public boolean equals(Object o) {
            return
                    (o instanceof Not) &&
                    (((Not) o).getChild().equals(this.getChild()));
        }
        
        public int hashCode() {
            return getChild().hashCode();
        }
        
        public String toString() {
            return "Not(" + child + ")";
        }
    }
    
    
    /**
     *  A filter that returns all features accepted by both child filter.
     *
     * @author Thomas Down
     * @author Matthew Pocock
     * @author Richard Holland
     * @since 1.5
     */
    public final static class And implements BioSQLFeatureFilter {
        static { WalkerFactory.getInstance().addTypeWithParent(And.class); }
        
        BioSQLFeatureFilter c1, c2;
        private Method and;
        
        public BioSQLFeatureFilter getChild1() {
            return c1;
        }
        
        public BioSQLFeatureFilter getChild2() {
            return c2;
        }
        
        public And(BioSQLFeatureFilter c1, BioSQLFeatureFilter c2) {
            if (!(c1 instanceof BioSQLFeatureFilter) || !(c2 instanceof BioSQLFeatureFilter))
                throw new BioRuntimeException("Cannot use non-BioSQLFeatureFilter instances with this class");
            this.c1 = c1;
            this.c2 = c2;
            try {
                // Lazy load the Restrictions class from Hibernate.
                Class restrictions = Class.forName("org.hibernate.criterion.Restrictions");
                // Lazy load the Criterion class from Hibernate.
                Class criterion = Class.forName("org.hibernate.Criterion");
                // Lookup the methods
                this.and = restrictions.getMethod("and", new Class[]{criterion,criterion});
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        
        public boolean accept(Feature f) {
            return (c1.accept(f) && c2.accept(f));
        }
        
        public Object asCriterion() {
            try {
                return this.and.invoke(null,new Object[]{c1.asCriterion(),c2.asCriterion()});
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        public boolean criterionRefersToParent() { return c1.criterionRefersToParent() || c2.criterionRefersToParent(); }
        
        public boolean criterionRefersToLocation() { return c1.criterionRefersToLocation() || c2.criterionRefersToLocation(); }
        
        public boolean criterionRefersToAnnotation() { return false; }
        
        public boolean equals(Object o) {
            if(o instanceof BioSQLFeatureFilter) {
                return FilterUtils.areEqual(this, (FeatureFilter) o);
            } else {
                return false;
            }
        }
        
        public int hashCode() {
            return getChild1().hashCode() ^ getChild2().hashCode();
        }
        
        public String toString() {
            return "And(" + c1 + " , " + c2 + ")";
        }
    }
    
    /**
     *  A filter that returns all features accepted by at least one child filter.
     *
     * @author Thomas Down
     * @author Matthew Pocock
     * @author Richard Holland
     * @since 1.5
     */
    public final static class Or implements BioSQLFeatureFilter {
        static { WalkerFactory.getInstance().addTypeWithParent(Or.class); }
        
        BioSQLFeatureFilter c1, c2;
        private Method or;
        
        public BioSQLFeatureFilter getChild1() {
            return c1;
        }
        
        public BioSQLFeatureFilter getChild2() {
            return c2;
        }
        
        public Or(BioSQLFeatureFilter c1, BioSQLFeatureFilter c2) {
            if (!(c1 instanceof BioSQLFeatureFilter) || !(c2 instanceof BioSQLFeatureFilter))
                throw new BioRuntimeException("Cannot use non-BioSQLFeatureFilter instances with this class");
            this.c1 = c1;
            this.c2 = c2;
            try {
                // Lazy load the Restrictions class from Hibernate.
                Class restrictions = Class.forName("org.hibernate.criterion.Restrictions");
                // Lazy load the Criterion class from Hibernate.
                Class criterion = Class.forName("org.hibernate.Criterion");
                // Lookup the methods
                this.or = restrictions.getMethod("or", new Class[]{criterion,criterion});
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        
        public boolean accept(Feature f) {
            return (c1.accept(f) || c2.accept(f));
        }
        
        public Object asCriterion() {
            try {
                return this.or.invoke(null,new Object[]{c1.asCriterion(),c2.asCriterion()});
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        public boolean criterionRefersToParent() { return c1.criterionRefersToParent() || c2.criterionRefersToParent(); }
        
        public boolean criterionRefersToLocation() { return c1.criterionRefersToLocation() || c2.criterionRefersToLocation(); }
        
        public boolean criterionRefersToAnnotation() { return false; }
        
        public boolean equals(Object o) {
            if(o instanceof BioSQLFeatureFilter) {
                return FilterUtils.areEqual(this, (FeatureFilter) o);
            } else {
                return false;
            }
        }
        
        public int hashCode() {
            return getChild1().hashCode() ^ getChild2().hashCode();
        }
        
        public String toString() {
            return "Or(" + c1 + " , " + c2 + ")";
        }
    }
    
    /**
     * Construct one of these to filter features by display name.
     *
     * @author Richard Holland
     * @since 1.5
     */
    final public static class ByName implements BioSQLFeatureFilter {
        private String name;
        private Method eq;
        
        public String getName() {
            return name;
        }
        
        /**
         * Create a ByType filter that filters in all features with type fields
         * equal to type.
         *
         * @param type  the String to match type fields against
         */
        public ByName(String name) {
            if (name == null) {
                throw new NullPointerException("Name may not be null");
            }
            this.name = name;
            try {
                // Lazy load the Restrictions class from Hibernate.
                Class restrictions = Class.forName("org.hibernate.criterion.Restrictions");
                // Lookup the methods
                this.eq = restrictions.getMethod("eq", new Class[]{String.class,Object.class});
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        
        /**
         * Returns true if the feature has a matching type property.
         */
        public boolean accept(Feature f) {
            if (!(f instanceof RichFeature)) throw new BioRuntimeException("Cannot use ByName on non-RichFeature instances");
            return name.equals(((RichFeature)f).getName());
        }
        
        public Object asCriterion() {
            try {
                return this.eq.invoke(null,new Object[]{"name",name});
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        public boolean criterionRefersToParent() { return false; }
        
        public boolean criterionRefersToLocation() { return false; }
        
        public boolean criterionRefersToAnnotation() { return false; }
        
        public boolean equals(Object o) {
            return
                    (o instanceof ByName) &&
                    (((ByName) o).getName().equals(this.getName()));
        }
        
        public int hashCode() {
            return getName().hashCode();
        }
        
        public String toString() {
            return "ByName(" + name + ")";
        }
    }
    
    /**
     * Construct one of these to filter features by rank.
     *
     * @author Richard Holland
     * @since 1.5
     */
    final public static class ByRank implements BioSQLFeatureFilter {
        private int rank;
        private Method eq;
        
        public int getRank() {
            return rank;
        }
        
        /**
         * Create a ByType filter that filters in all features with type fields
         * equal to type.
         *
         * @param type  the String to match type fields against
         */
        public ByRank(int rank) {
            this.rank = rank;
            try {
                // Lazy load the Restrictions class from Hibernate.
                Class restrictions = Class.forName("org.hibernate.criterion.Restrictions");
                // Lookup the methods
                this.eq = restrictions.getMethod("eq", new Class[]{String.class,Object.class});
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        
        /**
         * Returns true if the feature has a matching type property.
         */
        public boolean accept(Feature f) {
            if (!(f instanceof RichFeature)) throw new BioRuntimeException("Cannot use ByName on non-RichFeature instances");
            return rank==((RichFeature)f).getRank();
        }
        
        public Object asCriterion() {
            try {
                return this.eq.invoke(null,new Object[]{"rank",new Integer(rank)});
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        public boolean criterionRefersToParent() { return false; }
        
        public boolean criterionRefersToLocation() { return false; }
        
        public boolean criterionRefersToAnnotation() { return false; }
        
        public boolean equals(Object o) {
            return
                    (o instanceof ByRank) &&
                    (((ByRank) o).getRank() == this.getRank());
        }
        
        public int hashCode() {
            return rank;
        }
        
        public String toString() {
            return "ByRank(" + rank + ")";
        }
    }
    
    /**
     * Construct one of these to filter features by type.
     *
     * @author Matthew Pocock
     * @author Richard Holland
     * @since 1.5
     */
    final public static class ByTypeTerm implements BioSQLFeatureFilter {
        private String typeTerm;
        private Method eq;
        
        public String getTypeTerm() {
            return typeTerm;
        }
        
        /**
         * Create a ByType filter that filters in all features with type fields
         * equal to type.
         *
         * @param type  the String to match type fields against
         */
        public ByTypeTerm(String typeTerm) {
            if (typeTerm == null) {
                throw new NullPointerException("Type may not be null");
            }
            this.typeTerm = typeTerm;
            try {
                // Lazy load the Restrictions class from Hibernate.
                Class restrictions = Class.forName("org.hibernate.criterion.Restrictions");
                // Lookup the methods
                this.eq = restrictions.getMethod("eq", new Class[]{String.class,Object.class});
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        
        /**
         * Returns true if the feature has a matching type property.
         */
        public boolean accept(Feature f) {
            return typeTerm.equals(f.getTypeTerm());
        }
        
        public Object asCriterion() {
            try {
                return this.eq.invoke(null,new Object[]{"typeTerm",typeTerm});
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        public boolean criterionRefersToParent() { return false; }
        
        public boolean criterionRefersToLocation() { return false; }
        
        public boolean criterionRefersToAnnotation() { return false; }
        
        public boolean equals(Object o) {
            return
                    (o instanceof ByTypeTerm) &&
                    (((ByTypeTerm) o).getTypeTerm().equals(this.getTypeTerm()));
        }
        
        public int hashCode() {
            return getTypeTerm().hashCode();
        }
        
        public String toString() {
            return "ByTypeTerm(" + typeTerm + ")";
        }
    }
    
    
    /**
     * Construct one of these to filter features by source.
     *
     * @author Matthew Pocock
     * @author Richard Holland
     * @since 1.5
     */
    final public static class BySourceTerm implements BioSQLFeatureFilter {
        private String sourceTerm;
        private Method eq;
        
        public String getSourceTerm() {
            return sourceTerm;
        }
        
        /**
         * Create a ByType filter that filters in all features with type fields
         * equal to type.
         *
         * @param type  the String to match type fields against
         */
        public BySourceTerm(String typeTerm) {
            if (sourceTerm == null) {
                throw new NullPointerException("Source may not be null");
            }
            this.sourceTerm = sourceTerm;
            try {
                // Lazy load the Restrictions class from Hibernate.
                Class restrictions = Class.forName("org.hibernate.criterion.Restrictions");
                // Lookup the methods
                this.eq = restrictions.getMethod("eq", new Class[]{String.class,Object.class});
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        
        /**
         * Returns true if the feature has a matching source property.
         */
        public boolean accept(Feature f) {
            return sourceTerm.equals(f.getSourceTerm());
        }
        
        public Object asCriterion() {
            try {
                return this.eq.invoke(null,new Object[]{"sourceTerm",sourceTerm});
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        public boolean criterionRefersToParent() { return false; }
        
        public boolean criterionRefersToLocation() { return false; }
        
        public boolean criterionRefersToAnnotation() { return false; }
        
        public boolean equals(Object o) {
            return
                    (o instanceof BySourceTerm) &&
                    (((BySourceTerm) o).getSourceTerm().equals(this.getSourceTerm()));
        }
        
        public int hashCode() {
            return getSourceTerm().hashCode();
        }
        
        public String toString() {
            return "BySourceTerm(" + sourceTerm + ")";
        }
    }
    
    /**
     * Accept features that reside on a sequence with a particular name.
     *
     * @author Matthew Pocock
     * @author Richard Holland
     * @since 1.5
     */
    public final static class BySequenceName
            implements BioSQLFeatureFilter {
        private String seqName;
        private Method eq;
        
        public BySequenceName(String seqName) {
            this.seqName = seqName;
            try {
                // Lazy load the Restrictions class from Hibernate.
                Class restrictions = Class.forName("org.hibernate.criterion.Restrictions");
                // Lookup the methods
                this.eq = restrictions.getMethod("eq", new Class[]{String.class,Object.class});
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        
        public String getSequenceName() {
            return seqName;
        }
        
        public boolean accept(Feature f) {
            return f.getSequence().getName().equals(seqName);
        }
        
        public Object asCriterion() {
            try {
                return this.eq.invoke(null,new Object[]{"p.name",seqName});
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        public boolean criterionRefersToParent() { return true; }
        
        public boolean criterionRefersToLocation() { return false; }
        
        public boolean criterionRefersToAnnotation() { return false; }
        
        public boolean equals(Object o) {
            return
                    (o instanceof BySequenceName) &&
                    ((BySequenceName) o).getSequenceName().equals(seqName);
        }
        
        public int hashCode() {
            return seqName.hashCode();
        }
    }
    
    /**
     * A filter that returns all features contained within a location. Contained means
     * that a feature is entirely within, on the same strand and on the same sequence
     * as any single member of the flattened query location.
     *
     * @author Matthew Pocock
     * @author Richard Holland
     * @since 1.5
     */
    public final static class ContainedByRichLocation implements BioSQLFeatureFilter {
        private RichLocation loc;
        private Method eq;
        private Method le;
        private Method ge;
        private Method conjunction;
        private Method disjunction;
        private Method conjunctAdd;
        private Method disjunctAdd;
        
        public RichLocation getRichLocation() {
            return loc;
        }
        
        /**
         * Creates a filter that returns everything contained within loc.
         *
         * @param loc  the location that will contain the accepted features
         */
        public ContainedByRichLocation(RichLocation loc) {
            if (loc == null) {
                throw new NullPointerException("Loc may not be null");
            }
            this.loc = loc;
            try {
                // Lazy load the Restrictions class from Hibernate.
                Class restrictions = Class.forName("org.hibernate.criterion.Restrictions");
                // Lookup the methods
                this.eq = restrictions.getMethod("eq", new Class[]{String.class,Object.class});
                this.le = restrictions.getMethod("le", new Class[]{String.class,Object.class});
                this.ge = restrictions.getMethod("ge", new Class[]{String.class,Object.class});
                this.conjunction = restrictions.getMethod("conjunction", new Class[]{});
                this.disjunction = restrictions.getMethod("disjunction", new Class[]{});
                // Lazy load the Restrictions class from Hibernate.
                Class criterion = Class.forName("org.hibernate.criterion.Criterion");
                // Lazy load the Restrictions class from Hibernate.
                Class conjunctClass = Class.forName("org.hibernate.criterion.Conjunction");
                Class disjunctClass = Class.forName("org.hibernate.criterion.Disjunction");
                // Lookup the methods
                this.conjunctAdd = conjunctClass.getMethod("add", new Class[]{criterion});
                this.disjunctAdd = disjunctClass.getMethod("add", new Class[]{criterion});
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        
        /**
         * Returns true if the feature is within this filter's location.
         */
        public boolean accept(Feature f) {
            return loc.contains(f.getLocation());
        }
        
        public Object asCriterion() {
            try {
                // Conjunction of criteria for each member of the query location.
                Collection members = Tools.flatten(loc);
                // some combo of Tools.flatten(loc), min(loc.start,feat.start) and min(loc.end,feat.end)
                Object parentConjunct = this.conjunction.invoke(null,null);
                for (Iterator i = members.iterator(); i.hasNext(); ) {
                    RichLocation loc = (RichLocation)i.next();
                    Object childDisjunct = this.disjunction.invoke(null,null);
                    // for each member, find features that have start>=member.start,
                    // end<=member.end and strand=member.strand and crossref=member.crossref
                    this.disjunctAdd.invoke(childDisjunct,new Object[]{this.eq.invoke(null, new Object[]{"l.strandNum",new Integer(loc.getStrand().intValue())})});
                    this.disjunctAdd.invoke(childDisjunct,new Object[]{this.eq.invoke(null, new Object[]{"l.crossRef",loc.getCrossRef()})});
                    this.disjunctAdd.invoke(childDisjunct,new Object[]{this.ge.invoke(null, new Object[]{"l.min",new Integer(loc.getMin())})});
                    this.disjunctAdd.invoke(childDisjunct,new Object[]{this.le.invoke(null, new Object[]{"l.max",new Integer(loc.getMax())})});
                    // add the member to the set of restrictions
                    this.conjunctAdd.invoke(parentConjunct,new Object[]{childDisjunct});
                }
                return parentConjunct;
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        public boolean criterionRefersToParent() { return false; }
        
        public boolean criterionRefersToLocation() { return true; }
        
        public boolean criterionRefersToAnnotation() { return false; }
        
        public boolean equals(Object o) {
            return
                    (o instanceof ContainedByRichLocation) &&
                    (((ContainedByRichLocation) o).getRichLocation().equals(this.getRichLocation()));
        }
        
        public int hashCode() {
            return getRichLocation().hashCode();
        }
        
        public String toString() {
            return "ContainedBy(" + loc + ")";
        }
    }
    
    /**
     * A filter that returns all features overlapping a location. Overlaps means
     * that a feature includes part of, on the same strand and on the same sequence
     * any single member of the flattened query location.
     *
     * @author Matthew Pocock
     * @author Richard Holland
     * @since 1.5
     */
    public final static class OverlapsRichLocation implements BioSQLFeatureFilter {
        private RichLocation loc;
        private Method eq;
        private Method le;
        private Method ge;
        private Method conjunction;
        private Method disjunction;
        private Method conjunctAdd;
        private Method disjunctAdd;
        
        public RichLocation getRichLocation() {
            return loc;
        }
        
        /**
         * Creates a filter that returns everything overlapping loc.
         *
         * @param loc  the location that will overlap the accepted features
         */
        public OverlapsRichLocation(RichLocation loc) {
            if (loc == null) {
                throw new NullPointerException("Loc may not be null");
            }
            this.loc = loc;
            try {
                // Lazy load the Restrictions class from Hibernate.
                Class restrictions = Class.forName("org.hibernate.criterion.Restrictions");
                // Lookup the methods
                this.eq = restrictions.getMethod("eq", new Class[]{String.class,Object.class});
                this.le = restrictions.getMethod("le", new Class[]{String.class,Object.class});
                this.ge = restrictions.getMethod("ge", new Class[]{String.class,Object.class});
                this.conjunction = restrictions.getMethod("conjunction", new Class[]{});
                this.disjunction = restrictions.getMethod("disjunction", new Class[]{});
                // Lazy load the Restrictions class from Hibernate.
                Class criterion = Class.forName("org.hibernate.criterion.Criterion");
                // Lazy load the Restrictions class from Hibernate.
                Class conjunctClass = Class.forName("org.hibernate.criterion.Conjunction");
                Class disjunctClass = Class.forName("org.hibernate.criterion.Disjunction");
                // Lookup the methods
                this.conjunctAdd = conjunctClass.getMethod("add", new Class[]{criterion});
                this.disjunctAdd = disjunctClass.getMethod("add", new Class[]{criterion});
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        
        /**
         * Returns true if the feature overlaps this filter's location.
         */
        public boolean accept(Feature f) {
            return loc.overlaps(f.getLocation());
        }
        
        public Object asCriterion() {
            try {
                // Conjunction of criteria for each member of the query location.
                Collection members = Tools.flatten(loc);
                // some combo of Tools.flatten(loc), min(loc.start,feat.start) and min(loc.end,feat.end)
                Object parentConjunct = this.conjunction.invoke(null,null);
                for (Iterator i = members.iterator(); i.hasNext(); ) {
                    RichLocation loc = (RichLocation)i.next();
                    Object childDisjunct = this.disjunction.invoke(null,null);
                    // for each member, find features that have start<=member.end,  end>=member.start,
                    // strand=member.strand and crossref=member.crossref
                    this.disjunctAdd.invoke(childDisjunct,new Object[]{this.eq.invoke(null, new Object[]{"l.strandNum",new Integer(loc.getStrand().intValue())})});
                    this.disjunctAdd.invoke(childDisjunct,new Object[]{this.eq.invoke(null, new Object[]{"l.crossRef",loc.getCrossRef()})});
                    this.disjunctAdd.invoke(childDisjunct,new Object[]{this.ge.invoke(null, new Object[]{"l.max",new Integer(loc.getMin())})});
                    this.disjunctAdd.invoke(childDisjunct,new Object[]{this.le.invoke(null, new Object[]{"l.min",new Integer(loc.getMax())})});
                    // add the member to the set of restrictions
                    this.conjunctAdd.invoke(parentConjunct,new Object[]{childDisjunct});
                }
                return parentConjunct;
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        public boolean criterionRefersToParent() { return false; }
        
        public boolean criterionRefersToLocation() { return true; }
        
        public boolean criterionRefersToAnnotation() { return false; }
        
        public boolean equals(Object o) {
            return
                    (o instanceof OverlapsRichLocation) &&
                    (((OverlapsRichLocation) o).getRichLocation().equals(this.getRichLocation()));
        }
        
        public int hashCode() {
            return getRichLocation().hashCode();
        }
        
        public String toString() {
            return "Overlaps(" + loc + ")";
        }
    }
    
    /**
     * A filter that returns all features that have the given note, and
     * the value and rank is checked as well.
     *
     * @author Richard Holland
     * @since 1.5
     */
    public final static class ByNote
            implements BioSQLFeatureFilter {
        private Note note;
        private Method eq;
        private Method conjunction;
        private Method conjunctAdd;
        
        public ByNote(Note note) {
            this.note = note;
            try {
                // Lazy load the Restrictions class from Hibernate.
                Class restrictions = Class.forName("org.hibernate.criterion.Restrictions");
                // Lookup the methods
                this.eq = restrictions.getMethod("eq", new Class[]{String.class,Object.class});
                this.conjunction = restrictions.getMethod("conjunction", new Class[]{});
                // Lazy load the Restrictions class from Hibernate.
                Class criterion = Class.forName("org.hibernate.criterion.Criterion");
                // Lazy load the Restrictions class from Hibernate.
                Class conjunctClass = Class.forName("org.hibernate.criterion.Conjunction");
                // Lookup the methods
                this.conjunctAdd = conjunctClass.getMethod("add", new Class[]{criterion});
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        
        public Note getNote() {
            return note;
        }
        
        public boolean accept(Feature f) {
            if (f instanceof RichFeature) {
                RichAnnotation ra = (RichAnnotation)((RichFeature)f).getAnnotation();
                try {
                    Note n = ra.getNote(note);
                    return (n.getValue()==note.getValue()) || (n.getValue()!=null && note.getValue()!=null && n.getValue().equals(note.getValue()));
                } catch (NoSuchElementException e) {
                    return false;
                }
            } else {
                return false;
            }
        }
        
        public Object asCriterion() {
            try {
                Object conjunct = this.conjunction.invoke(null,null);
                this.conjunctAdd.invoke(conjunct,new Object[]{this.eq.invoke(null, new Object[]{"n.term",note.getTerm()})});
                this.conjunctAdd.invoke(conjunct,new Object[]{this.eq.invoke(null, new Object[]{"n.value",note.getValue()})});
                this.conjunctAdd.invoke(conjunct,new Object[]{this.eq.invoke(null, new Object[]{"n.rank",new Integer(note.getRank())})});
                return conjunct;
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        public boolean criterionRefersToParent() { return false; }
        
        public boolean criterionRefersToLocation() { return false; }
        
        public boolean criterionRefersToAnnotation() { return true; }
        
        public boolean equals(Object o) {
            if(o instanceof ByNote) {
                ByNote that = (ByNote) o;
                return this.getNote() == that.getNote();
            }
            
            return false;
        }
        
        public int hashCode() {
            return getNote().hashCode();
        }
        
        public String toString() {
            return "ByNote {" + note + "}";
        }
    }
    
    /**
     * A filter that returns all features that have the given note. The value
     * is not checked.
     *
     * @author Richard Holland
     * @since 1.5
     */
    public final static class ByNoteTermOnly
            implements BioSQLFeatureFilter {
        private Note note;
        private Method eq;
        
        public ByNoteTermOnly(Note note) {
            this.note = note;
            try {
                // Lazy load the Restrictions class from Hibernate.
                Class restrictions = Class.forName("org.hibernate.criterion.Restrictions");
                // Lookup the methods
                this.eq = restrictions.getMethod("eq", new Class[]{String.class,Object.class});
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        
        public Note getNote() {
            return note;
        }
        
        public boolean accept(Feature f) {
            if (f instanceof RichFeature) {
                return ((RichFeature)f).getNoteSet().contains(note);
            } else {
                return false;
            }
        }
        
        public Object asCriterion() {
            try {
                return this.eq.invoke(null, new Object[]{"n.term",note.getTerm()});
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        public boolean criterionRefersToParent() { return false; }
        
        public boolean criterionRefersToLocation() { return false; }
        
        public boolean criterionRefersToAnnotation() { return true; }
        
        public boolean equals(Object o) {
            if(o instanceof ByNoteTermOnly) {
                ByNoteTermOnly that = (ByNoteTermOnly) o;
                return this.getNote() == that.getNote();
            }
            
            return false;
        }
        
        public int hashCode() {
            return getNote().hashCode();
        }
        
        public String toString() {
            return "ByNoteTermOnly {" + note + "}";
        }
    }
}

