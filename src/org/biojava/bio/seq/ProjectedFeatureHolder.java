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

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * Helper class for projecting Feature objects into an alternative
 * coordinate system.
 *
 * <p>TODO: Projection onto the opposite strand</p>
 *
 * @author Thomas Down
 * @since 1.1
 */

public class ProjectedFeatureHolder extends AbstractFeatureHolder {
    private final FeatureHolder wrapped;
    private final FeatureHolder parent;
    private final int translate;
    private FeatureHolder projectedFeatures;

    /**
     * Construct a new FeatureHolder which projects a set of features
     * into a new coordinate system.
     *
     * @param fh The set of features to project.
     * @param parent The FeatureHolder which is to act as parent
     *               for the projected features.
     * @param translation The translation to apply to map into
     *                    the projected coordinate system.
     */

    public ProjectedFeatureHolder(FeatureHolder fh, 
				  FeatureHolder parent, 
				  int translation) 
    {
	this.wrapped = fh;
	this.parent = parent;
	this.translate = translation;
    }

    protected FeatureHolder getProjectedFeatures() {
	if (projectedFeatures == null) {
	    SimpleFeatureHolder sfh = new SimpleFeatureHolder();
	    for (Iterator i = wrapped.features(); i.hasNext(); ) {
		Feature f = (Feature) i.next();
		Feature wf = null;
		if (f instanceof ComponentFeature) {
		    wf = new ProjectedComponentFeatureWrapper((ComponentFeature) f,
							      parent,
							      translate);
		} else if (f instanceof StrandedFeature) {
		    wf = new ProjectedStrandedFeatureWrapper((StrandedFeature) f,
							     parent,
							     translate);
		} else {
		    wf = new ProjectedFeatureWrapper(f, parent, translate);
		}
    try {
      sfh.addFeature(wf);
    } catch (ChangeVetoException cve) {
      throw new BioError(
        cve,
        "Assertion failure: Should be able to manipulate this FeatureHolder"
      );
    }
	    }
	    projectedFeatures = sfh;
	}
	return projectedFeatures;
    }

    public int countFeatures() {
	return wrapped.countFeatures();
    }

    public Iterator features() {
	return getProjectedFeatures().features();
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	return getProjectedFeatures().filter(ff, recurse);
    }
}
