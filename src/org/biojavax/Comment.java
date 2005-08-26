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

package org.biojavax;

import org.biojavax.bio.BioEntry;

/**
 * A simple ranked comment designed to be used for BioEntry comments 
 * in BioSQL. The interface is immutable, the fields are intended to 
 * be set by the constructor.
 * @author Richard Holland
 * @see BioEntry
 */
public interface Comment extends Comparable {
    
    /**
     * Returns the comment part of this comment.
     * @return a comment.
     */
    public String getComment();
    
    /**
     * Returns the rank of this comment.
     * @return the rank.
     */
    public int getRank();
    
}
