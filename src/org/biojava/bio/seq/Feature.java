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

package org.biojava.bio.seq;

import java.io.Serializable;
import java.util.Comparator;
import java.lang.reflect.*;

import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * A feature within a sequence, or nested within another feature.
 *
 *<p> Features contain annotation and a location. The type of the
 * feature is something like 'Repeat' or 'BetaStrand'. Where the
 * feature has been read from an EMBL or Genbank source the type will
 * be the same as the feature key in the feature table e.g. 'gene',
 * 'CDS', 'repeat_unit', 'misc_feature'. The source of the feature is
 * something like 'genscan', 'repeatmasker' or 'made-up'. </p>
 *
 * <p>
 * Features are <em>always</em> contained by a parent <code>FeatureHolder</code>,
 * which may either be a <code>Sequence</code> or another <code>Feature</code>. 
 * Feature instances should never be constructed directly by client
 * code, and the BioJava core does not contain any publicly accessible
 * implementations of the <code>Feature</code> interface.  Instead, you
 * should create a suitable <code>Feature.Template</code>, then pass this
 * to the <code>createFeature</code> method of a <code>Sequence</code>
 * or <code>Feature</code>.
 * </p>
 *
 * <p>
 * We may need some standardisation for what the fields mean. In particular, we
 * should be compliant where sensible with GFF.
 * </p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 */

public interface Feature extends FeatureHolder, Annotatable {

    /**
     * This is used as a key in the <code>Annotation</code> where it
     * identifies internal data. This is not printed when the
     * <code>Feature</code> is written to a flatfile. E.g. the
     * original feature's EMBL location string (if it has one) is
     * stored here.
     */
    public static final String PROPERTY_DATA_KEY = "internal_data";

    /**
     * The location of this feature.
     * <P>
     * The location may be complicated, or simply a range.
     * The annotation is assumed to apply to all the region contained
     * within the location.
     */
    Location getLocation();
  
    /**
     * The type of the feature.
     *
     * @return the type of this sequence
     */
    String getType();
  
    /**
     * The source of the feature. This may be a program or process.
     *
     * @return the source, or generator
     */
    String getSource();
  
    /**
     * Return a list of symbols that are contained in this feature.
     * <P>
     * The symbols may not be contiguous in the original sequence, but they
     * will be concatenated together in the resulting SymbolList.
     * <P>
     * The order of the Symbols within the resulting symbol list will be 
     * according to the concept of ordering within the location object.
     *
     * @return  a SymbolList containing each symbol of the parent sequence contained
     *          within this feature in the order they appear in the parent
     */
    SymbolList getSymbols();
  
    /**
     * Return the <code>FeatureHolder</code> to which this feature has been
     * attached.  This will be a <code>Sequence</code> object for top level
     * features, and a <code>Feature</code> object for features further
     * down the tree.
     */

    public FeatureHolder getParent();

    /**
     * Return the <code>Sequence</code> object to which this feature
     * is (ultimately) attached. For top level features, this will be
     * equal to the <code>FeatureHolder</code> returned by
     * <code>getParent</code>.
     *
     * @return the ultimate parent Sequence
     */
    public Sequence getSequence();

    /**
     * Iterate over any child features which are held by this
     * feature.  The order of iteration <em>MAY</em> be significant
     * for some types of Feature.
     */

    public Iterator features();

    /**
     * Create a new Template that could be used to generate a feature identical
     * to this one. The fields of the template can be edited without changing
     * the feature.
     *
     * @return a new Template that would make a feature like this one
     */
    public Template makeTemplate();
    
    /**
     * Template class for a plain feature.
     * <P>
     * This just has fields for representing the properties of a basic Feature. Each
     * sub-interface should provide a template class that inherits off this, and
     * the constructor or factory methods should make a particular feature
     * implementation from the template.
     *
     * <p>
     * Equals and Hashcode methods are defined such that two templates
     * are equal if all their fields are equal.  These are implemented
     * by reflection, and automatically pick up any extra fields
     * added in subclasses.
     * </p>
     *
     * @author Thomas Down
     */

    public static class Template implements Serializable {
	public Location location;
	public String type;
	public String source;
	public Annotation annotation;

	public int hashCode() {
	    Class templClazz = getClass();
	    Field[] fields = templClazz.getFields();
	    int hc = 0;
	    for (int i = 0; i < fields.length; ++i) {
		try {
		    Object o = fields[i].get(this);
		    if (o != null) {
			hc += o.hashCode();
		    }
		} catch (Exception ex) {
		    throw new BioError(ex, "Can't access template fields");
		}
	    }
	    
	    return hc;
	}

	public boolean equals(Object b) {
	    Class aClazz = getClass();
	    Class bClazz = b.getClass();
	    if (! aClazz.equals(bClazz)) {
		return false;
	    }

	    Field[] fields = aClazz.getFields();
	    for (int i = 0; i < fields.length; ++i) {
		try {
		    Object ao = fields[i].get(this);
		    Object bo = fields[i].get(b);
		    if (ao != bo) {
			if (ao == null) {
			    return false;
			} else {
			    if (! (ao.equals(bo))) {
				return false;
			    }
			}
		    }
		} catch (Exception ex) {
		    throw new BioError(ex, "Can't access template fields");
		}
	    }
	    
	    return true;
	}
    }

    /**
     * <code>byLocationOrder</code> contains a <code>Feature</code>
     * comparator which compares by the minimum base position of their
     * <code>Location</code>.
     */
    public static final ByLocationComparator byLocationOrder =
        new ByLocationComparator();

    /**
     * <code>byEmblOrder</code> contains a <code>Feature</code>
     * comparator which compares by the minimum base position of their
     * <code>Location</code>, but places <code>Feature</code>s with
     * type "source" first.
     */
    public static final ByEmblOrderComparator byEmblOrder =
        new ByEmblOrderComparator();

    /**
     * <code>ByLocationComparator</code> compares
     * <code>Feature</code>s by the minimum base position of their
     * <code>Location</code>.
     *
     * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
     * @since 1.2
     */
    public static final class ByLocationComparator implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            Feature f1 = (Feature) o1;
            Feature f2 = (Feature) o2;

            // We don't subtract one coordinate from another as one or
            // both may be set to Integer.MAX_VALUE/Integer.MIN_VALUE
            // and the result could wrap around. Convert to Long if
            // necessary.
            if (f1.getLocation().getMin() > f2.getLocation().getMin())
                return 1;
            else if (f1.getLocation().getMin() < f2.getLocation().getMin())
                return -1;
            else
                return 0;
        }
    }

    /**
     * <code>ByEmblOrderComparator</code> compares
     * <code>Feature</code>s by the minimum base position of their
     * <code>Location</code>, but places <code>Feature</code>s with
     * type "source" first.
     *
     * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
     * @since 1.2
     */
    public static final class ByEmblOrderComparator implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            Feature f1 = (Feature) o1;
            Feature f2 = (Feature) o2;

            boolean source1 = f1.getType().equals("source") ? true : false;
            boolean source2 = f2.getType().equals("source") ? true : false;

            // We don't subtract one coordinate from another as one or
            // both may be set to Integer.MAX_VALUE/Integer.MIN_VALUE
            // and the result could wrap around. Convert to Long if
            // necessary.
            if (! source1 && source2)
            {
                return 1;
            }
            else if (source1 && ! source2)
            {
                return -1;
            }
            else if (f1.getLocation().getMin() > f2.getLocation().getMin())
                return 1;
            else if (f1.getLocation().getMin() < f2.getLocation().getMin())
                return -1;
            else
                return 0;
        }
    }
}
