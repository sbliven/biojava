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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.SimpleFeatureHolder;
import org.biojava.utils.ChangeVetoException;


/**
 *
 * @author Richard Holland
 */
public class SimpleRichSequenceFeatureHolder extends SimpleFeatureHolder implements RichSequenceFeatureHolder {
        
    public SimpleRichSequenceFeatureHolder() {
        super();
    }
    
    public void addFeature(Feature f) throws ChangeVetoException {
        
        if (!(f instanceof RichSequenceFeature)) throw new ChangeVetoException("Can only add RichSequenceFeature objects as features");
        
        super.addFeature(f);
        
    }
    
    // Hibernate requirement - not for public use.
    protected Set getFeatureSet() {
        return new HashSet(this.getFeatures());
    }
    
    // Hibernate requirement - not for public use.
    protected void setFeatureSet(Set features) throws ChangeVetoException {
        this.getFeatures().clear();
        for (Iterator i = features.iterator(); i.hasNext(); ) this.addFeature((Feature)i.next());
    }
    
    // Hibernate requirement - not for public use.
    private Long id;
    
    
    // Hibernate requirement - not for public use.
    private Long getId() {
        
        return this.id;
    }
    
    
    // Hibernate requirement - not for public use.
    private void setId(Long id) {
        
        this.id = id;
    }
}
