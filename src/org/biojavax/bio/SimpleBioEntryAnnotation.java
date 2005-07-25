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

package org.biojavax.bio;

import org.biojava.bio.SimpleAnnotation;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.ontology.ComparableTerm;

/**
 *
 * @author Richard Holland
 */
public class SimpleBioEntryAnnotation extends SimpleAnnotation implements BioEntryAnnotation {
    
    /** Creates a new instance of SimpleBioEntryAnnotation */
    public SimpleBioEntryAnnotation() {
        super();
    }
    
    public void setProperty(Object key, Object value) throws ChangeVetoException {
        
        if (!(key instanceof ComparableTerm)) throw new ChangeVetoException("Can only annotate using ComparableTerm objects as keys");
        
        if (!(value instanceof String)) throw new ChangeVetoException("Can only annotate using single String objects as values");
        
        super.setProperty(key, value);
        
    }
    
}
