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

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * A feature within a sequence, or nested within another feature.
 * <P>
 * Features contain annotation and a location. The type of the feature
 * is something like 'Repeat' or 'BetaStrand'. The source of the feature is
 * something like 'genscan', 'repeatmasker' or 'made-up'.</p>
 *
 * <p>
 * Features are <em>always</em> contained by a parent <code>FeatureHolder</code>,
 * which may either be a <code>Sequence</code> or another <code>Feature</code>. 
 * Feature instances should never be constructed directly by client
 * code, and the BioJava core does not contain any publically accessible
 * implementations of the <code>Feature</code> interface.  Instead, you
 * should create a suitable <code>Feature.Template</code>, then pass this
 * to the <code>createFeature</code> method of a <code>Sequence</code>
 * or <code>Feature</code>.
 * </p>
 *
 * <P>
 * We may need some standardisation for what the fields mean. In particular, we
 * should be compliant where sensible with GFF.
 * </p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */

public interface Feature extends FeatureHolder, Annotatable {
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
     * will be concatinated together in the resulting SymbolList.
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
     * is (ultimately) attached.  For top level features, this will be
     * equal to the <code>FeatureHolder</code> returned by <code>getParent</code>.
     */

    public Sequence getSequence();

    /**
     * Template class for a plain feature.
     * <P>
     * This just has fields for representing the properties of a basic Feature. Each
     * sub-interface should provide a template class that inherits off this, and
     * the constructor or factory methods should make a particular feature
     * implementation from the template.
     */
    public static class Template implements Serializable {
      public Location location;
      public String type;
      public String source;
      public Annotation annotation;
    }
}
