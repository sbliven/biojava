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

/*
 * ComparableTerm.java
 *
 * Created on July 11, 2005, 10:53 AM
 */

package org.biojavax.ontology;

import org.biojava.ontology.Term;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.Changeable;

/**
 * Makes Term objects comparable properly.
 * @author Richard Holland
 */
public interface ComparableTerm extends Term,Comparable,Changeable {
    
        public static final ChangeType IDENTIFIER = new ChangeType(
            "This term's identifier has changed",
            "org.biojavax.ontology.ComparableTerm",
                "identifier"
            );        
        public static final ChangeType OBSOLETE = new ChangeType(
            "This term's obsolescence has changed",
            "org.biojavax.ontology.ComparableTerm",
                "obsolete"
            );
        
    public String getIdentifier();
    
    public void setIdentifier(String identifier) throws ChangeVetoException;
        
    public boolean getObsolete();
    
    public void setObsolete(boolean obsolete) throws ChangeVetoException;
    
}