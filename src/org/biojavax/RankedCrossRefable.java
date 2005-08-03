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
 * RankedCrossRefable.java
 *
 * Created on July 29, 2005, 9:56 AM
 */

package org.biojavax;

import java.util.Set;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.Changeable;

/**
 * Ranked crossrefs.
 * @author Richard Holland
 */
public interface RankedCrossRefable extends Changeable {
    
    public Set getRankedCrossRefs();
    
    public void setRankedCrossRefs(Set crossrefs) throws ChangeVetoException;
    
    public void addRankedCrossRef(RankedCrossRef crossref) throws ChangeVetoException;
    
    public void removeRankedCrossRef(RankedCrossRef crossref) throws ChangeVetoException;
}
