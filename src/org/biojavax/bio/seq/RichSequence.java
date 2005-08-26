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

import java.util.Iterator;
import java.util.Set;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.Sequence;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.db.RichObjectFactory;

/**
 * A rich sequence is a combination of a org.biojavax.bio.Bioentry
 * and a Sequence. It inherits and merges the methods
 * of both. The RichSequence is based on the BioSQL model and
 * provides a richer array of methods to access information than Sequence
 * does. Whenever possible RichSequence should be used in preference 
 * to Sequence.
 * @author Mark Schreiber
 * @author Richard Holland
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
     * The version of the associated symbol list. Note the use of an object
     * for the value means that it can be nulled.
     * @return  the version
     */
    public Double getSeqVersion();
    
    /**
     * Sets the version of the associated symbol list. Note the use of an object
     * for the value means that it can be nulled.
     * @param seqVersion the version to set.
     * @throws ChangeVetoException if it doesn't want to change.
     */
    public void setSeqVersion(Double seqVersion) throws ChangeVetoException;
    
    /**
     * The features for this sequence.
     * @return a set of RichFeature objects.
     */
    public Set getFeatureSet();
    
    /**
     * Sets the features of this sequence. Note that it is not checked to see if
     * the features actually belong to this sequence, you'd best check that yourself
     * and make changes using feature.setParent() if necessary.
     * @param features the features to assign to this sequence, replacing all others.
     * Must be a set of RichFeature objects.
     * @throws ChangeVetoException if they could not be assigned.
     */
    public void setFeatureSet(Set features) throws ChangeVetoException;
    
    /**
     * Circularises the sequence. The circular length can then be said to be the
     * length of the sequence itself.
     * @param circular set to true if you want it to be circular
     * @throws ChangeVetoException if the change is blocked. Some implementations
     *   may choose not to support circularisation and should throw an exception here.
     *   Some implementations may only support this method for certain Alphabets.
     */
    public void setCircular(boolean circular) throws ChangeVetoException;
    
    /**
     * Is the sequence circular? Circularity has implications for work with locations
     * and any coordinate work eg symbolAt(int i). 
     * Classes that allow it should test this method when working with coordinates or
     * locations / features.
     * @return true if the this is circular else false.
     */
    public boolean getCircular();
    
    /**
     * Some useful tools for working with RichSequence objects.
     */
    public static class Tools {
        
        // because we are static we don't want any instances
        private Tools() {}
        
        /**
         * Boldly attempts to convert a Sequence into a RichSequence. Sequences 
         * will be assigned to the default namespace. The accession will be 
         * assumed to be the name of the old sequence.
         * The version of the sequence will be set to 0 and the seqversion set
         * to 0.0. Features are converted to RichFeatures.
         * The old Annotation bundle is converted to a RichAnnotation
         * @throws ChangeVetoException if s is locked or the conversion fails.
         */
        public static RichSequence enrich(Sequence s) throws ChangeVetoException {
            if (s instanceof RichSequence) return (RichSequence)s;
            String name = s.getName();
            RichSequence rs = new SimpleRichSequence(
                    RichObjectFactory.getDefaultNamespace(),
                    name==null?"UnknownName":name,
                    name==null?"UnknownAccession":name,
                    0,
                    s,
                    new Double(0.0));
            // Transfer features
            for (Iterator i = s.features(); i.hasNext(); ) {
                Feature f = (Feature)i.next();
                try {
                    rs.createFeature(f.makeTemplate());
                } catch (BioException e) {
                    throw new ChangeVetoException("They hates us!",e);
                }
            }
            // Transfer annotations
            for (Iterator i = s.getAnnotation().keys().iterator(); i.hasNext(); ) {
                Object key = i.next();
                Object value = s.getAnnotation().getProperty(key);
                rs.getAnnotation().setProperty(key,value);
            }
            return rs;
        }
    }
}
