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

package org.biojavax.bio.seq;

import java.util.Set;
import org.biojava.bio.seq.Sequence;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.bio.BioEntry;

/**
 * A rich sequence is a combination of a <code>org.biojavax.bio.Bioentry</code>
 * and a <code>org.biojava.seq.Sequence</code>. It inherits and merges the methods
 * of both. The <code>RichSequence</code> is based on the BioSQL model and
 * provides a richer array of methods to access information than <code>Sequence</code>
 * does. The interface introduces no new methods of it's own. It is essentially
 * a <code>BioEntry</code> with sequence information.
 * <p>
 * Whenever possible <code>RichSequence</code> should be used in preference to
 * <code>Sequence</code>
 * @author Mark Schreiber
 */
public interface RichSequence extends BioEntry,Sequence {
    
    public static final ChangeType SYMLISTVERSION = new ChangeType(
            "This sequences's symbollist version has changed",
            "org.biojavax.bio.seq.RichSequence",
            "SYMLISTVERSION"
            );
    
    public static final ChangeType CIRCULAR = new ChangeType(
            "This sequences's circularity has changed",
            "org.biojavax.bio.seq.RichSequence",
            "CIRCULAR"
            );
    
    /**
     * The version of the associated symbol list.
     * @return  the version
     */
    public Double getSeqVersion();
    
    /**
     * Sets the version of the associated symbol list.
     * @param seqVersion the version to set.
     * @throws ChangeVetoException if it doesn't want to change.
     */
    public void setSeqVersion(Double seqVersion) throws ChangeVetoException;
    
    /**
     * The features for this sequence.
     * @return the features.
     */
    public Set getFeatureSet();
    
    /**
     * Sets the features of this sequence. Note that it is not checked to see if
     * the features actually belong to this sequence, you'd best check that yourself
     * and make changes using feature.setParent() if necessary.
     * @param features the features to assign to this sequence, replacing all others.
     * @throws ChangeVetoException if they could not be assigned.
     */
    public void setFeatureSet(Set features) throws ChangeVetoException;
    
    public void setCircular(boolean circular) throws ChangeVetoException;
    
    public boolean getCircular();
}
