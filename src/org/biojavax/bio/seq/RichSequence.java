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
    
    /**
     * Circularises the sequence.
     * @param circular set to true if you want it to be circular
     * @throws ChangeVetoException if the change is blocked. Some implementations
     *   may choose not to support circularisation and should throw an exception here.
     *   Some implementations may only support this method for certain 
     * <code>Alphabet</code>s.
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
    
    public static class Tools {
        private Tools() {}
        
        /**
         * Boldly attempts to convert a <code>Sequence</code> into a <code>
         * RichSequence</code>. Sequences will be assigned to the default name
         * space. The accession will be assumed to be the name of the old sequence.
         * The version of the sequence will be set to 0 and the seqversion set
         * to 0.0. <code>Features</code> are converted to <code>RichFeatures</code>
         * The old <code>Annotation</code> bundle is and converted to a <code>
         * RichAnnotation</code>
         * @throws ChangeVetoException if <code>s</code> is locked or the conversion fails.
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
