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

package org.biojava.bio.seq.distributed;

import java.util.*;
import java.lang.reflect.*;

import org.biojava.utils.*;
import org.biojava.utils.bytecode.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.seq.projection.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.das.*;

/**
 * Projection for MetaDAS.
 *
 * <p>
 * In this new version, most of the functionality is inherited from the normal
 * ProjectedFeatureHolder
 * </p>
 *
 * @author Thomas Down
 * @since 1.2
 */


class DistProjectedFeatureHolder extends ProjectedFeatureHolder {
    private Annotation annotation;
    private Map componentFeatureCache = new HashMap();
    
    /**
     * Construct a new FeatureHolder which projects a set of features
     * into a new coordinate system.  If <code>translation</code> is 0
     * and <code>oppositeStrand</code> is <code>false</code>, the features
     * are simply reparented without any transformation.
     *
     * @param fh The set of features to project.
     * @param parent The FeatureHolder which is to act as parent
     *               for the projected features.
     */

    public DistProjectedFeatureHolder(FeatureHolder fh,
				                      FeatureHolder parent,
                                      Annotation annotation)
    {
        super(fh, parent, 0, false);
        this.annotation = annotation;
    }
    
    public Feature projectFeature(Feature f) {
	    if (f instanceof ComponentFeature && getParent() instanceof DistributedSequence) {
            ComponentFeature pcf = (ComponentFeature) componentFeatureCache.get(f);
            if (pcf == null) {
                ComponentFeature.Template cft = (ComponentFeature.Template) ((ComponentFeature) f).makeTemplate();
                if (cft.componentSequenceName == null) {
                    cft.componentSequenceName = cft.componentSequence.getName();
                }
                cft.componentSequence = null;    // We need to go back though the DistDB for the
		                                         // proper component sequence to use here.
                if (cft.componentSequenceName == null) {
                    throw new NullPointerException("Can't get component sequence name");
                }

                try {
                    pcf = new DistComponentFeature((DistributedSequence) getParent(),
						                           cft);
                } catch (Exception ex) {
                    throw new BioRuntimeException(ex, "Error instantiating DistComponentFeature");
                }
                componentFeatureCache.put(f, pcf);
            } 
            return pcf;
	    } else {
            // Default: generate a throwaway ProjectedFeature
            
            return super.projectFeature(f);
	    }
	}

	public Annotation getAnnotation(Feature f) {
        if (annotation != null) {
            try {
                MergeAnnotation ma = new MergeAnnotation();
                ma.addAnnotation(f.getAnnotation());
                ma.addAnnotation(annotation);
                return ma;
            } catch (ChangeVetoException cve) {
                throw new BioError(cve);
            }
        } else {
            return f.getAnnotation();
        }
	}
}
