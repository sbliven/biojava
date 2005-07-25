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
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.SimpleFeatureHolder;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.utils.ChangeVetoException;


/**
 *
 * @author Richard Holland
 */
public class SimpleRichSequenceFeatureHolder extends SimpleFeatureHolder implements RichSequenceFeatureHolder {
    
    private final RichSequence yomama;
    
    public SimpleRichSequenceFeatureHolder(RichSequence yomama) {
        super();
        this.yomama = yomama;
    }
        
    public Feature createFeature(Feature.Template f) throws ChangeVetoException {
        
        StrandedFeature.Template sft;
        
        if (f instanceof StrandedFeature.Template) {
            
            sft = (StrandedFeature.Template)f;
            
        } else {
            
            sft = new StrandedFeature.Template();
            
            sft.annotation = f.annotation;
            
            sft.location = f.location;
            
            sft.source = f.source;
            
            sft.sourceTerm = f.sourceTerm;
            
            sft.type = f.type;
            
            sft.typeTerm = f.typeTerm;
            
            sft.strand = StrandedFeature.UNKNOWN;
            
        }
        
        RichSequenceFeature bef = new SimpleRichSequenceFeature(this.yomama,this,sft);
        
        // make the feature a singleton
        
        if (this.containsFeature(bef)) for (Iterator i = this.features(); i.hasNext(); ) {
            
            RichSequenceFeature bef2 = (RichSequenceFeature)i.next();
            
            if (bef.equals(bef2)) return bef2;
            
        }
        
        // or create a new feature
        
        this.addFeature(bef);
        
        return bef;
        
    }
    
    public void addFeature(Feature f) throws ChangeVetoException {
        
        if (!(f instanceof RichSequenceFeature)) throw new ChangeVetoException("Can only add BioEntryFeature objects as features");
        
        super.addFeature(f);
        
    }
    
    
}
