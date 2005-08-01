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
 * RichAnnotatable.java
 *
 * Created on July 28, 2005, 9:58 AM
 */

package org.biojavax;

import java.util.Set;
import org.biojava.bio.Annotatable;
import org.biojava.utils.ChangeVetoException;


/**
 * Annotatable objects that can have rich annotations.
 * @author Richard Holland
 */
public interface RichAnnotatable extends Annotatable {
    
    /**
     * Returns the set of notes associated with this object. Would normally
     * delegate call to internal RichAnnotation instance.
     * @return set the set of notes.
     */
    public Set getNoteSet();
    
    /**
     * Clears the notes associated with this object and replaces them with
     * the contents of this set. Would normally delegate call to internal
     * RichAnnotation instance.
     * @param set the set of notes to replace the existing ones with.
     * @throws ChangeVetoException if it doesn't like them.
     */
    public void setNoteSet(Set notes) throws ChangeVetoException;
    
}
