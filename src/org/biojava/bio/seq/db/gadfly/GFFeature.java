/**
 * BioJava development code
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

package org.biojava.bio.seq.db.gadfly;

import org.biojava.bio.seq.StrandedFeature;

/**
 * an extension of Feature to implement
 * Gadfly-specific aspects.
 */
public interface GFFeature
    extends StrandedFeature
{
    /**
     * returns the seq_feature id
     * of this Feature.
     */
    public int getID();
}
